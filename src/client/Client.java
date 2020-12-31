package client;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.Scanner;

public class Client {
    private static final String MESSAGE_TO_SEND = "Hello there!";
    
    private final InetAddress serverAddress;
    private final DatagramSocket socket;
    
    public Client(InetAddress serverAddress) throws SocketException {
        this.serverAddress = serverAddress;
        this.socket = new DatagramSocket();
    }
    
    public void start(int[] ports) throws IOException {
        sendKnocks(ports);
        
        int tcpPort = awaitTcpPort();
        System.out.println("Port number received: " + tcpPort);
        
        sendAndReceiveMessage(tcpPort, MESSAGE_TO_SEND);
    }
    
    private void sendKnocks(int[] ports) {
        for (int port : ports)
            sendKnockPacketUnchecked(port);
    }
    
    private void sendKnockPacketUnchecked(int port) {
        try {
            sendKnockPacket(port);
            System.out.println("Knock sent on port: " + port);
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
    
    private void sendKnockPacket(int port) throws IOException {
        byte[] buffer = new byte[0];
        DatagramPacket packet = new DatagramPacket(
            buffer, buffer.length, serverAddress, port
        );
        socket.send(packet);
    }
    
    private int awaitTcpPort() throws IOException {
        System.out.println("Waiting for port number...");
        byte[] buffer = new byte[Integer.BYTES];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        socket.setSoTimeout(3000);
        
        socket.receive(packet);
        return ByteBuffer.wrap(packet.getData()).getInt();
    }
    
    public void sendAndReceiveMessage(int port, String message) throws IOException {
        try (Socket tcpSocket = new Socket(serverAddress, port);
            Scanner scanner = new Scanner(tcpSocket.getInputStream());
            PrintWriter printWriter = new PrintWriter(tcpSocket.getOutputStream(), true))
        {
            printWriter.println(message);
            System.out.println("SENT: " + message);
            
            String respond = scanner.nextLine();
            System.out.println("RECEIVED: " + respond);
        }
    }
    
    
}
