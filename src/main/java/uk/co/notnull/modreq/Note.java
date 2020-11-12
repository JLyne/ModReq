package uk.co.notnull.modreq;

import java.util.UUID;

public class Note {
    private final int id;
    private final int requestId;
    private final UUID creator;
    private final String message;

    public Note(int id, int requestId, UUID creator, String message) {
        this.id = id;
        this.requestId = requestId;
        this.creator = creator;
        this.message = message;
    }

    @Deprecated
    public Note(int id, int requestId, String uuid, String message) {
        this.id = id;
        this.requestId = requestId;
        this.creator = UUID.fromString(uuid);
        this.message = message;
    }

    public int getId() {
        return this.id;
    }

    @Deprecated
    public int getModreq_id() {
        return this.requestId;
    }

    public int getRequestId() {
        return this.requestId;
    }

    @Deprecated
    public String getUuid() {
        return this.creator.toString();
    }

    @Deprecated
    public String getNote() {
        return this.message;
    }

    public UUID getCreator() {
        return this.creator;
    }

    public String getMessage() {
        return this.message;
    }
}
