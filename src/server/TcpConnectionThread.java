package server;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;

public class TcpConnectionThread extends Thread {
    private static final String MESSAGE_TO_SEND = "General Kenobi...";
    
    private final ServerSocket socket;
    private final InetSocketAddress clientAddress;
    
    public TcpConnectionThread(InetSocketAddress clientAddress) throws IOException {
        this.socket = new ServerSocket(0);
        this.socket.setSoTimeout(3000);
        this.clientAddress = clientAddress;
        System.out.println("Reserved port: " + this.socket.getLocalPort());
    }
    
    @Override
    public void run() {
        try {
            sendOpenedPort();
            awaitConnection();
        }
        catch (SocketTimeoutException e) {
            System.err.println("Connection timed out");
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
    
    private void sendOpenedPort() throws IOException {
        byte[] buffer = ByteBuffer.allocate(Integer.BYTES)
                                  .putInt(socket.getLocalPort())
                                  .array();
        DatagramPacket packet = new DatagramPacket(
            buffer, buffer.length, clientAddress
        );
        DatagramSocket socket = new DatagramSocket();
        socket.send(packet);
        System.out.println("Port number sent to client: " + clientAddress);
    }
    
    private void awaitConnection() throws IOException {
        System.out.println("Waiting for connection from: " + clientAddress);
        try (Socket connectionSocket = socket.accept()) {
            receiveAndSendMessage(connectionSocket, MESSAGE_TO_SEND);
        }
    }
    
    private void receiveAndSendMessage(Socket socket, String message) throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));
             PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true)
        ) {
            System.out.println("RECEIVED: " + reader.readLine());
            printWriter.println(message);
            System.out.println("SENT: " + message);
        }
    }
    
    
    
}
