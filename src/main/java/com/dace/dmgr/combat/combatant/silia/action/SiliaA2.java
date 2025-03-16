package com.dace.dmgr.combat.combatant.silia.action;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.CombatEffectUtil;
import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.ActiveSkill;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.DamageType;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.Movable;
import com.dace.dmgr.combat.interaction.Projectile;
import com.dace.dmgr.util.LocationUtil;
import com.dace.dmgr.util.VectorUtil;
import com.dace.dmgr.util.task.IntervalTask;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.function.Consumer;

public final class SiliaA2 extends ActiveSkill {
    public SiliaA2(@NonNull CombatUser combatUser) {
        super(combatUser, SiliaA2Info.getInstance(), SiliaA2Info.COOLDOWN, Timespan.MAX, 1);
    }

    @Override
    @NonNull
    public ActionKey @NonNull [] getDefaultActionKeys() {
        return new ActionKey[]{ActionKey.SLOT_2, ActionKey.RIGHT_CLICK};
    }

    @Override
    public boolean canUse(@NonNull ActionKey actionKey) {
        return super.canUse(actionKey) && isDurationFinished() && combatUser.getSkill(SiliaP2Info.getInstance()).isDurationFinished()
                && combatUser.getSkill(SiliaUltInfo.getInstance()).isDurationFinished();
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        setDuration();
        combatUser.setGlobalCooldown(SiliaA2Info.GLOBAL_COOLDOWN);

        SiliaA3 skill3 = combatUser.getSkill(SiliaA3Info.getInstance());
        skill3.cancel();

        SiliaA2Info.SOUND.USE.play(combatUser.getLocation());

        addActionTask(new IntervalTask(i -> {
            Location loc = LocationUtil.getLocationFromOffset(combatUser.getEntity().getEyeLocation(), 0, 0, 1);
            Vector vector = VectorUtil.getYawAxis(loc).multiply(0.8);
            Vector axis = VectorUtil.getRollAxis(loc);

            for (int j = 0; j < 6; j++) {
                Vector vec = VectorUtil.getRotatedVector(vector, axis, i * 23 + j * 60).multiply(1.6 - i * 0.2);
                SiliaA2Info.PARTICLE.USE_TICK.play(loc.clone().add(vec), vec);
            }
        }, () -> {
            cancel();

            new SiliaA2Projectile().shot();

            SiliaA2Info.SOUND.USE_READY.play(combatUser.getLocation());
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
            super(combatUser, SiliaA2Info.VELOCITY, CombatUtil.EntityCondition.enemy(combatUser),
                    Option.builder().size(SiliaA2Info.SIZE).maxDistance(SiliaA2Info.DISTANCE).build());
        }

        @Override
        protected void onHit(@NonNull Location location) {
            for (int i = 0; i < 40; i++) {
                Vector vec = VectorUtil.getSpreadedVector(new Vector(0, 1, 0), 60);
                SiliaA2Info.PARTICLE.HIT.play(location, vec, Math.random());
            }
        }

        @Override
        @NonNull
        protected IntervalHandler getIntervalHandler() {
            return createPeriodIntervalHandler(4, new Consumer<Location>() {
                private int i = 0;

                @Override
                public void accept(Location location) {
                    i++;

                    Vector vector = VectorUtil.getYawAxis(location).multiply(0.8);
                    Vector axis = VectorUtil.getRollAxis(location);

                    int angle = i * 12;
                    for (int j = 0; j < 2; j++) {
                        angle += 180;
                        Vector vec = VectorUtil.getSpreadedVector(VectorUtil.getRotatedVector(vector, axis, angle), 8);
                        Location loc = location.clone().add(vec);

                        SiliaA2Info.PARTICLE.BULLET_TRAIL.play(loc, vec);
                    }
                }
            });
        }

        @Override
        @NonNull
        protected HitBlockHandler getHitBlockHandler() {
            return (location, hitBlock) -> {
                CombatEffectUtil.playHitBlockParticle(location, hitBlock, 3);
                return false;
            };
        }

        @Override
        @NonNull
        protected HitEntityHandler<Damageable> getHitEntityHandler() {
            return (location, target) -> {
                if (target.getDamageModule().damage(this, SiliaA2Info.DAMAGE, DamageType.NORMAL, location,
                        SiliaT1.isBackAttack(getVelocity(), target) ? SiliaT1Info.CRIT_MULTIPLIER : 1, true)) {

                    if (target instanceof Movable)
                        ((Movable) target).getMoveModule().knockback(new Vector(0, SiliaA2Info.PUSH, 0), true);

                    Location loc = target.getLocation().add(0, 0.1, 0);
                    loc.setPitch(0);
                    loc = LocationUtil.getLocationFromOffset(loc, 0, 0, -1.5);
                    for (Location loc2 : LocationUtil.getLine(combatUser.getLocation(), loc, 0.5))
                        SiliaA2Info.PARTICLE.HIT_ENTITY.play(loc2.clone().add(0, 1, 0));
                    SiliaA2Info.SOUND.HIT_ENTITY.play(location);

                    if (target.isCreature() && LocationUtil.canPass(combatUser.getEntity().getEyeLocation(), loc)
                            && (!(target instanceof CombatUser) || !((CombatUser) target).isDead())) {
                        combatUser.getMoveModule().teleport(loc);
                        combatUser.getMoveModule().push(new Vector(0, SiliaA2Info.PUSH, 0), true);

                        if (target instanceof CombatUser)
                            combatUser.addScore("적 띄움", SiliaA2Info.DAMAGE_SCORE);
                    }
                }

                return false;
            };
        }
    }
}
