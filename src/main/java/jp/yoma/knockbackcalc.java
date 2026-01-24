package jp.yoma;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Arrow;
import org.bukkit.inventory.ItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.util.Vector;
import org.bukkit.Location;
import org.bukkit.potion.PotionEffectType;

public class knockbackcalc {//ノックバックの計算(これを別のクラスにするの大丈夫か...?)
    public Vector calculateKnockback(Entity player, Entity attacker, configvalues configvalues) {
        Location attackerpos = attacker.getLocation();//攻撃者の位置を取得。目的は向きのみ
        Location playerloc = player.getLocation();//攻撃受けた側の位置を取得
        float yaw = attackerpos.getYaw();//攻撃者の向きを取得
        int knockbacklevel = 0;//ノックバックエンチャレベルの初期化

        debug.getInstance().debugmessage("yaw: "+yaw);//debug

        //飛び道具用のコード
        if (attacker instanceof Projectile) {
            debug.getInstance().debugmessage("Attacker is Projectile");//debug
            Vector prjvec = attacker.getVelocity();//飛び道具の速度ベクトルを取得
            if (prjvec.lengthSquared() < 0.001) {//速度ベクトルがゼロに近い場合の対策
                debug.getInstance().debugmessage("Why is velocity zero?");//debug 0になるタイミングがわからないけど
                prjvec = playerloc.toVector().subtract(attackerpos.toVector());//※動くとは思ってない。
            }
            //飛び道具のベクトルからYawを計算
            Location dummy = new Location(player.getWorld(), 0, 0, 0);
            dummy.setDirection(prjvec); 
            yaw = dummy.getYaw();
            debug.getInstance().debugmessage("yaw: "+yaw);//debug

            //飛び道具が矢の場合、矢に付与されているノックバックエンチャレベルを取得
            if (attacker instanceof Arrow) {
                debug.getInstance().debugmessage("Projectile is Arrow");//debug
                ItemStack arrowweapon = ((Arrow) attacker).getWeapon();//矢を放った武器を取得
                debug.getInstance().debugmessage("Arrow weapon: "+arrowweapon);//debug
                if (arrowweapon != null && arrowweapon.hasItemMeta()) {//武器のノックバックエンチャントレベルを取得
                    knockbacklevel += arrowweapon.getItemMeta().getEnchantLevel(Enchantment.PUNCH);//パンチ
                    knockbacklevel += arrowweapon.getItemMeta().getEnchantLevel(Enchantment.KNOCKBACK);//ノックバック(本来は付きません)
                    debug.getInstance().debugmessage("Arrow Knockback Level: "+knockbacklevel);//debug
                }
            }
        }

        //向きからXZの成分を計算
        double angle = Math.toRadians(yaw);
        double cos = Math.cos(angle);
        double sin = -Math.sin(angle);

        debug.getInstance().debugmessage("angle: "+angle+", sin: "+sin+", cos: "+cos);//debug

        double extrahkb = 1.0;//追加ノックバック倍率の初期設定
        double extravkb = 1.0;//クリティカルノックバック倍率の初期設定

        double HorizonKB = configvalues.getHorizontalMultiplier();//水平KB倍率の取得

        boolean shieldblock = false;//盾ガードフラグ

        //攻撃者がダッシュ中なら追加KBを加算するものの、プレイヤーにしかダッシュは存在しない。
        if (attacker instanceof Player) {
            Player attackerPlayer = (Player) attacker;
            if (attackerPlayer.isSprinting()) {//ダッシュ攻撃
                extrahkb = configvalues.getSprintkb();
                debug.getInstance().debugmessage("SprintKB");//debug
            }
            if (isCritical(attackerPlayer)) {//クリティカル攻撃
                extravkb = configvalues.getCritkb();
                debug.getInstance().debugmessage("CriticalKB");//debug
            }
            ItemStack weapon = attackerPlayer.getInventory().getItemInMainHand();
            debug.getInstance().debugmessage("ItemStack: "+weapon);//debug
            if (weapon != null && weapon.hasItemMeta()) {//武器のノックバックエンチャントレベルを取得
                knockbacklevel += weapon.getItemMeta().getEnchantLevel(Enchantment.KNOCKBACK);//ノックバック
                knockbacklevel += weapon.getItemMeta().getEnchantLevel(Enchantment.PUNCH);//パンチ(本来は効果ありません)
                debug.getInstance().debugmessage("Weapon Knockback Level: "+knockbacklevel+", added: "+weapon.getItemMeta().getEnchantLevel(Enchantment.KNOCKBACK));//debug
            }
        }
        if (player instanceof Player) {//盾のガード判定
            Player playerEntity = (Player) player;
            debug.getInstance().debugmessage("Player is Player");//debug

            //プレイヤーは盾でガードしているか
            if (playerEntity.isBlocking()){
                debug.getInstance().debugmessage("Player is blocking");//debug
                Vector direction = playerloc.getDirection();
                Vector towardAttacker = attackerpos.subtract(playerloc).toVector();
                double dot = direction.dot(towardAttacker);
                debug.getInstance().debugmessage("direction: "+direction+", towardAttacker: "+towardAttacker+", dot: "+dot);//debug
                if (dot >= 0){//ガード成功する向きか
                    shieldblock = true;
                    debug.getInstance().debugmessage("Shield block success");//debug
                }
            }
            //盾ガード判定は不安定で、向きによってはガードできてるのにfalseになることがあります。
        }

        extrahkb += knockbacklevel * configvalues.getKBEnchantMultiplier();//ノックバックエンチャントによる追加KB

        debug.getInstance().debugmessage("extrahkb: "+extrahkb+", added: "+(knockbacklevel * configvalues.getKBEnchantMultiplier()));//debug

        /* 未使用のプレイヤーの速度考慮版
        return new Vector(
            (playerVelocity.getX() + sin * configvalues.getHorizontalMultiplier() * extrakb),
            configvalues.getVerticalBoost(),
            (playerVelocity.getZ() + cos * configvalues.getHorizontalMultiplier() * extrakb)
        );
        */

        //ノックバック速度を返す
        debug.getInstance().debugmessage("Flag: "+!configvalues.isCkbactiveonshield()+"+"+shieldblock+"="+(!configvalues.isCkbactiveonshield() && shieldblock));
        if (!configvalues.isCkbactiveonshield() && shieldblock) {//盾でガードするとノックバックしないやつ
            debug.getInstance().debugmessage("BlockSuccessKB");
            return new Vector(0,0,0);
        }else{
            //通常ノックバック速度
            debug.getInstance().debugmessage("NormalKB");
            return new Vector(//ノックバックの設定
                (sin * HorizonKB * extrahkb),
                configvalues.getVerticalBoost() * extravkb,
                (cos * HorizonKB * extrahkb)
            );
        }
    }

    private boolean isCritical(Player player) {//クリティカル攻撃検知(現在のJava版は本来だとダッシュ中にクリティカルにはならない。)
        Entity entity = (Entity) player;
        return player.getFallDistance() > 0.0F
            && !entity.isOnGround()
            && !player.isInsideVehicle()
            && !player.hasPotionEffect(PotionEffectType.BLINDNESS)
            && player.getLocation().getBlock().getType() != org.bukkit.Material.LADDER
            && player.getLocation().getBlock().getType() != org.bukkit.Material.VINE
            && !player.isInWater();
    }
}
