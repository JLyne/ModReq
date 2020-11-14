package uk.co.notnull.modreq.util;

import net.kyori.adventure.text.Component;

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

public class Replacer extends de.themoep.minedown.adventure.Replacer {
	public Replacer() {
    }

	@Override
	public Replacer replace(Map<String, ?> replacements) {
        if (replacements != null && !replacements.isEmpty()) {
            Object any = replacements.values().stream().filter(Objects::nonNull).findAny().orElse(null);

            if (any instanceof String) {
                this.replacements().putAll((Map<String, String>) replacements);
            } else if (any instanceof Component) {
                this.componentReplacements().putAll((Map<String, Component>) replacements);
            } else {
                Iterator var3 = replacements.entrySet().iterator();

                while(var3.hasNext()) {
                    Map.Entry<String, ?> entry = (Map.Entry)var3.next();
                    this.replacements().put(entry.getKey(), String.valueOf(entry.getValue()));
                }
            }
        }

        return this;
    }
}
