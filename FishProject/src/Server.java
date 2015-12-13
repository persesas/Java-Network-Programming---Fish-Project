import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

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

    private int findIdxNode(String from){
        for (int i = 0; i < nodes.size(); i++) {
            if(from.contains(nodes.get(i).getIp_add().getHostAddress() + " " + nodes.get(i).getPort()))
                return i;
        }
        return -1;
    }

    public Server(int port) {
        System.out.println("Server running and waiting for connections...");
        Runnable serverTask = () -> {
            try {
                ServerSocket serverSocket = new ServerSocket(port);
                while(true) {
                    Socket socket = serverSocket.accept();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                    InetAddress from_inet = socket.getInetAddress();

                    String data;

                    if((data = reader.readLine()) != null){
                        System.out.println(data);

                        String [] table = data.split(",");
                        String command = table[0];
                        int clientPort = Integer.parseInt(table[1]);
                        String fromIP_Port = from_inet.getHostAddress() + " " + clientPort;

                        if(Objects.equals(command, "shared_files")){ // pck(shared_files, CLIENT_PORT, file1_dest1;fil2_dest2:file3_dest3;...
                            String sharedFiles[] = table[2].split(";");
                            System.out.println("Client connected from " + fromIP_Port + ", shared files: " + Arrays.toString(sharedFiles));

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
                        }
                        else if(Objects.equals(command, "unshare")){  // pck(unshare, CLIENT_PORT)
                            System.out.println("Client unshared files " + fromIP_Port);
                            nodes.remove(findIdxNode(fromIP_Port));
                        }
                        else if(Objects.equals(command, "file_req")){ // pck(file_req, CLIENT_PORT, fileName1;fileName2;...)
                            String fileNames[] = table[2].split(";");
                            System.out.println("Client " + fromIP_Port + " requested " + Arrays.toString(fileNames));

                            BroadcasterMediator bm = new BroadcasterMediator(from_inet, clientPort);
                            boolean fileFound = false;
                            for (int f = 0; f < fileNames.length; f++) {
                                for (int j = 0; j < nodes.size(); j++) {
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
                        printNodes();
                    }
                }
            }catch (IOException e) {
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
}