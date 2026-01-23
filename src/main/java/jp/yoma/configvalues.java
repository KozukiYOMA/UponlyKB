package jp.yoma;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;

public class configvalues {
    
    private static configvalues instance;
    public static configvalues getInstance() {
        return instance;
    }

    //values
    private final Main plugin;
    private double horizontalMultiplier;
    private double verticalBoost;
    private double Sprintkb;
    private double Critkb;
    private boolean ckbactive;
    private boolean ckbactiveonshield;
    private boolean debugmode;
    private int configversion;
    private int requiredconfigversion = 6;

    public configvalues(Main plugin) {
        this.plugin = plugin;
        instance = this;
    }

    public void loadConfigValues() {
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();
        ckbactive = config.getBoolean("knockback.active",true);
        ckbactiveonshield = config.getBoolean("knockback.active-on-shield",false);
        horizontalMultiplier = config.getDouble("knockback.horizontalkb",0.28);
        verticalBoost = config.getDouble("knockback.verticalkb",0.35);
        Sprintkb = config.getDouble("knockback.sprintkb",1.2);
        Critkb = config.getDouble("knockback.critboostkb",1.0);
        debugmode = config.getBoolean("debug",false);
        configversion = config.getInt("config-version",0);
        if (configversion < requiredconfigversion){
            File configFile = new File(plugin.getDataFolder(), "config.yml");
            File oldConfigFile = new File(plugin.getDataFolder(), "config_old.yml");
            if (configFile.exists()) {
                if (oldConfigFile.exists()) {
                    oldConfigFile.delete();
                }
                boolean renamed = configFile.renameTo(oldConfigFile);
                if (renamed) {plugin.saveDefaultConfig();}
                if (renamed){
                    plugin.getLogger().info("Renamed the old config file to config_old.yml and regenerated the config file.");
                    loadConfigValues();
                }else{
                    plugin.getLogger().warning("Failed to backup the old config file!");
                }
            }
        }
    }

    public double getHorizontalMultiplier() {
        return horizontalMultiplier;
    }

    public double getVerticalBoost() {
        return verticalBoost;
    }

    public double getSprintkb() {
        return Sprintkb;
    }

    public double getCritkb() {
        return Critkb;
    }

    public boolean isCkbactive() {
        return ckbactive;
    }

    public boolean isCkbactiveonshield() {
        return ckbactiveonshield;
    }

    public boolean isDebugmode() {
        return debugmode;
    }
    
}
