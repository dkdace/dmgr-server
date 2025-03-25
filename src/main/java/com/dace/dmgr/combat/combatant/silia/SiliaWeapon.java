package com.dace.dmgr.combat.combatant.silia;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.CombatEffectUtil;
import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.weapon.AbstractWeapon;
import com.dace.dmgr.combat.action.weapon.Weapon;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.DamageType;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.Movable;
import com.dace.dmgr.combat.interaction.Hitscan;
import com.dace.dmgr.combat.interaction.Projectile;
import com.dace.dmgr.util.LocationUtil;
import com.dace.dmgr.util.VectorUtil;
import com.dace.dmgr.util.task.DelayTask;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.inventory.MainHand;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.function.IntConsumer;

public final class SiliaWeapon extends AbstractWeapon {
    /** 일격 사용 가능 여부 */
    private boolean isStrike = false;
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
        return super.canUse(actionKey) && combatUser.getSkill(SiliaP2Info.getInstance()).isDurationFinished();
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        isOpposite = !isOpposite;

        if (isStrike)
            useStrike();
        else {
            setCooldown();
            combatUser.playMeleeAttackAnimation(-4, Timespan.ofTicks(10), MainHand.RIGHT);

            new SiliaWeaponProjectile().shot();

            SiliaWeaponInfo.SOUND.USE.play(combatUser.getLocation());
        }

        combatUser.getSkill(SiliaA3Info.getInstance()).cancel();
    }

    @Override
    protected void onCancelled() {
        setVisible(true);
    }

    /**
     * 일격을 사용한다.
     */
    private void useStrike() {
        if (!combatUser.getSkill(SiliaUltInfo.getInstance()).isDurationFinished())
            setCooldown(SiliaUltInfo.STRIKE_COOLDOWN);

        combatUser.setGlobalCooldown(SiliaT2Info.GLOBAL_COOLDOWN);
        combatUser.playMeleeAttackAnimation(-2, Timespan.ofTicks(6), isOpposite ? MainHand.LEFT : MainHand.RIGHT);

        combatUser.getWeapon().setVisible(false);

        HashSet<Damageable> targets = new HashSet<>();

        IntConsumer onIndex = i -> {
            Location loc = combatUser.getEntity().getEyeLocation();
            Vector vector = VectorUtil.getPitchAxis(loc);
            Vector axis = VectorUtil.getYawAxis(loc);

            double angle = 16 * (i - 3.5);
            Vector vec = VectorUtil.getRotatedVector(vector, axis, (isOpposite ? 90 + angle : 90 - angle));
            vec = VectorUtil.getRotatedVector(vec, VectorUtil.getRollAxis(loc), isOpposite ? 30 : -30);

            new SiliaWeaponStrikeAttack(targets).shot(loc, vec);

            combatUser.addYawAndPitch(isOpposite ? 0.5 : -0.5, 0.15);
            if (i < 3)
                SiliaT2Info.SOUND.USE.play(loc.add(vec), 1, i / 2.0);
            if (i == 7) {
                combatUser.addYawAndPitch(isOpposite ? -0.7 : 0.7, -0.85);
                cancel();
            }
        };

        int delay = 0;
        int[] delays = {0, 1, 1, 0, 0, 0, 1, 1};

        for (int i = 0; i < delays.length; i++) {
            int index = i;
            delay += delays[i];

            addActionTask(new DelayTask(() -> onIndex.accept(index), delay));
        }
    }

    /**
     * 일격 사용 가능 여부를 설정한다.
     *
     * @param isStrike 일격 사용 가능 여부
     */
    void setStrike(boolean isStrike) {
        this.isStrike = isStrike;

        Weapon weapon = combatUser.getWeapon();
        weapon.setGlowing(isStrike);
        weapon.setDurability(isStrike ? SiliaWeaponInfo.RESOURCE.EXTENDED : SiliaWeaponInfo.RESOURCE.DEFAULT);
    }

    private final class SiliaWeaponProjectile extends Projectile<Damageable> {
        private SiliaWeaponProjectile() {
            super(SiliaWeapon.this, SiliaWeaponInfo.VELOCITY, CombatUtil.EntityCondition.enemy(combatUser),
                    Option.builder().size(SiliaWeaponInfo.SIZE).maxDistance(SiliaWeaponInfo.DISTANCE).build());
        }

        @Override
        protected void onHit(@NonNull Location location) {
            SiliaWeaponInfo.PARTICLE.HIT.play(location);
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
                    SiliaWeaponInfo.PARTICLE.BULLET_TRAIL.play(location.clone().add(vec));
                }
            });
        }

        @Override
        @NonNull
        protected HitBlockHandler getHitBlockHandler() {
            return (location, hitBlock) -> {
                SiliaWeaponInfo.SOUND.HIT_BLOCK.play(location);
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

                SiliaWeaponInfo.PARTICLE.HIT_ENTITY.play(location);
                SiliaWeaponInfo.SOUND.HIT_ENTITY.play(location);

                return false;
            };
        }
    }

    private final class SiliaWeaponStrikeAttack extends Hitscan<Damageable> {
        private final HashSet<Damageable> targets;

        private SiliaWeaponStrikeAttack(@NonNull HashSet<Damageable> targets) {
            super(combatUser, CombatUtil.EntityCondition.enemy(combatUser), Option.builder().size(SiliaT2Info.SIZE).maxDistance(SiliaT2Info.DISTANCE).build());
            this.targets = targets;
        }

        @Override
        protected void onHit(@NonNull Location location) {
            SiliaT2Info.PARTICLE.HIT.play(location);
        }

        @Override
        protected void onDestroy(@NonNull Location location) {
            Location loc = LocationUtil.getLocationFromOffset(location, 0, -0.3, 0);
            SiliaT2Info.PARTICLE.BULLET_TRAIL_DECO.play(loc);
        }

        @Override
        @NonNull
        protected IntervalHandler getIntervalHandler() {
            return createPeriodIntervalHandler(5, location -> {
                if (getTravelDistance() <= 1)
                    return;

                Location loc = LocationUtil.getLocationFromOffset(location, 0, -0.3, 0);
                SiliaT2Info.PARTICLE.BULLET_TRAIL_CORE.play(loc);
            });
        }

        @Override
        @NonNull
        protected HitBlockHandler getHitBlockHandler() {
            return (location, hitBlock) -> {
                CombatEffectUtil.playHitBlockParticle(location, hitBlock, 1.5);
                CombatEffectUtil.playHitBlockSound(location, hitBlock, 1);

                return false;
            };
        }

        @Override
        @NonNull
        protected HitEntityHandler<Damageable> getHitEntityHandler() {
            return (location, target) -> {
                if (targets.add(target)) {
                    if (target.getDamageModule().damage(combatUser, SiliaT2Info.DAMAGE, DamageType.NORMAL, location,
                            SiliaT1.getCritMultiplier(combatUser.getLocation().getDirection(), target), true)) {

                        if (target instanceof Movable) {
                            Vector dir = combatUser.getLocation().getDirection().normalize().multiply(SiliaT2Info.KNOCKBACK);
                            ((Movable) target).getMoveModule().knockback(dir);
                        }

                        if (combatUser.getSkill(SiliaUltInfo.getInstance()).isDurationFinished() && target instanceof CombatUser)
                            combatUser.addScore("일격", SiliaT2Info.DAMAGE_SCORE);
                    }

                    SiliaT2Info.PARTICLE.HIT_ENTITY.play(location);
                    SiliaWeaponInfo.SOUND.HIT_ENTITY.play(location);
                }

                return true;
            };
        }
    }
}
