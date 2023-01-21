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

import org.bukkit.entity.Player;
import uk.co.notnull.modreq.Messages;
import uk.co.notnull.modreq.ModReq;
import uk.co.notnull.modreq.NotificationType;
import uk.co.notnull.modreq.Request;

public class CmdElevate {
    private final ModReq plugin;

    public CmdElevate(ModReq plugin) {
        this.plugin = plugin;
    }

    public void elevateModReq(final Player player, final int id) {
        CompletableFuture<Void> shortcut = new CompletableFuture<>();

        plugin.getRequestRegistry().get(id).thenComposeAsync((Request request) -> {
            if(request == null) {
                Messages.send(player, "error.ID-ERROR", "id", String.valueOf(id));
                shortcut.complete(null);
				return new CompletableFuture<>();
            }

            if(!request.isClosed()) {
                return plugin.getRequestRegistry().elevate(request, player, !request.isElevated());
            } else {
                Messages.send(player, "error.ALREADY-CLOSED");
                shortcut.complete(null);
				return new CompletableFuture<>();
            }
        }).thenAcceptAsync((Request result) -> {
            NotificationType type = result.isElevated() ? NotificationType.ELEVATED : NotificationType.UNELEVATED;
            Messages.sendNotification(type, player, result);
        }).applyToEither(shortcut, Function.identity()).exceptionally(e -> {
            e.printStackTrace();
            Messages.send(player, "error.DATABASE-ERROR");
            return null;
        });
    }
}