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

    private String toIp;
    private int toPort;
    private String msg;

    /**
     * Constructs the Broadcaster sending a msg to a given toIp:toPort
     * @param toIp - toIp used
     * @param toPort - Port used
     * @param msg - Message to send to all nodes
     */
    public Broadcaster(String toIp, int toPort, String msg) {
        this.toIp = toIp;
        this.toPort = toPort;
        this.msg = msg;
    }

    @Override
    public void run() {
        try {
            DatagramSocket socket = new DatagramSocket();

            InetAddress addr = InetAddress.getByName(toIp);

            DatagramPacket packet = new DatagramPacket(msg.getBytes(),
                        msg.getBytes().length, addr, toPort);
            System.out.println("msg sent " + " at " + addr + " " + toPort + " : " + msg);
            socket.send(packet);


        } catch (UnknownHostException e) {
            System.out.println("Unknown host");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}