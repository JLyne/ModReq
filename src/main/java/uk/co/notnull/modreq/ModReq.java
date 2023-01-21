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

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package uk.co.notnull.modreq;

import java.text.SimpleDateFormat;
import java.util.Locale;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import uk.co.notnull.modreq.commands.Commands;
import uk.co.notnull.modreq.listener.PlayerListener;
import uk.co.notnull.modreq.storage.DataSource;
import uk.co.notnull.modreq.storage.SqlDataSource;

public final class ModReq extends JavaPlugin {
    private static ModReq plugin;
    private Configuration cfg;
    private DataSource dataSource;
    private SimpleDateFormat format;
    private RequestRegistry requestRegistry;

    public ModReq() {}

    public void onEnable() {
        plugin = this;
        this.reloadConfiguration();

        this.requestRegistry = new RequestRegistry(this);

        new Commands(this);

        getServer().getPluginManager().registerEvents(new PlayerListener(), this);

        if (this.cfg.isMySQL()) {
            this.getLogger().info("Plugin enabled (database: MySQL).");
        } else {
            this.getLogger().info("Plugin enabled (database: SQLite).");
        }
    }

    public void onDisable() {
        if(this.dataSource != null) {
            this.dataSource.destroy();
        }

        this.getLogger().info("Plugin disabled.");
    }

    public static ModReq getPlugin() {
        return plugin;
    }

    public Configuration getConfiguration() {
        return this.cfg;
    }

    public DataSource getDataSource() {
        return this.dataSource;
    }

    public SimpleDateFormat getFormat() {
        return this.format;
    }

    public RequestRegistry getRequestRegistry() {
        return requestRegistry;
    }

    public void reloadConfiguration() {
        Messages.reload();
        Locale locale = Locale.forLanguageTag(Messages.getString("general.LANGUAGE-TAG"));
        Configuration cfg = new Configuration();
        this.format = new SimpleDateFormat(Messages.getString("general.DATE-FORMAT"), locale);
        this.cfg = cfg;

        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
            if(this.dataSource != null) {
                this.dataSource.destroy();
            }

            this.dataSource = new SqlDataSource(this, cfg);

            if(!this.dataSource.init()) {
                getServer().getPluginManager().disablePlugin(plugin);
            }
        });
    }

    public void playSound(Player player) {
        Sound sound = null;

        try {
            sound = Sound.valueOf("BLOCK_NOTE_HARP");
        } catch (Exception var6) {
            try {
                sound = Sound.valueOf("BLOCK_NOTE_BLOCK_HARP");
            } catch (Exception ignored) { }
        }

        if (sound != null) {
            player.playSound(player.getLocation(), sound, 1.0F, 1.0F);
        } else {
            getPlugin().getLogger().warning("Cannot find sound...");
        }

    }

    public void playModSound() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (plugin.isMod(player)) {
                this.playSound(player);
            }
        }
    }

    public boolean isMod(@NotNull CommandSender player) {
        return player.hasPermission("modreq.mod");
    }

    public boolean isAdmin(@NotNull CommandSender player) {
        return player.hasPermission("modreq.admin");
    }

    public boolean isVanished(Player player) {
        for (MetadataValue meta : player.getMetadata("vanished")) {
            if (meta.asBoolean()) return true;
        }
            return false;
    }
}
