package com.dace.dmgr.combat.character.quaker.action;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.CombatEffectUtil;
import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.ActiveSkill;
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
import com.dace.dmgr.util.task.TaskUtil;
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
        super(combatUser, QuakerA3Info.getInstance(), 2);
    }

    @Override
    @NonNull
    public ActionKey @NonNull [] getDefaultActionKeys() {
        return new ActionKey[]{ActionKey.SLOT_3};
    }

    @Override
    public long getDefaultCooldown() {
        return QuakerA3Info.COOLDOWN;
    }

    @Override
    public long getDefaultDuration() {
        return -1;
    }

    @Override
    public boolean canUse(@NonNull ActionKey actionKey) {
        return super.canUse(actionKey) && isDurationFinished() && combatUser.getSkill(QuakerA1Info.getInstance()).isDurationFinished();
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        setDuration();
        combatUser.getWeapon().onCancelled();
        combatUser.getWeapon().setVisible(false);
        combatUser.setGlobalCooldown(Timespan.ofTicks(QuakerA3Info.GLOBAL_COOLDOWN));
        combatUser.getMoveModule().getSpeedStatus().addModifier(MODIFIER);
        combatUser.playMeleeAttackAnimation(-7, Timespan.ofTicks(12), MainHand.RIGHT);

        QuakerA3Info.SOUND.USE.play(combatUser.getLocation());

        TaskUtil.addTask(taskRunner, new IntervalTask(i -> {
            Location loc = LocationUtil.getLocationFromOffset(combatUser.getEntity().getEyeLocation(), 0, 0, 1);
            Vector vector = VectorUtil.getYawAxis(loc).multiply(-1);
            Vector axis = VectorUtil.getPitchAxis(loc);

            for (int j = 0; j < i; j++) {
                Vector vec = VectorUtil.getRotatedVector(vector, axis, 90 + 30 * (j - 2.5));
                new QuakerA3Effect().shot(loc.clone().add(vec), vec);
            }
        }, () -> {
            onCancelled();

            new QuakerA3Projectile().shot();

            QuakerA3Info.SOUND.USE_READY.play(combatUser.getLocation());
        }, 1, QuakerA3Info.READY_DURATION));
    }

    @Override
    public boolean isCancellable() {
        return !isDurationFinished();
    }

    @Override
    public void onCancelled() {
        super.onCancelled();

        setDuration(0);
        combatUser.getMoveModule().getSpeedStatus().removeModifier(MODIFIER);
        combatUser.getWeapon().setVisible(true);
    }

    private final class QuakerA3Effect extends Hitscan<CombatEntity> {
        private QuakerA3Effect() {
            super(combatUser, CombatUtil.EntityCondition.all(), Option.builder().maxDistance(0.6).build());
        }

        @Override
        protected void onDestroy(@NonNull Location location) {
            for (int i = 0; i < 3; i++) {
                Location loc = LocationUtil.getLocationFromOffset(location, -0.25 + i * 0.25, 0, 0);
                QuakerA3Info.PARTICLE.BULLET_TRAIL_EFFECT_DECO.play(loc);
            }
        }

        @Override
        @NonNull
        protected IntervalHandler getIntervalHandler() {
            return (location, i) -> {
                if (i == 0)
                    for (int j = 0; j < 3; j++) {
                        Location loc = LocationUtil.getLocationFromOffset(location, -0.25 + j * 0.25, 0, 0);
                        QuakerA3Info.PARTICLE.BULLET_TRAIL_EFFECT_CORE.play(loc);
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
            super(combatUser, QuakerA3Info.VELOCITY, CombatUtil.EntityCondition.enemy(combatUser),
                    Option.builder().size(QuakerA3Info.SIZE).maxDistance(QuakerA3Info.DISTANCE).build());
        }

        @Override
        protected void onHit(@NonNull Location location) {
            QuakerA3Info.PARTICLE.HIT.play(location);
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

                    Vector vec2 = VectorUtil.getSpreadedVector(getVelocity().clone().normalize(), 30);
                    QuakerA3Info.PARTICLE.BULLET_TRAIL.play(location, vec2);
                }

                QuakerA3Info.SOUND.TICK.play(location);
            });
        }

        @Override
        @NonNull
        protected HitBlockHandler getHitBlockHandler() {
            return (location, hitBlock) -> {
                QuakerA3Info.SOUND.HIT.play(location);
                CombatEffectUtil.playHitBlockParticle(location, hitBlock, 5);

                return false;
            };
        }

        @Override
        @NonNull
        protected HitEntityHandler<Damageable> getHitEntityHandler() {
            return (location, target) -> {
                onImpact(location.add(0, 0.1, 0), target);
                return false;
            };
        }

        private void onImpact(@NonNull Location location, @NonNull Damageable target) {
            if (!targets.add(target) || !onDamage(location, target))
                return;

            Vector vec = getVelocity().clone().normalize().multiply(QuakerA3Info.KNOCKBACK);
            TaskUtil.addTask(QuakerA3.this, new IntervalTask(i -> {
                if (!target.canBeTargeted() || target.isDisposed())
                    return false;

                if (i < 3 && target instanceof Movable)
                    ((Movable) target).getMoveModule().knockback(vec, true);

                Location loc = target.getCenterLocation().add(0, 0.1, 0);
                new QuakerA3Area().emit(loc);

                for (int j = 0; j < 5; j++) {
                    Vector vec2 = VectorUtil.getSpreadedVector(vec.clone().normalize(), 20);
                    QuakerA3Info.PARTICLE.HIT_ENTITY_CORE.play(target.getCenterLocation(), vec2);
                }

                Location hitLoc = loc.clone().add(getVelocity().clone().normalize());
                if (!LocationUtil.isNonSolid(hitLoc)) {
                    onDamage(loc, target);
                    if (target instanceof Movable)
                        ((Movable) target).getMoveModule().knockback(new Vector(), true);

                    CombatEffectUtil.playHitBlockParticle(loc, hitLoc.getBlock(), 7);

                    return false;
                }

                return true;
            }, 1, 8));
        }

        private boolean onDamage(@NonNull Location location, @NonNull Damageable target) {
            QuakerA3Info.PARTICLE.HIT_ENTITY_DECO.play(location);
            QuakerA3Info.SOUND.HIT.play(location);

            if (target.getDamageModule().damage(QuakerA3Projectile.this, QuakerA3Info.DAMAGE, DamageType.NORMAL, location,
                    false, true) && target instanceof Movable) {
                target.getStatusEffectModule().apply(Snare.getInstance(), combatUser, Timespan.ofTicks(QuakerA3Info.SNARE_DURATION));

                if (target instanceof CombatUser)
                    combatUser.addScore("돌풍 강타", QuakerA3Info.DAMAGE_SCORE);

                return true;
            }

            return false;
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
                onImpact(location, target);
                return !(target instanceof Barrier);
            }
        }
    }
}
