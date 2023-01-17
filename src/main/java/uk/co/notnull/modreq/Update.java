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

import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Date;
import java.util.UUID;

public class Update {
    private final int id;
    private final int requestId;
    private final UUID creator;
    private final String message;
    private final Date time;
    private final UpdateType type;
    private final boolean seen;

    public Update(int id, int requestId, UpdateType type, Date time, UUID creator) {
        if(type.isMessageRequired()) {
            throw new RuntimeException("Message required for this UpdateType");
        }

        this.id = id;
        this.requestId = requestId;
        this.type = type;
        this.time = time;
        this.creator = creator;

        message = null;
        seen = true;
    }

    public Update(int id, int requestId, UpdateType type, Date time, UUID creator, @Nullable String message) {
        this.id = id;
        this.requestId = requestId;
        this.type = type;
        this.time = time;
        this.creator = creator;
        this.message = message;
        this.seen = true;
    }

    public Update(int id, int requestId, UpdateType type, Date time, UUID creator, @Nullable String message, boolean seen) {
        this.id = id;
        this.requestId = requestId;
        this.type = type;
        this.time = time;
        this.creator = creator;
        this.message = message;
        this.seen = seen;
    }

    public int getId() {
        return id;
    }

    public int getRequestId() {
        return requestId;
    }

    public UUID getCreator() {
        return creator;
    }

    public String getMessage() {
        return message;
    }

    public Date getTime() {
        return time;
    }

    public UpdateType getType() {
        return type;
    }

    public boolean isSeen() {
        return seen;
    }
}
