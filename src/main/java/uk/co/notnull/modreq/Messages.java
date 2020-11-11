package uk.co.notnull.modreq;

import java.io.File;
import java.io.IOException;

import de.themoep.minedown.adventure.MineDown;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class Messages {
    private static File file;
    private static YamlConfiguration cfg;

    private static void load() {
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
        setDefaultString("error.ID-ERROR", "&cError: ModReq with ID %id does not exist.");
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
        setDefaultString("general.CLAIMED", "&eCLAIMED (%mod)");
        setDefaultString("general.ELEVATED", "&b[ADMIN]");
        setDefaultString("general.NOTES", "&4[NOTES]");
        setDefaultString("general.DONE-MESSAGE", "&7Message: %msg");
        setDefaultString("general.ON-JOIN-HEADER", "&b-=-=- -=-=-");
        setDefaultString("general.HELP-LIST-MODREQS", "&b-=-=- List your last ModReqs: /modreq -=-=-");
        setDefaultString("general.DATE-FORMAT", "MMM.dd.yyyy 'at' HH:mm:ss");
        setDefaultString("general.LANGUAGE-TAG", "en-GB");
        setDefaultString("player.REQUEST-FILED", "&aYour ModReq has been sent to the staff members. Please be patient.");
        setDefaultString("player.DONE", "&2%mod &ahas closed your ModReq (%id).");
        setDefaultString("player.check.1", "&b-=-=- Last %count ModReq(s) -=-=-");
        setDefaultString("player.check.2", "&6%id [&a%status&6] %date");
        setDefaultString("player.check.3", "&7Message: %msg");
        setDefaultString("player.check.4", "&6Answered by &a%mod &6on &a%date&6.");
        setDefaultString("player.check.5", "&7Message: %msg");
        setDefaultString("player.check.6", "&b -=-=- New ModReq: /modreq <request> -=-=-");
        setDefaultString("player.check.NO-MODREQS", "&bYou did not file a ModReq yet. New ModReq: /modreq <request>");
        setDefaultString("mod.NEW-MODREQ", "&4&l[MODREQ] &aNew ModReq. Write &2/check %id &ato get more information.");
        setDefaultString("mod.MODREQS-OPEN", "&2%count &aModReq(s) open. Write &2/check &ato get more information.");
        setDefaultString("mod.TELEPORT", "&aTeleported to &2%id&a.");
        setDefaultString("mod.CLAIM", "&aModReq &2%id &ahas been claimed by &2%mod&a.");
        setDefaultString("mod.UNCLAIM", "&aModReq &2%id &ahas been unclaimed by &2%mod&a.");
        setDefaultString("mod.elevate.1", "&aModReq &2%id &ahas been flagged for ADMIN by &2%mod&a.");
        setDefaultString("mod.elevate.2", "&aADMIN-flag has been removed from ModReq &2%id &aby &2%mod&a.");
        setDefaultString("mod.DONE", "&aModReq &2%id &ahas been closed by &2%mod&a.");
        setDefaultString("mod.REOPEN", "&aModReq &2%id &ahas been reopened by &2%mod&a.");
        setDefaultString("mod.check.special.1", "&bModReq %id - %status");
        setDefaultString("mod.check.special.2", "&eFiled by &a%player &eon &a%date &eat &a%world &e(&a%x %y %z&e).");
        setDefaultString("mod.check.special.3", "&7Message: %msg");
        setDefaultString("mod.check.special.4", "&eAnswered by &a%mod &eon &a%date&e.");
        setDefaultString("mod.check.special.5", "&7Message: %msg");
        setDefaultString("mod.check.special.6", "&4[%id] &7%mod - %msg");
        setDefaultString("mod.check.1", "&b-=-=- %count ModReq(s) -=-=-");
        setDefaultString("mod.check.2", "&6%id &6[&a%status&6] %date &a%player");
        setDefaultString("mod.check.3", "&7Message: %msg");
        setDefaultString("mod.check.4", "&b-=-=- Page %page of %allpages -=-=-");
        setDefaultString("mod.check.NO-MODREQS", "&aNo modreqs open.");
        setDefaultString("mod.note.ADD", "&2%mod &aadded a note to ModReq &2%id&a: &7%msg");
        setDefaultString("mod.note.REMOVE", "&2%mod &aremoved the following note from ModReq &2%id&a: &7%msg");

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

    public static Component get(String pEntry, String ...replacements) {
        if(cfg == null) {
            load();
        }

        if(cfg.getString(pEntry) != null) {
            return new MineDown(cfg.getString(pEntry)).replace(replacements).toComponent();
        } else {
            ModReq.getPlugin().getLogger().warning("Error: Cannot find language string. " + pEntry);
            return Component.empty();
        }
    }

    @Deprecated
    public static String getLangString(String pEntry) {
        if(cfg == null) {
            load();
        }

        if (cfg.getString(pEntry) != null) {
            return ChatColor.translateAlternateColorCodes('&', cfg.getString(pEntry, "Missing language string: " + pEntry));
        } else {
            ModReq.getPlugin().getLogger().warning("Error: Cannot find language string. " + pEntry);
            return "";
        }
    }

    public static void send(Player recipient, String key, String ...replacements) {
        Audience audience = ModReq.getPlugin().getBukkitAudiences().player(recipient);

        if(cfg.getString(key) != null) {
            audience.sendMessage(new MineDown(cfg.getString(key)).replace(replacements).toComponent());
        } else {
            ModReq.getPlugin().getLogger().warning("Error: Cannot find language string. " + key);
        }
    }

    public static void sendToMods(String key, String ...replacements) {
        Component message;

        if(cfg.getString(key) != null) {
            message = new MineDown(cfg.getString(key)).replace(replacements).toComponent();
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
