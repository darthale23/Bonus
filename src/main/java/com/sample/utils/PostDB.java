package com.sample.utils;
import java.sql.*;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;


public class PostDB {
    private Connection con;
    private LoginInfo login;
    private boolean isLoggedIn;

    public PostDB() {
        isLoggedIn = false;
    }

    public PostDB(LoginInfo login) {
        isLoggedIn = false;
        Login(login);
    }

    public void Login(LoginInfo login) {
        this.login = login;
        connect();
    }

    public void Logout() {
        if (con != null) {
            try {
                con.close();
            } catch (SQLException e) {
                System.out.println(e.getMessage());
                System.out.println("Couldn't close SQL connection");
            }
        }
        isLoggedIn = false;
    }

    public boolean GetLoggedIn() {
        return isLoggedIn;
    }

    private void connect() {
        try {
            int dbPort = 3306;

            JSch jsch = new JSch();
            Session session;
            int realDBPort;

            if (login.jumpHost != null) {
                session = jsch.getSession(login.jumpUser, login.jumpHost, 22);

                if (new File("~/.ssh/known_hosts").exists())
                    jsch.setKnownHosts("~/.ssh/known_hosts");
                session.setPassword(login.jumpPass);
                if (new File("~/.ssh/id_rsa").exists())
                    // Public key
                    jsch.addIdentity("~/.ssh/id_rsa");

                // Connect to SSH jump server
                session.connect();

                // Forward randomly chosen local port through the SSH channel to
                //   database host/port
                realDBPort = session.setPortForwardingL(0, login.dbHost,
                        dbPort);
            } else {
                realDBPort = dbPort;
            }
            String driver = "org.mariadb.jdbc.Driver";
            Class.forName(driver);
            // Connect to the forwarded port (the local end of the SSH tunnel)
            String url = "jdbc:mariadb://localhost:" + realDBPort + "/" +
                login.dbDBName;
            con = DriverManager.getConnection(url, login.dbUser, login.dbPass);

            isLoggedIn = true;
            createTable();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    boolean createTable() {
        if (!isLoggedIn)
            return false;
        try {
            Statement st = con.createStatement();
            String sqlQuery = "CREATE TABLE IF NOT EXISTS Posts(timeStamp DATE, text VARCHAR(200));";
            st.execute(sqlQuery);
            return true;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            System.out.println("Couldn't create SQL Table");
            return false;
        }
    }

    boolean addPost(String post) {
        if (!isLoggedIn)
            return false;
        if (post.length() == 0)
            return false;
        try {
            Statement st = con.createStatement();
            //MariaDB Date format "YYYY-MM-DD"
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("YYYY-MM-dd");
            LocalDateTime now = LocalDateTime.now();
            String timeStampToDB = dtf.format(now);
            String sqlQuery = "INSERT INTO Posts " +
                    "VALUES (\""+ timeStampToDB + "\", '" + post + "')";
            st.executeUpdate(sqlQuery);
            System.out.println("query was successfully executed");
            return true;
        }
        catch (SQLException e) {
            System.out.println(e.getMessage());
            System.out.println("Couldn't add post to DB");
            return false;
        }
    }

    // Returns 'false' if post doesn't exist in DB
    boolean deletePost(String post) {
        if (!isLoggedIn)
            return false;
        try {
            Statement st = con.createStatement();
            String sqlQuery = "DELETE FROM Posts WHERE text = \"" +
                post + "\";";
            int rowsAffected = st.executeUpdate(sqlQuery);
            return rowsAffected != 0;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            System.out.println("Couldn't delete post from DB");
            return false;
        }
    }

    String getAllPosts() {
        if (!isLoggedIn)
            return "Internal error: Not logged in";
        try {
            Statement st = con.createStatement();
            String sql = "SELECT * FROM Posts";
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
}

