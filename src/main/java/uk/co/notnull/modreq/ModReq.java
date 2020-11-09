//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package uk.co.notnull.modreq;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.Locale;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import uk.co.notnull.modreq.commands.Commands;
import uk.co.notnull.modreq.listener.PlayerListener;

public final class ModReq extends JavaPlugin {
    private static ModReq plugin;
    private Configuration cfg;
    private LanguageFile lang;
    private SQLHandler sqlHandler;
    private SimpleDateFormat format;

    public ModReq() {
    }

    public void onEnable() {
        plugin = this;
        this.reloadConfiguration();

        new Commands(this);

//        this.getCommand("modreq").setExecutor(new PlayerCommands());
//        this.getCommand("check").setExecutor(new PlayerCommands());
//        this.getCommand("done").setExecutor(new PlayerCommands());
//        this.getCommand("tpid").setExecutor(new PlayerCommands());
//        this.getCommand("claim").setExecutor(new PlayerCommands());
//        this.getCommand("unclaim").setExecutor(new PlayerCommands());
//        this.getCommand("reopen").setExecutor(new PlayerCommands());
//        this.getCommand("elevate").setExecutor(new PlayerCommands());
//        this.getCommand("mrreload").setExecutor(new PlayerCommands());
//        this.getCommand("mrnote").setExecutor(new PlayerCommands());
        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new PlayerListener(), this);
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

    public LanguageFile getLanguageFile() {
        return this.lang;
    }

    public SQLHandler getSqlHandler() {
        return this.sqlHandler;
    }

    public SimpleDateFormat getFormat() {
        return this.format;
    }

    public void reloadConfiguration() {
        this.cfg = new Configuration();
        this.lang = new LanguageFile();
        Locale locale = Locale.forLanguageTag(this.getLanguageFile().getLangString("general.LANGUAGE-TAG"));
        this.format = new SimpleDateFormat(this.getLanguageFile().getLangString("general.DATE-FORMAT"), locale);
        this.sqlHandler = new SQLHandler(this.getLogger(), this.cfg.getMySQLHost(), this.cfg.getMySQLPort(), this.cfg.getMySQLDatabase(), this.cfg.getMySQLUser(), this.cfg.getMySQLPassword(), "modreq", "plugins/ModReq", this.cfg.isMySQL());
        BukkitRunnable runnable = new BukkitRunnable() {
            public void run() {
                try {
                    Connection connection = ModReq.this.sqlHandler.open();
                    if (connection == null) {
                        ModReq.getPlugin().getLogger().warning("Error while connecting to database.");
                        return;
                    }

                    PreparedStatement pStatement;
                    if (ModReq.this.cfg.isMySQL()) {
                        pStatement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS modreq (id INTEGER(10) UNSIGNED PRIMARY KEY auto_increment, uuid CHAR(36), request VARCHAR(256), timestamp BIGINT(13) UNSIGNED, world VARCHAR(256), x INTEGER(11), y INTEGER(11), z INTEGER(11), claimed CHAR(36) NOT NULL DEFAULT '', mod_uuid CHAR(36) NOT NULL DEFAULT '', mod_comment VARCHAR(256) NOT NULL DEFAULT '', mod_timestamp BIGINT(13) UNSIGNED NOT NULL DEFAULT '0', done TINYINT(1) UNSIGNED NOT NULL DEFAULT '0', elevated TINYINT(1) UNSIGNED NOT NULL DEFAULT '0');");
                        pStatement.executeUpdate();
                        pStatement.close();
                        pStatement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS modreq_notes (id INTEGER(10) UNSIGNED PRIMARY KEY auto_increment, modreq_id INTEGER(10) UNSIGNED, uuid CHAR(36), note VARCHAR(256));");
                        pStatement.executeUpdate();
                        pStatement.close();
                    } else {
                        pStatement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS modreq (id INTEGER PRIMARY KEY AUTOINCREMENT, uuid CHAR(36), request VARCHAR(256), timestamp UNSIGNED BIGINT(13), world VARCHAR(256), x INTEGER(11), y INTEGER(11), z INTEGER(11), claimed CHAR(36) NOT NULL DEFAULT '', mod_uuid CHAR(36) NOT NULL DEFAULT '', mod_comment VARCHAR(256) NOT NULL DEFAULT '', mod_timestamp UNSIGNED BIGINT(13) NOT NULL DEFAULT '0', done UNSIGNED TINYINT(1) NOT NULL DEFAULT '0', elevated UNSIGNED TINYINT(1) NOT NULL DEFAULT '0');");
                        pStatement.executeUpdate();
                        pStatement.close();
                        pStatement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS modreq_notes (id INTEGER PRIMARY KEY AUTOINCREMENT, modreq_id UNSIGNED INTEGER(10), uuid CHAR(36), note VARCHAR(256));");
                        pStatement.executeUpdate();
                        pStatement.close();
                    }

                    connection.close();
                } catch (SQLException var3) {
                    var3.printStackTrace();
                }

            }
        };
        runnable.runTaskAsynchronously(getPlugin());
    }

    public void sendMsg(CommandSender sender, String message) {
        sender.sendMessage(this.getLanguageFile().getLangString(message));
    }

    public void sendModMsg(String message) {
        Iterator var3 = Bukkit.getOnlinePlayers().iterator();

        while(var3.hasNext()) {
            Player player = (Player)var3.next();
            if (player.hasPermission("modreq.mod")) {
                player.sendMessage(message);
            }
        }

    }

    public void playSound(Player player) {
        Sound sound = null;

        try {
            sound = Sound.valueOf("BLOCK_NOTE_HARP");
        } catch (Exception var6) {
            try {
                sound = Sound.valueOf("BLOCK_NOTE_BLOCK_HARP");
            } catch (Exception var5) {
            }
        }

        if (sound != null) {
            player.playSound(player.getLocation(), sound, 1.0F, 1.0F);
        } else {
            getPlugin().getLogger().warning("Cannot find sound...");
        }

    }

    public void playModSound() {
        Iterator var2 = Bukkit.getOnlinePlayers().iterator();

        while(var2.hasNext()) {
            Player player = (Player)var2.next();
            if (player.hasPermission("modreq.mod")) {
                this.playSound(player);
            }
        }

    }

    public OfflinePlayer getOfflinePlayer(String uuidString) {
        UUID uuid = UUID.fromString(uuidString);
        return Bukkit.getOfflinePlayer(uuid);
    }
}
