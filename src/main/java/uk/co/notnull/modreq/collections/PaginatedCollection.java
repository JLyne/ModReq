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
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import uk.co.notnull.modreq.ModReq;

import java.util.ArrayList;
import java.util.List;

abstract class PaginatedCollection<T> extends ArrayList<T> {
	protected final boolean paginated;
	protected final int offset;
	protected final int total;

	PaginatedCollection(List<T> items) {
		super(items);
		this.paginated = false;
		this.total = items.size();
		this.offset = 0;
	}

	PaginatedCollection(List<T> items, int offset, int total) {
		super(items);
		this.paginated = true;
		this.offset = offset;
		this.total = total;
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

	public Component toComponent(@NotNull Player context, String paginationCommand) {
		return Component.empty();
	}
}
