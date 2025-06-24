package com.dace.dmgr.combat.combatant.quaker;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.ability.ActionKey;
import com.dace.dmgr.combat.ability.skill.ActiveSkill;
import com.dace.dmgr.combat.ability.weapon.Weapon;
import com.dace.dmgr.combat.entity.DamageType;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.EntityCondition;
import com.dace.dmgr.combat.entity.Movable;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.combat.entity.module.Modifier;
import com.dace.dmgr.combat.entity.module.statuseffect.Snare;
import com.dace.dmgr.combat.entity.temporary.Barrier;
import com.dace.dmgr.combat.interaction.Area;
import com.dace.dmgr.combat.interaction.Projectile;
import com.dace.dmgr.util.location.LocationUtil;
import com.dace.dmgr.util.task.IntervalTask;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.inventory.MainHand;
import org.bukkit.util.Vector;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

public final class QuakerA3 extends ActiveSkill {
    /** 수정자 */
    private static final Modifier MODIFIER = new Modifier(-100);

    public QuakerA3(@NonNull CombatUser combatUser, @NonNull QuakerA3Info skillInfo) {
        super(combatUser, skillInfo, QuakerA3Info.COOLDOWN, Timespan.MAX, 2);
    }

    @Override
    @NonNull
    public Set<@NonNull ActionKey> getDefaultActionKeys() {
        return EnumSet.of(ActionKey.SLOT_3);
    }

    @Override
    public boolean canUse(@NonNull ActionKey actionKey) {
        return super.canUse(actionKey) && isDurationFinished() && combatUser.getAbilityManager().getSkill(QuakerA1Info.getInstance()).isDurationFinished();
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        setDuration();

        combatUser.setGlobalCooldown(QuakerA3Info.GLOBAL_COOLDOWN);
        combatUser.getMoveModule().addModifier(MODIFIER);
        combatUser.playMeleeAttackAnimation(-7, Timespan.ofTicks(12), MainHand.RIGHT);

        Weapon weapon = combatUser.getAbilityManager().getWeapon();
        weapon.cancel();
        weapon.setVisible(false);

        QuakerA3Info.Effects.USE.play(combatUser.getLocation());

        addActionTask(new IntervalTask(i -> QuakerA3Info.Effects.playUseTick(i, combatUser.getEntity().getEyeLocation()), () -> {
            cancel();

            new QuakerA3Projectile().shot();

            QuakerA3Info.Effects.USE_READY.play(combatUser.getLocation());
        }, 1, QuakerA3Info.READY_DURATION.toTicks()));
    }

    @Override
    public boolean isCancellable() {
        return !isDurationFinished();
    }

    @Override
    protected void onCancelled() {
        setDuration(Timespan.ZERO);

        combatUser.getMoveModule().removeModifier(MODIFIER);
        combatUser.getAbilityManager().getWeapon().setVisible(true);
    }

    private final class QuakerA3Projectile extends Projectile<Damageable> {
        private final HashSet<Damageable> targets = new HashSet<>();

        private QuakerA3Projectile() {
            super(QuakerA3.this, QuakerA3Info.VELOCITY, EntityCondition.enemy(combatUser),
                    Option.builder().size(QuakerA3Info.SIZE).maxDistance(QuakerA3Info.DISTANCE).build());
        }

        @Override
        protected void onHit(@NonNull Location location) {
            QuakerA3Info.Effects.HIT.play(location);
        }

        @Override
        @NonNull
        protected IntervalHandler getIntervalHandler() {
            return createPeriodIntervalHandler(16, location -> QuakerA3Info.Effects.playBulletTrail(location, getVelocity()));
        }

        @Override
        @NonNull
        protected HitBlockHandler getHitBlockHandler() {
            return (location, hitBlock) -> {
                QuakerA3Info.Effects.HIT_BLOCK.apply(hitBlock).play(location);
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

            QuakerA3Info.Effects.HIT_ENTITY.play(location);
        }

        /**
         * 맞은 적을 밀쳐낸다.
         *
         * @param target 대상 엔티티
         */
        private void knockback(@NonNull Movable target) {
            target.addTask(new IntervalTask(i -> {
                if (!target.canBeTargeted())
                    return false;

                if (i < 3)
                    target.getKnockbackModule().knockback(getVelocity().normalize().multiply(QuakerA3Info.KNOCKBACK), true);

                Location loc = target.getCenterLocation().add(0, 0.1, 0);
                new QuakerA3Area().emit(loc);

                QuakerA3Info.Effects.playHitEntityKnockback(target.getCenterLocation(), getVelocity());

                Location hitLoc = loc.clone().add(getVelocity().normalize());
                if (!LocationUtil.isNonSolid(hitLoc)) {
                    onHitEnemy(hitLoc, (Damageable) target);
                    target.getKnockbackModule().knockback(new Vector(), true);

                    QuakerA3Info.Effects.HIT_ENTITY_WALL.apply(hitLoc.getBlock()).play(loc);

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
