package uk.co.notnull.modreq;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import de.themoep.minedown.adventure.MineDownParser;
import de.themoep.minedown.adventure.Replacer;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
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

        setDefaultString("error.DATABASE-ERROR", "[Error while connecting to database. Please contact an administrator.](red)");
        setDefaultString("error.ID-ERROR", "[Error: ModReq #%id does not exist.](red)");
        setDefaultString("error.NUMBER-ERROR", "[Error: %id is not a number/ID/page.](red)");
        setDefaultString("error.NOT-CLOSED", "[ModReq is not closed.](red)");
        setDefaultString("error.ALREADY-CLOSED", "[ModReq already closed.](red)");
        setDefaultString("error.ALREADY-CLAIMED", "[ModReq already claimed.](red)");
        setDefaultString("error.NOT-CLAIMED", "[ModReq is not claimed.](red)");
        setDefaultString("error.OTHER-CLAIMED", "[ModReq has been claimed by someone else.](red)");
        setDefaultString("error.TELEPORT-ERROR", "[Error while teleporting to location. Please contact an administrator.](red)");
        setDefaultString("error.PAGE-ERROR", "[Error: Page %page does not exist.](red)");
        setDefaultString("error.NOTE-DOES-NOT-EXIST", "[Error: This note id does not exist.](red)");
        setDefaultString("error.NOTE-OTHER", "[Error: You did not write this note.](red)");
        setDefaultString("error.MAX-OPEN-MODREQS", "[You cannot open more than](red) [%max](dark_red) [ModReq(s) at the same time.](red)");

        setDefaultString("general.PREFIX", "[\\[MODREQ\\]](color=red)");
        setDefaultString("general.OPEN", "[OPEN](green)");
        setDefaultString("general.CLOSED", "[CLOSED](red)");
        setDefaultString("general.CLAIMED", "[CLAIMED \\(%actor\\)](gold)");
        setDefaultString("general.ELEVATED", "[\\[ADMIN\\]](aqua)");
        setDefaultString("general.NOTES", "[\\[NOTES\\]](dark_red)");
        setDefaultString("general.ON-JOIN-HEADER", "[-=-=- -=-=-](aqua)");
        setDefaultString("general.HELP-LIST-MODREQS", "[-=-=- List your last ModReqs: /modreq -=-=-](aqua)");
        setDefaultString("general.DATE-FORMAT", "MMM.dd.yyyy, HH:mm:ss");
        setDefaultString("general.LANGUAGE-TAG", "en-GB");
        setDefaultString("general.ONLINE-PLAYER", "[%player](suggest_command=/w %player  show_text=Click to whisper %player color=green)");
        setDefaultString("general.OFFLINE-PLAYER", "[%player](show_text=%player is offline color=red)");
        setDefaultString("general.UNKNOWN-PLAYER", "[Unknown](show_text=Unknown player color=red)");
        setDefaultString("general.REQUEST-LINK", "[#%id](#07a0ff)");

        setDefaultString("player.notification.CLOSED", "%actor [has closed your ModReq](green) (%id).\n[Message: %response](gray)");
        setDefaultString("player.notification.CREATED", "[Your ModReq has been sent to the staff members. Please be patient.](green)");

        setDefaultString("player.list.HEADER", "[----------](color=aqua format=bold) Your ModReqs [----------](color=aqua format=bold)");
        setDefaultString("player.list.ITEM-REQUEST", "%id [\\[](#fce469)%status[\\]](#fce469) [%date](#fce469)\n[Message: %message](gray)");
        setDefaultString("player.list.ITEM-RESPONSE", "[Answered by %responder on](#fce469) [%close_time](green)[.](#fce469)\n[Message: %response](gray)");
        setDefaultString("player.list.FOOTER", "");
        setDefaultString("player.list.NO-RESULTS", "[You have no open ModReqs. New ModReq: /modreq <request>](aqua)");

        setDefaultString("player.info.HEADER", "[----------](color=aqua format=bold) #%id - %status [----------](color=aqua format=bold)");
        setDefaultString("player.info.REQUEST", "%world (%x %y %z)");
        setDefaultString("player.info.REQUEST", "&eCreated on %date at [%location](green).\n[Message: %message](gray)");
        setDefaultString("player.info.RESPONSE", "[Answered by](yellow) [%responder](green) [on](yellow) [%response_date](green)\n[Message: %response](gray)");
        setDefaultString("player.info.LOCATION", "%world (%x %y %z)");
        setDefaultString("player.info.FOOTER", "");

        setDefaultString("mod.notification.CREATED", "%prefix %actor [created a new ModReq](green) (%link). %view");
        setDefaultString("mod.notification.CLAIMED", "%prefix %link [has been claimed by](green) %actor. %view");
        setDefaultString("mod.notification.UNCLAIMED", "%prefix %link [has been un-claimed by](green) %actor. %view");
        setDefaultString("mod.notification.ELEVATED", "%prefix %link [has been flagged for admin attention by](green) %actor. %view");
        setDefaultString("mod.notification.UNELEVATED", "%prefix [Admin flag has been removed from](green) %link [by](green) %actor. %view");
        setDefaultString("mod.notification.CLOSED", "%prefix %link has been closed by %actor.\n[Message: %message](gray)\n%view");
        setDefaultString("mod.notification.REOPENED", "%prefix %link [has been re-opened by](green) %actor. %view");
        setDefaultString("mod.notification.TELEPORTED", "%prefix [Teleported to](green) %link.");
        setDefaultString("mod.notification.NOTE-ADDED", "%prefix %actor [added a note to](green) %link. %view\n[Message: %message](gray)");
        setDefaultString("mod.notification.NOTE-REMOVED", "%prefix %actor [removed a note from](green) %link. %view\n[Message: %message](gray)");

        setDefaultString("mod.info.HEADER", "[----------](color=aqua format=bold) #%id - %status [----------](color=aqua format=bold)");
        setDefaultString("mod.info.REQUEST", "&e[#%id - %status](aqua)\nCreated by [%creator](green) on %date at [%location](green).\n[Message: %message](gray)");
        setDefaultString("mod.info.RESPONSE", "[Answered by](yellow) [%responder](green) [on](yellow) [%response_date](green)\n[Message: %response](gray)");
        setDefaultString("mod.info.LOCATION", "[%world (%x %y %z)](show_text=Click to teleport run_command=/mr tp %id)");
        setDefaultString("mod.info.NOTE", "[\\[%id\\]](aqua) [%creator - %message](gray)");
        setDefaultString("mod.info.FOOTER", "");

        setDefaultString("mod.list.HEADER", "[----------](color=aqua format=bold) %count ModReq(s) [----------](color=aqua format=bold)");
        setDefaultString("mod.list.ITEM", "%link [\\[](#fce469)[%status%elevated%notes](green)[\\]](#fce469) [%date](#fce469) %creator\n[Message: %message](gray)");
        setDefaultString("mod.list.FOOTER", "[-=-=- Page %page of %allpages -=-=-](aqua)");
        setDefaultString("mod.list.NO-RESULTS", "[There are no open ModReqs.](green)");

        setDefaultString("mod.action.VIEW","[\\[View\\]](run_command=/mr info %id show_text=Show the details of this ModReq color=gold)");
        setDefaultString("mod.action.CLOSE","[\\[Close\\]](suggest_command=/mr close %id  show_text=Close this ModReq color=red)");
        setDefaultString("mod.action.OPEN","[\\[Re-open\\]](suggest_command=/mr open %id show_text=Re-open this ModReq color=red)");
        setDefaultString("mod.action.TELEPORT","[\\[Teleport\\]](run_command=/mr tp %id show_text=Teleport to where this ModReq was created color=gold)");
        setDefaultString("mod.action.CLAIM","[\\[Claim\\]](run_command=/mr claim %id show_text=Claim this ModReq to indicate you are working on it color=gold)");
        setDefaultString("mod.action.UNCLAIM","[\\[Un-claim\\]](run_command=/mr unclaim %id show_text=Un-claim this ModReq to allow other mods to work on it color=gold)");
        setDefaultString("mod.action.ELEVATE","[\\[Elevate\\]](run_command=/mr elevate %id show_text=Flag this ModReq for admin attention color=gold)");
        setDefaultString("mod.action.UNELEVATE","[\\[Un-elevate\\]](run_command=/mr elevate %id show_text=Remove the admin flag from this ModReq color=gold)");
        setDefaultString("mod.action.NOTE","[\\[Add Note\\]](suggest_command=/mr note add %id  show_text=Add a note to this ModReq. Notes are only visible to staff. color=gold)");

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
     * @param action The action to notify about
     * @param actor The player who performed the action
     * @param request The request that was acted on
     * @param replacements A list of placeholders and their replacements
     */
    public static void sendModNotification(NotificationType action, OfflinePlayer actor, Request request, String ...replacements) {
        Component message;
        String key = "mod.notification." + action.toString();

        if(cfg.getString(key) != null) {
            String username;

            if(actor.getName() != null) {
                if (actor.isOnline()) {
                    username = Messages.getString("general.ONLINE-PLAYER","player", actor.getName());
                } else {
                    username = Messages.getString("general.OFFLINE-PLAYER", "player", actor.getName());
                }
            } else {
                username = Messages.getString("general.UNKNOWN-PLAYER");
            }

            message = new MineDown(cfg.getString(key))
                    .placeholderPrefix("%").placeholderSuffix("")
                    .replace("id", String.valueOf(request.getId()))
                    .replace("actor", new MineDownParser().parse(username).build())
                    .replace("prefix", Messages.get("general.PREFIX"))
                    .replace("link", Messages.get("general.REQUEST-LINK",
                                                  "id", String.valueOf(request.getId())))
                    .replace("view", Messages.get("mod.action.VIEW",
                                                        "id", String.valueOf(request.getId())))
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
