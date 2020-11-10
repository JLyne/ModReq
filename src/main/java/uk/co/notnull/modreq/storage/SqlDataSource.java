package uk.co.notnull.modreq.storage;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import uk.co.notnull.modreq.Configuration;
import uk.co.notnull.modreq.ModReq;
import uk.co.notnull.modreq.Request;

import java.io.File;
import java.sql.*;

public class SqlDataSource implements DataSource {
	private final ModReq plugin;

	private final HikariConfig config;
	private HikariDataSource ds;
	private final Configuration cfg;

	private File sqliteFile = null;
	private Connection sqliteConnection = null;

	public SqlDataSource(ModReq plugin, Configuration cfg) {
        this.plugin = plugin;
        this.cfg = cfg;
		config = new HikariConfig();

        if(cfg.isMySQL()) {
			String url = "jdbc:mysql://" +
					cfg.getMySQLHost() +
					":" + cfg.getMySQLPort() +
					"/" + cfg.getMySQLDatabase() +
					"?" + cfg.getMySQLOptions();

			config.setJdbcUrl(url);
			config.setUsername(cfg.getMySQLUser());
			config.setPassword(cfg.getMySQLPassword());
			config.addDataSourceProperty("cachePrepStmts" , "true");
			config.addDataSourceProperty("prepStmtCacheSize" , "250");
			config.addDataSourceProperty("prepStmtCacheSqlLimit" , "2048");
		} else {
        	File folder = plugin.getDataFolder();

			if(!folder.exists()) {
				folder.mkdir();
			}

			this.sqliteFile = new File(folder.getAbsolutePath() + File.separator + "modreq.db");
		}
    }

	@Override
	public Connection getConnection() throws SQLException {
		if(cfg.isMySQL()) {
        	return ds.getConnection();
		} else {
			if(sqliteConnection == null || sqliteConnection.isClosed()) {
				sqliteConnection = DriverManager.getConnection("jdbc:sqlite:" + this.sqliteFile.getAbsolutePath());
			}

			return sqliteConnection;
		}
    }

	@Override
	public boolean init() {
		try {
			if(cfg.isMySQL()) {
				ds = new HikariDataSource(config);
			}

    		Connection connection = getConnection();
			PreparedStatement statement;

			if(cfg.isMySQL()) {
				statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS modreq (id INTEGER(10) UNSIGNED PRIMARY KEY auto_increment, uuid CHAR(36), request VARCHAR(256), timestamp BIGINT(13) UNSIGNED, world VARCHAR(256), x INTEGER(11), y INTEGER(11), z INTEGER(11), claimed CHAR(36) NOT NULL DEFAULT '', mod_uuid CHAR(36) NOT NULL DEFAULT '', mod_comment VARCHAR(256) NOT NULL DEFAULT '', mod_timestamp BIGINT(13) UNSIGNED NOT NULL DEFAULT '0', done TINYINT(1) UNSIGNED NOT NULL DEFAULT '0', elevated TINYINT(1) UNSIGNED NOT NULL DEFAULT '0');");
				statement.executeUpdate();
				statement.close();
				statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS modreq_notes (id INTEGER(10) UNSIGNED PRIMARY KEY auto_increment, modreq_id INTEGER(10) UNSIGNED, uuid CHAR(36), note VARCHAR(256));");
			} else {
				statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS modreq (id INTEGER PRIMARY KEY AUTOINCREMENT, uuid CHAR(36), request VARCHAR(256), timestamp UNSIGNED BIGINT(13), world VARCHAR(256), x INTEGER(11), y INTEGER(11), z INTEGER(11), claimed CHAR(36) NOT NULL DEFAULT '', mod_uuid CHAR(36) NOT NULL DEFAULT '', mod_comment VARCHAR(256) NOT NULL DEFAULT '', mod_timestamp UNSIGNED BIGINT(13) NOT NULL DEFAULT '0', done UNSIGNED TINYINT(1) NOT NULL DEFAULT '0', elevated UNSIGNED TINYINT(1) NOT NULL DEFAULT '0');");
				statement.executeUpdate();
				statement.close();
				statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS modreq_notes (id INTEGER PRIMARY KEY AUTOINCREMENT, modreq_id UNSIGNED INTEGER(10), uuid CHAR(36), note VARCHAR(256));");
			}

			statement.executeUpdate();
			statement.close();

			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public void destroy() {
		if(cfg.isMySQL()) {
			ds.close();
		} else if (sqliteConnection != null) {
			try {
				sqliteConnection.close();
			} catch (SQLException ignored) {}
		}
	}

	public boolean requestExists(int id) throws SQLException {
		Connection connection = getConnection();

		PreparedStatement pStatement = connection.prepareStatement("SELECT 1 FROM modreq WHERE id = ?");
		pStatement.setInt(1, id);
		ResultSet sqlres = pStatement.executeQuery();

		boolean exists = sqlres.next();
		sqlres.close();
		pStatement.close();

		return exists;
	}

	public Request getRequest(int id) throws SQLException {
		Connection connection = plugin.getDataSource().getConnection();

		PreparedStatement pStatement = connection.prepareStatement("SELECT uuid,request,timestamp,world,x,y,z,claimed,mod_uuid,mod_comment,mod_timestamp,done,elevated FROM modreq WHERE id = ?");
		pStatement.setInt(1, id);
		ResultSet sqlres = pStatement.executeQuery();

		if(!sqlres.next()) {
			return null;
		} else {
			Request request = new Request(id, sqlres.getString(1), sqlres.getString(2), sqlres.getLong(3),
										  sqlres.getString(4), sqlres.getInt(5), sqlres.getInt(6),
										  sqlres.getInt(7), sqlres.getString(8), sqlres.getString(9),
										  sqlres.getString(10), sqlres.getLong(11), sqlres.getInt(12),
										  sqlres.getInt(13));
			sqlres.close();
			pStatement.close();
			return request;
		}
	}

	public boolean elevateRequest(int id) throws SQLException {
		return true;
	}

	public boolean reopenRequest(int id) throws SQLException {
		Connection connection = plugin.getDataSource().getConnection();
		PreparedStatement pStatement = connection.prepareStatement("UPDATE modreq SET claimed='',mod_uuid='',mod_comment='',mod_timestamp='0',done='0',elevated='0' WHERE id=?");

		pStatement.setInt(1, id);
		int result = pStatement.executeUpdate();
		pStatement.close();

		return result > 1;
	}
}
