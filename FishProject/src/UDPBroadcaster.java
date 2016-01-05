import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class UDPBroadcaster implements Runnable {
    private String msg;
    private volatile static boolean send = false;

    public UDPBroadcaster(String msg){
        this.msg = msg;
    }

    @Override
    public void run() {
        try {
            MulticastSocket socket = new MulticastSocket(Client.MULTICAST_PORT);
            InetAddress group = InetAddress.getByName(Client.IP_MULTICAST);
            socket.joinGroup(group);

            byte[] buf = msg.getBytes();
            DatagramPacket packet = new DatagramPacket(buf, buf.length, group, Client.MULTICAST_PORT);
            send = true;
            socket.send(packet);
            System.out.println("(UDPBroadcaster) sending " + msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized static boolean isSent(){
        return send;
    }

    public synchronized static void setSend(boolean send){
        UDPBroadcaster.send = send;
    }
}
