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

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import uk.co.notnull.modreq.Messages;
import uk.co.notnull.modreq.Update;
import uk.co.notnull.modreq.UpdateType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SuppressWarnings("serial")
public class UpdateCollection extends PaginatedCollection<Update>  {
	UpdateCollection(List<Update> updates) {
		super(updates);
	}

	UpdateCollection(List<Update> updates, int offset, int total) {
		super(updates, offset, total);
	}

	public static UpdateCollectionBuilder builder() {
		return new UpdateCollectionBuilder();
	}

	public UpdateCollectionBuilder toBuilder() {
		UpdateCollectionBuilder builder = new UpdateCollectionBuilder().updates(new ArrayList<>(this));

		if(this.isPaginated()) {
			return builder.paginated(offset, total);
		}

		return builder;
	}

	@Override
	public Component toComponent(@NotNull Player context, String paginationCommand) {
		if(isEmpty() || get(0).getType().equals(UpdateType.CREATE)) {
			return Component.empty();
		}

		Component result = Component.empty();
		Component pagination = getPagination(context, paginationCommand);
		boolean isMod = context.hasPermission("modreq.mod") || context.hasPermission("modreq.admin");

		result = result.append(Messages.get(isMod ? "mod.activity.HEADER" : "player.activity.HEADER", Map.of(
				"count", Component.text(getTotal()),
				"page", Component.text(getPage()),
				"allpages", Component.text(getTotalPages()))));

		if(isPaginated() && !(isFirstPage() && isLastPage())) {
			result = result.append(Component.newline()).append(pagination);
		}

        for(int i = 0; i < size(); i++) {
            Update update = get(i);
            OfflinePlayer actor = Bukkit.getOfflinePlayer(update.getCreator());

            Map<String, ?> updateReplacements = Map.of(
                    "id", Component.text(offset + i + 1),
                    "actor", Messages.getPlayer(actor, context),
                    "date", Messages.getFormattedDate(update.getTime()),
                    "message", update.hasMessage() ? Component.text(update.getMessage()) : Component.empty());

            result = result.append(Component.newline());
            result = result.append(
                    Messages.get("mod.activity." + update.getType().toString(), updateReplacements));
        }

		if(isPaginated() && !(isFirstPage() && isLastPage())) {
			result = result.append(Component.newline()).append(pagination);
		}

		return result;
	}

	private Component getPagination(Player context, String command) {
		if (!isPaginated() || (isFirstPage() && isLastPage())) {
			return Component.empty();
		}

		Component nextButton;
		Component prevButton;
		boolean isMod = context.hasPermission("modreq.mod") || context.hasPermission("modreq.admin");

		if (isLastPage()) {
			nextButton = Messages.get("pagination.activity.DISABLED");
		} else {
			nextButton = Messages.get("pagination.activity.NEXT",
									  "command", command.replace("%page%", String.valueOf(getPage() + 1)));
		}

		if (isFirstPage()) {
			prevButton = Messages.get("pagination.activity.DISABLED");
		} else {
			prevButton = Messages.get("pagination.activity.PREV",
									  "command", command.replace("%page%",
																		   String.valueOf(getPage() - 1)));
		}

		return Messages.get(isMod ? "mod.activity.PAGINATION" : "player.activity.PAGINATION", Map.of(
							"count", Component.text(getTotal()),
							"page", Component.text(getPage()),
							"allpages", Component.text(getTotalPages()),
							"next", nextButton,
							"prev", prevButton
					));
	}
}
