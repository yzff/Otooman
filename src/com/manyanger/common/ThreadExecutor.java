package com.manyanger.common;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import com.manyanger.common.ThreadPool.BlockingStack;
import com.manyanger.common.ThreadPool.SkyThreadFactory;

public class ThreadExecutor extends AbstractExecutorService
{

    private final AtomicInteger mCtl = new AtomicInteger(ctlOf(RUNNING, 0));

    private static final int COUNT_BITS = Integer.SIZE - 3;

    private static final int CAPACITY = (1 << COUNT_BITS) - 1;

    // runState is stored in the high-order bits
    private static final int RUNNING = -1 << COUNT_BITS;

    private static final int SHUTDOWN = 0 << COUNT_BITS;

    private static final int STOP = 1 << COUNT_BITS;

    private static final int TIDYING = 2 << COUNT_BITS;

    private static final int TERMINATED = 3 << COUNT_BITS;

    // Packing and unpacking ctl
    private static int runStateOf(int c)
    {
        return c & ~CAPACITY;
    }

    private static int workerCountOf(int c)
    {
        return c & CAPACITY;
    }

    private static int ctlOf(int rs, int wc)
    {
        return rs | wc;
    }

    private static boolean runStateLessThan(int c, int s)
    {
        return c < s;
    }

    private static boolean runStateAtLeast(int c, int s)
    {
        return c >= s;
    }

    private static boolean isRunning(int c)
    {
        return c < SHUTDOWN;
    }

    private boolean compareAndIncrementWorkerCount(int expect)
    {
        return mCtl.compareAndSet(expect, expect + 1);
    }

    private boolean compareAndDecrementWorkerCount(int expect)
    {
        return mCtl.compareAndSet(expect, expect - 1);
    }

    private void decrementWorkerCount()
    {
        do
        {
        }
        while (!compareAndDecrementWorkerCount(mCtl.get()));
    }

    private final BlockingQueue<Runnable> mWorkQueue;

    private final ReentrantLock mMainLock = new ReentrantLock();

    private final HashSet<Worker> mWorkers = new HashSet<Worker>();

    private final Condition mTermination = mMainLock.newCondition();

    private int mLargestPoolSize;

    private long mCompletedTaskCount;

    private volatile ThreadFactory mThreadFactory;

    private volatile RejectedTaskHandler mRejectHandler;

    private volatile long keepAliveTime;

    private volatile boolean allowCoreThreadTimeOut;

    private volatile int corePoolSize;

    private volatile int maximumPoolSize;

    private static final RuntimePermission shutdownPerm =
        new RuntimePermission("modifyThread");

    private final class Worker extends AbstractQueuedSynchronizer implements
        Runnable
    {

        private static final long serialVersionUID = 6138294804551838833L;

        final Thread thread;

        Runnable firstTask;

        Runnable curTask;

        boolean mStop;

        volatile long completedTasks;

        Worker(Runnable firstTask)
        {
            this.firstTask = firstTask;
            thread = mThreadFactory.newThread(this);
        }

        @Override
        public void run()
        {
            runWorker(this);
        }

        @Override
        protected boolean isHeldExclusively()
        {
            return getState() == 1;
        }

        @Override
        protected boolean tryAcquire(int unused)
        {
            if (compareAndSetState(0, 1))
            {
                //setExclusiveOwnerThread(Thread.currentThread());
                return true;
            }
            return false;
        }

        @Override
        protected boolean tryRelease(int unused)
        {
           // setExclusiveOwnerThread(null);
            setState(0);
            return true;
        }

        public void lock()
        {
            acquire(1);
        }

        public boolean tryLock()
        {
            return tryAcquire(1);
        }

        public void unlock()
        {
            release(1);
        }

        public boolean isLocked()
        {
            return isHeldExclusively();
        }
    }

    private void advanceRunState(int targetState)
    {
        for (;;)
        {
            int c = mCtl.get();
            if (runStateAtLeast(c, targetState)
                || mCtl.compareAndSet(c, ctlOf(targetState, workerCountOf(c))))
            {
                break;
            }
        }
    }

    final void tryTerminate()
    {
        for (;;)
        {
            int c = mCtl.get();
            if (isRunning(c) || runStateAtLeast(c, TIDYING)
                || (runStateOf(c) == SHUTDOWN && !mWorkQueue.isEmpty()))
            {
                return;
            }
            if (workerCountOf(c) != 0)
            { // Eligible to terminate
                interruptIdleWorkers(ONLY_ONE);
                return;
            }

            final ReentrantLock mainLock = mMainLock;
            mainLock.lock();
            try
            {
                if (mCtl.compareAndSet(c, ctlOf(TIDYING, 0)))
                {
                    try
                    {
                        terminated();
                    }
                    finally
                    {
                        mCtl.set(ctlOf(TERMINATED, 0));
                        mTermination.signalAll();
                    }
                    return;
                }
            }
            finally
            {
                mainLock.unlock();
            }
            // else retry on failed CAS
        }
    }

    private void checkShutdownAccess()
    {
        SecurityManager security = System.getSecurityManager();
        if (security != null)
        {
            security.checkPermission(shutdownPerm);
            final ReentrantLock mainLock = mMainLock;
            mainLock.lock();
            try
            {
                for (Worker w : mWorkers)
                {
                    security.checkAccess(w.thread);
                }
            }
            finally
            {
                mainLock.unlock();
            }
        }
    }

    private void interruptWorkers()
    {
        final ReentrantLock mainLock = mMainLock;
        mainLock.lock();
        try
        {
            for (Worker w : mWorkers)
            {
                try
                {
                    w.thread.interrupt();
                }
                catch (SecurityException ignore)
                {
                }
            }
        }
        finally
        {
            mainLock.unlock();
        }
    }

    private void interruptIdleWorkers(boolean onlyOne)
    {
        final ReentrantLock mainLock = mMainLock;
        mainLock.lock();
        try
        {
            for (Worker w : mWorkers)
            {
                Thread t = w.thread;
                if (!t.isInterrupted() && w.tryLock())
                {
                    try
                    {
                        t.interrupt();
                    }
                    catch (SecurityException ignore)
                    {
                    }
                    finally
                    {
                        w.unlock();
                    }
                }
                if (onlyOne)
                {
                    break;
                }
            }
        }
        finally
        {
            mainLock.unlock();
        }
    }

    private List<Runnable> interruptIdleWorkers(int size)
    {
        final ReentrantLock mainLock = mMainLock;
        mainLock.lock();
        try
        {
            List<Runnable> list = new ArrayList<Runnable>();
            int i = 0;
            for (Worker w : mWorkers)
            {
                if (i == size)
                {
                    break;
                }
                Thread t = w.thread;
                i++;
                if (!t.isInterrupted() && w.tryLock())
                {
                    try
                    {
                        t.interrupt();
                    }
                    catch (SecurityException ignore)
                    {
                    }
                    finally
                    {
                        w.unlock();
                    }
                }
                else
                {
                    w.mStop = true;
                    list.add(w.curTask);
                    compareAndDecrementWorkerCount(mCtl.get());
                }
            }
            return list;
        }
        finally
        {
            mainLock.unlock();
        }
    }

    private void interruptIdleWorkers()
    {
        interruptIdleWorkers(false);
    }

    private static final boolean ONLY_ONE = true;

    private void clearInterruptsForTaskRun()
    {
        if (runStateLessThan(mCtl.get(), STOP) && Thread.interrupted()
            && runStateAtLeast(mCtl.get(), STOP))
        {
            Thread.currentThread().interrupt();
        }
    }

    final void reject(Runnable command)
    {
        mRejectHandler.rejectedExecution(command, this);
    }

    void onShutdown()
    {
    }

    final boolean isRunningOrShutdown(boolean shutdownOK)
    {
        int rs = runStateOf(mCtl.get());
        return rs == RUNNING || (rs == SHUTDOWN && shutdownOK);
    }

    private List<Runnable> drainQueue()
    {
        BlockingQueue<Runnable> q = mWorkQueue;
        List<Runnable> taskList = new ArrayList<Runnable>();
        q.drainTo(taskList);
        if (!q.isEmpty())
        {
            for (Runnable r : q.toArray(new Runnable[0]))
            {
                if (q.remove(r))
                {
                    taskList.add(r);
                }
            }
        }
        return taskList;
    }

    private boolean addWorker(Runnable firstTask, boolean core)
    {
        retry: for (;;)
        {
            int c = mCtl.get();
            int rs = runStateOf(c);

            // Check if queue empty only if necessary.
            if (rs >= SHUTDOWN
                && !(rs == SHUTDOWN && firstTask == null && !mWorkQueue
                    .isEmpty()))
            {
                return false;
            }

            for (;;)
            {
                int wc = workerCountOf(c);
                if (wc >= CAPACITY
                    || wc >= (core ? corePoolSize : maximumPoolSize))
                {
                    return false;
                }
                if (compareAndIncrementWorkerCount(c))
                {
                    break retry;
                }
                c = mCtl.get(); // Re-read ctl
                if (runStateOf(c) != rs)
                {
                    continue retry;
                    // else CAS failed due to workerCount change; retry inner loop
                }
            }
        }

        Worker w = new Worker(firstTask);
        Thread t = w.thread;

        final ReentrantLock mainLock = mMainLock;
        mainLock.lock();
        try
        {
            // Recheck while holding lock.
            // Back out on ThreadFactory failure or if
            // shut down before lock acquired.
            int c = mCtl.get();
            int rs = runStateOf(c);

            if (t == null
                || (rs >= SHUTDOWN && !(rs == SHUTDOWN && firstTask == null)))
            {
                decrementWorkerCount();
                tryTerminate();
                return false;
            }

            mWorkers.add(w);

            int s = mWorkers.size();
            if (s > mLargestPoolSize)
            {
                mLargestPoolSize = s;
            }
        }
        finally
        {
            mainLock.unlock();
        }

        t.start();
        // It is possible (but unlikely) for a thread to have been
        // added to workers, but not yet started, during transition to
        // STOP, which could result in a rare missed interrupt,
        // because Thread.interrupt is not guaranteed to have any effect
        // on a non-yet-started Thread (see Thread#interrupt).
        if (runStateOf(mCtl.get()) == STOP && !t.isInterrupted())
        {
            t.interrupt();
        }

        return true;
    }

    private void processWorkerExit(Worker w, boolean completedAbruptly)
    {
        if (completedAbruptly)
        {
            decrementWorkerCount();
        }

        final ReentrantLock mainLock = mMainLock;
        mainLock.lock();
        try
        {
            mCompletedTaskCount += w.completedTasks;
            mWorkers.remove(w);
        }
        finally
        {
            mainLock.unlock();
        }

        tryTerminate();

        int c = mCtl.get();
        if (runStateLessThan(c, STOP))
        {
            if (!completedAbruptly)
            {
                int min = allowCoreThreadTimeOut ? 0 : corePoolSize;
                if (min == 0 && !mWorkQueue.isEmpty())
                {
                    min = 1;
                }
                if (workerCountOf(c) >= min)
                {
                    return; // replacement not needed
                }
            }
            addWorker(null, false);
        }
    }

    private Runnable getTask()
    {
        boolean timedOut = false; // Did the last poll() time out?

        retry: for (;;)
        {
            int c = mCtl.get();
            int rs = runStateOf(c);

            // Check if queue empty only if necessary.
            if (rs >= SHUTDOWN && (rs >= STOP || mWorkQueue.isEmpty()))
            {
                decrementWorkerCount();
                return null;
            }

            boolean timed; // Are workers subject to culling?

            for (;;)
            {
                int wc = workerCountOf(c);
                timed = allowCoreThreadTimeOut || wc > corePoolSize;

                if (wc <= maximumPoolSize && !(timedOut && timed))
                {
                    break;
                }
                if (compareAndDecrementWorkerCount(c))
                {
                    return null;
                }
                c = mCtl.get(); // Re-read ctl
                if (runStateOf(c) != rs)
                {
                    continue retry;
                    // else CAS failed due to workerCount change; retry inner loop
                }
            }

            try
            {
                Runnable r =
                    timed ? mWorkQueue
                        .poll(keepAliveTime, TimeUnit.NANOSECONDS) : mWorkQueue
                        .take();
                if (r != null)
                {
                    return r;
                }
                timedOut = true;
            }
            catch (InterruptedException retry)
            {
                timedOut = false;
            }
        }
    }

    final void runWorker(Worker w)
    {
        Runnable task = w.firstTask;
        w.firstTask = null;
        boolean completedAbruptly = true;
        try
        {
            while (task != null || (task = getTask()) != null)
            {
                w.lock();
                w.curTask = task;
                clearInterruptsForTaskRun();
                try
                {
                    beforeExecute(w.thread, task);
                    Throwable thrown = null;
                    try
                    {
                        task.run();
                    }
                    catch (RuntimeException x)
                    {
                        thrown = x;
                        throw x;
                    }
                    catch (Error x)
                    {
                        thrown = x;
                        throw x;
                    }
                    catch (Throwable x)
                    {
                        thrown = x;
                        throw new Error(x);
                    }
                    finally
                    {
                        afterExecute(task, thrown);
                    }
                }
                finally
                {
                    task = null;
                    w.completedTasks++;
                    w.unlock();
                    if (w.mStop)
                    {
                        break;
                    }
                }
            }
            completedAbruptly = false;
        }
        finally
        {
            w.mStop = false;
            processWorkerExit(w, completedAbruptly);
        }
    }

    public interface RejectedTaskHandler
    {
        void rejectedExecution(Runnable r, ThreadExecutor e);
    }

    public ThreadExecutor(int corePoolSize, int maximumPoolSize,
        long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue,
        ThreadFactory threadFactory, RejectedTaskHandler handler)
    {
        if (corePoolSize < 0 || maximumPoolSize <= 0
            || maximumPoolSize < corePoolSize || keepAliveTime < 0)
        {
            throw new IllegalArgumentException();
        }
        mWorkQueue =
            workQueue == null ? new BlockingStack<Runnable>() : workQueue;
        mThreadFactory =
            threadFactory == null ? new SkyThreadFactory(Thread.NORM_PRIORITY)
                : threadFactory;
        mRejectHandler = handler == null ? new DefaultAbortPolicy() : handler;
        this.corePoolSize = corePoolSize;
        this.maximumPoolSize = maximumPoolSize;
        this.keepAliveTime = unit.toNanos(keepAliveTime);
    }

    @Override
    public void execute(Runnable command)
    {
        if (command == null)
        {
            throw new NullPointerException();
        }
        int c = mCtl.get();
        if (workerCountOf(c) < corePoolSize)
        {
            if (addWorker(command, true))
            {
                return;
            }
            c = mCtl.get();
        }
        if (isRunning(c) && mWorkQueue.offer(command))
        {
            int recheck = mCtl.get();
            if (!isRunning(recheck) && remove(command))
            {
                reject(command);
            }
            else if (workerCountOf(recheck) == 0)
            {
                addWorker(null, false);
            }
        }
        else if (!addWorker(command, false))
        {
            reject(command);
        }
    }

    @Override
    public void shutdown()
    {
        final ReentrantLock mainLock = mMainLock;
        mainLock.lock();
        try
        {
            checkShutdownAccess();
            advanceRunState(SHUTDOWN);
            interruptIdleWorkers();
            onShutdown(); // hook for ScheduledThreadPoolExecutor
        }
        finally
        {
            mainLock.unlock();
        }
        tryTerminate();
    }

    @Override
    public List<Runnable> shutdownNow()
    {
        List<Runnable> tasks;
        final ReentrantLock mainLock = mMainLock;
        mainLock.lock();
        try
        {
            checkShutdownAccess();
            advanceRunState(STOP);
            interruptWorkers();
            tasks = drainQueue();
        }
        finally
        {
            mainLock.unlock();
        }
        tryTerminate();
        return tasks;
    }

    @Override
    public boolean isShutdown()
    {
        return !isRunning(mCtl.get());
    }

    public boolean isTerminating()
    {
        int c = mCtl.get();
        return !isRunning(c) && runStateLessThan(c, TERMINATED);
    }

    @Override
    public boolean isTerminated()
    {
        return runStateAtLeast(mCtl.get(), TERMINATED);
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit)
        throws InterruptedException
    {
        long nanos = unit.toNanos(timeout);
        final ReentrantLock mainLock = mMainLock;
        mainLock.lock();
        try
        {
            for (;;)
            {
                if (runStateAtLeast(mCtl.get(), TERMINATED))
                {
                    return true;
                }
                if (nanos <= 0)
                {
                    return false;
                }
                nanos = mTermination.awaitNanos(nanos);
            }
        }
        finally
        {
            mainLock.unlock();
        }
    }

    @Override
    protected void finalize()
    {
        try
        {
            shutdown();
        }
        finally
        {
            try
            {
                super.finalize();
            }
            catch (Throwable t)
            {
                throw new AssertionError(t);
            }
        }
    }

    public List<Runnable> setCorePoolSize(int corePoolSize)
    {
        if (corePoolSize < 0)
        {
            throw new IllegalArgumentException();
        }
        int delta = corePoolSize - this.corePoolSize;
        this.corePoolSize = corePoolSize;
        if (workerCountOf(mCtl.get()) > corePoolSize)
        {
            return interruptIdleWorkers(Math.abs(delta));
        }
        else if (delta > 0)
        {
            int k = Math.min(delta, mWorkQueue.size());
            while (k-- > 0 && addWorker(null, true))
            {
                if (mWorkQueue.isEmpty())
                {
                    break;
                }
            }
        }
        return null;
    }

    public int getCorePoolSize()
    {
        return corePoolSize;
    }

    public boolean prestartCoreThread()
    {
        return workerCountOf(mCtl.get()) < corePoolSize
            && addWorker(null, true);
    }

    public int prestartAllCoreThreads()
    {
        int n = 0;
        while (addWorker(null, true))
        {
            ++n;
        }
        return n;
    }

    public boolean allowsCoreThreadTimeOut()
    {
        return allowCoreThreadTimeOut;
    }

    public void allowCoreThreadTimeOut(boolean value)
    {
        if (value && keepAliveTime <= 0)
        {
            throw new IllegalArgumentException(
                "Core threads must have nonzero keep alive times");
        }
        if (value != allowCoreThreadTimeOut)
        {
            allowCoreThreadTimeOut = value;
            if (value)
            {
                interruptIdleWorkers();
            }
        }
    }

    public void setMaximumPoolSize(int maximumPoolSize)
    {
        if (maximumPoolSize <= 0 || maximumPoolSize < corePoolSize)
        {
            throw new IllegalArgumentException();
        }
        this.maximumPoolSize = maximumPoolSize;
        if (workerCountOf(mCtl.get()) > maximumPoolSize)
        {
            interruptIdleWorkers();
        }
    }

    public int getMaximumPoolSize()
    {
        return maximumPoolSize;
    }

    public void setKeepAliveTime(long time, TimeUnit unit)
    {
        if (time < 0)
        {
            throw new IllegalArgumentException();
        }
        if (time == 0 && allowsCoreThreadTimeOut())
        {
            throw new IllegalArgumentException(
                "Core threads must have nonzero keep alive times");
        }
        long keepAliveTime = unit.toNanos(time);
        long delta = keepAliveTime - this.keepAliveTime;
        this.keepAliveTime = keepAliveTime;
        if (delta < 0)
        {
            interruptIdleWorkers();
        }
    }

    public long getKeepAliveTime(TimeUnit unit)
    {
        return unit.convert(keepAliveTime, TimeUnit.NANOSECONDS);
    }

    public BlockingQueue<Runnable> getQueue()
    {
        return mWorkQueue;
    }

    public boolean remove(Runnable task)
    {
        boolean removed = mWorkQueue.remove(task);
        tryTerminate(); // In case SHUTDOWN and now empty
        return removed;
    }

    public void purge()
    {
        final BlockingQueue<Runnable> q = mWorkQueue;
        try
        {
            Iterator<Runnable> it = q.iterator();
            while (it.hasNext())
            {
                Runnable r = it.next();
                if (r instanceof Future<?> && ((Future<?>) r).isCancelled())
                {
                    it.remove();
                }
            }
        }
        catch (ConcurrentModificationException fallThrough)
        {
            // Take slow path if we encounter interference during traversal.
            // Make copy for traversal and call remove for cancelled entries.
            // The slow path is more likely to be O(N*N).
            for (Object r : q.toArray())
            {
                if (r instanceof Future<?> && ((Future<?>) r).isCancelled())
                {
                    q.remove(r);
                }
            }
        }

        tryTerminate(); // In case SHUTDOWN and now empty
    }

    public int getPoolSize()
    {
        final ReentrantLock mainLock = mMainLock;
        mainLock.lock();
        try
        {
            // Remove rare and surprising possibility of
            // isTerminated() && getPoolSize() > 0
            return runStateAtLeast(mCtl.get(), TIDYING) ? 0 : mWorkers.size();
        }
        finally
        {
            mainLock.unlock();
        }
    }

    public int getActiveCount()
    {
        final ReentrantLock mainLock = mMainLock;
        mainLock.lock();
        try
        {
            int n = 0;
            for (Worker w : mWorkers)
            {
                if (w.isLocked())
                {
                    ++n;
                }
            }
            return n;
        }
        finally
        {
            mainLock.unlock();
        }
    }

    public int getLargestPoolSize()
    {
        final ReentrantLock mainLock = mMainLock;
        mainLock.lock();
        try
        {
            return mLargestPoolSize;
        }
        finally
        {
            mainLock.unlock();
        }
    }

    public long getTaskCount()
    {
        final ReentrantLock mainLock = mMainLock;
        mainLock.lock();
        try
        {
            long n = mCompletedTaskCount;
            for (Worker w : mWorkers)
            {
                n += w.completedTasks;
                if (w.isLocked())
                {
                    ++n;
                }
            }
            return n + mWorkQueue.size();
        }
        finally
        {
            mainLock.unlock();
        }
    }

    public long getCompletedTaskCount()
    {
        final ReentrantLock mainLock = mMainLock;
        mainLock.lock();
        try
        {
            long n = mCompletedTaskCount;
            for (Worker w : mWorkers)
            {
                n += w.completedTasks;
            }
            return n;
        }
        finally
        {
            mainLock.unlock();
        }
    }

    protected void beforeExecute(Thread t, Runnable r)
    {
    }

    protected void afterExecute(Runnable r, Throwable t)
    {
    }

    protected void terminated()
    {
    }

    public static class DefaultAbortPolicy implements RejectedTaskHandler
    {

        public DefaultAbortPolicy()
        {
        }

        @Override
        public void rejectedExecution(Runnable r, ThreadExecutor e)
        {
            int queueSize = e.getQueue().size();
            int remainingCapacity = e.getQueue().remainingCapacity();
            String message =
                "pool=" + e.getPoolSize() + "/" + e.maximumPoolSize
                    + ", queue=" + queueSize;
            if (remainingCapacity != Integer.MAX_VALUE)
            {
                message += "/" + (queueSize + remainingCapacity);
            }
            throw new RejectedExecutionException(message);
        }
    }

}
