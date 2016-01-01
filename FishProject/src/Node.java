import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
     * Removes the corresponding file name
     * @param fileName - file name to be removed
     */
    public void removeFile(String fileName){
        if(!files.containsKey(fileName)) System.err.println("File doesn't exists");
        files.remove(fileName);
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
     * Returns if a file is shared or not.
     * @param filename - file name to be searched for
     */
    public boolean hasFile(String filename){
        //System.out.println(filename.matches("\\d+"));
        return files.containsKey(filename);
    }

    /**
     * Returns a list of file_dir that match the pattern
     * @param pattern - regex to search with
     */
    public ArrayList<String> search(String pattern) {
        ArrayList<String> result = new ArrayList<>();
        Pattern p = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
        for(String file: files.keySet()) {
            Matcher m = p.matcher(file);
            if(m.find()) result.add(file + "_" + files.get(file));
        }
        return result;
    }

    /**
     * Overrides toString method of Object
     * @return ip: ip_add port: port size shared: files.size()
     */
    @Override
    public String toString(){
        return "ip: " + ip_add.getHostAddress() + " port:" + port + " size shared:" + files.size();
    }

    public String filesToString() {
        StringBuffer sb = new StringBuffer();
        for(String filename: files.keySet()) {
            sb.append(filename + "_" + files.get(filename));
            sb.append(";");
        }

        return sb.toString();
    }
}
