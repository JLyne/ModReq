package uk.co.notnull.modreq.storage;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import uk.co.notnull.modreq.*;

import java.io.File;
import java.sql.Connection;
import java.sql.Date;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.DriverManager;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;

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

		PreparedStatement pStatement = connection.prepareStatement("SELECT id,uuid,request,timestamp,world,x,y,z,claimed,mod_uuid,mod_comment,mod_timestamp,done,elevated FROM modreq WHERE id = ?");
		pStatement.setInt(1, id);
		ResultSet sqlres = pStatement.executeQuery();

		if(!sqlres.next()) {
			return null;
		} else {
			Request request = buildRequest(sqlres).notes(getNotes(id)).build();

			sqlres.close();
			pStatement.close();
			return request;
		}
	}

	public Request elevateRequest(Request request, boolean elevated) throws SQLException {
		Connection connection = getConnection();
		PreparedStatement pStatement = connection.prepareStatement("UPDATE modreq SET elevated=? WHERE id=?");
		pStatement.setInt(1, elevated ? 1 : 0);
		pStatement.setInt(2, request.getId());
		int result = pStatement.executeUpdate();
		pStatement.close();

		if(result != 1) {
			throw new SQLException("Row update failed");
		}

		return Request.builder()
				.id(request.getId())
				.creator(request.getCreator())
				.message(request.getMessage())
				.createdAt(request.getCreateTime())
				.location(request.getLocation())
				.claimedBy(request.getOwner())
				.elevated(true)
				.response(request.getResponse())
				.notes(request.getNotes())
				.build();
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

		Response response = new Response(mod.getUniqueId(), message, new Date(time), request.isCreatorOnline());

		return Request.builder()
				.id(request.getId())
				.creator(request.getCreator())
				.message(request.getMessage())
				.createdAt(request.getCreateTime())
				.location(request.getLocation())
				.claimedBy(request.getOwner())
				.elevated(false)
				.response(response)
				.notes(request.getNotes())
				.build();
	}

	public Request reopenRequest(Request request) throws SQLException {
		Connection connection = getConnection();
		PreparedStatement pStatement = connection.prepareStatement("UPDATE modreq SET claimed='',mod_uuid='',mod_comment='',mod_timestamp='0',done='0',elevated='0' WHERE id=?");

		pStatement.setInt(1, request.getId());
		int result = pStatement.executeUpdate();
		pStatement.close();

		if(result != 1) {
			throw new SQLException("Row update failed");
		}

		return Request.builder()
				.id(request.getId())
				.creator(request.getCreator())
				.message(request.getMessage())
				.createdAt(request.getCreateTime())
				.location(request.getLocation())
				.notes(request.getNotes())
				.build();
	}

	public Request claim(Request request, Player player) throws SQLException {
		Connection connection = getConnection();
		PreparedStatement pStatement = connection.prepareStatement("UPDATE modreq SET claimed=? WHERE id=?");

		pStatement.setString(1, player.getUniqueId().toString());
		pStatement.setInt(2, request.getId());

		int result = pStatement.executeUpdate();
		pStatement.close();

		if(result != 1) {
			throw new SQLException("Row update failed");
		}

		return Request.builder()
				.id(request.getId())
				.creator(request.getCreator())
				.message(request.getMessage())
				.createdAt(request.getCreateTime())
				.location(request.getLocation())
				.claimedBy(player.getUniqueId())
				.elevated(request.isElevated())
				.response(request.getResponse())
				.notes(request.getNotes())
				.build();
	}

	public Request unclaim(Request request) throws SQLException {
		Connection connection = getConnection();
		PreparedStatement pStatement = connection.prepareStatement("UPDATE modreq SET claimed='' WHERE id=?");

		pStatement.setInt(1, request.getId());

		int result = pStatement.executeUpdate();
		pStatement.close();

		if(result != 1) {
			throw new SQLException("Row update failed");
		}

		return Request.builder()
				.id(request.getId())
				.creator(request.getCreator())
				.message(request.getMessage())
				.createdAt(request.getCreateTime())
				.location(request.getLocation())
				.elevated(request.isElevated())
				.response(request.getResponse())
				.notes(request.getNotes())
				.build();
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

		return Request.builder()
				.id(id)
				.creator(player.getUniqueId())
				.message(message)
				.createdAt(new Date(time))
				.location(location)
				.build();
	}

	public RequestCollection getOpenRequests(Player player) throws SQLException {
		Connection connection = getConnection();
		PreparedStatement pStatement;

		pStatement = connection.prepareStatement("SELECT id,uuid,request,timestamp,world,x,y,z,claimed,mod_uuid,mod_comment,mod_timestamp,done,elevated FROM modreq WHERE done='0' AND uuid=?");
		pStatement.setString(1, player.getUniqueId().toString());

		ResultSet sqlres = pStatement.executeQuery();
		RequestCollection requests = createRequestCollection(sqlres);

		sqlres.close();
		pStatement.close();

		return requests;
	}

	public RequestCollection getAllOpenRequests(boolean includeElevated) throws SQLException {
		Connection connection = getConnection();
		PreparedStatement pStatement;
		String sql = "SELECT id,uuid,request,timestamp,world,x,y,z,claimed,mod_uuid,mod_comment,mod_timestamp,done,elevated FROM modreq WHERE done='0'";

		if(!includeElevated) {
			sql += " AND elevated = '0'";
		}

		pStatement = connection.prepareStatement(sql);

		ResultSet sqlres = pStatement.executeQuery();
		RequestCollection requests = createRequestCollection(sqlres);

		sqlres.close();
		pStatement.close();

		return requests;
	}

	public RequestCollection getOpenRequests(int page, boolean includeElevated) throws SQLException {
		Connection connection = getConnection();
		PreparedStatement pStatement;
		String sql = "SELECT id,uuid,request,timestamp,world,x,y,z,claimed,mod_uuid,mod_comment,mod_timestamp,done,elevated FROM modreq WHERE done='0'";

		if(!includeElevated) {
			sql += " AND elevated = '0'";
		}

		sql += " LIMIT ?,?";
		pStatement = connection.prepareStatement(sql);
		pStatement.setInt(1, (page - 1) * cfg.getModreqs_per_page());
		pStatement.setInt(2, cfg.getModreqs_per_page());

		ResultSet sqlres = pStatement.executeQuery();
		RequestCollection requests = createRequestCollection(sqlres);

		sqlres.close();
		pStatement.close();

		return requests;
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
		PreparedStatement pStatement = connection.prepareStatement("SELECT id,uuid,request,timestamp,world,x,y,z,claimed,mod_uuid,mod_comment,mod_timestamp,done,elevated FROM modreq WHERE done=1 AND uuid=?");
		pStatement.setString(1, player.getUniqueId().toString());
		ResultSet sqlres = pStatement.executeQuery();

		RequestCollection requests = createRequestCollection(sqlres);

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

		requests = requests.stream().map(request -> Request.builder()
				.id(request.getId())
				.creator(request.getCreator())
				.message(request.getMessage())
				.createdAt(request.getCreateTime())
				.location(request.getLocation())
				.claimedBy(request.getOwner())
				.elevated(request.isElevated())
				.response(request.getResponse())
				.notes(request.getNotes())
				.build()
		).collect(RequestCollection::new, RequestCollection::add, RequestCollection::addAll);

		return requests;
	}

	public List<Note> getNotesForRequest(Request request) throws SQLException {
		Connection connection = getConnection();
		PreparedStatement pStatement = connection.prepareStatement("SELECT id,uuid,note FROM modreq_notes WHERE modreq_id=? ORDER BY id ASC");
		pStatement.setInt(1, request.getId());
		ResultSet sqlres = pStatement.executeQuery();

		List<Note> results = new ArrayList<>();

		while(!sqlres.isAfterLast()) {
			UUID creator = UUID.fromString(sqlres.getString(2));

			results.add(new Note(sqlres.getInt(1), request.getId(), creator, sqlres.getString(3)));
			sqlres.next();
		}

		sqlres.close();
		pStatement.close();

		return results;
	}

	public Note addNoteToRequest(Request request, Player player, String message) throws SQLException {
		Connection connection = getConnection();
		message = message.trim();

		PreparedStatement pStatement = connection.prepareStatement("INSERT INTO modreq_notes (modreq_id,uuid,note) VALUES (?,?,?)", Statement.RETURN_GENERATED_KEYS);
		pStatement.setInt(1, request.getId());
		pStatement.setString(2, player.getUniqueId().toString());
		pStatement.setString(3, message);
		pStatement.executeUpdate();
		pStatement.close();

		ResultSet rs = pStatement.getGeneratedKeys();

		if(!rs.next()) {
			throw new SQLException("No row id returned?");
		}

		int id = rs.getInt(1);
		pStatement.close();

		return new Note(id, request.getId(), player.getUniqueId(), message);
	}

	public boolean removeNote(Note note) throws SQLException {
		Connection connection = getConnection();
		PreparedStatement pStatement = connection.prepareStatement("DELETE FROM modreq_notes WHERE id=?");

		pStatement.setInt(1, note.getId());
		int result = pStatement.executeUpdate();
		pStatement.close();

		return result > 0;
	}

	private List<Note> getNotes(int id) throws SQLException {
		return getNotes(List.of(id)).get(id);
	}

	private Map<Integer, List<Note>> getNotes(List<Integer> ids) throws SQLException {
		Connection connection = getConnection();
		StringBuilder builder = new StringBuilder("SELECT id,modreq_id,uuid,note FROM modreq_notes WHERE modreq_id IN (");
		Map<Integer, List<Note>> results = new HashMap<>();

		for(int id: ids) {
			results.put(id, new ArrayList<>());
			builder.append("?,");
		}

		String sql = builder.deleteCharAt(builder.length() -1).append(")").toString();
		PreparedStatement pStatement = connection.prepareStatement(sql);

		int i = 1;
		for(Integer id: ids) {
			pStatement.setInt(i++, id);
		}

		ResultSet sqlres = pStatement.executeQuery();

		while(!sqlres.isAfterLast()) {
			int requestId = sqlres.getInt(2);
			UUID creator = UUID.fromString(sqlres.getString(3));

			if(!results.containsKey(requestId)) {
				results.put(requestId, new ArrayList<>());
			}

			results.get(requestId).add(
					new Note(sqlres.getInt(1), requestId, creator, sqlres.getString(4)));
			sqlres.next();
		}

		sqlres.close();
		pStatement.close();

		return results;
	}

	private RequestBuilder.BuildStep buildRequest(ResultSet resultSet) throws SQLException {
		World world = Bukkit.getWorld(resultSet.getString(5));
			Date createdDate = new Date(resultSet.getLong(4));
			Date closedDate = resultSet.getLong(12) != 0 ? new Date(resultSet.getLong(12)) : null;
			Location location = new Location(world, resultSet.getInt(6), resultSet.getInt(7), resultSet.getInt(8));

			UUID owner = null;
			UUID responder = null;

			try {
				if(!resultSet.getString(9).isEmpty()) {
					owner = UUID.fromString(resultSet.getString(9));
				}
			} catch(IllegalArgumentException ignored) {}

			try {
				if(!resultSet.getString(10).isEmpty()) {
					responder = UUID.fromString(resultSet.getString(10));
				}
			} catch(IllegalArgumentException ignored) {}

			Response response = null;

			if(responder != null) {
				 response = new Response(responder, resultSet.getString(11), closedDate,
									 resultSet.getInt(13) == 2);
			}

			return Request.builder()
				 .id(resultSet.getInt(1))
				 .creator(UUID.fromString(resultSet.getString(2)))
				 .message(resultSet.getString(3))
				 .createdAt(createdDate)
				 .location(location)
				 .claimedBy(owner)
				 .elevated(resultSet.getBoolean(14))
				 .response(response);
	}

	private RequestCollection createRequestCollection(ResultSet resultSet) throws SQLException {
		Map<Integer, RequestBuilder.BuildStep> requests = new HashMap<>();
		List<Integer> ids = new ArrayList<>();
		RequestCollection results = new RequestCollection();

		if(resultSet.isAfterLast()) {
			return results;
		}

		while(!resultSet.isAfterLast()) {
			int id = resultSet.getInt(1);
			ids.add(id);
			requests.put(id, buildRequest(resultSet));
			resultSet.next();
		}

		Map<Integer, List<Note>> notes = getNotes(ids);

		for(int id: ids) {
			if(notes.containsKey(id)) {
				results.add(requests.get(id).notes(notes.get(id)).build());
			} else {
				results.add(requests.get(id).build());
			}
		}

		return results;
	}
}
