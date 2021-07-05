package uk.co.notnull.modreq;

import de.themoep.minedown.adventure.MineDownParser;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.*;

public class Request {
    private final Location location;
    private final int id;
    private final boolean elevated;
    private final Date createTime;
    private final UUID creator;
    private final String message;
    private final UUID owner;
    private final Response response;
    private final List<Note> notes;

    Request(int id, UUID creator, String message, Date createTime, Location location, UUID owner, boolean elevated, Response response, List<Note> notes) {
        Objects.requireNonNull(creator);
        Objects.requireNonNull(message);
        Objects.requireNonNull(createTime);
        Objects.requireNonNull(location);
        this.id = id;
        this.creator = creator;
        this.message = message;
        this.createTime = createTime;
        this.owner = owner;
        this.response = response;
        this.elevated = elevated;
        this.location = location;
        this.notes = notes;
    }

    public static RequestBuilder.IDStep builder() {
        return RequestBuilder.builder();
    }

    public int getId() {
        return this.id;
    }

    public String getMessage() {
        return message;
    }

    public Location getLocation() {
        return location;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public Date getCloseTime() {
        return hasResponse() ? response.getTime() : null;
    }

    public UUID getCreator() {
        return creator;
    }

    public UUID getOwner() {
        return owner;
    }

    public boolean hasResponse() {
        return response != null;
    }

    public UUID getResponder() {
        return hasResponse() ? response.getResponder() : null;
    }

    public String getResponseMessage() {
        return hasResponse() ? response.getMessage() : null;
    }

    public Response getResponse() {
        return response;
    }

    public boolean hasNotes() {
        return !notes.isEmpty();
    }

    public List<Note> getNotes() {
        return new ArrayList<>(notes);
    }

    public boolean isElevated() { return this.elevated; }

    public boolean isClosed() { return hasResponse(); }

    public boolean isClaimed() { return owner != null; }

    public boolean isClaimedBy(UUID uuid) { return uuid.equals(owner); }

    public boolean isCreatorOnline() {
        Player player = Bukkit.getPlayer(creator);
        return player != null && player.isOnline();
    }

    public Component toComponent(Player context) {
        boolean isMod = context != null && (context.hasPermission("modreq.mod") || context.hasPermission("modreq.admin"));
        Component result = Component.empty();

        OfflinePlayer creator = Bukkit.getOfflinePlayer(getCreator());
        OfflinePlayer owner = isClaimed() ? Bukkit.getOfflinePlayer(getOwner()) : null;
        OfflinePlayer responder = getResponder() != null ? Bukkit.getOfflinePlayer(getResponder()) : null;
        Map<String, Component> replacements = new HashMap<>();

        String status;
        String username;
        Component location;

        if (isMod) {
            if (isClosed()) {
                status = Messages.getString("general.CLOSED");
            } else if(isClaimed()) {
                if (owner == null || owner.getName() == null) {
                    status = Messages.getString("general.UNKNOWN-PLAYER");
                } else if (owner.isOnline()) {
                    status = Messages.getString("general.ONLINE-PLAYER", "player", owner.getName());
                } else {
                    status = Messages.getString("general.OFFLINE-PLAYER", "player", owner.getName());
                }
            } else {
                status = Messages.getString("general.OPEN");
            }

            if(isElevated()) {
				status += Messages.getString("general.ELEVATED");
			}
        } else {
            status = Messages.getString("general." + (isClosed() ? "CLOSED": "OPEN"));
        }

        if (creator.getName() != null) {
            if (creator.isOnline()) {
                username = Messages.getString("general.ONLINE-PLAYER","player", creator.getName());
            } else {
                username = Messages.getString("general.OFFLINE-PLAYER", "player", creator.getName());
            }
        } else {
            username = Messages.getString("general.UNKNOWN-PLAYER");
        }

        if(isMod) {
            replacements.put("elevated", isElevated() ? Messages.get("general.ELEVATED") : Component.empty());
            replacements.put("notes", hasNotes() ? Messages.get("general.NOTES") : Component.empty());
            replacements.put("note_count", Component.text(getNotes().size()));
            location = Messages.get("mod.info.LOCATION",
                                    "id",  String.valueOf(id),
                                    "world", getLocation().getWorld().getName(),
                                    "x", String.valueOf(getLocation().getBlockX()),
                                    "y", String.valueOf(getLocation().getBlockY()),
                                    "z", String.valueOf(getLocation().getBlockZ()));
        } else {
            location = Messages.get("player.info.LOCATION",
                                    "id",  String.valueOf(id),
                                    "world", getLocation().getWorld().getName(),
                                    "x", String.valueOf(getLocation().getBlockX()),
                                    "y", String.valueOf(getLocation().getBlockY()),
                                    "z", String.valueOf(getLocation().getBlockZ()));
        }

        replacements.put("id", Component.text(getId()));
        replacements.put("status", new MineDownParser().parse(status).build());
        replacements.put("creator", new MineDownParser().parse(username).build());
        replacements.put("date", Component.text(ModReq.getPlugin().getFormat().format(getCreateTime())));
        replacements.put("message", Component.text(getMessage()));
        replacements.put("location", location);
        replacements.put("world", Component.text(getLocation().getWorld().getName()));
        replacements.put("x", Component.text(getLocation().getBlockX()));
        replacements.put("y", Component.text(getLocation().getBlockY()));
        replacements.put("z", Component.text(getLocation().getBlockZ()));

        if(isMod) {
            result = result.append(Messages.get("mod.info.HEADER", replacements));
            result = result.append(Component.newline());
            result = result.append(Messages.get("mod.info.REQUEST", replacements));
        } else {
            result = result.append(Messages.get("player.info.HEADER", replacements));
            result = result.append(Component.newline());
            result = result.append(Messages.get("player.info.REQUEST", replacements));
        }

        if(isClosed()) {
            replacements.put("response_date", Component.text(ModReq.getPlugin().getFormat().format(getCloseTime())));
            replacements.put("response", Component.text(getResponseMessage()));

            if(responder != null && responder.getName() != null) {
                replacements.put("responder", Component.text(responder.getName()));
            } else {
                replacements.put("responder", Messages.get("general.UNKNOWN-PLAYER"));
            }
        }

        if(isMod) {
            if(isClosed()) {
                result = result.append(Component.newline());
                result = result.append(Messages.get("mod.info.RESPONSE", replacements));
            }

            for(int i = 0; i < notes.size(); i++) {
                Note note = notes.get(i);
                OfflinePlayer noteCreator = Bukkit.getOfflinePlayer(note.getCreator());
                String creatorName;

                if (noteCreator.getName() != null) {
                    creatorName = noteCreator.getName();
                } else {
                    creatorName = Messages.getString("general.UNKNOWN-PLAYER");
                }

                result = result.append(Component.newline());
                result = result.append(Messages.get("mod.info.NOTE",
                                                    "id", String.valueOf(i + 1),
                                                    "creator", creatorName,
                                                    "message", note.getMessage()));
            }

            result = result.append(Component.newline());
            result = result.append(getActions(context));
            result = result.append(Component.newline());
            result = result.append(Messages.get("mod.info.FOOTER", replacements));
        } else {
            if(isClosed()) {
                result = result.append(Component.newline());
                result = result.append(Messages.get("player.info.RESPONSE", replacements));
            }

            result = result.append(Component.newline());
            result = result.append(Messages.get("player.info.FOOTER", replacements));
        }

        return result;
    }

    private Component getActions(Player context) {
        Component actions = Component.newline();

        if(context != null && !context.hasPermission("modreq.mod") && !context.hasPermission("modreq.admin")) {
            return Component.empty();
        }

        if (isClosed()) {
            actions = actions.append(Messages.get("mod.action.OPEN", "id", String.valueOf(id)));
            actions = actions.append(Component.space());
        } else {
            actions = actions.append(Messages.get("mod.action.CLOSE", "id", String.valueOf(id)));
            actions = actions.append(Component.space());
        }

        actions = actions.append(Messages.get("mod.action.TELEPORT", "id", String.valueOf(id)));

        if (!isClosed()) {
            actions = actions.append(Component.space());

            if (isClaimedBy(context.getUniqueId())) {
                actions = actions.append(Messages.get("mod.action.UNCLAIM", "id", String.valueOf(id)));
                actions = actions.append(Component.space());
            } else if (!isClaimed()) {
                actions = actions.append(Messages.get("mod.action.CLAIM", "id", String.valueOf(id)));
                actions = actions.append(Component.space());
            }

            if (isElevated()) {
                actions = actions.append(Messages.get("mod.action.UNELEVATE", "id", String.valueOf(id)));
                actions = actions.append(Component.space());
            } else {
                actions = actions.append(Messages.get("mod.action.ELEVATE", "id", String.valueOf(id)));
                actions = actions.append(Component.space());
            }

            actions = actions.append(Messages.get("mod.action.NOTE", "id", String.valueOf(id)));
        }

        return actions;
    }
}

