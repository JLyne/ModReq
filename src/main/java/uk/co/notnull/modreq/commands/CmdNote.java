package uk.co.notnull.modreq.commands;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.bukkit.entity.Player;
import uk.co.notnull.modreq.Messages;
import uk.co.notnull.modreq.ModReq;
import uk.co.notnull.modreq.Note;
import uk.co.notnull.modreq.Request;

public class CmdNote {
    private final ModReq plugin;

    public CmdNote(ModReq plugin) {
        this.plugin = plugin;
    }

    public void addNote(final Player player, final int id, final String message) {
        plugin.getRequestRegistry().get(id).thenComposeAsync((Request request) -> {
            if(request == null) {
                Messages.send(player, "error.ID-ERROR", "id", String.valueOf(id));
                return CompletableFuture.completedFuture(null);
            }

            if(request.isClosed()) {
                Messages.send(player, "error.ALREADY-CLOSED");
                return CompletableFuture.completedFuture(null);
            }

            return plugin.getRequestRegistry().addNote(request, player, message).thenAcceptAsync((Note note) -> {
                Messages.sendToMods("mod.note.ADD",
                                                "mod", player.getName(),
                                                "id", String.valueOf(id),
                                                "msg", message);
            });
        }).exceptionally((e) -> {
            Messages.send(player, "error.DATABASE-ERROR");
            return null;
        });
    }

    public void removeNote(final Player player, final int id, final int noteId) {
        plugin.getRequestRegistry().get(id).thenComposeAsync((Request request) -> {
            if(request == null) {
                Messages.send(player, "error.ID-ERROR", "id", String.valueOf(id));
                return CompletableFuture.completedFuture(null);
            }

            if(request.isClosed()) {
                Messages.send(player, "error.ALREADY-CLOSED");
                return CompletableFuture.completedFuture(null);
            }

             return plugin.getRequestRegistry().getNotes(request).thenComposeAsync((List<Note> notes) -> {
                Note note = notes.get(noteId);

                if(note == null) {
                    Messages.send(player, "error.NOTE-DOES-NOT-EXIST");
                    return CompletableFuture.completedFuture(null);
                }

                if(!note.getCreator().equals(player.getUniqueId()) && !player.hasPermission("modreq.admin")) {
                    Messages.send(player, "error.NOTE-OTHER");
                    return CompletableFuture.completedFuture(null);
                }

                return plugin.getRequestRegistry().removeNote(note).thenAcceptAsync((Boolean result) -> {
                    Messages.sendToMods("mod.note.REMOVE",
                                        "mod", player.getName(),
                                        "id", String.valueOf(id),
                                        "msg", note.getMessage());
                });
             });
        }).exceptionally((e) -> {
            Messages.send(player, "error.DATABASE-ERROR");
            return null;
        });
    }
}

