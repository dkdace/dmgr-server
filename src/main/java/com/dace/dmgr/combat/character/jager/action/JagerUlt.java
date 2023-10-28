package com.dace.dmgr.combat.character.jager.action;

import com.dace.dmgr.combat.BouncingProjectile;
import com.dace.dmgr.combat.BouncingProjectileOption;
import com.dace.dmgr.combat.ProjectileOption;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.ActionModule;
import com.dace.dmgr.combat.action.skill.HasEntity;
import com.dace.dmgr.combat.action.skill.UltimateSkill;
import com.dace.dmgr.combat.action.skill.module.HasEntityModule;
import com.dace.dmgr.combat.entity.CombatEntityUtil;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.system.task.ActionTaskTimer;
import com.dace.dmgr.system.task.TaskManager;
import com.dace.dmgr.util.LocationUtil;
import com.dace.dmgr.util.ParticleUtil;
import com.dace.dmgr.util.SoundUtil;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.MagmaCube;
import org.bukkit.util.Vector;

@Getter
public final class JagerUlt extends UltimateSkill implements HasEntity<JagerUltEntity> {
    /** 소환된 엔티티 */
    private final HasEntityModule<JagerUltEntity> hasEntityModule;

    public JagerUlt(CombatUser combatUser) {
        super(4, combatUser, JagerUltInfo.getInstance());
        hasEntityModule = new HasEntityModule<>(this);
    }

    @Override
    public ActionModule[] getModules() {
        return new ActionModule[]{hasEntityModule};
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
    protected void onUseUltimateSkill(ActionKey actionKey) {
        if (((JagerWeaponL) combatUser.getWeapon()).getAimModule().isAiming()) {
            ((JagerWeaponL) combatUser.getWeapon()).getAimModule().toggleAim();
            ((JagerWeaponL) combatUser.getWeapon()).getSwapModule().swap();
        }

        combatUser.setGlobalCooldown((int) JagerUltInfo.READY_DURATION);
        Location location = combatUser.getEntity().getLocation();
        playUseSound(location);
        setDuration();
        hasEntityModule.removeSummonEntity();

        TaskManager.addTask(this, new ActionTaskTimer(combatUser, 1, JagerUltInfo.READY_DURATION) {
            @Override
            public boolean onTickAction(int i) {
                return true;
            }

            @Override
            public void onEnd(boolean cancelled) {
                setDuration(0);
                if (cancelled)
                    return;

                Location loc = LocationUtil.getLocationFromOffset(combatUser.getEntity().getEyeLocation().subtract(0, 0.4, 0),
                        combatUser.getEntity().getLocation().getDirection(), 0.2, 0, 0);
                SoundUtil.play(Sound.ENTITY_WITCH_THROW, loc, 0.8F, 0.7F);

                new JagerUltProjectile().shoot(loc);
            }
        });
    }

    /**
     * 사용 시 효과음을 재생한다.
     *
     * @param location 사용 위치
     */
    private void playUseSound(Location location) {
        SoundUtil.play(Sound.ENTITY_CAT_PURREOW, location, 0.5F, 1.6F);
    }

    private class JagerUltProjectile extends BouncingProjectile {
        public JagerUltProjectile() {
            super(JagerUlt.this.combatUser, JagerUltInfo.VELOCITY, -1, ProjectileOption.builder().trailInterval(8).hasGravity(true)
                    .condition(JagerUlt.this.combatUser::isEnemy).build(), BouncingProjectileOption.builder().bounceVelocityMultiplier(0.35F)
                    .destroyOnHitFloor(true).build());
        }

        @Override
        public void trail(Location location) {
            ParticleUtil.playRGB(ParticleUtil.ColoredParticle.REDSTONE, location, 15,
                    0.6F, 0.02F, 0.6F, 96, 220, 255);
        }

        @Override
        public boolean onHitBlockBouncing(Location location, Vector direction, Block hitBlock) {
            return false;
        }

        @Override
        public boolean onHitEntityBouncing(Location location, Vector direction, Damageable target, boolean isCrit) {
            return false;
        }

        @Override
        public void onDestroy(Location location) {
            MagmaCube magmaCube = CombatEntityUtil.spawn(MagmaCube.class, location);
            JagerUltEntity jagerUltEntity = new JagerUltEntity(magmaCube, combatUser);
            jagerUltEntity.init();
            hasEntityModule.setSummonEntity(jagerUltEntity);
        }
    }
}
