package com.dace.dmgr.combat.character.jager.action;

import com.dace.dmgr.combat.BouncingProjectile;
import com.dace.dmgr.combat.BouncingProjectileOption;
import com.dace.dmgr.combat.ProjectileOption;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.HasEntity;
import com.dace.dmgr.combat.action.skill.UltimateSkill;
import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.combat.entity.CombatEntityUtil;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.SummonEntity;
import com.dace.dmgr.system.task.TaskTimer;
import com.dace.dmgr.util.LocationUtil;
import com.dace.dmgr.util.ParticleUtil;
import com.dace.dmgr.util.SoundUtil;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.MagmaCube;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JagerUlt extends UltimateSkill implements HasEntity {
    /** 소환된 엔티티 목록 */
    @Getter
    private final List<JagerUltEntity> summonEntities = new ArrayList<>();

    public JagerUlt(CombatUser combatUser) {
        super(4, combatUser, JagerUltInfo.getInstance());
    }

    @Override
    public List<ActionKey> getDefaultActionKeys() {
        return Arrays.asList(ActionKey.SLOT_4);
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
        return super.canUse() && isDurationFinished() && !((JagerA1) combatUser.getSkill(JagerA1Info.getInstance())).isConfirming() &&
                combatUser.getSkill(JagerA3Info.getInstance()).isDurationFinished();
    }

    @Override
    protected void onUseUltimateSkill(ActionKey actionKey) {
        if (((JagerWeaponL) combatUser.getWeapon()).isAiming()) {
            ((JagerWeaponL) combatUser.getWeapon()).aim();
            ((JagerWeaponL) combatUser.getWeapon()).swap();
        }

        combatUser.setGlobalCooldown((int) JagerUltInfo.READY_DURATION);
        Location location = combatUser.getEntity().getLocation();
        SoundUtil.play(Sound.ENTITY_CAT_PURREOW, location, 0.5F, 1.6F);
        summonEntities.forEach(SummonEntity::remove);
        summonEntities.clear();
        setDuration();

        new TaskTimer(1, JagerUltInfo.READY_DURATION) {
            @Override
            public boolean run(int i) {
                return true;
            }

            @Override
            public void onEnd(boolean cancelled) {
                setDuration(0);
                if (cancelled)
                    return;

                Location location = LocationUtil.getLocationFromOffset(combatUser.getEntity().getEyeLocation(),
                        combatUser.getEntity().getLocation().getDirection(), 0.2, -0.4, 0);
                SoundUtil.play(Sound.ENTITY_WITCH_THROW, location, 0.8F, 0.7F);

                new BouncingProjectile(combatUser, JagerUltInfo.VELOCITY, -1, ProjectileOption.builder().trailInterval(5).hasGravity(true).build(),
                        BouncingProjectileOption.builder().bounceVelocityMultiplier(0.35F).destroyOnHitFloor(true).build()) {
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
                    public boolean onHitEntityBouncing(Location location, Vector direction, CombatEntity<?> target, boolean isCrit) {
                        return false;
                    }

                    @Override
                    public void onDestroy(Location location) {
                        SoundUtil.play(Sound.ENTITY_PLAYER_HURT, location, 0.5F, 0.5F);

                        MagmaCube magmaCube = CombatEntityUtil.spawn(MagmaCube.class, location);
                        JagerUltEntity jagerUltEntity = new JagerUltEntity(magmaCube, combatUser);
                        jagerUltEntity.init();
                        summonEntities.add(jagerUltEntity);
                    }
                }.shoot(location);
            }
        };
    }
}
