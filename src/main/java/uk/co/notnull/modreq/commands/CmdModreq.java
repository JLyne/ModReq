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
import uk.co.notnull.modreq.*;

public class CmdModreq {
    private final ModReq plugin;

    public CmdModreq(ModReq plugin) {
        this.plugin = plugin;
    }

    public void modreq(final Player player, final String message) {
        CompletableFuture<Void> shortcut = new CompletableFuture<>();

        RequestQuery query = RequestQuery.open().creator(player.getUniqueId());

        plugin.getRequestRegistry().getCount(query).thenComposeAsync((Integer count) -> {
            if(count >= plugin.getConfiguration().getMax_open_modreqs()) {

                Messages.send(player, "error.MAX-OPEN-MODREQS", "max",
                              String.valueOf(plugin.getConfiguration().getMax_open_modreqs()));

                shortcut.complete(null);
                return new CompletableFuture<>();
            }

            return plugin.getRequestRegistry().create(player, message);
        }).thenAcceptAsync((Request request) -> {
            Messages.sendModNotification(NotificationType.CREATED, player, request);
            Util.playModSound();
        }).applyToEither(shortcut, Function.identity()).exceptionally((e) -> {
            Messages.send(player, "error.DATABASE-ERROR");
            return null;
        });
    }

    public void checkPlayerModReqs(final Player player, final int page) {
        if(page < 1) {
			Messages.send(player, "error.NUMBER-ERROR", "id", String.valueOf(page));
			return;
		}

		boolean isMod = Util.isMod(player);
        RequestQuery query = new RequestQuery().creator(player.getUniqueId());

		plugin.getRequestRegistry().get(query, page, isMod).thenAcceptAsync(requests -> {
			player.sendMessage(requests.toComponent(player, "/mr me %page%"));
		}).exceptionally((e) -> {
			Messages.send(player, "error.DATABASE-ERROR");
			e.printStackTrace();
			return null;
		});
    }
}

