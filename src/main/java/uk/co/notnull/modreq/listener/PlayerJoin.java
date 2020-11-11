package uk.co.notnull.modreq.listener;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import uk.co.notnull.modreq.ModReq;
import uk.co.notnull.modreq.Request;
import uk.co.notnull.modreq.RequestCollection;

public class PlayerJoin {
    private final ModReq plugin;

    public PlayerJoin(ModReq plugin) {
        this.plugin = plugin;
    }

    public void onPlayerJoin(final Player player) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> this.joinChecks(player), 100L);
    }

    public void joinChecks(Player player) {
        plugin.getRequestRegistry().getUnseenClosed(player, true).thenAcceptAsync((RequestCollection requests) -> {
            if(requests.isEmpty()) {
                return;
            }

            plugin.sendMsg(player, "general.ON-JOIN-HEADER");

            for(Request request: requests) {
                OfflinePlayer mod = request.getResponder() != null ? Bukkit.getOfflinePlayer(request.getResponder()) : null;
                String modName = mod != null & mod.getName() != null ? mod.getName() : "unknown";

                player.sendMessage(plugin.getLanguageFile().getLangString("player.DONE")
                                            .replaceAll("%mod", modName)
                                            .replaceAll("%id", "" + request.getId()));

                player.sendMessage(plugin.getLanguageFile().getLangString("general.DONE-MESSAGE")
                                            .replaceAll("%msg", request.getResponse()));
                player.sendMessage("");
            }

            plugin.sendMsg(player, "general.HELP-LIST-MODREQS");
            plugin.playSound(player);
        }).exceptionally(e -> {
            e.printStackTrace();
            plugin.sendMsg(player, "error.DATABASE-ERROR");
            return null;
        });

        if(player.hasPermission("modreq.mod") || player.hasPermission("modreq.admin")) {
            plugin.getRequestRegistry().getOpenCount(player.hasPermission("modreq.admin")).thenAcceptAsync((Integer count) -> {
                if(count == 0) {
                    return;
                }

                player.sendMessage(plugin.getLanguageFile().getLangString("mod.MODREQS-OPEN")
                                           .replaceAll("%count", String.valueOf(count)));
                plugin.playSound(player);
            }).exceptionally(e -> {
                e.printStackTrace();
                plugin.sendMsg(player, "error.DATABASE-ERROR");
                return null;
            });
        }
    }
}
