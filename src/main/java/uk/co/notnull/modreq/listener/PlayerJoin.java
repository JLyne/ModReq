package uk.co.notnull.modreq.listener;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import uk.co.notnull.modreq.ModReq;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;

public class PlayerJoin {
    public PlayerJoin() {
    }

    public void onPlayerJoin(final Player pPlayer) {
        BukkitRunnable runnable = new BukkitRunnable() {
            public void run() {
                try {
                    Connection connection = ModReq.getPlugin().getSqlHandler().open();
                    if (connection == null) {
                        ModReq.getPlugin().sendMsg(pPlayer, "error.DATABASE-ERROR");
                        return;
                    }

                    PreparedStatement pStatement = connection.prepareStatement("SELECT id,mod_uuid,mod_comment FROM modreq WHERE uuid=? AND done='1'");
                    pStatement.setString(1, pPlayer.getUniqueId().toString());
                    ResultSet sqlres = pStatement.executeQuery();
                    if (sqlres.next()) {
                        ModReq.getPlugin().sendMsg(pPlayer, "general.ON-JOIN-HEADER");
                        boolean first = true;
                        ArrayList ids = new ArrayList();

                        while(!sqlres.isAfterLast()) {
                            if (first) {
                                first = false;
                            } else {
                                pPlayer.sendMessage("");
                            }

                            OfflinePlayer mod = ModReq.getPlugin().getOfflinePlayer(sqlres.getString(2));
                            if (mod != null) {
                                pPlayer.sendMessage(ModReq.getPlugin().getLanguageFile().getLangString("player.DONE").replaceAll("%mod", mod.getName()).replaceAll("%id", "" + sqlres.getInt(1)));
                            } else {
                                pPlayer.sendMessage(ModReq.getPlugin().getLanguageFile().getLangString("player.DONE").replaceAll("%mod", "unknown").replaceAll("%id", "" + sqlres.getInt(1)));
                            }

                            pPlayer.sendMessage(ModReq.getPlugin().getLanguageFile().getLangString("general.DONE-MESSAGE").replaceAll("%msg", sqlres.getString(3)));
                            ids.add(sqlres.getInt(1));
                            sqlres.next();
                        }

                        pStatement.close();
                        Iterator var7 = ids.iterator();

                        while(var7.hasNext()) {
                            int i = (Integer)var7.next();
                            pStatement = connection.prepareStatement("UPDATE modreq SET done='2' WHERE id=?");
                            pStatement.setInt(1, i);
                            pStatement.executeUpdate();
                            pStatement.close();
                        }

                        ModReq.getPlugin().sendMsg(pPlayer, "general.HELP-LIST-MODREQS");
                        ModReq.getPlugin().playSound(pPlayer);
                    }

                    sqlres.close();
                    pStatement.close();
                    if (pPlayer.hasPermission("modreq.mod") || pPlayer.hasPermission("modreq.admin")) {
                        if (pPlayer.hasPermission("modreq.admin")) {
                            pStatement.close();
                            pStatement = connection.prepareStatement("SELECT COUNT(id) FROM modreq WHERE done='0'");
                        } else {
                            pStatement.close();
                            pStatement = connection.prepareStatement("SELECT COUNT(id) FROM modreq WHERE done='0' AND elevated='0'");
                        }

                        sqlres = pStatement.executeQuery();
                        if (sqlres.next() && sqlres.getInt(1) > 0) {
                            pPlayer.sendMessage(ModReq.getPlugin().getLanguageFile().getLangString("mod.MODREQS-OPEN").replaceAll("%count", "" + sqlres.getInt(1)));
                            ModReq.getPlugin().playSound(pPlayer);
                        }

                        sqlres.close();
                    }

                    connection.close();
                } catch (SQLException var8) {
                    var8.printStackTrace();
                    ModReq.getPlugin().sendMsg(pPlayer, "error.DATABASE-ERROR");
                }

            }
        };
        runnable.runTaskLaterAsynchronously(ModReq.getPlugin(), 100L);
    }
}
