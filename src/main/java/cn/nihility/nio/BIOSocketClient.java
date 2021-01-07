package cn.nihility.nio;

import java.io.IOException;
import java.net.Socket;

public class BIOSocketClient {

    private int port;
    private Socket socket;
    private String name;

    public BIOSocketClient(int port) {
        this.port = port;
    }

    public BIOSocketClient(int port, String name) {
        this.port = port;
        this.name = name;
    }

    public static void main(String[] args) {
        BIOSocketClient client = new BIOSocketClient(4000, "INDEX:1");
        client.start();
    }

    public void start() {
        try {
            socket = new Socket("127.0.0.1", port);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        try {
            /*writeThings(socket);*/
            /*readThings(socket);*/

           /* SocketUtil.writeRead(socket);*/
            SocketUtil.readSocket(socket);

        } catch (IOException e) {
            e.printStackTrace();
        }

        /*if (socket != null) {
            try {
                System.out.println("Close Socket");
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }*/
    }

    private void readThings(Socket socket) throws IOException {
        SocketUtil.readSocket(socket);
    }

    private void writeThings(Socket socket) throws IOException {
        SocketUtil.writeSocket(socket, "Hello <-> [" + name + "]");
        /*if (null == socket) return;

        OutputStream outputStream = socket.getOutputStream();
        BufferedOutputStream bos = null;

        try {
            System.out.println("Start Write, Socket [" + socket.hashCode() + "]");
            bos = new BufferedOutputStream(outputStream);
            bos.write(("Hello <-> [" + name + "]").getBytes());
            System.out.println("Write End.");
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (null != bos) {
                bos.close();
            }
        }*/
    }

}
