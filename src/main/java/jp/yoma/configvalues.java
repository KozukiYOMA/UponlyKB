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
    private double kbencmulti;
    private boolean ckbactive;
    private boolean ckbactiveonshield;
    private boolean debugmode;
    private int configversion;
    private int requiredconfigversion = 7;

    public configvalues(Main plugin) {
        this.plugin = plugin;
        instance = this;
    }

    public void loadConfigValues() {
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();
        ckbactive = config.getBoolean("knockback.active",true);//プラグインの有効/無効
        ckbactiveonshield = config.getBoolean("knockback.active-on-shield",false);//盾ガード時のノックバック有効/無効
        horizontalMultiplier = config.getDouble("knockback.horizontalkb",0.28);//水平ノックバック量
        verticalBoost = config.getDouble("knockback.verticalkb",0.35);//垂直ノックバック量
        Sprintkb = config.getDouble("knockback.sprintkb",1.2);//ダッシュ時の水平ノックバック倍率
        Critkb = config.getDouble("knockback.critboostkb",1.0);//クリティカル時の垂直ノックバック倍率
        kbencmulti = config.getDouble("knockback.knockback-enchant-multiplier",0.75);//ノックバックエンチャントの追加KB倍率
        debugmode = config.getBoolean("debug",false);//デバッグモードの有効/無効
        configversion = config.getInt("config-version",0);//configのバージョン

        //configのバージョンが古い場合のconfig更新処理
        if (configversion < requiredconfigversion){
            File configFile = new File(plugin.getDataFolder(), "config.yml");//現在のconfigファイル
            File oldConfigFile = new File(plugin.getDataFolder(), "config_old.yml");//既に存在するバックアップconfigファイル

            //configファイルのリネームとバックアップの作成
            if (configFile.exists()) {
                if (oldConfigFile.exists()) {
                    oldConfigFile.delete();//既に存在するバックアップファイルを削除
                }
                boolean renamed = configFile.renameTo(oldConfigFile);//現在のconfigファイルをバックアップconfigファイルにリネーム
                if (renamed) {plugin.saveDefaultConfig();}//デフォルトのコンフィグを保存
                if (renamed){
                    //configの更新通知
                    plugin.getLogger().info("Renamed the old config file to config_old.yml and regenerated the config file.");
                    loadConfigValues();//もう一回configを読み込む
                }else{
                    //なんかバグった時
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

    public double getKBEnchantMultiplier() {
        return kbencmulti;
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
