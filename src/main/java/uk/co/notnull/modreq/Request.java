package uk.co.notnull.modreq;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.util.Collections;

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

    @Deprecated
    public Request(int pId, String pUuid, String pRequest, long pTimestamp, String world, int x, int y, int z, String pClaimed, String pMod_uuid, String pMod_comment, long pMod_timestamp, int pDone, int pElevated) {
        this.id = pId;
        this.creator = UUID.fromString(pUuid);
        this.message = pRequest;
        this.createTime = new Date(pTimestamp);
        this.owner = pClaimed.isEmpty() ? null : UUID.fromString(pClaimed);

        if(!pMod_uuid.isEmpty()) {
            this.response = new Response(pMod_uuid.isEmpty() ? null : UUID.fromString(pMod_uuid),
                                         pMod_comment,
                                         pMod_timestamp == 0 ? null : new Date(pMod_timestamp),
                                         pDone == 2);
        } else {
            this.response = null;
        }

        this.elevated = pElevated == 1;
        this.location = new Location(Bukkit.getWorld(world), x, y, z);
        this.notes = Collections.emptyList();
    }

    public static RequestBuilder.IDStep builder() {
        return RequestBuilder.builder();
    }

    public int getId() {
        return this.id;
    }

    @Deprecated
    public String getUuid() {
        return this.creator.toString();
    }

    @Deprecated
    public String getClaimed() {
        return this.owner != null ? this.owner.toString() : "";
    }

    @Deprecated
    public String getMod_uuid() {
        return hasResponse() ? response.getResponder().toString() : "";
    }

    @Deprecated
    public int getDone() {
        return hasResponse() ? (response.isSeen() ? 2 : 1) : 0;
    }

    @Deprecated
    public int getElevated() {
        return this.elevated ? 1 : 0;
    }

    @Deprecated
    public String getWorld() {
        return this.location.getWorld().getName();
    }

    @Deprecated
    public int getY() {
        return this.location.getBlockY();
    }

    @Deprecated
    public int getX() {
        return this.location.getBlockX();
    }

    @Deprecated
    public int getZ() {
        return this.location.getBlockZ();
    }

    @Deprecated
    public long getTimestamp() {
        return this.createTime.getTime();
    }

    @Deprecated
    public long getMod_timestamp() {
        return hasResponse() ? this.response.getTime().getTime() : 0;
    }

    @Deprecated
    public String getRequest() {
        return this.message;
    }

    @Deprecated
    public String getMod_comment() {
        return hasResponse() ? response.getMessage() : "";
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
}

