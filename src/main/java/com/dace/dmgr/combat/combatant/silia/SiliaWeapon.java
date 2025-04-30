package com.dace.dmgr.combat.combatant.silia;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.CombatEffectUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.weapon.AbstractWeapon;
import com.dace.dmgr.combat.entity.DamageType;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.EntityCondition;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.combat.interaction.Projectile;
import com.dace.dmgr.util.VectorUtil;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.inventory.MainHand;
import org.bukkit.util.Vector;

public final class SiliaWeapon extends AbstractWeapon {
    /** 검기 방향의 반대 방향 여부 */
    private boolean isOpposite = true;

    public SiliaWeapon(@NonNull CombatUser combatUser) {
        super(combatUser, SiliaWeaponInfo.getInstance(), SiliaWeaponInfo.COOLDOWN);
    }

    @Override
    @NonNull
    public ActionKey @NonNull [] getDefaultActionKeys() {
        return new ActionKey[]{ActionKey.LEFT_CLICK};
    }

    @Override
    public boolean canUse(@NonNull ActionKey actionKey) {
        return super.canUse(actionKey) && combatUser.getActionManager().getSkill(SiliaP2Info.getInstance()).isDurationFinished();
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        isOpposite = !isOpposite;

        SiliaT2 skillt2 = combatUser.getActionManager().getTrait(SiliaT2Info.getInstance());

        if (skillt2.isStrike())
            skillt2.useStrike(isOpposite);
        else {
            setCooldown();
            combatUser.playMeleeAttackAnimation(-4, Timespan.ofTicks(10), MainHand.RIGHT);

            new SiliaWeaponProjectile().shot();

            SiliaWeaponInfo.Sounds.USE.play(combatUser.getLocation());
        }

        combatUser.getActionManager().getSkill(SiliaA3Info.getInstance()).cancel();
    }

    @Override
    protected void onCancelled() {
        setVisible(true);
    }

    private final class SiliaWeaponProjectile extends Projectile<Damageable> {
        private SiliaWeaponProjectile() {
            super(SiliaWeapon.this, SiliaWeaponInfo.VELOCITY, EntityCondition.enemy(combatUser),
                    Option.builder().size(SiliaWeaponInfo.SIZE).maxDistance(SiliaWeaponInfo.DISTANCE).build());
        }

        @Override
        protected void onHit(@NonNull Location location) {
            SiliaWeaponInfo.Particles.HIT.play(location);
        }

        @Override
        @NonNull
        protected IntervalHandler getIntervalHandler() {
            return createPeriodIntervalHandler(10, location -> {
                for (int i = 0; i < 8; i++) {
                    Vector vector = VectorUtil.getYawAxis(location).multiply(-1);
                    Vector axis = VectorUtil.getPitchAxis(location);

                    Vector vec = VectorUtil.getRotatedVector(vector, axis, 90 + 20 * (i - 3.5)).multiply(0.8);
                    vec = VectorUtil.getRotatedVector(vec, VectorUtil.getRollAxis(location), isOpposite ? -30 : 30);
                    SiliaWeaponInfo.Particles.BULLET_TRAIL.play(location.clone().add(vec));
                }
            });
        }

        @Override
        @NonNull
        protected HitBlockHandler getHitBlockHandler() {
            return (location, hitBlock) -> {
                SiliaWeaponInfo.Sounds.HIT_BLOCK.play(location);
                CombatEffectUtil.playHitBlockSound(location, hitBlock, 1);
                CombatEffectUtil.playHitBlockParticle(location, hitBlock, 1.5);

                return false;
            };
        }

        @Override
        @NonNull
        protected HitEntityHandler<Damageable> getHitEntityHandler() {
            return (location, target) -> {
                target.getDamageModule().damage(this, SiliaWeaponInfo.DAMAGE, DamageType.NORMAL, location,
                        SiliaT1.getCritMultiplier(getVelocity(), target), true);

                SiliaWeaponInfo.Particles.HIT_ENTITY.play(location);
                SiliaWeaponInfo.Sounds.HIT_ENTITY.play(location);

                return false;
            };
        }
    }
}
