import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Represents the Database Mediator
 * @author Perséas Charoud-Got
 * @author Fanti Samisti
 */
public class DBMediator {
    private static DBMediator instance;
    private Connection connection;

    private PreparedStatement newNodeStatement;
    private PreparedStatement allFilesStatement;
    private PreparedStatement deleteNodeStatement;
    private PreparedStatement getFileStatement;
    private PreparedStatement getAllNodesStatement;
    private PreparedStatement searchFilesStatement;

    /**
     * Constructor of the Database Mediator
     */
    public DBMediator(){
        try {
            Class.forName("org.mariadb.jdbc.Driver");
            connection = DriverManager.getConnection(
                    "jdbc:mariadb://localhost:3306/p2p", "root", "root");
            createTables();
            prepareStatements();
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    // creates tables for the database
    private void createTables() {
        try {
            Statement statement = connection.createStatement();

            statement.executeUpdate("CREATE TABLE IF NOT EXISTS clients "
                    + "(ip VARCHAR(32) NOT NULL,"
                    + " port VARCHAR(32) NOT NULL,"
                    + " fileName VARCHAR(64) NOT NULL,"
                    + " path VARCHAR(128) NOT NULL,"
                    + " CONSTRAINT pk_ClientsID PRIMARY KEY (ip, port, fileName) );");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Prepares statements
    private void prepareStatements() throws SQLException {
        newNodeStatement = connection.prepareStatement("INSERT INTO clients VALUES(?, ?, ?, ?)");  // adds a new node
        deleteNodeStatement = connection.prepareStatement("DELETE FROM clients WHERE ip=? AND port=?");    // deletes a node
        allFilesStatement = connection.prepareStatement("SELECT DISTINCT fileName FROM clients");   // returns all files
        getFileStatement = connection.prepareStatement("SELECT * FROM clients where fileName=?");   // returns all files with a given fileName
        getAllNodesStatement = connection.prepareStatement("SELECT DISTINCT ip, port FROM clients");
        searchFilesStatement = connection.prepareStatement("SELECT * FROM clients WHERE fileName REGEXP \"?\"");
    }

    /**
     *
     * @return an instance of DBMediatore
     */
    public static DBMediator getInstance(){
        if(instance == null){
            instance = new DBMediator();
        }
        return instance;
    }

    /**
     * Creates a Node
     * @param ip - ip of the Node
     * @param port - port of the Node
     * @param fileName - fileName that is shared from the Node
     * @param path - path of the file that's being shared
     * @return true if insertion was successful false if wasn't
     */
    public boolean createNode(String ip, String port, String fileName, String path){
        try {
            newNodeStatement.setString(1, ip);
            newNodeStatement.setString(2, port);
            newNodeStatement.setString(3, fileName);
            newNodeStatement.setString(4, path);

            int rows = newNodeStatement.executeUpdate();
            if (rows == 1) {
                System.out.println("(DBMediator) Client created: " + ip + " " + port + " filename " + fileName + " path " + path);
                return true;
            } else {
                throw new RejectedException("(DBMediator) Cannot create client :" + fileName);
            }
        } catch (SQLIntegrityConstraintViolationException e){
            System.err.println("(DBMediator) Node and file is already in database, ignoring request");
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } catch (RejectedException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Given a regex return all the files that follow it
     * @param query - regexp
     */
    public ArrayList<String> searchFiles(String query) {
        ArrayList<String> result = new ArrayList<>();
        try {
            //searchFilesStatement.setString(1, query);
            //ResultSet rs = searchFilesStatement.executeQuery();

            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("SELECT * FROM clients WHERE fileName REGEXP \"" + query + "\"" );

            while(rs.next()) {
                String sb = rs.getString("ip") +
                        "::" +
                        rs.getString("port") +
                        "::" +
                        rs.getString("fileName") +
                        "::" +
                        rs.getString("path") +
                        "<->";

                result.add(sb);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Deletes a Node from the Database
     * @param ip - ip of the Node to be delete
     * @param port - port of the Node to be delete
     */
    public void deleteNode(String ip, String port) {
        try {
            deleteNodeStatement.setString(1, ip);
            deleteNodeStatement.setString(2, port);

            deleteNodeStatement.executeUpdate();
            System.out.println("(DBMediator) Deleted " + ip + " " + port);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @return all files in the database
     */
    public HashMap<String, String> allFiles() {
        HashMap<String, String> files = new HashMap<>();
        try {
            ResultSet rs = allFilesStatement.executeQuery();
            while(rs.next()){
                files.put(rs.getString("fileName"), rs.getString("path"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        return files;
    }

    /**
     * Get all the nodes containing a particular file
     * @param fileName - Requested file
     * @return all nodes containing the given fileName
     */
    public ArrayList<Node> getFile(String fileName) {
        ArrayList<Node> nodes = new ArrayList<>();
        try {
            getFileStatement.setString(1, fileName);
            ResultSet rs = getFileStatement.executeQuery();
            while(rs.next()){
                Node n = new Node(InetAddress.getByName(rs.getString("ip")), Integer.parseInt(rs.getString("port")));
                n.addFile(rs.getString("fileName"), rs.getString("path"));
                nodes.add(n);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return nodes;
    }

    /**
     * Get all distinct nodes
     * @return an ArrayList of unique nodes
     */
    public ArrayList<Node> getAllNodes(){
        ArrayList<Node> nodes = new ArrayList<>();
        try {
            ResultSet rs = getAllNodesStatement.executeQuery();
            while(rs.next()){
                Node n = new Node(InetAddress.getByName(rs.getString("ip")), Integer.parseInt(rs.getString("port")));
                nodes.add(n);
            }
        } catch (SQLException | UnknownHostException e) {
            e.printStackTrace();
        }
        return nodes;
    }
}
