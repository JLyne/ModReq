package uk.co.notnull.modreq.commands;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import uk.co.notnull.modreq.ModReq;
import uk.co.notnull.modreq.Request;

public class CmdTpid {
    private final ModReq plugin;
    public CmdTpid(ModReq plugin) {
        this.plugin = plugin;
    }

    public void tpToModReq(final Player player, final int id) {
        plugin.getRequestRegistry().get(id).thenAcceptAsync((Request request) -> {
            if(request == null) {
                player.sendMessage(plugin.getLanguageFile().getLangString("error.ID-ERROR")
                                           .replaceAll("%id", String.valueOf(id)));
                return;
            }

            Bukkit.getScheduler().runTask(plugin, () -> {
                if(player.teleport(request.getLocation())) {
                    player.sendMessage(ModReq.getPlugin().getLanguageFile().getLangString("mod.TELEPORT")
                                               .replaceAll("%id", "" + id));
                } else {
                    ModReq.getPlugin().sendMsg(player, "error.TELEPORT-ERROR");
                }
            });
        }).exceptionally((e) -> {
            ModReq.getPlugin().sendMsg(player, "error.DATABASE-ERROR");
            return null;
        });
    }
}

