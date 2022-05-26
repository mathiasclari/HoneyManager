package fun.mcbee.bungee.honeynetwork.data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class MYSQL {

    private static String host, database, username, password;
    private static int port;
    private static Connection connection;

    public static String mysql_real_escape_string(String str) {
        if (str == null) {
            return "";
        }

        if (str.replaceAll("[a-zA-Z0-9_!@#$%^&*()-=+~.;:,\\Q[\\E\\Q]\\E<>{}\\/? ]","").length() < 1) {
            return str;
        }

        String clean_string = str;
        clean_string = clean_string.replaceAll("\\\\", "\\\\\\\\");
        clean_string = clean_string.replaceAll("\\n","\\\\n");
        clean_string = clean_string.replaceAll("\\r", "\\\\r");
        clean_string = clean_string.replaceAll("\\t", "\\\\t");
        clean_string = clean_string.replaceAll("\\00", "\\\\0");
        clean_string = clean_string.replaceAll("'", "\\\\'");
        clean_string = clean_string.replaceAll("\\\"", "\\\\\"");
        return clean_string;
    }

    public static void Login(String host, String database, String username, String password, int port) {
        MYSQL.host = host;
        MYSQL.database = database;
        MYSQL.username = username;
        MYSQL.password = password;
        MYSQL.port = port;
        try {
            connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false", username, password);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Connection GetConnection() {
        if(connection != null) {
            Login(host, database, username, password, port);
        }
        return connection;
    }

    public static void CreateTables() {
        Connection con = GetConnection();
        String query = "CREATE TABLE IF NOT EXISTS FriendsManagerPD( " +
                "uuid VARCHAR(36) PRIMARY KEY NOT NULL, " +
                "name VARCHAR(16) NOT NULL, " +
                "nickname VARCHAR(16));";
        Statement stmt = null;
        try {
            stmt = con.createStatement();
            stmt.executeUpdate(query);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if(stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        con = GetConnection();
        query = "CREATE TABLE IF NOT EXISTS NetworkManagerBans( " +
                "uuid VARCHAR(36) NOT NULL PRIMARY KEY, " +
                "time_ban_expires BIGINT DEFAULT -1," +
                "banned_times int(11) DEFAULT 1," +
                "banned_date DATETIME DEFAULT now()," +
                "banned_ip INT(11) DEFAULT 0," +
                "reason VARCHAR(200) DEFAULT null);";
        stmt = null;
        try {
            stmt = con.createStatement();
            stmt.executeUpdate(query);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if(stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        Connection con2 = GetConnection();
        String query2 = "CREATE TABLE IF NOT EXISTS FriendsManagerRelationsPD( " +
                "uuid1 VARCHAR(36) NOT NULL, " +
                "uuid2 VARCHAR(36) NOT NULL, " +
                "date DATETIME NOT NULL DEFAULT now(), " +
                "status SET('Accepted', 'Denied') NOT NULL, " +
                "PRIMARY KEY (uuid1, uuid2));";
        Statement stmt2 = null;
        try {
            stmt2 = con2.createStatement();
            stmt2.executeUpdate(query2);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if(stmt2 != null) {
                try {
                    stmt2.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
