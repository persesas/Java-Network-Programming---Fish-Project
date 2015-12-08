import java.sql.*;
import java.util.HashMap;

public class DBMediator {
    private static final int MIN_PASSWORD_LENGTH = 8;
    private static DBMediator instance;
    private Connection connection;
    private PreparedStatement loginStatement;
    private PreparedStatement createStatement;
    private PreparedStatement sellItemStatement;
    private PreparedStatement allItemsStatement;
    private PreparedStatement deleteItemStatement;
    private PreparedStatement getItemStatement;
    private PreparedStatement incrSalesStatement;
    private PreparedStatement incrPurchasesStatement;
    private PreparedStatement getMetricsStatement;

    public DBMediator(){
        try {
            Class.forName("org.mariadb.jdbc.Driver");
            connection = DriverManager.getConnection(
                    "jdbc:mariadb://localhost:3306/market", "root", "root");
            prepareStatements();
            createTables();
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    private void createTables() {
        try {
            Statement statement = connection.createStatement();

            statement.executeUpdate("CREATE TABLE IF NOT EXISTS users "
                    + "(username VARCHAR(32) PRIMARY KEY,"
                    + " password VARCHAR(32) not null,"
                    + " purchases INTEGER,"
                    + " sales INTEGER)");

            statement.executeUpdate("CREATE TABLE IF NOT EXISTS items "
                    + "(id VARCHAR(32) PRIMARY KEY,"
                    + " sellername VARCHAR(32) not null,"
                    + " itemname VARCHAR(32) not null,"
                    + " price FLOAT,"
                    + " amount INTEGER)");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void prepareStatements() throws SQLException {

        loginStatement = connection.prepareStatement("SELECT * FROM users WHERE username = ? AND password = ?");
        createStatement = connection.prepareStatement("INSERT INTO users VALUES(?, ?, 0, 0)");
        sellItemStatement = connection.prepareStatement("INSERT INTO items VALUES(?, ?, ?, ?, 1)");
        deleteItemStatement = connection.prepareStatement("DELETE FROM items where id=?");
        allItemsStatement = connection.prepareStatement("SELECT * FROM items");
        getItemStatement = connection.prepareStatement("SELECT * FROM items where id=?");
        incrSalesStatement = connection.prepareStatement("UPDATE users SET sales = sales + 1 where username=?");
        incrPurchasesStatement = connection.prepareStatement("UPDATE users SET purchases = purchases + 1 where username=?");
        getMetricsStatement = connection.prepareStatement("SELECT purchases, sales FROM users where username = ?");
    }

    public static DBMediator getInstance(){
        if(instance == null){
            instance = new DBMediator();
        }
        return instance;
    }
    /*
    public HashMap<String, Item> getAllItems() {
        HashMap<String, Item> items = new HashMap<>();
        try {
            ResultSet rs = allItemsStatement.executeQuery();
            while(rs.next()) {
                Item i = new Item(rs.getString("sellername"), rs.getString("itemname"),
                                  rs.getString("id"), rs.getFloat("price"));
                items.put(rs.getString("id"), i);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    public int[] getUserMetrics(String username) {
        try {
            getMetricsStatement.setString(1, username);
            ResultSet rs = getMetricsStatement.executeQuery();
            if(rs.next()) {
                return new int[] {rs.getInt("purchases"), rs.getInt("sales")};
            }
            else
                return null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void incrementSales(String username) {
        try {
            incrSalesStatement.setString(1, username);
            int rows = incrSalesStatement.executeUpdate();
            if (rows == 1) {
                System.out.println("(DBMediator) Increased sales count of " + username);
            }
            else {
                throw new RejectedException("(DBMediator) Failed to increase sales of " + username);
            }
        } catch (SQLException | RejectedException e) {
            e.printStackTrace();
        }
    }

    public void incrementPurchases(String username) {
        try {
            incrPurchasesStatement.setString(1, username);
            int rows = incrPurchasesStatement.executeUpdate();
            if (rows == 1) {
                System.out.println("(DBMediator) Increased purchases count of " + username);
            }
            else {
                throw new RejectedException("(DBMediator) Failed to increase purchases of " + username);
            }
        } catch (SQLException | RejectedException e) {
            e.printStackTrace();
        }
    }

    public Item getItem(String itemid) {
        Item i = null;
        try {
            getItemStatement.setString(1, itemid);
            ResultSet rs = getItemStatement.executeQuery();
            if(rs.next()) {
                i = new Item(rs.getString("sellername"), rs.getString("itemname"),
                        rs.getString("id"), rs.getFloat("price"));
                return i;
            }
            else
                return null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void deleteItem(String itemid) {
        try {
            deleteItemStatement.setString(1, itemid);
            int rows = deleteItemStatement.executeUpdate();
            if (rows == 1) {
                System.out.println("(DBMediator) Item deleted: " + itemid);
            }
            else {
                throw new RejectedException("(DBMediator) Item not found: " + itemid);
            }
        } catch (SQLException | RejectedException e) {
            e.printStackTrace();
        }
    }

    public void sell(String itemid, String sellername, String itemname, float price) {
        try {
            sellItemStatement.setString(1, itemid);
            sellItemStatement.setString(2, sellername);
            sellItemStatement.setString(3, itemname);
            sellItemStatement.setFloat(4, price);
            int rows = sellItemStatement.executeUpdate();
            if (rows == 1) {
                System.out.println("(DBMediator) New item for sale: " + itemname + " @ " + price);
            }
            else {
                throw new RejectedException("(DBMediator) Cannot post item for sale.");
            }
        } catch (SQLException | RejectedException e) {
            e.printStackTrace();
        }
    }

    public boolean login(String username, String password){
        try {
            loginStatement.setString(1, username);
            loginStatement.setString(2, password);
            ResultSet result = loginStatement.executeQuery();
            return result.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean createClient(String username, String password){
        if (password.length() >= MIN_PASSWORD_LENGTH) {
            try {
                createStatement.setString(1, username);
                createStatement.setString(2, password);
                int rows = createStatement.executeUpdate();
                if (rows == 1) {
                    System.out.println("(DBMediator) Client created: " + username);
                    return true;
                } else {
                    throw new RejectedException("(DBMediator) Cannot create client :" + username);
                }
            } catch (SQLException e) {
                System.out.println("(DBMediator) Duplicate user " + username);
                return false;
            } catch (RejectedException e) {
                e.printStackTrace();
                return false;
            }
        }
        else
            return false;
    }
    */
}
