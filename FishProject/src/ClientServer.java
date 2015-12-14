import java.io.*;
import java.net.*;

/**
 * Represents the Server side of the Client
 * @author Perséas Charoud-Got
 * @author Fanti Samisti
 **/
public class ClientServer {


    /**
     * Creates the Server Side of the Clietn listening to a given port
     * @param port - Port listening to
     */
    public ClientServer(int port) {
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
                            case "file_req_resp":  //pck(file_req_resp,file1_dest,ipOwner,portOwner)
                                if (!table[2].equals("not found")) {
                                    String fileName = table[1].split("_")[0];
                                    String path = table[1].split("_")[1];
                                    InetAddress ipOwner = InetAddress.getByName(table[2].substring(1));
                                    int portOwner = Integer.parseInt(table[3]);

                                    BroadcasterMediator bm = new BroadcasterMediator(ipOwner, portOwner);
                                    bm.downloadReq(fileName, path, port);
                                } else System.out.println("Warning: requested file not found");

                                break;

                            case "download_req":  //pck(downloadReq,clientPort, file_path)
                                int portDestination = Integer.parseInt(table[1]);
                                String fileName = table[2].split("_")[0];
                                String path = table[2].split("_")[1];

                                BroadcasterMediator bm = new BroadcasterMediator(socket.getInetAddress(), portDestination);
                                bm.uploadFile(fileName, path);
                                break;

                            case "file_data":  //pck(file_data,fileName)
                                String name = table[1];
                                createFile(name, reader);
                                break;
                            case "lookup_resp": //pck(lookup_resp,fileName_path,found/notFound, ownerIp, ownerPort)
                                String nameFile = table[1].split("_")[0];
                                if(table[2].equals("file not found")) System.out.println(nameFile + " not found");
                                else {
                                    String ownerIP = table[3];
                                    String ownerPort = table[4];
                                    System.out.println(nameFile + " found at " + ownerIP + " on port " + ownerPort);
                                }

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
