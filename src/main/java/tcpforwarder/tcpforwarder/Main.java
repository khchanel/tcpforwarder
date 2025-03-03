package tcpforwarder.tcpforwarder;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {

    private static final int THREAD_POOL_SIZE = 20;
    private static volatile boolean running = true;

    public static void main(String[] args) {

        if (args.length != 3) {
            System.err.println("Usage: java TcpForwarder <incomingPort> <targetHost> <targetPort>");
            System.exit(1);
        }

        int incomingPort = Integer.parseInt(args[0]);
        String targetHost = args[1];
        int targetPort = Integer.parseInt(args[2]);
        ExecutorService threadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

        try (ServerSocket serverSocket = new ServerSocket(incomingPort)){
            System.out.println("TCP Port Forward started. Listening on port " + incomingPort);

            // Handle shutdown gracefully
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Shutting down TCP Forwarder...");
                running = false;
                threadPool.shutdown();
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    System.err.println("Error closing server socket: " + e.getMessage());
                }
            }));

            while (running) {
                try {
                    Socket incomingSocket = serverSocket.accept();
                    System.out.println("Incoming connection from " + incomingSocket.getInetAddress());

                    Socket targetSocket = new Socket(targetHost,targetPort);
                    System.out.println("Connecting to target host " + targetHost + ":" + targetPort);

                    threadPool.submit(() -> forwardData(incomingSocket, targetSocket));
                    threadPool.submit(() -> forwardData(targetSocket, incomingSocket));

                } catch (IOException e) {
                    if(running) {
                        System.err.println("Failed to accept incoming connection: " + e.getMessage());
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("Failed to start server: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void forwardData(Socket sourceSocket, Socket destinationSocket) {
        try {
            InputStream inputStream = sourceSocket.getInputStream();
            OutputStream outputStream = destinationSocket.getOutputStream();

            byte[] buffer = new byte[1024];
            int bytesRead;

            while (!sourceSocket.isClosed() && (bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
                outputStream.flush();
            }

            sourceSocket.close();
            destinationSocket.close();

        } catch (SocketException e) {
            if ("Socket closed".equals(e.getMessage())) {
                System.out.println("Connection closed by remote host.");
            } else {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                sourceSocket.close();
                destinationSocket.close();
            } catch (IOException ignored) {}
        }
    }
}