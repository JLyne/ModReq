package uk.co.notnull.modreq.commands;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import uk.co.notnull.modreq.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CmdCheck implements Listener {
	private final ModReq plugin;

	Map<Player, String> lastSearch = new ConcurrentHashMap<>();

	public CmdCheck(ModReq plugin) {
		this.plugin = plugin;
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent event) {
		lastSearch.remove(event.getPlayer());
	}

	public void checkOpenModreqs(final Player player, final int page) {
		if(page < 1) {
			Messages.send(player, "error.NUMBER-ERROR", "id", String.valueOf(page));
			return;
		}

		plugin.getRequestRegistry().get(RequestQuery.open(), page).thenAcceptAsync(requests -> {
			Messages.sendList(player, requests, "/mr list %page%");
		}).exceptionally((e) -> {
			Messages.send(player, "error.DATABASE-ERROR");
			e.printStackTrace();
			return null;
		});
	}

	public void checkSpecialModreq(final Player player, final int id) {
		boolean isMod = (player.hasPermission("modreq.mod") || player.hasPermission("modreq.admin"));

		plugin.getRequestRegistry().get(id).thenAcceptAsync((Request request) -> {
            if(request == null || (!isMod && !request.getCreator().equals(player.getUniqueId()))) {
                Messages.send(player, "error.ID-ERROR", "id", String.valueOf(id));
                return;
            }

			Messages.send(player, request.toComponent(player));
		}).exceptionally((e) -> {
			Messages.send(player, "error.DATABASE-ERROR");
			e.printStackTrace();
			return null;
		});
	}

	public void searchModreqs(final Player player, final String search) {
		searchModreqs(player, search, 1);
	}

	public void searchModreqs(final Player player, final int page) {
		if(!lastSearch.containsKey(player)) {
			Messages.send(player, "error.NO-PREVIOUS-SEARCH");
			return;
		}

		searchModreqs(player, lastSearch.get(player), page);
	}

	public void searchModreqs(final Player player, final String search, final int page) {
		if (page < 1) {
			Messages.send(player, "error.NUMBER-ERROR", "id", String.valueOf(page));
			return;
		}

		lastSearch.compute(player, (a, b) -> search);

		plugin.getRequestRegistry().get(new RequestQuery().search(search), page).thenAcceptAsync(requests -> {
			Messages.sendList(player, requests, "/mr searchpage %page%");
		}).exceptionally((e) -> {
			Messages.send(player, "error.DATABASE-ERROR");
			e.printStackTrace();
			return null;
		});
	}
}
