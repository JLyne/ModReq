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

package uk.co.notnull.modreq.commands;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import uk.co.notnull.modreq.Messages;
import uk.co.notnull.modreq.ModReq;
import uk.co.notnull.modreq.Request;

import java.util.Map;

public class CmdTpid {
    private final ModReq plugin;
    public CmdTpid(ModReq plugin) {
        this.plugin = plugin;
    }

    public void tpToModReq(final Player player, final int id) {
        plugin.getRequestRegistry().get(id).thenAcceptAsync((Request request) -> {
            if(request == null) {
                Messages.send(player, "error.ID-ERROR", "id", String.valueOf(id));
                return;
            }

            if(request.getLocation().getWorld() == null) {
                Messages.send(player, "error.TELEPORT-UNLOADED");
                return;
            }

            Bukkit.getScheduler().runTask(plugin, () -> {
                if(player.teleport(request.getLocation())) {
                    Messages.send(player, "confirmation.TELEPORTED", Map.of(
                            "id", Component.text(id),
                            "link", Messages.getRequestLink(request)
                    ));
                } else {
                    Messages.send(player, "error.TELEPORT-ERROR");
                }
            });
        }).exceptionally((e) -> {
            Messages.send(player, "error.DATABASE-ERROR");
            return null;
        });
    }
}

