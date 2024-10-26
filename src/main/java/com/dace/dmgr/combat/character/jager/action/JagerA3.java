package com.dace.dmgr.combat.character.jager.action;

import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.ActiveSkill;
import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.Property;
import com.dace.dmgr.combat.entity.module.statuseffect.Snare;
import com.dace.dmgr.combat.entity.temporary.Barrier;
import com.dace.dmgr.combat.interaction.*;
import com.dace.dmgr.util.*;
import com.dace.dmgr.util.task.DelayTask;
import com.dace.dmgr.util.task.IntervalTask;
import com.dace.dmgr.util.task.TaskUtil;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.jetbrains.annotations.Nullable;

@Getter
public final class JagerA3 extends ActiveSkill {
    /** 쿨타임 ID */
    private static final String COOLDOWN_ID = "ExplodeDuration";
    /** 활성화 완료 여부 */
    private boolean isEnabled = false;

    public JagerA3(@NonNull CombatUser combatUser) {
        super(combatUser, JagerA3Info.getInstance(), 2);
    }

    @Override
    public int getPriority() {
        return 2;
    }

    @Override
    @NonNull
    public ActionKey @NonNull [] getDefaultActionKeys() {
        return new ActionKey[]{ActionKey.SLOT_3, ActionKey.LEFT_CLICK};
    }

    @Override
    public long getDefaultCooldown() {
        return JagerA3Info.COOLDOWN;
    }

    @Override
    public long getDefaultDuration() {
        return -1;
    }

    @Override
    public boolean canUse(@NonNull ActionKey actionKey) {
        return super.canUse(actionKey) && !combatUser.getSkill(JagerA1Info.getInstance()).getConfirmModule().isChecking();
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        if (isDurationFinished()) {
            if (actionKey != ActionKey.SLOT_3)
                return;

            setDuration();
            combatUser.getWeapon().onCancelled();
            combatUser.setGlobalCooldown((int) JagerA3Info.READY_DURATION);
            combatUser.getWeapon().setVisible(false);

            SoundUtil.playNamedSound(NamedSound.COMBAT_JAGER_A3_USE, combatUser.getEntity().getLocation());

            TaskUtil.addTask(taskRunner, new DelayTask(() -> {
                isEnabled = true;
                CooldownUtil.setCooldown(combatUser, COOLDOWN_ID, JagerA3Info.EXPLODE_DURATION);

                SoundUtil.playNamedSound(NamedSound.COMBAT_JAGER_A3_USE_READY, combatUser.getEntity().getLocation());

                TaskUtil.addTask(taskRunner, new IntervalTask(i -> {
                    if (isDurationFinished())
                        return false;

                    Location loc = LocationUtil.getLocationFromOffset(combatUser.getArmLocation(true), 0, 0, 0.3);
                    playTickEffect(loc);

                    return true;
                }, isCancelled -> {
                    if (isCancelled)
                        return;

                    Location loc = LocationUtil.getLocationFromOffset(combatUser.getArmLocation(true), 0, 0, 0.3);
                    explode(loc, null);

                    isEnabled = false;
                    onCancelled();
                }, 1, JagerA3Info.EXPLODE_DURATION));
            }, JagerA3Info.READY_DURATION));
        } else {
            combatUser.getWeapon().setCooldown(2);

            Location loc = combatUser.getArmLocation(true);
            new JagerA3Projectile().shoot(loc);

            SoundUtil.playNamedSound(NamedSound.COMBAT_THROW, loc);

            isEnabled = false;
            onCancelled();
        }
    }

    @Override
    public boolean isCancellable() {
        return !isEnabled && !isDurationFinished();
    }

    @Override
    public void onCancelled() {
        super.onCancelled();

        setDuration(0);
        combatUser.getWeapon().setVisible(true);
    }

    /**
     * 수류탄 표시 효과를 재생한다.
     *
     * @param location 사용 위치
     */
    private void playTickEffect(@NonNull Location location) {
        ParticleUtil.playRGB(ParticleUtil.ColoredParticle.REDSTONE, location, 3, 0.1, 0.1, 0.1, 120, 220, 240);
    }

    /**
     * 수류탄 폭파 시 실행할 작업.
     *
     * @param location   폭파 위치
     * @param projectile 투사체
     */
    private void explode(@NonNull Location location, @Nullable JagerA3Projectile projectile) {
        Location loc = location.clone().add(0, 0.1, 0);
        new JagerA3Area(projectile).emit(loc);

        SoundUtil.playNamedSound(NamedSound.COMBAT_JAGER_A3_EXPLODE, loc);
        ParticleUtil.playBlock(ParticleUtil.BlockParticle.BLOCK_DUST, Material.ICE, 0, loc,
                300, 0.2, 0.2, 0.2, 0.5);
        ParticleUtil.playBlock(ParticleUtil.BlockParticle.BLOCK_DUST, Material.PACKED_ICE, 0, loc,
                300, 0.2, 0.2, 0.2, 0.5);
        ParticleUtil.play(Particle.EXPLOSION_LARGE, loc, 1, 0, 0, 0, 0);
        ParticleUtil.play(Particle.FIREWORKS_SPARK, loc, 200, 0, 0, 0, 0.3);
    }

    /**
     * 빙결 상태 효과 클래스.
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class Freeze extends Snare {
        private static final Freeze instance = new Freeze();

        @Override
        public void onTick(@NonNull Damageable combatEntity, @NonNull CombatEntity provider, long i) {
            if (combatEntity instanceof CombatUser)
                ((CombatUser) combatEntity).getUser().sendTitle("§c§l얼어붙음!", "", 0, 2, 10);

            if (combatEntity.getDamageModule().isLiving())
                ParticleUtil.playRGB(ParticleUtil.ColoredParticle.REDSTONE,
                        combatEntity.getCenterLocation(), 5, combatEntity.getEntity().getWidth() / 2,
                        combatEntity.getEntity().getHeight() / 2, combatEntity.getEntity().getWidth() / 2,
                        120, 220, 240);
        }
    }

    private final class JagerA3Projectile extends BouncingProjectile {
        private JagerA3Projectile() {
            super(combatUser, JagerA3Info.VELOCITY, -1, ProjectileOption.builder().trailInterval(8)
                    .duration(CooldownUtil.getCooldown(combatUser, COOLDOWN_ID)).hasGravity(true)
                    .condition(combatUser::isEnemy).build(), BouncingProjectileOption.builder().bounceVelocityMultiplier(0.35).build());
        }

        @Override
        protected void onTrailInterval() {
            playTickEffect(getLocation());
        }

        @Override
        protected void onHitBlockBouncing(@NonNull Block hitBlock) {
            if (getVelocity().length() > 0.01)
                SoundUtil.playNamedSound(NamedSound.COMBAT_THROW_BOUNCE, getLocation(), 1 + getVelocity().length() * 2);
        }

        @Override
        protected boolean onHitEntityBouncing(@NonNull Damageable target, boolean isCrit) {
            if (getVelocity().length() > 0.05)
                target.getDamageModule().damage(this, JagerA3Info.DAMAGE_DIRECT, DamageType.NORMAL, getLocation(), false, true);
            return false;
        }

        @Override
        protected void onDestroy() {
            explode(getLocation(), this);
        }
    }

    private final class JagerA3Area extends Area {
        private final JagerA3Projectile projectile;

        private JagerA3Area(JagerA3Projectile projectile) {
            super(combatUser, JagerA3Info.RADIUS, combatEntity -> combatEntity.isEnemy(JagerA3.this.combatUser) || combatEntity == JagerA3.this.combatUser);
            this.projectile = projectile;
        }

        @Override
        protected boolean onHitBlock(@NonNull Location center, @NonNull Location location, @NonNull Block hitBlock) {
            return false;
        }

        @Override
        public boolean onHitEntity(@NonNull Location center, @NonNull Location location, @NonNull Damageable target) {
            double distance = center.distance(location);
            int damage = CombatUtil.getDistantDamage(JagerA3Info.DAMAGE_EXPLODE, distance, JagerA3Info.RADIUS / 2.0);
            int freeze = CombatUtil.getDistantDamage(JagerA3Info.FREEZE, distance, JagerA3Info.RADIUS / 2.0);
            boolean isDamaged = projectile == null ?
                    target.getDamageModule().damage(combatUser, damage, DamageType.NORMAL, null, false, true) :
                    target.getDamageModule().damage(projectile, damage, DamageType.NORMAL, null, false, true);

            if (isDamaged) {
                target.getKnockbackModule().knockback(LocationUtil.getDirection(center, location.add(0, 0.5, 0)).multiply(JagerA3Info.KNOCKBACK));
                JagerT1.addFreezeValue(target, freeze);

                if (target.getPropertyManager().getValue(Property.FREEZE) >= JagerT1Info.MAX) {
                    target.getStatusEffectModule().applyStatusEffect(combatUser, Freeze.instance, JagerA3Info.SNARE_DURATION);
                    if (target != combatUser) {
                        if (target instanceof CombatUser)
                            combatUser.addScore("적 얼림", JagerA3Info.SNARE_SCORE);

                        JagerP1 skillp1 = combatUser.getSkill(JagerP1Info.getInstance());
                        skillp1.setTarget(target);
                        combatUser.useAction(ActionKey.PERIODIC_1);
                    }
                }
            }

            return !(target instanceof Barrier);
        }
    }
}
