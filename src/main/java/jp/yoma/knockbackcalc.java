package jp.yoma;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.bukkit.Location;
import org.bukkit.potion.PotionEffectType;

public class knockbackcalc {//ノックバックの計算(これを別のクラスにするの大丈夫か...?)
    public Vector calculateKnockback(Entity player, Entity attacker, configvalues configvalues) {
        Location attackerpos = attacker.getLocation();//攻撃者の位置を取得。目的は向きのみ
        Location playerloc = player.getLocation();
        float yaw = attackerpos.getYaw();//攻撃者の向きを取得

        //向きからXZの成分を計算
        double angle = Math.toRadians(yaw);
        double cos = Math.cos(angle);
        double sin = -Math.sin(angle);

        double extrahkb = 1.0;//追加ノックバック倍率の初期設定
        double extravkb = 1.0;//クリティカルノックバック倍率の初期設定

        double HorizonKB = configvalues.getHorizontalMultiplier();//水平KB倍率の取得

        boolean shieldblock = false;//盾ガードフラグ

        //攻撃者がダッシュ中なら追加KBを加算するものの、プレイヤーにしかダッシュは存在しない。
        if (attacker instanceof Player) {
            Player attackerPlayer = (Player) attacker;
            if (attackerPlayer.isSprinting()) {//ダッシュ攻撃
                extrahkb = configvalues.getSprintkb();
            }
            if (isCritical(attackerPlayer)) {//クリティカル攻撃
                extravkb = configvalues.getCritkb();
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
                    if (dot >= 0){//ガード成功する向きか
                        shieldblock = true;
                        debug.getInstance().debugmessage("Shield block success");//debug
                    }
                }
            }
        }

        /* 未使用
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
            debug.getInstance().debugmessage("NormalKB");
            return new Vector(//ノックバックの設定
                (sin * HorizonKB * extrahkb),
                configvalues.getVerticalBoost() * extravkb,
                (cos * HorizonKB * extrahkb)
            );
        }
    }

    private boolean isCritical(Player player) {//クリティカル攻撃検知
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
