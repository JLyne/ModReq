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

package uk.co.notnull.modreq.storage;

import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.Nullable;
import uk.co.notnull.modreq.*;
import uk.co.notnull.modreq.collections.RequestCollection;
import uk.co.notnull.modreq.collections.UpdateCollection;

import java.sql.Connection;
import java.sql.SQLException;

public interface DataSource {
	Connection getConnection() throws SQLException;
	boolean init();
	void destroy();

	boolean requestExists(int id) throws Exception;
	RequestCollection getAllRequests(RequestQuery query, boolean includePrivateUpdates) throws Exception;
	RequestCollection getRequests(RequestQuery query, int page, boolean includePrivateUpdates) throws Exception;
	int getRequestCount(RequestQuery query) throws Exception;
	Request getRequest(int id, boolean includePrivateUpdates) throws Exception;
	Request elevateRequest(Request request, Player mod, boolean elevated) throws Exception;
	Request reopenRequest(Request request, Player mod) throws Exception;
	Request claim(Request request, Player player) throws Exception;
	Request unclaim(Request request, Player mod) throws Exception;
	Request closeRequest(Request request, Player mod, String message) throws Exception;
	Request createRequest(Player player, String message) throws Exception;
	Request markRequestAsSeen(Request request) throws Exception;
	Update addCommentToRequest(Request request, Player player, boolean isPublic, String content) throws Exception;
	boolean removeComment(Update update) throws Exception;
	UpdateCollection getAllUpdatesForRequest(Request request, boolean includePrivate) throws Exception;
	UpdateCollection getUpdatesForRequest(Request request, @Nullable Integer page, boolean includePrivate) throws Exception;
}
