import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author Perséas Charoud-Got
 * @author Fanti Samisti
 */
public class Client {

    public static void main(String[] args) {

        String shared_file_path = "./";
        String server_address = "127.0.0.1";
        String server_port = "8000";

        if(args.length>3){
          throw new IllegalArgumentException("The arguments format should be:" +
                  " \"Client [shared_file_path] [server_address] [server_port]\"");
        } else if(args.length==3){
            shared_file_path = args[0];
            server_address = args[1];
            server_port = args[2];
        }

        String fileNames [] = {"hello", "star wars", "jumbo"};
        try {
            share(fileNames, InetAddress.getByName(server_address), Integer.parseInt(server_port));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }


    private static void share(String [] fileNames, InetAddress serverAdd, int serverPort){
        BroadcasterMediator bm = new BroadcasterMediator(serverAdd, serverPort);
        bm.file_req(fileNames);

    }

    public static void unshare(InetAddress serverAdd, int serverPort){
        BroadcasterMediator bm = new BroadcasterMediator(serverAdd, serverPort);
        bm.disconnect();
    }
}
