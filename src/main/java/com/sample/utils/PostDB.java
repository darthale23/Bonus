package com.sample.utils;
import java.sql.*;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;


public class PostDB {
    static Connection con;
    static void connect() {
        try {
            // Gateway Server setting
            String jumpServerHost = "";
            String jumpServerUsername = "";
            String jumpServerPassword = "";
            // DataBase settings
            String databaseHost = "";
            int databasePort = 3306;
            String databaseUsername = "";
            String databasePassword = "";

            JSch jsch = new JSch();

            Session session;
            session = jsch.getSession(jumpServerUsername, jumpServerHost, 22);

            if (new File("~/.ssh/known_hosts").exists())
                jsch.setKnownHosts("~/.ssh/known_hosts");
            session.setPassword(jumpServerPassword);
            if (new File("~/.ssh/id_rsa").exists())
                // Public key
                jsch.addIdentity("~/.ssh/id_rsa");

            // Connect to SSH jump server
            session.connect();

            // Forward randomly chosen local port through the SSH channel to
            //   database host/port
            int forwardedPort = session.setPortForwardingL(0, databaseHost,
                    databasePort);
            String driver = "org.mariadb.jdbc.Driver";
            Class.forName(driver);
            // Connect to the forwarded port (the local end of the SSH tunnel)
            String url = "jdbc:mariadb://localhost:" + forwardedPort + "/dbname";
            con = DriverManager.getConnection(url, databaseUsername, databasePassword);

            createTable();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    static void createTable() {
        try {
            Statement st = con.createStatement();
            String sqlQuery = "CREATE TABLE IF NOT EXISTS Posts(timeStamp DATE, text VARCHAR(200));";
            st.execute(sqlQuery);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            System.out.println("Couldn't create SQL Table");
        }
    }

    static void addPost(String post) {
        if (post.length() == 0)
            return;
        try {
            Statement st = con.createStatement();
            //MariaDB Date format "YYYY-MM-DD"
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("YYYY-MM-DD");
            LocalDateTime now = LocalDateTime.now();
            String timeStampToDB = dtf.format(now);
            String sqlQuery = "INSERT INTO Posts " +
                    "VALUES (\""+ timeStampToDB + "\", '" + post + "')";
            st.executeUpdate(sqlQuery);
            System.out.println("query was successfully executed");
        }
        catch (SQLException e) {
            System.out.println(e.getMessage());
            System.out.println("Couldn't add post to DB");
        }
    }

    // Returns 'false' if post doesn't exist in DB
    static boolean deletePost(String post) {
        try {
            Statement st = con.createStatement();
            String sqlQuery = "DELETE FROM Posts WHERE post = \"" +
                post + "\";";
            int rowsAffected = st.executeUpdate(sqlQuery);
            return rowsAffected != 0;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            System.out.println("Couldn't delete post from DB");
            return false;
        }
    }

    static String getAllPosts() {
        try {
            Statement st = con.createStatement();
            String sql = "select * from SpringBootTable";
            ResultSet rs = st.executeQuery(sql);
            String result = "timeStamp" + "\t" + "text\n";
            // Extract result from ResultSet rs
            while(rs.next()){
                result += "" + rs.getString("timeStamp")
                        + "\t" + rs.getString("text") + "\n";
            }
            rs.close();
            return result;
        }
        catch (SQLException e) {
            System.out.println(e.getMessage());
            System.out.println("Couldn't get all posts from DB");
            return "Internal error: 500";
        }
    }

    public static void main(String[] args) {
        connect();
        addPost("test2");
        System.out.println(getAllPosts());
    }

}

