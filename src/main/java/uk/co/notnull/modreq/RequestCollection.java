package uk.co.notnull.modreq;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class RequestCollection extends ArrayList<Request> {

	public Component toComponent(Player context) {
		Component result = Component.empty();

		for(Request request: this) {
			OfflinePlayer creator = Bukkit.getOfflinePlayer(request.getCreator());
			OfflinePlayer owner = request.isClaimed() ? Bukkit.getOfflinePlayer(request.getOwner()) : null;
			OfflinePlayer responder = request.getResponder() != null ? Bukkit.getOfflinePlayer(request.getResponder()) : null;

			String status;

			if(!request.isClaimed()) {
				status = Messages.getString("general.OPEN");
			} else if(owner != null) {
				status = owner.getName();
			} else {
				status = "unknown";
			}

			if(request.isElevated()) {
				status += Messages.getString("general.ELEVATED");
			}

			//TODO: Notes

			if(request.hasNotes()) {
				status = status + " " + Messages.getString("general.NOTES");
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

			String timestamp = ModReq.getPlugin().getFormat().format(request.getCreateTime());

			if(context != null && context.hasPermission("modreq.mod") && context.hasPermission("modreq.admin")) {
				result = result.append(Messages.get("mod.check.2",
										   "id", String.valueOf(request.getId()),
										   "status", status, "date", timestamp, "player", username));
				result = result.append(Component.newline());
				result = result.append(Messages.get("mod.check.3", "msg", request.getMessage()));
			} else {
				result = result.append(Messages.get("player.check.2",
										   "id", String.valueOf(request.getId()),
										   "status", status, "date", timestamp));
				result = result.append(Component.newline());
				result = result.append(Messages.get("player.check.3", "msg", request.getMessage()));

				if(request.isClosed()) {
					String closeTimestamp = ModReq.getPlugin().getFormat().format(request.getCloseTime());

					result = result.append(Component.newline());

					if(responder != null && responder.getName() != null) {
						result = result.append(Messages.get("player.check.4", "mod", responder.getName(),
												   "date", closeTimestamp));
					} else {
						result = result.append(Messages.get("player.check.4",
												   "mod", Messages.getString("general.UNKNOWN-PLAYER"),
												   "date", closeTimestamp));
					}

					result = result.append(Component.newline());
					result = result.append(Messages.get("player.check.5", "msg", request.getMessage()));
				}
			}

			result = result.append(Component.newline());
		}

		return result;
	}
}
