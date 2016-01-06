import java.net.InetAddress;

/**
 * Represents a Broadcaster Mediator which facilitate the sending of the messages
 * @author Perséas Charoud-Got
 * @author Fanti Samisti
 */
public class BroadcasterMediator {
    private String to_add;
    private int to_port;
    private Broadcaster b;

    /**
     * Creates the BroadcasterMediator and will send the message to the specified ip and port, for TCP/UDP communication
     * @param to_add - Msg to be sent to that ip
     * @param to_port - Msg to be sent to that port
     */
    public BroadcasterMediator(InetAddress to_add, int to_port){
        this.to_add = to_add.getHostAddress();
        this.to_port = to_port;
    }

    /**
     * Download request made by a Node to another Node, via TCP
     * @param fileName - Name of the file requested for download
     * @param path - Location where the file is stored on HD
     * @param clientPort - port of the sender
     */
    public void downloadReq(String fileName, String path, int clientPort){
        String msg = "download_req," + clientPort + "," + fileName + "____" + path;
        b = new Broadcaster(to_add, to_port, msg);
        (new Thread(b)).start();
    }

    /**
     * Sends a file from a client to another client, via TCP
     * @param fileName - name of the file to be sent
     * @param path - path of the file to be sent
     */
    public void uploadFile(String fileName, String path){
        FileBroadcaster fB = new FileBroadcaster(path, fileName, to_add, to_port);
        (new Thread(fB)).start();
    }

    /**
     * Sends a lookup request, via TCP
     * @param fileName - name of the file
     * @param clientPort - port of the sending client
     */
    public void lookup(String fileName, int clientPort){
        b = new Broadcaster(to_add, to_port, "lookup,"+ clientPort + "," +fileName);
        (new Thread(b)).start();
    }

    /**
     * Sends the reply of a lookup request, via TCP
     * @param clientPort - port of the sending client
     * @param fileName - name of the file
     * @param path - Path of the file
     */
    public void lookupResp(int clientPort, String fileName, String path){
        if(!path.equals("")) b = new Broadcaster(to_add, to_port, "lookup_resp,"+ clientPort + "," + fileName + ",found," + path);
        else b = new Broadcaster(to_add, to_port, "lookup_resp," + clientPort +"," + fileName + ",file not found");
        (new Thread(b)).start();
    }

    /**
     * Broadcasts a request for a file
     * @param fileName - name of the file requested
     * @param clientPort - port of the sending client (TCP)
     */
    public void discovery(String fileName, int clientPort){
        UDPBroadcaster udpB = new UDPBroadcaster(to_add, to_port, "discovery," + clientPort + "," +fileName);
        (new Thread(udpB)).start();
    }

    /**
     * Response from a discovery request
     * @param fileName - name of the file requested
     * @param path - path of the file on node's HD
     * @param clientPort - port of the sending client (TCP)
     */
    public void discoveryResp(String fileName, String path, int clientPort){
        b = new Broadcaster(to_add, to_port, "discovery_resp," + clientPort + "," +fileName + "," + path);
        (new Thread(b)).start();
    }
}