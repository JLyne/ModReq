package uk.co.notnull.modreq.storage;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import uk.co.notnull.modreq.Configuration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Logger;

public class MysqlDataSource implements DataSource {
	private final Logger logger;

	private final HikariConfig config;
	private HikariDataSource ds;

	public MysqlDataSource(Logger logger, Configuration cfg) {
        this.logger = logger;

		config = new HikariConfig();
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
    }

	@Override
	public Connection getConnection() throws SQLException {
        return ds.getConnection();
    }

	@Override
	public boolean init() {
		try {
			ds = new HikariDataSource(config);
    		Connection connection = ds.getConnection();
			PreparedStatement statement;

			statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS modreq (id INTEGER(10) UNSIGNED PRIMARY KEY auto_increment, uuid CHAR(36), request VARCHAR(256), timestamp BIGINT(13) UNSIGNED, world VARCHAR(256), x INTEGER(11), y INTEGER(11), z INTEGER(11), claimed CHAR(36) NOT NULL DEFAULT '', mod_uuid CHAR(36) NOT NULL DEFAULT '', mod_comment VARCHAR(256) NOT NULL DEFAULT '', mod_timestamp BIGINT(13) UNSIGNED NOT NULL DEFAULT '0', done TINYINT(1) UNSIGNED NOT NULL DEFAULT '0', elevated TINYINT(1) UNSIGNED NOT NULL DEFAULT '0');");
			statement.executeUpdate();
			statement.close();
			statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS modreq_notes (id INTEGER(10) UNSIGNED PRIMARY KEY auto_increment, modreq_id INTEGER(10) UNSIGNED, uuid CHAR(36), note VARCHAR(256));");
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
		ds.close();
	}
}
