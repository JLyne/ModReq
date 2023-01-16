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
		connection.setAutoCommit(false);

		for(int version = currentVersion + 1; version <= latestVersion; version++) {
			migrateToVersion(connection, version);
			plugin.getLogger().info("Commit");
			connection.commit();
		}

		connection.setAutoCommit(true);
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
				.elevated(elevated)
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

		String sql = "SELECT id,uuid,request,timestamp,world,x,y,z,claimed,mod_uuid,mod_comment,mod_timestamp,done,elevated FROM modreq WHERE ";
		List<Object> parameters = new ArrayList<>();

		sql += buildWhere(query, parameters);
		sql += " ORDER BY done ASC, timestamp DESC";

		if(page != null) {
			sql += " LIMIT ?,?";

			parameters.add((page - 1) * cfg.getModreqs_per_page()); //Offset
			parameters.add(cfg.getModreqs_per_page()); //Limit
		}

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
			parameters.addAll(creators.stream().map(UUID::toString).collect(Collectors.toList()));

			if(creators.size() == 1) {
				where.add("uuid = ?");
			} else {
				where.add(buildIn("uuid ", creators.size()));
			}
		}

		if(query.hasResponders()) {
			List<UUID> responders = query.getResponders();
			parameters.addAll(responders.stream().map(UUID::toString).collect(Collectors.toList()));

			if(responders.size() == 1) {
				where.add("mod_uuid = ?");
			} else {
				where.add(buildIn("mod_uuid ", responders.size()));
			}
		}

		if(query.hasOwners()) {
			List<UUID> owners = query.getOwners();
			parameters.addAll(owners.stream().map(UUID::toString).collect(Collectors.toList()));

			if(owners.size() == 1) {
				where.add("claimed = ?");
			} else {
				where.add(buildIn("claimed ", owners.size()));
			}
		}

		if(query.hasStatuses()) {
			List<RequestStatus> statuses = query.getStatuses();
			parameters.addAll(statuses.stream().map(RequestStatus::ordinal).collect(Collectors.toList()));

			if(statuses.size() == 1) {
				where.add("done = ?");
			} else {
				where.add(buildIn("done ", statuses.size()));
			}
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

		//FIXME: There must be a better way
		List<Request> results = requests.stream().map(request -> Request.builder()
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
		).collect(Collectors.toList());

		return requests.toBuilder().requests(results).build();
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

	private Map<Integer, List<Note>> getNotes(List<Integer> ids) throws SQLException {
		if(ids.isEmpty()) {
			new HashMap<>();
		}

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

		while(sqlres.next()) {
			int requestId = sqlres.getInt(2);
			UUID creator = UUID.fromString(sqlres.getString(3));

			if(!results.containsKey(requestId)) {
				results.put(requestId, new ArrayList<>());
			}

			results.get(requestId).add(
					new Note(sqlres.getInt(1), requestId, creator, sqlres.getString(4)));
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

		Map<Integer, List<Note>> notes = getNotes(ids);

		for(int id: ids) {
			if(notes.containsKey(id)) {
				collection.addRequest(requests.get(id).notes(notes.get(id)).build());
			} else {
				collection.addRequest(requests.get(id).build());
			}
		}

		return collection.build();
	}
}
