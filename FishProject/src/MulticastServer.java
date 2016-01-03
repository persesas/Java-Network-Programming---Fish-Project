import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.HashMap;
import java.util.Objects;

public class MulticastServer {
    private HashMap<String, String> sharedFiles;

    public MulticastServer(HashMap<String, String> sharedFiles){
        this.sharedFiles = sharedFiles;
        start();
    }

    private void start(){
        Runnable serverTask = () -> {
            try {
                MulticastSocket socket = new MulticastSocket(Client.MULTICAST_PORT);
                InetAddress group = InetAddress.getByName(Client.IP_MULTICAST);
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
                        UDPBroadcaster.setSend(false);

                        System.out.println("(MulticastServer) received " + received);
                        if(Objects.equals(command,"discovery")){ //pcK(discovery,)
                            int port = Integer.parseInt(table[1]);
                            String fileName = table[2];
                            System.out.println("(MulticastServer) Client " + fromIP + " requested " + fileName);
                            if(sharedFiles.containsKey(fileName)){
                                System.out.println("(MulticastServer) The file " + fileName + " is being shared in this client");
                            }
                            else System.out.println(fileName + " not found");
                        }
                    }
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        };

        (new Thread(serverTask)).start();
    }

    public void setSharedFiles(HashMap<String, String> newFiles){  //TODO use it when sharing in client
        this.sharedFiles = newFiles;
    }
}
