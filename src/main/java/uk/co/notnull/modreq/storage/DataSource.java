package uk.co.notnull.modreq.storage;

import org.bukkit.entity.Player;
import uk.co.notnull.modreq.Request;
import uk.co.notnull.modreq.RequestCollection;

import java.sql.Connection;
import java.sql.SQLException;

public interface DataSource {
	Connection getConnection() throws SQLException;
	boolean init();
	void destroy();


	boolean requestExists(int id) throws Exception;

	RequestCollection getAllOpenRequests(boolean includeElevated) throws Exception;
	RequestCollection getOpenRequests(int page, boolean includeElevated) throws Exception;
	RequestCollection getOpenRequests(Player player) throws Exception;
	Request getRequest(int id) throws Exception;
	Request elevateRequest(Request request, boolean elevated) throws Exception;
	Request reopenRequest(Request request) throws Exception;
	Request claim(Request request, Player player) throws Exception;
	Request unclaim(Request request) throws Exception;
	Request closeRequest(Request request, Player mod, String message) throws Exception;
	Request createRequest(Player player, String message) throws Exception;
	int getOpenRequestCount(boolean includeElevated) throws Exception;
	int getOpenRequestCount(Player player) throws Exception;
	RequestCollection getUnseenClosedRequests(Player player) throws Exception;
	RequestCollection markRequestsAsSeen(RequestCollection ids) throws Exception;
}
