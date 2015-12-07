import java.net.InetAddress;
import java.util.HashMap;

/**
 * @author Perséas Charoud-Got
 * @author Fanti Samisti
 */

public class Node {
    private InetAddress ip_add;
    private int port;
    private HashMap<String, String> files;  //<filename, path>

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
        if(files.containsKey(fileName)) throw new IllegalArgumentException("File already exists");
        files.put(fileName, path);
    }

    /**
     * Removes the corresponding file name
     * @param fileName - file name to be removed
     */
    public void removeFile(String fileName){
        if(!files.containsKey(fileName)) throw new IllegalArgumentException("File doesn't exists");
        files.remove(fileName);
    }

    /**
     * Returns if a file is shared or not.
     * @param fileName - file name to be searched for
     */
    public boolean hasFile(String fileName){
        return files.containsKey(fileName);
    }
}
