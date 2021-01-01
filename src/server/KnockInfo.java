package server;

import java.net.InetAddress;
import java.net.InetSocketAddress;

public class KnockInfo implements Comparable<KnockInfo> {
    private final int serverPort;
    private final InetSocketAddress clientSocketAddress;
    private final long createTime;
    
    public KnockInfo(int serverPort, InetSocketAddress clientSocketAddress, long createTime) {
        this.serverPort = serverPort;
        this.clientSocketAddress = clientSocketAddress;
        this.createTime = createTime;
    }
    
    public KnockInfo(int serverPort, InetAddress clientAddress, int clientPort, long createTime) {
        this(serverPort, new InetSocketAddress(clientAddress, clientPort), createTime);
    }
    
    
    public int getServerPort() {
        return serverPort;
    }
    
    public InetSocketAddress getClientSocketAddress() {
        return clientSocketAddress;
    }
    
    @Override
    public int compareTo(KnockInfo o) {
        return Long.compare(this.createTime, o.createTime);
    }
}
