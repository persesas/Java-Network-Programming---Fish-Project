import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author Perséas Charoud-Got
 * @author Fanti Samisti
 */
public class Broadcaster implements Runnable {
    // This class sends an update to the other players-clients

    private String ip; // (IP Port) list of the nodes needing this update.
    private int port;
    private String msg;

    /**
     * Constructs the Broadcaster sending a msg to a given ip:port
     * @param ip - ip used
     * @param port - Port used
     * @param msg - Message to send to all nodes
     */
    public Broadcaster(String ip, int port, String msg) {
        this.ip = ip;
        this.port = port;
        this.msg = msg;
    }

    @Override
    public void run() {
        try {
            DatagramSocket socket = new DatagramSocket();

            InetAddress addr = InetAddress.getByName(ip);

            DatagramPacket packet = new DatagramPacket(msg.getBytes(),
                        msg.getBytes().length, addr, port);
            socket.send(packet);


        } catch (UnknownHostException e) {
            System.out.println("Unknown host");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}