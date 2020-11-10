package uk.co.notnull.modreq.storage;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Logger;

public class SqliteDataSource implements DataSource {
	private final Logger logger;

	private final HikariConfig config;
	private HikariDataSource ds;
    private File sqlFile;

    public SqliteDataSource(Logger pLogger, String filename, String location) {
        this.logger = pLogger;

        File folder = new File(location);

        if(!folder.exists()) {
			folder.mkdir();
		}

        this.sqlFile = new File(folder.getAbsolutePath() + File.separator + filename + ".db");

        config = new HikariConfig();
		config.setPoolName(filename);
		config.setDriverClassName("org.sqlite.JDBC");
		config.setJdbcUrl("jdbc:sqlite:" + this.sqlFile.getAbsolutePath());
		config.setConnectionTestQuery("SELECT 1");
		config.setMaximumPoolSize(5);
		config.setMinimumIdle(2);
    }

	@Override
	public Connection getConnection() throws SQLException {
		return ds.getConnection();
	}

	@Override
	public void destroy() {
		ds.close();
	}

	@Override
	public boolean init() {
		try {
			ds = new HikariDataSource(config);
    		Connection connection = ds.getConnection();
			PreparedStatement statement;

			statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS modreq (id INTEGER PRIMARY KEY AUTOINCREMENT, uuid CHAR(36), request VARCHAR(256), timestamp UNSIGNED BIGINT(13), world VARCHAR(256), x INTEGER(11), y INTEGER(11), z INTEGER(11), claimed CHAR(36) NOT NULL DEFAULT '', mod_uuid CHAR(36) NOT NULL DEFAULT '', mod_comment VARCHAR(256) NOT NULL DEFAULT '', mod_timestamp UNSIGNED BIGINT(13) NOT NULL DEFAULT '0', done UNSIGNED TINYINT(1) NOT NULL DEFAULT '0', elevated UNSIGNED TINYINT(1) NOT NULL DEFAULT '0');");
			statement.executeUpdate();
			statement.close();
			statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS modreq_notes (id INTEGER PRIMARY KEY AUTOINCREMENT, modreq_id UNSIGNED INTEGER(10), uuid CHAR(36), note VARCHAR(256));");
			statement.executeUpdate();
			statement.close();

			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
}
