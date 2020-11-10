package uk.co.notnull.modreq.commands;

import java.util.concurrent.CompletableFuture;

import org.bukkit.entity.Player;
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
                player.sendMessage(plugin.getLanguageFile().getLangString("error.ID-ERROR").replaceAll("%id", "" + id));
                return CompletableFuture.completedFuture(null);
            }

            if(request.getDone() > 0) {
                return plugin.getRequestRegistry().reopen(id).thenAcceptAsync((Boolean result) -> {
                     plugin.sendModMsg(plugin.getLanguageFile().getLangString("mod.REOPEN")
                                              .replaceAll("%mod", player.getName())
                                              .replaceAll("%id", "" + id));
                });
            } else {
                plugin.sendMsg(player, "error.NOT-CLOSED");
            }

            return CompletableFuture.completedFuture(null);
        }).exceptionally(e -> {
            e.printStackTrace();
            plugin.sendMsg(player, "error.DATABASE-ERROR");
            return null;
        });
    }
}

