package uk.co.notnull.modreq.commands;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import uk.co.notnull.modreq.Messages;
import uk.co.notnull.modreq.ModReq;
import uk.co.notnull.modreq.Note;

public class CmdNote {
    public CmdNote() {
    }

    public void addNote(final Player player, final int id, final String message) {
        BukkitRunnable runnable = new BukkitRunnable() {
            public void run() {
                try {
                    Connection connection = ModReq.getPlugin().getDataSource().getConnection();

                    PreparedStatement pStatement = connection.prepareStatement("SELECT done FROM modreq WHERE id=?");
                    pStatement.setInt(1, id);
                    ResultSet sqlres = pStatement.executeQuery();
                    if (!sqlres.next()) {
                        Messages.send(player, "error.ID-ERROR", "id", String.valueOf(id));
                    } else {
                        int done = sqlres.getInt(1);
                        sqlres.close();
                        pStatement.close();
                        if (done != 0) {
                            Messages.send(player, "error.ALREADY-CLOSED");
                        } else {
                            pStatement = connection.prepareStatement("INSERT INTO modreq_notes (modreq_id,uuid,note) VALUES (?,?,?)");
                            pStatement.setInt(1, id);
                            pStatement.setString(2, player.getUniqueId().toString());
                            pStatement.setString(3, message.trim());
                            pStatement.executeUpdate();
                            pStatement.close();
                            Messages.sendToMods("mod.note.ADD",
                                                "mod", player.getName(),
                                                "id", String.valueOf(id),
                                                "msg", message);
                        }
                    }
                } catch (SQLException var9) {
                    var9.printStackTrace();
                    Messages.send(player, "error.DATABASE-ERROR");
                }

            }
        };
        runnable.runTaskAsynchronously(ModReq.getPlugin());
    }

    public void removeNote(final Player player, final int id, final int noteId) {
        BukkitRunnable runnable = new BukkitRunnable() {
            public void run() {
                try {
                    Connection connection = ModReq.getPlugin().getDataSource().getConnection();

                    PreparedStatement pStatement = connection.prepareStatement("SELECT done FROM modreq WHERE id=?");
                    pStatement.setInt(1, id);
                    ResultSet sqlres = pStatement.executeQuery();
                    if (!sqlres.next()) {
                        Messages.send(player, "error.ID-ERROR", "id", String.valueOf(id));
                    } else {
                        int done = sqlres.getInt(1);
                        sqlres.close();
                        pStatement.close();
                        if (done != 0) {
                            Messages.send(player, "error.ALREADY-CLOSED");
                        } else {
                            pStatement = connection.prepareStatement("SELECT id,uuid,note FROM modreq_notes WHERE modreq_id=? ORDER BY id ASC");
                            pStatement.setInt(1, id);
                            sqlres = pStatement.executeQuery();
                            if (!sqlres.next()) {
                                Messages.send(player, "error.NOTE-DOES-NOT-EXIST");
                            } else {
                                ArrayList notes = new ArrayList();

                                while(!sqlres.isAfterLast()) {
                                    notes.add(new Note(sqlres.getInt(1), id, sqlres.getString(2), sqlres.getString(3)));
                                    sqlres.next();
                                }

                                sqlres.close();
                                pStatement.close();
                                if (noteId < notes.size() && noteId > -1) {
                                    if (((Note)notes.get(noteId)).getUuid().equals(player.getUniqueId().toString())) {
                                        pStatement.close();
                                        pStatement = connection.prepareStatement("DELETE FROM modreq_notes WHERE id=?");
                                        pStatement.setInt(1, ((Note)notes.get(noteId)).getId());
                                        pStatement.executeUpdate();
                                        pStatement.close();
                                        Messages.sendToMods("mod.note.REMOVE",
                                                            "mod", player.getName(),
                                                            "id", String.valueOf(id),
                                                            "msg", ((Note)notes.get(noteId)).getNote());
                                    } else {
                                        Messages.send(player, "error.NOTE-OTHER");
                                    }
                                } else {
                                    Messages.send(player, "error.NOTE-DOES-NOT-EXIST");
                                }
                            }
                        }
                    }
                } catch (SQLException var9) {
                    var9.printStackTrace();
                    Messages.send(player, "error.DATABASE-ERROR");
                }

            }
        };
        runnable.runTaskAsynchronously(ModReq.getPlugin());
    }
}

