/*
 * ModReq
 * Copyright (C) 2021 James Lyne
 *
 * Based on ModReq 1.2 (https://www.spigotmc.org/resources/modreq.57560/)
 * Copyright (C) 2019 Aladram and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package uk.co.notnull.modreq;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Map;

import de.themoep.minedown.adventure.MineDown;
import de.themoep.minedown.adventure.MineDownParser;
import de.themoep.minedown.adventure.Replacer;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

        setDefaultString("error.DATABASE-ERROR", "%prefix% [An error has occurred. please contact an administrator](red)");
        setDefaultString("error.ID-ERROR", "%prefix% [ModReq #%id% does not exist](red)");
        setDefaultString("error.NUMBER-ERROR", "%prefix% [%id% is not a valid number](red)");
        setDefaultString("error.NOT-CLOSED", "%prefix% [ModReq is not closed](red)");
        setDefaultString("error.ALREADY-CLOSED", "%prefix% [ModReq is already closed](red)");
        setDefaultString("error.ALREADY-CLAIMED", "%prefix% [ModReq is already claimed](red)");
        setDefaultString("error.NOT-CLAIMED", "%prefix% [ModReq is not claimed](red)");
        setDefaultString("error.OTHER-CLAIMED", "%prefix% [ModReq has been claimed by someone else](red)");
        setDefaultString("error.TELEPORT-ERROR", "%prefix% [An error has occurred while teleporting, please contact an administrator.](red)");
        setDefaultString("error.TELEPORT-UNLOADED", "%prefix% [The world this ModReq was created in is not loaded.](red)");
        setDefaultString("error.PAGE-ERROR", "%prefix% [Page %page% does not exist](red)");
        setDefaultString("error.COMMENT-DOES-NOT-EXIST", "%prefix% [This comment does not exist.](red)");
        setDefaultString("error.COMMENT-OTHER", "%prefix% [You did not create this comment.](red)");
        setDefaultString("error.NOT-A-COMMENT", "%prefix% [You may only delete comments, not other activity.](red)");
        setDefaultString("error.MAX-OPEN-MODREQS", "%prefix% [You cannot open more than](red) [%max%](dark_red) [ModReq(s) at the same time.](red)");
        setDefaultString("error.NO-PREVIOUS-SEARCH", "%prefix% [You have no previous search.](red) [\\[Start new Search\\]](suggest_command=/mr search  show_text=Start a new search color=gold)");
        setDefaultString("error.NO-PERMISSION", "%prefix% [You do not have permission to do this.](red)");
        setDefaultString("error.REOPEN-INSTEAD", "%prefix% [You cannot comment on closed ModReqs. Would you like to reopen the ModReq instead?](red)\n[\\[Reopen\\]](run_command=/mr open %id% %message% show_text=Reopen this ModReq color=gold)");

        setDefaultString("general.PREFIX", "[ModReq >](color=red format=bold)");
        setDefaultString("general.OPEN", "[OPEN](green)");
        setDefaultString("general.CLOSED", "[CLOSED](red)");
        setDefaultString("general.CLAIMED", "[CLAIMED \\(%player%\\)](gold)");
        setDefaultString("general.ELEVATED", " [\\[ADMIN\\]](show_text=This ModReq has been flagged for admin attention color=aqua)");
        setDefaultString("general.UNREAD", " [\\[UNREAD\\]](run_command=/mr info %id% show_text=This ModReq has activity you have not yet seen color=aqua)");
        setDefaultString("general.DATE-FORMAT", "MMM.dd.yyyy, HH:mm:ss");
        setDefaultString("general.LANGUAGE-TAG", "en-GB");
        setDefaultString("general.ONLINE-PLAYER", "[%player%](suggest_command=/w %player%  show_text=Click to whisper %player% color=green)");
        setDefaultString("general.OFFLINE-PLAYER", "[%player%](show_text=%player% is offline color=red)");
        setDefaultString("general.YOU", "[You](show_text=You are online, believe it or not color=green)");
        setDefaultString("general.UNKNOWN-PLAYER", "[Unknown](show_text=Unknown player color=red)");
        setDefaultString("general.UNKNOWN-WORLD", "[Unknown world](show_text=The world this ModReq was created in is not loaded)");
        setDefaultString("general.UNKNOWN-DATE", "[Unknown Date](show_text=This update was created in a previous version of ModReq which lacks date information color=red)");
        setDefaultString("general.REQUEST-LINK", "[#%id%](run_command=/mr info %id% show_text=Click to show the details of #%id% color=#07a0ff)");

        setDefaultString("pagination.requests.PREV", "[\\[< Prev\\]](run_command=%command% show_text=Previous page color=gray)");
        setDefaultString("pagination.requests.NEXT", "[\\[Next >\\]](run_command=%command% show_text=Next page color=gray)");
        setDefaultString("pagination.requests.DISABLED", "         ");

        setDefaultString("pagination.activity.PREV", "[\\[< Older Activity\\]](run_command=%command% show_text=View older activity on this ModReq color=gray)");
        setDefaultString("pagination.activity.NEXT", "[\\[Newer Activity >\\]](run_command=%command% show_text=View more recent activity on this ModReq color=gray)");
        setDefaultString("pagination.activity.DISABLED", "                     ");

        setDefaultString("confirmation.reopen", "%prefix% [Please confirm you wish to reopen the modreq.\nWe request you only do this if you still require support.](#fce469)\n[\\[Confirm\\]](run_command=/mr confirm show_text=Confirm the ModReq should be reopened color=gold)");
        setDefaultString("confirmation.generic", "%prefix% [Confirmation required](red) [\\[Confirm\\]](run_command=/mr confirm show_text=Click to confirm the command you just entered color=gold)");
        setDefaultString("confirmation.nothing", "%prefix% [You don't have any pending commands.](red)");

        setDefaultString("player.notification.JOIN-MULTIPLE", "%prefix% [New activity on](green) [%count%](#07a0ff) [of your ModReq(s)](green) [\\[View all\\]](run_command=/mr me show_text=List all ModReqs you have created color=gold)");
        setDefaultString("player.notification.JOIN", "%prefix% [New activity on your ModReq](green) %link% %view%");
        setDefaultString("player.notification.CLOSED", "%prefix% %actor% [has closed your ModReq](green) %link%\n[Message: %message%](gray)\n%view%");
        setDefaultString("player.notification.REOPENED", "%prefix% %actor% [has reopened your ModReq](green) %link%\n[Message: %message%](gray)\n%view%");
        setDefaultString("player.notification.COMMENT-ADDED", "%prefix% %actor% [commented on your ModReq](gray) %link%\n[Message: %message%](gray)\n%view%");

        setDefaultString("player.list.HEADER", "%prefix% Your ModReqs [\\[Create\\]](suggest_command=/modreq  show_text=Create a new ModReq color=gold)");
        setDefaultString("player.list.ITEM", "%link% [\\[](#fce469)%status%[\\]](#fce469) [%date%](#fce469) %view%\n%last_update%[Message: %message%](gray)\n");
        setDefaultString("player.list.ITEM-LAST-UPDATE", "[Updated by %update_actor% on](#fce469) [%update_time%](green)\n");
        setDefaultString("player.list.PAGINATION", "[%prev% %next%](white)");
        setDefaultString("player.list.NO-RESULTS", "[You have no open ModReqs. New ModReq: /modreq <request>](aqua)");

        setDefaultString("player.info.HEADER", "[----------](color=aqua format=bold) [#%id%](#07a0ff) - %status% [----------](color=aqua format=bold)");
        setDefaultString("player.info.LOCATION", "[%world% (%x% %y% %z%)](green)");
        setDefaultString("player.info.UNKNOWN-LOCATION", "[Unknown location](show_text=This ModReq was created in a world that is not currently loaded)");
        setDefaultString("player.info.REQUEST", "[Created on %date% at %location%](#fce469)\n[Message: %message%](gray)");

        setDefaultString("player.activity.HEADER", "[------](color=aqua format=bold) Activity - Page %page% of %allpages% [------](color=aqua format=bold)");
        setDefaultString("player.activity.COMMENT", "[\\[%id%\\]](aqua) [%date% - %actor% commented](color=#fce469 format=italic)\n[%message%](gray)");
        setDefaultString("player.activity.CLOSE", "[\\[%id%\\]](aqua) [%date% - %actor% closed](color=#fce469 format=italic)\n[%message%](gray)");
        setDefaultString("player.activity.REOPEN", "[\\[%id%\\]](aqua) [%date% - %actor% reopened](color=#fce469 format=italic)\n[%message%](gray)");
        setDefaultString("player.activity.CREATE", "[\\[%id%\\]](aqua) [%date% - %actor% created](color=#fce469 format=italic)\n[%message%](gray)");
        setDefaultString("player.activity.PAGINATION", "[%prev% %next%](white)");

        setDefaultString("mod.notification.JOIN", "%prefix% [%count%](#07a0ff) [ModReq(s) open](green) [\\[View\\]](run_command=/mr list show_text=View all open modreqs color=gold)");
        setDefaultString("mod.notification.CREATED", "%prefix% %actor% [created a new ModReq (%link%)](green) %view%");
        setDefaultString("mod.notification.CLAIMED", "%prefix% %link% [has been claimed by](gray) %actor% %view%");
        setDefaultString("mod.notification.UNCLAIMED", "%prefix% %link% [has been un-claimed by](gray) %actor% %view%");
        setDefaultString("mod.notification.ELEVATED", "%prefix% %link% [has been flagged for admin attention by](gray) %actor% %view%");
        setDefaultString("mod.notification.UNELEVATED", "%prefix% [Admin flag has been removed from](gray) %link% [by](gray) %actor% %view%");
        setDefaultString("mod.notification.CLOSED", "%prefix% %link% [has been closed by](gray) %actor% %view%\n[Message: %message%](gray)");
        setDefaultString("mod.notification.REOPENED", "%prefix% %link% [has been reopened by](gray) %actor% %view%\n[Message: %message%](gray)");
        setDefaultString("mod.notification.COMMENT-ADDED", "%prefix% %actor% [commented on](gray) %link% %view%\n[Message: %message%](gray)");
        setDefaultString("mod.notification.PRIVATE-COMMENT-ADDED", "%prefix% %actor% [commented privately on](gray) %link% %view%\n[Message: %message%](gray)");
        setDefaultString("mod.notification.COMMENT-REMOVED", "%prefix% %actor% [removed a comment from](gray) %link% %view%\n[Message: %message%](gray)");

        setDefaultString("confirmation.CREATED", """
        %prefix% [Your ModReq (%link%) has been created and sent to staff members. Please be patient.](green) %view%
        [Have more information to add?](#fce469) %comment%
        [No longer require support?](#fce469) %close%""");
        setDefaultString("confirmation.CLAIMED", "%prefix% %link% [has been claimed](green) %view%");
        setDefaultString("confirmation.UNCLAIMED", "%prefix% %link% [has been un-claimed](green) %view%");
        setDefaultString("confirmation.ELEVATED", "%prefix% %link% [has been flagged for admin attention](green) %view%");
        setDefaultString("confirmation.UNELEVATED", "%prefix% [Admin flag has been removed from](green) %link% %view%");
        setDefaultString("confirmation.CLOSED", "%prefix% %link% [has been closed](green) %view%");
        setDefaultString("confirmation.REOPENED", "%prefix% %link% [has been reopened](green) %view%");
        setDefaultString("confirmation.TELEPORTED", "%prefix% [Teleported to](green) %link%");
        setDefaultString("confirmation.COMMENT-ADDED", "%prefix% [Comment has been added to](green) %link% %view%");
        setDefaultString("confirmation.PRIVATE-COMMENT-ADDED", "%prefix% [Private comment has been added to](green) %link% %view%");
        setDefaultString("confirmation.COMMENT-REMOVED", "%prefix% [Comment has been removed from](green) %link% %view%");

        setDefaultString("mod.info.HEADER", "[----------](color=aqua format=bold) [#%id%](#07a0ff) - %status% [----------](color=aqua format=bold)");
        setDefaultString("mod.info.REQUEST", "[Created by %creator% on %date% at %location%](#fce469)\n[Message: %message%](gray)");
        setDefaultString("mod.info.LOCATION", "[%world% (%x% %y% %z%)](show_text=Click to teleport run_command=/mr tp %id% color=green)");
        setDefaultString("mod.info.UNKNOWN-LOCATION", "[Unknown location](show_text=This ModReq was created in a world that is not currently loaded)");

        setDefaultString("mod.list.HEADER", "%prefix% %count% ModReq(s) - Page %page% of %allpages%");
        setDefaultString("mod.list.ITEM", "%link% [\\[](#fce469)[%status%](green)[\\]](#fce469) [%date%](#fce469) %creator% %view%\n[%last_update%](format=italic)[Message: %message%](gray)");
        setDefaultString("mod.list.ITEM-LAST-UPDATE", "[Updated by %update_actor% on](#fce469) [%update_time%](green)\n");
        setDefaultString("mod.list.PAGINATION", "[%prev% %next%](white)");
        setDefaultString("mod.list.NO-RESULTS", "%prefix% [No ModReqs found](gray)");


        setDefaultString("mod.activity.HEADER", "[------](color=aqua format=bold) Activity - Page %page% of %allpages% [------](color=aqua format=bold)");
        setDefaultString("mod.activity.PUBLIC_COMMENT", "[\\[%id%\\]](aqua) [%date% - %actor% [commented](bold)](color=#fce469)\n[%message%](gray)");
        setDefaultString("mod.activity.PRIVATE_COMMENT", "[\\[%id%\\]](aqua) [%date% - %actor% [commented privately](bold)](color=#fce469)\n[%message%](gray)");
        setDefaultString("mod.activity.CLOSE", "[\\[%id%\\]](aqua) [%date% - %actor% [closed](bold)](color=#fce469)\n[%message%](gray)");
        setDefaultString("mod.activity.REOPEN", "[\\[%id%\\]](aqua) [%date% - %actor% [reopened](bold)](color=#fce469)\n[%message%](gray)");
        setDefaultString("mod.activity.CREATE", "[\\[%id%\\]](aqua) [%date% - %actor% [created](bold)](color=#fce469)\n[%message%](gray)");
        setDefaultString("mod.activity.ELEVATE", "[\\[%id%\\]](aqua) [%date% - %actor% [flagged](bold) for admin attention](color=#fce469)");
        setDefaultString("mod.activity.UNELEVATE", "[\\[%id%\\]](aqua) [%date% - %actor% [removed](bold) the admin flag](color=#fce469)");
        setDefaultString("mod.activity.CLAIM", "[\\[%id%\\]](aqua) [%date% - %actor% [unclaimed](bold)](color=#fce469)");
        setDefaultString("mod.activity.UNCLAIM", "[\\[%id%\\]](aqua) [%date% - %actor% [claimed](bold)](color=#fce469)");
        setDefaultString("mod.activity.PAGINATION", "[%prev% %next%](white)");

        setDefaultString("action.VIEW","[\\[View\\]](run_command=/mr info %id% show_text=Show the details of this ModReq color=gold)");
        setDefaultString("action.CLOSE","[\\[Close\\]](suggest_command=/mr close %id%  show_text=Close this ModReq color=red)");
        setDefaultString("action.OPEN","[\\[Reopen\\]](suggest_command=/mr open %id%  show_text=Re-open this ModReq color=red)");
        setDefaultString("action.TELEPORT","[\\[Teleport\\]](run_command=/mr tp %id% show_text=Teleport to where this ModReq was created color=gold)");
        setDefaultString("action.CLAIM","[\\[Claim\\]](run_command=/mr claim %id% show_text=Claim this ModReq to indicate you are working on it color=gold)");
        setDefaultString("action.UNCLAIM","[\\[Un-claim\\]](run_command=/mr unclaim %id% show_text=Un-claim this ModReq to allow other staff to work on it color=gold)");
        setDefaultString("action.ELEVATE","[\\[Elevate\\]](run_command=/mr elevate %id% show_text=Flag this ModReq for admin attention color=gold)");
        setDefaultString("action.UNELEVATE","[\\[Un-elevate\\]](run_command=/mr elevate %id% show_text=Remove the admin flag from this ModReq color=gold)");
        setDefaultString("action.COMMENT","[\\[Add Comment\\]](suggest_command=/mr comment add %id%  show_text=Add a comment to this ModReq. color=gold)");
        setDefaultString("action.PRIVATE_COMMENT","[\\[Add Private Comment\\]](suggest_command=/mr comment addprivate %id%  show_text=Add a private comment to this ModReq. Private comments are only visible to staff. color=gold)");

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
                    .placeholderIndicator("%")
                    .replace("prefix", new MineDownParser().parse(cfg.getString("general.PREFIX", "")).build())
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
                    .placeholderIndicator("%")
                    .replace("prefix", new MineDownParser().parse(cfg.getString("general.PREFIX", "")).build())
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
            return new Replacer().placeholderIndicator("%")
                    .replace("prefix", cfg.getString("general.PREFIX", ""))
                    .replace(replacements).replaceIn(cfg.getString(key));
        } else {
            ModReq.getPlugin().getLogger().warning("Error: Cannot find language string. " + key);
            return "";
        }
    }

    /**
     * Returns the language string for a player, parsed into a component, taking into account their online status
     * @param player The player
     */
    public static Component getPlayer(OfflinePlayer player) {
        return getPlayer(player, null);
    }

    /**
     * Returns the language string for a player, parsed into a component, taking into account their online status.
     * "You" will be returned if the context and player are the same player.
     * @param player The player
     * @param context The player this string will be shown to.
     */
    public static Component getPlayer(OfflinePlayer player, @Nullable OfflinePlayer context) {
        if(cfg == null) {
            reload();
        }

        if(player.getName() != null) {
            if(player.equals(context)) {
                return Messages.get("general.YOU");
            } else if (player.isOnline()) {
                return Messages.get("general.ONLINE-PLAYER","player", player.getName());
            } else {
                return Messages.get("general.OFFLINE-PLAYER", "player", player.getName());
            }
        } else {
            return Messages.get("general.UNKNOWN-PLAYER");
        }
    }

    public static String getPlayerString(OfflinePlayer player) {
        return getPlayerString(player, null);
    }

    /**
     * Returns the language string for a player, taking into account their online status.
     * "You" will be returned if the context and player are the same player.
     * @param player The player
     * @param context The player this string will be shown to.
     */
    public static String getPlayerString(OfflinePlayer player, @Nullable OfflinePlayer context) {
        if(cfg == null) {
            reload();
        }

        if(player != null && player.getName() != null) {
            if(player.equals(context)) {
                return Messages.getString("general.YOU");
            } else if (player.isOnline()) {
                return Messages.getString("general.ONLINE-PLAYER","player", player.getName());
            } else {
                return Messages.getString("general.OFFLINE-PLAYER", "player", player.getName());
            }
        } else {
            return Messages.getString("general.UNKNOWN-PLAYER");
        }
    }

    /**
     * Returns the given date formatted according to the configured date format
     * @param date The Data
     */
    public static Component getFormattedDate(@NotNull Date date) {
        return date.getTime() > 0
                ? Component.text(getFormattedDateString(date)) : Messages.get("general.UNKNOWN-DATE");
    }

    /**
     * Returns the given date formatted according to the configured date format
     * @param date The Data
     */
    public static String getFormattedDateString(@NotNull Date date) {
        return date.getTime() > 0
                ? ModReq.getPlugin().getFormat().format(date.getTime()) :
                Messages.getString("general.UNKNOWN-DATE");
    }

    /**
     * Returns a component containing a clickable request ID
     * @param request The request
     */
    public static Component getRequestLink(Request request) {
        return Messages.get("general.REQUEST-LINK", "id", String.valueOf(request.getId()));
    }

    /**
     * Returns a component containing a clickable request view button
     * @param request The request
     */
    public static Component getViewButton(Request request) {
        return Messages.get("action.VIEW", "id", String.valueOf(request.getId()));
    }

    /**
     * Send a language string to the specified player
     * @param recipient The player to send the message to
     * @param key The key for the language string to send
     * @param replacements A list of placeholders and their replacements
     */
    public static void send(Player recipient, String key, String ...replacements) {
        if(cfg.getString(key) != null) {
            recipient.sendMessage(Messages.get(key, replacements));
        } else {
            ModReq.getPlugin().getLogger().warning("Error: Cannot find language string. " + key);
        }
    }

    /**
     * Send a language string to the specified player
     * @param recipient The player to send the message to
     * @param key The key for the language string to send
     * @param replacements A list of placeholders and their replacements
     */
    public static void send(Player recipient, String key, Map<String, ?> replacements) {
        if(cfg.getString(key) != null) {
            recipient.sendMessage(Messages.get(key, replacements));
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
        recipient.sendMessage(message);
    }

    /**
     * Sends a notification regarding the given action, actor and request, to relevant players if they are online
     * @param action The action to notify about
     * @param player The player who performed the action
     * @param request The request that was acted on
     * @param replacements A list of placeholders and their replacements
     */
    public static void sendModNotification(NotificationType action, OfflinePlayer player, Request request, String ...replacements) {
        Component message;
        Component prefix = Messages.get("general.PREFIX");
        Component link = Messages.getRequestLink(request);
        Component view = Messages.getViewButton(request);
        Component comment = Messages.get("action.COMMENT", "id", String.valueOf(request.getId()));
        Component close = Messages.get("action.CLOSE", "id", String.valueOf(request.getId()));
        Component actor = getPlayer(player);

        String modKey = "mod.notification." + action.toString().replace("_", "-");

        if(cfg.getString(modKey) != null) {
            message = new MineDown(cfg.getString(modKey))
                    .placeholderIndicator("%")
                    .replace("id", String.valueOf(request.getId()))
                    .replace("actor", actor)
                    .replace("prefix", prefix)
                    .replace("link", link)
                    .replace("view", view)
                    .replace("comment", comment)
                    .replace("close", close)
                    .replace(replacements).toComponent();

            for(Player p : Bukkit.getOnlinePlayers()) {
                if(!p.equals(player) && ModReq.getPlugin().isMod(p)) {
                    p.sendMessage(message);
                }
            }
        } else {
            ModReq.getPlugin().getLogger().warning("Error: Cannot find language string. " + modKey);
        }

        // Send confirmation to acting player
        if(player.isOnline()) {
            String confirmationKey = "confirmation." + action.toString().replace("_", "-");

            message = new MineDown(cfg.getString(confirmationKey))
                    .placeholderIndicator("%")
                    .replace("id", String.valueOf(request.getId()))
                    .replace("prefix", prefix)
                    .replace("link", link)
                    .replace("view", view)
                    .replace("comment", comment)
                    .replace("close", close)
                    .replace(replacements).toComponent();

            ((Player) player).sendMessage(message);
        }

        // Send notification to creator if they should be notified, are online, and are not the player who performed
        // the action
        if(action.sendToCreator() && !player.getUniqueId().equals(request.getCreator())) {
            OfflinePlayer creator = Bukkit.getOfflinePlayer(request.getCreator());
            String playerKey = "player.notification." + action.toString().replace("_", "-");

            if(!creator.isOnline() || cfg.getString(playerKey) == null) {
                return;
            }

            message = new MineDown(cfg.getString(playerKey))
                    .placeholderIndicator("%")
                    .replace("id", String.valueOf(request.getId()))
                    .replace("actor", actor)
                    .replace("prefix", prefix)
                    .replace("link", link)
                    .replace("view", view)
                    .replace(replacements).toComponent();

            ((Player) creator).sendMessage(message);
        }
    }
}
