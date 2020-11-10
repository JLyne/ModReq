package uk.co.notnull.modreq;

import org.bukkit.Bukkit;
import org.bukkit.Location;

public class Request {
    private final Location location;
    private int id;
    private int x;
    private int y;
    private int z;
    private int done;
    private int elevated;
    private long timestamp;
    private long mod_timestamp;
    private String uuid;
    private String request;
    private String world;
    private String claimed;
    private String mod_uuid;
    private String mod_comment;

    public Request(int pId, String pUuid, String pRequest, long pTimestamp, String pWorld, int pX, int pY, int pZ, String pClaimed, String pMod_uuid, String pMod_comment, long pMod_timestamp, int pDone, int pElevated) {
        this.id = pId;
        this.uuid = pUuid;
        this.request = pRequest;
        this.timestamp = pTimestamp;
        this.world = pWorld;
        this.x = pX;
        this.y = pY;
        this.z = pZ;
        this.claimed = pClaimed;
        this.mod_uuid = pMod_uuid;
        this.mod_comment = pMod_comment;
        this.mod_timestamp = pMod_timestamp;
        this.done = pDone;
        this.elevated = pElevated;
        this.location = new Location(Bukkit.getWorld(world), x, y, z);
    }

    public int getId() {
        return this.id;
    }

    public String getUuid() {
        return this.uuid;
    }

    public String getClaimed() {
        return this.claimed;
    }

    public String getMod_uuid() {
        return this.mod_uuid;
    }

    public int getDone() {
        return this.done;
    }

    public int getElevated() {
        return this.elevated;
    }

    public String getWorld() {
        return this.world;
    }

    public int getY() {
        return this.y;
    }

    public int getX() {
        return this.x;
    }

    public int getZ() {
        return this.z;
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public long getMod_timestamp() {
        return this.mod_timestamp;
    }

    public String getRequest() {
        return this.request;
    }

    public String getMod_comment() {
        return this.mod_comment;
    }

    public Location getLocation() {
        return location;
    }
}

