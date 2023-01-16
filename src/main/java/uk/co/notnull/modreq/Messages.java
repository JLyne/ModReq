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
import java.util.Map;

import de.themoep.minedown.adventure.MineDown;
import de.themoep.minedown.adventure.MineDownParser;
import de.themoep.minedown.adventure.Replacer;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

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
        setDefaultString("error.TELEPORT-ERROR", "%prefix% [An error has occurred while teleporting, Please contact an administrator.](red)");
        setDefaultString("error.TELEPORT-UNLOADED", "%prefix% [The world this ModReq was created in is not loaded.](red)");
        setDefaultString("error.PAGE-ERROR", "%prefix% [Page %page% does not exist](red)");
        setDefaultString("error.NOTE-DOES-NOT-EXIST", "%prefix% [This note id does not exist.](red)");
        setDefaultString("error.NOTE-OTHER", "%prefix% [You did not create this note.](red)");
        setDefaultString("error.MAX-OPEN-MODREQS", "%prefix% [You cannot open more than](red) [%max%](dark_red) [ModReq(s) at the same time.](red)");
        setDefaultString("error.NO-PREVIOUS-SEARCH", "%prefix% [You have no previous search.](red) [\\[Start new Search\\]](suggest_command=/mr search  show_text=Start a new search color=gold)");
        setDefaultString("error.NO-PERMISSION", "%prefix% [You do not have permssion to do this.](red)");

        setDefaultString("general.PREFIX", "[ModReq](color=red) [//](white)");
        setDefaultString("general.OPEN", "[OPEN](green)");
        setDefaultString("general.CLOSED", "[CLOSED](red)");
        setDefaultString("general.CLAIMED", "[CLAIMED \\(%player%\\)](gold)");
        setDefaultString("general.ELEVATED", " [\\[ADMIN\\]](show_text=This ModReq has been flagged for admin attention color=aqua)");
        setDefaultString("general.NOTES", " [\\[NOTES\\]](show_text=This ModReq has notes attached color=dark_red)");
        setDefaultString("general.ON-JOIN-HEADER", "%prefix% [Updates to your ModReqs](green) [\\[View all\\]](run_command=/mr me show_text=List all ModReqs you have created color=gold)");
        setDefaultString("general.DATE-FORMAT", "MMM.dd.yyyy, HH:mm:ss");
        setDefaultString("general.LANGUAGE-TAG", "en-GB");
        setDefaultString("general.ONLINE-PLAYER", "[%player%](suggest_command=/w %player%  show_text=Click to whisper %player% color=green)");
        setDefaultString("general.OFFLINE-PLAYER", "[%player%](show_text=%player% is offline color=red)");
        setDefaultString("general.UNKNOWN-PLAYER", "[Unknown](show_text=Unknown player color=red)");
        setDefaultString("general.UNKNOWN-WORLD", "[Unknown world](show_text=The world this ModReq was created in is not loaded)");
        setDefaultString("general.REQUEST-LINK", "[#%id%](run_command=/mr info %id% show_text=Click to show the details of #%id% color=#07a0ff)");

        setDefaultString("pagination.PREV", "[\\[Prev\\]](run_command=%command% show_text=Previous page color=gray)");
        setDefaultString("pagination.NEXT", "[\\[Next\\]](run_command=%command% show_text=Next page color=gray)");
        setDefaultString("pagination.DISABLED", "      ");

        setDefaultString("confirmation.confirm", "%prefix% [Confirmation required](red) [\\[Confirm\\]](run_command=/mr confirm show_text=Click to confirm the command you just entered color=gold)");
        setDefaultString("confirmation.nothing", "%prefix% [You don't have any pending commands.](red)");

        setDefaultString("player.notification.CLOSED", "%prefix% %actor% [has closed your ModReq](green) %link% %view%\n[Message: %message%](gray)");
        setDefaultString("player.notification.REOPENED", "%prefix% %actor% [has been re-opened your ModReq](green) %link% %view%");
        setDefaultString("player.notification.CREATED", "%prefix% [Your ModReq (%link%) has been created and sent to staff members. Please be patient.](green) %view%");

        setDefaultString("player.action.VIEW", "[\\[View\\]](run_command=/mr info %id% show_text=Show the details of this ModReq color=gold)");

        setDefaultString("player.list.HEADER", "%prefix% Your ModReqs [\\[Create\\]](suggest_command=/modreq  show_text=Create a new ModReq color=gold)");
        setDefaultString("player.list.ITEM-REQUEST", "%link% [\\[](#fce469)%status%[\\]](#fce469) [%date%](#fce469)\n[Message: %message%](gray)");
        setDefaultString("player.list.ITEM-RESPONSE", "[Answered by %responder% on](#fce469) [%close_time%](green)[.](#fce469)\n[Message: %response%](gray)");
        setDefaultString("player.list.FOOTER", "[%prev% %next%](white)");
        setDefaultString("player.list.NO-RESULTS", "[You have no open ModReqs. New ModReq: /modreq <request>](aqua)");

        setDefaultString("player.info.HEADER", "[----------](color=aqua format=bold) [#%id%](#07a0ff) - %status% [----------](color=aqua format=bold)");
        setDefaultString("player.info.LOCATION", "[%world% (%x% %y% %z%)](green)");
        setDefaultString("player.info.UNKNOWN-LOCATION", "[Unknown location](show_text=This ModReq was created in a world that is not currently loaded)");
        setDefaultString("player.info.REQUEST", "[Created on %date% at %location%](#fce469)\n[Message: %message%](gray)");
        setDefaultString("player.info.RESPONSE", "[Answered by %responder% on](#fce469) [%response_date%](green)\n[Message: %response%](gray)");
        setDefaultString("player.info.FOOTER", "");

        setDefaultString("mod.JOIN", "%prefix% [%count%](#07a0ff) [ModReq(s) open](green) [\\[View\\]](run_command=/mr list show_text=View all open modreqs color=gold)");

        setDefaultString("mod.notification.CREATED", "%prefix% %actor% [created a new ModReq (%link%)](green) %view%");
        setDefaultString("mod.notification.CLAIMED", "%prefix% %link% [has been claimed by](gray) %actor% %view%");
        setDefaultString("mod.notification.UNCLAIMED", "%prefix% %link% [has been un-claimed by](gray) %actor% %view%");
        setDefaultString("mod.notification.ELEVATED", "%prefix% %link% [has been flagged for admin attention by](gray) %actor% %view%");
        setDefaultString("mod.notification.UNELEVATED", "%prefix% [Admin flag has been removed from](gray) %link% [by](gray) %actor% %view%");
        setDefaultString("mod.notification.CLOSED", "%prefix% %link% [has been closed by](gray) %actor% %view%\n[Message: %message%](gray)");
        setDefaultString("mod.notification.REOPENED", "%prefix% %link% [has been re-opened by](gray) %actor% %view%");
        setDefaultString("mod.notification.TELEPORTED", "%prefix% [Teleported to](gray) %link%");
        setDefaultString("mod.notification.NOTE-ADDED", "%prefix% %actor% [added a note to](gray) %link% %view%\n[Message: %message%](gray)");
        setDefaultString("mod.notification.NOTE-REMOVED", "%prefix% %actor% [removed a note from](gray) %link% %view%\n[Message: %message%](gray)");

        setDefaultString("mod.info.HEADER", "[----------](color=aqua format=bold) [#%id%](#07a0ff) - %status% [----------](color=aqua format=bold)");
        setDefaultString("mod.info.REQUEST", "[Created by %creator% on %date% at %location%](#fce469)\n[Message: %message%](gray)");
        setDefaultString("mod.info.RESPONSE", "[Answered by %responder% on](#fce469) [%response_date%](green)\n[Message: %response%](gray)");
        setDefaultString("mod.info.LOCATION", "[%world% (%x% %y% %z%)](show_text=Click to teleport run_command=/mr tp %id% color=green)");
        setDefaultString("mod.info.UNKNOWN-LOCATION", "[Unknown location](show_text=This ModReq was created in a world that is not currently loaded)");
        setDefaultString("mod.info.NOTE", "[\\[%id%\\]](aqua) [%creator% - %message%](gray)");
        setDefaultString("mod.info.FOOTER", "");

        setDefaultString("mod.list.HEADER", "%prefix% %count% ModReq(s) - Page %page% of %allpages%");
        setDefaultString("mod.list.ITEM", "%link% [\\[](#fce469)[%status%](green)[\\]](#fce469) [%date%](#fce469) %creator%\n[Message: %message%](gray)");
        setDefaultString("mod.list.FOOTER", "[%prev% %next%](white)");
        setDefaultString("mod.list.NO-RESULTS", "%prefix% [No ModReqs found](gray)");

        setDefaultString("mod.action.VIEW","[\\[View\\]](run_command=/mr info %id% show_text=Show the details of this ModReq color=gold)");
        setDefaultString("mod.action.CLOSE","[\\[Close\\]](suggest_command=/mr close %id%  show_text=Close this ModReq color=red)");
        setDefaultString("mod.action.OPEN","[\\[Re-open\\]](run_command=/mr open %id% show_text=Re-open this ModReq color=red)");
        setDefaultString("mod.action.TELEPORT","[\\[Teleport\\]](run_command=/mr tp %id% show_text=Teleport to where this ModReq was created color=gold)");
        setDefaultString("mod.action.CLAIM","[\\[Claim\\]](run_command=/mr claim %id% show_text=Claim this ModReq to indicate you are working on it color=gold)");
        setDefaultString("mod.action.UNCLAIM","[\\[Un-claim\\]](run_command=/mr unclaim %id% show_text=Un-claim this ModReq to allow other mods to work on it color=gold)");
        setDefaultString("mod.action.ELEVATE","[\\[Elevate\\]](run_command=/mr elevate %id% show_text=Flag this ModReq for admin attention color=gold)");
        setDefaultString("mod.action.UNELEVATE","[\\[Un-elevate\\]](run_command=/mr elevate %id% show_text=Remove the admin flag from this ModReq color=gold)");
        setDefaultString("mod.action.NOTE","[\\[Add Note\\]](suggest_command=/mr note add %id%  show_text=Add a note to this ModReq. Notes are only visible to staff. color=gold)");

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
     * Returns the language string for a player, taking into account their online status
     * @param player The player
     */
    public static Component getPlayer(OfflinePlayer player) {
        if(cfg == null) {
            reload();
        }

        if(player.getName() != null) {
            if (player.isOnline()) {
                return Messages.get("general.ONLINE-PLAYER","player", player.getName());
            } else {
                return Messages.get("general.OFFLINE-PLAYER", "player", player.getName());
            }
        } else {
            return Messages.get("general.UNKNOWN-PLAYER");
        }
    }

    /**
     * Send a language string to the specified player
     * @param recipient The player to send the message to
     * @param key The key for the language string to send
     * @param replacements A list of placeholders and their replacements
     */
    public static void send(Player recipient, String key, String ...replacements) {
        if(cfg.getString(key) != null) {
            recipient.sendMessage(new MineDown(cfg.getString(key))
                                         .placeholderIndicator("%")
                                         .replace("prefix", Messages.get("general.PREFIX"))
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
        recipient.sendMessage(message);
    }

    /**
     * Send a RequestCollection to a player, which will be displayed as a mod or player focused list
     * depending on permissions
     * @param player The player to send the list to. Will be used for permission checks.
     * @param requests The collection to send as a list
     * @param command The command any present pagination buttons should run. Should contain "%page%", which
     *               will be replaced with the relevant page number.
     */
    public static void sendList(Player player, RequestCollection requests, String command) {
        boolean isMod = player.hasPermission("modreq.mod") || player.hasPermission("modreq.admin");
        Component message = Component.empty();

        if(requests.getTotal() == 0 && requests.getPage() == 1) {
			Messages.send(player,  isMod ? "mod.list.NO-RESULTS" : "player.list.NO-RESULTS");
			return;
		} else if (requests.isAfterLastPage()) {
			Messages.send(player, "error.PAGE-ERROR", "page", "" + requests.getPage());
			return;
		}

        String headerKey = isMod ? "mod.list.HEADER" : "player.list.HEADER";

        message = message.append(Messages.get(headerKey,
                                              "count", String.valueOf(requests.getTotal()),
                                              "page", String.valueOf(requests.getPage()),
                                              "allpages", String.valueOf(requests.getTotalPages())));
        message = message.append(Component.newline());

		message = message.append(requests.toComponent(player));

		if(requests.isPaginated()) {
		    String footerKey = isMod ? "mod.list.FOOTER" : "player.list.FOOTER";
		    Component nextButton;
		    Component prevButton;

		    if(requests.isLastPage()) {
                nextButton = Messages.get("pagination.DISABLED");
            } else {
                nextButton = Messages.get("pagination.NEXT",
                                          "command", command.replace("%page%",
                                                                     String.valueOf(requests.getPage() + 1)));
            }

		    if(requests.isFirstPage()) {
                prevButton = Messages.get("pagination.DISABLED");
            } else {
                prevButton = Messages.get("pagination.PREV",
                                          "command", command.replace("%page%",
                                                                     String.valueOf(requests.getPage() - 1)));
            }

            message = message.append(Component.newline());
		    message = message.append(new MineDown(cfg.getString(footerKey))
                                             .placeholderIndicator("%")
                                             .replace(
                                                     "count", String.valueOf(requests.getTotal()),
                                                     "page", String.valueOf(requests.getPage()),
                                                     "allpages", String.valueOf(requests.getTotalPages())
                                             )
                                             .replace("next", nextButton)
                                             .replace("prev", prevButton)
                                             .toComponent());
		}

        player.sendMessage(message);
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
        Component link = Messages.get("general.REQUEST-LINK", "id", String.valueOf(request.getId()));

        String username;

        if(player.getName() != null) {
            if (player.isOnline()) {
                username = Messages.getString("general.ONLINE-PLAYER","player", player.getName());
            } else {
                username = Messages.getString("general.OFFLINE-PLAYER", "player", player.getName());
            }
        } else {
            username = Messages.getString("general.UNKNOWN-PLAYER");
        }

        Component actor = new MineDownParser().parse(username).build();

        String modKey = "mod.notification." + action.toString().replace("_", "-");

        if(cfg.getString(modKey) != null) {
            message = new MineDown(cfg.getString(modKey))
                    .placeholderIndicator("%")
                    .replace("id", String.valueOf(request.getId()))
                    .replace("actor", actor)
                    .replace("prefix", prefix)
                    .replace("link", link)
                    .replace("view", Messages.get("mod.action.VIEW",
                                                        "id", String.valueOf(request.getId())))
                    .replace(replacements).toComponent();

            for(Player p : Bukkit.getOnlinePlayers()) {
                if(p.hasPermission("modreq.mod")) {
                    p.sendMessage(message);
                }
            }
        } else {
            ModReq.getPlugin().getLogger().warning("Error: Cannot find language string. " + modKey);
        }

        if(action.sendToCreator() && !player.getUniqueId().equals(request.getCreator())) {
            OfflinePlayer creator = Bukkit.getOfflinePlayer(request.getCreator());
            String playerKey = "player.notification." + action.toString().replace("_", "-");

            if(!creator.isOnline() || cfg.getString(modKey) == null) {
                return;
            }

            message = new MineDown(cfg.getString(playerKey))
                    .placeholderIndicator("%")
                    .replace("id", String.valueOf(request.getId()))
                    .replace("actor", actor)
                    .replace("prefix", prefix)
                    .replace("link", link)
                    .replace("view", Messages.get("player.action.VIEW",
                                                        "id", String.valueOf(request.getId())))
                    .replace(replacements).toComponent();

            ((Player) creator).sendMessage(message);
        }
    }
}
