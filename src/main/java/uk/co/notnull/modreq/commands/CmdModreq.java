package uk.co.notnull.modreq.commands;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import uk.co.notnull.modreq.Messages;
import uk.co.notnull.modreq.ModReq;
import uk.co.notnull.modreq.Request;

public class CmdModreq {
    private final ModReq plugin;

    public CmdModreq(ModReq plugin) {
        this.plugin = plugin;
    }

    public void modreq(final Player player, final String message) {
        plugin.getRequestRegistry().getOpenCount(player).thenComposeAsync((Integer count) -> {
            if(count >= plugin.getConfiguration().getMax_open_modreqs()) {

                Messages.send(player, "error.MAX-OPEN-MODREQS", "max",
                              String.valueOf(plugin.getConfiguration().getMax_open_modreqs()));

                return CompletableFuture.completedFuture(null);
            }

            return plugin.getRequestRegistry().create(player, message).thenAcceptAsync((Request request) -> {
                Messages.send(player, "player.REQUEST-FILED");
                Messages.sendToMods("mod.NEW-MODREQ", "id", String.valueOf(request.getId()));
                plugin.playModSound();
            });
        }).exceptionally((e) -> {
            Messages.send(player, "error.DATABASE-ERROR");
            return null;
        });
    }

    public void checkPlayerModReqs(final Player player) {
        BukkitRunnable runnable = new BukkitRunnable() {
            public void run() {
                try {
                    Connection connection = ModReq.getPlugin().getDataSource().getConnection();

                    PreparedStatement pStatement = connection.prepareStatement("SELECT id,request,timestamp,mod_uuid,mod_comment,mod_timestamp,done FROM modreq WHERE uuid=? ORDER BY id DESC");
                    pStatement.setString(1, player.getUniqueId().toString());
                    ResultSet sqlres = pStatement.executeQuery();
                    if (!sqlres.next()) {
                        ModReq.getPlugin().sendMsg(player, "player.check.NO-MODREQS");
                    } else {
                        ArrayList requests = new ArrayList();

                        while(!sqlres.isAfterLast()) {
                            requests.add(new Request(sqlres.getInt(1), "", sqlres.getString(2), sqlres.getLong(3), "", 0, 0, 0, "", sqlres.getString(4), sqlres.getString(5), sqlres.getLong(6), sqlres.getInt(7), 0));
                            sqlres.next();
                        }

                        int end = requests.size();
                        if (end > ModReq.getPlugin().getConfiguration().getShow_last_modreqs()) {
                            end = ModReq.getPlugin().getConfiguration().getShow_last_modreqs();
                        }

                        --end;
                        int count = ModReq.getPlugin().getConfiguration().getShow_last_modreqs();
                        player.sendMessage(ModReq.getPlugin().getLanguageFile().getLangString("player.check.1").replaceAll("%count", "" + count));
                        boolean first = true;

                        for(int i = end; i > -1; --i) {
                            if (first) {
                                first = false;
                            } else {
                                player.sendMessage("");
                            }

                            OfflinePlayer modUser = null;
                            if (!((Request)requests.get(i)).getMod_uuid().equals("")) {
                                modUser = ModReq.getPlugin().getOfflinePlayer(((Request)requests.get(i)).getMod_uuid());
                            }

                            String status = "";
                            if (((Request)requests.get(i)).getDone() != 0) {
                                status = status + ModReq.getPlugin().getLanguageFile().getLangString("general.CLOSED");
                            } else {
                                status = status + ModReq.getPlugin().getLanguageFile().getLangString("general.OPEN");
                            }

                            String timestamp_formatted = ModReq.getPlugin().getFormat().format(((Request)requests.get(i)).getTimestamp());
                            String mod_timestamp_formatted = ModReq.getPlugin().getFormat().format(((Request)requests.get(i)).getMod_timestamp());
                            player.sendMessage(ModReq.getPlugin().getLanguageFile().getLangString("player.check.2").replaceAll("%status", status).replaceAll("%date", timestamp_formatted).replaceAll("%id", "" + ((Request)requests.get(i)).getId()));
                            player.sendMessage(ModReq.getPlugin().getLanguageFile().getLangString("player.check.3").replaceAll("%msg", ((Request)requests.get(i)).getRequest()));
                            if (((Request)requests.get(i)).getDone() != 0) {
                                if (modUser.getName() != null) {
                                    player.sendMessage(ModReq.getPlugin().getLanguageFile().getLangString("player.check.4").replaceAll("%mod", modUser.getName()).replaceAll("%date", mod_timestamp_formatted));
                                } else {
                                    player.sendMessage(ModReq.getPlugin().getLanguageFile().getLangString("player.check.4").replaceAll("%mod", "unknown").replaceAll("%date", mod_timestamp_formatted));
                                }

                                player.sendMessage(ModReq.getPlugin().getLanguageFile().getLangString("player.check.5").replaceAll("%msg", ((Request)requests.get(i)).getMod_comment()));
                            }
                        }

                        ModReq.getPlugin().sendMsg(player, "player.check.6");
                    }
                } catch (SQLException var13) {
                    var13.printStackTrace();
                    Messages.send(player , "error.DATABASE-ERROR");
                }

            }
        };
        runnable.runTaskAsynchronously(ModReq.getPlugin());
    }
}

