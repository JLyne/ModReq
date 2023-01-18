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

import org.bukkit.Location;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class RequestBuilder {
	private RequestBuilder() {
	}

	public static IDStep builder() {
		return new Steps();
	}

	public interface IDStep {
		CreatorStep id(int id);
	}

	public interface CreatorStep {
		MessageStep creator(@NonNull UUID creator);
	}

	public interface MessageStep {
		DateStep message(@NonNull String meat);
	}

	public interface DateStep {
		LocationStep createdAt(@NonNull Date date);
	}

	public interface LocationStep {
		BuildStep location(@NonNull Location location);
	}

	public interface BuildStep {
		BuildStep claimedBy(UUID owner);
		BuildStep elevated(boolean elevated);
		BuildStep status(RequestStatus status);
		BuildStep lastUpdate(Update lastUpdate);
		Request build();
	}

	private static class Steps implements IDStep, CreatorStep, MessageStep, DateStep, LocationStep, BuildStep {
		private int id;
		private UUID creator;
		private String message;
		private Location location;
		private Date createTime;
		private boolean elevated = false;
		private RequestStatus status = RequestStatus.OPEN;
		private UUID owner = null;
		private Update lastUpdate = null;

		public CreatorStep id(int id) {
			this.id = id;
			return this;
		}

		public MessageStep creator(@NonNull UUID creator) {
			this.creator = creator;
			return this;
		}

		public DateStep message(@NonNull String message) {
			this.message = message;
			return this;
		}

		public LocationStep createdAt(@NonNull Date createTime) {
			this.createTime = createTime;
			return this;
		}

		public BuildStep location(@NonNull Location location) {
			this.location = location;
			return this;
		}

		public BuildStep claimedBy(UUID owner) {
			this.owner = owner;
			return this;
		}

		public BuildStep elevated(boolean elevated) {
			this.elevated = elevated;
			return this;
		}

		public BuildStep status(RequestStatus status) {
			this.status = status;
			return this;
		}

		public BuildStep lastUpdate(Update lastUpdate) {
			this.lastUpdate = lastUpdate;
			return this;
		}

		public Request build() {
			return new Request(id, creator, status, message, createTime, location, owner, elevated, lastUpdate);
		}
	}
}