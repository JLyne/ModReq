/*
 * ModReq
 * Copyright (C) 2023 James Lyne
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

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Util {
	public static void playSound(Player player) {
        Sound sound = null;

        try {
            sound = Sound.BLOCK_NOTE_BLOCK_HARP;
        } catch (Exception var6) {
            try {
                sound = Sound.BLOCK_NOTE_BLOCK_HARP;
            } catch (Exception ignored) { }
        }

        if (sound != null) {
            player.playSound(player.getLocation(), sound, 1.0F, 1.0F);
        } else {
            ModReq.getPlugin().getLogger().warning("Cannot find sound...");
        }

    }

    public static void playModSound() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (isMod(player)) {
                playSound(player);
            }
        }
    }

    public static boolean isMod(@NotNull CommandSender player) {
        return player.hasPermission("modreq.mod");
    }

    public static boolean isAdmin(@NotNull CommandSender player) {
        return player.hasPermission("modreq.admin");
    }

    public static boolean canSee(@Nullable Player viewer, Player target) {
        if(viewer == null) {
            return true;
        }

        return viewer.canSee(target);
    }
}
