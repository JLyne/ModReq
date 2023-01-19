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
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class Request {
    private final Location location;
    private final int id;
    private final boolean elevated;
    private final RequestStatus status;
    private final Date createTime;
    private final UUID creator;
    private final String message;
    private final UUID owner;
    private final Update lastUpdate;

    Request(int id, UUID creator, RequestStatus status, String message, Date createTime, Location location, UUID owner, boolean elevated, Update lastUpdate) {
        Objects.requireNonNull(creator);
        Objects.requireNonNull(message);
        Objects.requireNonNull(createTime);
        Objects.requireNonNull(location);
        this.id = id;
        this.creator = creator;
        this.status = status;
        this.message = message;
        this.createTime = createTime;
        this.owner = owner;
        this.lastUpdate = lastUpdate;
        this.elevated = elevated;
        this.location = location;
    }

    public static RequestBuilder.IDStep builder() {
        return RequestBuilder.builder();
    }

    public int getId() {
        return this.id;
    }

    public String getMessage() {
        return message;
    }

    public Location getLocation() {
        return location;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public Date getLastUpdateTime() {
        return hasUpdates() ? lastUpdate.getTime() : null;
    }

    public UUID getCreator() {
        return creator;
    }

    public UUID getOwner() {
        return owner;
    }

    public Update getLastUpdate() {
        return lastUpdate;
    }

    public boolean hasUpdates() {
        return lastUpdate != null;
    }

    public boolean isElevated() { return this.elevated; }

    public boolean isClosed() { return status.equals(RequestStatus.CLOSED); }

    public boolean isClaimed() { return owner != null; }

    public boolean isClaimedBy(UUID uuid) { return uuid.equals(owner); }

    public boolean isCreatorOnline() {
        Player player = Bukkit.getPlayer(creator);
        return player != null && player.isOnline();
    }

    public Component toComponent(@NotNull Player context) {
        boolean isMod = context.hasPermission("modreq.mod") || context.hasPermission("modreq.admin");
        Component result = Component.empty();

        OfflinePlayer creator = Bukkit.getOfflinePlayer(getCreator());
        OfflinePlayer owner = isClaimed() ? Bukkit.getOfflinePlayer(getOwner()) : null;
        Map<String, Component> replacements = new HashMap<>();

        String status;
        Component location;

        if (isMod) {
            if (isClosed()) {
                status = Messages.getString("general.CLOSED");
            } else if(isClaimed()) {
                status = Messages.getPlayerString(owner, context);
            } else {
                status = Messages.getString("general.OPEN");
            }

            if(isElevated()) {
				status += Messages.getString("general.ELEVATED");
			}
        } else {
            status = Messages.getString("general." + (isClosed() ? "CLOSED": "OPEN"));
        }

        String world = getLocation().getWorld() != null ? getLocation().getWorld().getName() : Messages.getString("general.UNKNOWN-WORLD");

        if(isMod) {
            replacements.put("elevated", isElevated() ? Messages.get("general.ELEVATED") : Component.empty());

            if(getLocation().getWorld() != null) {
                location = Messages.get("mod.info.LOCATION",
                                        "id",  String.valueOf(id),
                                        "world", world,
                                        "x", String.valueOf(getLocation().getBlockX()),
                                        "y", String.valueOf(getLocation().getBlockY()),
                                        "z", String.valueOf(getLocation().getBlockZ()));
            } else {
                location = Messages.get("mod.info.UNKNOWN-LOCATION");
            }
        } else {
            if(getLocation().getWorld() != null) {
                location = Messages.get("player.info.LOCATION",
                                        "id",  String.valueOf(id),
                                        "world", world,
                                        "x", String.valueOf(getLocation().getBlockX()),
                                        "y", String.valueOf(getLocation().getBlockY()),
                                        "z", String.valueOf(getLocation().getBlockZ()));
            } else {
                location = Messages.get("player.info.UNKNOWN-LOCATION");
            }
        }

        replacements.put("id", Component.text(getId()));
        replacements.put("status", new MineDownParser().parse(status).build());
        replacements.put("creator", Messages.getPlayer(creator, context));
        replacements.put("date", Component.text(ModReq.getPlugin().getFormat().format(getCreateTime())));
        replacements.put("message", Component.text(getMessage()));
        replacements.put("location", location);
        replacements.put("world", Component.text(world));
        replacements.put("x", Component.text(getLocation().getBlockX()));
        replacements.put("y", Component.text(getLocation().getBlockY()));
        replacements.put("z", Component.text(getLocation().getBlockZ()));

        result = result.append(Messages.get(isMod ? "mod.info.HEADER" : "player.info.HEADER", replacements));
        result = result.append(Component.newline());
        result = result.append(Messages.get(isMod ? "mod.info.REQUEST" : "player.info.REQUEST", replacements));

        return result;
    }

    public Component getActions(@NotNull Player context) {
        Component actions = Component.newline();
        boolean isMod = context.hasPermission("modreq.mod") || context.hasPermission("modreq.admin");

        if (isClosed() && isMod) {
            actions = actions.append(Messages.get("action.OPEN", "id", String.valueOf(id)));
            actions = actions.append(Component.space());
        } else if(!isClosed()) {
            actions = actions.append(Messages.get("action.CLOSE", "id", String.valueOf(id)));
            actions = actions.append(Component.space());
        }

        if(isMod) {
            actions = actions.append(Messages.get("action.TELEPORT", "id", String.valueOf(id)));
            actions = actions.append(Component.space());

            if (!isClosed()) {
                if (isClaimedBy(context.getUniqueId())) {
                    actions = actions.append(Messages.get("action.UNCLAIM", "id", String.valueOf(id)));
                    actions = actions.append(Component.space());
                } else if (!isClaimed()) {
                    actions = actions.append(Messages.get("action.CLAIM", "id", String.valueOf(id)));
                    actions = actions.append(Component.space());
                }

                if (isElevated()) {
                    actions = actions.append(Messages.get("action.UNELEVATE", "id", String.valueOf(id)));
                } else {
                    actions = actions.append(Messages.get("action.ELEVATE", "id", String.valueOf(id)));
                }

                actions = actions.append(Component.space());
            }
        }

        actions = actions.append(Messages.get("action.COMMENT", "id", String.valueOf(id)));
        actions = actions.append(Component.space());

        if(isMod) {
            actions = actions.append(Messages.get("action.PRIVATE_COMMENT", "id", String.valueOf(id)));
        }

        return actions;
    }
}

