package uk.co.notnull.modreq.commands;

import java.util.concurrent.CompletableFuture;

import org.bukkit.entity.Player;
import uk.co.notnull.modreq.Messages;
import uk.co.notnull.modreq.ModReq;
import uk.co.notnull.modreq.Request;

public class CmdElevate {
    private final ModReq plugin;

    public CmdElevate(ModReq plugin) {
        this.plugin = plugin;
    }

    public void elevateModReq(final Player player, final int id) {
        plugin.getRequestRegistry().get(id).thenComposeAsync((Request request) -> {
            if(request == null) {
                Messages.send(player, "error.ID-ERROR", "id", String.valueOf(id));
                return CompletableFuture.completedFuture(null);
            }

            if(!request.isClosed()) {
                return plugin.getRequestRegistry().elevate(request, !request.isElevated()).thenAcceptAsync((Request result) -> {
                    String message = "mod.elevate." + (request.isElevated() ? "1" : "2");
                    Messages.sendToMods(message, "mod", player.getName(), "id", String.valueOf(id));
                });
            } else {
                Messages.send(player, "error.ALREADY-CLOSED");
                return CompletableFuture.completedFuture(null);
            }
        }).exceptionally(e -> {
            e.printStackTrace();
            Messages.send(player, "error.DATABASE-ERROR");
            return null;
        });
    }
}