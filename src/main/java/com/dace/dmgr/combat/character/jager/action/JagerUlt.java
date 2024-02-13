package com.dace.dmgr.combat.character.jager.action;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.UltimateSkill;
import com.dace.dmgr.combat.entity.CombatEntityUtil;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.interaction.BouncingProjectile;
import com.dace.dmgr.combat.interaction.BouncingProjectileOption;
import com.dace.dmgr.combat.interaction.ProjectileOption;
import com.dace.dmgr.util.LocationUtil;
import com.dace.dmgr.util.ParticleUtil;
import com.dace.dmgr.util.SoundUtil;
import com.dace.dmgr.util.task.DelayTask;
import com.dace.dmgr.util.task.TaskUtil;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.MagmaCube;
import org.bukkit.util.Vector;

@Getter
public final class JagerUlt extends UltimateSkill {
    /** 소환한 엔티티 */
    JagerUltEntity entity = null;

    public JagerUlt(@NonNull CombatUser combatUser) {
        super(4, combatUser, JagerUltInfo.getInstance());
    }

    @Override
    public long getDefaultDuration() {
        return -1;
    }

    @Override
    public int getCost() {
        return JagerUltInfo.COST;
    }

    @Override
    public boolean canUse() {
        return super.canUse() && isDurationFinished() && !((JagerA1) combatUser.getSkill(JagerA1Info.getInstance())).getConfirmModule().isChecking() &&
                combatUser.getSkill(JagerA3Info.getInstance()).isDurationFinished();
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        super.onUse(actionKey);

        combatUser.getWeapon().onCancelled();
        combatUser.setGlobalCooldown((int) JagerUltInfo.READY_DURATION);

        Location location = combatUser.getEntity().getLocation();
        playUseSound(location);
        setDuration();
        if (entity != null)
            entity.dispose();

        TaskUtil.addTask(taskRunner, new DelayTask(() -> {
            onCancelled();

            Location loc = LocationUtil.getLocationFromOffset(combatUser.getEntity().getEyeLocation().subtract(0, 0.4, 0),
                    combatUser.getEntity().getLocation().getDirection(), 0.2, 0, 0);
            SoundUtil.play(Sound.ENTITY_WITCH_THROW, loc, 0.8, 0.7);

            new JagerUltProjectile().shoot(loc);
        }, JagerUltInfo.READY_DURATION));
    }

    @Override
    public void onCancelled() {
        super.onCancelled();
        setDuration(0);
    }

    /**
     * 사용 시 효과음을 재생한다.
     *
     * @param location 사용 위치
     */
    private void playUseSound(Location location) {
        SoundUtil.play(Sound.ENTITY_CAT_PURREOW, location, 0.5, 1.6);
    }

    @Override
    public void reset() {
        super.reset();

        if (entity != null)
            entity.dispose();
    }

    private class JagerUltProjectile extends BouncingProjectile {
        public JagerUltProjectile() {
            super(JagerUlt.this.combatUser, JagerUltInfo.VELOCITY, -1, ProjectileOption.builder().trailInterval(8).hasGravity(true)
                    .condition(JagerUlt.this.combatUser::isEnemy).build(), BouncingProjectileOption.builder().bounceVelocityMultiplier(0.35)
                    .destroyOnHitFloor(true).build());
        }

        @Override
        public void trail(@NonNull Location location) {
            ParticleUtil.playRGB(ParticleUtil.ColoredParticle.REDSTONE, location, 15,
                    0.6, 0.02, 0.6, 96, 220, 255);
        }

        @Override
        public boolean onHitBlockBouncing(@NonNull Location location, @NonNull Vector direction, @NonNull Block hitBlock) {
            return false;
        }

        @Override
        public boolean onHitEntityBouncing(@NonNull Location location, @NonNull Vector direction, @NonNull Damageable target, boolean isCrit) {
            return false;
        }

        @Override
        public void onDestroy(@NonNull Location location) {
            MagmaCube magmaCube = CombatEntityUtil.spawn(MagmaCube.class, location);
            entity = new JagerUltEntity(magmaCube, combatUser);
            entity.activate();
        }
    }
}
