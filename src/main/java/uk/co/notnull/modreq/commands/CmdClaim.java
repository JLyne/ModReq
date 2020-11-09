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

    public void claimModReq(final Player pPlayer, final String[] pArgs, final boolean pClaim) {
        BukkitRunnable runnable = new BukkitRunnable() {
            public void run() {
                boolean var1 = false;

                int id;
                try {
                    id = Integer.parseInt(pArgs[0]);
                } catch (NumberFormatException var7) {
                    pPlayer.sendMessage(
                            ModReq.getPlugin().getLanguageFile().getLangString("error.NUMBER-ERROR").replaceAll("%id", pArgs[0]));
                    return;
                }

                try {
                    Connection connection = ModReq.getPlugin().getSqlHandler().open();
                    if (connection == null) {
                        ModReq.getPlugin().sendMsg(pPlayer, "error.DATABASE-ERROR");
                        return;
                    }

                    PreparedStatement pStatement = connection.prepareStatement("SELECT done,claimed FROM modreq WHERE id=?");
                    pStatement.setInt(1, id);
                    ResultSet sqlres = pStatement.executeQuery();
                    if (sqlres.next()) {
                        int done = sqlres.getInt(1);
                        String claimed = sqlres.getString(2);
                        sqlres.close();
                        pStatement.close();
                        if (done == 0) {
                            if (pClaim) {
                                if (!claimed.equals("") && !pPlayer.hasPermission("modreq.admin") && !pPlayer.hasPermission("modreq.mod.overrideclaimed")) {
                                    ModReq.getPlugin().sendMsg(pPlayer, "error.ALREADY-CLAIMED");
                                } else {
                                    pStatement.close();
                                    pStatement = connection.prepareStatement("UPDATE modreq SET claimed=? WHERE id=?");
                                    pStatement.setString(1, pPlayer.getUniqueId().toString());
                                    pStatement.setInt(2, id);
                                    pStatement.executeUpdate();
                                    pStatement.close();
                                    ModReq.getPlugin().sendModMsg(ModReq.getPlugin().getLanguageFile().getLangString("mod.CLAIM").replaceAll("%mod", pPlayer.getName()).replaceAll("%id", "" + id));
                                }
                            } else if (!claimed.equals("")) {
                                if (!claimed.equals(pPlayer.getUniqueId().toString()) && !pPlayer.hasPermission("modreq.admin") && !pPlayer.hasPermission("modreq.mod.overrideclaimed")) {
                                    ModReq.getPlugin().sendMsg(pPlayer, "error.OTHER-CLAIMED");
                                } else {
                                    pStatement.close();
                                    pStatement = connection.prepareStatement("UPDATE modreq SET claimed='' WHERE id=?");
                                    pStatement.setInt(1, id);
                                    pStatement.executeUpdate();
                                    pStatement.close();
                                    ModReq.getPlugin().sendModMsg(ModReq.getPlugin().getLanguageFile().getLangString("mod.UNCLAIM").replaceAll("%mod", pPlayer.getName()).replaceAll("%id", "" + id));
                                }
                            } else {
                                ModReq.getPlugin().sendMsg(pPlayer, "error.NOT-CLAIMED");
                            }
                        } else {
                            ModReq.getPlugin().sendMsg(pPlayer, "error.ALREADY-CLOSED");
                        }
                    } else {
                        pPlayer.sendMessage(ModReq.getPlugin().getLanguageFile().getLangString("error.ID-ERROR").replaceAll("%id", "" + id));
                    }

                    connection.close();
                } catch (SQLException var8) {
                    var8.printStackTrace();
                    ModReq.getPlugin().sendMsg(pPlayer, "error.DATABASE-ERROR");
                }

            }
        };
        runnable.runTaskAsynchronously(ModReq.getPlugin());
    }
}

