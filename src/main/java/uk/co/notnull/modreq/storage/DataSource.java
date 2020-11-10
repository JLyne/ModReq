package uk.co.notnull.modreq.storage;

import uk.co.notnull.modreq.Request;

import java.sql.Connection;
import java.sql.SQLException;

public interface DataSource {
	Connection getConnection() throws SQLException;
	boolean init();
	void destroy();
	boolean requestExists(int id) throws Exception;
	Request getRequest(int id) throws Exception;
	boolean elevateRequest(int id) throws Exception;
	boolean reopenRequest(int id) throws Exception;
}
