import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

public class FileBroadcaster implements Runnable {
    private String path;
    private String fileName;
    private String toIp;
    private int toPort;

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
            System.out.println("sending " + fileName + " to : " + toIp +  " on port: " + toPort);
            Socket socket = new Socket(InetAddress.getByName(toIp), toPort);

            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println("file_data,"+fileName);
            BufferedInputStream in = new BufferedInputStream(new FileInputStream(myFile));

            byte[] contents = new byte[1024];
            int bytesRead=0;
            String strFileContents;
            while( (bytesRead = in.read(contents)) != -1) {
                strFileContents = new String(contents, 0, bytesRead);
                out.println(strFileContents);
            }

            socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
