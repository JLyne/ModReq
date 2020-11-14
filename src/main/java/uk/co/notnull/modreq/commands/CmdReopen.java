package uk.co.notnull.modreq.commands;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

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
        CompletableFuture<Void> shortcut = new CompletableFuture<>();

        plugin.getRequestRegistry().get(id).thenComposeAsync((Request request) -> {
            if(request != null && request.isClosed()) {
                return plugin.getRequestRegistry().reopen(request);
            }

            if(request == null) {
                Messages.send(player, "error.ID-ERROR", "id", String.valueOf(id));
            } else {
                Messages.send(player,"error.NOT-CLOSED");
            }

            shortcut.complete(null);
            return new CompletableFuture<>();
        }).thenAcceptAsync((Request result) -> {
            Messages.sendToMods("mod.notification.REOPENED", "actor", player.getName(), "id", String.valueOf(id));
        }).applyToEither(shortcut, Function.identity()).exceptionally(e -> {
            e.printStackTrace();
            Messages.send(player, "error.DATABASE-ERROR");
            return null;
        });
    }
}

