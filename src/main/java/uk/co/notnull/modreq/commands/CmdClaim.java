package uk.co.notnull.modreq.commands;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import uk.co.notnull.modreq.ModReq;

public class CmdClaim {
    public CmdClaim() {
    }

    public void claimModReq(final Player player, final int id, final boolean claim) {
        BukkitRunnable runnable = new BukkitRunnable() {
            public void run() {
                try {
                    Connection connection = ModReq.getPlugin().getDataSource().getConnection();

                    PreparedStatement pStatement = connection.prepareStatement("SELECT done,claimed FROM modreq WHERE id=?");
                    pStatement.setInt(1, id);
                    ResultSet sqlres = pStatement.executeQuery();
                    if (sqlres.next()) {
                        int done = sqlres.getInt(1);
                        String claimed = sqlres.getString(2);
                        sqlres.close();
                        pStatement.close();
                        if (done == 0) {
                            if (claim) {
                                if (!claimed.equals("") && !player.hasPermission("modreq.admin") && !player.hasPermission("modreq.mod.overrideclaimed")) {
                                    ModReq.getPlugin().sendMsg(player, "error.ALREADY-CLAIMED");
                                } else {
                                    pStatement.close();
                                    pStatement = connection.prepareStatement("UPDATE modreq SET claimed=? WHERE id=?");
                                    pStatement.setString(1, player.getUniqueId().toString());
                                    pStatement.setInt(2, id);
                                    pStatement.executeUpdate();
                                    pStatement.close();
                                    ModReq.getPlugin().sendModMsg(ModReq.getPlugin().getLanguageFile().getLangString("mod.CLAIM").replaceAll("%mod", player.getName()).replaceAll("%id", "" + id));
                                }
                            } else if (!claimed.equals("")) {
                                if (!claimed.equals(player.getUniqueId().toString()) && !player.hasPermission("modreq.admin") && !player.hasPermission("modreq.mod.overrideclaimed")) {
                                    ModReq.getPlugin().sendMsg(player, "error.OTHER-CLAIMED");
                                } else {
                                    pStatement.close();
                                    pStatement = connection.prepareStatement("UPDATE modreq SET claimed='' WHERE id=?");
                                    pStatement.setInt(1, id);
                                    pStatement.executeUpdate();
                                    pStatement.close();
                                    ModReq.getPlugin().sendModMsg(ModReq.getPlugin().getLanguageFile().getLangString("mod.UNCLAIM").replaceAll("%mod", player.getName()).replaceAll("%id", "" + id));
                                }
                            } else {
                                ModReq.getPlugin().sendMsg(player, "error.NOT-CLAIMED");
                            }
                        } else {
                            ModReq.getPlugin().sendMsg(player, "error.ALREADY-CLOSED");
                        }
                    } else {
                        player.sendMessage(ModReq.getPlugin().getLanguageFile().getLangString("error.ID-ERROR").replaceAll("%id", "" + id));
                    }
                } catch (SQLException var8) {
                    var8.printStackTrace();
                    ModReq.getPlugin().sendMsg(player, "error.DATABASE-ERROR");
                }

            }
        };
        runnable.runTaskAsynchronously(ModReq.getPlugin());
    }
}

