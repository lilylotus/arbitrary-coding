package cn.nihility.util.http;

import cn.nihility.util.http.handler.HttpRestResult;
import cn.nihility.util.http.handler.ResponseHandler;
import cn.nihility.util.http.handler.ResponseHandlerUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
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
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

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

    public static InputStream entityContent(CloseableHttpResponse response) throws IOException {
        HttpEntity entity = response.getEntity();
        if (entity == null) {
            return null;
        }
        if (entity.isStreaming()) {
            return entity.getContent();
        }
        return null;
    }

    public static Map<String, String> convertHeader(Header[] headers) {
        Map<String, String> h = new LinkedHashMap<>();
        if (null != headers) {
            for (Header header : headers) {
                h.put(header.getName(), header.getValue());
            }
        }
        return h;
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

    public static <R> R execute(HttpUriRequest request, Function<CloseableHttpResponse, R> func) {
        CloseableHttpClient client = defaultHttpClient();
        try (CloseableHttpResponse response = client.execute(request)) {
            R result = func.apply(response);
            EntityUtils.consume(response.getEntity());
            return result;
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

    @SuppressWarnings("unchecked")
    public static <R> HttpRestResult<R> execute(HttpUriRequest request, Class<R> responseType) {
        Function<CloseableHttpResponse, HttpRestResult<R>> func = (resp) -> {
            ResponseHandler<R> handler = ResponseHandlerUtils.selectResponseHandler(responseType);
            try {
                return handler.handle(resp);
            } catch (Exception e) {
                log.error("解析请求 [{}] 响应异常", request.getURI(), e);
            }
            return null;
        };
        return execute(request, func);
    }

    public static <R> HttpRestResult<R> execute(HttpUriRequest request, TypeReference<R> responseType) {
        Function<CloseableHttpResponse, HttpRestResult<R>> func = (resp) -> {
            HttpRestResult<R> result = new HttpRestResult<>();
            int status = resp.getStatusLine().getStatusCode();
            result.setCode(status);
            result.setHeader(convertHeader(resp.getAllHeaders()));
            R resultData = null;
            try {
                InputStream inputStream = entityContent(resp);
                if (null != inputStream) {
                    resultData = JacksonUtils.toObj(inputStream, responseType);
                }
            } catch (Exception e) {
                log.error("解析请求 [{}] 响应异常", request.getURI(), e);
            }
            result.setData(resultData);
            return result;
        };
        return execute(request, func);
    }

    public static Response<String> execute(HttpUriRequest request) {
        Function<CloseableHttpResponse, Response<String>> func = (rsp) -> {
            StatusLine statusLine = rsp.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            String reasonPhrase = statusLine.getReasonPhrase();

            Map<String, Collection<String>> headers = new LinkedHashMap<>(8);
            Header[] allHeaders = rsp.getAllHeaders();
            for (Header h : allHeaders) {
                Collection<String> hList = headers.computeIfAbsent(h.getName(), (k) -> new ArrayList<>(4));
                hList.add(h.getValue());
            }

            HttpEntity entity = rsp.getEntity();
            try {
                String body = EntityUtils.toString(entity, StandardCharsets.UTF_8);
                return new Response<>(statusCode, reasonPhrase, headers, body);
            } catch (IOException e) {
                log.error("获取请求 [{}] 响应内容异常", request.getURI(), e);
            }

            return new Response<>(statusCode, reasonPhrase, headers, null);
        };
        return execute(request, func);
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
