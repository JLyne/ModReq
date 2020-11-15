package uk.co.notnull.modreq;

import de.themoep.minedown.adventure.MineDownParser;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.*;

public class RequestCollection extends ArrayList<Request> {
	private final boolean paginated;
	private final int offset;
	private final int total;

	RequestCollection(List<Request> requests) {
		super(requests);
		this.paginated = false;
		this.offset = 0;
		this.total = requests.size();
	}

	RequestCollection(List<Request> requests, int offset, int total) {
		super(requests);
		this.paginated = true;
		this.offset = offset;
		this.total = total;
	}

	public static RequestCollectionBuilder builder() {
		return new RequestCollectionBuilder();
	}

	public RequestCollectionBuilder toBuilder() {
		RequestCollectionBuilder builder = new RequestCollectionBuilder().requests(new ArrayList<>(this));

		if(this.isPaginated()) {
			return builder.paginated(offset, total);
		}

		return builder;
	}

	public Component toComponent(Player context) {
		Component result = Component.empty();

		for(Request request: this) {
			OfflinePlayer creator = Bukkit.getOfflinePlayer(request.getCreator());
			OfflinePlayer owner = request.isClaimed() ? Bukkit.getOfflinePlayer(request.getOwner()) : null;
			OfflinePlayer responder = request.getResponder() != null ? Bukkit.getOfflinePlayer(request.getResponder()) : null;
			Map<String, Component> replacements = new HashMap<>();

			String status;

			if(!request.isClaimed()) {
				status = Messages.getString("general.OPEN");
			} else if(owner != null) {
				status = owner.getName();
			} else {
				status = "unknown";
			}

			String username;
			if (creator.getName() != null) {
				if (creator.isOnline()) {
					username = Messages.getString("general.ONLINE-PLAYER","player", creator.getName());
				} else {
					username = Messages.getString("general.OFFLINE-PLAYER", "player", creator.getName());
				}
			} else {
				username = Messages.getString("general.UNKNOWN-PLAYER");
			}

			replacements.put("id", Component.text(request.getId()));
			replacements.put("link", Messages.get("general.REQUEST-LINK", "id", String.valueOf(request.getId())));
			replacements.put("status", new MineDownParser().parse(status).build());
			replacements.put("elevated", request.isElevated() ? Messages.get("general.ELEVATED") : Component.empty());
			replacements.put("notes", request.hasNotes() ? Messages.get("general.NOTES") : Component.empty());
			replacements.put("note_count", Component.text(request.getNotes().size()));
			replacements.put("creator", new MineDownParser().parse(username).build());
			replacements.put("date", Component.text(ModReq.getPlugin().getFormat().format(request.getCreateTime())));
			replacements.put("message", Component.text(request.getMessage()));

			if(context != null && context.hasPermission("modreq.mod") && context.hasPermission("modreq.admin")) {
				result = result.append(Messages.get("mod.list.ITEM", replacements));
			} else {
				result = result.append(Messages.get("player.list.ITEM-REQUEST", replacements));

				if(request.isClosed()) {
					replacements.put("close_time", Component.text(ModReq.getPlugin().getFormat().format(request.getCloseTime())));
					replacements.put("response", Component.text(request.getResponseMessage()));

					result = result.append(Component.newline());

					if(responder != null && responder.getName() != null) {
						replacements.put("responder", Component.text(responder.getName()));
					} else {
						replacements.put("responder", Messages.get("general.UNKNOWN-PLAYER"));
					}

					result = result.append(Component.newline());
					result = result.append(Messages.get("player.list.ITEM-RESPONSE", replacements));
				}
			}

			result = result.append(Component.newline());
		}

		return result;
	}

	public boolean isPaginated() {
		return paginated;
	}

	public int getOffset() {
		return offset;
	}

	public int getTotal() {
		return total;
	}

	public int getPage() {
		return paginated ? (offset / ModReq.getPlugin().getConfiguration().getModreqs_per_page()) : 1;
	}

	public int getTotalPages() {
		int perPage = ModReq.getPlugin().getConfiguration().getModreqs_per_page();
		return paginated ? total / perPage + ((total % perPage == 0) ? 0 : 1) : 1;
	}

	public boolean isAfterLastPage() {
		return paginated && offset >= total;
	}
}
