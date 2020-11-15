package uk.co.notnull.modreq.commands;

import org.bukkit.entity.Player;
import uk.co.notnull.modreq.*;

public class CmdCheck {
	private final ModReq plugin;

	public CmdCheck(ModReq plugin) {
		this.plugin = plugin;
	}

	public void checkOpenModreqs(final Player player) {
		checkOpenModreqs(player, 1);
	}

	public void checkOpenModreqs(final Player player, final int page) {
		if(page < 1) {
			Messages.send(player, "error.NUMBER-ERROR", "id", String.valueOf(page));
			return;
		}

		plugin.getRequestRegistry().get(RequestQuery.open(), page).thenAcceptAsync(requests -> {
			sendList(player, requests);
		}).exceptionally((e) -> {
			Messages.send(player, "error.DATABASE-ERROR");
			e.printStackTrace();
			return null;
		});
	}

	public void checkSpecialModreq(final Player player, final int id) {
		plugin.getRequestRegistry().get(id).thenAcceptAsync((Request request) -> {
            if(request == null) {
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

	public void searchModreqs(final Player player, final String search, int page) {
		if (page < 1) {
			Messages.send(player, "error.NUMBER-ERROR", "id", String.valueOf(page));
			return;
		}

		plugin.getRequestRegistry().get(new RequestQuery().search(search), page).thenAcceptAsync(requests -> {
			sendList(player, requests);
		}).exceptionally((e) -> {
			Messages.send(player, "error.DATABASE-ERROR");
			e.printStackTrace();
			return null;
		});
	}

	private void sendList(Player player, RequestCollection requests) {
		if(requests.isAfterLastPage()) {
			Messages.send(player, "error.PAGE-ERROR", "page", "" + requests.getPage());
			return;
		} else if (requests.isEmpty()) {
			Messages.send(player, "mod.list.NO-RESULTS");
			return;
		}

		Messages.send(player, "mod.list.HEADER", "count", String.valueOf(requests.getTotal()));
		Messages.send(player, requests.toComponent(player));

		if(requests.isPaginated()) {
			Messages.send(player, "mod.list.FOOTER",
						  "page", String.valueOf(requests.getPage()),
						  "allpages", String.valueOf(requests.getTotalPages()));
		}
	}
}
