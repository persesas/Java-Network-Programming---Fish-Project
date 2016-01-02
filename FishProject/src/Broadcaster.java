import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.*;

/**
 * @author Perséas Charoud-Got
 * @author Fanti Samisti
 */
public class Broadcaster implements Runnable {
    // This class sends an update to the other players-clients

    private String toIp;
    private int toPort;
    private String msg;

    /**
     * Constructs the Broadcaster sending a msg to a given toIp:toPort
     * @param toIp - toIp used
     * @param toPort - Port used
     * @param msg - Message to send to all nodes
     */
    public Broadcaster(String toIp, int toPort, String msg) {
        this.toIp = toIp;
        this.toPort = toPort;
        this.msg = msg;
    }

    @Override
    public void run() {
        try {
            Socket socket = new Socket(toIp, toPort);
            PrintWriter out =new PrintWriter(socket.getOutputStream(), true);

            InetAddress addr = InetAddress.getByName(toIp);
            System.out.println("(Broadcaster) Msg sent at " + addr + " " + toPort + " : " + msg);
            out.println(msg);
            socket.close();

        } catch (UnknownHostException e) {
            System.out.println("(Broadcaster) Unknown host");
        } catch (ConnectException e){
            System.err.println("(Broadcaster) Connection refused, nobody there");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}