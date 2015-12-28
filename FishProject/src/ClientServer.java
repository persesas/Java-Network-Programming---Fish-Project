import java.io.*;
import java.net.*;

/**
 * Represents the Server side of the Client
 * @author Perséas Charoud-Got
 * @author Fanti Samisti
 **/
public class ClientServer {


    /**
     * Creates the Server Side of the Client listening to a given port
     * @param port - Port listening to
     * @param shared_file_path
     */
    public ClientServer(int port, String shared_file_path) {
        Runnable serverTask = () -> {

            try {
                ServerSocket serverSocket = new ServerSocket(port);

                while(true) {
                    Socket socket = serverSocket.accept();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String data;
                    if ((data = reader.readLine()) != null) {
                        String[] table = data.split(",");
                        String command = table[0];

                        switch (command) {
                            case "file_req_resp":  //pck(file_req_resp,fileName,ip1::port1::path1<->ip2::port2::path2<->...)
                                if (!table[2].equals("not found")) {
                                    String fileName = table[1];
                                    String [] nodes = table[2].split("<->");
                                    for(String s: nodes){
                                        System.out.println("Node having file - ip: " + s.split("::")[0]+ " port: " + s.split("::")[1] + " path: "+ s.split("::")[2]);
                                    }
                                    String path = nodes[0].split("::")[2];
                                    InetAddress ipOwner = InetAddress.getByName(nodes[0].split("::")[0].substring(1));
                                    int portOwner = Integer.parseInt(nodes[0].split("::")[1]);

                                    BroadcasterMediator bm = new BroadcasterMediator(ipOwner, portOwner);
                                    bm.downloadReq(fileName, path, port);
                                } else System.out.println("Warning: requested file not found");

                                break;

                            case "download_req":  //pck(downloadReq,clientPort, file_path)
                                int portDestination = Integer.parseInt(table[1]);
                                String fileName = table[2].split("_")[0];
                                String path = table[2].split("_")[1];
                                // Check if the path is aa legitimate one
                                if(!path.startsWith(shared_file_path)) {
                                    System.err.println("Illegal path: " + path);
                                    break;
                                }

                                BroadcasterMediator bm = new BroadcasterMediator(socket.getInetAddress(), portDestination);
                                bm.uploadFile(fileName, path);
                                break;

                            case "file_data":  //pck(file_data,fileName)
                                String name = table[1];
                                createFile(name, reader);
                                break;
                            case "lookup_resp": //pck(lookup_resp,fileName_path,found/notFound, ip1::port1::path1<->ip2::port2::path2)
                                String nameFile = table[1];
                                if(table[2].equals("file not found")) System.out.println(nameFile + " not found");
                                else {
                                    String [] nodes = table[2].split("<->");
                                    for(String s: nodes){
                                        System.out.println("Found at - ip: " + s.split("::")[0]+ " port: " + s.split("::")[1] + " path: "+ s.split("::")[2]);
                                    }
                                }
                                break;
                        }
                    }

                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        };
        Thread serverThread = new Thread(serverTask);
        serverThread.start();
    }


    //Creates a file from a given reader
    private void createFile(String name, BufferedReader reader) throws IOException {
        String line;
        PrintWriter writer = new PrintWriter(name, "UTF-8");
        while((line = reader.readLine()) != null){
            writer.write(line);
        }
        writer.close();
    }
}
