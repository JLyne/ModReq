package uk.co.notnull.modreq.commands;

import java.util.concurrent.CompletableFuture;

import org.bukkit.entity.Player;
import uk.co.notnull.modreq.ModReq;
import uk.co.notnull.modreq.Request;

public class CmdClaim {
    private final ModReq plugin;

    public CmdClaim(ModReq plugin) {
        this.plugin = plugin;
    }

    public void claimModReq(final Player player, final int id, final boolean claim) {
        plugin.getRequestRegistry().get(id).thenComposeAsync((Request request) -> {
            if(request == null) {
                player.sendMessage(plugin.getLanguageFile().getLangString("error.ID-ERROR")
                                           .replaceAll("%id", "" + id));
                return CompletableFuture.completedFuture(null);
            }

            if(request.getDone() != 0) {
                plugin.sendMsg(player, "error.ALREADY-CLOSED");
                return CompletableFuture.completedFuture(null);
            }

            boolean canClaimOther = player.hasPermission("modreq.admin")
                    || player.hasPermission("modreq.mod.overrideclaimed");

            if(claim) {
                if(!request.getClaimed().isEmpty() && !canClaimOther) {
                    plugin.sendMsg(player, "error.ALREADY-CLAIMED");
                    return CompletableFuture.completedFuture(null);
                }

                return plugin.getRequestRegistry().claim(id, player).thenAcceptAsync((Boolean result) -> {
                    plugin.sendModMsg(plugin.getLanguageFile()
                                              .getLangString("mod.CLAIM")
                                              .replaceAll("%mod", player.getName())
                                              .replaceAll("%id", "" + id));
                });
            } else if(!request.getClaimed().isEmpty()) {
                if(!request.getClaimed().equals(player.getUniqueId().toString()) && !canClaimOther) {
                    plugin.sendMsg(player, "error.OTHER-CLAIMED");
                    return CompletableFuture.completedFuture(null);
                }

                return plugin.getRequestRegistry().unclaim(id).thenAcceptAsync((Boolean result) -> {
                    plugin.sendModMsg(plugin.getLanguageFile()
                                              .getLangString("mod.UNCLAIM")
                                              .replaceAll("%mod", player.getName())
                                              .replaceAll("%id", "" + id));
                });
            } else {
                plugin.sendMsg(player, "error.NOT-CLAIMED");
                return CompletableFuture.completedFuture(null);
            }
        }).exceptionally((e) -> {
            ModReq.getPlugin().sendMsg(player, "error.DATABASE-ERROR");
            return null;
        });
    }
}

