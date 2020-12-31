package server;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Objects;

public class Server {
    private final int[] portSequence;
    private final KnockStateHolder knockStateHolder = new KnockStateHolder();
    
    public Server(int ... portSequence) {
        this.portSequence = portSequence;
    }
    
    public void listen() {
        int[] portsToListen = Arrays.stream(portSequence).distinct().toArray();
        System.out.println(
            "Listening for port sequence: " +
            Arrays.stream(portSequence).collect(
                StringBuilder::new, (sb, n) -> sb.append(n).append(" "), StringBuilder::append)
        );
        for (int port : portsToListen) {
            new Thread(
                () -> listenPortUnchecked(port)
            ).start();
        }
    }
    
    private void listenPortUnchecked(int port) {
        try {
            listenPort(port);
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
            System.out.println("Packet received from " + packet.getSocketAddress() + " on port: " + port);
            knockStateHolder.update(port, packet.getAddress(), packet.getPort());

            if (knockStateHolder.isValid()) {
                knockStateHolder.restart();
                System.out.println("Accepted packet sequence from: " + packet.getAddress() + ":" + packet.getPort());
                new TcpConnectionThread(
                    new InetSocketAddress(packet.getAddress(), packet.getPort())
                ).start();
            }
        }
    }
    
    
    private class KnockStateHolder {
        private int nextWantedPortIndex;
        private InetAddress lastClientAddress;
        private int lastClientPort;
        
        public synchronized void update(int serverPort, InetAddress clientAddress, int clientPort) {
            if (incorrectAddress(clientAddress, clientPort))
                restartWith(clientAddress, clientPort);
            if (serverPort == portSequence[nextWantedPortIndex])
                ++nextWantedPortIndex;
        }
        
        private synchronized boolean incorrectAddress(InetAddress address, int port) {
            return !Objects.equals(address, lastClientAddress) || port != lastClientPort;
        }
        
        public synchronized void restart() {
            restartWith(null, -1);
        }
        
        private synchronized void restartWith(InetAddress clientAddress, int clientPort) {
            nextWantedPortIndex = 0;
            lastClientAddress = clientAddress;
            lastClientPort = clientPort;
        }
        
        public synchronized boolean isValid() {
            return nextWantedPortIndex == portSequence.length;
        }
    }
}
