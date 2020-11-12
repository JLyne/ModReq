package uk.co.notnull.modreq.commands;

import java.util.concurrent.CompletableFuture;

import org.bukkit.entity.Player;
import uk.co.notnull.modreq.Messages;
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
                Messages.send(player,"error.ID-ERROR",  "%id", String.valueOf(id));
                return CompletableFuture.completedFuture(null);
            }

            if(request.getDone() != 0) {
                Messages.send(player, "error.ALREADY-CLOSED");
                return CompletableFuture.completedFuture(null);
            }

            boolean canClaimOther = player.hasPermission("modreq.admin")
                    || player.hasPermission("modreq.mod.overrideclaimed");

            if(claim) {
                if(!request.getClaimed().isEmpty() && !canClaimOther) {
                    Messages.send(player, "error.ALREADY-CLAIMED");
                    return CompletableFuture.completedFuture(null);
                }

                return plugin.getRequestRegistry().claim(request, player).thenAcceptAsync((Request result) -> {
                    Messages.sendToMods("mod.CLAIM", "%mod", player.getName(), "%id", String.valueOf(id));
                });
            } else if(!request.getClaimed().isEmpty()) {
                if(!request.getClaimed().equals(player.getUniqueId().toString()) && !canClaimOther) {
                    Messages.send(player, "error.OTHER-CLAIMED");
                    return CompletableFuture.completedFuture(null);
                }

                return plugin.getRequestRegistry().unclaim(request).thenAcceptAsync((Request result) -> {
                    Messages.sendToMods("error.UNCLAIM", "%mod", player.getName(), "%id", String.valueOf(id));
                });
            } else {
                Messages.send(player, "error.NOT-CLAIMED");
                return CompletableFuture.completedFuture(null);
            }
        }).exceptionally((e) -> {
            Messages.send(player, "error.DATABASE-ERROR");
            return null;
        });
    }
}

