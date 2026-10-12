package com.group_finity.mascot.combat;

import java.awt.Rectangle;

/** Independent projectile geometry and lifecycle, in world coordinates; no windows or timers. */
public final class WeaponSystem {
    private WeaponSystem() { }
    public record ProjectileSpec(String weapon,double spawnX,double spawnY,int direction,double speed,
            int lifetime,Rectangle hitbox,int damage,String effect,boolean destroyOnHit) {
        public static ProjectileSpec of(CombatProfile.Attack a,double x,double y,boolean right,double scale) {
            // Attack XML uses feet-relative projectile boxes; turn them into centered local boxes.
            Rectangle authored=a.hitbox();
            Rectangle local=new Rectangle(authored.x,authored.y-a.polish().projectileY(),authored.width,authored.height);
            return new ProjectileSpec(a.polish().weapon(),x+(right?1:-1)*a.polish().projectileX()*scale,y+a.polish().projectileY()*scale,right?1:-1,
                a.projectileSpeed()*scale,Math.min(250,(int)Math.ceil(a.range()/a.projectileSpeed())+2),
                local,a.damage(),a.effect(),true);
        }
        public Rectangle sweep(double oldX,double newX,double y,double scale) {
            Rectangle before=CombatMatch.box(hitbox,oldX,y,direction>0,scale);
            return before.union(CombatMatch.box(hitbox,newX,y,direction>0,scale));
        }
    }
}
