package uk.co.notnull.modreq.commands;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import uk.co.notnull.modreq.ModReq;

public class CmdTpid {
    public CmdTpid() {
    }

    public void tpToModReq(final Player player, final int id) {
        BukkitRunnable runnable = new BukkitRunnable() {
            public void run() {
                try {
                    Connection connection = ModReq.getPlugin().getDataSource().getConnection();

                    PreparedStatement pStatement = connection.prepareStatement("SELECT world,x,y,z,done FROM modreq WHERE id=?");
                    pStatement.setInt(1, id);
                    ResultSet sqlres = pStatement.executeQuery();
                    if (sqlres.next()) {
                        String world = sqlres.getString(1);
                        int x = sqlres.getInt(2);
                        int y = sqlres.getInt(3);
                        int z = sqlres.getInt(4);
                        int done = sqlres.getInt(5);
                        sqlres.close();
                        pStatement.close();
                        if (done == 0) {
                            Bukkit.getScheduler().runTask(ModReq.getPlugin(), () -> {
                                if (player.teleport(new Location(Bukkit.getWorld(world), (double)x, (double)y, (double)z))) {
                                    player.sendMessage(ModReq.getPlugin().getLanguageFile().getLangString("mod.TELEPORT").replaceAll("%id", "" + id));
                                } else {
                                    ModReq.getPlugin().sendMsg(player, "error.TELEPORT-ERROR");
                                }

                            });
                        } else {
                            ModReq.getPlugin().sendMsg(player, "error.ALREADY-CLOSED");
                        }
                    } else {
                        player.sendMessage(ModReq.getPlugin().getLanguageFile().getLangString("error.ID-ERROR").replaceAll("%id", "" + id));
                    }
                } catch (SQLException var11) {
                    var11.printStackTrace();
                    ModReq.getPlugin().sendMsg(player, "error.DATABASE-ERROR");
                }

            }
        };
        runnable.runTaskAsynchronously(ModReq.getPlugin());
    }
}

