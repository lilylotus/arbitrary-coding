package cn.nihility.controller.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ReadListener;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.Part;
import java.io.*;
import java.util.Collection;

public class HttpServletRequestStringBodyWrapper extends HttpServletRequestWrapper {

    private static final Logger log = LoggerFactory.getLogger(HttpServletRequestStringBodyWrapper.class);

    private byte[] requestBodyByteData;
    private String requestBodyString;

    /**
     * Constructs a request object wrapping the given request.
     *
     * @param request The request to wrap
     * @throws IllegalArgumentException if the request is null
     */
    public HttpServletRequestStringBodyWrapper(HttpServletRequest request) {
        super(request);

        try (ServletInputStream inputStream = request.getInputStream();
             final ByteArrayOutputStream outputStream = new ByteArrayOutputStream(2048)) {
            int len;
            int transferSize = 0;
            byte[] buffer = new byte[2048];

            while ((len = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, len);
                transferSize += len;
            }
            this.requestBodyByteData = outputStream.toByteArray();
            this.requestBodyString = new String(requestBodyByteData, getCharacterEncoding());

            if (log.isDebugEnabled()) {
                log.debug("请求 [{}] 传输数据 [{}] byte", request.getRequestURI(), transferSize);
            }
        } catch (IOException e) {
            log.error("获取 HttpServletRequest 请求 body 数据出错，请求 uri [{}]", request.getRequestURI(), e);
            this.requestBodyByteData = null;
        }
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(getInputStream(), getCharacterEncoding()));
    }

    @Override
    public Collection<Part> getParts() throws IOException, ServletException {
        return ((HttpServletRequest) super.getRequest()).getParts();
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        return new RequestCachingInputStream(requestBodyByteData);
    }

    public String getRequestBodyString() {
        return requestBodyString;
    }

    private static class RequestCachingInputStream extends ServletInputStream {

        private final ByteArrayInputStream inputStream;

        public RequestCachingInputStream(byte[] bytes) {
            inputStream = new ByteArrayInputStream(bytes);
        }

        @Override
        public int read() throws IOException {
            return inputStream.read();
        }

        @Override
        public boolean isFinished() {
            return inputStream.available() == 0;
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setReadListener(ReadListener readlistener) {
        }

    }

    public static String getLineSeparator() {
        String lineSeparator = System.lineSeparator();
        if (null == lineSeparator) {
            lineSeparator = "\n";
        }
        return lineSeparator;
    }

}
