package uk.co.notnull.modreq.listener;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import uk.co.notnull.modreq.*;

public class PlayerJoin {
    private final ModReq plugin;

    public PlayerJoin(ModReq plugin) {
        this.plugin = plugin;
    }

    public void onPlayerJoin(final Player player) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> this.joinChecks(player), 100L);
    }

    public void joinChecks(Player player) {
        RequestQuery query = RequestQuery.unseen().creator(player.getUniqueId());

        plugin.getRequestRegistry().getAll(query).thenAcceptAsync((RequestCollection requests) -> {
            if(requests.isEmpty()) {
                return;
            }

            Messages.send(player, "general.ON-JOIN-HEADER");

            for(Request request: requests) {
                OfflinePlayer mod = request.getResponder() != null ? Bukkit.getOfflinePlayer(request.getResponder()) : null;
                String modName = mod != null && mod.getName() != null ? mod.getName() : "unknown";

                Messages.send(player, "player.notification.CLOSED",
                              "actor", modName,
                              "id", String.valueOf(request.getId()),
                              "message", request.getResponseMessage());
            }

            Messages.send(player, "general.HELP-LIST-MODREQS");
            plugin.playSound(player);
        }).exceptionally(e -> {
            e.printStackTrace();
            Messages.send(player, "error.DATABASE-ERROR");
            return null;
        });

        if(player.hasPermission("modreq.mod") || player.hasPermission("modreq.admin")) {
            plugin.getRequestRegistry().getCount(RequestQuery.open()).thenAcceptAsync((Integer count) -> {
                if(count == 0) {
                    return;
                }

                Messages.send(player, "mod.MODREQS-OPEN", "count", String.valueOf(count));
                plugin.playSound(player);
            }).exceptionally(e -> {
                e.printStackTrace();
                Messages.send(player, "error.DATABASE-ERROR");
                return null;
            });
        }
    }
}
