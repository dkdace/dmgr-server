package com.dace.dmgr.combat.character.jager.action;

import com.dace.dmgr.combat.BouncingProjectile;
import com.dace.dmgr.combat.BouncingProjectileOption;
import com.dace.dmgr.combat.ProjectileOption;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.ActiveSkill;
import com.dace.dmgr.combat.action.skill.HasEntity;
import com.dace.dmgr.combat.entity.CombatEntityUtil;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.damageable.Damageable;
import com.dace.dmgr.system.task.ActionTaskTimer;
import com.dace.dmgr.system.task.TaskManager;
import com.dace.dmgr.util.LocationUtil;
import com.dace.dmgr.util.ParticleUtil;
import com.dace.dmgr.util.SoundUtil;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.MagmaCube;
import org.bukkit.util.Vector;

@Getter
@Setter
public final class JagerA2 extends ActiveSkill implements HasEntity<JagerA2Entity> {
    /** 소환된 엔티티 */
    private JagerA2Entity summonEntity = null;

    public JagerA2(CombatUser combatUser) {
        super(2, combatUser, JagerA2Info.getInstance(), 1);
    }

    @Override
    public ActionKey[] getDefaultActionKeys() {
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
    public void onUse(ActionKey actionKey) {
        if (((JagerWeaponL) combatUser.getWeapon()).getAimModule().isAiming()) {
            ((JagerWeaponL) combatUser.getWeapon()).toggleAim();
            ((JagerWeaponL) combatUser.getWeapon()).swap();
        }

        combatUser.setGlobalCooldown((int) JagerA2Info.READY_DURATION);
        Location location = combatUser.getEntity().getLocation();
        setDuration();
        playUseSound(location);
        removeSummonEntity();

        TaskManager.addTask(this, new ActionTaskTimer(combatUser, 1, JagerA2Info.READY_DURATION) {
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
                new JagerA2Projectile().shoot(loc);

                playThrowSound(loc);
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

    /**
     * 투척 시 효과음을 재생한다.
     *
     * @param location 사용 위치
     */
    private void playThrowSound(Location location) {
        SoundUtil.play(Sound.ENTITY_WITCH_THROW, location, 0.8F, 0.7F);
    }

    private class JagerA2Projectile extends BouncingProjectile {
        public JagerA2Projectile() {
            super(JagerA2.this.combatUser, JagerA2Info.VELOCITY, -1, ProjectileOption.builder().trailInterval(8).hasGravity(true)
                    .condition(JagerA2.this.combatUser::isEnemy).build(), BouncingProjectileOption.builder().bounceVelocityMultiplier(0.35F)
                    .destroyOnHitFloor(true).build());
        }

        @Override
        public void trail(Location location) {
            ParticleUtil.playRGB(ParticleUtil.ColoredParticle.REDSTONE, location, 17,
                    0.7F, 0, 0.7F, 120, 120, 135);
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
            JagerA2Entity jagerA2Entity = new JagerA2Entity(magmaCube, combatUser);
            jagerA2Entity.init();
            setSummonEntity(jagerA2Entity);
        }
    }
}
