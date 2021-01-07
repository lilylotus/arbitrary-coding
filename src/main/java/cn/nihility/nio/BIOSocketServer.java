package cn.nihility.nio;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class BIOSocketServer {

    private ServerSocket serverSocket;
    private volatile boolean start = true;
    private int port;

    public static void main(String[] args) throws InterruptedException {
        BIOSocketServer server = new BIOSocketServer(4000);
        server.start();

       /* Thread.sleep(10 * 60 * 1000);*/

        /*server.stop();*/
    }


    public BIOSocketServer(int port) {
        this.port = port;
    }

    public void start() {
        try {
            serverSocket = new ServerSocket(port);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        while (start) {
            Socket client = null;
            try {
                // 阻塞
                client = serverSocket.accept();
                System.out.println("Accept A Client.");
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                /*readSocket(client);*/
                /*writeSocket(client);*/

                /*SocketUtil.readWrite(client);*/

                SocketUtil.writeSocket2(client, "Welcome");

            } catch (IOException e) {
                e.printStackTrace();

                try {
                    if (null != client) {
                        System.out.println("Close Client Socket");
                        client.close();
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }

        if (null != serverSocket) {
            try {
                System.out.println("Shutdown Server Socket.");
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void writeSocket(Socket client) throws IOException {
        SocketUtil.writeSocket(client, "Welcome");
        /*if (client == null) { return; }
        OutputStream outputStream = client.getOutputStream();

        BufferedOutputStream bos = null;

        try {
            System.out.println("Start Write, Client [" + client.hashCode() + "]");
            bos = new BufferedOutputStream(outputStream);

            bos.write("Welcome".getBytes());

            System.out.println("Write End.");

        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (null != bos) {
                bos.close();
            }
        }*/

    }

    private void readSocket(Socket client) throws IOException {
        SocketUtil.readSocket(client);

        /*if (null == client) { return; }

        InputStream inputStream = client.getInputStream();

        BufferedInputStream bis = null;
        ByteArrayOutputStream baos = null;

        try {
            System.out.println("Start Read. Client [" + client.hashCode() + "]");
            bis = new BufferedInputStream(inputStream);
            baos = new ByteArrayOutputStream();

            byte[] buffer = new byte[1024];
            int len;

            while ((len = bis.read(buffer)) != -1) {
                baos.write(buffer, 0, len);
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
        }*/


    }

    public void stop() {
        start = false;
    }

}
