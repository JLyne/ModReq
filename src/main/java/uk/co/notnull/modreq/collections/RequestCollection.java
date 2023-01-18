/*
 * ModReq
 * Copyright (C) 2023 James Lyne
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

package uk.co.notnull.modreq.collections;

import de.themoep.minedown.adventure.MineDownParser;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import uk.co.notnull.modreq.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("serial")
public class RequestCollection extends PaginatedCollection<Request> {
	RequestCollection(List<Request> requests) {
		super(requests);
	}

	RequestCollection(List<Request> requests, int offset, int total) {
		super(requests, offset, total);
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

	@Override
	public Component toComponent(@NotNull Player context, String paginationCommand) {
		Component result = Component.empty();
		boolean isMod = context.hasPermission("modreq.mod") || context.hasPermission("modreq.admin");

        if(getTotal() == 0 && getPage() == 1) {
			return Messages.get(isMod ? "mod.list.NO-RESULTS" : "player.list.NO-RESULTS");
		} else if (isAfterLastPage()) {
			return Messages.get("error.PAGE-ERROR", "page", "" + getPage());
		}

        String headerKey = isMod ? "mod.list.HEADER" : "player.list.HEADER";

        result = result.append(Messages.get(headerKey,
                                              "count", String.valueOf(getTotal()),
                                              "page", String.valueOf(getPage()),
                                              "allpages", String.valueOf(getTotalPages())));
        result = result.append(Component.newline());

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
					status = Messages.getPlayerString(owner, context);
				} else {
					status = Messages.getString("general.OPEN");
				}

				if(request.isElevated()) {
					status += Messages.getString("general.ELEVATED");
				}
			} else {
				status = Messages.getString("general." + (request.isClosed() ? "CLOSED": "OPEN"));
			}

			if(request.getCreator().equals(context.getUniqueId()) && request.hasUpdates() &&
					request.getLastUpdate().getSeenStatus().isUnread()) {
				status += Messages.getString("general.UNREAD", "id", String.valueOf(request.getId()));
			}

			if(isMod) {
				replacements.put("elevated", request.isElevated() ? Messages.get("general.ELEVATED") : Component.empty());
			}

			Update lastUpdate = request.getLastUpdate();
			if(lastUpdate != null && !lastUpdate.getType().equals(UpdateType.CREATE)) {
				OfflinePlayer actor = Bukkit.getOfflinePlayer(request.getLastUpdate().getCreator());

				replacements.put("last_update", Messages.get(
						isMod ? "mod.list.ITEM-LAST-UPDATE" : "player.list.ITEM-LAST-UPDATE",
						Map.of("update_actor", Messages.getPlayer(actor, context),
							   "update_time", Messages.getFormattedDate(request.getLastUpdateTime()))));
			} else {
				replacements.put("last_update", Component.empty());
			}

			replacements.put("id", Component.text(request.getId()));
			replacements.put("link", Messages.getRequestLink(request));
			replacements.put("status", new MineDownParser().parse(status).build());
			replacements.put("creator", Messages.getPlayer(creator, context));
			replacements.put("date", Messages.getFormattedDate(request.getCreateTime()));
			replacements.put("message", Component.text(request.getMessage()));
			replacements.put("view", Messages.getViewButton(request));

			if(isMod) {
				result = result.append(Messages.get("mod.list.ITEM", replacements));
			} else {
				result = result.append(Messages.get("player.list.ITEM", replacements));
			}
		}

		if(isPaginated()) {
		    Component nextButton;
		    Component prevButton;

		    if(isLastPage()) {
                nextButton = Messages.get("pagination.requests.DISABLED");
            } else {
                nextButton = Messages.get("pagination.requests.NEXT",
                                          "command", paginationCommand.replace("%page%",
                                                                     String.valueOf(getPage() + 1)));
            }

		    if(isFirstPage()) {
                prevButton = Messages.get("pagination.requests.DISABLED");
            } else {
                prevButton = Messages.get("pagination.requests.PREV",
                                          "command", paginationCommand.replace("%page%",
                                                                     String.valueOf(getPage() - 1)));
            }

            result = result.append(Component.newline());
		    result = result.append(Messages.get(isMod ? "mod.list.PAGINATION" : "player.list.PAGINATION", Map.of(
					"count", Component.text(getTotal()),
					"page", Component.text(getPage()),
					"allpages", Component.text(getTotalPages()),
					"next", nextButton,
					"prev", prevButton
			)));
		}

		return result;
	}
}
