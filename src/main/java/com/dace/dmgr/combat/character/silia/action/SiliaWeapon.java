package com.dace.dmgr.combat.character.silia.action;

import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.weapon.AbstractWeapon;
import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.temporal.Barrier;
import com.dace.dmgr.combat.interaction.*;
import com.dace.dmgr.util.*;
import com.dace.dmgr.util.task.DelayTask;
import com.dace.dmgr.util.task.TaskUtil;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

import java.util.HashSet;

public final class SiliaWeapon extends AbstractWeapon {
    /** 일격 사용 가능 여부 */
    @Getter
    private boolean isStrike = false;
    /** 검기 방향의 반대 방향 여부 */
    private boolean isOpposite = true;

    SiliaWeapon(@NonNull CombatUser combatUser) {
        super(combatUser, SiliaWeaponInfo.getInstance());
    }

    @Override
    @NonNull
    public ActionKey @NonNull [] getDefaultActionKeys() {
        return new ActionKey[]{ActionKey.LEFT_CLICK};
    }

    @Override
    public long getDefaultCooldown() {
        return SiliaWeaponInfo.COOLDOWN;
    }

    @Override
    public boolean canUse() {
        return super.canUse() && combatUser.getSkill(SiliaP2Info.getInstance()).isDurationFinished();
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        isOpposite = !isOpposite;

        if (isStrike)
            strike();
        else {
            setCooldown();
            combatUser.playMeleeAttackAnimation(-4, 10, true);

            new SiliaWeaponProjectile().shoot();

            SoundUtil.playNamedSound(NamedSound.COMBAT_SILIA_WEAPON_USE, combatUser.getEntity().getLocation());
        }

        if (!combatUser.getSkill(SiliaA3Info.getInstance()).isDurationFinished())
            combatUser.getSkill(SiliaA3Info.getInstance()).onCancelled();
    }

    @Override
    public void onCancelled() {
        super.onCancelled();
        setVisible(true);
    }

    /**
     * 일격을 사용한다.
     */
    private void strike() {
        if (!combatUser.getSkill(SiliaUltInfo.getInstance()).isDurationFinished())
            setCooldown(SiliaUltInfo.STRIKE_COOLDOWN);

        combatUser.setGlobalCooldown(SiliaT2Info.GLOBAL_COOLDOWN);
        combatUser.getWeapon().setVisible(false);
        combatUser.playMeleeAttackAnimation(-2, 6, isOpposite);

        HashSet<CombatEntity> targets = new HashSet<>();

        int delay = 0;
        for (int i = 0; i < 8; i++) {
            final int index = i;

            switch (i) {
                case 1:
                case 2:
                case 6:
                case 7:
                    delay += 1;
                    break;
            }

            TaskUtil.addTask(taskRunner, new DelayTask(() -> {
                Location loc = combatUser.getEntity().getEyeLocation();
                Vector vector = VectorUtil.getPitchAxis(loc);
                Vector axis = VectorUtil.getYawAxis(loc);

                Vector vec = VectorUtil.getRotatedVector(vector, VectorUtil.getRollAxis(loc), isOpposite ? -30 : 30);
                axis = VectorUtil.getRotatedVector(axis, VectorUtil.getRollAxis(loc), isOpposite ? -30 : 30);

                vec = VectorUtil.getRotatedVector(vec, axis, (isOpposite ? 90 - 16 * (index - 3.5) : 90 + 16 * (index - 3.5)));
                new SiliaWeaponStrikeAttack(combatUser, targets).shoot(loc, vec);

                CombatUtil.addYawAndPitch(combatUser.getEntity(), (isOpposite ? -0.5 : 0.5), 0.15);
                if (index < 3)
                    SoundUtil.playNamedSound(NamedSound.COMBAT_SILIA_T2_USE, loc.add(vec), 1, index * 0.12);
                if (index == 7) {
                    CombatUtil.addYawAndPitch(combatUser.getEntity(), (isOpposite ? 0.7 : -0.7), -0.85);
                    onCancelled();
                }
            }, delay));
        }
    }

    /**
     * 일격 사용 가능 여부를 설정한다.
     *
     * @param isStrike 일격 사용 가능 여부
     */
    void setStrike(boolean isStrike) {
        if (isStrike) {
            this.isStrike = true;
            combatUser.getWeapon().setGlowing(true);
            combatUser.getWeapon().displayDurability(SiliaWeaponInfo.RESOURCE.EXTENDED);
        } else {
            this.isStrike = false;
            combatUser.getWeapon().setGlowing(false);
            combatUser.getWeapon().displayDurability(SiliaWeaponInfo.RESOURCE.DEFAULT);
        }
    }

    private final class SiliaWeaponProjectile extends Projectile {
        private SiliaWeaponProjectile() {
            super(combatUser, SiliaWeaponInfo.VELOCITY, ProjectileOption.builder().trailInterval(10).size(SiliaWeaponInfo.SIZE)
                    .maxDistance(SiliaWeaponInfo.DISTANCE).condition(combatUser::isEnemy).build());
        }

        @Override
        protected void trail() {
            for (int i = 0; i < 8; i++) {
                Vector vector = VectorUtil.getYawAxis(getLocation()).multiply(-1);
                Vector axis = VectorUtil.getPitchAxis(getLocation());
                Vector vec = VectorUtil.getRotatedVector(vector, VectorUtil.getRollAxis(getLocation()), isOpposite ? 30 : -30);
                axis = VectorUtil.getRotatedVector(axis, VectorUtil.getRollAxis(getLocation()), isOpposite ? 30 : -30);

                vec = VectorUtil.getRotatedVector(vec, axis, 90 + 20 * (i - 3.5)).multiply(0.8);
                ParticleUtil.playRGB(ParticleUtil.ColoredParticle.REDSTONE, getLocation().clone().add(vec), 2, 0.05, 0.05, 0.05,
                        255, 255, 255);
            }
        }

        @Override
        protected void onHit() {
            ParticleUtil.play(Particle.EXPLOSION_NORMAL, getLocation(), 10, 0.1, 0.1, 0.1, 0.15);
        }

        @Override
        protected boolean onHitBlock(@NonNull Block hitBlock) {
            SoundUtil.playNamedSound(NamedSound.COMBAT_MELEE_ATTACK_HIT_BLOCK, getLocation());
            CombatUtil.playBlockHitSound(getLocation(), hitBlock, 1);
            CombatUtil.playBlockHitEffect(getLocation(), hitBlock, 1.5);

            return false;
        }

        @Override
        protected boolean onHitEntity(@NonNull Damageable target, boolean isCrit) {
            target.getDamageModule().damage(this, SiliaWeaponInfo.DAMAGE, DamageType.NORMAL, getLocation(),
                    SiliaT1.isBackAttack(getVelocity(), target) ? SiliaT1Info.CRIT_MULTIPLIER : 1, true);

            ParticleUtil.play(Particle.CRIT, getLocation(), 15, 0, 0, 0, 0.4);
            SoundUtil.playNamedSound(NamedSound.COMBAT_SILIA_WEAPON_HIT_ENTITY, getLocation());

            return false;
        }
    }

    private final class SiliaWeaponStrikeAttack extends Hitscan {
        private final HashSet<CombatEntity> targets;

        private SiliaWeaponStrikeAttack(CombatUser combatUser, HashSet<CombatEntity> targets) {
            super(combatUser, HitscanOption.builder().trailInterval(5).size(SiliaT2Info.SIZE).maxDistance(SiliaT2Info.DISTANCE)
                    .condition(combatUser::isEnemy).build());

            this.targets = targets;
        }

        @Override
        protected void trail() {
            if (getLocation().distance(combatUser.getEntity().getEyeLocation()) <= 1)
                return;

            Location loc = LocationUtil.getLocationFromOffset(getLocation(), 0, -0.3, 0);
            ParticleUtil.playRGB(ParticleUtil.ColoredParticle.REDSTONE, loc, 8, 0.15, 0.15, 0.15,
                    255, 255, 255);
        }

        @Override
        protected void onHit() {
            ParticleUtil.play(Particle.EXPLOSION_NORMAL, getLocation(), 3, 0.05, 0.05, 0.05, 0.05);
        }

        @Override
        protected boolean onHitBlock(@NonNull Block hitBlock) {
            CombatUtil.playBlockHitEffect(getLocation(), hitBlock, 1.5);
            CombatUtil.playBlockHitSound(getLocation(), hitBlock, 1);

            return false;
        }

        @Override
        protected boolean onHitEntity(@NonNull Damageable target, boolean isCrit) {
            if (targets.add(target)) {
                if (target.getDamageModule().damage(combatUser, SiliaT2Info.DAMAGE, DamageType.NORMAL, getLocation(),
                        SiliaT1.isBackAttack(getVelocity(), target) ? SiliaT1Info.CRIT_MULTIPLIER : 1, true)) {
                    target.getKnockbackModule().knockback(VectorUtil.getRollAxis(combatUser.getEntity().getLocation()).multiply(SiliaT2Info.KNOCKBACK));
                    if (target instanceof CombatUser)
                        combatUser.addScore("일격", SiliaT2Info.DAMAGE_SCORE);
                }

                ParticleUtil.play(Particle.CRIT, getLocation(), 40, 0, 0, 0, 0.4);
                SoundUtil.playNamedSound(NamedSound.COMBAT_SILIA_WEAPON_HIT_ENTITY, getLocation());
            }

            return !(target instanceof Barrier);
        }

        @Override
        protected void onDestroy() {
            Location loc = LocationUtil.getLocationFromOffset(getLocation(), 0, -0.3, 0);
            ParticleUtil.play(Particle.CRIT, loc, 15, 0.08, 0.08, 0.08, 0.08);
        }
    }
}
