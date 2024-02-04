package com.dace.dmgr.combat.character.jager.action;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.ActiveSkill;
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
public final class JagerA2 extends ActiveSkill {
    /** 소환한 엔티티 */
    JagerA2Entity entity = null;

    public JagerA2(CombatUser combatUser) {
        super(2, combatUser, JagerA2Info.getInstance(), 1);
    }

    @Override
    @NonNull
    public ActionKey @NonNull [] getDefaultActionKeys() {
        return new ActionKey[]{ActionKey.SLOT_2};
    }

    @Override
    public long getDefaultCooldown() {
        return JagerA2Info.COOLDOWN;
    }

    @Override
    public long getDefaultDuration() {
        return -1;
    }

    @Override
    public boolean canUse() {
        return super.canUse() && isDurationFinished() && !((JagerA1) combatUser.getSkill(JagerA1Info.getInstance())).getConfirmModule().isChecking() &&
                combatUser.getSkill(JagerA3Info.getInstance()).isDurationFinished();
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        if (((JagerWeaponL) combatUser.getWeapon()).getAimModule().isAiming()) {
            ((JagerWeaponL) combatUser.getWeapon()).getAimModule().toggleAim();
            ((JagerWeaponL) combatUser.getWeapon()).getSwapModule().swap();
        }

        combatUser.setGlobalCooldown((int) JagerA2Info.READY_DURATION);
        Location location = combatUser.getEntity().getLocation();
        setDuration();
        playUseSound(location);
        if (entity != null)
            entity.dispose();

        TaskUtil.addTask(taskRunner, new DelayTask(() -> {
            onCancelled();

            Location loc = LocationUtil.getLocationFromOffset(combatUser.getEntity().getEyeLocation().subtract(0, 0.4, 0),
                    combatUser.getEntity().getLocation().getDirection(), 0.2, 0, 0);
            new JagerA2Projectile().shoot(loc);

            playThrowSound(loc);
        }, JagerA2Info.READY_DURATION));
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

    /**
     * 투척 시 효과음을 재생한다.
     *
     * @param location 사용 위치
     */
    private void playThrowSound(Location location) {
        SoundUtil.play(Sound.ENTITY_WITCH_THROW, location, 0.8, 0.7);
    }


    @Override
    public void reset() {
        super.reset();

        if (entity != null)
            entity.dispose();
    }

    private class JagerA2Projectile extends BouncingProjectile {
        public JagerA2Projectile() {
            super(JagerA2.this.combatUser, JagerA2Info.VELOCITY, -1, ProjectileOption.builder().trailInterval(8).hasGravity(true)
                    .condition(JagerA2.this.combatUser::isEnemy).build(), BouncingProjectileOption.builder().bounceVelocityMultiplier(0.35)
                    .destroyOnHitFloor(true).build());
        }

        @Override
        public void trail(@NonNull Location location) {
            ParticleUtil.playRGB(ParticleUtil.ColoredParticle.REDSTONE, location, 17,
                    0.7, 0, 0.7, 120, 120, 135);
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
            entity = new JagerA2Entity(magmaCube, combatUser);
            entity.activate();
        }
    }
}
