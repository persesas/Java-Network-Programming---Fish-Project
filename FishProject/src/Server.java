import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;

/**
 * Represents the Server
 * @author Perséas Charoud-Got
 * @author Fanti Samisti
 */
public class Server {
    private volatile static ArrayList<Node> nodes = new ArrayList<>();
    private CrashHandler crashHandler;
    private DBMediator dbMediator;

    public static void main(String[] args) {
        // int port = Integer.parseInt(args[0]);
        new Server(8000);
    }


    //Given a String from, returns the position in the ArrayList nodes where the from is found and -1 if not found
    private int findIdxNode(String from){
        for (int i = 0; i < nodes.size(); i++) {
            if(from.contains(nodes.get(i).getIp_add().getHostAddress() + " " + nodes.get(i).getPort()))
                return i;
        }
        return -1;
    }


    /**
     * Creates a server listening to a given port
     * @param port - port listening to
     */
    public Server(int port) {
        System.out.println("Server running and waiting for connections...");

        crashHandler = new CrashHandler(nodes, port); // start crash Handler
        dbMediator = DBMediator.getInstance();

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

                            for (String sharedFile : sharedFiles) {
                                String name = sharedFile.split("_")[0];
                                String dest = sharedFile.split("_")[1];
                                nodes.get(idxNode).addFile(name, dest);
                                dbMediator.createNode(from_inet.getHostAddress(), Integer.toString(clientPort), name, dest);
                            }
                            printNodes();
                        }
                        else if(Objects.equals(command, "unshare")){  // pck(unshare, CLIENT_PORT)
                            System.out.println("Client unshared files " + fromIP_Port);
                            nodes.remove(findIdxNode(fromIP_Port));
                            dbMediator.deleteNode(from_inet.getHostAddress(), Integer.toString(clientPort));
                            printNodes();
                        }
                        else if(Objects.equals(command, "file_req")){ // pck(fileReq, CLIENT_PORT, fileName1;fileName2;...)
                            String fileNames[] = table[2].split(";");
                            System.out.println("Client " + fromIP_Port + " requested " + Arrays.toString(fileNames));

                            BroadcasterMediator bm = new BroadcasterMediator(from_inet, clientPort);

                            for (String fileName : fileNames) {
                                String msgWithNodes = createNodesMessage(fileName,dbMediator.getFile(fileName)); // with DB
                                //String msgWithNodes = createNodesMessage(fileName,nodesHavingFile(fileName)); // without DB
                                bm.file_req_resp(fileName, msgWithNodes);
                            }

                        } else if(Objects.equals(command, "lookup")){ // pck(lookup, CLIENT_PORT, file1)
                            /*
                                reply: node1:file1,file2;node2:file3,file4
                             */
                            String query = table[2];
                            BroadcasterMediator bm = new BroadcasterMediator(from_inet, clientPort);

                            String msgWithNodes = createRegexMessage(searchNodes(query));
                            bm.lookupResp(query, msgWithNodes);
                        } else if(Objects.equals(command, "state")) { // pck(lookup, CLIENT_PORT, file1)
                            int idxNode = findIdxNode(fromIP_Port);
                            BroadcasterMediator bm = new BroadcasterMediator(from_inet, clientPort);
                            if(idxNode<0){ // not  shared anything yet
                                bm.stateResp("notfound");
                            } else {
                                Node n = nodes.get(idxNode);
                                bm.stateResp(n.filesToString());
                            }
                        } else if(Objects.equals(command,"ping_resp")){  // pck(ping,)
                            crashHandler.receivedPing(new Node(socket.getInetAddress(), clientPort));
                        }
                        crashHandler.updateNodes();
                        // crashHandler.setNodes(nodes); // updating list of nodes witout db
                    }
                }
            }catch (IOException e) {
                e.printStackTrace();
            }
        };

        Thread serverThread = new Thread(serverTask);
        serverThread.start();
    }

    private HashMap searchNodes(String query) {
        HashMap<Node, ArrayList<String>> result = new HashMap<>();

        for(Node n : nodes) {
            ArrayList<String> matches = n.search(query);
            if(matches.size() != 0) result.put(n, matches);
        }

        return result;
    }

    private String createRegexMessage(HashMap<Node, ArrayList<String>> results) {
        StringBuilder sb = new StringBuilder();
        for (Node n: results.keySet()){
            for(String filename: results.get(n)) {
                String[] data = filename.split("_");
                sb.append(n.getIp_add())
                  .append("::")
                  .append(n.getPort())
                  .append("::")
                  .append(data[0])
                  .append("::")
                  .append(data[1])
                        .append("<->");
            }
        }
        return sb.toString();
    }


    private Node nodeHasFile(String fileName){
        for(Node n: nodes){
            if(n.hasFile(fileName)) return n;
        }
        return null;
    }

    private ArrayList<Node> nodesHavingFile(String filename){
        ArrayList<Node> returnNodes = new ArrayList<>();
        for(Node n: nodes){
            //System.out.println(n.search(pattern));
            if(n.hasFile(filename)) returnNodes.add(n);
        }
        return returnNodes;
    }

    private String createNodesMessage(String filename, ArrayList<Node> nodes){
        StringBuilder sb = new StringBuilder();
        for (Node n: nodes){
            sb.append(n.getIp_add()).append("::").append(n.getPort()).append("::").append(n.getPath(filename)).append("<->");
        }
        return sb.toString();
    }


    /**
     * Prints all the registered nodes in the Server in a nice format (For debugging)
     */
    public static synchronized void printNodes(){
        System.out.println("v-------- nodes -----v");
        nodes.forEach(System.out::println);
        System.out.println("^--------------------^");
    }
}