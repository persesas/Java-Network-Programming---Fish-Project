import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Objects;
import java.util.Scanner;

/**
 * @author Perséas Charoud-Got
 * @author Fanti Samisti
 */
public class Client {
    public static final int CLIENT_SERVER_PORT = 9001;

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
        ClientServer cl = new ClientServer(CLIENT_SERVER_PORT);

        InetAddress serverAddress=null;
        int serverPort=-1;

        try {
            serverAddress = InetAddress.getByName(server_address);
            serverPort = Integer.parseInt(server_port);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        String fileNames [] = {"hello_./", "star wars_./", "jumbo_./"};
        String file = "hello";
        String path = "./data";
        InetAddress addDestination = null;
        int dest_port = CLIENT_SERVER_PORT+1;
        try {
            addDestination = InetAddress.getByName("127.0.0.1");
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        System.out.println("HOST : "  + CLIENT_SERVER_PORT);
        System.out.println("DESTINATION : "  + addDestination+ " " + dest_port);

        String userInput;
        do{
            Scanner sc = new Scanner(System.in);
            userInput = sc.next();

            switch(userInput){
                case "exit":
                    unshare(serverAddress, serverPort);
                    break;
                case "share":
                    share(fileNames, serverAddress, serverPort);
                    break;
                case "file_req":
                    fileReq(file, serverAddress, serverPort);
                    break;
                case "download_req":
                    downloadReq(file, path, serverAddress, serverPort);
                    break;
                case "upload_req":  //Only for debugging
                    uploadReq("myFiles.txt",path, addDestination, dest_port);
                    break;
                default:
                    System.err.println("unknown command, retry again:");
            }

        }while(!Objects.equals(userInput, "exit"));
    }


    private static void share(String [] fileNames, InetAddress serverAdd, int serverPort){
        BroadcasterMediator bm = new BroadcasterMediator(serverAdd, serverPort);
        bm.share(fileNames);

    }

    private static void fileReq(String fileName, InetAddress serverAdd, int serverPort){
        BroadcasterMediator bm = new BroadcasterMediator(serverAdd, serverPort);
        bm.file_req(fileName);
    }

    private static void unshare(InetAddress serverAdd, int serverPort){
        BroadcasterMediator bm = new BroadcasterMediator(serverAdd, serverPort);
        bm.disconnect();
    }

    private static void downloadReq(String fileName, String path, InetAddress serverAdd, int serverPort){
        BroadcasterMediator bm = new BroadcasterMediator(serverAdd, serverPort);
        bm.download_req(fileName,path);
    }

    private static void uploadReq(String fileName, String path, InetAddress addDestination, int portDestination){
        BroadcasterMediator bm = new BroadcasterMediator(addDestination, portDestination);
        bm.upload_file(fileName,path);
    }
}