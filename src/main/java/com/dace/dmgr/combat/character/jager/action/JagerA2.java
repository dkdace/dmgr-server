package com.dace.dmgr.combat.character.jager.action;

import com.dace.dmgr.combat.BouncingProjectile;
import com.dace.dmgr.combat.BouncingProjectileOption;
import com.dace.dmgr.combat.ProjectileOption;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.HasEntity;
import com.dace.dmgr.combat.action.skill.Skill;
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

public final class JagerA2 extends Skill implements HasEntity {
    /** 소환된 엔티티 목록 */
    @Getter
    private final List<JagerA2Entity> summonEntities = new ArrayList<>();

    public JagerA2(CombatUser combatUser) {
        super(2, combatUser, JagerA2Info.getInstance(), 1);
    }

    @Override
    public List<ActionKey> getDefaultActionKeys() {
        return Arrays.asList(ActionKey.SLOT_2);
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
        return super.canUse() && isDurationFinished() && !((JagerA1) combatUser.getSkill(JagerA1Info.getInstance())).isConfirming() &&
                combatUser.getSkill(JagerA3Info.getInstance()).isDurationFinished();
    }

    @Override
    public void onUse(ActionKey actionKey) {
        if (((JagerWeaponL) combatUser.getWeapon()).isAiming()) {
            ((JagerWeaponL) combatUser.getWeapon()).aim();
            ((JagerWeaponL) combatUser.getWeapon()).swap();
        }

        Location location = combatUser.getEntity().getLocation();
        SoundUtil.play(Sound.ENTITY_CAT_PURREOW, location, 0.5F, 1.6F);
        summonEntities.forEach(SummonEntity::remove);
        summonEntities.clear();
        setGlobalCooldown((int) JagerA2Info.READY_DURATION);
        setDuration();

        new TaskTimer(1, JagerA2Info.READY_DURATION) {
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

                new BouncingProjectile(combatUser, JagerA2Info.VELOCITY, -1, ProjectileOption.builder().trailInterval(5).hasGravity(true).build(),
                        BouncingProjectileOption.builder().bounceVelocityMultiplier(0.35F).destroyOnHitFloor(true).build()) {
                    @Override
                    public void trail(Location location) {
                        ParticleUtil.playRGB(ParticleUtil.ColoredParticle.REDSTONE, location, 10,
                                0.3F, 0, 0.3F, 120, 120, 135);
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
                        SoundUtil.play(Sound.ENTITY_HORSE_ARMOR, location, 0.5F, 1.6F);
                        SoundUtil.play("random.craft", location, 0.5F, 1.3F);
                        SoundUtil.play(Sound.ENTITY_PLAYER_HURT, location, 0.5F, 0.5F);

                        MagmaCube magmaCube = CombatEntityUtil.spawn(MagmaCube.class, location);
                        JagerA2Entity jagerA2Entity = new JagerA2Entity(magmaCube, combatUser);
                        jagerA2Entity.init();
                        summonEntities.add(jagerA2Entity);
                    }
                }.shoot(location);
            }
        };
    }
}
