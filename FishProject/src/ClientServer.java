import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
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
                Socket socket = serverSocket.accept();
                FileOutputStream fos = new FileOutputStream("./test");
                byte[] buffer = new byte[1024];
                int count;
                InputStream in = socket.getInputStream();
                while((count=in.read(buffer)) >0){
                    fos.write(buffer, 0, count);
                }
                fos.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        };

        Thread serverThread = new Thread(serverTask);
        serverThread.start();
    }
}
