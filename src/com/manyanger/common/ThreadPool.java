package com.manyanger.common;

import android.os.Handler;

import com.manyanger.data.net.HttpService.HttpThread;

import java.util.AbstractQueue;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;


public class ThreadPool
{

    private static Handler mTextHandler;

    private static ThreadExecutor iconService;

    private static ThreadExecutor textService;

    private static ThreadExecutor viewService;

    private static ThreadExecutor newFixedThreadPool(int nThreads, int priority)
    {
        return new ThreadExecutor(nThreads, nThreads, 0L,
            TimeUnit.MILLISECONDS, new BlockingStack<Runnable>(),
            new SkyThreadFactory(priority), null);
    }

//    public static ThreadExecutor newCachedThreadPool(int priority)
//    {
//        return new ThreadExecutor(SettingData.getDownloadTaskCount(), 3, 60L,
//            TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(),
//            new SkyThreadFactory(priority), null);
//    }

    public static int getIconServiceQueueSize()
    {
        try
        {
            return iconService.getQueue().size();
        }
        catch (Exception e)
        {
            return 0;
        }

    }

    public static int getViewServiceQueueSize()
    {
        try
        {
            return viewService.getQueue().size();
        }
        catch (Exception e)
        {
            return 0;
        }

    }

    public static void submitText(Runnable runnable)
    {

        if (textService == null)
        {
            textService = newFixedThreadPool(2, 5);
        }
        textService.submit(runnable);
    }



    public static void submit(Runnable runnable)
    {
        if (iconService == null)
        {
            iconService = newFixedThreadPool(2, 4);
        }
        iconService.execute(runnable);
    }

    
    public static void submitPreview(Runnable runnable)
    {
        if (viewService == null)
        {
            viewService = newFixedThreadPool(2, 4);
        }
        viewService.execute(runnable);
    }

    private ThreadPool()
    {

    }

    public static void destory()
    {
        if (iconService != null)
        {
            iconService.shutdownNow();
            iconService = null;
        }

        if (textService != null)
        {
            textService.shutdownNow();
            textService = null;
        }

        if (mTextHandler != null)
        {
            mTextHandler.getLooper().quit();
            mTextHandler = null;
        }
        
        if (viewService != null)
        {
            viewService.shutdownNow();
            viewService = null;
        }

    }

    static class SkyThreadFactory implements ThreadFactory
    {

        private static final AtomicInteger poolNumber = new AtomicInteger(1);

        private final ThreadGroup group;

        private final AtomicInteger threadNumber = new AtomicInteger(1);

        private final String namePrefix;

        private int priority;

        public SkyThreadFactory(int priority)
        {
            SecurityManager s = System.getSecurityManager();
            group =
                (s != null) ? s.getThreadGroup() : Thread.currentThread()
                    .getThreadGroup();
            namePrefix = "pool-" + poolNumber.getAndIncrement() + "-thread-";
            this.priority = priority;
        }

        @Override
        public Thread newThread(Runnable r)
        {
            HttpThread t =
                new HttpThread(group, r, namePrefix
                    + threadNumber.getAndIncrement(), 0);
            if (t.isDaemon())
            {
                t.setDaemon(false);
            }
            if (priority > Thread.MAX_PRIORITY
                || priority < Thread.MIN_PRIORITY)
            {
                priority = Thread.NORM_PRIORITY;
            }
            t.setPriority(priority);
            return t;
        }

    }

    static class BlockingStack<E> extends AbstractQueue<E> implements
        BlockingQueue<E>
    {

        static class Node<E>
        {
            E item;

            Node<E> next;

            Node(E x)
            {
                item = x;
            }
        }

        private final int capacity;

        private final AtomicInteger count = new AtomicInteger(0);

        private transient Node<E> head;

        private transient Node<E> last;

        /** Lock held by take, poll, etc */
        private final ReentrantLock takeLock = new ReentrantLock();

        /** Wait queue for waiting takes */
        private final Condition notEmpty = takeLock.newCondition();

        /** Lock held by put, offer, etc */
        private final ReentrantLock putLock = new ReentrantLock();

        /** Wait queue for waiting puts */
        private final Condition notFull = putLock.newCondition();

        /**
         * Signals a waiting take. Called only from put/offer (which do not
         * otherwise ordinarily lock takeLock.)
         */
        private void signalNotEmpty()
        {
            final ReentrantLock takeLock = this.takeLock;
            takeLock.lock();
            try
            {
                notEmpty.signal();
            }
            finally
            {
                takeLock.unlock();
            }
        }

        /**
         * Signals a waiting put. Called only from take/poll.
         */
        private void signalNotFull()
        {
            final ReentrantLock putLock = this.putLock;
            putLock.lock();
            try
            {
                notFull.signal();
            }
            finally
            {
                putLock.unlock();
            }
        }

        /**
         * Creates a node and links it at end of queue.
         * @param x
         *            the item
         */
        private void enqueue(E x)
        {
            last = last.next = new Node<E>(x);
        }

        /**
         * Creates a node and links it at head of queue
         * Method: prequeue
         * <p>Author: Nick.Zhang
         * <p>Description:
         * <p>Modified: 2012-3-19
         * @param x
         */
        //        private void headqueue(E x)
        //        {
        //            Node<E> h = head;
        //            Node<E> first = h.next;
        //            Node<E> n = new Node<E>(x);
        //            n.next = first;
        //            head.next = n;
        //        }

        /**
         * Removes a node from head of queue.
         * @return the node
         */
        private E dequeue()
        {
            Node<E> h = head;
            Node<E> first = h.next;
            h.next = h; // help GC
            head = first;
            E x = first.item;
            first.item = null;
            return x;
        }

        /**
         * Lock to prevent both puts and takes.
         */
        void fullyLock()
        {
            putLock.lock();
            takeLock.lock();
        }

        /**
         * Unlock to allow both puts and takes.
         */
        void fullyUnlock()
        {
            takeLock.unlock();
            putLock.unlock();
        }

        public BlockingStack()
        {
            this(Integer.MAX_VALUE);
        }

        public BlockingStack(int capacity)
        {
            if (capacity <= 0)
            {
                throw new IllegalArgumentException();
            }
            this.capacity = capacity;
            last = head = new Node<E>(null);
        }

        @Override
        public int size()
        {
            return count.get();
        }

        @Override
        public int remainingCapacity()
        {
            return capacity - count.get();
        }

        @Override
        public void put(E e) throws InterruptedException
        {
            if (e == null)
            {
                throw new NullPointerException();
            }
            int c = -1;
            final ReentrantLock putLock = this.putLock;
            final AtomicInteger count = this.count;
            putLock.lockInterruptibly();
            try
            {
                while (count.get() == capacity)
                {
                    notFull.await();
                }
                enqueue(e);
                c = count.getAndIncrement();
                if (c + 1 < capacity)
                {
                    notFull.signal();
                }
            }
            finally
            {
                putLock.unlock();
            }
            if (c == 0)
            {
                signalNotEmpty();
            }
        }

        @Override
        public boolean offer(E e, long timeout, TimeUnit unit)
            throws InterruptedException
        {

            if (e == null)
            {
                throw new NullPointerException();
            }
            long nanos = unit.toNanos(timeout);
            int c = -1;
            final ReentrantLock putLock = this.putLock;
            final AtomicInteger count = this.count;
            putLock.lockInterruptibly();
            try
            {
                while (count.get() == capacity)
                {
                    if (nanos <= 0)
                    {
                        return false;
                    }
                    nanos = notFull.awaitNanos(nanos);
                }
                enqueue(e);
                c = count.getAndIncrement();
                if (c + 1 < capacity)
                {
                    notFull.signal();
                }
            }
            finally
            {
                putLock.unlock();
            }
            if (c == 0)
            {
                signalNotEmpty();
            }
            return true;
        }

        @Override
        public boolean offer(E e)
        {
            if (e == null)
            {
                throw new NullPointerException();
            }
            final AtomicInteger count = this.count;
            if (count.get() == capacity)
            {
                return false;
            }
            int c = -1;
            final ReentrantLock putLock = this.putLock;
            putLock.lock();
            try
            {
                if (count.get() < capacity)
                {
                    enqueue(e);
                    c = count.getAndIncrement();
                    if (c + 1 < capacity)
                    {
                        notFull.signal();
                    }
                }
            }
            finally
            {
                putLock.unlock();
            }
            if (c == 0)
            {
                signalNotEmpty();
            }
            return c >= 0;
        }

        @Override
        public E take() throws InterruptedException
        {
            E x;
            int c = -1;
            final AtomicInteger count = this.count;
            final ReentrantLock takeLock = this.takeLock;
            takeLock.lockInterruptibly();
            try
            {
                while (count.get() == 0)
                {
                    notEmpty.await();
                }
                x = dequeue();
                c = count.getAndDecrement();
                if (c > 1)
                {
                    notEmpty.signal();
                }
            }
            finally
            {
                takeLock.unlock();
            }
            if (c == capacity)
            {
                signalNotFull();
            }
            return x;
        }

        @Override
        public E poll(long timeout, TimeUnit unit) throws InterruptedException
        {
            E x = null;
            int c = -1;
            long nanos = unit.toNanos(timeout);
            final AtomicInteger count = this.count;
            final ReentrantLock takeLock = this.takeLock;
            takeLock.lockInterruptibly();
            try
            {
                while (count.get() == 0)
                {
                    if (nanos <= 0)
                    {
                        return null;
                    }
                    nanos = notEmpty.awaitNanos(nanos);
                }
                x = dequeue();
                c = count.getAndDecrement();
                if (c > 1)
                {
                    notEmpty.signal();
                }
            }
            finally
            {
                takeLock.unlock();
            }
            if (c == capacity)
            {
                signalNotFull();
            }
            return x;
        }

        @Override
        public E poll()
        {
            final AtomicInteger count = this.count;
            if (count.get() == 0)
            {
                return null;
            }
            E x = null;
            int c = -1;
            final ReentrantLock takeLock = this.takeLock;
            takeLock.lock();
            try
            {
                if (count.get() > 0)
                {
                    x = dequeue();
                    c = count.getAndDecrement();
                    if (c > 1)
                    {
                        notEmpty.signal();
                    }
                }
            }
            finally
            {
                takeLock.unlock();
            }
            if (c == capacity)
            {
                signalNotFull();
            }
            return x;
        }

        @Override
        public E peek()
        {
            if (count.get() == 0)
            {
                return null;
            }
            final ReentrantLock takeLock = this.takeLock;
            takeLock.lock();
            try
            {
                Node<E> first = head.next;
                if (first == null)
                {
                    return null;
                }
                else
                {
                    return first.item;
                }
            }
            finally
            {
                takeLock.unlock();
            }
        }

        /**
         * Unlinks interior Node p with predecessor trail.
         */
        void unlink(Node<E> p, Node<E> trail)
        {
            p.item = null;
            trail.next = p.next;
            if (last == p)
            {
                last = trail;
            }
            if (count.getAndDecrement() == capacity)
            {
                notFull.signal();
            }
        }

        @Override
        public boolean remove(Object o)
        {
            if (o == null)
            {
                return false;
            }
            fullyLock();
            try
            {
                for (Node<E> trail = head, p = trail.next; p != null; trail = p, p =
                    p.next)
                {
                    if (o.equals(p.item))
                    {
                        unlink(p, trail);
                        return true;
                    }
                }
                return false;
            }
            finally
            {
                fullyUnlock();
            }
        }

        @Override
        public Object[] toArray()
        {
            fullyLock();
            try
            {
                int size = count.get();
                Object[] a = new Object[size];
                int k = 0;
                for (Node<E> p = head.next; p != null; p = p.next)
                {
                    a[k++] = p.item;
                }
                return a;
            }
            finally
            {
                fullyUnlock();
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> T[] toArray(T[] a)
        {
            fullyLock();
            try
            {
                int size = count.get();
                if (a.length < size)
                {
                    a =
                        (T[]) java.lang.reflect.Array.newInstance(a.getClass()
                            .getComponentType(), size);
                }

                int k = 0;
                for (Node<E> p = head.next; p != null; p = p.next)
                {
                    a[k++] = (T) p.item;
                }
                if (a.length > k)
                {
                    a[k] = null;
                }
                return a;
            }
            finally
            {
                fullyUnlock();
            }
        }

        @Override
        public String toString()
        {
            fullyLock();
            try
            {
                return super.toString();
            }
            finally
            {
                fullyUnlock();
            }
        }

        @Override
        public void clear()
        {
            fullyLock();
            try
            {
                for (Node<E> p, h = head; (p = h.next) != null; h = p)
                {
                    h.next = h;
                    p.item = null;
                }
                head = last;
                // assert head.item == null && head.next == null;
                if (count.getAndSet(0) == capacity)
                {
                    notFull.signal();
                }
            }
            finally
            {
                fullyUnlock();
            }
        }

        /**
         * @throws UnsupportedOperationException
         *             {@inheritDoc}
         * @throws ClassCastException
         *             {@inheritDoc}
         * @throws NullPointerException
         *             {@inheritDoc}
         * @throws IllegalArgumentException
         *             {@inheritDoc}
         */
        @Override
        public int drainTo(Collection<? super E> c)
        {
            return drainTo(c, Integer.MAX_VALUE);
        }

        @Override
        public int drainTo(Collection<? super E> c, int maxElements)
        {
            if (c == null)
            {
                throw new NullPointerException();
            }
            if (c == this)
            {
                throw new IllegalArgumentException();
            }
            boolean signalNotFull = false;
            final ReentrantLock takeLock = this.takeLock;
            takeLock.lock();
            try
            {
                int n = Math.min(maxElements, count.get());
                // count.get provides visibility to first n Nodes
                Node<E> h = head;
                int i = 0;
                try
                {
                    while (i < n)
                    {
                        Node<E> p = h.next;
                        c.add(p.item);
                        p.item = null;
                        h.next = h;
                        h = p;
                        ++i;
                    }
                    return n;
                }
                finally
                {
                    // Restore invariants even if c.add() threw
                    if (i > 0)
                    {
                        // assert h.item == null;
                        head = h;
                        signalNotFull = (count.getAndAdd(-i) == capacity);
                    }
                }
            }
            finally
            {
                takeLock.unlock();
                if (signalNotFull)
                {
                    signalNotFull();
                }
            }
        }

        @Override
        public Iterator<E> iterator()
        {
            return new Itr();
        }

        private class Itr implements Iterator<E>
        {
            /*
             * Basic weakly-consistent iterator. At all times hold the next
             * item to hand out so that if hasNext() reports true, we will
             * still have it to return even if lost race with a take etc.
             */
            private Node<E> current;

            private Node<E> lastRet;

            private E currentElement;

            Itr()
            {
                fullyLock();
                try
                {
                    current = head.next;
                    if (current != null)
                    {
                        currentElement = current.item;
                    }
                }
                finally
                {
                    fullyUnlock();
                }
            }

            @Override
            public boolean hasNext()
            {
                return current != null;
            }

            /**
             * Returns the next live successor of p, or null if no such.
             * Unlike other traversal methods, iterators need to handle both:
             * - dequeued nodes (p.next == p)
             * - (possibly multiple) interior removed nodes (p.item == null)
             */
            private Node<E> nextNode(Node<E> p)
            {
                for (;;)
                {
                    Node<E> s = p.next;
                    if (s == p)
                    {
                        return head.next;
                    }
                    if (s == null || s.item != null)
                    {
                        return s;
                    }
                    p = s;
                }
            }

            @Override
            public E next()
            {
                fullyLock();
                try
                {
                    if (current == null)
                    {
                        throw new NoSuchElementException();
                    }
                    E x = currentElement;
                    lastRet = current;
                    current = nextNode(current);
                    currentElement = (current == null) ? null : current.item;
                    return x;
                }
                finally
                {
                    fullyUnlock();
                }
            }

            @Override
            public void remove()
            {
                if (lastRet == null)
                {
                    throw new IllegalStateException();
                }
                fullyLock();
                try
                {
                    Node<E> node = lastRet;
                    lastRet = null;
                    for (Node<E> trail = head, p = trail.next; p != null; trail =
                        p, p = p.next)
                    {
                        if (p == node)
                        {
                            unlink(p, trail);
                            break;
                        }
                    }
                }
                finally
                {
                    fullyUnlock();
                }
            }
        }
    }

}
