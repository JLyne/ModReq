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

public enum NotificationType {
	CREATED(false),
	CLOSED(true),
	REOPENED(true),
	CLAIMED(false),
	UNCLAIMED(false),
	ELEVATED(false),
	UNELEVATED(false),
	COMMENT_ADDED(true),
	COMMENT_REMOVED(false),
	PRIVATE_COMMENT_ADDED(false);

	private final boolean sendToCreator;

	private NotificationType(boolean sendToCreator) {
		this.sendToCreator = sendToCreator;
	}

	public boolean sendToCreator() {
		return sendToCreator;
	}
}
