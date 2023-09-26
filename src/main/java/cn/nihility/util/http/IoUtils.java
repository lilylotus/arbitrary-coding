package cn.nihility.util.http;

import org.apache.commons.lang3.StringUtils;

import java.io.*;

public class IoUtils {

    private static final String ENCODE = "UTF-8";

    private IoUtils() {
    }

    /**
     * To string from stream.
     *
     * @param input    stream
     * @param encoding charset of stream
     * @return string
     * @throws IOException io exception
     */
    public static String toString(InputStream input, String encoding) throws IOException {
        if (input == null) {
            return StringUtils.EMPTY;
        }
        return (null == encoding) ? toString(new InputStreamReader(input, ENCODE))
            : toString(new InputStreamReader(input, encoding));
    }

    /**
     * To string from reader.
     *
     * @param reader reader
     * @return string
     * @throws IOException io exception
     */
    public static String toString(Reader reader) throws IOException {
        CharArrayWriter sw = new CharArrayWriter();
        copy(reader, sw);
        return sw.toString();
    }

    /**
     * Copy data.
     *
     * @param input  source
     * @param output target
     * @return copy size
     * @throws IOException io exception
     */
    public static long copy(Reader input, Writer output) throws IOException {
        char[] buffer = new char[1 << 12];
        long count = 0;
        for (int n; (n = input.read(buffer)) >= 0; ) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }


}
