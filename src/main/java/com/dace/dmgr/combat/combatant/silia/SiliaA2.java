package com.dace.dmgr.combat.combatant.silia;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.ability.ActionKey;
import com.dace.dmgr.combat.ability.skill.ActiveSkill;
import com.dace.dmgr.combat.entity.DamageType;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.EntityCondition;
import com.dace.dmgr.combat.entity.Movable;
import com.dace.dmgr.combat.entity.combatuser.AbilityManager;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.combat.interaction.Projectile;
import com.dace.dmgr.util.location.LocationUtil;
import com.dace.dmgr.util.task.IntervalTask;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.EnumSet;
import java.util.Set;
import java.util.function.Consumer;

public final class SiliaA2 extends ActiveSkill {
    public SiliaA2(@NonNull CombatUser combatUser, @NonNull SiliaA2Info skillInfo) {
        super(combatUser, skillInfo, SiliaA2Info.COOLDOWN, Timespan.MAX, 1);
    }

    @Override
    @NonNull
    public Set<@NonNull ActionKey> getDefaultActionKeys() {
        return EnumSet.of(ActionKey.SLOT_2, ActionKey.RIGHT_CLICK);
    }

    @Override
    public boolean canUse(@NonNull ActionKey actionKey) {
        AbilityManager abilityManager = combatUser.getAbilityManager();
        return super.canUse(actionKey) && isDurationFinished() && abilityManager.getSkill(SiliaP2Info.getInstance()).isDurationFinished()
                && abilityManager.getSkill(SiliaUltInfo.getInstance()).isDurationFinished();
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        setDuration();
        combatUser.setGlobalCooldown(SiliaA2Info.GLOBAL_COOLDOWN);

        combatUser.getAbilityManager().getSkill(SiliaA3Info.getInstance()).cancel();

        SiliaA2Info.Effects.USE.play(combatUser.getLocation());

        addActionTask(new IntervalTask(i -> SiliaA2Info.Effects.playUseTick(i, combatUser.getEntity().getEyeLocation()), () -> {
            cancel();

            new SiliaA2Projectile().shot();

            SiliaA2Info.Effects.USE_READY.play(combatUser.getLocation());
        }, 1, SiliaA2Info.READY_DURATION.toTicks()));
    }

    @Override
    public boolean isCancellable() {
        return !isDurationFinished();
    }

    @Override
    protected void onCancelled() {
        setDuration(Timespan.ZERO);
    }

    private final class SiliaA2Projectile extends Projectile<Damageable> {
        private SiliaA2Projectile() {
            super(SiliaA2.this, SiliaA2Info.VELOCITY, EntityCondition.enemy(combatUser),
                    Option.builder().size(SiliaA2Info.SIZE).maxDistance(SiliaA2Info.DISTANCE).build());
        }

        @Override
        protected void onHit(@NonNull Location location) {
            SiliaA2Info.Effects.playHit(location);
        }

        @Override
        @NonNull
        protected IntervalHandler getIntervalHandler() {
            return createPeriodIntervalHandler(4, new Consumer<Location>() {
                private int i = 0;

                @Override
                public void accept(Location location) {
                    SiliaA2Info.Effects.playBulletTrail(i++, location);
                }
            });
        }

        @Override
        @NonNull
        protected HitBlockHandler getHitBlockHandler() {
            return (location, hitBlock) -> {
                SiliaA2Info.Effects.HIT_BLOCK.apply(hitBlock).play(location);
                return false;
            };
        }

        @Override
        @NonNull
        protected HitEntityHandler<Damageable> getHitEntityHandler() {
            return (location, target) -> {
                if (target.getDamageModule().damage(this, SiliaA2Info.DAMAGE, DamageType.NORMAL, location,
                        SiliaT1Util.getCritMultiplier(getVelocity(), target), true)) {

                    if (target instanceof Movable)
                        ((Movable) target).getKnockbackModule().knockback(new Vector(0, SiliaA2Info.PUSH, 0), true);

                    Location loc = target.getLocation().add(0, 0.1, 0);
                    loc.setPitch(0);
                    loc = LocationUtil.getLocationFromOffset(loc, 0, 0, -1.5);

                    SiliaA2Info.Effects.playHitEntity(location, combatUser.getLocation(), loc);

                    knockback(loc, target);
                }

                return false;
            };
        }

        /**
         * 맞은 적을 공중에 띄우고 순간이동한다.
         *
         * @param location 이동 위치
         * @param target   대상 엔티티
         */
        private void knockback(@NonNull Location location, @NonNull Damageable target) {
            if (!target.isCreature() || !target.canBeTargeted() || !LocationUtil.canPass(combatUser.getEntity().getEyeLocation(), location))
                return;

            combatUser.getMoveModule().teleport(location);
            combatUser.getMoveModule().push(new Vector(0, SiliaA2Info.PUSH, 0), true);

            if (target.isGoalTarget())
                combatUser.addScore("적 띄움", SiliaA2Info.DAMAGE_SCORE);
        }
    }
}
