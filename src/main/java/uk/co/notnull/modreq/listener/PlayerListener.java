package uk.co.notnull.modreq.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerListener implements Listener {
    public PlayerListener() {
    }

    @EventHandler(
        priority = EventPriority.NORMAL
    )
    public void onPlayerJoin(PlayerJoinEvent pEvent) {
        (new PlayerJoin()).onPlayerJoin(pEvent.getPlayer());
    }
}
