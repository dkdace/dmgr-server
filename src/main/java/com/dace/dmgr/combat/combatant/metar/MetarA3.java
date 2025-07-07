package com.dace.dmgr.combat.combatant.metar;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.ability.ActionBarDisplay;
import com.dace.dmgr.combat.ability.ActionKey;
import com.dace.dmgr.combat.ability.skill.ActiveSkill;
import com.dace.dmgr.combat.ability.skill.HasBonusScore;
import com.dace.dmgr.combat.ability.skill.module.BonusScoreModule;
import com.dace.dmgr.combat.entity.DamageType;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.EntityCondition;
import com.dace.dmgr.combat.entity.Movable;
import com.dace.dmgr.combat.entity.combatuser.CombatScore;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.combat.interaction.Area;
import com.dace.dmgr.combat.interaction.Projectile;
import com.dace.dmgr.util.location.LocationUtil;
import com.dace.dmgr.util.task.DelayTask;
import com.dace.dmgr.util.task.IntervalTask;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.function.Consumer;

public final class MetarA3 extends ActiveSkill implements HasBonusScore {
    /** 보너스 점수 모듈 */
    @NonNull
    @Getter
    private final BonusScoreModule bonusScoreModule;

    /** 활성화 완료 여부 */
    private boolean isEnabled = false;
    /** 현재 투사체 */
    @Nullable
    private MetarA3Projectile projectile;

    public MetarA3(@NonNull CombatUser combatUser, @NonNull MetarA3Info skillInfo) {
        super(combatUser, skillInfo, MetarA3Info.COOLDOWN, Timespan.MAX);
        this.bonusScoreModule = new BonusScoreModule(this);
    }

    @Override
    @Nullable
    public ActionBarDisplay getActionBarDisplay() {
        if (isDurationFinished() || !isEnabled)
            return null;

        return ActionBarDisplay.builder(this).title().keyInfo("격발").build();
    }

    @Override
    @NonNull
    public ActionKey.Slot getSlot() {
        return ActionKey.Slot.SLOT_3;
    }

    @Override
    public void onSlot() {
        if (!isDurationFinished()) {
            forceCancel();
            MetarA3Info.Effects.DETONATE.play(combatUser.getLocation());

            return;
        }

        setDuration();

        combatUser.getAbilityManager().getWeapon().cancel();
        combatUser.setGlobalCooldown(MetarA3Info.READY_DURATION);

        MetarA3Info.Effects.USE.play(combatUser.getLocation());

        addActionTask(new DelayTask(() -> {
            isEnabled = true;

            MetarA3Info.Effects.USE_READY.play(combatUser.getLocation());

            Location loc = combatUser.getEntity().getEyeLocation().subtract(0, 0.4, 0);
            projectile = new MetarA3Projectile();
            projectile.shot(loc);
        }, MetarA3Info.READY_DURATION.toTicks()));
    }

    @Override
    public boolean isCancellable() {
        return (!isEnabled || combatUser.isDead()) && !isDurationFinished();
    }

    @Override
    protected void onCancelled() {
        isEnabled = false;
        setDuration(Timespan.ZERO);

        if (projectile != null && !projectile.isDestroyed())
            projectile.destroy();
    }

    @Override
    @NonNull
    public CombatScore getCombatScore() {
        return MetarA3Info.ASSIST_SCORE;
    }

    @Override
    public boolean isAssistMode() {
        return true;
    }

    private final class MetarA3Projectile extends Projectile<Damageable> {
        private final HashSet<Damageable> targets = new HashSet<>();

        private MetarA3Projectile() {
            super(MetarA3.this, MetarA3Info.VELOCITY, EntityCondition.enemy(combatUser),
                    Option.builder().size(MetarA3Info.SIZE).duration(MetarA3Info.EXPLODE_DURATION).build());
        }

        @Override
        protected void onDestroy(@NonNull Location location, boolean isForce) {
            projectile = null;
            if (isEnabled)
                forceCancel();

            Location[] locs = new Location[30];

            addTask(new IntervalTask(i -> {
                Location loc = location.clone().add(0, 0.1, 0);
                new MetarA3Area().emit(loc);

                MetarA3Info.Effects.playDetonateTick(i, loc, locs);
            }, 1, MetarA3Info.DURATION.toTicks()));
        }

        @Override
        @NonNull
        protected IntervalHandler getIntervalHandler() {
            return createPeriodIntervalHandler(13, new Consumer<Location>() {
                private long i = 0;

                @Override
                public void accept(Location location) {
                    MetarA3Info.Effects.playBulletTrail(i++, location);
                }
            });
        }

        @Override
        @NonNull
        protected HitBlockHandler getHitBlockHandler() {
            return (location, hitBlock) -> {
                setVelocity(new Vector(0, 0, 0));
                return true;
            };
        }

        @Override
        @NonNull
        protected HitEntityHandler<Damageable> getHitEntityHandler() {
            return (location, target) -> false;
        }

        private final class MetarA3Area extends Area<Damageable> {
            private MetarA3Area() {
                super(combatUser, MetarA3Info.RADIUS, MetarA3Projectile.this.entityCondition);
            }

            @Override
            protected boolean onHitBlock(@NonNull Location center, @NonNull Location location, @NonNull Block hitBlock) {
                return true;
            }

            @Override
            protected boolean onHitEntity(@NonNull Location center, @NonNull Location location, @NonNull Damageable target) {
                if (target.getDamageModule().damage(MetarA3Projectile.this, 0, DamageType.NORMAL, null, false, true)) {
                    if (target instanceof Movable) {
                        Vector dir = LocationUtil.getDirection(target.getLocation(), center).multiply(MetarA3Info.KNOCKBACK);
                        ((Movable) target).getKnockbackModule().knockback(dir, true);
                    }

                    if (targets.add(target)) {
                        target.getDamageModule().damage(MetarA3Projectile.this, 1, DamageType.NORMAL, null, false, true);

                        if (target != combatUser && target.isGoalTarget()) {
                            combatUser.addScore(MetarA3Info.EFFECT_SCORE);
                            bonusScoreModule.addTarget(target, MetarA3Info.ASSIST_SCORE_TIME_LIMIT);
                        }
                    }
                }

                return true;
            }
        }
    }
}
