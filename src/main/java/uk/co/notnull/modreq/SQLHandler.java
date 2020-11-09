package uk.co.notnull.modreq;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Logger;

public class SQLHandler {
    private Logger logger;
    private String mysql_hostname;
    private String mysql_port;
    private String mysql_database;
    private String mysql_username;
    private String mysql_password;
    private String sqlite_name;
    private String sqlite_location;
    private File sqlFile;
    private boolean usingMySQL;

    public SQLHandler(Logger pLogger, String pMysql_hostname, String pMysql_port, String pMysql_database, String pMysql_username, String pMysql_password, String pSqlite_name, String pSqlite_location, boolean pUsingMySQL) {
        this.logger = pLogger;
        this.mysql_hostname = pMysql_hostname;
        this.mysql_port = pMysql_port;
        this.mysql_database = pMysql_database;
        this.mysql_username = pMysql_username;
        this.mysql_password = pMysql_password;
        this.sqlite_name = pSqlite_name;
        this.sqlite_location = pSqlite_location;
        this.usingMySQL = pUsingMySQL;
        if (!this.usingMySQL) {
            File folder = new File(this.sqlite_location);
            if (!folder.exists()) {
                folder.mkdir();
            }

            this.sqlFile = new File(folder.getAbsolutePath() + File.separator + this.sqlite_name + ".db");
        }

    }

    private boolean initializeMySQL() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            return true;
        } catch (ClassNotFoundException var2) {
            this.logger.warning("Cannot open MySQL connection. The driver class is missing.");
            return false;
        }
    }

    private boolean initializeSQLite() {
        try {
            Class.forName("org.sqlite.JDBC");
            return true;
        } catch (ClassNotFoundException var2) {
            this.logger.warning("Cannot open SQLite connection. The driver class is missing.");
            return false;
        }
    }

    public Connection open() throws SQLException {
        if (this.usingMySQL) {
            if (this.initializeMySQL()) {
                String url = "jdbc:mysql://" + this.mysql_hostname + ":" + this.mysql_port + "/" + this.mysql_database;
                Connection connection = DriverManager.getConnection(url, this.mysql_username, this.mysql_password);
                return connection;
            } else {
                throw new SQLException("Cannot open MySQL connection. The driver class is missing.");
            }
        } else if (this.initializeSQLite()) {
            Connection connection = DriverManager.getConnection("jdbc:sqlite:" + this.sqlFile.getAbsolutePath());
            return connection;
        } else {
            throw new SQLException("Cannot open SQLite connection. The driver class is missing.");
        }
    }
}
