package uk.co.notnull.modreq;

import java.util.Date;
import java.util.UUID;

public class Response {
	private final boolean seen;
	private final UUID responder;
	private final String message;
	private final Date time;

	public Response(UUID responder, String message, Date time, boolean seen) {
		this.responder = responder;
		this.message = message;
		this.time = time;
		this.seen = seen;
	}

	public boolean isSeen() {
		return seen;
	}

	public UUID getResponder() {
		return responder;
	}

	public String getMessage() {
		return message;
	}

	public Date getTime() {
		return time;
	}
}
