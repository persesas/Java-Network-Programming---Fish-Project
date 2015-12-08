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
         b = new Broadcaster(to_add, to_port, "unshare,");
        (new Thread(b)).start();
    }

    public void share(String [] fileNames){
        String msg = "shared_files,";
        for(String fileName: fileNames){
            msg = msg.concat(fileName+";");
        }

        b = new Broadcaster(to_add, to_port, msg);
        (new Thread(b)).start();
    }

    public void file_req(String fileName){
        String msg = "file_req,";
        msg = msg.concat(fileName+";");

        b = new Broadcaster(to_add, to_port, msg);
        (new Thread(b)).start();
    }

    public void file_req_resp(boolean hasFile, String fileName, String path, InetAddress owner_add, int owner_port){
        if(hasFile){
            String msg = "file_req_resp," + fileName + "_" + path +"," + owner_add + "," + owner_port;
            b = new Broadcaster(to_add, to_port, msg);
        } else{
            b = new Broadcaster(to_add, to_port, fileName + " not found");
        }
        (new Thread(b)).start();
    }

    public void download_req(String fileName, String path){
        String msg = "download_req," + fileName + "_" + path;
        b = new Broadcaster(to_add, to_port, msg);
        (new Thread(b)).start();
    }

    public void upload_file(String fileName, String path){//TODO
        FileBroadcaster fB = new FileBroadcaster(path, fileName, to_add, to_port);
        (new Thread(fB)).start();
    }

}