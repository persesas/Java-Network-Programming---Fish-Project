import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;
import java.util.Scanner;

/**
 * Represents the Client
 * @author Perséas Charoud-Got
 * @author Fanti Samisti
 */
public class Client {
    private static int client_port = 9001;

    public static void main(String[] args) {
        String shared_file_path = "./";
        String server_address = "127.0.0.1";
        String server_port = "8000";
        String[] commands = {"help", "share", "fileReq", "downloadReq", "upload_req", "lookup", "exit"};

        if(args.length == 1) {
            // Only client port is provided, listens on every IP, rest is default
            client_port = Integer.parseInt(args[0]);
        }
        else if(args.length > 4){
            throw new IllegalArgumentException("The arguments format should be:" +
                    " \"Client [shared_file_path] [server_address] [server_port] [client_port]\"");
        } else if(args.length == 4){
            shared_file_path = args[0];
            server_address = args[1];
            server_port = args[2];
            client_port = Integer.parseInt(args[3]);
        }

        new ClientServer(client_port, shared_file_path);    // start client server

        InetAddress serverAddress = null;
        int serverPort = -1;

        try {
            serverAddress = InetAddress.getByName(server_address);
            serverPort = Integer.parseInt(server_port);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        // Print the files that the client is about to share
        System.out.println("__ is sharing __");
        HashMap<String, String> files = getFilesFromDir(shared_file_path);
        for(String fileName: files.keySet()){
            System.out.println(fileName + " - " + files.get(fileName));
        }
        System.out.println("__ is sharing __");

        String file = "cl2-file2";
        String path = "./data2";
        int otherClientPort = 9002;

        InetAddress clientIP = null;
        try {
            clientIP = InetAddress.getByName("127.0.0.1");
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        System.out.println("Running on: "  + clientIP + ":" + client_port);

        String userInput;
        do{
            Scanner sc = new Scanner(System.in);
            userInput = sc.next();

            switch(userInput){
                case "share":       // client registers to the server indicating what files he's sharing
                    files = getFilesFromDir(shared_file_path);
                    share(files, serverAddress, serverPort);
                    break;
                case "file_req":    // request from a client to the server for downloading a given file
                    if(sc.hasNext())
                        fileReq(sc.next(), serverAddress, serverPort);
                    else
                        System.out.println("WARNING: fileReq needs file(s) as arguments");
                    break;
                case "download_req": // request from a client to another client to download a given file
                    downloadReq(file, path, serverAddress, serverPort);
                    break;
                case "upload_req":  // uploads a file to a given client IP + port (for debugging)
                    uploadReq("myFiles.txt", path, clientIP, otherClientPort);
                    break;
                case "help":    // command indicating to client what are the available commands
                    System.out.print("Available commands:");
                    System.out.println(Arrays.toString(commands));
                    break;
                case "exit":    // disconnects client from server
                    unshare(serverAddress, serverPort);
                    break;
                case "lookup":  // request from client to server asking if a particular file is available
                    if(sc.hasNext())
                        lookup(sc.next(), serverAddress, serverPort);
                    else
                        System.out.println("WARNING: lookup needs file as argument");
                    break;
                default:
                    System.out.println("unknown command, please retry");
            }

        }while(!Objects.equals(userInput, "exit"));
    }

    private static HashMap<String, String> getFilesFromDir(String path){
        HashMap<String, String> files = new HashMap<>();
        final File folder = new File(path);
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                files.putAll(getFilesFromDir(fileEntry.getPath()));
            } else {
                files.put(fileEntry.getName(), fileEntry.getParent());
            }
        }
        return files;
    }

    private static void share(HashMap<String, String> fileNames, InetAddress serverAdd, int serverPort){
        BroadcasterMediator bm = new BroadcasterMediator(serverAdd, serverPort);
        bm.share(fileNames, client_port);
    }

    private static void fileReq(String fileName, InetAddress serverAdd, int serverPort){
        BroadcasterMediator bm = new BroadcasterMediator(serverAdd, serverPort);
        bm.fileReq(fileName, client_port);
    }

    private static void unshare(InetAddress serverAdd, int serverPort){
        BroadcasterMediator bm = new BroadcasterMediator(serverAdd, serverPort);
        bm.disconnect(client_port);
    }

    private static void downloadReq(String fileName, String path, InetAddress serverAdd, int serverPort){
        BroadcasterMediator bm = new BroadcasterMediator(serverAdd, serverPort);
        bm.downloadReq(fileName,path,client_port);
    }

    private static void uploadReq(String fileName, String path, InetAddress addDestination, int portDestination){
        BroadcasterMediator bm = new BroadcasterMediator(addDestination, portDestination);
        bm.uploadFile(fileName,path);
    }

    private static void lookup(String fileName, InetAddress serverAdd, int serverPort){
        BroadcasterMediator bm = new BroadcasterMediator(serverAdd, serverPort);
        bm.lookup(fileName,client_port);
    }
}
