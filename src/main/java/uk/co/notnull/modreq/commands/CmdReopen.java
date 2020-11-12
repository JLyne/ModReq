package uk.co.notnull.modreq.commands;

import java.util.concurrent.CompletableFuture;

import org.bukkit.entity.Player;
import uk.co.notnull.modreq.Messages;
import uk.co.notnull.modreq.ModReq;
import uk.co.notnull.modreq.Request;

public class CmdReopen {
    private final ModReq plugin;

    public CmdReopen(ModReq plugin) {
        this.plugin = plugin;
    }

    public void reopenModReq(final Player player, final int id) {
        plugin.getRequestRegistry().get(id).thenComposeAsync((Request request) -> {
            if(request == null) {
                Messages.send(player, "error.ID-ERROR", "%id", String.valueOf(id));
                return CompletableFuture.completedFuture(null);
            }

            if(request.getDone() > 0) {
                return plugin.getRequestRegistry().reopen(request).thenAcceptAsync((Request result) -> {
                    Messages.sendToMods("error.REOPEN", "%mod", player.getName(), "%id", String.valueOf(id));
                });
            } else {
                Messages.send(player,"error.NOT-CLOSED");
            }

            return CompletableFuture.completedFuture(null);
        }).exceptionally(e -> {
            e.printStackTrace();
            Messages.send(player, "error.DATABASE-ERROR");
            return null;
        });
    }
}

