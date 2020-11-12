package uk.co.notnull.modreq.commands;

import java.util.concurrent.CompletableFuture;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import uk.co.notnull.modreq.Messages;
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
                Messages.send(player, "error.ID-ERROR", "id", String.valueOf(id));
                return CompletableFuture.completedFuture(null);
            }

            if(request.isClosed()) {
                Messages.send(player, "error.ALREADY-CLOSED");
                return CompletableFuture.completedFuture(null);
            }

            boolean canClaimOther = player.hasPermission("modreq.admin")
                    || player.hasPermission("modreq.mod.overrideclaimed");

            if(!request.isClaimedBy(player.getUniqueId()) && !canClaimOther) {
                Messages.send(player, "error.OTHER-CLAIMED");
                return CompletableFuture.completedFuture(null);
            }

            return plugin.getRequestRegistry().close(request, player, message).thenAcceptAsync((Request result) -> {
                Player creator = Bukkit.getPlayer(result.getCreator());

                if(creator != null) {
                    Messages.send(creator, "player.DONE", "mod", player.getName(), "id", String.valueOf(id));
                    Messages.send(creator, "general.DONE-MESSAGE", "msg", message);
                    plugin.playSound(creator);
                }

                Messages.sendToMods("player.DONE","mod", player.getName(), "id", String.valueOf(id));
                Messages.sendToMods("general.DONE-MESSAGE","msg", message);
            });
        }).exceptionally((e) -> {
            Messages.send(player, "error.DATABASE-ERROR");
            return null;
        });
    }
}
