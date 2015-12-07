import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
        * @author Perséas Charoud-Got
        * @author Fanti Samisti
 **/
public class ClientServer {

    public ClientServer(int port) {
        final ExecutorService clientProcessingPool = Executors.newFixedThreadPool(10);

        Runnable serverTask = () -> {
            try {
                DatagramSocket serverSocket = new DatagramSocket(port);
                byte[] recv_data = new byte[1024];
                while (true) {
                    DatagramPacket recv_packet = new DatagramPacket(recv_data, recv_data.length);
                    serverSocket.receive(recv_packet);
                    clientProcessingPool.submit(new ClientTask(recv_packet));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
        Thread serverThread = new Thread(serverTask);
        serverThread.start();
    }

    private class ClientTask implements Runnable {
        private final DatagramPacket recv_packet;

        private ClientTask(DatagramPacket packet) {
            this.recv_packet = packet;
        }

        @Override
        public void run() {
            String from = this.recv_packet.getAddress().getHostAddress() + ", " + this.recv_packet.getPort();
            String data = new String(this.recv_packet.getData(), 0, this.recv_packet.getLength());
            String [] table = data.split(",");
            String command = table[0];
            if (Objects.equals(command, "...")) {
                //TODO
            }
        }
    }
}
