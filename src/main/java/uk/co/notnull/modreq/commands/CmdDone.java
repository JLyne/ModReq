package uk.co.notnull.modreq.commands;

import java.util.concurrent.CompletableFuture;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import uk.co.notnull.modreq.ModReq;
import uk.co.notnull.modreq.Request;

public class CmdDone {
    private final ModReq plugin;

    public CmdDone(ModReq plugin) {
        this.plugin = plugin;
    }

    public void doneModReq(final Player player, final int id, String message) {
        plugin.getRequestRegistry().get(id).thenComposeAsync((Request request) -> {
            if(request == null) {
                player.sendMessage(plugin.getLanguageFile().getLangString("error.ID-ERROR")
                                           .replaceAll("%id", "" + id));
                return CompletableFuture.completedFuture(null);
            }

            if(request.isClosed()) {
                plugin.sendMsg(player, "error.ALREADY-CLOSED");
                return CompletableFuture.completedFuture(null);
            }

            boolean canClaimOther = player.hasPermission("modreq.admin")
                    || player.hasPermission("modreq.mod.overrideclaimed");

            if(!request.isClaimedBy(player.getUniqueId()) && !canClaimOther) {
                plugin.sendMsg(player, "error.OTHER-CLAIMED");
                return CompletableFuture.completedFuture(null);
            }

            return plugin.getRequestRegistry().close(request, player, message).thenAcceptAsync((Request result) -> {
                Player creator = Bukkit.getPlayer(result.getCreator());

                if(creator != null) {
                    creator.sendMessage(plugin.getLanguageFile().getLangString("player.DONE")
                                                .replaceAll("%mod", player.getName())
                                                .replaceAll("%id", "" + id));
                    creator.sendMessage(plugin.getLanguageFile().getLangString("general.DONE-MESSAGE")
                                                .replaceAll("%msg", message));
                    plugin.playSound(creator);
                }

                plugin.sendModMsg(plugin.getLanguageFile().getLangString("mod.DONE")
                                          .replaceAll("%id", "" + id)
                                          .replaceAll("%mod", player.getName()));
                plugin.sendModMsg(plugin.getLanguageFile().getLangString("general.DONE-MESSAGE")
                                          .replaceAll("%msg", message));
            });
        }).exceptionally((e) -> {
            ModReq.getPlugin().sendMsg(player, "error.DATABASE-ERROR");
            return null;
        });
    }
}
