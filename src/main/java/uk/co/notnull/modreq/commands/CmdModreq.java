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

        plugin.getRequestRegistry().getOpenCount(player).thenComposeAsync((Integer count) -> {
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

		plugin.getRequestRegistry().getOpen(player, page).thenAcceptAsync(requests -> {
			sendList(player, requests);
		}).exceptionally((e) -> {
			Messages.send(player, "error.DATABASE-ERROR");
			e.printStackTrace();
			return null;
		});
    }

    private void sendList(Player player, RequestCollection requests) {
		if(requests.isAfterLastPage()) {
			Messages.send(player, "error.PAGE-ERROR", "page", "" + requests.getPage());
		} else if (requests.isEmpty()) {
			Messages.send(player, "player.check.NO-MODREQS");
		}

		Messages.send(player, "player.list.HEADER", "count", String.valueOf(requests.getTotal()));
		Messages.send(player, requests.toComponent(player));

		if(requests.isPaginated()) {
			Messages.send(player, "player.list.FOOTER",
						  "page", String.valueOf(requests.getPage()),
						  "allpages", String.valueOf(requests.getTotalPages()));
		}
	}
}

