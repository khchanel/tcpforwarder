package dev.tcpforwarder;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
    public static void main(String[] args) {
        if (args.length != 3) {
            System.err.println("Usage: java TcpForwarder <incomingPort> <targetHost> <targetPort>");
            System.exit(1);
        }

        int incomingPort = Integer.parseInt(args[0]);
        String targetHost = args[1];
        int targetPort = Integer.parseInt(args[2]);

        try {
            ServerSocket serverSocket = new ServerSocket(incomingPort);
            System.out.println("TCP Port Forward started. Listening on port " + incomingPort);

            while (true) {
                Socket incomingSocket = serverSocket.accept();
                System.out.println("Incoming connection from " + incomingSocket.getInetAddress());

                Socket targetSocket = new Socket(targetHost,targetPort);
                System.out.println("Connecting to target host " + targetHost + ":" + targetPort);

                Thread forwarderThread = new Thread(() -> forwardData(incomingSocket, targetSocket));
                Thread reverseForwarderThread = new Thread(() -> forwardData(targetSocket, incomingSocket));

                forwarderThread.start();
                reverseForwarderThread.start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void forwardData(Socket sourceSocket, Socket destinationSocket) {
        try {
            InputStream inputStream = sourceSocket.getInputStream();
            OutputStream outputStream = destinationSocket.getOutputStream();

            byte[] buffer = new byte[1024];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
                outputStream.flush();
            }

            sourceSocket.close();
            destinationSocket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}