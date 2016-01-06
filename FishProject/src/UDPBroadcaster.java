import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

/**
 * Represents a UDP broadcaster
 * @author Perséas Charoud-Got
 * @author Fanti Samisti
 */
public class UDPBroadcaster implements Runnable {
    private String msg;
    private volatile static boolean send = false;
    private String multicastIP;
    private int multicastPort;

    /**
     * Constructor of the UDBroadcaster
     * @param multicastIP - IP address of the multicast group
     * @param multicastPort - Port of the multicast group
     * @param msg - Message to be sent on the group
     */
    public UDPBroadcaster(String multicastIP, int multicastPort, String msg){
        this.multicastIP = multicastIP;
        this.multicastPort = multicastPort;
        this.msg = msg;
    }

    @Override
    public void run() {
        try {
            MulticastSocket socket = new MulticastSocket(multicastPort);
            InetAddress group = InetAddress.getByName(multicastIP);
            socket.joinGroup(group);

            byte[] buf = msg.getBytes();
            DatagramPacket packet = new DatagramPacket(buf, buf.length, group, multicastPort);
            send = true;
            socket.send(packet);
            System.out.println("(UDPBroadcaster) sending " + msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @return the boolean is sent, telling if the broadcaster sent a message or not
     */
    public synchronized static boolean isSent(){
        return send;
    }

    /**
     *
     * @param send - this.send will take the value given
     */
    public synchronized static void setSend(boolean send){
        UDPBroadcaster.send = send;
    }
}
