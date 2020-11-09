package uk.co.notnull.modreq.commands;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import uk.co.notnull.modreq.ModReq;

public class CmdReopen {
    public CmdReopen() {
    }

    public void reopenModReq(final Player pPlayer, final String[] pArgs) {
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

                    PreparedStatement pStatement = connection.prepareStatement("SELECT done FROM modreq WHERE id=?");
                    pStatement.setInt(1, id);
                    ResultSet sqlres = pStatement.executeQuery();
                    if (sqlres.next()) {
                        int done = sqlres.getInt(1);
                        sqlres.close();
                        pStatement.close();
                        if (done > 0) {
                            pStatement = connection.prepareStatement("UPDATE modreq SET claimed='',mod_uuid='',mod_comment='',mod_timestamp='0',done='0',elevated='0' WHERE id=?");
                            pStatement.setInt(1, id);
                            pStatement.executeUpdate();
                            pStatement.close();
                            ModReq.getPlugin().sendModMsg(ModReq.getPlugin().getLanguageFile().getLangString("mod.REOPEN").replaceAll("%mod", pPlayer.getName()).replaceAll("%id", "" + id));
                        } else {
                            ModReq.getPlugin().sendMsg(pPlayer, "error.NOT-CLOSED");
                        }
                    } else {
                        pPlayer.sendMessage(ModReq.getPlugin().getLanguageFile().getLangString("error.ID-ERROR").replaceAll("%id", "" + id));
                    }

                    connection.close();
                } catch (SQLException var6) {
                    var6.printStackTrace();
                    ModReq.getPlugin().sendMsg(pPlayer, "error.DATABASE-ERROR");
                }

            }
        };
        runnable.runTaskAsynchronously(ModReq.getPlugin());
    }
}

