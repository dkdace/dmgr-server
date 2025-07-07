package com.dace.dmgr.combat.combatant.jager;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.Timestamp;
import com.dace.dmgr.combat.CombatEffectUtil;
import com.dace.dmgr.combat.ability.ActionBarDisplay;
import com.dace.dmgr.combat.ability.ActionKey;
import com.dace.dmgr.combat.ability.handler.LeftClickHandler;
import com.dace.dmgr.combat.ability.skill.ActiveSkill;
import com.dace.dmgr.combat.ability.weapon.Weapon;
import com.dace.dmgr.combat.entity.DamageType;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.EntityCondition;
import com.dace.dmgr.combat.entity.Movable;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.combat.entity.module.statuseffect.Snare;
import com.dace.dmgr.combat.entity.temporary.Barrier;
import com.dace.dmgr.combat.interaction.Area;
import com.dace.dmgr.combat.interaction.BouncingProjectile;
import com.dace.dmgr.combat.interaction.Projectile;
import com.dace.dmgr.util.location.LocationUtil;
import com.dace.dmgr.util.task.DelayTask;
import com.dace.dmgr.util.task.IntervalTask;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.inventory.MainHand;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

public final class JagerA3 extends ActiveSkill implements LeftClickHandler {
    /** 폭발 타임스탬프 */
    private Timestamp explodeTimestamp = Timestamp.now();
    /** 활성화 완료 여부 */
    private boolean isEnabled = false;

    public JagerA3(@NonNull CombatUser combatUser, @NonNull JagerA3Info skillInfo) {
        super(combatUser, skillInfo, JagerA3Info.COOLDOWN, Timespan.MAX);
    }

    @Override
    public int getPriority() {
        return 2;
    }

    @Override
    @Nullable
    public ActionBarDisplay getActionBarDisplay() {
        if (isDurationFinished() || !isEnabled)
            return null;

        return ActionBarDisplay.builder(this).title().keyInfo("투척").build();
    }

    @Override
    protected boolean canUse() {
        return super.canUse() && !combatUser.getAbilityManager().getAbility(JagerA1Info.getInstance()).getConfirmModule().isChecking();
    }

    @Override
    @NonNull
    public ActionKey.Slot getSlot() {
        return ActionKey.Slot.SLOT_3;
    }

    @Override
    public void onSlot() {
        if (!isDurationFinished()) {
            onThrow();
            return;
        }

        setDuration();
        combatUser.setGlobalCooldown(JagerA3Info.READY_DURATION);

        Weapon weapon = combatUser.getAbilityManager().getWeapon();
        weapon.cancel();
        weapon.setVisible(false);

        JagerA3Info.Effects.USE.play(combatUser.getLocation());

        addActionTask(new DelayTask(() -> {
            isEnabled = true;
            explodeTimestamp = Timestamp.now().plus(JagerA3Info.EXPLODE_DURATION);

            JagerA3Info.Effects.USE_READY.play(combatUser.getLocation());

            addActionTask(new IntervalTask(i -> {
                Location loc = LocationUtil.getLocationFromOffset(combatUser.getArmLocation(MainHand.RIGHT), 0, 0, 0.3);
                JagerA3Info.Effects.BULLET_TRAIL.play(loc);
            }, () -> {
                forceCancel();

                Location loc = LocationUtil.getLocationFromOffset(combatUser.getArmLocation(MainHand.RIGHT), 0, 0, 0.3);
                onExplode(loc, null);
            }, 1, JagerA3Info.EXPLODE_DURATION.toTicks()));
        }, JagerA3Info.READY_DURATION.toTicks()));
    }

    @Override
    public void onLeftClick() {
        if (!isDurationFinished())
            onThrow();
    }

    @Override
    public boolean isCancellable() {
        return (!isEnabled || combatUser.isDead()) && !isDurationFinished();
    }

    @Override
    protected void onCancelled() {
        isEnabled = false;

        setDuration(Timespan.ZERO);
        combatUser.getAbilityManager().getWeapon().setVisible(true);
    }

    /**
     * 수류탄 투척 시 실행할 작업.
     */
    private void onThrow() {
        forceCancel();

        combatUser.getAbilityManager().getWeapon().setCooldown(Timespan.ofTicks(2));

        Location loc = combatUser.getArmLocation(MainHand.RIGHT);
        new JagerA3Projectile().shot(loc);

        JagerA3Info.Effects.THROW.play(loc);
    }

    /**
     * 수류탄 폭파 시 실행할 작업.
     *
     * @param location   폭파 위치
     * @param projectile 투사체
     */
    private void onExplode(@NonNull Location location, @Nullable JagerA3Projectile projectile) {
        Location loc = location.clone().add(0, 0.1, 0);
        new JagerA3Area(projectile).emit(loc);

        JagerA3Info.Effects.EXPLODE.play(loc);
    }

    /**
     * 빙결 상태 효과 클래스.
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class Freeze extends Snare {
        private static final Freeze instance = new Freeze();

        @Override
        public void onTick(@NonNull Damageable combatEntity, long i) {
            if (combatEntity instanceof CombatUser)
                ((CombatUser) combatEntity).getUser().sendTitle("§c§l얼어붙음!", "", Timespan.ZERO, Timespan.ofTicks(2), Timespan.ofTicks(10));

            if (combatEntity.isCreature())
                JagerA3Info.Effects.FREEZE_TICK.apply(combatEntity).play(combatEntity.getCenterLocation());
        }
    }

    private final class JagerA3Projectile extends BouncingProjectile<Damageable> {
        private JagerA3Projectile() {
            super(JagerA3.this, JagerA3Info.VELOCITY, EntityCondition.enemy(combatUser),
                    Projectile.Option.builder().duration(Timestamp.now().until(explodeTimestamp)).build(),
                    Option.builder().bounceVelocityMultiplier(0.35).build());
        }

        @Override
        protected void onDestroy(@NonNull Location location, boolean isForce) {
            if (!isForce)
                onExplode(location, this);
        }

        @Override
        @NonNull
        protected IntervalHandler getIntervalHandler() {
            return IntervalHandler
                    .chain(createGravityIntervalHandler())
                    .next(createPeriodIntervalHandler(8, JagerA3Info.Effects.BULLET_TRAIL::play));
        }

        @Override
        @NonNull
        protected HitBlockHandler getPreHitBlockHandler() {
            return (location, hitBlock) -> {
                if (getVelocity().length() > 0.01)
                    CombatEffectUtil.THROW_BOUNCE_SOUND.apply(getVelocity().length()).play(location);

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
            super(combatUser, JagerA3Info.RADIUS, EntityCondition.enemy(combatUser).include(combatUser));
            this.projectile = projectile;
        }

        @Override
        protected boolean onHitBlock(@NonNull Location center, @NonNull Location location, @NonNull Block hitBlock) {
            return false;
        }

        @Override
        protected boolean onHitEntity(@NonNull Location center, @NonNull Location location, @NonNull Damageable target) {
            double distance = center.distance(location);
            double damage = JagerA3Info.DISTANT_DAMAGE_EXPLODE.getDamage(distance);
            boolean isDamaged = projectile == null
                    ? target.getDamageModule().damage(combatUser, damage, DamageType.NORMAL, null, false, true)
                    : target.getDamageModule().damage(projectile, damage, DamageType.NORMAL, null, false, true);

            if (isDamaged) {
                if (target instanceof Movable) {
                    Vector dir = LocationUtil.getDirection(center, location.add(0, 0.5, 0)).multiply(JagerA3Info.KNOCKBACK);
                    ((Movable) target).getKnockbackModule().knockback(dir);
                }

                if (JagerT1.addValue(target, (int) JagerA3Info.DISTANT_FREEZE.getDamage(distance)).getValue() >= JagerT1Info.MAX) {
                    target.getStatusEffectModule().apply(Freeze.instance, JagerA3Info.SNARE_DURATION);

                    combatUser.getAbilityManager().getAbility(JagerP1Info.getInstance()).use(target);

                    if (target != combatUser && target.isGoalTarget())
                        combatUser.addScore(JagerA3Info.SNARE_SCORE);
                }
            }

            return !(target instanceof Barrier);
        }
    }
}
