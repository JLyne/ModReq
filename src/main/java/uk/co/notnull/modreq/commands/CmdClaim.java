package uk.co.notnull.modreq.commands;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import org.bukkit.entity.Player;
import uk.co.notnull.modreq.Messages;
import uk.co.notnull.modreq.ModReq;
import uk.co.notnull.modreq.NotificationType;
import uk.co.notnull.modreq.Request;

public class CmdClaim {
    private final ModReq plugin;

    public CmdClaim(ModReq plugin) {
        this.plugin = plugin;
    }

    public void claimModReq(final Player player, final int id, final boolean claim) {
        CompletableFuture<Void> shortcut = new CompletableFuture<>();

        plugin.getRequestRegistry().get(id).thenComposeAsync((Request request) -> {
            if(request == null) {
                Messages.send(player,"error.ID-ERROR",  "id", String.valueOf(id));
                shortcut.complete(null);
				return new CompletableFuture<>();
            }

            if(request.isClosed()) {
                Messages.send(player, "error.ALREADY-CLOSED");
                shortcut.complete(null);
				return new CompletableFuture<>();
            }

            boolean canClaimOther = player.hasPermission("modreq.admin")
                    || player.hasPermission("modreq.mod.overrideclaimed");

            if(claim) {
                if(request.isClaimed() && !canClaimOther) {
                    Messages.send(player, "error.ALREADY-CLAIMED");
                    shortcut.complete(null);
				    return new CompletableFuture<>();
                }

                return plugin.getRequestRegistry().claim(request, player);
            } else if(request.isClaimed()) {
                if(!request.isClaimedBy(player.getUniqueId()) && !canClaimOther) {
                    Messages.send(player, "error.OTHER-CLAIMED");
                    shortcut.complete(null);
				    return new CompletableFuture<>();
                }

                return plugin.getRequestRegistry().unclaim(request);
            } else {
                Messages.send(player, "error.NOT-CLAIMED");
                shortcut.complete(null);
				return new CompletableFuture<>();
            }
        }).thenAcceptAsync((Request result) -> {
            if(result.isClaimed()) {
                Messages.sendModNotification(NotificationType.CLAIMED, player, result);
            } else {
                Messages.sendModNotification(NotificationType.UNCLAIMED, player, result);
            }
        }).applyToEither(shortcut, Function.identity()).exceptionally((e) -> {
            Messages.send(player, "error.DATABASE-ERROR");
            return null;
        });
    }
}

