import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Handles crashes from clients
 * @author Perséas Charoud-Got
 * @author Fanti Samisti
 */
public class CrashHandler {
    private final int TIME_BEFORE_PING = 10000;
    private final int TIME_BEFORE_TIMEOUT = 1000;
    private int serverPort;
    private ArrayList<Node> nodes;
    private ArrayList<Node> confirmedNodes;
    private DBMediator dbMediator;

    public CrashHandler(ArrayList<Node> nodes, int serverPort){
        this.nodes = nodes;
        this.serverPort = serverPort;
        start();
        dbMediator = DBMediator.getInstance();
    }


    private void start(){
        TimerTask timerTaskPing = new TimerTask() {
            @Override
            public void run() {
                confirmedNodes = new ArrayList<>();
                System.out.println("(CrashHandler) ping all nodes (" + nodes.size()+ ")");

                for(Node n: nodes){
                    BroadcasterMediator broadcasterMediator= new BroadcasterMediator(n.getIp_add(), n.getPort());
                    broadcasterMediator.ping(serverPort);
                }


                TimerTask timerTaskTimeout = new TimerTask() {
                    @Override
                    public void run() {
                        nodes = confirmedNodes;
                    }
                };
                Timer timer = new Timer();
                timer.schedule(timerTaskTimeout, TIME_BEFORE_TIMEOUT);
            }


        };

        Timer t = new Timer();
        t.scheduleAtFixedRate(timerTaskPing, TIME_BEFORE_PING, TIME_BEFORE_PING);
    }

    public void receivedPing(Node node){
        confirmedNodes.add(node);
    }

    public void updateNodes(){ this.nodes = dbMediator.getAllNodes(); }
    public void setNodes(ArrayList<Node> nodes){
        this.nodes = nodes;
    }

}
