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
