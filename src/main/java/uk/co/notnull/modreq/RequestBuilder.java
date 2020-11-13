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
		BuildStep response(Response owner);
		BuildStep notes(List<Note> notes);
		Request build();
	}

	private static class Steps implements IDStep, CreatorStep, MessageStep, DateStep, LocationStep, BuildStep {
		private int id;
		private UUID creator;
		private String message;
		private Location location;
		private Date createTime;
		private boolean elevated = false;
		private UUID owner = null;
		private Response response = null;
		private List<Note> notes = Collections.emptyList();

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

		public BuildStep response(Response response) {
			this.response = response;
			return this;
		}

		public BuildStep notes(List<Note> notes) {
			this.notes = notes;
			return this;
		}

		public Request build() {
			return new Request(id, creator, message, createTime, location, owner, elevated, response, notes);
		}
	}
}