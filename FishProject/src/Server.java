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
        // int port = Integer.parseInt(args[0]);
        new Server(8000);
    }

    public Server(int port) {
        System.out.println("Running and waiting for connections...");
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
        System.out.println("v-------- nodes -----v");
        nodes.forEach(System.out::println);
        System.out.println("^--------------------^");
    }

    private class ServerTask implements Runnable {
        private final DatagramPacket recv_packet;

        private ServerTask(DatagramPacket packet) {
            this.recv_packet = packet;
        }

        private int findIdxNode(String from){
            for (int i = 0; i < nodes.size(); i++) {
                if(from.contains(nodes.get(i).getIp_add().getHostAddress() + " " + nodes.get(i).getPort()))
                    return i;
            }
            return -1;
        }

        @Override
        public void run() {
            InetAddress from_inet = this.recv_packet.getAddress();
            String data = new String(this.recv_packet.getData(), 0, this.recv_packet.getLength());

            String [] table = data.split(",");
            String command = table[0];
            int clientPort = Integer.parseInt(table[1]);
            String fromIP_Port = this.recv_packet.getAddress().getHostAddress() + " " + clientPort;

            System.out.println("SERVER : " + data);

            if(Objects.equals(command, "shared_files")){ // pck(shared_files, CLIENT_PORT, file1_dest1;fil2_dest2:file3_dest3;...
                String sharedFiles[] = table[2].split(";");
                int idxNode = findIdxNode(fromIP_Port);
                if(idxNode<0){ // New node
                    nodes.add(new Node(from_inet, clientPort));
                    idxNode = nodes.size()-1;
                }
                for (int i = 0; i < sharedFiles.length; i++) {
                    String name = sharedFiles[i].split("_")[0];
                    String dest = sharedFiles[i].split("_")[1];
                    nodes.get(idxNode).addFile(name, dest);
                }
                // print result of nodes
                Server.printNodes();
            }
            else if(Objects.equals(command, "unshare")){  // pck(unshare, CLIENT_PORT)
                nodes.remove(findIdxNode(fromIP_Port));
            }
            else if(Objects.equals(command, "file_req")){ // pck(file_req, CLIENT_PORT, fileName1;fileName2;...)
                String fileNames[] = table[2].split(";");
                BroadcasterMediator bm = new BroadcasterMediator(from_inet, clientPort);
                boolean fileFound = false;
                for (int f = 0; f < fileNames.length; f++) {
                    for (int j = 0; j < nodes.size(); j++) {
                        System.out.println(nodes.get(j).hasFile(fileNames[f]));
                        fileFound = nodes.get(j).hasFile(fileNames[f]);
                        if (fileFound) {
                            Node n = nodes.get(j);
                            bm.file_req_resp(true, fileNames[f], n.getPath(fileNames[f]), n.getIp_add(), n.getPort());
                            //TODO return all nodes having the files
                        }
                    }
                    if(!fileFound) bm.file_req_resp(false, fileNames[f], null,  null, -1); //TODO not very nicely coded
                }
            }
        }
    }
}