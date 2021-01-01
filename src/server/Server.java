package server;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Arrays;

public class Server {
    private final int[] portSequence;
    private final Watcher watcher;
    
    public Server(int ... portSequence) {
        this.portSequence = portSequence;
        this.watcher = new Watcher(this.portSequence);
    }
    
    public void listen() {
        int[] portsToListen = Arrays.stream(portSequence).distinct().toArray();
        System.out.println(
            "Listening for port sequence: " +
            Arrays.stream(portSequence).collect(
                StringBuilder::new, (sb, n) -> sb.append(n).append(" "), StringBuilder::append)
        );
        
        watcher.start();
        
        for (int port : portsToListen) {
            try {
                new Thread(
                    () -> listenPortUnchecked(port)
                ).start();
            }
            catch (RuntimeException e) {
                System.err.println(e.getMessage());
            }
        }
    }
    
    private void listenPortUnchecked(int port) {
        try {
            listenPort(port);
        }
        catch (IllegalArgumentException e) {
            System.err.println("Invalid port number");
            System.exit(0);
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
    
    private void listenPort(int port) throws IOException {
        DatagramSocket socket = new DatagramSocket(port);
        byte[] buffer = new byte[64];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        
        while (true) {
            socket.receive(packet);
            long time = System.nanoTime();
            System.out.println("Packet received from " + packet.getSocketAddress() + " on port: " + port);
            watcher.addKnock(
                new KnockInfo(port, packet.getAddress(), packet.getPort(), time)
            );
        }
    }
}
