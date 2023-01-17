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

import de.themoep.minedown.adventure.MineDownParser;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@SuppressWarnings("serial")
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

	public Component toComponent(@NotNull Player context) {
		boolean isMod = context.hasPermission("modreq.mod") || context.hasPermission("modreq.admin");
		Component result = Component.empty();
		boolean first = true;

		for(Request request: this) {
			if(first) {
				first = false;
			} else {
				result = result.append(Component.newline());
			}

			OfflinePlayer creator = Bukkit.getOfflinePlayer(request.getCreator());
			OfflinePlayer owner = request.isClaimed() ? Bukkit.getOfflinePlayer(request.getOwner()) : null;
			Map<String, Component> replacements = new HashMap<>();

			String status;

			if(isMod) {
				if (request.isClosed()) {
					status = Messages.getString("general.CLOSED");
				} else if (request.isClaimed()) {
					if (owner == null || owner.getName() == null) {
						status = Messages.getString("general.UNKNOWN-PLAYER");
					} else if (owner.isOnline()) {
						status = Messages.getString("general.ONLINE-PLAYER", "player", owner.getName());
					} else {
						status = Messages.getString("general.OFFLINE-PLAYER", "player", owner.getName());
					}
				} else {
					status = Messages.getString("general.OPEN");
				}

				if(request.isElevated()) {
					status += Messages.getString("general.ELEVATED");
				}
			} else {
				status = Messages.getString("general." + (request.isClosed() ? "CLOSED": "OPEN"));
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

			if(isMod) {
				replacements.put("elevated", request.isElevated() ? Messages.get("general.ELEVATED") : Component.empty());
			}

			replacements.put("id", Component.text(request.getId()));
			replacements.put("link", Messages.get("general.REQUEST-LINK", "id", String.valueOf(request.getId())));
			replacements.put("status", new MineDownParser().parse(status).build());
			replacements.put("creator", new MineDownParser().parse(username).build());
			replacements.put("date", Component.text(ModReq.getPlugin().getFormat().format(request.getCreateTime())));
			replacements.put("message", Component.text(request.getMessage()));

			if(isMod) {
				result = result.append(Messages.get("mod.list.ITEM", replacements));
			} else {
				result = result.append(Messages.get("player.list.ITEM-REQUEST", replacements));

				if(request.isClosed()) {
					result = result.append(Component.newline());


					result = result.append(Messages.get("player.list.ITEM-RESPONSE", replacements));
					result = result.append(Component.newline());
				}
			}
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
		return paginated ? (offset / ModReq.getPlugin().getConfiguration().getModreqs_per_page()) + 1 : 1;
	}

	public int getTotalPages() {
		int perPage = ModReq.getPlugin().getConfiguration().getModreqs_per_page();
		return paginated ? total / perPage + ((total % perPage == 0) ? 0 : 1) : 1;
	}

	public boolean isFirstPage() {
		return getPage() == 1;
	}

	public boolean isLastPage() {
		return getPage() == getTotalPages();
	}

	public boolean isAfterLastPage() {
		return paginated && offset >= total;
	}
}
