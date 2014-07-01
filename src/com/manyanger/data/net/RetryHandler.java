
package com.manyanger.data.net;

import android.os.SystemClock;
import android.util.Log;

import org.apache.http.NoHttpResponseException;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.SocketException;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Iterator;

import javax.net.ssl.SSLException;

class RetryHandler implements HttpRequestRetryHandler {

    private static final String TAG = "RetryHandler";

    private static final int RETRY_SLEEP_TIME_MILLIS = 1500;
    private static HashSet<Class<?>> exceptionWhitelist = new HashSet<Class<?>>();
    private static HashSet<Class<?>> exceptionBlacklist = new HashSet<Class<?>>();

    
//    static {
//        exceptionWhitelist.add(NoHttpResponseException.class);
//        exceptionWhitelist.add(UnknownHostException.class);
//        exceptionWhitelist.add(SocketException.class);
//
//        exceptionBlacklist.add(InterruptedIOException.class);
//        exceptionBlacklist.add(SSLException.class);
//    }
//    
    // yuzhif modified 2014.05.09
    static {
        exceptionWhitelist.add(NoHttpResponseException.class);
        exceptionWhitelist.add(UnknownHostException.class);
        exceptionWhitelist.add(InterruptedIOException.class); // subclasses:ConnectTimeoutException, SocketTimeoutException
        // 有可能返回 SocketException:Connection reset by peer;这种情况是可以重试的，所以加到白名单 
        exceptionWhitelist.add(SocketException.class); //subclasses:BindException, ConnectException, NoRouteToHostException, PortUnreachableException
        
        exceptionBlacklist.add(SSLException.class);
    }

    private final int maxRetries;

    // INetState netState;

    public RetryHandler(int maxRetries) {
        this.maxRetries = maxRetries;
        // this.netState = netState;
    }

    @Override
    public boolean retryRequest(IOException exception, int executionCount,
            HttpContext context) {
        boolean retry = true;

        Boolean b = (Boolean) context
                .getAttribute(ExecutionContext.HTTP_REQ_SENT);
        boolean sent = (b != null && b.booleanValue());

        if (executionCount > maxRetries) {
            Log.d(TAG, "超过设置的次数[" + maxRetries + "],不再重试!");
            retry = false;
        } else if (isInList(exceptionBlacklist, exception)) {
            Log.d(TAG, "黑名单列表中的,不再重试!" + exception.getMessage());
            retry = false;
        } else if (isInList(exceptionWhitelist, exception)) {
            Log.d(TAG, "白名单列表中的,重试!" + exception.getMessage());
            retry = true;
        } else if (!sent) {
            Log.d(TAG, "请求未到达,重试!");
            retry = true;
        }

        if (retry) {
            HttpUriRequest currentReq = (HttpUriRequest) context
                    .getAttribute(ExecutionContext.HTTP_REQUEST);
            String requestType = currentReq.getMethod();
            retry = !requestType.equals("POST") ;

        }
        HttpUriRequest currentReq = (HttpUriRequest) context
                .getAttribute(ExecutionContext.HTTP_REQUEST);
        URI requstUri = currentReq.getURI();
        Log.i(TAG, "URI: "+ requstUri.toString() + "   path: "+requstUri.getPath());

        if (retry) {
            SystemClock.sleep(RETRY_SLEEP_TIME_MILLIS);
        } else {
            exception.printStackTrace();
//            Log.w(TAG, "RetryHandler Error信息:" + ExceptionUtils.toString(exception));
        }
        Log.i(TAG, "return retry:"+retry);
        return retry;
    }

    protected boolean isInList(HashSet<Class<?>> list, Throwable error) {
        Iterator<Class<?>> itr = list.iterator();
        while (itr.hasNext()) {
            if (itr.next().isInstance(error)) {
                return true;
            }
        }
        return false;
    }
}
