import java.io.*;
import java.net.*;
import java.util.HashMap;

/**
 * Represents the Server side of the Client
 * @author Perséas Charoud-Got
 * @author Fanti Samisti
 **/
public class ClientServer {

    /**
     * Creates the Server Side of the Client listening to a given port
     * @param port - Port listening to
     * @param sharedFiles - All shared files from the client
     */
    public ClientServer(int port, HashMap<String, String> sharedFiles) {
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
                            case "download_req":  //pck(downloadReq,clientPort, file_path)
                                int portDestination = Integer.parseInt(table[1]);
                                String fileName = table[2].split("____")[0];
                                String path = table[2].split("____")[1];

                                BroadcasterMediator bm = new BroadcasterMediator(socket.getInetAddress(), portDestination);
                                bm.uploadFile(fileName, path);
                                break;
                            case "file_data":  //pck(file_data,fileName)
                                String name = table[1];
                                createFile(name, reader);
                                break;
                            case "lookup": //pck(lookup,clientPort,filename")
                                String fileNameReq = table[2];
                                BroadcasterMediator bM = new BroadcasterMediator(socket.getInetAddress(), Integer.parseInt(table[1]));
                                if(sharedFiles.containsKey(fileNameReq)){
                                    bM.lookupResp(port, fileNameReq, sharedFiles.get(fileNameReq));
                                } else{
                                    bM.lookupResp(port, fileNameReq, "");
                                }

                            case "lookup_resp": //pck(lookup_resp,clientPort,fileName,found,path)
                                String nameFile = table[2];
                                if(table[3].equals("file not found")) System.out.println("(ClientServer)" + socket.getInetAddress() + ":" + table[1] + " doesn't have " + nameFile);
                                else {
                                    System.out.println("(ClientServer) Found " + table[2] +  " at " + socket.getInetAddress() + ":" + table[1]
                                            + " path: " + table[4]);

                                }
                                break;
                            case "discovery_resp":  //pck(discovery_resp,port,fileName)
                                System.out.println("(ClientServer) ip: " + socket.getInetAddress() + ":" + table[1] + " got file " + table[2] + " @ " + table[3]);
                        }
                    }

                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        };
        (new Thread(serverTask)).start();
    }


    //Creates a file from a given BufferedReader
    private void createFile(String name, BufferedReader reader) throws IOException {
        String line;
        PrintWriter writer = new PrintWriter(name, "UTF-8");
        while((line = reader.readLine()) != null){
            writer.write(line);
        }
        writer.close();
    }
}
