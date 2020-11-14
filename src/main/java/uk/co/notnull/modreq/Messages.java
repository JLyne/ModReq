package uk.co.notnull.modreq;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import de.themoep.minedown.adventure.Replacer;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import uk.co.notnull.modreq.util.MineDown;

public class Messages {
    private static File file;
    private static YamlConfiguration cfg;

    public static void reload() {
        File folder = new File("plugins/ModReq");
        if (!folder.exists()) {
            folder.mkdir();
        }

        file = new File("plugins/ModReq/lang.yml");
        if (!file.exists()) {
            ModReq.getPlugin().getLogger().info("No language file found. Creating new one...");

            try {
                file.createNewFile();
                YamlConfiguration cfg = new YamlConfiguration();
                cfg.save(file);
            } catch (IOException var3) {
                var3.printStackTrace();
            }
        }

        cfg = YamlConfiguration.loadConfiguration(file);

        setDefaultString("error.DATABASE-ERROR", "&cError while connecting to database. Please contact an administrator.");
        setDefaultString("error.ID-ERROR", "&cError: ModReq #%id does not exist.");
        setDefaultString("error.NUMBER-ERROR", "&cError: %id is not a number/ID/page.");
        setDefaultString("error.NOT-CLOSED", "&cModReq is not closed.");
        setDefaultString("error.ALREADY-CLOSED", "&cModReq already closed.");
        setDefaultString("error.ALREADY-CLAIMED", "&cModReq already claimed.");
        setDefaultString("error.NOT-CLAIMED", "&cModReq is not claimed.");
        setDefaultString("error.OTHER-CLAIMED", "&cModReq has been claimed by someone else.");
        setDefaultString("error.TELEPORT-ERROR", "&cError while teleporting to location. Please contact an administrator.");
        setDefaultString("error.PAGE-ERROR", "&cError: Page %page does not exist.");
        setDefaultString("error.NOTE-DOES-NOT-EXIST", "&cError: This note id does not exist.");
        setDefaultString("error.NOTE-OTHER", "&cError: You did not write this note.");
        setDefaultString("error.MAX-OPEN-MODREQS", "&cYou cannot open more than &4%max &cModReq(s) at the same time.");

        setDefaultString("general.OPEN", "&aOPEN");
        setDefaultString("general.CLOSED", "&cCLOSED");
        setDefaultString("general.CLAIMED", "&eCLAIMED (%actor)");
        setDefaultString("general.ELEVATED", " &b\\[ADMIN\\]");
        setDefaultString("general.NOTES", " &4\\[NOTES\\]");
        setDefaultString("general.DONE-MESSAGE", "&7Message: %message");
        setDefaultString("general.ON-JOIN-HEADER", "&b-=-=- -=-=-");
        setDefaultString("general.HELP-LIST-MODREQS", "&b-=-=- List your last ModReqs: /modreq -=-=-");
        setDefaultString("general.DATE-FORMAT", "MMM.dd.yyyy 'at' HH:mm:ss");
        setDefaultString("general.LANGUAGE-TAG", "en-GB");
        setDefaultString("general.ONLINE-PLAYER", "&a%player");
        setDefaultString("general.OFFLINE-PLAYER", "&c%player");
        setDefaultString("general.UNKNOWN-PLAYER", "&cUnknown");

        setDefaultString("player.notification.CLOSED", "&2%actor &ahas closed your ModReq (%id).\n&7Message: %message");
        setDefaultString("player.notification.CREATED", "&aYour ModReq has been sent to the staff members. Please be patient.");

        setDefaultString("player.list.HEADER", "&b-=-=- Last %count ModReq(s) -=-=-");
        setDefaultString("player.list.ITEM-REQUEST", "&6%id \\[&a%status&6\\] %date\n&7Message: %message");
        setDefaultString("player.list.ITEM-RESPONSE", "&6Answered by &a%responder &6on &a%close_time&6.\n&7Message: %response");
        setDefaultString("player.list.FOOTER", "&b -=-=- New ModReq: /modreq <request> -=-=-");
        setDefaultString("player.list.NO-RESULTS", "&bYou have no open ModReqs. New ModReq: /modreq <request>");

        setDefaultString("mod.notification.CREATED", "&4&l[MODREQ] &aNew ModReq. Write &2/check %id &ato get more information.");
        setDefaultString("mod.notification.CLAIMED", "&aModReq &2#%id &ahas been claimed by &2%actor&a.");
        setDefaultString("mod.notification.UNCLAIMED", "&aModReq &2#%id &ahas been un-claimed by &2%actor&a.");
        setDefaultString("mod.notification.ELEVATED", "&aModReq &2#%id &ahas been flagged for admin attention by &2%actor&a.");
        setDefaultString("mod.notification.UNELEVATED", "&aAdmin flag has been removed from ModReq &2#%id &aby &2%actor&a.");
        setDefaultString("mod.notification.CLOSED", "&aModReq &2#%id &ahas been closed by &2%actor&a.");
        setDefaultString("mod.notification.REOPENED", "&aModReq &2#%id &ahas been re-opened by &2%actor&a.");
        setDefaultString("mod.notification.TELEPORTED", "&aTeleported to &2#%id&a.");
        setDefaultString("mod.notification.NOTE-ADDED", "&2%actor &aadded a note to ModReq &2#%id&a:\n&7%message");
        setDefaultString("mod.notification.NOTE-REMOVED", "&2%actor &aremoved a note from ModReq &2#%id&a:\n&7%message");

        setDefaultString("mod.info.REQUEST", "&e[ModReq %id - %status](aqua)\nFiled by [%creator](green) on %date at [%location](green).\n[Message: %message](gray)");
        setDefaultString("mod.info.RESPONSE", "[Answered by](yellow) [%responder](green) [on](yellow) [%response_date](green)\n[Message: %response](gray)");
        setDefaultString("mod.info.LOCATION", "[%world (%x %y %z)](show_text=Click to teleport run_command=/mr tp %id)");
        setDefaultString("mod.info.NOTE", "&4\\[%id\\] &7%actor - %message");

        setDefaultString("mod.list.HEADER", "&b-=-=- %count ModReq(s) -=-=-");
        setDefaultString("mod.list.ITEM", "&6%id [&a%status%elevated%notes&6] %date &a%creator\n[Message: %message](gray)");
        setDefaultString("mod.list.FOOTER", "&b-=-=- Page %page of %allpages -=-=-");
        setDefaultString("mod.list.NO-RESULTS", "&aThere are no open ModReqs.");

        setDefaultString("mod.action.CLOSE","[\\[Close\\]](suggest_command=/mr close %id  show_text=Close this ModReq)");
        setDefaultString("mod.action.OPEN","[\\[Re-open\\]](suggest_command=/mr open %id show_text=Re-open this ModReq)");
        setDefaultString("mod.action.TELEPORT","[\\[Teleport\\]](run_command=/mr tp %id show_text=Teleport to where this ModReq was created)");
        setDefaultString("mod.action.CLAIM","[\\[Claim\\]](run_command=/mr claim %id show_text=Claim this ModReq to indicate you are working on it)");
        setDefaultString("mod.action.UNCLAIM","[\\[Un-claim\\]](run_command=/mr unclaim %id show_text=Un-claim this ModReq to allow other mods to work on it)");
        setDefaultString("mod.action.ELEVATE","[\\[Elevate\\]](run_command=/mr elevate %id show_text=Flag this ModReq for admin attention)");
        setDefaultString("mod.action.UNELEVATE","[\\[Un-elevate\\]](run_command=/mr elevate %id show_text=Remove the admin flag from this ModReq)");
        setDefaultString("mod.action.NOTE","[\\[Add Note\\]](suggest_command=/mr note add %id  show_text=Add a note to this ModReq. Notes are only visible to staff.)");

        try {
            cfg.save(file);
        } catch (IOException var2) {
            var2.printStackTrace();
        }
    }

    private static void setDefaultString(String pEntry, String pValue) {
        if (cfg.getString(pEntry) == null) {
            cfg.set(pEntry, pValue);
        }
    }

    /**
     * Returns the specified language string, parsed into a component
     * @param key The key for the language string
     * @param replacements A list of placeholders and their replacements
     */
    public static Component get(String key, String ...replacements) {
        if(cfg == null) {
            reload();
        }

        if(cfg.getString(key) != null) {
            return new MineDown(cfg.getString(key))
                    .placeholderPrefix("%").placeholderSuffix("")
                    .replace(replacements).toComponent();
        } else {
            ModReq.getPlugin().getLogger().warning("Error: Cannot find language string. " + key);
            return Component.empty();
        }
    }

    /**
     * Returns the specified language string, parsed into a component
     * @param key The key for the language string
     * @param replacements A list of placeholders and their replacements
     */
    public static Component get(String key, Map<String, ?> replacements) {
        if(cfg == null) {
            reload();
        }

        if(cfg.getString(key) != null) {
            return new MineDown(cfg.getString(key))
                    .placeholderPrefix("%").placeholderSuffix("")
                    .replace(replacements).toComponent();
        } else {
            ModReq.getPlugin().getLogger().warning("Error: Cannot find language string. " + key);
            return Component.empty();
        }
    }

    /**
     * Returns the specified language string
     * @param key The key for the language string
     * @param replacements A list of placeholders and their replacements
     */
    public static String getString(String key, String ...replacements) {
        if(cfg == null) {
            reload();
        }

        if(cfg.getString(key) != null) {
            return new Replacer().placeholderPrefix("%").placeholderSuffix("")
                    .replace(replacements).replaceIn(cfg.getString(key));
        } else {
            ModReq.getPlugin().getLogger().warning("Error: Cannot find language string. " + key);
            return "";
        }
    }

    @Deprecated
    public static String getLangString(String pEntry) {
        if(cfg == null) {
            reload();
        }

        if (cfg.getString(pEntry) != null) {
            return ChatColor.translateAlternateColorCodes('&', cfg.getString(pEntry, "Missing language string: " + pEntry));
        } else {
            ModReq.getPlugin().getLogger().warning("Error: Cannot find language string. " + pEntry);
            return "";
        }
    }

    /**
     * Send a language string to the specified player
     * @param recipient The player to send the message to
     * @param key The key for the language string to send
     * @param replacements A list of placeholders and their replacements
     */
    public static void send(Player recipient, String key, String ...replacements) {
        Audience audience = ModReq.getPlugin().getBukkitAudiences().player(recipient);

        if(cfg.getString(key) != null) {
            audience.sendMessage(new MineDown(cfg.getString(key))
                                         .placeholderPrefix("%").placeholderSuffix("")
                                         .replace(replacements).toComponent());
        } else {
            ModReq.getPlugin().getLogger().warning("Error: Cannot find language string. " + key);
        }
    }

    /**
     * Send a previously created component to the specified player
     * @param recipient The player to send the message to
     * @param message The component message to send
     */
    public static void send(Player recipient, Component message) {
        Audience audience = ModReq.getPlugin().getBukkitAudiences().player(recipient);

        audience.sendMessage(message);
    }

    /**
     * Send a language string to all online players with mod permissions
     * @param key The key for the language string to send
     * @param replacements A list of placeholders and their replacements
     */
    public static void sendToMods(String key, String ...replacements) {
        Component message;

        if(cfg.getString(key) != null) {
            message = new MineDown(cfg.getString(key))
                    .placeholderPrefix("%").placeholderSuffix("")
                    .replace(replacements).toComponent();
        } else {
            ModReq.getPlugin().getLogger().warning("Error: Cannot find language string. " + key);
            return;
        }

        for(Player player : Bukkit.getOnlinePlayers()) {
            if(player.hasPermission("modreq.mod")) {
                Audience audience = ModReq.getPlugin().getBukkitAudiences().player(player);
                audience.sendMessage(message);
            }
        }
    }
}
