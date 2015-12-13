import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Perséas Charoud-Got
 * @author Fanti Samisti
 **/
public class ClientServer {

    public ClientServer(int port) {

        Runnable serverTask = () -> {

            try {
                ServerSocket serverSocket = new ServerSocket(port);
                while(true) {

                    Socket socket = serverSocket.accept();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String data;
                    if ((data = reader.readLine()) != null) {
                        System.out.println("received: " + data);
                        String[] table = data.split(",");
                        String command = table[0];
                        if (command.equals("file_req_resp")) { //pck(file_req_resp,file1_dest,ipOwner,portOwner)
                                //TODO
                        } else if(command.equals("file_data")){ //pck(file_data,fileName)
                            String name = table[1];
                            createFile(name, reader);
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

    private void createFile(String name, BufferedReader reader) throws IOException {
        String line;
        PrintWriter writer = new PrintWriter(name, "UTF-8");
        while((line = reader.readLine()) != null){
            writer.write(line);
        }
        writer.close();
    }
}
