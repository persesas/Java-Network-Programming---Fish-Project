import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.HashMap;
import java.util.Objects;

/**
 * Represents a Multicast UDP Server
 * @author Perséas Charoud-Got
 * @author Fanti Samisti
 */
public class MulticastServer {
    private HashMap<String, String> sharedFiles;
    private int clientPortServer;
    private int multicastPort;
    private String ipMulticast;

    /**
     * Constructs a MulticastServer
     * @param ipMulticast - IP address of the multicast group
     * @param multicastPort - Port of the multicast group
     * @param clientPort - Client port (TCP)
     * @param sharedFiles - Map with all shared files/paths
     */
    public MulticastServer(String ipMulticast, int multicastPort, int clientPort, HashMap<String, String> sharedFiles){
        this.ipMulticast = ipMulticast;
        this.multicastPort = multicastPort;
        this.clientPortServer = clientPort;
        this.sharedFiles = sharedFiles;
        start();
    }

    // starts the server
    private void start(){
        Runnable serverTask = () -> {
            try {
                MulticastSocket socket = new MulticastSocket(multicastPort);
                InetAddress group = InetAddress.getByName(ipMulticast);
                socket.joinGroup(group);
                while(true){
                    byte[] buf = new byte[256];

                    DatagramPacket packet = new DatagramPacket(buf, buf.length);
                    socket.receive(packet);
                    if(!UDPBroadcaster.isSent()){
                        String received = new String(packet.getData(), 0, packet.getLength());

                        String [] table = received.split(",");
                        String command = table[0];
                        String fromIP = packet.getAddress().getHostAddress();

                        System.out.println("(MulticastServer) received " + received);
                        if(Objects.equals(command,"discovery")){ //pcK(discovery,)
                            int port = Integer.parseInt(table[1]);
                            String fileName = table[2];
                            System.out.println("(MulticastServer) Client " + fromIP + " requested " + fileName);
                            if(sharedFiles.containsKey(fileName)){
                                System.out.println("(MulticastServer) The file " + fileName + " is being shared in this client");
                                BroadcasterMediator broadcasterMediator = new BroadcasterMediator(packet.getAddress(),port);
                                broadcasterMediator.discoveryResp(fileName, sharedFiles.get(fileName), clientPortServer);
                            }
                            else System.out.println(fileName + " not found");
                        }
                    }
                    UDPBroadcaster.setSend(false);
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        };

        (new Thread(serverTask)).start();
    }
}
