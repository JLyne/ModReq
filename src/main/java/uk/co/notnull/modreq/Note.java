package uk.co.notnull.modreq;

public class Note {
    private int id;
    private int modreq_id;
    private String uuid;
    private String note;

    public Note(int pId, int pModreq_id, String pUuid, String pNote) {
        this.id = pId;
        this.modreq_id = pModreq_id;
        this.uuid = pUuid;
        this.note = pNote;
    }

    public int getId() {
        return this.id;
    }

    public int getModreq_id() {
        return this.modreq_id;
    }

    public String getUuid() {
        return this.uuid;
    }

    public String getNote() {
        return this.note;
    }
}
