/*
 * ModReq
 * Copyright (C) 2021 James Lyne
 *
 * Based on ModReq 1.2 (https://www.spigotmc.org/resources/modreq.57560/)
 * Copyright (C) 2019 Aladram and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package uk.co.notnull.modreq.storage;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.Nullable;
import uk.co.notnull.modreq.*;
import uk.co.notnull.modreq.collections.RequestCollection;
import uk.co.notnull.modreq.collections.RequestCollectionBuilder;

import java.io.File;
import java.sql.Connection;
import java.sql.Date;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.DriverManager;

import java.util.*;
import java.util.stream.Collectors;

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
		Connection connection;

		if(cfg.isMySQL()) {
        	connection = ds.getConnection();
		} else {
			if(sqliteConnection == null || sqliteConnection.isClosed()) {
				sqliteConnection = DriverManager.getConnection("jdbc:sqlite:" + this.sqliteFile.getAbsolutePath());
			}

			connection = sqliteConnection;
		}

		connection.setAutoCommit(false);

		return connection;
    }

	@Override
	public boolean init() {
		try {
			if(cfg.isMySQL()) {
				ds = new HikariDataSource(config);
			}

			migrateData();

			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	private int getCurrentDataVersion() throws SQLException {
		Connection connection = getConnection();
		PreparedStatement statement = connection.prepareStatement("PRAGMA user_version");
		ResultSet result = statement.executeQuery();

		int version = result.getInt(1);
		plugin.getLogger().info(String.valueOf(version));
		result.close();
		statement.close();

		return version;
	}

	private int getLatestDataVersion() {
		return 2;
	}

	public void migrateData() throws SQLException {
		int currentVersion = getCurrentDataVersion();
		int latestVersion = getLatestDataVersion();
		Connection connection = getConnection();

		for(int version = currentVersion + 1; version <= latestVersion; version++) {
			migrateToVersion(connection, version);
			connection.commit();
		}
	}

	private void migrateToVersion(Connection connection, int version) throws SQLException {
		PreparedStatement statement;

		plugin.getLogger().info("Migrating DB to version " + version);

		switch (version) {
			case 1 -> {
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

				statement = connection.prepareStatement("PRAGMA user_version = 1");
				statement.executeUpdate();
				statement.close();
			}
			case 2 -> {
				// Rename notes table
				statement = connection.prepareStatement("ALTER TABLE modreq_notes RENAME TO modreq_updates");
				statement.executeUpdate();
				statement.close();

				statement = connection.prepareStatement("ALTER TABLE modreq_updates RENAME COLUMN uuid TO actor");
				statement.executeUpdate();
				statement.close();

				statement = connection.prepareStatement("ALTER TABLE modreq_updates RENAME COLUMN note TO content");
				statement.executeUpdate();
				statement.close();

				statement = connection.prepareStatement("ALTER TABLE modreq_updates ADD COLUMN timestamp BIGINT(13)");
				statement.executeUpdate();
				statement.close();

				statement = connection.prepareStatement(
						"ALTER TABLE modreq_updates ADD COLUMN seen TINYINT DEFAULT 0 NOT NULL");
				statement.executeUpdate();
				statement.close();

				// Add update type (0 being old notes)
				statement = connection.prepareStatement(
						"ALTER TABLE modreq_updates ADD COLUMN type INT DEFAULT 0 NOT NULL");
				statement.executeUpdate();
				statement.close();

				// Move old mod responses to update table
				statement = connection.prepareStatement(
						"SELECT id, mod_uuid, mod_comment, mod_timestamp, done FROM modreq WHERE mod_uuid <> ''");
				ResultSet modreqs = statement.executeQuery();

				while(modreqs.next()) {
					statement = connection.prepareStatement(
						"INSERT INTO modreq_updates (modreq_id,actor,content,type,timestamp,seen) VALUES(?,?,?,?,?,?)");
					statement.setInt(1, modreqs.getInt(1));
					statement.setString(2, modreqs.getString(2));
					statement.setString(3, modreqs.getString(3));
					statement.setInt(4, 2); // Type
					statement.setLong(5, modreqs.getLong(4));
					statement.setBoolean(6, modreqs.getInt(5) == 2); // Seen
					statement.executeUpdate();
				}

				// Remove old response columns
				statement = connection.prepareStatement("ALTER TABLE modreq DROP COLUMN mod_uuid");
				statement.executeUpdate();
				statement.close();

				statement = connection.prepareStatement("ALTER TABLE modreq DROP COLUMN mod_comment");
				statement.executeUpdate();
				statement.close();

				statement = connection.prepareStatement("ALTER TABLE modreq DROP COLUMN mod_timestamp");
				statement.executeUpdate();
				statement.close();

				// Mark all old notes as seen
				statement = connection.prepareStatement("UPDATE modreq_updates SET seen = 1 WHERE type = 0");
				statement.executeUpdate();
				statement.close();

				statement = connection.prepareStatement("PRAGMA user_version = 2");
				statement.executeUpdate();
				statement.close();
			}
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

	public RequestCollection getAllRequests(RequestQuery query) throws SQLException {
		PreparedStatement statement = getQuery(query, null);
		ResultSet results = statement.executeQuery();

		RequestCollection collection = createRequestCollection(results);

		statement.close();
		results.close();

		return collection;
	}

	public RequestCollection getRequests(RequestQuery query, int page) throws SQLException {
		int offset = (page - 1) * cfg.getModreqs_per_page();
		int count = getRequestCount(query);
		RequestCollectionBuilder builder = RequestCollection.builder().paginated(offset, count);

		if(count == 0 || count < ((cfg.getModreqs_per_page() * (page - 1)) + 1)) {
			return builder.build();
		}

		PreparedStatement statement = getQuery(query, page);
		ResultSet results = statement.executeQuery();
		RequestCollection collection = populateRequestCollection(builder, results);

		statement.close();
		results.close();

		return collection;
	}

	public int getRequestCount(RequestQuery query) throws SQLException {
		PreparedStatement statement = getCountQuery(query);
		ResultSet sqlres = statement.executeQuery();

		if(!sqlres.next()) {
			sqlres.close();
			statement.close();
			throw new SQLException("Invalid response");
		}

		int count = sqlres.getInt(1);
		sqlres.close();
		statement.close();

		return count;
	}

	public Request getRequest(int id) throws SQLException {
		Connection connection = getConnection();

		PreparedStatement requestStatement = connection.prepareStatement(
				"SELECT m.*, u.id as update_id, u.type, u.actor, u.timestamp, u.timestamp as update_timestamp, " +
						"u.content, u.seen FROM modreq m " +
						"LEFT JOIN (SELECT modreq_id, MAX(id) as latest_update FROM modreq_updates GROUP BY modreq_id) lu " +
						"ON lu.modreq_id = m.id " +
						"LEFT JOIN modreq_updates u ON u.id = lu.latest_update WHERE m.id = ?");
		requestStatement.setInt(1, id);

		ResultSet requestSet = requestStatement.executeQuery();
		if(!requestSet.next()) {
			return null;
		} else {
			Request request = buildRequest(requestSet).updates(getUpdates(id)).build();

			requestSet.close();
			requestStatement.close();
			return request;
		}
	}

	public Request elevateRequest(Request request, Player mod, boolean elevated) throws SQLException {
		Connection connection = getConnection();

		PreparedStatement pStatement = connection.prepareStatement("UPDATE modreq SET elevated=? WHERE id=?");
		pStatement.setInt(1, elevated ? 1 : 0);
		pStatement.setInt(2, request.getId());
		int result = pStatement.executeUpdate();
		pStatement.close();

		if(result != 1) {
			throw new SQLException("Row update failed");
		}

		Update update = addUpdateToRequest(connection, request, UpdateType.ELEVATE, mod, null);
		connection.commit();

		//TODO Add to update list

		return Request.builder()
				.id(request.getId())
				.creator(request.getCreator())
				.message(request.getMessage())
				.createdAt(request.getCreateTime())
				.location(request.getLocation())
				.claimedBy(request.getOwner())
				.elevated(elevated)
				.lastUpdate(update)
				.updates(request.getUpdates())
				.build();
	}

	public Request closeRequest(Request request, Player mod, String message) throws SQLException {
		long time = System.currentTimeMillis();
		int status = request.isCreatorOnline() ? 2 : 1;
		message = message.trim();

		Connection connection = getConnection();
		PreparedStatement pStatement = connection.prepareStatement("UPDATE modreq SET claimed='',done=?,elevated='0' WHERE id=?");
		pStatement.setString(1, mod.getUniqueId().toString());
		pStatement.setString(2, message);
		pStatement.setLong(3, time);
		pStatement.setLong(4, status);
		pStatement.setInt(5, request.getId());
		pStatement.executeUpdate();
		pStatement.close();

		Update update = addUpdateToRequest(connection, request, UpdateType.CLOSE, mod, message);
		connection.commit();
		//TODO Add to update list

		return Request.builder()
				.id(request.getId())
				.creator(request.getCreator())
				.message(request.getMessage())
				.createdAt(request.getCreateTime())
				.location(request.getLocation())
				.claimedBy(request.getOwner())
				.elevated(false)
				.lastUpdate(update)
				.updates(request.getUpdates())
				.build();
	}

	public Request reopenRequest(Request request, Player mod) throws SQLException {
		Connection connection = getConnection();
		PreparedStatement pStatement = connection.prepareStatement("UPDATE modreq SET claimed=NULL,done=0,elevated=0 WHERE id=?");

		pStatement.setInt(1, request.getId());
		int result = pStatement.executeUpdate();
		pStatement.close();

		if(result != 1) {
			throw new SQLException("Row update failed");
		}

		Update update = addUpdateToRequest(connection, request, UpdateType.REOPEN, mod, null);
		connection.commit();
		//TODO Add to update list

		return Request.builder()
				.id(request.getId())
				.creator(request.getCreator())
				.message(request.getMessage())
				.createdAt(request.getCreateTime())
				.location(request.getLocation())
				.updates(request.getUpdates())
				.lastUpdate(update)
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

		Update update = addUpdateToRequest(connection, request, UpdateType.CLAIM, player, null);
		connection.commit();
		//TODO Add to update list

		return Request.builder()
				.id(request.getId())
				.creator(request.getCreator())
				.message(request.getMessage())
				.createdAt(request.getCreateTime())
				.location(request.getLocation())
				.claimedBy(player.getUniqueId())
				.elevated(request.isElevated())
				.lastUpdate(update)
				.updates(request.getUpdates())
				.build();
	}

	public Request unclaim(Request request, Player mod) throws SQLException {
		Connection connection = getConnection();
		PreparedStatement pStatement = connection.prepareStatement("UPDATE modreq SET claimed='' WHERE id=?");

		pStatement.setInt(1, request.getId());

		int result = pStatement.executeUpdate();
		pStatement.close();

		if(result != 1) {
			throw new SQLException("Row update failed");
		}

		Update update = addUpdateToRequest(connection, request, UpdateType.UNCLAIM, mod, null);
		connection.commit();

		return Request.builder()
				.id(request.getId())
				.creator(request.getCreator())
				.message(request.getMessage())
				.createdAt(request.getCreateTime())
				.location(request.getLocation())
				.elevated(request.isElevated())
				.lastUpdate(update)
				.updates(request.getUpdates())
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

		//Update

		return Request.builder()
				.id(id)
				.creator(player.getUniqueId())
				.message(message)
				.createdAt(new Date(time))
				.location(location)
				.build();
	}

	private PreparedStatement getCountQuery(RequestQuery query) throws SQLException {
		Connection connection = getConnection();

		String sql = "SELECT COUNT(*) FROM modreq WHERE ";
		List<Object> parameters = new ArrayList<>();

		sql += buildWhere(query, parameters);

		PreparedStatement statement = connection.prepareStatement(sql);

		for(int i = 0; i < parameters.size(); i++) {
			statement.setObject(i + 1, parameters.get(i));
		}

		return statement;
	}

	private PreparedStatement getQuery(RequestQuery query, @Nullable Integer page) throws SQLException {
		Connection connection = getConnection();

		String sql = "SELECT m.*, u.id as update_id, u.type, u.actor, u.timestamp, u.timestamp as update_timestamp, " +
				"u.content, u.seen FROM modreq m " +
				"LEFT JOIN (SELECT modreq_id, MAX(id) as latest_update FROM modreq_updates GROUP BY modreq_id) lu " +
				"ON lu.modreq_id = m.id " +
				"LEFT JOIN modreq_updates u ON u.id = lu.latest_update WHERE ";
		List<Object> parameters = new ArrayList<>();

		sql += buildWhere(query, parameters);
		sql += " ORDER BY done ASC, timestamp DESC";

		if(page != null) {
			sql += " LIMIT ?,?";

			parameters.add((page - 1) * cfg.getModreqs_per_page()); //Offset
			parameters.add(cfg.getModreqs_per_page()); //Limit
		}

		plugin.getLogger().info(sql);

		PreparedStatement statement = connection.prepareStatement(sql);

		for(int i = 0; i < parameters.size(); i++) {
			statement.setObject(i + 1, parameters.get(i));
		}

		return statement;
	}

	private String buildWhere(RequestQuery query, List<Object> parameters) {
		List<String> where = new ArrayList<>();

		if(query.hasIds()) {
			List<Integer> ids = query.getIds();
			parameters.addAll(ids);

			if(ids.size() == 1) {
				where.add("id = ?");
			} else {
				where.add(buildIn("id ", ids.size()));
			}
		}

		if(query.hasCreators()) {
			List<UUID> creators = query.getCreators();
			parameters.addAll(creators.stream().map(UUID::toString).toList());

			if(creators.size() == 1) {
				where.add("uuid = ?");
			} else {
				where.add(buildIn("uuid ", creators.size()));
			}
		}

		if(query.hasResponders()) {
			List<UUID> responders = query.getResponders();
			parameters.addAll(responders.stream().map(UUID::toString).toList());

			if(responders.size() == 1) {
				where.add("mod_uuid = ?");
			} else {
				where.add(buildIn("mod_uuid ", responders.size()));
			}
		}

		if(query.hasOwners()) {
			List<UUID> owners = query.getOwners();
			parameters.addAll(owners.stream().map(UUID::toString).toList());

			if(owners.size() == 1) {
				where.add("claimed = ?");
			} else {
				where.add(buildIn("claimed ", owners.size()));
			}
		}

		if(query.hasStatuses()) {
			List<RequestStatus> statuses = query.getStatuses();
			parameters.addAll(statuses.stream().map(RequestStatus::ordinal).toList());

			if(statuses.size() == 1) {
				where.add("done = ?");
			} else {
				where.add(buildIn("done ", statuses.size()));
			}
		}

		if(query.hasSeenState()) {
			where.add("seen = ?");
			parameters.add(query.getSeenState() ? 1 : 0);
		}

		if(query.hasSearch()) {
			parameters.add("%" + query.getSearch() + "%");

			where.add("request LIKE ?");
		}

		return String.join(" AND ", where);
	}

	private String buildIn(String sql, int count) {
		sql += "IN(?" + ",?".repeat(Math.max(0, count - 1)) + ")";
		return sql;
	}

	public RequestCollection markRequestsAsSeen(RequestCollection requests) throws SQLException {
		if(requests.isEmpty()) {
			return requests;
		}

		Connection connection = getConnection();

		StringBuilder builder = new StringBuilder("UPDATE modreq SET done='2' WHERE id IN (");

		builder.append("?,".repeat(requests.size()));

		String sql = builder.deleteCharAt(builder.length() -1).append(")").toString();
		PreparedStatement pStatement = connection.prepareStatement(sql);

		int i = 1;
		for(Request request: requests) {
			pStatement.setInt(i++, request.getId());
		}

		pStatement.executeUpdate();
		pStatement.close();
		connection.commit();

		//FIXME: There must be a better way
		List<Request> results = requests.stream().map(request -> Request.builder()
				.id(request.getId())
				.creator(request.getCreator())
				.message(request.getMessage())
				.createdAt(request.getCreateTime())
				.location(request.getLocation())
				.claimedBy(request.getOwner())
				.elevated(request.isElevated())
				.lastUpdate(request.getLastUpdate())
				.updates(request.getUpdates())
				.build()
		).collect(Collectors.toList());

		return requests.toBuilder().requests(results).build();
	}

	public List<Update> getUpdatesForRequest(Request request) throws SQLException {
		Connection connection = getConnection();
		PreparedStatement pStatement = connection.prepareStatement(
				"SELECT id,type,actor,timestamp,content,seen FROM modreq_updates WHERE modreq_id=? ORDER BY time DESC");
		pStatement.setInt(1, request.getId());
		ResultSet sqlres = pStatement.executeQuery();

		List<Update> results = new ArrayList<>();

		while(!sqlres.isAfterLast()) {
			UUID creator = UUID.fromString(sqlres.getString(2));
			Date time = new Date(sqlres.getLong(4));
			UpdateType type = UpdateType.values()[sqlres.getInt(2)];

			results.add(new Update(sqlres.getInt(1), request.getId(), type, time, creator,
								   sqlres.getString(5), sqlres.getBoolean(6)));
			sqlres.next();
		}

		sqlres.close();
		pStatement.close();

		return results;
	}

	public Update addCommentToRequest(Request request, Player player, boolean isPublic, String message) throws SQLException {
		UpdateType type = isPublic ? UpdateType.PUBLIC_COMMENT : UpdateType.PRIVATE_COMMENT;
		Connection connection = getConnection();

		Update update = addUpdateToRequest(connection, request, type, player, message);
		connection.commit();

		return update;
	}

	public boolean removeComment(Update update) throws SQLException {
		Connection connection = getConnection();
		PreparedStatement pStatement = connection.prepareStatement("DELETE FROM modreq_updates WHERE id=?");

		pStatement.setInt(1, update.getId());
		int result = pStatement.executeUpdate();
		pStatement.close();
		connection.commit();

		return result > 0;
	}

	private Update addUpdateToRequest(Connection connection, Request request, UpdateType type, Player player, String message) throws SQLException {
		long time = System.currentTimeMillis();

		if(message != null) {
			message = message.trim();
		}

		PreparedStatement pStatement = connection.prepareStatement(
				"INSERT INTO modreq_updates (modreq_id,type,actor,timestamp,content) VALUES (?,?,?,?,?)",
				Statement.RETURN_GENERATED_KEYS);
		pStatement.setInt(1, request.getId());
		pStatement.setInt(2, type.ordinal());
		pStatement.setString(3, player.getUniqueId().toString());
		pStatement.setLong(4, time);
		pStatement.setString(5, message);
		pStatement.executeUpdate();
		pStatement.close();

		ResultSet rs = pStatement.getGeneratedKeys();

		if(!rs.next()) {
			throw new SQLException("No row id returned?");
		}

		int id = rs.getInt(1);
		rs.close();
		pStatement.close();

		return new Update(id, request.getId(), type, new Date(time), player.getUniqueId(), message);
	}

	private List<Update> getUpdates(int id) throws SQLException {
		return getUpdates(List.of(id)).get(id);
	}

	private int getCount(PreparedStatement statement) throws SQLException {
		ResultSet sqlres = statement.executeQuery();

		if(!sqlres.next()) {
			sqlres.close();
			statement.close();
			throw new SQLException("Invalid response");
		}

		int count = sqlres.getInt(1);
		sqlres.close();

		return count;
	}

	private Map<Integer, List<Update>> getUpdates(List<Integer> ids) throws SQLException {
		if(ids.isEmpty()) {
			new HashMap<>();
		}

		Connection connection = getConnection();
		StringBuilder builder = new StringBuilder("SELECT id,modreq_id,type,timestamp,actor,content,seen FROM modreq_updates WHERE modreq_id IN (");
		Map<Integer, List<Update>> results = new HashMap<>();

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

		while(sqlres.next()) {
			int requestId = sqlres.getInt(2);
			UUID creator = UUID.fromString(sqlres.getString(5));

			if(!results.containsKey(requestId)) {
				results.put(requestId, new ArrayList<>());
			}

			results.get(requestId).add(new Update(
					sqlres.getInt("id"), requestId,
					UpdateType.values()[sqlres.getInt("type")],
					new Date(sqlres.getLong("timestamp")), creator,
					sqlres.getString("content"), sqlres.getBoolean("seen")));
		}

		sqlres.close();
		pStatement.close();

		return results;
	}

	private RequestBuilder.BuildStep buildRequest(ResultSet resultSet) throws SQLException {
		int id = resultSet.getInt(1);
		World world = Bukkit.getWorld(resultSet.getString("world"));
		Date createdDate = new Date(resultSet.getLong("timestamp"));
		Location location = new Location(world, resultSet.getInt("x"),
										 resultSet.getInt("y"), resultSet.getInt("z"));
		RequestStatus status = RequestStatus.values()[resultSet.getInt("done")];

		UUID owner = null;

		try {
			if(!resultSet.getString("claimed").isEmpty()) {
				owner = UUID.fromString(resultSet.getString(9));
			}
		} catch(IllegalArgumentException ignored) {}

		Update lastUpdate = null;

		int updateId = resultSet.getInt("update_id");

		if(updateId > 0) {
			UpdateType type = UpdateType.values()[resultSet.getInt("type")];
			UUID actor = UUID.fromString(resultSet.getString("actor"));
			Date date = new Date(resultSet.getLong("update_timestamp"));

			lastUpdate = new Update(updateId, id, type, date, actor,
									resultSet.getString("content"), resultSet.getBoolean("seen"));
		}

		return Request.builder()
			 .id(id)
			 .creator(UUID.fromString(resultSet.getString("uuid")))
			 .message(resultSet.getString("request"))
			 .createdAt(createdDate)
			 .location(location)
			 .claimedBy(owner)
			 .elevated(resultSet.getBoolean("elevated"))
			 .status(status)
			 .lastUpdate(lastUpdate);
	}

	private RequestCollection createRequestCollection(ResultSet resultSet) throws SQLException {
		return populateRequestCollection(RequestCollection.builder(), resultSet);
	}

	private RequestCollection populateRequestCollection(RequestCollectionBuilder collection, ResultSet resultSet) throws SQLException {
		List<Integer> ids = new ArrayList<>();

		if(resultSet.isAfterLast() || !resultSet.next()) {
			return collection.build();
		}

		Map<Integer, RequestBuilder.BuildStep> requests = new HashMap<>();

		while(!resultSet.isAfterLast()) {
			int id = resultSet.getInt(1);
			ids.add(id);
			requests.put(id, buildRequest(resultSet));
			resultSet.next();
		}

		Map<Integer, List<Update>> notes = getUpdates(ids);

		for(int id: ids) {
			if(notes.containsKey(id)) {
				collection.addRequest(requests.get(id).updates(notes.get(id)).build());
			} else {
				collection.addRequest(requests.get(id).build());
			}
		}

		return collection.build();
	}
}
