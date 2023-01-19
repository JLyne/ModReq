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

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import org.bukkit.entity.Player;
import uk.co.notnull.modreq.*;

public class CmdComment {
    private final ModReq plugin;

    public CmdComment(ModReq plugin) {
        this.plugin = plugin;
    }

    public void addComment(final Player player, final int id, final boolean isPublic, final String message) {
        AtomicReference<Request> request = new AtomicReference<>();
        CompletableFuture<Void> shortcut = new CompletableFuture<>();

        plugin.getRequestRegistry().get(id).thenComposeAsync((Request result) -> {
            request.set(result);

            if(result == null) {
                Messages.send(player, "error.ID-ERROR", "id", String.valueOf(id));
                shortcut.complete(null);
				return new CompletableFuture<>();
            }

            if(result.isClosed() && result.getCreator().equals(player.getUniqueId())) {
                Messages.send(player, "error.REOPEN-INSTEAD", "id", String.valueOf(id),
                              "message", message);
                shortcut.complete(null);
				return new CompletableFuture<>();
            }

            return plugin.getRequestRegistry().addComment(result, player, isPublic, message);
        }).thenAccept((Update update) -> {
            Messages.sendModNotification(
                    isPublic ? NotificationType.COMMENT_ADDED : NotificationType.PRIVATE_COMMENT_ADDED, player,
                    request.get(), "message", message);
        }).applyToEither(shortcut, Function.identity()).exceptionally((e) -> {
            Messages.send(player, "error.DATABASE-ERROR");
            return null;
        });
    }

    public void removeComment(final Player player, final int id, final int commentId) {
        CompletableFuture<Void> shortcut = new CompletableFuture<>();
        AtomicReference<Request> request = new AtomicReference<>();
        AtomicReference<Update> comment = new AtomicReference<>();

        plugin.getRequestRegistry().get(id).thenComposeAsync((Request result) -> {
            request.set(result);

            if(result == null) {
                Messages.send(player, "error.ID-ERROR", "id", String.valueOf(id));
                shortcut.complete(null);
				return new CompletableFuture<>();
            }

             return plugin.getRequestRegistry().getAllUpdates(result, true);
        }).thenComposeAsync((List<Update> updates) -> {
            comment.set(updates.get(commentId));

            if(comment.get() == null) {
                Messages.send(player, "error.COMMENT-DOES-NOT-EXIST");
                shortcut.complete(null);
				return new CompletableFuture<>();
            }

            if(!comment.get().getCreator().equals(player.getUniqueId()) && !plugin.isAdmin(player)) {
                Messages.send(player, "error.COMMENT-OTHER");
                shortcut.complete(null);
				return new CompletableFuture<>();
            }

            return plugin.getRequestRegistry().removeComment(comment.get());
        }).thenAcceptAsync((Boolean result) -> {
            Messages.sendModNotification(NotificationType.COMMENT_REMOVED, player, request.get(),
                                         "message", comment.get().getMessage());
        }).applyToEither(shortcut, Function.identity()).exceptionally((e) -> {
            Messages.send(player, "error.DATABASE-ERROR");
            return null;
        });
    }
}

