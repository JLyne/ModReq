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

import java.util.UUID;

public class Note {
    private final int id;
    private final int requestId;
    private final UUID creator;
    private final String message;

    public Note(int id, int requestId, UUID creator, String message) {
        this.id = id;
        this.requestId = requestId;
        this.creator = creator;
        this.message = message;
    }

    public int getId() {
        return this.id;
    }

    public int getRequestId() {
        return this.requestId;
    }

    public UUID getCreator() {
        return this.creator;
    }

    public String getMessage() {
        return this.message;
    }
}
