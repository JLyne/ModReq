package uk.co.notnull.modreq.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import uk.co.notnull.modreq.ModReq;

public class PlayerListener implements Listener {
    public PlayerListener() {
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent pEvent) {
        (new PlayerJoin(ModReq.getPlugin())).onPlayerJoin(pEvent.getPlayer());
    }
}
