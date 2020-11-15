package uk.co.notnull.modreq.commands;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import uk.co.notnull.modreq.Messages;
import uk.co.notnull.modreq.ModReq;
import uk.co.notnull.modreq.Request;
import uk.co.notnull.modreq.util.MineDown;

public class CmdTpid {
    private final ModReq plugin;
    public CmdTpid(ModReq plugin) {
        this.plugin = plugin;
    }

    public void tpToModReq(final Player player, final int id) {
        plugin.getRequestRegistry().get(id).thenAcceptAsync((Request request) -> {
            if(request == null) {
                Messages.send(player, "error.ID-ERROR", "id", String.valueOf(id));
                return;
            }

            Bukkit.getScheduler().runTask(plugin, () -> {
                if(player.teleport(request.getLocation())) {
                    Messages.send(player, new MineDown(Messages.getString("mod.notification.TELEPORTED"))
                            .placeholderIndicator("%")
                            .replace("id", String.valueOf(id))
                            .replace("link", Messages.get("general.REQUEST-LINK",
                                                          "id", String.valueOf(request.getId())))
                            .toComponent());
                } else {
                    Messages.send(player, "error.TELEPORT-ERROR");
                }
            });
        }).exceptionally((e) -> {
            Messages.send(player, "error.DATABASE-ERROR");
            return null;
        });
    }
}

