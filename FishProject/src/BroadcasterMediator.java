import java.net.InetAddress;
import java.util.HashMap;

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
     * Constructor for UDP (known ip and port)
     */
    public BroadcasterMediator(){}

    /**
     * Creates the BroadcasterMediator and will send the message to the specified ip and port, for TCP communication
     * @param to_add - Msg to be sent to that ip
     * @param to_port - Msg to be sent to that port
     */
    public BroadcasterMediator(InetAddress to_add, int to_port){
        this.to_add = to_add.getHostAddress();
        this.to_port = to_port;
    }

    /**
     * Unregisters a Node from all clients listening in the multicast group, via UDP
     */
    public void disconnect(HashMap<String, String> fileNames) {
        String msg = "shared_files,";
        for(String fileName: fileNames.keySet()){
            msg = msg.concat(fileName+"_"+fileNames.get(fileName)+";");
        }
        UDPBroadcaster udpB = new UDPBroadcaster("unshare," + msg);
        (new Thread(udpB)).start();
    }

    /**
     * Registers a Node to all other clients listening in the multicast group, via UDP
     * @param fileNames - List of all files the node is sharing
     */
    public void share(HashMap<String, String> fileNames){
        String msg = "shared_files,";
        for(String fileName: fileNames.keySet()){
            msg = msg.concat(fileName+"_"+fileNames.get(fileName)+";");
        }

        UDPBroadcaster udpB = new UDPBroadcaster(msg);
        (new Thread(udpB)).start();
    }

    /**
     * Requests a file from another client, via TCP
     * @param fileName - filename requested
     * @param clientPort - port of the sender
     */
    public void fileReq(String fileName, int clientPort){
        String msg = "file_req," + clientPort + ",";
        msg = msg.concat(fileName+";");

        b = new Broadcaster(to_add, to_port, msg);
        (new Thread(b)).start();
    }

    /**
     * Download request made by a Node to another Node, via TCP
     * @param fileName - Name of the file requested for download
     * @param path - Location where the file is stored on HD
     * @param clientPort - port of the sender
     */
    public void downloadReq(String fileName, String path, int clientPort){
        String msg = "download_req," + clientPort + "," + fileName + "_" + path;
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
     * @param fileName - name of the file
     * @param msgWithNodes - String containing all nodes containing the file
     */
    public void lookupResp(String fileName, String msgWithNodes){
        if(!msgWithNodes.equals("")) b = new Broadcaster(to_add, to_port, "lookup_resp," + fileName + ",found," + msgWithNodes);
        else b = new Broadcaster(to_add, to_port, "lookup_resp," + fileName + ",file not found");
        (new Thread(b)).start();
    }


    /**
     * Ping from client to another client, via TCP
     * @param clientPort - port of the server
     */
    public void ping(int clientPort){
        b = new Broadcaster(to_add, to_port, "ping," + clientPort);
        (new Thread(b)).start();
    }

    /**
     * Ping response from client to another client, via TCP
     * @param clientPort - port of client
     */
    public void pingResp(int clientPort){
        b = new Broadcaster(to_add, to_port, "ping_resp," + clientPort);
        (new Thread(b)).start();
    }

    /**
     * Using UDBPBroadcaster, searching file via UDP
     * @param fileName - fileName requested
     */
    public void discovery(String fileName, int clientPort){
        UDPBroadcaster udpB = new UDPBroadcaster("discovery," + clientPort + "," +fileName);
        (new Thread(udpB)).start();
    }

    public void discoveryResp(String fileName, int clientPort){
        UDPBroadcaster udpB = new UDPBroadcaster("discovery," + clientPort + "," +fileName);
        (new Thread(udpB)).start();
    }
}