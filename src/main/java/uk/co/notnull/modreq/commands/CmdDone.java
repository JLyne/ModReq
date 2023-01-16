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

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import uk.co.notnull.modreq.Messages;
import uk.co.notnull.modreq.ModReq;
import uk.co.notnull.modreq.NotificationType;
import uk.co.notnull.modreq.Request;

public class CmdDone {
    private final ModReq plugin;

    public CmdDone(ModReq plugin) {
        this.plugin = plugin;
    }

    public void doneModReq(final Player player, final int id, String message) {
        CompletableFuture<Void> shortcut = new CompletableFuture<>();

        plugin.getRequestRegistry().get(id).thenComposeAsync((Request request) -> {
            if(request == null) {
                Messages.send(player, "error.ID-ERROR", "id", String.valueOf(id));
                shortcut.complete(null);
				return new CompletableFuture<>();
            }

            if(request.isClosed()) {
                Messages.send(player, "error.ALREADY-CLOSED");
                shortcut.complete(null);
				return new CompletableFuture<>();
            }

            if(!player.getUniqueId().equals(request.getCreator())) {
                return plugin.getRequestRegistry().close(request, player, message);
            } else if(!player.hasPermission("modreq.mod")) {
                Messages.send(player, "error.NO-PERMISSION");
                shortcut.complete(null);
                return new CompletableFuture<>();
            }

            boolean canClaimOther = player.hasPermission("modreq.admin")
                    || player.hasPermission("modreq.mod.overrideclaimed");

            if(request.isClaimed() && !request.isClaimedBy(player.getUniqueId()) && !canClaimOther) {
                Messages.send(player, "error.OTHER-CLAIMED");
                shortcut.complete(null);
				return new CompletableFuture<>();
            }

            return plugin.getRequestRegistry().close(request, player, message);
        }).thenAcceptAsync((Request result) -> {
            Player creator = Bukkit.getPlayer(result.getCreator());

            if(creator != null) {
                plugin.playSound(creator);
            }

            Messages.sendModNotification(NotificationType.CLOSED, player, result, "message", message);
        }).applyToEither(shortcut, Function.identity()).exceptionally((e) -> {
            Messages.send(player, "error.DATABASE-ERROR");
            return null;
        });
    }
}
