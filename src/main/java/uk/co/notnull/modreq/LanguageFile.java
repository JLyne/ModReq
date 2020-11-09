package uk.co.notnull.modreq;

import java.io.File;
import java.io.IOException;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;

public class LanguageFile {
    private File file;
    private YamlConfiguration cfg;

    public LanguageFile() {
        File folder = new File("plugins/ModReq");
        if (!folder.exists()) {
            folder.mkdir();
        }

        this.file = new File("plugins/ModReq/lang.yml");
        if (!this.file.exists()) {
            ModReq.getPlugin().getLogger().info("No language file found. Creating new one...");

            try {
                this.file.createNewFile();
                YamlConfiguration cfg = new YamlConfiguration();
                cfg.save(this.file);
            } catch (IOException var3) {
                var3.printStackTrace();
            }
        }

        this.cfg = YamlConfiguration.loadConfiguration(this.file);
        this.load();
    }

    private void load() {
        this.setDefaultString("error.DATABASE-ERROR", "&cError while connecting to database. Please contact an administrator.");
        this.setDefaultString("error.ID-ERROR", "&cError: ModReq with ID %id does not exist.");
        this.setDefaultString("error.NUMBER-ERROR", "&cError: %id is not a number/ID/page.");
        this.setDefaultString("error.NOT-CLOSED", "&cModReq is not closed.");
        this.setDefaultString("error.ALREADY-CLOSED", "&cModReq already closed.");
        this.setDefaultString("error.ALREADY-CLAIMED", "&cModReq already claimed.");
        this.setDefaultString("error.NOT-CLAIMED", "&cModReq is not claimed.");
        this.setDefaultString("error.OTHER-CLAIMED", "&cModReq has been claimed by someone else.");
        this.setDefaultString("error.TELEPORT-ERROR", "&cError while teleporting to location. Please contact an administrator.");
        this.setDefaultString("error.PAGE-ERROR", "&cError: Page %page does not exist.");
        this.setDefaultString("error.NOTE-DOES-NOT-EXIST", "&cError: This note id does not exist.");
        this.setDefaultString("error.NOTE-OTHER", "&cError: You did not write this note.");
        this.setDefaultString("error.MAX-OPEN-MODREQS", "&cYou cannot open more than &4%max &cModReq(s) at the same time.");
        this.setDefaultString("general.OPEN", "&aOPEN");
        this.setDefaultString("general.CLOSED", "&cCLOSED");
        this.setDefaultString("general.CLAIMED", "&eCLAIMED (%mod)");
        this.setDefaultString("general.ELEVATED", "&b[ADMIN]");
        this.setDefaultString("general.NOTES", "&4[NOTES]");
        this.setDefaultString("general.DONE-MESSAGE", "&7Message: %msg");
        this.setDefaultString("general.ON-JOIN-HEADER", "&b-=-=- -=-=-");
        this.setDefaultString("general.HELP-LIST-MODREQS", "&b-=-=- List your last ModReqs: /modreq -=-=-");
        this.setDefaultString("general.DATE-FORMAT", "MMM.dd.yyyy 'at' HH:mm:ss");
        this.setDefaultString("general.LANGUAGE-TAG", "en-GB");
        this.setDefaultString("player.REQUEST-FILED", "&aYour ModReq has been sent to the staff members. Please be patient.");
        this.setDefaultString("player.DONE", "&2%mod &ahas closed your ModReq (%id).");
        this.setDefaultString("player.check.1", "&b-=-=- Last %count ModReq(s) -=-=-");
        this.setDefaultString("player.check.2", "&6%id [&a%status&6] %date");
        this.setDefaultString("player.check.3", "&7Message: %msg");
        this.setDefaultString("player.check.4", "&6Answered by &a%mod &6on &a%date&6.");
        this.setDefaultString("player.check.5", "&7Message: %msg");
        this.setDefaultString("player.check.6", "&b -=-=- New ModReq: /modreq <request> -=-=-");
        this.setDefaultString("player.check.NO-MODREQS", "&bYou did not file a ModReq yet. New ModReq: /modreq <request>");
        this.setDefaultString("mod.NEW-MODREQ", "&4&l[MODREQ] &aNew ModReq. Write &2/check %id &ato get more information.");
        this.setDefaultString("mod.MODREQS-OPEN", "&2%count &aModReq(s) open. Write &2/check &ato get more information.");
        this.setDefaultString("mod.TELEPORT", "&aTeleported to &2%id&a.");
        this.setDefaultString("mod.CLAIM", "&aModReq &2%id &ahas been claimed by &2%mod&a.");
        this.setDefaultString("mod.UNCLAIM", "&aModReq &2%id &ahas been unclaimed by &2%mod&a.");
        this.setDefaultString("mod.elevate.1", "&aModReq &2%id &ahas been flagged for ADMIN by &2%mod&a.");
        this.setDefaultString("mod.elevate.2", "&aADMIN-flag has been removed from ModReq &2%id &aby &2%mod&a.");
        this.setDefaultString("mod.DONE", "&aModReq &2%id &ahas been closed by &2%mod&a.");
        this.setDefaultString("mod.REOPEN", "&aModReq &2%id &ahas been reopened by &2%mod&a.");
        this.setDefaultString("mod.check.special.1", "&bModReq %id - %status");
        this.setDefaultString("mod.check.special.2", "&eFiled by &a%player &eon &a%date &eat &a%world &e(&a%x %y %z&e).");
        this.setDefaultString("mod.check.special.3", "&7Message: %msg");
        this.setDefaultString("mod.check.special.4", "&eAnswered by &a%mod &eon &a%date&e.");
        this.setDefaultString("mod.check.special.5", "&7Message: %msg");
        this.setDefaultString("mod.check.special.6", "&4[%id] &7%mod - %msg");
        this.setDefaultString("mod.check.1", "&b-=-=- %count ModReq(s) -=-=-");
        this.setDefaultString("mod.check.2", "&6%id &6[&a%status&6] %date &a%player");
        this.setDefaultString("mod.check.3", "&7Message: %msg");
        this.setDefaultString("mod.check.4", "&b-=-=- Page %page of %allpages -=-=-");
        this.setDefaultString("mod.check.NO-MODREQS", "&aNo modreqs open.");
        this.setDefaultString("mod.note.ADD", "&2%mod &aadded a note to ModReq &2%id&a: &7%msg");
        this.setDefaultString("mod.note.REMOVE", "&2%mod &aremoved the following note from ModReq &2%id&a: &7%msg");

        try {
            this.cfg.save(this.file);
        } catch (IOException var2) {
            var2.printStackTrace();
        }

    }

    private void setDefaultString(String pEntry, String pValue) {
        if (this.cfg.getString(pEntry) == null) {
            this.cfg.set(pEntry, pValue);
        }

    }

    public String getLangString(String pEntry) {
        if (this.cfg.getString(pEntry) != null) {
            return ChatColor.translateAlternateColorCodes('&', this.cfg.getString(pEntry));
        } else {
            ModReq.getPlugin().getLogger().warning("Error: Cannot find language string. " + pEntry);
            return "";
        }
    }
}
