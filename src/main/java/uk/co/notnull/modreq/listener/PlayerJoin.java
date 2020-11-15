package uk.co.notnull.modreq.listener;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import uk.co.notnull.modreq.*;
import uk.co.notnull.modreq.util.MineDown;

public class PlayerJoin {
    private final ModReq plugin;

    public PlayerJoin(ModReq plugin) {
        this.plugin = plugin;
    }

    public void onPlayerJoin(final Player player) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> this.joinChecks(player), 100L);
    }

    public void joinChecks(Player player) {
        plugin.getRequestRegistry().getUnseen(player, true).thenAcceptAsync((RequestCollection requests) -> {
            if(requests.isEmpty()) {
                return;
            }

            Messages.send(player, "general.ON-JOIN-HEADER");

            for(Request request: requests) {
                OfflinePlayer mod = Bukkit.getOfflinePlayer(request.getResponder());

                Messages.send(player, new MineDown(Messages.getString("player.notification.CLOSED"))
                            .placeholderIndicator("%")
                            .replace(
                                    "id", String.valueOf(request.getId()),
                                    "message", request.getResponseMessage())
                            .replace("actor", Messages.getPlayer(mod))
                            .replace("link", Messages.get("general.REQUEST-LINK",
                                                          "id", String.valueOf(request.getId())))
                            .replace("view", Messages.get("player.action.VIEW",
                                                          "id", String.valueOf(request.getId())))
                            .toComponent());
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

                Messages.send(player, "mod.JOIN", "count", String.valueOf(count));
                plugin.playSound(player);
            }).exceptionally(e -> {
                e.printStackTrace();
                Messages.send(player, "error.DATABASE-ERROR");
                return null;
            });
        }
    }
}
