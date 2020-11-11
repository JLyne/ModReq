package uk.co.notnull.modreq.storage;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import uk.co.notnull.modreq.Configuration;
import uk.co.notnull.modreq.ModReq;
import uk.co.notnull.modreq.Request;
import uk.co.notnull.modreq.RequestCollection;

import java.io.File;
import java.sql.*;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

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
		Connection connection = getConnection();

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

	public boolean elevateRequest(int id, boolean elevated) throws SQLException {
		Connection connection = getConnection();
		PreparedStatement pStatement = connection.prepareStatement("UPDATE modreq SET elevated=? WHERE id=?");
		pStatement.setInt(1, elevated ? 1 : 0);
		pStatement.setInt(2, id);
		int result = pStatement.executeUpdate();
		pStatement.close();

		return result > 1;
	}

	public Request closeRequest(Request request, Player mod, String message) throws SQLException {
		long time = System.currentTimeMillis();
		int status = request.isCreatorOnline() ? 2 : 1;
		message = message.trim();

		Connection connection = getConnection();
		PreparedStatement pStatement = connection.prepareStatement("UPDATE modreq SET claimed='',mod_uuid=?,mod_comment=?,mod_timestamp=?,done=?,elevated='0' WHERE id=?");
		pStatement.setString(1, mod.getUniqueId().toString());
		pStatement.setString(2, message);
		pStatement.setLong(3, time);
		pStatement.setLong(4, status);
		pStatement.setInt(5, request.getId());
		pStatement.executeUpdate();

		pStatement.close();
		pStatement = connection.prepareStatement("DELETE FROM modreq_notes WHERE modreq_id=?");
		pStatement.setInt(1, request.getId());
		pStatement.executeUpdate();
		pStatement.close();

		return new Request(request.getId(), request.getCreator(), request.getMessage(),
						   request.getCreateTime(), request.getLocation(), request.getOwner(),
						   mod.getUniqueId(), message, new Date(time), status, false);
	}

	public boolean reopenRequest(int id) throws SQLException {
		Connection connection = getConnection();
		PreparedStatement pStatement = connection.prepareStatement("UPDATE modreq SET claimed='',mod_uuid='',mod_comment='',mod_timestamp='0',done='0',elevated='0' WHERE id=?");

		pStatement.setInt(1, id);
		int result = pStatement.executeUpdate();
		pStatement.close();

		return result > 1;
	}

	public boolean claim(int id, Player player) throws SQLException {
		Connection connection = getConnection();
		PreparedStatement pStatement = connection.prepareStatement("UPDATE modreq SET claimed=? WHERE id=?");

		pStatement.setString(1, player.getUniqueId().toString());
		pStatement.setInt(2, id);

		int result = pStatement.executeUpdate();
		pStatement.close();

		return result > 1;
	}

	public boolean unclaim(int id) throws SQLException {
		Connection connection = getConnection();
		PreparedStatement pStatement = connection.prepareStatement("UPDATE modreq SET claimed='' WHERE id=?");

		pStatement.setInt(1, id);

		int result = pStatement.executeUpdate();
		pStatement.close();

		return result > 1;
	}

	public Request createRequest(Player player, String message) throws SQLException {
		Location location = player.getLocation();
		Connection connection = getConnection();
		long time = System.currentTimeMillis();
		int id;

		message = message.trim();

		PreparedStatement pStatement = connection.prepareStatement("INSERT INTO modreq (uuid,request,timestamp,world,x,y,z) VALUES (?,?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
		pStatement.setString(1, player.getUniqueId().toString());
		pStatement.setString(2, message);
		pStatement.setLong(3, time);
		pStatement.setString(4, location.getWorld().getName());
		pStatement.setInt(5, location.getBlockX());
		pStatement.setInt(6, location.getBlockY());
		pStatement.setInt(7, location.getBlockZ());
		pStatement.executeUpdate();

		ResultSet rs = pStatement.getGeneratedKeys();

		if(!rs.next()) {
			throw new SQLException("No row id returned?");
		}

		id = rs.getInt(1);
		pStatement.close();

		return new Request(id, player.getUniqueId(), message, new Date(time), location);
	}

	public int getOpenRequestCount(boolean includeElevated) throws SQLException {
		Connection connection = getConnection();
		PreparedStatement pStatement;

		if(includeElevated) {
			pStatement= connection.prepareStatement("SELECT COUNT(id) FROM modreq WHERE done='0'");
		} else {
			pStatement= connection.prepareStatement("SELECT COUNT(id) FROM modreq WHERE done='0' AND elevated='0'");
		}

		ResultSet sqlres = pStatement.executeQuery();

		if(!sqlres.next()) {
			sqlres.close();
			pStatement.close();
			throw new SQLException("Invalid response");
		}

		int count = sqlres.getInt(1);
		sqlres.close();
		pStatement.close();

		return count;
	}

	public int getOpenRequestCount(Player player) throws SQLException {
		Connection connection = getConnection();
		PreparedStatement pStatement = connection.prepareStatement("SELECT COUNT(id) FROM modreq WHERE uuid=? AND done='0'");
		pStatement.setString(1, player.getUniqueId().toString());
		ResultSet sqlres = pStatement.executeQuery();

		if (!sqlres.next()) {
			sqlres.close();
			pStatement.close();
			throw new SQLException("Invalid response");
		}

		int count = sqlres.getInt(1);
		sqlres.close();
		pStatement.close();

		return count;
	}

	public RequestCollection getUnseenClosedRequests(Player player) throws SQLException {
		Connection connection = getConnection();
		PreparedStatement pStatement = connection.prepareStatement("SELECT id,request,timestamp,world,x,y,z,claimed,mod_uuid,mod_comment,mod_timestamp,done,elevated FROM modreq WHERE done=1 AND uuid=?");
		pStatement.setString(1, player.getUniqueId().toString());
		ResultSet sqlres = pStatement.executeQuery();

		RequestCollection requests = new RequestCollection();

		while(!sqlres.isAfterLast()) {
			World world = Bukkit.getWorld(sqlres.getString(4));
			Date createdDate = new Date(sqlres.getLong(3));
			Date closedDate = sqlres.getLong(11) != 0 ? new Date(sqlres.getLong(11)) : null;
			Location location = new Location(world, sqlres.getInt(5), sqlres.getInt(6), sqlres.getInt(7));

			UUID owner = sqlres.getString(8).isEmpty() ? null : UUID.fromString(sqlres.getString(8));
			UUID responder = sqlres.getString(9).isEmpty() ? null : UUID.fromString(sqlres.getString(9));

			requests.add(new Request(sqlres.getInt(1), player.getUniqueId(),
									 sqlres.getString(2), createdDate, location,
									 owner, responder, sqlres.getString(10), closedDate,
									 sqlres.getInt(12), sqlres.getBoolean(13)
			));

			sqlres.next();
		}

		sqlres.close();
		pStatement.close();

		return requests;
	}

	public RequestCollection markRequestsAsSeen(RequestCollection requests) throws SQLException {
		if(requests.isEmpty()) {
			return requests;
		}

		Connection connection = getConnection();

		StringBuilder builder = new StringBuilder("UPDATE modreq SET done='2' WHERE id IN (");

		for(int i = 0 ; i < requests.size(); i++) {
			builder.append("?,");
		}

		String sql = builder.deleteCharAt(builder.length() -1).append(")").toString();
		PreparedStatement pStatement = connection.prepareStatement(sql);

		int i = 1;
		for(Request request: requests) {
			pStatement.setInt(i++, request.getId());
		}

		pStatement.executeUpdate();
		pStatement.close();

		requests = requests.stream().map(request -> {
			return new Request(request.getId(), request.getCreator(), request.getMessage(),
						   request.getCreateTime(), request.getLocation(), request.getOwner(),
						   request.getResponder(), request.getResponse(), request.getCloseTime(), request.getDone(), request.isElevated());
		}).collect(RequestCollection::new, RequestCollection::add, RequestCollection::addAll);

		return requests;
	}
}
