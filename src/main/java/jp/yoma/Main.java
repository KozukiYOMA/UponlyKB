package jp.yoma;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.command.CommandSender;
import org.bukkit.command.Command;
import org.bukkit.event.entity.EntityKnockbackEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.Listener;
import org.bukkit.util.Vector;

public final class Main extends JavaPlugin implements Listener {
    configvalues configvalues = new configvalues(this);//関数を使うのに必須
    debug debug = new debug(this);

    @Override
    public void onEnable() {//起動時
        saveDefaultConfig();//デフォルトのconfig.ymlを保存するらしい...?どうやら既にあると失敗するらしいけど。
        configvalues.loadConfigValues();//configファイルの読み込み
        Bukkit.getPluginManager().registerEvents(this, this);//イベントリスナーの登録
        getCommand("uponlykb").setExecutor(this);//コマンドの登録
        getLogger().info("UpOnlyKB Loaded.");//ログの表示
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {//ダメージ時に発生するノックバック変更ロジック
        debug.debugmessage("=================EntityDamageByEntityEvent triggered.=================");//debug

        //無効化されている、もしくは無敵状態の場合はなにもしない。
        if (!configvalues.isCkbactive()||event.getEntity().isInvulnerable()){
            debug.debugmessage("EventCanceled");//debug
            debug.debugmessage("============================================================");//debug
            return;
        }
        debug.debugmessage("UpOnlyKB is active now.");//debug
        
        Entity entity = event.getEntity();//ダメージを受けたエンティティを取得
        Entity attacker = event.getDamager();//ダメージを与えたエンティティを取得

        //プレイヤーにのみ処理を行う。希望があればプレイヤー以外にも対応させようかな?まぁプレイヤー以外はdataコマンド使って変えれるし。
        if (entity instanceof Player) {
            debug.debugmessage("HurtEntity is Player");//debug
            LivingEntity living = (LivingEntity) entity;//無敵時間かどうかを確認するためのキャスト

            //無敵時間さんが存在しないなら実行
            if (living.getNoDamageTicks() <= 10) {
                debug.debugmessage("Player is not invincible.");//debug

                //キャンセルされていないなら実行
                if (!event.isCancelled()){
                    debug.debugmessage("run CustomKnockback");//debug
                    knockbackcalc kbcalc = new knockbackcalc();//関数を使うのに必須

                    //ノックバックを計算する。
                    Vector knockbackVector = kbcalc.calculateKnockback(entity, attacker, configvalues);
                    entity.setVelocity(knockbackVector);//ノックバックの適用
                    debug.debugmessage(String.valueOf(knockbackVector));//debug
                    debug.debugmessage("============================================================");//debug
                }else{
                    debug.debugmessage("Event is trued Cancelled flags");//debug
                    debug.debugmessage("============================================================");//debug
                }
            }else{
                debug.debugmessage("Player is invincible.");//debug
                debug.debugmessage("NoDamageTicks: "+living.getNoDamageTicks());//debug
                debug.debugmessage("============================================================");//debug
            }
        }else{
            debug.debugmessage("HurtEntity is not Player");//debug
            debug.debugmessage("============================================================");//debug
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityKnockback(EntityKnockbackEvent event) {//ノックバックをキャンセルしてカスタムのもののみにする。
        if (!configvalues.isCkbactive()||event.getEntity().isInvulnerable()){return;}//無効化時は動作させない。
        //プレイヤーにのみ処理を行う。希望があればプレイヤー以外にも対応させようかな?まぁプレイヤー以外はdataコマンド使って変えれるし。
        if (event.getEntity() instanceof Player) {
            //エンティティへの攻撃と盾ブロックのKBをキャンセルする。これでノックバックはカスタムのものだけになるはずで。
            if (event.getCause() == EntityKnockbackEvent.KnockbackCause.ENTITY_ATTACK || event.getCause() == EntityKnockbackEvent.KnockbackCause.SHIELD_BLOCK) {
                event.setCancelled(true);
            }
        }
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {//コマンド処理
        if (label.equalsIgnoreCase("uponlykb")){
            if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
                //コンフィグの再読み込み
                command commandInstance = new command();
                return commandInstance.reloadcommand(sender);
            }else{
                //なんのコマンドを実行したんですか?
                sender.sendMessage("Usage: /uponlykb reload");
                return false;
            }
        }else{
            return false;//なんで呼び出されたんですか?
        }
    }

}

