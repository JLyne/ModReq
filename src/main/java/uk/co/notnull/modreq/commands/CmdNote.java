package uk.co.notnull.modreq.commands;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import uk.co.notnull.modreq.ModReq;
import uk.co.notnull.modreq.Note;

public class CmdNote {
    public CmdNote() {
    }

    public void addNote(final Player pPlayer, final String[] pArgs) {
        BukkitRunnable runnable = new BukkitRunnable() {
            public void run() {
                boolean var1 = false;

                int id;
                try {
                    id = Integer.parseInt(pArgs[1]);
                } catch (NumberFormatException var8) {
                    pPlayer.sendMessage(
                            ModReq.getPlugin().getLanguageFile().getLangString("error.NUMBER-ERROR").replaceAll("%id", pArgs[1]));
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
                    if (!sqlres.next()) {
                        pPlayer.sendMessage(ModReq.getPlugin().getLanguageFile().getLangString("error.ID-ERROR").replaceAll("%id", "" + id));
                    } else {
                        int done = sqlres.getInt(1);
                        sqlres.close();
                        pStatement.close();
                        if (done != 0) {
                            ModReq.getPlugin().sendMsg(pPlayer, "error.ALREADY-CLOSED");
                        } else {
                            String note = "";

                            for(int i = 2; i < pArgs.length; ++i) {
                                note = note + pArgs[i] + " ";
                            }

                            pStatement = connection.prepareStatement("INSERT INTO modreq_notes (modreq_id,uuid,note) VALUES (?,?,?)");
                            pStatement.setInt(1, id);
                            pStatement.setString(2, pPlayer.getUniqueId().toString());
                            pStatement.setString(3, note.trim());
                            pStatement.executeUpdate();
                            pStatement.close();
                            ModReq.getPlugin().sendModMsg(ModReq.getPlugin().getLanguageFile().getLangString("mod.note.ADD").replaceAll("%mod", pPlayer.getName()).replaceAll("%id", "" + id).replaceAll("%msg", note));
                        }
                    }

                    connection.close();
                } catch (SQLException var9) {
                    var9.printStackTrace();
                    ModReq.getPlugin().sendMsg(pPlayer, "error.DATABASE-ERROR");
                }

            }
        };
        runnable.runTaskAsynchronously(ModReq.getPlugin());
    }

    public void removeNote(final Player pPlayer, final String[] pArgs) {
        BukkitRunnable runnable = new BukkitRunnable() {
            public void run() {
                int modreq_id = 0;
                boolean var2 = false;

                int modreq_idx;
                int note_id;
                try {
                    modreq_idx = Integer.parseInt(pArgs[1]);
                    note_id = Integer.parseInt(pArgs[2]);
                } catch (NumberFormatException var8) {
                    pPlayer.sendMessage(ModReq.getPlugin().getLanguageFile().getLangString("error.NUMBER-ERROR").replaceAll("%id", pArgs[1]));
                    pPlayer.sendMessage(ModReq.getPlugin().getLanguageFile().getLangString("error.NUMBER-ERROR").replaceAll("%id", pArgs[2]));
                    return;
                }

                try {
                    Connection connection = ModReq.getPlugin().getSqlHandler().open();
                    if (connection == null) {
                        ModReq.getPlugin().sendMsg(pPlayer, "error.DATABASE-ERROR");
                        return;
                    }

                    PreparedStatement pStatement = connection.prepareStatement("SELECT done FROM modreq WHERE id=?");
                    pStatement.setInt(1, modreq_idx);
                    ResultSet sqlres = pStatement.executeQuery();
                    if (!sqlres.next()) {
                        pPlayer.sendMessage(ModReq.getPlugin().getLanguageFile().getLangString("error.ID-ERROR").replaceAll("%id", "" + modreq_idx));
                    } else {
                        int done = sqlres.getInt(1);
                        sqlres.close();
                        pStatement.close();
                        if (done != 0) {
                            ModReq.getPlugin().sendMsg(pPlayer, "error.ALREADY-CLOSED");
                        } else {
                            pStatement = connection.prepareStatement("SELECT id,uuid,note FROM modreq_notes WHERE modreq_id=? ORDER BY id ASC");
                            pStatement.setInt(1, modreq_idx);
                            sqlres = pStatement.executeQuery();
                            if (!sqlres.next()) {
                                ModReq.getPlugin().sendMsg(pPlayer, "error.NOTE-DOES-NOT-EXIST");
                            } else {
                                ArrayList notes = new ArrayList();

                                while(!sqlres.isAfterLast()) {
                                    notes.add(new Note(sqlres.getInt(1), modreq_idx, sqlres.getString(2), sqlres.getString(3)));
                                    sqlres.next();
                                }

                                sqlres.close();
                                pStatement.close();
                                if (note_id < notes.size() && note_id > -1) {
                                    if (((Note)notes.get(note_id)).getUuid().equals(pPlayer.getUniqueId().toString())) {
                                        pStatement.close();
                                        pStatement = connection.prepareStatement("DELETE FROM modreq_notes WHERE id=?");
                                        pStatement.setInt(1, ((Note)notes.get(note_id)).getId());
                                        pStatement.executeUpdate();
                                        pStatement.close();
                                        ModReq.getPlugin().sendModMsg(ModReq.getPlugin().getLanguageFile().getLangString("mod.note.REMOVE").replaceAll("%mod", pPlayer.getName()).replaceAll("%id", "" + modreq_idx).replaceAll("%msg", ((Note)notes.get(note_id)).getNote()));
                                    } else {
                                        ModReq.getPlugin().sendMsg(pPlayer, "error.NOTE-OTHER");
                                    }
                                } else {
                                    ModReq.getPlugin().sendMsg(pPlayer, "error.NOTE-DOES-NOT-EXIST");
                                }
                            }
                        }
                    }

                    connection.close();
                } catch (SQLException var9) {
                    var9.printStackTrace();
                    ModReq.getPlugin().sendMsg(pPlayer, "error.DATABASE-ERROR");
                }

            }
        };
        runnable.runTaskAsynchronously(ModReq.getPlugin());
    }
}

