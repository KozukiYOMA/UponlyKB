package jp.yoma;

public class debug {
    private static debug instance;
    public static debug getInstance() {
        return instance;
    }

    private final Main plugin;

    public debug(Main plugin) {
        this.plugin = plugin;
        instance = this;
    }
    public void debugmessage(String message) {//debug用メッセージ出力の共通化
        if (configvalues.getInstance().isDebugmode()){plugin.getLogger().info(message);}
    }
}
