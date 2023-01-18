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

package uk.co.notnull.modreq.listener;

import de.themoep.minedown.adventure.MineDown;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import uk.co.notnull.modreq.*;
import uk.co.notnull.modreq.collections.RequestCollection;

public class PlayerJoin {
    private final ModReq plugin;

    public PlayerJoin(ModReq plugin) {
        this.plugin = plugin;
    }

    public void onPlayerJoin(final Player player) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> this.joinChecks(player), 100L);
    }

    public void joinChecks(Player player) {
        plugin.getRequestRegistry().getUnseen(player).thenAcceptAsync((RequestCollection requests) -> {
            if(requests.isEmpty()) {
                return;
            }

            Messages.send(player, "player.notification.JOIN", "count", String.valueOf(requests.size()));
            plugin.playSound(player);
        }).exceptionally(e -> {
            e.printStackTrace();
            Messages.send(player, "error.DATABASE-ERROR");
            return null;
        });

        if(player.hasPermission("modreq.mod") || player.hasPermission("modreq.admin")) {
            plugin.getRequestRegistry().getCount(RequestQuery.open()).thenAcceptAsync((Integer count) -> {
                if(count == 0) {
                    return;
                }

                Messages.send(player, "mod.JOIN", "count", String.valueOf(count));
                plugin.playSound(player);
            }).exceptionally(e -> {
                e.printStackTrace();
                Messages.send(player, "error.DATABASE-ERROR");
                return null;
            });
        }
    }
}
