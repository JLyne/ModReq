package uk.co.notnull.modreq;

public enum NotificationType {
	CREATED(true),
	CLOSED(true),
	REOPENED(true),
	CLAIMED(false),
	UNCLAIMED(false),
	ELEVATED(false),
	UNELEVATED(false),
	NOTE_ADDED(false),
	NOTE_REMOVED(false);

	private final boolean sendToCreator;

	private NotificationType(boolean sendToCreator) {
		this.sendToCreator = sendToCreator;
	}

	public boolean sendToCreator() {
		return sendToCreator;
	}
}
