package server;

import java.io.UncheckedIOException;
import java.util.Arrays;

public class ServerMain {
    public static void main(String[] args) {
        try {
            int[] portSequence = parseAsInts(args);
            
            Server server = new Server(portSequence);
            server.listen();
        }
        catch (NumberFormatException e) {
            throw new RuntimeException("Illegal parameter passed - port number required");
        }
        catch (UncheckedIOException e) {
            System.err.println(e.getMessage());
        }
    }
    
    private static int[] parseAsInts(String[] args) throws NumberFormatException {
        return Arrays.stream(args)
                     .map(String::trim)
                     .mapToInt(Integer::parseInt)
                     .toArray();
    }
    
}
