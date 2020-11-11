package uk.co.notnull.modreq;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Date;
import java.util.UUID;

public class Request {
    private final Location location;
    private final int id;
    private final int done;
    private final boolean elevated;
    private final Date createTime;
    private final Date closeTime;
    private final UUID creator;
    private final String message;
    private final UUID owner;
    private final UUID responder;
    private final String response;

    public Request(int id, UUID creator, String message, Date createTime, Location location) {
        this.id = id;
        this.creator = creator;
        this.message = message;
        this.createTime = createTime;
        this.owner = null;
        this.responder = null;
        this.response = null;
        this.closeTime = null;
        this.done = 0;
        this.elevated = false;
        this.location = location;
    }

    public Request(int id, UUID creator, String message, Date createTime, Location location, UUID owner, UUID responder, String response, Date closeTime, int pDone, boolean elevated) {
        this.id = id;
        this.creator = creator;
        this.message = message;
        this.createTime = createTime;
        this.owner = owner;
        this.responder = responder;
        this.response = response;
        this.closeTime = closeTime;
        this.done = pDone;
        this.elevated = elevated;
        this.location = location;
    }

    @Deprecated
    public Request(int pId, String pUuid, String pRequest, long pTimestamp, String world, int x, int y, int z, String pClaimed, String pMod_uuid, String pMod_comment, long pMod_timestamp, int pDone, int pElevated) {
        this.id = pId;
        this.creator = UUID.fromString(pUuid);
        this.message = pRequest;
        this.createTime = new Date(pTimestamp);
        this.owner = pClaimed.isEmpty() ? null : UUID.fromString(pClaimed);
        this.responder = pMod_uuid.isEmpty() ? null : UUID.fromString(pMod_uuid);
        this.response = pMod_comment;
        this.closeTime = pMod_timestamp == 0 ? null : new Date(pMod_timestamp);
        this.done = pDone;
        this.elevated = pElevated == 1;
        this.location = new Location(Bukkit.getWorld(world), x, y, z);
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
        return this.responder != null ? this.responder.toString() : "";
    }

    public int getDone() {
        return this.done;
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
        return this.closeTime != null ? this.closeTime.getTime() : 0;
    }

    @Deprecated
    public String getRequest() {
        return this.message;
    }

    @Deprecated
    public String getMod_comment() {
        return this.response;
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
        return closeTime;
    }

    public UUID getCreator() {
        return creator;
    }

    public UUID getOwner() {
        return owner;
    }

    public UUID getResponder() {
        return responder;
    }

    public String getResponse() {
        return response;
    }

    public boolean isElevated() { return this.elevated; }

    public boolean isClosed() { return this.done > 0; }

    public boolean isClaimedBy(UUID uuid) { return uuid.equals(owner); }

    public boolean isCreatorOnline() {
        Player player = Bukkit.getPlayer(creator);
        return player != null && player.isOnline();
    }
}

