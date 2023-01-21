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

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import uk.co.notnull.modreq.*;
import uk.co.notnull.modreq.collections.RequestCollection;

import java.util.Map;

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
            } else if(requests.size() == 1) {
                Request request = requests.get(0);

                Messages.send(player, "player.notification.JOIN", Map.of(
                        "id", Component.text(request.getId()),
                        "view", Messages.getViewButton(request),
                        "link", Messages.getRequestLink(request)));
            } else {
                Messages.send(player, "player.notification.JOIN-MULTIPLE",
                              "count", String.valueOf(requests.size()));
            }

            Util.playSound(player);
        }).exceptionally(e -> {
            e.printStackTrace();
            Messages.send(player, "error.DATABASE-ERROR");
            return null;
        });

        if(Util.isMod(player)) {
            plugin.getRequestRegistry().getCount(RequestQuery.open()).thenAcceptAsync((Integer count) -> {
                if(count == 0) {
                    return;
                }

                Messages.send(player, "mod.notification.JOIN", "count", String.valueOf(count));
                Util.playSound(player);
            }).exceptionally(e -> {
                e.printStackTrace();
                Messages.send(player, "error.DATABASE-ERROR");
                return null;
            });
        }
    }
}
