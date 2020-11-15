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

    public int getId() {
        return this.id;
    }

    public int getRequestId() {
        return this.requestId;
    }

    public UUID getCreator() {
        return this.creator;
    }

    public String getMessage() {
        return this.message;
    }
}
