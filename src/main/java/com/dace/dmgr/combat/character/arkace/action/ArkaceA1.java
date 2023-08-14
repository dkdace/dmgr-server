package com.dace.dmgr.combat.character.arkace.action;

import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.Projectile;
import com.dace.dmgr.combat.ProjectileOption;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.Skill;
import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.system.task.TaskTimer;
import com.dace.dmgr.system.task.TaskWait;
import com.dace.dmgr.util.LocationUtil;
import com.dace.dmgr.util.ParticleUtil;
import com.dace.dmgr.util.SoundUtil;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;

import java.util.Arrays;
import java.util.List;

public final class ArkaceA1 extends Skill {
    public ArkaceA1(CombatUser combatUser) {
        super(1, combatUser, ArkaceA1Info.getInstance(), 1);
    }

    @Override
    public List<ActionKey> getDefaultActionKeys() {
        return Arrays.asList(ActionKey.SLOT_2, ActionKey.LEFT_CLICK);
    }

    @Override
    public long getDefaultCooldown() {
        return ArkaceA1Info.COOLDOWN;
    }

    @Override
    public long getDefaultDuration() {
        return -1;
    }

    @Override
    public boolean canUse() {
        return super.canUse() && isDurationFinished();
    }

    @Override
    public void onUse(ActionKey actionKey) {
        combatUser.getWeapon().setCooldown(10);
        setGlobalCooldown(10);
        setDuration();

        new TaskTimer(5, 3) {
            @Override
            public boolean run(int i) {
                Location location = LocationUtil.getLocationFromOffset(combatUser.getEntity().getEyeLocation(),
                        combatUser.getEntity().getLocation().getDirection(), -0.2, -0.4, 0);
                SoundUtil.play("random.gun.grenade", location, 3F, 1.5F);
                SoundUtil.play(Sound.ENTITY_SHULKER_SHOOT, location, 3F, 1.2F);

                new Projectile(combatUser, ArkaceA1Info.VELOCITY, ProjectileOption.builder().trailInterval(5).build()) {
                    @Override
                    public void trail(Location location) {
                        ParticleUtil.play(Particle.CRIT_MAGIC, location, 1, 0, 0, 0, 0);
                        ParticleUtil.playRGB(ParticleUtil.ColoredParticle.REDSTONE, location, 1,
                                0, 0, 0, 32, 250, 225);
                    }

                    @Override
                    public void onHit(Location location) {
                        explode(combatUser, location);
                    }

                    @Override
                    public void onHitEntity(Location location, CombatEntity<?> target, boolean isCrit) {
                        target.damage(combatUser, ArkaceA1Info.DAMAGE_DIRECT, "", false, true);
                    }
                }.shoot(location);

                return true;
            }

            @Override
            public void onEnd(boolean cancelled) {
                new TaskWait(4) {
                    @Override
                    public void run() {
                        setDuration(0);
                    }
                };
            }
        };
    }

    private void explode(CombatUser combatUser, Location location) {
        SoundUtil.play(Sound.ENTITY_FIREWORK_LARGE_BLAST, location, 4F, 0.8F);
        SoundUtil.play(Sound.ENTITY_GENERIC_EXPLODE, location, 4F, 1.4F);
        SoundUtil.play("random.gun_reverb2", location, 6F, 0.9F);
        ParticleUtil.playRGB(ParticleUtil.ColoredParticle.REDSTONE, location, 200,
                2.5F, 2.5F, 2.5F, 32, 250, 225);
        ParticleUtil.play(Particle.EXPLOSION_NORMAL, location, 40, 0.2F, 0.2F, 0.2F, 0.2F);

        if (location.distance(combatUser.getEntity().getLocation()) < ArkaceA1Info.RADIUS)
            combatUser.damage(combatUser, ArkaceA1Info.DAMAGE_EXPLODE, "", false, true);
        CombatUtil.getNearEnemies(combatUser, location, ArkaceA1Info.RADIUS).forEach(target ->
                target.damage(combatUser, ArkaceA1Info.DAMAGE_EXPLODE, "", false, true));
    }
}
