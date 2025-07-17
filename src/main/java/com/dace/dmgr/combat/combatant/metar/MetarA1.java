package com.dace.dmgr.combat.combatant.metar;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.ability.ActionKey;
import com.dace.dmgr.combat.ability.handler.LeftClickHandler;
import com.dace.dmgr.combat.ability.skill.ActiveSkill;
import com.dace.dmgr.combat.entity.DamageType;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.EntityCondition;
import com.dace.dmgr.combat.entity.Movable;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.combat.entity.temporary.Barrier;
import com.dace.dmgr.combat.interaction.Area;
import com.dace.dmgr.combat.interaction.Bullet;
import com.dace.dmgr.combat.interaction.Projectile;
import com.dace.dmgr.util.location.LocationUtil;
import com.dace.dmgr.util.task.DelayTask;
import com.dace.dmgr.util.task.IntervalTask;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.function.LongConsumer;

public final class MetarA1 extends ActiveSkill implements LeftClickHandler {
    /** 발사 방향의 반대 방향 여부 */
    private boolean isOpposite = true;

    public MetarA1(@NonNull CombatUser combatUser, @NonNull MetarA1Info skillInfo) {
        super(combatUser, skillInfo, MetarA1Info.COOLDOWN, Timespan.MAX);
    }

    @Override
    protected boolean canUse() {
        return super.canUse() && isDurationFinished();
    }

    @Override
    @NonNull
    public ActionKey.Slot getSlot() {
        return ActionKey.Slot.SLOT_1;
    }

    @Override
    public void onSlot() {
        setDuration();

        combatUser.getAbilityManager().getWeapon().cancel();
        combatUser.setGlobalCooldown(MetarA1Info.READY_DURATION);

        MetarA1Info.Effects.USE.play(combatUser.getLocation());

        addActionTask(new IntervalTask((LongConsumer) i -> MetarA1Info.Effects.playUseTick(i, combatUser.getEntity().getEyeLocation()), 1));

        double[] offsetYs = {-0.2, -0.2, 0.1, 0.1, 0.4, 0.4};

        addActionTask(new DelayTask(() -> addActionTask(new IntervalTask(i -> {
            isOpposite = !isOpposite;

            Location loc = LocationUtil.getLocationFromOffset(combatUser.getEntity().getEyeLocation().add(0, offsetYs[(int) i], 0),
                    isOpposite ? 0.4 : -0.4, 0, 0);
            new MetarA1Projectile().shot(loc);

            MetarA1Info.Effects.SHOOT.play(loc);
        }, this::cancel, 3, 6)), MetarA1Info.READY_DURATION.toTicks()));
    }

    @Override
    public void onLeftClick() {
        onSlot();
    }

    @Override
    public boolean isCancellable() {
        return !isDurationFinished();
    }

    @Override
    protected void onCancelled() {
        setDuration(Timespan.ZERO);
    }

    private final class MetarA1Projectile extends Projectile<Damageable> {
        @Nullable
        private Damageable target;

        private MetarA1Projectile() {
            super(MetarA1.this, MetarA1Info.VELOCITY, EntityCondition.enemy(combatUser));
        }

        @Override
        protected void onHit(@NonNull Location location) {
            Location loc = location.add(0, 0.1, 0);
            new MetarA1ExplodeArea().emit(loc);

            MetarA1Info.Effects.EXPLODE.play(loc);
        }

        @Override
        @NonNull
        protected IntervalHandler getIntervalHandler() {
            return createPeriodIntervalHandler(10, location -> {
                if (target == null)
                    new MetarA1Area().emit(location);
                else if (target.canBeTargeted() && !target.isRemoved() && location.distance(target.getCenterLocation()) <= MetarA1Info.ENEMY_DETECT_RADIUS)
                    setVelocity(LocationUtil.getDirection(location, target.getHitboxCenter()).multiply(Bullet.HITBOX_INTERVAL * 0.5));
                else
                    target = null;

                MetarA1Info.Effects.BULLET_TRAIL.apply(getVelocity()).play(location);
            });
        }

        @Override
        @NonNull
        protected HitBlockHandler getHitBlockHandler() {
            return (location, hitBlock) -> {
                MetarA1Info.Effects.HIT_BLOCK.apply(hitBlock).play(location);
                return false;
            };
        }

        @Override
        @NonNull
        protected HitEntityHandler<Damageable> getHitEntityHandler() {
            return (location, target) -> {
                if (target.getDamageModule().damage(this, MetarA1Info.DAMAGE_DIRECT, DamageType.NORMAL, location, false, true)) {
                    if (target instanceof Movable)
                        ((Movable) target).getKnockbackModule().knockback(getVelocity(), MetarA1Info.KNOCKBACK);

                    if (target.isGoalTarget())
                        combatUser.addScore(MetarA1Info.DIRECT_HIT_SCORE);
                }

                return false;
            };
        }

        private final class MetarA1Area extends Area<Damageable> {
            private MetarA1Area() {
                super(combatUser, MetarA1Info.ENEMY_DETECT_RADIUS, MetarA1Projectile.this.entityCondition.and(Damageable::isCreature));
            }

            @Override
            protected boolean onHitBlock(@NonNull Location center, @NonNull Location location, @NonNull Block hitBlock) {
                return false;
            }

            @Override
            protected boolean onHitEntity(@NonNull Location center, @NonNull Location location, @NonNull Damageable target) {
                if (MetarA1Projectile.this.target == null)
                    MetarA1Projectile.this.target = target;

                return true;
            }
        }

        private final class MetarA1ExplodeArea extends Area<Damageable> {
            private MetarA1ExplodeArea() {
                super(combatUser, MetarA1Info.RADIUS, MetarA1Projectile.this.entityCondition.include(combatUser));
            }

            @Override
            protected boolean onHitBlock(@NonNull Location center, @NonNull Location location, @NonNull Block hitBlock) {
                return false;
            }

            @Override
            protected boolean onHitEntity(@NonNull Location center, @NonNull Location location, @NonNull Damageable target) {
                double damage = MetarA1Info.DISTANT_DAMAGE_EXPLODE.getDamage(center.distance(location));

                if (target.getDamageModule().damage(MetarA1Projectile.this, damage, DamageType.NORMAL, null, false, true)
                        && target != combatUser && !MetarA1Projectile.this.getHitTargets().contains(target) && target instanceof Movable)
                    ((Movable) target).getKnockbackModule().knockback(LocationUtil.getDirection(center, location.add(0, 0.5, 0)),
                            MetarA1Info.KNOCKBACK);

                return !(target instanceof Barrier);
            }
        }
    }
}
