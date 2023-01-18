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

package uk.co.notnull.modreq;

public enum UpdateType {
	PRIVATE_COMMENT(false, true),
	PUBLIC_COMMENT(true, true),
	CREATE(true, false),
	CLOSE(true, false),
	REOPEN(true, false),
	ELEVATE(false, false),
	UNELEVATE(false, false),
	CLAIM(false, false),
	UNCLAIM(false, false);

	private final boolean isPublic;
	private final boolean isMessageRequired;

	UpdateType(boolean isPublic, boolean isMessageRequired) {
		this.isPublic = isPublic;
		this.isMessageRequired = isMessageRequired;
	}

	public boolean isPublic() {
		return isPublic;
	}

	public boolean isMessageRequired() {
		return isMessageRequired;
	}
}
