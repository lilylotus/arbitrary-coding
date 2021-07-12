package cn.nihility.http;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.util.Assert;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.concurrent.TimeUnit;


public class ApacheHttpClientConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApacheHttpClientConfiguration.class);

    /**
     * Scheme for HTTP based communication.
     */
    public static final String HTTP_SCHEME = "http";

    /**
     * Scheme for HTTPS based communication.
     */
    public static final String HTTPS_SCHEME = "https";

    private static final Timer connectionManagerTimer =
            new Timer("PoolingHttpClientConnectionManager.connectionManagerTimer", true);

    public static final int DEFAULT_MAX_TOTAL_CONNECTIONS = 200;
    public static final int DEFAULT_MAX_CONNECTIONS_PER_HOST = 50;
    public static final long DEFAULT_POOL_KEEP_ALIVE_TIME = 15 * 60L;
    public static final TimeUnit DEFAULT_POOL_KEEP_ALIVE_TIME_UNITS = TimeUnit.SECONDS;
    public static final int DEFAULT_CONNECT_TIMEOUT = 2000;
    public static final int DEFAULT_READ_TIMEOUT = 2000;
    public static final Boolean DEFAULT_FOLLOW_REDIRECTS = Boolean.FALSE;
    public static final boolean DEFAULT_GZIP_PAYLOAD = true;
    public static final int DEFAULT_CONNECTION_IDLE_TIMER_TASK_REPEAT_IN_MSECS = 30000; // every half minute (30 secs)

    private ApacheHttpClientConfiguration() {
    }

    public static HttpClientConnectionManager createDefaultConnectionManager() {
        return newConnectionManager(false);
    }

    public static HttpClientConnectionManager newConnectionManager(boolean disableSslValidation) {
        return newConnectionManager(disableSslValidation, DEFAULT_MAX_TOTAL_CONNECTIONS, DEFAULT_MAX_CONNECTIONS_PER_HOST,
                DEFAULT_POOL_KEEP_ALIVE_TIME, DEFAULT_POOL_KEEP_ALIVE_TIME_UNITS, null);
    }

    public static HttpClientConnectionManager newConnectionManager(boolean disableSslValidation,
                                                                   int maxTotalConnections, int maxConnectionsPerRoute) {
        return newConnectionManager(disableSslValidation, maxTotalConnections, maxConnectionsPerRoute,
                -1, TimeUnit.MILLISECONDS, null);
    }

    public static HttpClientConnectionManager newConnectionManager(boolean disableSslValidation, int maxTotalConnections,
                                                                   int maxConnectionsPerRoute, long timeToLive, TimeUnit timeUnit,
                                                                   RegistryBuilder<ConnectionSocketFactory> registryBuilder) {
        if (registryBuilder == null) {
            registryBuilder = RegistryBuilder.<ConnectionSocketFactory>create()
                    .register(HTTP_SCHEME, PlainConnectionSocketFactory.INSTANCE);
        }

        if (disableSslValidation) {
            try {
                final SSLContext sslContext = SSLContext.getInstance("SSL");
                sslContext.init(null,
                        new TrustManager[]{new DisabledValidationTrustManager()},
                        new SecureRandom());
                registryBuilder.register(HTTPS_SCHEME,
                        new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE));
            } catch (NoSuchAlgorithmException | KeyManagementException e) {
                LOGGER.warn("Error creating SSLContext", e);
            }
        } else {
            registryBuilder.register(HTTPS_SCHEME, SSLConnectionSocketFactory.getSocketFactory());
        }

        final Registry<ConnectionSocketFactory> registry = registryBuilder.build();

        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(
                registry, null, null, null, timeToLive, timeUnit);
        connectionManager.setMaxTotal(maxTotalConnections);
        connectionManager.setDefaultMaxPerRoute(maxConnectionsPerRoute);

        connectionManagerTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                connectionManager.closeExpiredConnections();
            }
        }, 30000, DEFAULT_CONNECTION_IDLE_TIMER_TASK_REPEAT_IN_MSECS);

        return connectionManager;
    }

    public static void destroy() {
        connectionManagerTimer.cancel();
    }

    public static CloseableHttpClient createHttpClient(boolean disableSslValidation) {
        HttpClientBuilder builder = HttpClientBuilder.create();
        HttpClientBuilder httpClientFactory = builder.disableContentCompression()
                .disableCookieManagement()
                .useSystemProperties();

        RequestConfig defaultRequestConfig = RequestConfig.custom()
                .setConnectTimeout(DEFAULT_CONNECT_TIMEOUT)
                .setRedirectsEnabled(DEFAULT_FOLLOW_REDIRECTS)
                .build();

        return httpClientFactory.setDefaultRequestConfig(defaultRequestConfig)
                .setConnectionManager(newConnectionManager(disableSslValidation))
                .build();
    }

    public static CloseableHttpClient createDefaultHttpClient() {
        HttpClientBuilder builder = HttpClientBuilder.create();
        HttpClientBuilder httpClientFactory = builder.disableContentCompression()
                .disableCookieManagement()
                .useSystemProperties();

        RequestConfig defaultRequestConfig = RequestConfig.custom()
                .setConnectTimeout(DEFAULT_CONNECT_TIMEOUT)
                .setRedirectsEnabled(DEFAULT_FOLLOW_REDIRECTS)
                .build();

        return httpClientFactory.setDefaultRequestConfig(defaultRequestConfig)
                .setConnectionManager(createDefaultConnectionManager())
                .build();

    }

    public static RequestConfig buildRequestConfig() {
        return RequestConfig.custom()
                .setConnectTimeout(DEFAULT_CONNECT_TIMEOUT)
                .setSocketTimeout(DEFAULT_READ_TIMEOUT)
                .setRedirectsEnabled(DEFAULT_FOLLOW_REDIRECTS)
                .setContentCompressionEnabled(DEFAULT_GZIP_PAYLOAD)
                .build();
    }

    public static HttpUriRequest buildRequest(String method, URI uri,
                                           Map<String, List<String>> headers,
                                           Map<String, List<String>> params,
                                           final String requestEntityContent) {
        return buildRequest(method, uri, StandardCharsets.UTF_8, headers,
                params, requestEntityContent, buildRequestConfig(), null);
    }

    public static HttpUriRequest buildRequest(String method, URI uri, Charset charset,
                                           Map<String, List<String>> headers,
                                           Map<String, List<String>> params,
                                           final String requestEntityContent,
                                           final RequestConfig requestConfig,
                                           List<RequestCustomizer> customizers) {
        final RequestBuilder builder = RequestBuilder.create(method);

        builder.setUri(uri);
        builder.setCharset(charset);

        if (null != headers && !headers.isEmpty()) {
            headers.forEach((name, values) ->
                    values.forEach(value -> builder.addHeader(name, value)));
        }

        if (null != params && !params.isEmpty()) {
            params.forEach((name, values) ->
                    values.forEach(value -> builder.addParameter(name, value)));
        }

        if (StringUtils.isNotBlank(requestEntityContent)) {
            final BasicHttpEntity entity = new BasicHttpEntity();
            final byte[] contentBytes = requestEntityContent.getBytes(charset);
            entity.setContent(new RequestServletInputStreamWrapper(contentBytes));

            /*StringEntity entity = new StringEntity(requestEntityContent, charset);
            paramEntity.setContentEncoding("UTF-8");
            paramEntity.setContentType("application/json");*/

            // if the entity contentLength isn't set, transfer-encoding will be set
            // to chunked in org.apache.http.protocol.RequestContent. See gh-1042
            final long contentLength = contentBytes.length;
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Content-Length [{}]", contentLength);
            }
            if ("GET".equals(method)) {
                entity.setContentLength(0);
            } else {
                entity.setContentLength(contentLength);
            }
            builder.setEntity(entity);
        }

        if (null != customizers && !customizers.isEmpty()) {
            RequestCustomizer.customize(customizers, builder);
        }

        builder.setConfig(requestConfig);
        return builder.build();
    }

    static class DisabledValidationTrustManager implements X509TrustManager {

        @Override
        public void checkClientTrusted(X509Certificate[] x509Certificates, String s)
                throws CertificateException {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] x509Certificates, String s)
                throws CertificateException {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }

    }

    static class RequestServletInputStreamWrapper extends ServletInputStream {

        private final ByteArrayInputStream input;

        public RequestServletInputStreamWrapper(byte[] data) {
            this.input = new ByteArrayInputStream(data);
        }

        @Override
        public boolean isFinished() {
            return false;
        }

        @Override
        public boolean isReady() {
            return false;
        }

        @Override
        public void setReadListener(ReadListener listener) {
        }

        @Override
        public int read() throws IOException {
            return input.read();
        }

        @Override
        public synchronized void reset() throws IOException {
            input.reset();
        }

    }

    interface RequestCustomizer {

        boolean accepts(Class<?> builderClass);

        void customize(RequestBuilder builder);

        static void customize(List<RequestCustomizer> customizers, RequestBuilder builder) {
            for (RequestCustomizer customizer : customizers) {
                if (customizer.accepts(builder.getClass())) {
                    customizer.customize(builder);
                }
            }
        }
    }

    public static class RequestApacheHttpResponse {

        private HttpResponse httpResponse;

        private URI uri;

        public RequestApacheHttpResponse(final HttpResponse httpResponse, final URI uri) {
            Assert.notNull(httpResponse, "httpResponse can not be null");
            this.httpResponse = httpResponse;
            this.uri = uri;
        }

        public URI getRequestedURI() {
            return this.uri;
        }

        public Object getPayload() throws IllegalStateException {
            try {
                if (!hasPayload()) {
                    return null;
                }
                return this.httpResponse.getEntity().getContent();
            } catch (final IOException e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
        }

        public boolean hasPayload() {
            return this.httpResponse.getEntity() != null;
        }

        public boolean isSuccess() {
            return HttpStatus.valueOf(this.httpResponse.getStatusLine().getStatusCode())
                    .is2xxSuccessful();
        }

        public int getStatus() {
            return httpResponse.getStatusLine().getStatusCode();
        }

        public String getStatusLine() {
            return httpResponse.getStatusLine().toString();
        }

        public InputStream getInputStream() {
            try {
                if (!hasPayload()) {
                    return null;
                }
                return this.httpResponse.getEntity().getContent();
            } catch (final IOException e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
        }

        public void close() {
            if (this.httpResponse != null && this.httpResponse.getEntity() != null) {
                try {
                    this.httpResponse.getEntity().getContent().close();
                } catch (final IOException e) {
                    throw new IllegalStateException(e.getMessage(), e);
                }
            }
        }

        public Map<String, Collection<String>> getHeaders() {
            final Map<String, Collection<String>> headers = new HashMap<>();
            for (final Header header : this.httpResponse.getAllHeaders()) {
                if (headers.containsKey(header.getName())) {
                    headers.get(header.getName()).add(header.getValue());
                } else {
                    final List<String> values = new ArrayList<>();
                    values.add(header.getValue());
                    headers.put(header.getName(), values);
                }
            }
            return headers;
        }

        /*public HttpHeaders getHttpHeaders() {
            final CaseInsensitiveMultiMap headers = new CaseInsensitiveMultiMap();
            for (final Header header : httpResponse.getAllHeaders()) {
                headers.addHeader(header.getName(), header.getValue());
            }
            return headers;
        }*/

    }

}
