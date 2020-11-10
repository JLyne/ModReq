package uk.co.notnull.modreq.commands;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import uk.co.notnull.modreq.ModReq;
import uk.co.notnull.modreq.Request;

public class CmdModreq {
    public CmdModreq() {
    }

    public void modreq(final Player player, final String message) {
        BukkitRunnable runnable = new BukkitRunnable() {
            public void run() {
                try {
                    Connection connection = ModReq.getPlugin().getDataSource().getConnection();

                    PreparedStatement pStatement = connection.prepareStatement("SELECT COUNT(id) FROM modreq WHERE uuid=? AND done='0'");
                    pStatement.setString(1, player.getUniqueId().toString());
                    ResultSet sqlres = pStatement.executeQuery();
                    if (!sqlres.next()) {
                        ModReq.getPlugin().sendMsg(player, "error.DATABASE-ERROR");
                        sqlres.close();
                        pStatement.close();
                    } else {
                        int count = sqlres.getInt(1);
                        sqlres.close();
                        pStatement.close();
                        if (count >= ModReq.getPlugin().getConfiguration().getMax_open_modreqs()) {
                            player.sendMessage(ModReq.getPlugin().getLanguageFile().getLangString("error.MAX-OPEN-MODREQS").replaceAll("%max", "" + ModReq.getPlugin().getConfiguration().getMax_open_modreqs()));
                        } else {
                            Location location = player.getLocation();
                            pStatement = connection.prepareStatement("INSERT INTO modreq (uuid,request,timestamp,world,x,y,z) VALUES (?,?,?,?,?,?,?)");
                            pStatement.setString(1, player.getUniqueId().toString());
                            pStatement.setString(2, message.trim());
                            pStatement.setLong(3, System.currentTimeMillis());
                            pStatement.setString(4, location.getWorld().getName());
                            pStatement.setInt(5, location.getBlockX());
                            pStatement.setInt(6, location.getBlockY());
                            pStatement.setInt(7, location.getBlockZ());
                            pStatement.executeUpdate();
                            pStatement.close();
                            ModReq.getPlugin().sendMsg(player, "player.REQUEST-FILED");
                            pStatement = connection.prepareStatement("SELECT MAX(id) FROM modreq WHERE uuid=?");
                            pStatement.setString(1, player.getUniqueId().toString());
                            sqlres = pStatement.executeQuery();
                            if (sqlres.next()) {
                                int id = sqlres.getInt(1);
                                sqlres.close();
                                pStatement.close();
                                ModReq.getPlugin().sendModMsg(ModReq.getPlugin().getLanguageFile().getLangString("mod.NEW-MODREQ").replaceAll("%id", "" + id));
                                ModReq.getPlugin().playModSound();
                            }
                        }
                    }
                } catch (SQLException var7) {
                    var7.printStackTrace();
                    ModReq.getPlugin().sendMsg(player, "error.DATABASE-ERROR");
                }

            }
        };
        runnable.runTaskAsynchronously(ModReq.getPlugin());
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
                    ModReq.getPlugin().sendMsg(player, "error.DATABASE-ERROR");
                }

            }
        };
        runnable.runTaskAsynchronously(ModReq.getPlugin());
    }
}

