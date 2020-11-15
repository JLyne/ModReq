package uk.co.notnull.modreq.commands;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import uk.co.notnull.modreq.Messages;
import uk.co.notnull.modreq.ModReq;
import uk.co.notnull.modreq.NotificationType;
import uk.co.notnull.modreq.Request;

public class CmdDone {
    private final ModReq plugin;

    public CmdDone(ModReq plugin) {
        this.plugin = plugin;
    }

    public void doneModReq(final Player player, final int id, String message) {
        CompletableFuture<Void> shortcut = new CompletableFuture<>();

        plugin.getRequestRegistry().get(id).thenComposeAsync((Request request) -> {
            if(request == null) {
                Messages.send(player, "error.ID-ERROR", "id", String.valueOf(id));
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

            if(!request.isClaimedBy(player.getUniqueId()) && !canClaimOther) {
                Messages.send(player, "error.OTHER-CLAIMED");
                shortcut.complete(null);
				return new CompletableFuture<>();
            }

            return plugin.getRequestRegistry().close(request, player, message);
        }).thenAcceptAsync((Request result) -> {
            Player creator = Bukkit.getPlayer(result.getCreator());

            if(creator != null) {
                plugin.playSound(creator);
            }

            Messages.sendModNotification(NotificationType.CLOSED, player, result, "message", message);
        }).applyToEither(shortcut, Function.identity()).exceptionally((e) -> {
            Messages.send(player, "error.DATABASE-ERROR");
            return null;
        });
    }
}
