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

    public void doneModReq(final Player player, final int id, String message) {
        BukkitRunnable runnable = new BukkitRunnable() {
            public void run() {
                try {
                    Connection connection = ModReq.getPlugin().getSqlHandler().open();
                    if (connection == null) {
                        ModReq.getPlugin().sendMsg(player, "error.DATABASE-ERROR");
                        return;
                    }

                    PreparedStatement pStatement = connection.prepareStatement("SELECT uuid,claimed,mod_uuid FROM modreq WHERE id=?");
                    pStatement.setInt(1, id);
                    ResultSet sqlres = pStatement.executeQuery();
                    if (!sqlres.next()) {
                        player.sendMessage(ModReq.getPlugin().getLanguageFile().getLangString("error.ID-ERROR").replaceAll("%id", "" + id));
                    } else {
                        String uuid = sqlres.getString(1);
                        String claimed = sqlres.getString(2);
                        String mod_uuid = sqlres.getString(3);
                        sqlres.close();
                        pStatement.close();
                        if (!mod_uuid.equals("")) {
                            ModReq.getPlugin().sendMsg(player, "error.ALREADY-CLOSED");
                            connection.close();
                            return;
                        }

                        if (!claimed.equals("") && !claimed.equals(player.getUniqueId().toString()) && !player.hasPermission("modreq.mod.admin") && !player.hasPermission("modreq.mod.overrideclaimed")) {
                            ModReq.getPlugin().sendMsg(player, "error.OTHER-CLAIMED");
                            connection.close();
                            return;
                        }

                        Player requestSender = Bukkit.getPlayer(UUID.fromString(uuid));

                        if (requestSender != null && requestSender.isOnline()) {
                            pStatement.close();
                            pStatement = connection.prepareStatement("UPDATE modreq SET claimed='',mod_uuid=?,mod_comment=?,mod_timestamp=?,done='2',elevated='0' WHERE id=?");
                            pStatement.setString(1, player.getUniqueId().toString());
                            pStatement.setString(2, message.trim());
                            pStatement.setLong(3, System.currentTimeMillis());
                            pStatement.setInt(4, id);
                            pStatement.executeUpdate();
                            requestSender.sendMessage(ModReq.getPlugin().getLanguageFile().getLangString("player.DONE").replaceAll("%mod", player.getName()).replaceAll("%id", "" + id));
                            requestSender.sendMessage(ModReq.getPlugin().getLanguageFile().getLangString("general.DONE-MESSAGE").replaceAll("%msg", message));
                            ModReq.getPlugin().playSound(requestSender);
                            ModReq.getPlugin().sendModMsg(ModReq.getPlugin().getLanguageFile().getLangString("mod.DONE").replaceAll("%id", "" + id).replaceAll("%mod", player.getName()));
                            ModReq.getPlugin().sendModMsg(ModReq.getPlugin().getLanguageFile().getLangString("general.DONE-MESSAGE").replaceAll("%msg", message));
                            pStatement.close();
                            pStatement = connection.prepareStatement("DELETE FROM modreq_notes WHERE modreq_id=?");
                            pStatement.setInt(1, id);
                            pStatement.executeUpdate();
                            pStatement.close();
                        } else {
                            pStatement.close();
                            pStatement = connection.prepareStatement("UPDATE modreq SET claimed='',mod_uuid=?,mod_comment=?,mod_timestamp=?,done='1',elevated='0' WHERE id=?");
                            pStatement.setString(1, player.getUniqueId().toString());
                            pStatement.setString(2, message.trim());
                            pStatement.setLong(3, System.currentTimeMillis());
                            pStatement.setInt(4, id);
                            pStatement.executeUpdate();
                            ModReq.getPlugin().sendModMsg(ModReq.getPlugin().getLanguageFile().getLangString("mod.DONE").replaceAll("%id", "" + id).replaceAll("%mod", player.getName()));
                            ModReq.getPlugin().sendModMsg(ModReq.getPlugin().getLanguageFile().getLangString("general.DONE-MESSAGE").replaceAll("%msg", message));
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
                    ModReq.getPlugin().sendMsg(player, "error.DATABASE-ERROR");
                }

            }
        };
        runnable.runTaskAsynchronously(ModReq.getPlugin());
    }
}
