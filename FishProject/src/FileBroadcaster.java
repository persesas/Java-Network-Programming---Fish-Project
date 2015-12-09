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
            System.out.println(toIp +  " " + toPort);
            Socket socket = new Socket(InetAddress.getByName(toIp), toPort);

            int count;
            byte[] buffer = new byte[1024];

            OutputStream out = socket.getOutputStream();
            BufferedInputStream in = new BufferedInputStream(new FileInputStream(myFile));
            while ((count = in.read(buffer)) > 0) {
                out.write(buffer, 0, count);
                out.flush();
            }
            socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
