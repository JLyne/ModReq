package uk.co.notnull.modreq.util;

public class MineDown extends de.themoep.minedown.adventure.MineDown {
	private final Replacer replacer = new Replacer();

	public MineDown(String message) {
		super(message);
	}

	@Override
	public Replacer replacer() {
        return this.replacer;
    }
}
