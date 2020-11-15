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
            Messages.send(player, "player.REQUEST-FILED");
            Messages.sendModNotification(NotificationType.CREATED, player, request);
            plugin.playModSound();
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

        RequestQuery query = new RequestQuery().creator(player.getUniqueId());

		plugin.getRequestRegistry().get(query, page).thenAcceptAsync(requests -> {
			Messages.sendList(player, requests, "/mr me %page%");
		}).exceptionally((e) -> {
			Messages.send(player, "error.DATABASE-ERROR");
			e.printStackTrace();
			return null;
		});
    }
}

