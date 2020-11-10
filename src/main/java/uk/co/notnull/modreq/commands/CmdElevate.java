package uk.co.notnull.modreq.commands;

import java.util.concurrent.CompletableFuture;

import org.bukkit.entity.Player;
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
                player.sendMessage(plugin.getLanguageFile().getLangString("error.ID-ERROR").replaceAll("%id", "" + id));
                return CompletableFuture.completedFuture(null);
            }

            int done = request.getDone();
            int elevated = request.getElevated();

            if(done == 0) {
                return plugin.getRequestRegistry().elevate(id, elevated == 0).thenAcceptAsync((Boolean result) -> {
                    plugin.sendModMsg(plugin.getLanguageFile()
                                              .getLangString("mod.elevate." + (elevated == 0 ? "1" : "2"))
                                              .replaceAll("%id", "" + id)
                                              .replaceAll("%mod", player.getName()));
                });
            } else {
                plugin.sendMsg(player, "error.ALREADY-CLOSED");
                return CompletableFuture.completedFuture(null);
            }
        }).exceptionally(e -> {
            e.printStackTrace();
            plugin.sendMsg(player, "error.DATABASE-ERROR");
            return null;
        });
    }
}