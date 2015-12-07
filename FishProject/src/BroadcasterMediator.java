import java.net.InetAddress;

/**
 * @author Perséas Charoud-Got
 * @author Fanti Samisti
 */
public class BroadcasterMediator {
    private String to_add;
    private int to_port;
    private Broadcaster b;

    public BroadcasterMediator(InetAddress to_add, int to_port){
        this.to_add = to_add.getHostAddress();
        this.to_port = to_port;
    }

    public void disconnect() {
         b = new Broadcaster(to_add, to_port, "unshare");
    }

    public void file_req(String [] fileNames){
        String msg = "file_req,";
        for(String fileName: fileNames){
            msg = msg.concat(fileName+";");
        }

        b = new Broadcaster(to_add, to_port, msg);
        (new Thread(b)).start();
    }

    public void file_req_resp(boolean hasFile, String fileName, InetAddress owner_add, int owner_port){
        if(hasFile){
            String msg = fileName + "," + owner_add + "," + owner_port;
            b = new Broadcaster(to_add, to_port, msg);
        } else{
            b = new Broadcaster(to_add, to_port, fileName + " not found");
        }
        (new Thread(b)).start();
    }

}