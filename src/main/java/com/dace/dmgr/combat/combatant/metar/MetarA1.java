package com.dace.dmgr.combat.combatant.metar;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.CombatEffectUtil;
import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.ActiveSkill;
import com.dace.dmgr.combat.entity.DamageType;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.EntityCondition;
import com.dace.dmgr.combat.entity.Movable;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.combat.entity.temporary.Barrier;
import com.dace.dmgr.combat.interaction.Area;
import com.dace.dmgr.combat.interaction.Bullet;
import com.dace.dmgr.combat.interaction.Projectile;
import com.dace.dmgr.util.LocationUtil;
import com.dace.dmgr.util.task.DelayTask;
import com.dace.dmgr.util.task.IntervalTask;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

public final class MetarA1 extends ActiveSkill {
    /** 발사 방향의 반대 방향 여부 */
    private boolean isOpposite = true;

    public MetarA1(@NonNull CombatUser combatUser) {
        super(combatUser, MetarA1Info.getInstance(), MetarA1Info.COOLDOWN, Timespan.MAX, 0);
    }

    @Override
    @NonNull
    public ActionKey @NonNull [] getDefaultActionKeys() {
        return new ActionKey[]{ActionKey.SLOT_1, ActionKey.LEFT_CLICK};
    }

    @Override
    public boolean canUse(@NonNull ActionKey actionKey) {
        return super.canUse(actionKey) && isDurationFinished();
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        setDuration();

        combatUser.getActionManager().getWeapon().cancel();
        combatUser.setGlobalCooldown(MetarA1Info.READY_DURATION);

        MetarA1Info.Sounds.USE.play(combatUser.getLocation());

        addActionTask(new IntervalTask(i -> {
            for (int j = 0; j < 2; j++) {
                for (int k = 0; k < Math.min(6, i); k++) {
                    Location loc = LocationUtil.getLocationFromOffset(combatUser.getEntity().getEyeLocation().add(0, -0.3 + k * 0.15, 0),
                            -0.5 + j, 0, -0.5);
                    MetarA1Info.Particles.USE_TICK_CORE.play(loc);
                }

                if (i > 5) {
                    Location loc = LocationUtil.getLocationFromOffset(combatUser.getEntity().getEyeLocation().add(0, 0.6, 0),
                            -0.5 + j, 0, -0.25);
                    MetarA1Info.Particles.USE_TICK_SHAPE.play(loc);
                }
            }
        }, 1));

        double[] offsetYs = {-0.2, -0.2, 0.1, 0.1, 0.4, 0.4};

        addActionTask(new DelayTask(() -> addActionTask(new IntervalTask(i -> {
            isOpposite = !isOpposite;

            Location loc = LocationUtil.getLocationFromOffset(combatUser.getEntity().getEyeLocation().add(0, offsetYs[(int) i], 0),
                    isOpposite ? 0.4 : -0.4, 0, 0);
            new MetarA1Projectile().shot(loc);

            MetarA1Info.Sounds.SHOOT.play(loc);
        }, this::cancel, 3, 6)), MetarA1Info.READY_DURATION.toTicks()));
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

            MetarA1Info.Sounds.EXPLODE.play(loc);
            MetarA1Info.Particles.EXPLODE.play(loc);
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

                MetarA1Info.Particles.BULLET_TRAIL.play(location, getVelocity().normalize());
            });
        }

        @Override
        @NonNull
        protected HitBlockHandler getHitBlockHandler() {
            return (location, hitBlock) -> {
                CombatEffectUtil.playHitBlockParticle(location, hitBlock, 4);
                return false;
            };
        }

        @Override
        @NonNull
        protected HitEntityHandler<Damageable> getHitEntityHandler() {
            return (location, target) -> {
                if (target.getDamageModule().damage(this, MetarA1Info.DAMAGE_DIRECT, DamageType.NORMAL, location, false, true)) {
                    if (target instanceof Movable) {
                        Vector dir = getVelocity().normalize().multiply(MetarA1Info.KNOCKBACK);
                        ((Movable) target).getMoveModule().knockback(dir);
                    }

                    if (target.isGoalTarget())
                        combatUser.addScore("미사일 직격", MetarA1Info.DIRECT_HIT_SCORE);
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
                double damage = CombatUtil.getDistantDamage(MetarA1Info.DAMAGE_EXPLODE, center.distance(location), radius / 2.0);

                if (target.getDamageModule().damage(MetarA1Projectile.this, damage, DamageType.NORMAL, null, false, true)
                        && target instanceof Movable && isNotHit(target)) {
                    Vector dir = LocationUtil.getDirection(center, location.add(0, 0.5, 0)).multiply(MetarA1Info.KNOCKBACK);
                    ((Movable) target).getMoveModule().knockback(dir);
                }

                return !(target instanceof Barrier);
            }
        }
    }
}
