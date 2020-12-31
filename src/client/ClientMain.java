package client;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Arrays;

public class ClientMain {
    public static void main(String[] args) {
        if (args.length <= 1) {
            System.err.println("Not enough parameters");
            System.exit(0);
        }
        
        try {
            InetAddress address = InetAddress.getByName(args[0].trim());
            int[] ports = parseAsInts(Arrays.copyOfRange(args, 1, args.length));
            
            Client client = new Client(address);
            client.start(ports);
        }
        catch (NumberFormatException e) {
            throw new RuntimeException("Illegal parameter passed - port number required");
        }
        catch (UnknownHostException e) {
            System.err.println("Invalid server IP");
        }
        catch (SocketTimeoutException e) {
            System.err.println("Request timed out");
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
    
    private static int[] parseAsInts(String[] args) throws NumberFormatException {
        return Arrays.stream(args)
                     .map(String::trim)
                     .mapToInt(Integer::parseInt)
                     .toArray();
    }
}
