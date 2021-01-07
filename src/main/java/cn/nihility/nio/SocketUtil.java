package cn.nihility.nio;

import java.io.*;
import java.net.Socket;
import java.time.Instant;

public class SocketUtil {

    public static void writeSocket(Socket socket, String msg) throws IOException {
        if (socket == null) { return; }
        OutputStream outputStream = socket.getOutputStream();
        BufferedOutputStream bos = null;
        try {
            System.out.println("Start Write, Client [" + socket.hashCode() + "]");
            bos = new BufferedOutputStream(outputStream);
            bos.write(String.valueOf(msg).getBytes());
            bos.flush();
            System.out.println("Write End.");
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (null != bos) {
                bos.close();
            }
        }

    }

    public static void writeSocket2(Socket socket, String msg) throws IOException {
        if (socket == null) { return; }
        OutputStream outputStream = socket.getOutputStream();
        PrintWriter bos = null;
        try {
            System.out.println("Start Write, Client [" + socket.hashCode() + "]");
            bos = new PrintWriter(outputStream);
            bos.println(msg);
            bos.flush();
            System.out.println("Write End.");
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (null != bos) {
                bos.close();
            }
        }

    }

    public static void writeOutputStream(OutputStream outputStream, String msg) throws IOException {
        if (outputStream == null) { return; }
        BufferedOutputStream bos = null;
        try {
            System.out.println("Start Write, msg [" + msg + "]");
            bos = new BufferedOutputStream(outputStream);
            bos.write(String.valueOf(msg).getBytes());
            bos.flush();
            System.out.println("Write End.");
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (null != bos) {
                bos.close();
            }
        }
    }

    public static void readWrite(Socket socket) throws IOException {
        if (socket == null) { return; }

        OutputStream outputStream = socket.getOutputStream();
        InputStream inputStream = socket.getInputStream();

        readInputStream(inputStream);
        writeOutputStream(outputStream, "message : [" + Instant.now().toEpochMilli() + "]");

        closeSteam(inputStream, outputStream);
    }

    public static void writeRead(Socket socket) throws IOException {
        if (socket == null) { return; }

        OutputStream outputStream = socket.getOutputStream();
        InputStream inputStream = socket.getInputStream();

        writeOutputStream(outputStream, "message : [" + Instant.now().toEpochMilli() + "]");
        readInputStream(inputStream);

        closeSteam(inputStream, outputStream);
    }

    public static void closeSteam(InputStream inputStream, OutputStream outputStream) {
        if (null != inputStream) {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (null != outputStream) {
            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void readSocket(Socket socket) throws IOException {
        if (null == socket) { return; }

        if (socket.isClosed()) {
            System.out.println("Socket Closed");
            return;
        }

        InputStream inputStream = socket.getInputStream();
        BufferedInputStream bis = null;
        ByteArrayOutputStream baos = null;
        try {
            System.out.println("Start Read. Client [" + socket.hashCode() + "]");
            bis = new BufferedInputStream(inputStream);
            baos = new ByteArrayOutputStream();

            byte[] buffer = new byte[1024];
            int len;

            while ((len = bis.read(buffer)) > 0) {
                baos.write(buffer, 0, len);
                System.out.println("Read len [" + len + "]");
            }

            byte[] recBytes = baos.toByteArray();

            String rec = new String(recBytes, 0, recBytes.length);
            System.out.println("rec [" + rec + "] len [" + recBytes.length + "]");
            System.out.println("Read End");

        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (baos != null) {
                baos.close();
            }
            if (bis != null) {
                bis.close();
            }
        }
    }

    public static void readInputStream(InputStream inputStream) throws IOException {
        if (null == inputStream) { return; }
        BufferedInputStream bis = null;
        ByteArrayOutputStream baos = null;
        try {
            System.out.println("Start Read.");
            bis = new BufferedInputStream(inputStream);
            baos = new ByteArrayOutputStream();

            byte[] buffer = new byte[1024];
            int len;

            while ((len = bis.read(buffer)) > 0) {
                baos.write(buffer, 0, len);
                System.out.println("Read len [" + len + "]");
            }

            byte[] recBytes = baos.toByteArray();

            String rec = new String(recBytes, 0, recBytes.length);
            System.out.println("rec [" + rec + "] len [" + recBytes.length + "]");
            System.out.println("Read End");

        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (baos != null) {
                baos.close();
            }
            if (bis != null) {
                bis.close();
            }
        }
    }

}
