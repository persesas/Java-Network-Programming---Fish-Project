import java.io.*;
import java.net.InetAddress;
import java.net.Socket;


/**
 * Represents the object responsible for sending a File
 * @author Perséas Charoud-Got
 * @author Fanti Samisti
 */
public class FileBroadcaster implements Runnable {
    private String path;
    private String fileName;
    private String toIp;
    private int toPort;

    /**
     * Creates a FileBroadcaster
     * @param path - path of the file
     * @param fileName - name of the file
     * @param toIp - ip to send the file
     * @param port - port to send the file
     */
    public FileBroadcaster(String path, String fileName, String toIp, int port){
        this.path = path;
        this.fileName = fileName;
        this.toIp = toIp;
        this.toPort = port;
    }

    @Override
    public void run() {
        try {
            File myFile = new File(path + "/" + fileName);
            BufferedInputStream in;
            try {
                in = new BufferedInputStream(new FileInputStream(myFile));
                Socket socket = new Socket(InetAddress.getByName(toIp), toPort);

                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                out.println("file_data,"+fileName);

                byte[] contents = new byte[1024];
                int bytesRead;
                String strFileContents;
                while( (bytesRead = in.read(contents)) != -1) {
                    strFileContents = new String(contents, 0, bytesRead);
                    out.println(strFileContents);
                }

                socket.close();
                System.out.println("sending " + fileName + " to : " + toIp +  " on port: " + toPort);
            }catch(FileNotFoundException e){
                System.err.println("Error: file not found!");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
