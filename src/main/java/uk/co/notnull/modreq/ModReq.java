//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package uk.co.notnull.modreq;

import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.Locale;
import java.util.UUID;

import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import uk.co.notnull.modreq.commands.Commands;
import uk.co.notnull.modreq.listener.PlayerListener;
import uk.co.notnull.modreq.storage.DataSource;
import uk.co.notnull.modreq.storage.SqlDataSource;

public final class ModReq extends JavaPlugin {
    private static ModReq plugin;
    private Configuration cfg;
    private Messages lang;
    private DataSource dataSource;
    private SimpleDateFormat format;
    private RequestRegistry requestRegistry;
    private BukkitAudiences bukkitAudiences;

    public ModReq() {}

    public void onEnable() {
        plugin = this;
        this.reloadConfiguration();

        this.requestRegistry = new RequestRegistry(this);
        this.bukkitAudiences = BukkitAudiences.create(plugin);

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

    @Deprecated
    public Messages getLanguageFile() {
        return this.lang;
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

    public BukkitAudiences getBukkitAudiences() {
        return bukkitAudiences;
    }

    public void reloadConfiguration() {
        this.lang = new Messages();
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

    @Deprecated
    public void sendMsg(CommandSender sender, String message) {
        sender.sendMessage(this.getLanguageFile().getLangString(message));
    }

    @Deprecated
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

    @Deprecated
    public OfflinePlayer getOfflinePlayer(String uuidString) {
        UUID uuid = UUID.fromString(uuidString);
        return Bukkit.getOfflinePlayer(uuid);
    }


}
