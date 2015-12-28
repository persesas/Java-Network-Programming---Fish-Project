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
     * Creates the BroadcasterMediator and will send the message to the specified ip and port
     * @param to_add - Msg to be sent to that ip
     * @param to_port - Msg to be sent to that port
     */
    public BroadcasterMediator(InetAddress to_add, int to_port){
        this.to_add = to_add.getHostAddress();
        this.to_port = to_port;
    }

    /**
     * Unregisters a Node from the Server
     * @param clientPort - port of the sender
     */
    public void disconnect(int clientPort) {
         b = new Broadcaster(to_add, to_port, "unshare," + clientPort);
        (new Thread(b)).start();
    }

    /**
     * Registers a Node to the Server
     * @param fileNames - List of all files the node is sharing
     * @param clientPort - port of the sender
     */
    public void share(HashMap<String, String> fileNames, int clientPort){
        String msg = "shared_files," + clientPort + ",";
        for(String fileName: fileNames.keySet()){
            msg = msg.concat(fileName+"_"+fileNames.get(fileName)+";");
        }

        b = new Broadcaster(to_add, to_port, msg);
        (new Thread(b)).start();
    }

    /**
     * Requests a file from the Server
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
     * Response of the Server to a File request
     * @param fileName - Name of the file
     * @param msg - All nodes containing the fileName
     */
    public void file_req_resp(String fileName, String msg){
        if(!msg.equals("")){
            String msgToSent = "file_req_resp," + fileName + "," + msg;
            b = new Broadcaster(to_add, to_port, msgToSent);
        } else{
            b = new Broadcaster(to_add, to_port, "file_req_resp," + fileName + ",not found");
        }
        (new Thread(b)).start();
    }

    /**
     * Download request made by a Node to another Node
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
     * Sends a file
     * @param fileName - name of the file to be sent
     * @param path - path of the file to be sent
     */
    public void uploadFile(String fileName, String path){//TODO
        FileBroadcaster fB = new FileBroadcaster(path, fileName, to_add, to_port);
        (new Thread(fB)).start();
    }

    /**
     * Sends a lookup request
     * @param fileName - name of the file
     * @param clientPort - port of the sending client
     */
    public void lookup(String fileName, int clientPort){
        b = new Broadcaster(to_add, to_port, "lookup,"+ clientPort + "," +fileName);
        (new Thread(b)).start();
    }

    /**
     * Sends the reply of a lookup request
     * @param fileName - name of the file
     * @param msgWithNodes - String containing all nodes containing the file
     */
    public void lookupResp(String fileName, String msgWithNodes){
        if(!msgWithNodes.equals("")) b = new Broadcaster(to_add, to_port, "lookup_resp," + fileName + ",found," + msgWithNodes);
        else b = new Broadcaster(to_add, to_port, "lookup_resp," + fileName + ",file not found");
        (new Thread(b)).start();
    }

}