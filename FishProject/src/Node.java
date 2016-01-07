import java.net.InetAddress;
import java.util.HashMap;

/**
 * Represent a Node, ie. someone sharing or downloading a file
 * @author Perséas Charoud-Got
 * @author Fanti Samisti
 */

public class Node {
    private InetAddress ip_add;
    private int port;
    private HashMap<String, String> files;  //<filename, path>

    /**
     * Creates a Node
     * @param ip_add - IP of the node
     * @param port - Port of the node
     */
    public Node(InetAddress ip_add, int port){
        this.ip_add = ip_add; //TODO profound copy
        this.port = port;
        files = new HashMap<>();
    }

    /**
     *
     * @return ip_add of the Node
     */
    public InetAddress getIp_add() {
        return ip_add;
    }

    /**
     *
     * @return port of the Node
     */
    public int getPort() {
        return port;
    }


    /**
     * Add a new file to the sharing files
     * @param fileName - name of the file to be added
     * @param path - path where the file is located on HD
     */
    public void addFile(String fileName, String path){
        if(files.containsKey(fileName)) System.err.println("File already exists, ignoring request");
        files.put(fileName, path);
    }


    /**
     * Returns the path from a given file
     * @param fileName - file from which we're looking the path
     * @return path
     */
    public String getPath(String fileName){
        return files.get(fileName);
    }

    /**
     * Overrides toString method of Object
     * @return ip: ip_add port: port size shared: files.size()
     */
    @Override
    public String toString(){
        return "ip: " + ip_add.getHostAddress() + " port:" + port + " size shared:" + files.size();
    }

    @Override
    public boolean equals(Object other){
        if (other == null) return false;
        if (other == this) return true;
        if (!(other instanceof Node))return false;
        Node otherNode = (Node)other;
        return ip_add.equals(otherNode.getIp_add()) && port==otherNode.getPort();
    }
}
