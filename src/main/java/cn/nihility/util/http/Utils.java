package cn.nihility.util.http;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import static java.lang.String.format;

/**
 * @author yzx
 */
public class Utils {

    /**
     * UTF-8: eight-bit UCS Transformation Format.
     */
    public static final Charset UTF_8 = StandardCharsets.UTF_8;

    // com.google.common.base.Charsets
    /**
     * ISO-8859-1: ISO Latin Alphabet Number 1 (ISO-LATIN-1).
     */
    public static final Charset ISO_8859_1 = Charset.forName("ISO-8859-1");

    private static final int BUF_SIZE = 0x800; // 2K chars (4K bytes)

    private Utils() {
    }

    /**
     * Returns an unmodifiable collection which may be empty, but is never null.
     */
    public static <T> Collection<T> valuesOrEmpty(Map<String, Collection<T>> map, String key) {
        Collection<T> values = map.get(key);
        return values != null ? values : Collections.emptyList();
    }

    /**
     * Copy of {@code com.google.common.base.Preconditions#checkNotNull}.
     */
    public static <T> T checkNotNull(T reference,
                                     String errorMessageTemplate,
                                     Object... errorMessageArgs) {
        if (reference == null) {
            // If either of these parameters is null, the right thing happens anyway
            throw new NullPointerException(
                format(errorMessageTemplate, errorMessageArgs));
        }
        return reference;
    }

    /**
     * Adapted from {@code com.google.common.io.ByteStreams.toByteArray()}.
     */
    public static byte[] toByteArray(InputStream in) throws IOException {
        checkNotNull(in, "in");
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            copy(in, out);
            return out.toByteArray();
        } finally {
            ensureClosed(in);
        }
    }

    public static void ensureClosed(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException ignored) { // NOPMD
            }
        }
    }

    /**
     * Adapted from {@code com.google.common.io.ByteStreams.copy()}.
     */
    private static long copy(InputStream from, OutputStream to)
        throws IOException {
        checkNotNull(from, "from");
        checkNotNull(to, "to");
        byte[] buf = new byte[BUF_SIZE];
        long total = 0;
        while (true) {
            int r = from.read(buf);
            if (r == -1) {
                break;
            }
            to.write(buf, 0, r);
            total += r;
        }
        return total;
    }

    /**
     * Copy of {@code com.google.common.base.Preconditions#checkState}.
     */
    public static void checkState(boolean expression,
                                  String errorMessageTemplate,
                                  Object... errorMessageArgs) {
        if (!expression) {
            throw new IllegalStateException(format(errorMessageTemplate, errorMessageArgs));
        }
    }

    public static String decodeOrDefault(byte[] data, Charset charset, String defaultValue) {
        if (data == null) {
            return defaultValue;
        }
        checkNotNull(charset, "charset");
        try {
            return charset.newDecoder().decode(ByteBuffer.wrap(data)).toString();
        } catch (CharacterCodingException ex) {
            return defaultValue;
        }
    }

}
