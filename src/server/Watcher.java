package server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

public class Watcher extends Thread {
    private final int[] portSequence;
    private final BlockingQueue<KnockInfo> knocks = new PriorityBlockingQueue<>();
    private final KnockStateHolder knockStateHolder = new KnockStateHolder();
    
    
    public Watcher(int[] portSequence) {
        this.portSequence = portSequence;
    }
    
    @Override
    public void run() {
        while (true) {
            try {
                KnockInfo knock = knocks.take();
                knockStateHolder.update(
                    knock.getServerPort(), knock.getClientSocketAddress()
                );
                acceptReadyConnections();
            }
            catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    private void acceptReadyConnections() throws IOException {
        for (InetSocketAddress address : knockStateHolder.removeReadyConnections()) {
            System.out.println("Accepted packet sequence from: " + address.getAddress() + ":" + address.getPort());
            new TcpConnectionThread(
                new InetSocketAddress(address.getAddress(), address.getPort())
            ).start();
        }
    }
    
    public void addKnock(KnockInfo knockInfo) {
        knocks.add(knockInfo);
    }
    
    
    private class KnockStateHolder {
        private Map<InetSocketAddress, Integer> nextWantedPortIndexes = new HashMap<>();
        private List<InetSocketAddress> readyConnections = new ArrayList<>();
        
        public void update(int serverPort, InetSocketAddress socketAddress) {
            int nextPortIndex = nextWantedPortIndexes.getOrDefault(socketAddress, 0);
            
            if (serverPort == portSequence[nextPortIndex]) {
                nextWantedPortIndexes.merge(
                    socketAddress, 1, Integer::sum
                );
                if (nextWantedPortIndexes.get(socketAddress) == portSequence.length) {
                    nextWantedPortIndexes.remove(socketAddress);
                    readyConnections.add(socketAddress);
                }
            }
            else
                nextWantedPortIndexes.remove(socketAddress);
            
        }
        
        public List<InetSocketAddress> removeReadyConnections() {
            List<InetSocketAddress> connections = new ArrayList<>(readyConnections);
            readyConnections.clear();
            return connections;
        }
    }
}
