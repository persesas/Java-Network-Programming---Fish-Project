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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Represents the Server
 * @author Perséas Charoud-Got
 * @author Fanti Samisti
 */
public class Server {
    private CrashHandler crashHandler;
    private DBMediator dbMediator;

    public static void main(String[] args) {
        int port = 8000;
        if(args.length>0) {
            port = Integer.parseInt(args[0]);
        }
        new Server(port);
    }


    /**
     * Creates a server listening to a given port
     * @param port - port listening to
     */
    public Server(int port) {
        System.out.println("Server running and waiting for connections...");

        crashHandler = new CrashHandler(port); // start crash Handler
        dbMediator = DBMediator.getInstance();

        final ExecutorService clientProcessingPool = Executors.newFixedThreadPool(10);

        Runnable serverTask = () -> {
            ServerSocket serverSocket = null;
            try {
                serverSocket = new ServerSocket(port);
                while(true) {
                    Socket socket = serverSocket.accept();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    clientProcessingPool.submit(new ServerTask(reader, socket));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        };

        new Thread(serverTask).start();
    }

    /*
    private HashMap searchNodes(String query) {
        HashMap<Node, ArrayList<String>> result = new HashMap<>();

        for(Node n : nodes) {
            ArrayList<String> matches = n.search(query);
            if(matches.size() != 0) result.put(n, matches);
        }

        return result;
    }
    */

    private String createRegexMessage(ArrayList<String> results) {
        StringBuilder sb = new StringBuilder();
        for(String s: results){
            sb.append(s);
        }
        return sb.toString();
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
    public synchronized void printNodes(){
        System.out.println("v-------- nodes -----v");
        dbMediator.getAllNodes().forEach(System.out::println);
        System.out.println("^--------------------^");
    }

    private class ServerTask implements Runnable {
        private BufferedReader reader;
        private Socket socket;

        public ServerTask(BufferedReader reader, Socket socket) {
            this.reader = reader;
            this.socket = socket;
        }

        @Override
        public void run() {
            InetAddress from_inet = socket.getInetAddress();

            String data;

            try {
                if ((data = reader.readLine()) != null) {
                    System.out.println("(Server) " + data);

                    String[] table = data.split(",");
                    String command = table[0];
                    int clientPort = Integer.parseInt(table[1]);
                    String fromIP_Port = from_inet.getHostAddress() + " " + clientPort;

                    if (Objects.equals(command, "shared_files")) { // pck(shared_files, CLIENT_PORT, file1_dest1;fil2_dest2:file3_dest3;...
                        String sharedFiles[] = table[2].split(";");
                        System.out.println("(Server) Client connected from " + fromIP_Port + ", shared files: " + Arrays.toString(sharedFiles));

                        for (String sharedFile : sharedFiles) {
                            String name = sharedFile.split("_")[0];
                            String dest = sharedFile.split("_")[1];
                            dbMediator.createNode(from_inet.getHostAddress(), Integer.toString(clientPort), name, dest);
                        }
                        //printNodes();
                    } else if (Objects.equals(command, "unshare")) {  // pck(unshare, CLIENT_PORT)
                        System.out.println("(Server) Client unshared files " + fromIP_Port);
                        dbMediator.deleteNode(from_inet.getHostAddress(), Integer.toString(clientPort));
                        //printNodes();
                    } else if (Objects.equals(command, "file_req")) { // pck(fileReq, CLIENT_PORT, fileName1;fileName2;...)
                        String fileNames[] = table[2].split(";");
                        System.out.println("(Server) Client " + fromIP_Port + " requested " + Arrays.toString(fileNames));

                        BroadcasterMediator bm = new BroadcasterMediator(from_inet, clientPort);

                        for (String fileName : fileNames) {
                            String msgWithNodes = createNodesMessage(fileName, dbMediator.getFile(fileName)); // with DB
                            //String msgWithNodes = createNodesMessage(fileName,nodesHavingFile(fileName)); // without DB
                            bm.file_req_resp(fileName, msgWithNodes);
                        }

                    } else if (Objects.equals(command, "lookup")) { // pck(lookup, CLIENT_PORT, file1)
                                /*
                                    reply: node1:file1,file2;node2:file3,file4
                                 */
                        String query = table[2];
                        BroadcasterMediator bm = new BroadcasterMediator(from_inet, clientPort);
                        String msgWithNodes = createRegexMessage(dbMediator.searchFiles(query));
                        bm.lookupResp(query, msgWithNodes);
                    } else if (Objects.equals(command, "ping_resp")) {  // pck(ping,)
                        crashHandler.receivedPing(new Node(socket.getInetAddress(), clientPort));
                    }
                    crashHandler.updateNodes();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}