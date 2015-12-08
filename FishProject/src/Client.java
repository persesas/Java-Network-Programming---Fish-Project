import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Objects;
import java.util.Scanner;

/**
 * @author Perséas Charoud-Got
 * @author Fanti Samisti
 */
public class Client {
    private static int client_port = 9001;

    public static void main(String[] args) {
        String shared_file_path = "./";
        String server_address = "127.0.0.1";
        String server_port = "8000";
        String[] commands = new String[] {"help", "share", "file_req", "download_req", "upload_req", "exit"};

        if(args.length == 1) {
            // Only client port is provided, listens on every IP, rest is default
            client_port = Integer.parseInt(args[0]);
        }
        else if(args.length > 4){
            throw new IllegalArgumentException("The arguments format should be:" +
                    " \"Client [shared_file_path] [server_address] [server_port]\"");
        } else if(args.length == 4){
            shared_file_path = args[0];
            server_address = args[1];
            server_port = args[2];
            client_port = Integer.parseInt(args[3]);
        }

        ClientServer cl = new ClientServer(client_port);

        InetAddress serverAddress = null;
        int serverPort = -1;

        try {
            serverAddress = InetAddress.getByName(server_address);
            serverPort = Integer.parseInt(server_port);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        String fileNames [] = {"file1", "file2", "file3"};
        String file = "file1";
        String path = "./data";

        InetAddress clientIP = null;
        try {
            clientIP = InetAddress.getByName("127.0.0.1");
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        System.out.println("Running on: "  + clientIP+ ":" + client_port);

        String userInput;
        do{
            Scanner sc = new Scanner(System.in);
            userInput = sc.next();

            switch(userInput){
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
                    uploadReq("myFiles.txt",path, clientIP, client_port);
                    break;
                case "help":
                    System.out.print("Available commands:");
                    System.out.println(Arrays.toString(commands));
                    break;
                case "exit":
                    unshare(serverAddress, serverPort);
                    break;
                default:
                    System.err.println("unknown command, please retry");
            }

        }while(!Objects.equals(userInput, "exit"));
    }

    private static void share(String [] fileNames, InetAddress serverAdd, int serverPort){
        BroadcasterMediator bm = new BroadcasterMediator(serverAdd, serverPort);
        bm.share(fileNames, client_port);

    }

    private static void fileReq(String fileName, InetAddress serverAdd, int serverPort){
        BroadcasterMediator bm = new BroadcasterMediator(serverAdd, serverPort);
        bm.file_req(fileName, client_port);
    }

    private static void unshare(InetAddress serverAdd, int serverPort){
        BroadcasterMediator bm = new BroadcasterMediator(serverAdd, serverPort);
        bm.disconnect(client_port);
    }

    private static void downloadReq(String fileName, String path, InetAddress serverAdd, int serverPort){
        BroadcasterMediator bm = new BroadcasterMediator(serverAdd, serverPort);
        bm.download_req(fileName,path,client_port);
    }

    private static void uploadReq(String fileName, String path, InetAddress addDestination, int portDestination){
        BroadcasterMediator bm = new BroadcasterMediator(addDestination, portDestination);
        bm.upload_file(fileName,path,client_port);
    }
}
