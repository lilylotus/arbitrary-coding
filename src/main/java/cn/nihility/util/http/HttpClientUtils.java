package cn.nihility.util.http;

import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class HttpClientUtils {

    private static final Logger log = LoggerFactory.getLogger(HttpClientUtils.class);

    /**
     * Default value for disabling SSL validation.
     */
    public static final boolean DEFAULT_DISABLE_SSL_VALIDATION = false;

    /**
     * Default value for max number od connections.
     */
    public static final int DEFAULT_MAX_CONNECTIONS = 200;

    /**
     * Default value for max number od connections per route.
     */
    public static final int DEFAULT_MAX_CONNECTIONS_PER_ROUTE = 50;

    /**
     * Default value for time to live.
     */
    public static final long DEFAULT_TIME_TO_LIVE = 900L;

    /**
     * Default time to live unit.
     */
    public static final TimeUnit DEFAULT_TIME_TO_LIVE_UNIT = TimeUnit.SECONDS;

    /**
     * Default value for following redirects.
     */
    public static final boolean DEFAULT_FOLLOW_REDIRECTS = true;

    /**
     * Default value for connection timeout.
     */
    public static final int DEFAULT_CONNECTION_TIMEOUT = 2000;

    /**
     * Default value for connection timer repeat.
     */
    public static final int DEFAULT_CONNECTION_TIMER_REPEAT = 3000;

    private static volatile HttpClientConnectionManager connectionManager;

    private static volatile Timer connectionManagerTimer;

    private static volatile CloseableHttpClient httpClient;

    private HttpClientUtils() {
    }

    private static CloseableHttpClient defaultHttpClient() {
        if (null == httpClient) {
            synchronized (HttpClientUtils.class) {
                if (null == httpClient) {
                    connectionManager = newConnectionManager(DEFAULT_DISABLE_SSL_VALIDATION, DEFAULT_MAX_CONNECTIONS,
                        DEFAULT_MAX_CONNECTIONS_PER_ROUTE, DEFAULT_TIME_TO_LIVE, DEFAULT_TIME_TO_LIVE_UNIT);

                    connectionManagerTimer = new Timer("HttpClientUtils.connectionManagerTimer", true);
                    connectionManagerTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            connectionManager.closeExpiredConnections();
                        }
                    }, 30000, DEFAULT_CONNECTION_TIMER_REPEAT);

                    RequestConfig defaultRequestConfig = RequestConfig.custom()
                        .setConnectTimeout(DEFAULT_CONNECTION_TIMEOUT)
                        .setRedirectsEnabled(DEFAULT_FOLLOW_REDIRECTS)
                        .build();

                    httpClient = HttpClientBuilder.create()
                        .disableContentCompression()
                        .disableCookieManagement()
                        // 定期清理闲置连接
                        .evictExpiredConnections()
                        .evictIdleConnections(20000L, TimeUnit.MILLISECONDS)
                        .useSystemProperties()
                        .setConnectionManager(connectionManager)
                        .setDefaultRequestConfig(defaultRequestConfig)
                        .build();

                    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                        if (null != connectionManagerTimer) {
                            connectionManagerTimer.cancel();
                        }
                        if (null != httpClient) {
                            try {
                                httpClient.close();
                            } catch (IOException e) {
                                log.error("HttpClientUtils#httpClient close error", e);
                            }
                        }
                        if (null != connectionManager) {
                            connectionManager.shutdown();
                        }
                    }, "HttpClientUtils#ShutdownHook"));
                }
            }
        }
        return httpClient;
    }

    public static HttpClientConnectionManager newConnectionManager(boolean disableSslValidation,
                                                                   int maxTotalConnections, int maxConnectionsPerRoute) {
        return newConnectionManager(disableSslValidation, maxTotalConnections,
            maxConnectionsPerRoute, -1, TimeUnit.MILLISECONDS);
    }

    public static HttpClientConnectionManager newConnectionManager(boolean disableSslValidation, int maxTotalConnections,
                                                                   int maxConnectionsPerRoute, long timeToLive, TimeUnit timeUnit) {
        RegistryBuilder<ConnectionSocketFactory> registryBuilder = RegistryBuilder.<ConnectionSocketFactory>create()
            .register(HttpConstant.HTTP_SCHEME, PlainConnectionSocketFactory.INSTANCE);
        if (disableSslValidation) {
            try {
                final SSLContext sslContext = SSLContext.getInstance("SSL");
                sslContext.init(null, new TrustManager[]{new DisabledValidationTrustManager()}, new SecureRandom());
                registryBuilder.register(HttpConstant.HTTPS_SCHEME,
                    new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE));
            } catch (NoSuchAlgorithmException e) {
                log.warn("Error creating SSLContext", e);
            } catch (KeyManagementException e) {
                log.warn("Error creating SSLContext", e);
            }
        } else {
            registryBuilder.register("https", SSLConnectionSocketFactory.getSocketFactory());
        }

        final Registry<ConnectionSocketFactory> registry = registryBuilder.build();

        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(
            registry, null, null, null, timeToLive, timeUnit);
        connectionManager.setMaxTotal(maxTotalConnections);
        connectionManager.setDefaultMaxPerRoute(maxConnectionsPerRoute);

        return connectionManager;
    }


    /**
     * Creates a new Options Instance.
     *
     * @param connectTimeout     value.
     * @param connectTimeoutUnit with the TimeUnit for the timeout value.
     * @param readTimeout        value.
     * @param readTimeoutUnit    with the TimeUnit for the timeout value.
     * @param followRedirects    if the request should follow 3xx redirections.
     */
    public static RequestConfig requestConfigOption(long connectTimeout, TimeUnit connectTimeoutUnit,
                                                    long readTimeout, TimeUnit readTimeoutUnit,
                                                    boolean followRedirects) {
        return RequestConfig.custom()
            .setConnectTimeout((int) connectTimeoutUnit.toMillis(connectTimeout))
            .setSocketTimeout((int) readTimeoutUnit.toMillis(readTimeout))
            .setRedirectsEnabled(followRedirects)
            .build();
    }

    public static void execute(HttpUriRequest request) {
        CloseableHttpClient client = defaultHttpClient();
        try (CloseableHttpResponse response = client.execute(request)) {
            StatusLine statusLine = response.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            String reasonPhrase = statusLine.getReasonPhrase();
            log.info("response status [{}:{}]", statusCode, reasonPhrase);

            String body = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            log.info("response body [{}]", body);
        } catch (ClientProtocolException e) {
            String error = String.format("请求 [%s: %s] 协议异常", request.getMethod(), request.getURI());
            log.error(error, e);
            throw new HttpClientException(error);
        } catch (IOException e) {
            String error = String.format("请求 [%s: %s] IO异常", request.getMethod(), request.getURI());
            log.error(error, e);
            throw new HttpClientException(error);
        }
    }

    static class DisabledValidationTrustManager implements X509TrustManager {

        @Override
        public void checkClientTrusted(X509Certificate[] x509Certificates, String s) {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] x509Certificates, String s) {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }

    }


}
