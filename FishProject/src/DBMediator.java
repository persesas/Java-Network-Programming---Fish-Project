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
    private PreparedStatement rmvFileFromNodeStatement;

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
                    + " path VARCHAR(128) NOT NULL);");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Prepares statements
    private void prepareStatements() throws SQLException {
        newNodeStatement = connection.prepareStatement("INSERT INTO clients VALUES(?, ?, ?, ?)");  // adds a new node
        deleteNodeStatement = connection.prepareStatement("DELETE FROM clients WHERE ip=? AND port=?");    // deletes a node
        rmvFileFromNodeStatement = connection.prepareStatement("DELETE FROM clients WHERE fileName = ? AND ip = ? AND port=?");    // rmv a file from a given node
        allFilesStatement = connection.prepareStatement("SELECT DISTINCT fileName FROM clients");   // returns all files
        getFileStatement = connection.prepareStatement("SELECT * FROM clients where fileName=?");   // returns all files with a given fileName
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
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } catch (RejectedException e) {
            e.printStackTrace();
            return false;
        }
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

            int rows = deleteNodeStatement.executeUpdate();
            if (rows == 1) {
                System.out.println("(DBMediator) Deleted " + ip + " " + port);
            }
            else {
                throw new RejectedException("(DBMediator) Failed to delete " + ip + " " + port);
            }
        } catch (SQLException | RejectedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Removes a file from a Node
     * @param ip - ip of Node
     * @param port - port of Node
     * @param fileName - fileName to be deleted from Node
     */
    public void rmvFileFromNode(String ip, String port, String fileName) {
        try {
            rmvFileFromNodeStatement.setString(1, ip);
            rmvFileFromNodeStatement.setString(2, port);
            rmvFileFromNodeStatement.setString(3, fileName);
            int rows = rmvFileFromNodeStatement.executeUpdate();
            if (rows == 1) System.out.println("(DBMediator) Deleted file" + fileName + " from " + ip + " " + port);
            else throw new RejectedException("(DBMediator) Failed to delete file " + fileName + " from " + ip + " " + port);
        } catch (SQLException | RejectedException e) {
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
}
