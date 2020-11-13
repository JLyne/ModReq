package uk.co.notnull.modreq.commands;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

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
        CompletableFuture<Void> shortcut = new CompletableFuture<>();

        plugin.getRequestRegistry().get(id).thenComposeAsync((Request request) -> {
            if(request == null) {
                Messages.send(player, "error.ID-ERROR", "id", String.valueOf(id));
                shortcut.complete(null);
				return new CompletableFuture<>();
            }

            if(!request.isClosed()) {
                return plugin.getRequestRegistry().elevate(request, !request.isElevated());
            } else {
                Messages.send(player, "error.ALREADY-CLOSED");
                shortcut.complete(null);
				return new CompletableFuture<>();
            }
        }).thenAcceptAsync((Request result) -> {
            String message = "mod.elevate." + (result.isElevated() ? "1" : "2");
            Messages.sendToMods(message, "mod", player.getName(), "id", String.valueOf(id));
        }).applyToEither(shortcut, Function.identity()).exceptionally(e -> {
            e.printStackTrace();
            Messages.send(player, "error.DATABASE-ERROR");
            return null;
        });
    }
}