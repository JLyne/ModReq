//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package uk.co.notnull.modreq;

import java.text.SimpleDateFormat;
import java.util.Locale;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
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
            if (player.hasPermission("modreq.mod")) {
                this.playSound(player);
            }
        }
    }
}
