import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Perséas Charoud-Got
 * @author Fanti Samisti
 */
public class Server {

    volatile static ArrayList<Node> nodes = new ArrayList<>();


    public static void main(String[] args) {
        new Server(Integer.parseInt(args[0]));
    }

    public Server(int port) {
        final ExecutorService clientProcessingPool = Executors.newFixedThreadPool(10);

        Runnable serverTask = () -> {
            try {
                DatagramSocket serverSocket = new DatagramSocket(port);
                byte[] recv_data = new byte[1024];
                while (true) {
                    DatagramPacket recv_packet = new DatagramPacket(recv_data, recv_data.length);
                    serverSocket.receive(recv_packet);
                    clientProcessingPool.submit(new ServerTask(recv_packet));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
        Thread serverThread = new Thread(serverTask);
        serverThread.start();
    }

    public static synchronized void printNodes(){
        nodes.forEach(System.out::println);
    }

    private class ServerTask implements Runnable {
        private final DatagramPacket recv_packet;

        private ServerTask(DatagramPacket packet) {
            this.recv_packet = packet;
        }

        private int findIdxNode(String from){
            for (int i = 0; i < nodes.size(); i++) {
                if(from.equals(nodes.get(i).getIp_add()+","+nodes.get(i).getPort())) return i;
            }
            return -1;
        }

        @Override
        public void run() {
            String from = this.recv_packet.getAddress().getHostAddress() + ", " + this.recv_packet.getPort();
            InetAddress from_inet = this.recv_packet.getAddress();
            int from_port = this.recv_packet.getPort();
            String data = new String(this.recv_packet.getData(), 0, this.recv_packet.getLength());
            String [] table = data.split(",");
            String command = table[0];
            Server.printNodes();
            if(Objects.equals(command, "shared_files")){ // pck(shared_files, file1_dest1;fil2_dest2:file3_dest3;...
                String shareFiles[] = table[1].split(";");
                int idxNode = findIdxNode(from);
                if(idxNode<0){ // New node
                    nodes.add(new Node(this.recv_packet.getAddress(), this.recv_packet.getPort()));
                    idxNode = nodes.size()-1;
                }
                System.out.println("received msg");
                for (String shareFile : shareFiles) {
                    String name = shareFile.split("_")[0];
                    String dest = shareFile.split("_")[1];
                    nodes.get(idxNode).addFile(name, dest);
                }

            } else if(Objects.equals(command, "unshare")){  // pck(unshare,)
                nodes.remove(findIdxNode(from));

            } else if(Objects.equals(command, "file_req")){ // pck(file_req, fileName1;fileName2;...)
                String fileNames[] = table[1].split(";");
                BroadcasterMediator bm = new BroadcasterMediator(from_inet, from_port);
                for (int f = 0; f < fileNames.length; f++) {
                    for (int j = 0; j < nodes.size(); j++) {
                        if(nodes.get(j).hasFile(fileNames[f])) {
                            bm.file_req_resp(true, fileNames[f], nodes.get(j).getIp_add(), nodes.get(j).getPort());
                        } else{
                            bm.file_req_resp(false, fileNames[f], null, -1);
                        }
                    }
                }

            }
        }
    }
}