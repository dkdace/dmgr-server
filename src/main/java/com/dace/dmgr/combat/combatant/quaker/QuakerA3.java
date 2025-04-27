package com.dace.dmgr.combat.combatant.quaker;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.CombatEffectUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.ActiveSkill;
import com.dace.dmgr.combat.action.weapon.Weapon;
import com.dace.dmgr.combat.entity.*;
import com.dace.dmgr.combat.entity.module.AbilityStatus;
import com.dace.dmgr.combat.entity.module.statuseffect.Snare;
import com.dace.dmgr.combat.entity.temporary.Barrier;
import com.dace.dmgr.combat.interaction.Area;
import com.dace.dmgr.combat.interaction.Hitscan;
import com.dace.dmgr.combat.interaction.Projectile;
import com.dace.dmgr.util.LocationUtil;
import com.dace.dmgr.util.VectorUtil;
import com.dace.dmgr.util.task.IntervalTask;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.inventory.MainHand;
import org.bukkit.util.Vector;

import java.util.HashSet;

public final class QuakerA3 extends ActiveSkill {
    /** 수정자 */
    private static final AbilityStatus.Modifier MODIFIER = new AbilityStatus.Modifier(-100);

    public QuakerA3(@NonNull CombatUser combatUser) {
        super(combatUser, QuakerA3Info.getInstance(), QuakerA3Info.COOLDOWN, Timespan.MAX, 2);
    }

    @Override
    @NonNull
    public ActionKey @NonNull [] getDefaultActionKeys() {
        return new ActionKey[]{ActionKey.SLOT_3};
    }

    @Override
    public boolean canUse(@NonNull ActionKey actionKey) {
        return super.canUse(actionKey) && isDurationFinished() && combatUser.getSkill(QuakerA1Info.getInstance()).isDurationFinished();
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        setDuration();

        combatUser.setGlobalCooldown(QuakerA3Info.GLOBAL_COOLDOWN);
        combatUser.getMoveModule().getSpeedStatus().addModifier(MODIFIER);
        combatUser.playMeleeAttackAnimation(-7, Timespan.ofTicks(12), MainHand.RIGHT);

        Weapon weapon = combatUser.getWeapon();
        weapon.cancel();
        weapon.setVisible(false);

        QuakerA3Info.Sounds.USE.play(combatUser.getLocation());

        addActionTask(new IntervalTask(i -> {
            Location loc = LocationUtil.getLocationFromOffset(combatUser.getEntity().getEyeLocation(), 0, 0, 1);
            Vector vector = VectorUtil.getYawAxis(loc).multiply(-1);
            Vector axis = VectorUtil.getPitchAxis(loc);

            for (int j = 0; j < i; j++) {
                Vector vec = VectorUtil.getRotatedVector(vector, axis, 90 + 30 * (j - 2.5));
                new QuakerA3Effect().shot(loc.clone().add(vec), vec);
            }
        }, () -> {
            cancel();

            new QuakerA3Projectile().shot();

            QuakerA3Info.Sounds.USE_READY.play(combatUser.getLocation());
        }, 1, QuakerA3Info.READY_DURATION.toTicks()));
    }

    @Override
    public boolean isCancellable() {
        return !isDurationFinished();
    }

    @Override
    protected void onCancelled() {
        setDuration(Timespan.ZERO);

        combatUser.getMoveModule().getSpeedStatus().removeModifier(MODIFIER);
        combatUser.getWeapon().setVisible(true);
    }

    private final class QuakerA3Effect extends Hitscan<CombatEntity> {
        private QuakerA3Effect() {
            super(combatUser, EntityCondition.all(), Option.builder().maxDistance(0.6).build());
        }

        @Override
        protected void onDestroy(@NonNull Location location) {
            for (int i = 0; i < 3; i++) {
                Location loc = LocationUtil.getLocationFromOffset(location, -0.25 + i * 0.25, 0, 0);
                QuakerA3Info.Particles.BULLET_TRAIL_EFFECT_DECO.play(loc);
            }
        }

        @Override
        @NonNull
        protected IntervalHandler getIntervalHandler() {
            return (location, i) -> {
                if (i == 0)
                    for (int j = 0; j < 3; j++) {
                        Location loc = LocationUtil.getLocationFromOffset(location, -0.25 + j * 0.25, 0, 0);
                        QuakerA3Info.Particles.BULLET_TRAIL_EFFECT_CORE.play(loc);
                    }

                return true;
            };
        }

        @Override
        @NonNull
        protected HitBlockHandler getHitBlockHandler() {
            return (location, hitBlock) -> false;
        }

        @Override
        @NonNull
        protected HitEntityHandler<CombatEntity> getHitEntityHandler() {
            return (location, target) -> true;
        }
    }

    private final class QuakerA3Projectile extends Projectile<Damageable> {
        private final HashSet<Damageable> targets = new HashSet<>();

        private QuakerA3Projectile() {
            super(QuakerA3.this, QuakerA3Info.VELOCITY, EntityCondition.enemy(combatUser),
                    Option.builder().size(QuakerA3Info.SIZE).maxDistance(QuakerA3Info.DISTANCE).build());
        }

        @Override
        protected void onHit(@NonNull Location location) {
            QuakerA3Info.Particles.HIT.play(location);
        }

        @Override
        @NonNull
        protected IntervalHandler getIntervalHandler() {
            return createPeriodIntervalHandler(16, location -> {
                Vector vector = VectorUtil.getYawAxis(location).multiply(-1);
                Vector axis = VectorUtil.getPitchAxis(location);

                for (int i = 0; i < 8; i++) {
                    Vector vec = VectorUtil.getRotatedVector(vector, axis, 90 + 30 * (i - 3.5)).multiply(0.6);
                    Location loc = location.clone().add(vec);
                    new QuakerA3Effect().shot(loc, vec);

                    Vector vec2 = VectorUtil.getSpreadedVector(getVelocity().normalize(), 30);
                    QuakerA3Info.Particles.BULLET_TRAIL.play(location, vec2);
                }

                QuakerA3Info.Sounds.TICK.play(location);
            });
        }

        @Override
        @NonNull
        protected HitBlockHandler getHitBlockHandler() {
            return (location, hitBlock) -> {
                QuakerA3Info.Sounds.HIT.play(location);
                CombatEffectUtil.playHitBlockParticle(location, hitBlock, 5);

                return false;
            };
        }

        @Override
        @NonNull
        protected HitEntityHandler<Damageable> getHitEntityHandler() {
            return (location, target) -> {
                if (targets.add(target)) {
                    onHitEnemy(location, target);

                    if (target instanceof Movable)
                        knockback((Movable) target);
                }

                return false;
            };
        }

        /**
         * 적이 맞았을 때 실행할 작업.
         *
         * @param location 맞은 위치
         * @param target   대상 엔티티
         */
        private void onHitEnemy(Location location, @NonNull Damageable target) {
            if (target.getDamageModule().damage(QuakerA3Projectile.this, QuakerA3Info.DAMAGE, DamageType.NORMAL, location,
                    false, true) && target instanceof Movable) {
                target.getStatusEffectModule().apply(Snare.getInstance(), QuakerA3Info.SNARE_DURATION);

                if (target.isGoalTarget())
                    combatUser.addScore("돌풍 강타", QuakerA3Info.DAMAGE_SCORE);
            }

            QuakerA3Info.Particles.HIT_ENTITY_DECO.play(location);
            QuakerA3Info.Sounds.HIT.play(location);
        }

        /**
         * 맞은 적을 밀쳐낸다.
         *
         * @param target 대상 엔티티
         */
        private void knockback(@NonNull Movable target) {
            Vector dir = getVelocity().normalize().multiply(QuakerA3Info.KNOCKBACK);

            target.addTask(new IntervalTask(i -> {
                if (!target.canBeTargeted())
                    return false;

                if (i < 3)
                    target.getMoveModule().knockback(dir, true);

                Location loc = target.getCenterLocation().add(0, 0.1, 0);
                new QuakerA3Area().emit(loc);

                for (int j = 0; j < 5; j++) {
                    Vector vec = VectorUtil.getSpreadedVector(dir.clone().normalize(), 20);
                    QuakerA3Info.Particles.HIT_ENTITY_CORE.play(target.getCenterLocation(), vec);
                }

                Location hitLoc = loc.clone().add(getVelocity().normalize());
                if (!LocationUtil.isNonSolid(hitLoc)) {
                    onHitEnemy(hitLoc, (Damageable) target);
                    target.getMoveModule().knockback(new Vector(), true);

                    CombatEffectUtil.playHitBlockParticle(loc, hitLoc.getBlock(), 7);

                    return false;
                }

                return true;
            }, 1, 8));
        }

        private final class QuakerA3Area extends Area<Damageable> {
            private QuakerA3Area() {
                super(combatUser, QuakerA3Info.RADIUS, QuakerA3Projectile.this.entityCondition);
            }

            @Override
            protected boolean onHitBlock(@NonNull Location center, @NonNull Location location, @NonNull Block hitBlock) {
                return false;
            }

            @Override
            protected boolean onHitEntity(@NonNull Location center, @NonNull Location location, @NonNull Damageable target) {
                getHitEntityHandler().onHitEntity(location, target);
                return !(target instanceof Barrier);
            }
        }
    }
}
