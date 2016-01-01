import java.sql.*;
import java.util.HashMap;
import java.util.Random;

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

    public DBMediator(){
        try {
            Class.forName("org.mariadb.jdbc.Driver");
            connection = DriverManager.getConnection(
                    "jdbc:mariadb://localhost:3306/p2p", "root", "root");
            prepareStatements();
            createTables();
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    private void createTables() {
        try {
            Statement statement = connection.createStatement();

            statement.executeUpdate("CREATE TABLE IF NOT EXISTS clients "
                    + "(id VARCHAR(32) NOT NULL,"
                    + " fileName VARCHAR(64) NOT NULL,"
                    + " path VARCHAR(128) NOT NULL);");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void prepareStatements() throws SQLException {

        newNodeStatement = connection.prepareStatement("INSERT INTO clients VALUES(?, ?, ?)");  // adds a new node
        deleteNodeStatement = connection.prepareStatement("DELETE FROM clients WHERE id=?");    // deletes a node
        rmvFileFromNodeStatement = connection.prepareStatement("DELETE FROM clients WHERE fileName = ? and id = ?");    // rmv a file from a given node
        allFilesStatement = connection.prepareStatement("SELECT DISTINCT fileName FROM clients");   // returns all files
        getFileStatement = connection.prepareStatement("SELECT * FROM clients where fileName=?");   // returns all files with a given fileName
    }

    public static DBMediator getInstance(){
        if(instance == null){
            instance = new DBMediator();
        }
        return instance;
    }

    public boolean createNode(String fileName, String path){
        try {
            Random r = new Random();
            String id = Integer.toString(r.nextInt(1000));

            newNodeStatement.setString(1, id);
            newNodeStatement.setString(2, fileName);
            newNodeStatement.setString(3, path);

            int rows = newNodeStatement.executeUpdate();
            if (rows == 1) {
                System.out.println("(DBMediator) Client created: " + id + " filename " + fileName + " path " + path);
                return true;
            } else {
                throw new RejectedException("(DBMediator) Cannot create client :" + fileName);
            }
        } catch (SQLException e) {
            return false;
        } catch (RejectedException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void deleteNode(String fileName) {
        try {
            deleteNodeStatement.setString(1, fileName);
            int rows = deleteNodeStatement.executeUpdate();
            if (rows == 1) {
                System.out.println("(DBMediator) Deleted " + fileName);
            }
            else {
                throw new RejectedException("(DBMediator) Failed to delete " + fileName);
            }
        } catch (SQLException | RejectedException e) {
            e.printStackTrace();
        }
    }

    public void rmvFileFromNodes(String fileName, int id) {
        try {
            rmvFileFromNodeStatement.setString(id, fileName);
            int rows = rmvFileFromNodeStatement.executeUpdate();
            if (rows == 1) System.out.println("(DBMediator) Deleted file" + fileName + " from " + id);
            else throw new RejectedException("(DBMediator) Failed to delete file " + fileName + " from " + id);
        } catch (SQLException | RejectedException e) {
            e.printStackTrace();
        }
    }

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


    public HashMap<String, String> getFile(String fileName) {
        HashMap<String, String> files = new HashMap<>();
        try {
            getFileStatement.setString(1, fileName);
            ResultSet rs = getFileStatement.executeQuery();
            while(rs.next()){
                files.put(rs.getString("fileName"), rs.getString("path"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        return files;
    }
}
