package com.dace.dmgr.combat.combatant.jager;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.Timestamp;
import com.dace.dmgr.combat.CombatEffectUtil;
import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.action.ActionBarStringUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.ActiveSkill;
import com.dace.dmgr.combat.action.weapon.Weapon;
import com.dace.dmgr.combat.entity.DamageType;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.EntityCondition;
import com.dace.dmgr.combat.entity.Movable;
import com.dace.dmgr.combat.entity.combatuser.ActionManager;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.combat.entity.module.statuseffect.Snare;
import com.dace.dmgr.combat.entity.module.statuseffect.ValueStatusEffect;
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

public final class JagerA3 extends ActiveSkill {
    /** 폭발 타임스탬프 */
    private Timestamp explodeTimestamp = Timestamp.now();
    /** 활성화 완료 여부 */
    private boolean isEnabled = false;

    public JagerA3(@NonNull CombatUser combatUser) {
        super(combatUser, JagerA3Info.getInstance(), JagerA3Info.COOLDOWN, Timespan.MAX, 2);
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
    @Nullable
    public String getActionBarString() {
        if (isDurationFinished() || !isEnabled)
            return null;

        return JagerA3Info.getInstance() + ActionBarStringUtil.getKeyInfo(this, "투척");
    }

    @Override
    public boolean canUse(@NonNull ActionKey actionKey) {
        return super.canUse(actionKey) && !combatUser.getActionManager().getSkill(JagerA1Info.getInstance()).getConfirmModule().isChecking();
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        if (!isDurationFinished()) {
            onThrow();
            return;
        }
        if (actionKey != ActionKey.SLOT_3)
            return;

        setDuration();
        combatUser.setGlobalCooldown(JagerA3Info.READY_DURATION);

        Weapon weapon = combatUser.getActionManager().getWeapon();
        weapon.cancel();
        weapon.setVisible(false);

        JagerA3Info.Sounds.USE.play(combatUser.getLocation());

        addActionTask(new DelayTask(() -> {
            isEnabled = true;
            explodeTimestamp = Timestamp.now().plus(JagerA3Info.EXPLODE_DURATION);

            JagerA3Info.Sounds.USE_READY.play(combatUser.getLocation());

            addActionTask(new IntervalTask(i -> {
                Location loc = LocationUtil.getLocationFromOffset(combatUser.getArmLocation(MainHand.RIGHT), 0, 0, 0.3);
                JagerA3Info.Particles.BULLET_TRAIL.play(loc);
            }, () -> {
                forceCancel();

                Location loc = LocationUtil.getLocationFromOffset(combatUser.getArmLocation(MainHand.RIGHT), 0, 0, 0.3);
                onExplode(loc, null);
            }, 1, JagerA3Info.EXPLODE_DURATION.toTicks()));
        }, JagerA3Info.READY_DURATION.toTicks()));
    }

    @Override
    public boolean isCancellable() {
        return (!isEnabled || combatUser.isDead()) && !isDurationFinished();
    }

    @Override
    protected void onCancelled() {
        isEnabled = false;

        setDuration(Timespan.ZERO);
        combatUser.getActionManager().getWeapon().setVisible(true);
    }

    /**
     * 수류탄 투척 시 실행할 작업.
     */
    private void onThrow() {
        forceCancel();

        combatUser.getActionManager().getWeapon().setCooldown(Timespan.ofTicks(2));

        Location loc = combatUser.getArmLocation(MainHand.RIGHT);
        new JagerA3Projectile().shot(loc);

        CombatEffectUtil.THROW_SOUND.play(loc);
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

        JagerA3Info.Sounds.EXPLODE.play(loc);
        JagerA3Info.Particles.EXPLODE.play(loc);
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
                JagerA3Info.Particles.FREEZE_TICK.play(combatEntity.getCenterLocation(), combatEntity.getWidth(), combatEntity.getHeight());
        }
    }

    private final class JagerA3Projectile extends BouncingProjectile<Damageable> {
        private JagerA3Projectile() {
            super(JagerA3.this, JagerA3Info.VELOCITY, EntityCondition.enemy(combatUser),
                    Projectile.Option.builder().duration(Timestamp.now().until(explodeTimestamp)).build(),
                    Option.builder().bounceVelocityMultiplier(0.35).build());
        }

        @Override
        protected void onDestroy(@NonNull Location location) {
            onExplode(location, this);
        }

        @Override
        @NonNull
        protected IntervalHandler getIntervalHandler() {
            return IntervalHandler
                    .chain(createGravityIntervalHandler())
                    .next(createPeriodIntervalHandler(8, JagerA3Info.Particles.BULLET_TRAIL::play));
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
            double damage = CombatUtil.getDistantDamage(JagerA3Info.DAMAGE_EXPLODE, distance, radius / 2.0);
            int freeze = (int) CombatUtil.getDistantDamage(JagerA3Info.FREEZE, distance, radius / 2.0);
            boolean isDamaged = projectile == null
                    ? target.getDamageModule().damage(combatUser, damage, DamageType.NORMAL, null, false, true)
                    : target.getDamageModule().damage(projectile, damage, DamageType.NORMAL, null, false, true);

            if (isDamaged) {
                if (target instanceof Movable) {
                    Vector dir = LocationUtil.getDirection(center, location.add(0, 0.5, 0)).multiply(JagerA3Info.KNOCKBACK);
                    ((Movable) target).getMoveModule().knockback(dir);
                }

                JagerT1.addFreezeValue(target, freeze);

                if (target.getStatusEffectModule().getValueStatusEffect(ValueStatusEffect.Type.FREEZE).getValue() >= JagerT1Info.MAX) {
                    target.getStatusEffectModule().apply(Freeze.instance, JagerA3Info.SNARE_DURATION);

                    ActionManager actionManager = combatUser.getActionManager();
                    actionManager.getSkill(JagerP1Info.getInstance()).setTarget(target);
                    actionManager.useAction(ActionKey.PERIODIC_1);

                    if (target != combatUser && target.isGoalTarget())
                        combatUser.addScore("적 얼림", JagerA3Info.SNARE_SCORE);
                }
            }

            return !(target instanceof Barrier);
        }
    }
}
