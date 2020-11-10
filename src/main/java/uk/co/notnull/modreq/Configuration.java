package uk.co.notnull.modreq;

import java.io.File;
import java.io.IOException;
import org.bukkit.configuration.file.YamlConfiguration;

public class Configuration {
    private File file;
    private YamlConfiguration cfg;
    private boolean isMySQL;
    private String mysql_host;
    private String mysql_port;
    private String mysql_database;
    private String mysql_user;
    private String mysql_pass;
    private String mysql_options;
    private int modreqs_per_page;
    private int show_last_modreqs;
    private int max_open_modreqs;

    public Configuration() {
        File folder = new File("plugins/ModReq");
        if (!folder.exists()) {
            folder.mkdir();
        }

        this.file = new File("plugins/ModReq/config.yml");
        if (!this.file.exists()) {
            ModReq.getPlugin().getLogger().info("No configuration file found. Creating new one...");

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
        this.isMySQL = this.getBoolean("database.use-mysql", false);
        this.mysql_host = this.getString("database.mysql.hostname", "localhost");
        this.mysql_port = this.getString("database.mysql.port", "3306");
        this.mysql_database = this.getString("database.mysql.database", "");
        this.mysql_user = this.getString("database.mysql.user", "");
        this.mysql_pass = this.getString("database.mysql.pass", "");
        this.mysql_options = this.getString("database.mysql.options", "autoreconnect=true&useSSL=false&serverTimezone=UTC");
        this.modreqs_per_page = this.getInt("settings.modreqs-per-page", 5);
        this.show_last_modreqs = this.getInt("settings.show-last-modreqs", 5);
        this.max_open_modreqs = this.getInt("settings.max-open-modreqs", 5);

        try {
            this.cfg.save(this.file);
        } catch (IOException var2) {
            var2.printStackTrace();
        }

    }

    private String getString(String pEntry, String pValue) {
        if (this.cfg.getString(pEntry) == null) {
            this.cfg.set(pEntry, pValue);
            return pValue;
        } else {
            return this.cfg.getString(pEntry);
        }
    }

    private int getInt(String pEntry, int pValue) {
        if (this.cfg.getString(pEntry) == null) {
            this.cfg.set(pEntry, pValue);
            return pValue;
        } else {
            return this.cfg.getInt(pEntry);
        }
    }

    private boolean getBoolean(String pEntry, boolean pValue) {
        if (this.cfg.getString(pEntry) == null) {
            this.cfg.set(pEntry, pValue);
            return pValue;
        } else {
            return this.cfg.getBoolean(pEntry);
        }
    }

    public boolean isMySQL() {
        return this.isMySQL;
    }

    public String getMySQLHost() {
        return this.mysql_host;
    }

    public String getMySQLPort() {
        return this.mysql_port;
    }

    public String getMySQLDatabase() {
        return this.mysql_database;
    }

    public String getMySQLOptions() {
        return this.mysql_options;
    }

    public String getMySQLUser() {
        return this.mysql_user;
    }

    public String getMySQLPassword() {
        return this.mysql_pass;
    }

    public int getModreqs_per_page() {
        return this.modreqs_per_page;
    }

    public int getShow_last_modreqs() {
        return this.show_last_modreqs;
    }

    public int getMax_open_modreqs() {
        return this.max_open_modreqs;
    }
}
