package com.dace.dmgr.combat.character.silia.action;

import com.dace.dmgr.DMGR;
import com.dace.dmgr.combat.CombatEffectUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.ActiveSkill;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.interaction.DamageType;
import com.dace.dmgr.combat.interaction.Projectile;
import com.dace.dmgr.combat.interaction.ProjectileOption;
import com.dace.dmgr.util.LocationUtil;
import com.dace.dmgr.util.ParticleUtil;
import com.dace.dmgr.util.VectorUtil;
import com.dace.dmgr.util.task.IntervalTask;
import com.dace.dmgr.util.task.TaskUtil;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

public final class SiliaA2 extends ActiveSkill {
    public SiliaA2(@NonNull CombatUser combatUser) {
        super(combatUser, SiliaA2Info.getInstance(), 1);
    }

    @Override
    @NonNull
    public ActionKey @NonNull [] getDefaultActionKeys() {
        return new ActionKey[]{ActionKey.SLOT_2, ActionKey.RIGHT_CLICK};
    }

    @Override
    public long getDefaultCooldown() {
        return SiliaA2Info.COOLDOWN;
    }

    @Override
    public long getDefaultDuration() {
        return -1;
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
        if (skill3.isCancellable())
            skill3.onCancelled();

        SiliaA2Info.SOUND.USE.play(combatUser.getEntity().getLocation());

        TaskUtil.addTask(taskRunner, new IntervalTask(i -> {
            Location loc = LocationUtil.getLocationFromOffset(combatUser.getEntity().getEyeLocation(), 0, 0, 1);
            Vector vector = VectorUtil.getYawAxis(loc).multiply(0.8);
            Vector axis = VectorUtil.getRollAxis(loc);

            for (int j = 0; j < 6; j++) {
                Vector vec = VectorUtil.getRotatedVector(vector, axis, i * 23 + j * 60).multiply(1.6 - i * 0.2);
                ParticleUtil.play(Particle.EXPLOSION_NORMAL, loc.clone().add(vec), 0, vec.getX(), vec.getY(), vec.getZ(), 0.2);
            }
        }, () -> {
            onCancelled();

            new SiliaA2Projectile().shoot();

            SiliaA2Info.SOUND.USE_READY.play(combatUser.getEntity().getLocation());
        }, 1, SiliaA2Info.READY_DURATION));
    }

    @Override
    public boolean isCancellable() {
        return !isDurationFinished();
    }

    @Override
    public void onCancelled() {
        super.onCancelled();
        setDuration(0);
    }

    private final class SiliaA2Projectile extends Projectile {
        int i = 0;

        private SiliaA2Projectile() {
            super(combatUser, SiliaA2Info.VELOCITY, ProjectileOption.builder().trailInterval(4).size(SiliaA2Info.SIZE)
                    .maxDistance(SiliaA2Info.DISTANCE).condition(combatUser::isEnemy).build());
        }

        @Override
        protected void onTrailInterval() {
            i++;

            Vector vector = VectorUtil.getYawAxis(getLocation()).multiply(0.8);
            Vector axis = VectorUtil.getRollAxis(getLocation());

            int angle = i * 12;
            for (int j = 0; j < 2; j++) {
                angle += 180;
                Vector vec = VectorUtil.getSpreadedVector(VectorUtil.getRotatedVector(vector, axis, angle), 8);
                Location loc = getLocation().clone().add(vec);

                ParticleUtil.play(Particle.EXPLOSION_NORMAL, loc, 0, vec.getX(), vec.getY(), vec.getZ(), 0.25);
                ParticleUtil.playRGB(ParticleUtil.ColoredParticle.REDSTONE, loc, 3,
                        0.3, 0.3, 0.3, 255, 255, 255);
            }
        }

        @Override
        protected void onHit() {
            for (int j = 0; j < 40; j++) {
                Vector vec = VectorUtil.getSpreadedVector(new Vector(0, 1, 0), 60);
                ParticleUtil.play(Particle.EXPLOSION_NORMAL, getLocation(), 0, vec.getX(), vec.getY(), vec.getZ(),
                        0.3 + DMGR.getRandom().nextDouble() * 0.4);
            }
        }

        @Override
        protected boolean onHitBlock(@NonNull Block hitBlock) {
            CombatEffectUtil.playBlockHitEffect(getLocation(), hitBlock, 3);
            return false;
        }

        @Override
        protected boolean onHitEntity(@NonNull Damageable target, boolean isCrit) {
            if (target.getDamageModule().damage(this, SiliaA2Info.DAMAGE, DamageType.NORMAL, getLocation(),
                    SiliaT1.isBackAttack(getVelocity(), target) ? SiliaT1Info.CRIT_MULTIPLIER : 1, true)) {
                target.getKnockbackModule().knockback(new Vector(0, SiliaA2Info.PUSH, 0), true);

                Location loc = target.getEntity().getLocation().add(0, 0.1, 0);
                loc.setPitch(0);
                loc = LocationUtil.getLocationFromOffset(loc, 0, 0, -1.5);
                for (Location loc2 : LocationUtil.getLine(combatUser.getEntity().getLocation(), loc, 0.5))
                    ParticleUtil.play(Particle.END_ROD, loc2.add(0, 1, 0), 3, 0, 0, 0, 0.05);
                SiliaA2Info.SOUND.HIT_ENTITY.play(getLocation());

                if (target.getDamageModule().isLiving() && LocationUtil.canPass(combatUser.getEntity().getEyeLocation(), loc)
                        && (!(target instanceof CombatUser) || !((CombatUser) target).isDead())) {
                    combatUser.getMoveModule().teleport(loc);
                    combatUser.getMoveModule().push(new Vector(0, SiliaA2Info.PUSH, 0), true);

                    if (target instanceof CombatUser)
                        combatUser.addScore("적 띄움", SiliaA2Info.DAMAGE_SCORE);
                }
            }

            return false;
        }
    }
}
