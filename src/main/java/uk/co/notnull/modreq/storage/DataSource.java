package uk.co.notnull.modreq.storage;

import org.bukkit.entity.Player;
import uk.co.notnull.modreq.Request;

import java.sql.Connection;
import java.sql.SQLException;

public interface DataSource {
	Connection getConnection() throws SQLException;
	boolean init();
	void destroy();
	boolean requestExists(int id) throws Exception;
	Request getRequest(int id) throws Exception;
	boolean elevateRequest(int id, boolean elevated) throws Exception;
	boolean reopenRequest(int id) throws Exception;
	boolean claim(int id, Player player) throws Exception;
	boolean unclaim(int id) throws Exception;
	Request closeRequest(Request request, Player mod, String message) throws Exception;
}
