
package com.manyanger.data.net;

import android.util.Log;

import com.manyanger.GlobalData;

import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.util.EntityUtils;

import java.io.InputStream;
import java.lang.Thread.UncaughtExceptionHandler;

public class HttpService  {

    private final static String TAG = "HttpService";
    
    public static final int TCP_SOCKET_CONNECT_TIMEOUT = 10000;

    public static final int TCP_SOCKET_BUFFER_SIZE = 8192;

    private final static String NOT_GET_DATA = "Can't get data";
    private final static String ILLEGAL_DATA = "Data error!";

//    private INetState netState = null;

    private static HttpParams params;

    public HttpService() {
        Log.d(TAG, "HttpService Construct..");
    }

    // simplebean set
//    public void setNetState(INetState netState) {
//        this.netState = netState;
//    }

    static {
        params = buildHttpParams();

    }

    private static ClientConnectionManager connectionManager;

    public final static int MAX_CONNECTIONS = 100;

    public final static int WAIT_TIMEOUT = 30000;

    public final static int MAX_ROUTE_CONNECTIONS = 30;


    public final static int READ_TIMEOUT = 15000;

    private static HttpParams buildHttpParams() {

        HttpParams httpParams = new BasicHttpParams();

        httpParams.setParameter("Content-Type", "text/html; charset=UTF-8");
        httpParams.setParameter("Accept", "*/*");
        httpParams.setParameter("User-Agent", "Android");
        httpParams.setParameter("Accept-Language", "zh-CN");
        httpParams.setParameter("Connection", "Keep-Alive"); // 闀胯繛鎺�
        httpParams.setParameter("Sky-Content-Version", Integer.toString(1));
        httpParams.setParameter("Content-Type", "application/octet-stream");
        httpParams.setParameter("Pragma", "no-cache");
        httpParams.setParameter(HttpConnectionParams.SO_TIMEOUT, READ_TIMEOUT);

        ConnManagerParams.setTimeout(httpParams, WAIT_TIMEOUT);

        ConnManagerParams.setMaxTotalConnections(httpParams, MAX_CONNECTIONS);

        ConnPerRouteBean connPerRoute = new ConnPerRouteBean(
                MAX_ROUTE_CONNECTIONS);

        HttpHost localhost = new HttpHost("api.manyanger.com:8101", 8101);
        connPerRoute.setMaxForRoute(new HttpRoute(localhost), 50);

        ConnManagerParams.setMaxConnectionsPerRoute(httpParams, connPerRoute);

        SchemeRegistry registry = new SchemeRegistry();
        registry.register(new Scheme("http", PlainSocketFactory
                .getSocketFactory(), 80));
        registry.register(new Scheme("https", SSLSocketFactory
                .getSocketFactory(), 443));
        connectionManager = new ThreadSafeClientConnManager(httpParams,
                registry);

        return httpParams;
    }

    public DefaultHttpClient getDefaultHttpClient() {

        if (connectionManager == null) {

            params = buildHttpParams();
        } else {
            try {
                // 鍏抽棴杩囨湡杩炴帴
                connectionManager.closeExpiredConnections();
            } catch (Exception e) {
            }

        }

//        if ((netState.isWifi() || netState.is3G()) && (!PhoneInfo.isLowPhone())) {
            params.setParameter(HttpConnectionParams.SOCKET_BUFFER_SIZE,
                    4 * TCP_SOCKET_BUFFER_SIZE);
            params.setParameter(HttpConnectionParams.CONNECTION_TIMEOUT,
                    TCP_SOCKET_CONNECT_TIMEOUT);
//        } else {
//            params.setParameter(HttpConnectionParams.SOCKET_BUFFER_SIZE,
//                    TCP_SOCKET_BUFFER_SIZE / (netState.isUniWap() ? 2 : 1));
//            params.setParameter(HttpConnectionParams.CONNECTION_TIMEOUT,
//                    2 * TCP_SOCKET_CONNECT_TIMEOUT);
//        }

        DefaultHttpClient defaultClient = new DefaultHttpClient(
                connectionManager, params);
        HttpProtocolParams.setUseExpectContinue(defaultClient.getParams(),false);
        defaultClient.setHttpRequestRetryHandler(new RetryHandler(2));


        return defaultClient;
    }


    public void shutdown() {
        try {
            connectionManager.shutdown();
        } catch (Exception e) {
        }
    }

    private boolean isSuccess(final HttpResponse response) {
        if (response != null && response.getStatusLine() != null) {
            Log.i(TAG, "HttpResponse:" + response.getStatusLine().getStatusCode());
        }
        if(response.getEntity() == null){
        	Log.i(TAG, "HttpResponse: content null");
        }
//        long length = response.getEntity().getContentLength();
//        Log.i(TAG, "HttpResponse: content length:"+length);
        
        return (response != null
                && response.getEntity() != null
                && response.getStatusLine().getStatusCode() == HttpStatus.SC_OK);
        
//        return (response != null
//                && response.getEntity() != null
//                && (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK || response
//                        .getStatusLine().getStatusCode() == HttpStatus.SC_PARTIAL_CONTENT)
//                && response.getEntity().getContentLength() > 0 && response
//                .getEntity().getContentLength() <= Integer.MAX_VALUE);
    }


    public String getHttpResponseInString(String url)
            throws DataException, Exception {

        HttpClient client = null;
        HttpGet httpGet = null;
        try {
        	String uri = url;
        	String prefix = url.substring(0, "http://".length());
        	if(!prefix.equalsIgnoreCase("http://")){
        		uri = GlobalData.SERVER_URL + url;
        	}
            client = getDefaultHttpClient();

            httpGet = new HttpGet(uri);

            HttpResponse response = client.execute(httpGet);
            
            Header[] head = response.getAllHeaders();
//            for(int i=0; i<head.length; i++){
//            	Log.i(TAG, "Head:"+head[i].toString());
//            }

            if (isSuccess(response)) {
                final String resStr = EntityUtils.toString(response.getEntity());
                return resStr;
            }
//            final String resStr = EntityUtils.toString(response.getEntity());
//            return resStr;

        } catch (Exception e) {
        	e.printStackTrace();
            if (httpGet != null && (!httpGet.isAborted())) {
                httpGet.abort();
            }
        }

        // TODO:
        throw new DataException(NOT_GET_DATA);
    }

    public InputStream getHttpResponseInputStream(String url)
            throws DataException, Exception {

        HttpClient client = null;
        HttpGet httpGet = null;
        try {
        	String uri = url;
        	String prefix = url.substring(0, "http://".length());
        	if(!prefix.equalsIgnoreCase("http://")){
        		uri = GlobalData.SERVER_URL + url;
        	}
            client = getDefaultHttpClient();

            Log.i("HTTP", "uri:"+uri);
            httpGet = new HttpGet(uri);

            HttpResponse response = client.execute(httpGet);
            
            Header[] head = response.getAllHeaders();
            for(int i=0; i<head.length; i++){
            	Log.i(TAG, "Head:"+head[i].toString());
            }

            if (isSuccess(response)) {
            	InputStream instream = response.getEntity().getContent();
            	return instream;

            }

        } catch (Exception e) {
        	e.printStackTrace();
            if (httpGet != null && (!httpGet.isAborted())) {
                httpGet.abort();
            }
        }

        // TODO:
        throw new DataException(NOT_GET_DATA);
    }


    public static class HttpThread extends Thread implements
        UncaughtExceptionHandler
    {
        protected HttpClient defaultClient;

        protected HttpRequestBase mDefaultPost;

        public HttpRequestBase getDefaultPost()
        {
            return mDefaultPost;
        }

        public HttpThread(String name, int priority, Runnable ruannable)
        {
            super(ruannable);
            setName(name);
            setPriority(priority);

            setUncaughtExceptionHandler(this);
        }

        public HttpThread(String name, int priority)
        {
            this(name, priority, null);
        }

        public HttpThread(ThreadGroup group, Runnable runnable,
            String threadName, long stackSize)
        {
            super(group, runnable, threadName, stackSize);

            setUncaughtExceptionHandler(this);
        }

        public void abort()
        {
            interrupt();
            if (mDefaultPost != null)
            {
                mDefaultPost.abort();
            }
        }


        @Override
        public void uncaughtException(Thread thread, Throwable ex)
        {
            if (defaultClient != null)
            {
                defaultClient.getConnectionManager().shutdown();
                defaultClient = null;
            }
            if (ex != null)
            {
                ex.printStackTrace();
            }

        }


        @Override
        protected void finalize() throws Throwable
        {
            try
            {
                uncaughtException(this, null);
            }
            catch (Exception e)
            {
                // TODO: handle exception
            }
            finally
            {
                super.finalize();
            }
        }

    }

}
