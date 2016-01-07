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
    private ArrayList<Node> nodes = new ArrayList<>();
    private ArrayList<Node> confirmedNodes;
    private DBMediator dbMediator = new DBMediator();

    /**
     * Constructor of CrashHandler class
     * @param serverPort - server port
     */
    public CrashHandler(int serverPort){
        this.serverPort = serverPort;
        start();
        dbMediator = DBMediator.getInstance();
    }


    private void start(){
        TimerTask timerTaskPing = new TimerTask() {
            @Override
            public void run() {
                nodes = dbMediator.getAllNodes();
                confirmedNodes = new ArrayList<>();
                System.out.println("(CrashHandler) ping all nodes (" + nodes.size()+ ")");
                System.out.flush();

                for(Node n: nodes){
                    BroadcasterMediator broadcasterMediator= new BroadcasterMediator(n.getIp_add(), n.getPort());
                    broadcasterMediator.ping(serverPort);
                }


                TimerTask timerTaskTimeout = new TimerTask() {
                    @Override
                    public void run() {
                        ArrayList<Node> toBeRem = removeAll(nodes,confirmedNodes);
                        for (Node n : toBeRem) {
                            dbMediator.deleteNode(n.getIp_add().getHostAddress(), Integer.toString(n.getPort()));
                        }
                    }

                    private ArrayList<Node> removeAll(ArrayList<Node> nodes, ArrayList<Node> confirmed) {
                        ArrayList<Node> ret = new ArrayList<>();
                        for(Node n: nodes){
                            if (!confirmed.contains(n)){
                                ret.add(n);
                            }
                        }
                        return ret;
                    }

                };
                Timer timer = new Timer();
                timer.schedule(timerTaskTimeout, TIME_BEFORE_TIMEOUT);
            }


        };

        Timer t = new Timer();
        t.scheduleAtFixedRate(timerTaskPing, TIME_BEFORE_PING, TIME_BEFORE_PING);
    }

    /**
     * Announce a received ping
     * @param node - Node received ping from
     */
    public void receivedPing(Node node){
        confirmedNodes.add(node);
    }

    /**
     * Updates the nodes
     */
    public void updateNodes(){ this.nodes = dbMediator.getAllNodes(); }
}
