package cn.nihility.http;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public class DefaultHttpClientUtil {

    private DefaultHttpClientUtil() {
    }

    private static final Logger log = LoggerFactory.getLogger(DefaultHttpClientUtil.class);

    /**
     * Scheme for HTTP based communication.
     */
    public static final String HTTP_SCHEME = "http";

    /**
     * Scheme for HTTPS based communication.
     */
    public static final String HTTPS_SCHEME = "https";

    public static final int DEFAULT_CONNECTION_REQUEST_TIMEOUT = -1;
    public static final int DEFAULT_CONNECTION_TIMEOUT = 50 * 1000;
    public static final int DEFAULT_SOCKET_TIMEOUT = 50 * 1000;
    public static final int DEFAULT_MAX_CONNECTIONS = 1024;
    public static final long DEFAULT_CONNECTION_TTL = -1;
    public static final long DEFAULT_IDLE_CONNECTION_TIME = 60 * 1000L;
    public static final int DEFAULT_VALIDATE_AFTER_INACTIVITY = 2 * 1000;
    public static final int DEFAULT_THREAD_POOL_WAIT_TIME = 60 * 1000;
    public static final int DEFAULT_REQUEST_TIMEOUT = 5 * 60 * 1000;
    public static final long DEFAULT_SLOW_REQUESTS_THRESHOLD = 5 * 60 * 1000L;
    public static final boolean DEFAULT_USE_REAPER = true;

    public static HttpClientConnectionManager createHttpClientConnectionManager() {
        SSLContext sslContext;
        try {
            sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {

                @Override
                public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    return true;
                }

            }).build();
        } catch (Exception e) {
            throw new IllegalArgumentException(e.getMessage());
        }

        //SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(sslContext);
        SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(sslContext,
            NoopHostnameVerifier.INSTANCE);
        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
            .register(HTTP_SCHEME, PlainConnectionSocketFactory.getSocketFactory())
            .register(HTTPS_SCHEME, sslSocketFactory).build();

        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(
            socketFactoryRegistry);
        connectionManager.setDefaultMaxPerRoute(DEFAULT_MAX_CONNECTIONS);
        connectionManager.setMaxTotal(DEFAULT_MAX_CONNECTIONS);
        connectionManager.setValidateAfterInactivity(DEFAULT_VALIDATE_AFTER_INACTIVITY);
        connectionManager.setDefaultSocketConfig(
            SocketConfig.custom().setSoTimeout(DEFAULT_SOCKET_TIMEOUT).setTcpNoDelay(true).build());
        IdleConnectionReaper.setIdleConnectionTime(DEFAULT_IDLE_CONNECTION_TIME);
        IdleConnectionReaper.registerConnectionManager(connectionManager);

        return connectionManager;
    }

    public static String getDefaultUserAgent() {
        return "HttpClient/v1.0(" + System.getProperty("os.name") + "/"
            + System.getProperty("os.version") + "/" + System.getProperty("os.arch") + ";"
            + System.getProperty("java.version") + ")";
    }

    public static CloseableHttpClient createHttpClient(HttpClientConnectionManager connectionManager) {
        return HttpClients.custom()
            .setConnectionManager(connectionManager)
            .setUserAgent(getDefaultUserAgent())
            .disableContentCompression()
            .disableAutomaticRetries()
            .build();
    }

    public static RequestConfig createRequestConfig() {
        RequestConfig.Builder requestConfigBuilder = RequestConfig.custom();
        requestConfigBuilder.setConnectTimeout(DEFAULT_CONNECTION_TIMEOUT);
        requestConfigBuilder.setSocketTimeout(DEFAULT_SOCKET_TIMEOUT);
        requestConfigBuilder.setConnectionRequestTimeout(DEFAULT_CONNECTION_REQUEST_TIMEOUT);
        return requestConfigBuilder.build();
    }

    public static void shutdown(HttpClientConnectionManager connectionManager) {
        if (null != connectionManager) {
            IdleConnectionReaper.removeConnectionManager(connectionManager);
            connectionManager.shutdown();
        }
    }

}
