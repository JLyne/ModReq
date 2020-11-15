package uk.co.notnull.modreq.storage;

import org.bukkit.entity.Player;
import uk.co.notnull.modreq.Note;
import uk.co.notnull.modreq.Request;
import uk.co.notnull.modreq.RequestCollection;
import uk.co.notnull.modreq.RequestQuery;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface DataSource {
	Connection getConnection() throws SQLException;
	boolean init();
	void destroy();

	boolean requestExists(int id) throws Exception;

	RequestCollection getAllRequests(RequestQuery query) throws Exception;
	RequestCollection getRequests(RequestQuery query, int page) throws Exception;
	int getRequestCount(RequestQuery query) throws Exception;
	Request getRequest(int id) throws Exception;
	Request elevateRequest(Request request, boolean elevated) throws Exception;
	Request reopenRequest(Request request) throws Exception;
	Request claim(Request request, Player player) throws Exception;
	Request unclaim(Request request) throws Exception;
	Request closeRequest(Request request, Player mod, String message) throws Exception;
	Request createRequest(Player player, String message) throws Exception;
	RequestCollection markRequestsAsSeen(RequestCollection ids) throws Exception;
	Note addNoteToRequest(Request request, Player player, String note) throws Exception;
	boolean removeNote(Note note) throws Exception;
	List<Note> getNotesForRequest(Request request) throws Exception;
}
