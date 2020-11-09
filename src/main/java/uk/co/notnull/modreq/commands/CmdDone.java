package uk.co.notnull.modreq.commands;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import uk.co.notnull.modreq.ModReq;

public class CmdDone {
    public CmdDone() {
    }

    public void doneModReq(final Player pPlayer, final String[] pArgs) {
        BukkitRunnable runnable = new BukkitRunnable() {
            public void run() {
                boolean var1 = false;

                int id;
                try {
                    id = Integer.parseInt(pArgs[0]);
                } catch (NumberFormatException var11) {
                    pPlayer.sendMessage(ModReq.getPlugin().getLanguageFile().getLangString("error.NUMBER-ERROR").replaceAll("%id", pArgs[0]));
                    return;
                }

                try {
                    Connection connection = ModReq.getPlugin().getSqlHandler().open();
                    if (connection == null) {
                        ModReq.getPlugin().sendMsg(pPlayer, "error.DATABASE-ERROR");
                        return;
                    }

                    PreparedStatement pStatement = connection.prepareStatement("SELECT uuid,claimed,mod_uuid FROM modreq WHERE id=?");
                    pStatement.setInt(1, id);
                    ResultSet sqlres = pStatement.executeQuery();
                    if (!sqlres.next()) {
                        pPlayer.sendMessage(ModReq.getPlugin().getLanguageFile().getLangString("error.ID-ERROR").replaceAll("%id", "" + id));
                    } else {
                        String uuid = sqlres.getString(1);
                        String claimed = sqlres.getString(2);
                        String mod_uuid = sqlres.getString(3);
                        sqlres.close();
                        pStatement.close();
                        if (!mod_uuid.equals("")) {
                            ModReq.getPlugin().sendMsg(pPlayer, "error.ALREADY-CLOSED");
                            connection.close();
                            return;
                        }

                        if (!claimed.equals("") && !claimed.equals(pPlayer.getUniqueId().toString()) && !pPlayer.hasPermission("modreq.mod.admin") && !pPlayer.hasPermission("modreq.mod.overrideclaimed")) {
                            ModReq.getPlugin().sendMsg(pPlayer, "error.OTHER-CLAIMED");
                            connection.close();
                            return;
                        }

                        Player requestSender = Bukkit.getPlayer(UUID.fromString(uuid));
                        String answer = "";

                        for(int i = 1; i < pArgs.length; ++i) {
                            answer = answer + pArgs[i] + " ";
                        }

                        if (requestSender != null && requestSender.isOnline()) {
                            pStatement.close();
                            pStatement = connection.prepareStatement("UPDATE modreq SET claimed='',mod_uuid=?,mod_comment=?,mod_timestamp=?,done='2',elevated='0' WHERE id=?");
                            pStatement.setString(1, pPlayer.getUniqueId().toString());
                            pStatement.setString(2, answer.trim());
                            pStatement.setLong(3, System.currentTimeMillis());
                            pStatement.setInt(4, id);
                            pStatement.executeUpdate();
                            requestSender.sendMessage(ModReq.getPlugin().getLanguageFile().getLangString("player.DONE").replaceAll("%mod", pPlayer.getName()).replaceAll("%id", "" + id));
                            requestSender.sendMessage(ModReq.getPlugin().getLanguageFile().getLangString("general.DONE-MESSAGE").replaceAll("%msg", answer));
                            ModReq.getPlugin().playSound(requestSender);
                            ModReq.getPlugin().sendModMsg(ModReq.getPlugin().getLanguageFile().getLangString("mod.DONE").replaceAll("%id", "" + id).replaceAll("%mod", pPlayer.getName()));
                            ModReq.getPlugin().sendModMsg(ModReq.getPlugin().getLanguageFile().getLangString("general.DONE-MESSAGE").replaceAll("%msg", answer));
                            pStatement.close();
                            pStatement = connection.prepareStatement("DELETE FROM modreq_notes WHERE modreq_id=?");
                            pStatement.setInt(1, id);
                            pStatement.executeUpdate();
                            pStatement.close();
                        } else {
                            pStatement.close();
                            pStatement = connection.prepareStatement("UPDATE modreq SET claimed='',mod_uuid=?,mod_comment=?,mod_timestamp=?,done='1',elevated='0' WHERE id=?");
                            pStatement.setString(1, pPlayer.getUniqueId().toString());
                            pStatement.setString(2, answer.trim());
                            pStatement.setLong(3, System.currentTimeMillis());
                            pStatement.setInt(4, id);
                            pStatement.executeUpdate();
                            ModReq.getPlugin().sendModMsg(ModReq.getPlugin().getLanguageFile().getLangString("mod.DONE").replaceAll("%id", "" + id).replaceAll("%mod", pPlayer.getName()));
                            ModReq.getPlugin().sendModMsg(ModReq.getPlugin().getLanguageFile().getLangString("general.DONE-MESSAGE").replaceAll("%msg", answer));
                            pStatement.close();
                            pStatement = connection.prepareStatement("DELETE FROM modreq_notes WHERE modreq_id=?");
                            pStatement.setInt(1, id);
                            pStatement.executeUpdate();
                            pStatement.close();
                        }
                    }

                    connection.close();
                } catch (SQLException var12) {
                    var12.printStackTrace();
                    ModReq.getPlugin().sendMsg(pPlayer, "error.DATABASE-ERROR");
                }

            }
        };
        runnable.runTaskAsynchronously(ModReq.getPlugin());
    }
}
