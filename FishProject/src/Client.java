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
    private static int client_port = 8000;
    private static String ip_multicast = "239.0.0.1";
    private static int port_multicast = 9001;

    public static void main(String[] args) {
        String shared_file_path = "./";
        String[] commands = {"help", "share", "fileReq", "downloadReq", "upload_req", "lookup", "exit"};

        if(args.length == 1) {
            // Only client port is provided, listens on every IP, rest is default
            client_port = Integer.parseInt(args[0]);
        }
        else if(args.length > 4){
            throw new IllegalArgumentException("The arguments format should be:" +
                    " \"Client [shared_file_path][ip_multicast] [port_multicast] [client_port] \"");
        } else if(args.length == 4){
            shared_file_path = args[0];
            client_port = Integer.parseInt(args[3]);
            ip_multicast = args[1];
            port_multicast = Integer.parseInt(args[2]);
        }

        new ClientServer(client_port, getFilesFromDir(shared_file_path));    // start client server
        new MulticastServer(ip_multicast, port_multicast, client_port, getFilesFromDir(shared_file_path));  // start multicast server

        // Print the files that the client is about to share
        System.out.println("___ Is sharing ___");
        HashMap<String, String> files = getFilesFromDir(shared_file_path);
        for(String fileName: files.keySet()){
            System.out.println(fileName + " - " + files.get(fileName));
        }
        System.out.println("___ Is sharing ___");

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
                case "download_req": // request from a client to another client to download a given file
                    String ip = null;
                    String port;
                    try {
                        if(sc.hasNext()) {
                            ip = sc.next();
                            if (sc.hasNext()){
                                port = sc.next();
                                if(sc.hasNext()){
                                    String filename = sc.next();
                                    if(sc.hasNext()){
                                        String path = sc.next();
                                        downloadReq(InetAddress.getByName(ip), Integer.parseInt(port), filename, path);
                                        break;
                                    }
                                }
                            }
                        }
                    } catch (UnknownHostException e) {
                        System.out.println("The address (" + ip + ") introduced doesn't exist");
                    } catch (NumberFormatException e){
                        System.out.println("The port must be an number");
                    }
                    System.out.println("WARNING: download_req uses the following format: download_req ip port filename path");
                    break;
                case "help":    // command indicating to client what are the available commands
                    System.out.print("Available commands:");
                    System.out.println(Arrays.toString(commands));
                    break;
                case "lookup":  // request from client to server asking if a particular file is available   //TODO
                    String ipAddNode = null;
                    String portNode;
                    try {
                        if (sc.hasNext()) {
                            ipAddNode = sc.next();
                            if (sc.hasNext()) {
                                portNode = sc.next();
                                if (sc.hasNext()) {
                                    String filename = sc.next();
                                    lookup(InetAddress.getByName(ipAddNode), Integer.parseInt(portNode), filename);
                                    break;
                                }
                            }
                        }
                    } catch (UnknownHostException e) {
                        System.out.println("The address (" + ipAddNode + ") introduced doesn't exist");
                    } catch (NumberFormatException e) {
                        System.out.println("The port must be an number");
                    }
                    System.out.println("WARNING: download_req uses the following format: lookup ip port filename");
                    break;
                case "discovery":
                    if(sc.hasNext())
                        try {
                            discovery(InetAddress.getByName(ip_multicast), port_multicast, sc.next());
                        } catch (UnknownHostException e) {
                            e.printStackTrace();
                        }
                    else
                        System.out.println("WARNING: lookup needs file as argument");
                    break;
                default:
                    System.out.println("unknown command, please retry");
            }

        }while(!Objects.equals(userInput, "exit"));
    }

    //Gets all files from a given directory
    private static HashMap<String, String> getFilesFromDir(String path){
        HashMap<String, String> files = new HashMap<>();
        File folder = new File(path);
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                files.putAll(getFilesFromDir(fileEntry.getPath()));
            } else {
                files.put(fileEntry.getName(), fileEntry.getParent());
            }
        }
        return files;
    }

    private static void downloadReq(InetAddress toAdd, int port, String fileName, String path){
        BroadcasterMediator bm = new BroadcasterMediator(toAdd, port);
        bm.downloadReq(fileName,path,client_port);
    }

    private static void lookup(InetAddress toAdd, int port, String fileName){
        BroadcasterMediator bm = new BroadcasterMediator(toAdd, port);
        bm.lookup(fileName,client_port);
    }

    private static void discovery(InetAddress toAdd, int port, String fileName){
        BroadcasterMediator bm = new BroadcasterMediator(toAdd, port);
        bm.discovery(fileName, client_port);
    }
}
