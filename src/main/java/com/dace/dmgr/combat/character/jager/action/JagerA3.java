package com.dace.dmgr.combat.character.jager.action;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.Timestamp;
import com.dace.dmgr.combat.CombatEffectUtil;
import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.ActiveSkill;
import com.dace.dmgr.combat.entity.*;
import com.dace.dmgr.combat.entity.module.statuseffect.Snare;
import com.dace.dmgr.combat.entity.temporary.Barrier;
import com.dace.dmgr.combat.interaction.Area;
import com.dace.dmgr.combat.interaction.BouncingProjectile;
import com.dace.dmgr.combat.interaction.Projectile;
import com.dace.dmgr.util.LocationUtil;
import com.dace.dmgr.util.task.DelayTask;
import com.dace.dmgr.util.task.IntervalTask;
import com.dace.dmgr.util.task.TaskUtil;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.inventory.MainHand;
import org.jetbrains.annotations.Nullable;

@Getter
public final class JagerA3 extends ActiveSkill {
    /** 폭발 타임스탬프 */
    private Timestamp explodeTimestamp = Timestamp.now();
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
            combatUser.setGlobalCooldown(Timespan.ofTicks(JagerA3Info.READY_DURATION));
            combatUser.getWeapon().setVisible(false);

            JagerA3Info.SOUND.USE.play(combatUser.getLocation());

            TaskUtil.addTask(taskRunner, new DelayTask(() -> {
                isEnabled = true;
                explodeTimestamp = Timestamp.now().plus(Timespan.ofTicks(JagerA3Info.EXPLODE_DURATION));

                JagerA3Info.SOUND.USE_READY.play(combatUser.getLocation());

                TaskUtil.addTask(taskRunner, new IntervalTask(i -> {
                    if (isDurationFinished())
                        return false;

                    Location loc = LocationUtil.getLocationFromOffset(combatUser.getArmLocation(MainHand.RIGHT), 0, 0, 0.3);
                    JagerA3Info.PARTICLE.BULLET_TRAIL.play(loc);

                    return true;
                }, isCancelled -> {
                    if (isCancelled)
                        return;

                    onCancelled();

                    Location loc = LocationUtil.getLocationFromOffset(combatUser.getArmLocation(MainHand.RIGHT), 0, 0, 0.3);
                    explode(loc, null);
                }, 1, JagerA3Info.EXPLODE_DURATION));
            }, JagerA3Info.READY_DURATION));
        } else {
            onCancelled();
            combatUser.getWeapon().setCooldown(2);

            Location loc = combatUser.getArmLocation(MainHand.RIGHT);
            new JagerA3Projectile().shot(loc);

            CombatEffectUtil.THROW_SOUND.play(loc);
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
        isEnabled = false;
        combatUser.getWeapon().setVisible(true);
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

        JagerA3Info.SOUND.EXPLODE.play(loc);
        JagerA3Info.PARTICLE.EXPLODE.play(loc);
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
                ((CombatUser) combatEntity).getUser().sendTitle("§c§l얼어붙음!", "", Timespan.ZERO, Timespan.ofTicks(2), Timespan.ofTicks(10));

            if (combatEntity.getDamageModule().isLiving())
                JagerA3Info.PARTICLE.FREEZE_TICK.play(combatEntity.getCenterLocation(), combatEntity.getWidth(), combatEntity.getHeight());
        }
    }

    private final class JagerA3Projectile extends BouncingProjectile<Damageable> {
        private JagerA3Projectile() {
            super(combatUser, JagerA3Info.VELOCITY, CombatUtil.EntityCondition.enemy(combatUser),
                    Projectile.Option.builder().duration(Timestamp.now().until(explodeTimestamp)).build(),
                    Option.builder().bounceVelocityMultiplier(0.35).build());
        }

        @Override
        protected void onDestroy(@NonNull Location location) {
            explode(location, this);
        }

        @Override
        @NonNull
        protected IntervalHandler getIntervalHandler() {
            return IntervalHandler
                    .chain(createGravityIntervalHandler())
                    .next(createPeriodIntervalHandler(8, JagerA3Info.PARTICLE.BULLET_TRAIL::play));
        }

        @Override
        @NonNull
        protected HitBlockHandler getPreHitBlockHandler() {
            return (location, hitBlock) -> {
                if (getVelocity().length() > 0.01)
                    CombatEffectUtil.THROW_BOUNCE_SOUND.play(location, 1 + getVelocity().length() * 2);

                return true;
            };
        }

        @Override
        @NonNull
        protected HitEntityHandler<Damageable> getPreHitEntityHandler() {
            return (location, target) -> {
                if (getVelocity().length() > 0.05)
                    target.getDamageModule().damage(this, JagerA3Info.DAMAGE_DIRECT, DamageType.NORMAL, location, false, true);

                return true;
            };
        }
    }

    private final class JagerA3Area extends Area<Damageable> {
        @Nullable
        private final JagerA3Projectile projectile;

        private JagerA3Area(@Nullable JagerA3Projectile projectile) {
            super(combatUser, JagerA3Info.RADIUS, CombatUtil.EntityCondition.enemy(combatUser).include(combatUser));
            this.projectile = projectile;
        }

        @Override
        protected boolean onHitBlock(@NonNull Location center, @NonNull Location location, @NonNull Block hitBlock) {
            return false;
        }

        @Override
        protected boolean onHitEntity(@NonNull Location center, @NonNull Location location, @NonNull Damageable target) {
            double distance = center.distance(location);
            double damage = CombatUtil.getDistantDamage(JagerA3Info.DAMAGE_EXPLODE, distance, JagerA3Info.RADIUS / 2.0);
            int freeze = (int) CombatUtil.getDistantDamage(JagerA3Info.FREEZE, distance, JagerA3Info.RADIUS / 2.0);
            boolean isDamaged = projectile == null
                    ? target.getDamageModule().damage(combatUser, damage, DamageType.NORMAL, null, false, true)
                    : target.getDamageModule().damage(projectile, damage, DamageType.NORMAL, null, false, true);

            if (isDamaged) {
                target.getKnockbackModule().knockback(LocationUtil.getDirection(center, location.add(0, 0.5, 0))
                        .multiply(JagerA3Info.KNOCKBACK));
                JagerT1.addFreezeValue(target, freeze);

                if (target.getPropertyManager().getValue(Property.FREEZE) >= JagerT1Info.MAX) {
                    target.getStatusEffectModule().applyStatusEffect(combatUser, Freeze.instance, JagerA3Info.SNARE_DURATION);
                    if (target != combatUser) {
                        combatUser.getSkill(JagerP1Info.getInstance()).setTarget(target);
                        combatUser.useAction(ActionKey.PERIODIC_1);

                        if (target instanceof CombatUser)
                            combatUser.addScore("적 얼림", JagerA3Info.SNARE_SCORE);
                    }
                }
            }

            return !(target instanceof Barrier);
        }
    }
}
