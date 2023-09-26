package cn.nihility.util.http;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class Response<T> {

    private final int status;
    private final String reason;
    private final Map<String, Collection<String>> headers;
    private final T body;

    public Response(int status, String reason, Map<String, Collection<String>> headers, T body) {
        this.status = status;
        this.reason = reason;
        this.headers = (headers != null)
            ? Collections.unmodifiableMap(Utils.caseInsensitiveCopyOf(headers))
            : new LinkedHashMap<>();
        this.body = body;
    }

    public int getStatus() {
        return status;
    }

    public String getReason() {
        return reason;
    }

    public Map<String, Collection<String>> getHeaders() {
        return headers;
    }

    public String getFirstHeader(String key) {
        Collection<String> v = headers.get(key);
        if (null == v || v.isEmpty()) {
            return null;
        }
        return v.iterator().next();
    }

    public T getBody() {
        return body;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("HTTP/1.1 ").append(status);
        if (reason != null)
            builder.append(' ').append(reason);
        builder.append('\n');
        for (String field : headers.keySet()) {
            for (String value : Utils.valuesOrEmpty(headers, field)) {
                builder.append(field).append(": ").append(value).append('\n');
            }
        }
        if (body != null)
            builder.append('\n').append(body);
        return builder.toString();
    }


    public interface Body extends Closeable {

        /**
         * length in bytes, if known. Null if unknown or greater than {@link Integer#MAX_VALUE}.
         *
         * <br>
         * <br>
         * <br>
         * <b>Note</b><br>
         * This is an integer as most implementations cannot do bodies greater than 2GB.
         */
        Integer length();

        /**
         * True if {@link #asInputStream()} and {@link #asReader()} can be called more than once.
         */
        boolean isRepeatable();

        /**
         * It is the responsibility of the caller to close the stream.
         */
        InputStream asInputStream() throws IOException;

        /**
         * It is the responsibility of the caller to close the stream.
         *
         * @deprecated favor {@link Body#asReader(Charset)}
         */
        @Deprecated
        default Reader asReader() throws IOException {
            return asReader(StandardCharsets.UTF_8);
        }

        /**
         * It is the responsibility of the caller to close the stream.
         */
        Reader asReader(Charset charset) throws IOException;
    }

    private static final class InputStreamBody implements Response.Body {

        private final InputStream inputStream;
        private final Integer length;

        private InputStreamBody(InputStream inputStream, Integer length) {
            this.inputStream = inputStream;
            this.length = length;
        }

        private static Body orNull(InputStream inputStream, Integer length) {
            if (inputStream == null) {
                return null;
            }
            return new InputStreamBody(inputStream, length);
        }

        @Override
        public Integer length() {
            return length;
        }

        @Override
        public boolean isRepeatable() {
            return false;
        }

        @Override
        public InputStream asInputStream() {
            return inputStream;
        }

        @Override
        public Reader asReader(Charset charset) throws IOException {
            Utils.checkNotNull(charset, "charset should not be null");
            return new InputStreamReader(inputStream, charset);
        }

        @Override
        public void close() throws IOException {
            inputStream.close();
        }

        @Override
        public String toString() {
            try {
                return new String(Utils.toByteArray(inputStream), Utils.UTF_8);
            } catch (Exception e) {
                return super.toString();
            }
        }
    }


    private static final class ByteArrayBody implements Response.Body {

        private byte[] data;

        public ByteArrayBody(byte[] data) {
            this.data = data;
        }

        private static Body orNull(byte[] data) {
            if (data == null) {
                return null;
            }
            return new ByteArrayBody(data);
        }

        private static Body orNull(String text, Charset charset) {
            if (text == null) {
                return null;
            }
            Utils.checkNotNull(charset, "charset");
            return new ByteArrayBody(text.getBytes(charset));
        }

        @Override
        public Integer length() {
            return data.length;
        }

        @Override
        public boolean isRepeatable() {
            return true;
        }

        @Override
        public InputStream asInputStream() {
            return new ByteArrayInputStream(data);
        }

        @Override
        public Reader asReader(Charset charset) throws IOException {
            Utils.checkNotNull(charset, "charset should not be null");
            return new InputStreamReader(asInputStream(), charset);
        }

        @Override
        public void close() {
            data = null;
        }

        @Override
        public String toString() {
            return Utils.decodeOrDefault(data, Utils.UTF_8, "Binary data");
        }
    }


}
