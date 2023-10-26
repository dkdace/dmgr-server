package com.dace.dmgr.combat.character.arkace.action;

import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.DamageType;
import com.dace.dmgr.combat.Projectile;
import com.dace.dmgr.combat.ProjectileOption;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.ActiveSkill;
import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.damageable.Damageable;
import com.dace.dmgr.system.task.ActionTaskTimer;
import com.dace.dmgr.system.task.TaskManager;
import com.dace.dmgr.system.task.TaskWait;
import com.dace.dmgr.util.LocationUtil;
import com.dace.dmgr.util.ParticleUtil;
import com.dace.dmgr.util.SoundUtil;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

public final class ArkaceA1 extends ActiveSkill {
    public ArkaceA1(CombatUser combatUser) {
        super(1, combatUser, ArkaceA1Info.getInstance(), 1);
    }

    @Override
    public ActionKey[] getDefaultActionKeys() {
        return new ActionKey[]{ActionKey.SLOT_2, ActionKey.LEFT_CLICK};
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
        combatUser.setGlobalCooldown(10);
        setDuration();

        TaskManager.addTask(this, new ActionTaskTimer(combatUser, 5, 3) {
            @Override
            public boolean onTickAction(int i) {
                Location loc = LocationUtil.getLocationFromOffset(combatUser.getEntity().getEyeLocation().subtract(0, 0.4, 0),
                        combatUser.getEntity().getLocation().getDirection(), -0.2, 0, 0);
                new ArkaceA1Projectile().shoot(loc);
                playShootSound(loc);

                return true;
            }

            @Override
            public void onEnd(boolean cancelled) {
                TaskManager.addTask(ArkaceA1.this, new TaskWait(4) {
                    @Override
                    public void onEnd() {
                        setDuration(0);
                    }
                });
            }
        });
    }

    /**
     * 발사 시 효과음을 재생한다.
     *
     * @param location 발사 위치
     */
    private void playShootSound(Location location) {
        SoundUtil.play("random.gun.grenade", location, 3F, 1.5F);
        SoundUtil.play(Sound.ENTITY_SHULKER_SHOOT, location, 3F, 1.2F);
    }

    private class ArkaceA1Projectile extends Projectile {
        public ArkaceA1Projectile() {
            super(ArkaceA1.this.combatUser, ArkaceA1Info.VELOCITY, ProjectileOption.builder().trailInterval(10)
                    .condition(ArkaceA1.this.combatUser::isEnemy).build());
        }

        @Override
        public void trail(Location location) {
            ParticleUtil.play(Particle.CRIT_MAGIC, location, 1, 0, 0, 0, 0);
            ParticleUtil.playRGB(ParticleUtil.ColoredParticle.REDSTONE, location, 1,
                    0, 0, 0, 32, 250, 225);
        }

        @Override
        public void onHit(Location location) {
            explode(location);
        }

        @Override
        public boolean onHitBlock(Location location, Vector direction, Block hitBlock) {
            return false;
        }

        @Override
        public boolean onHitEntity(Location location, Vector direction, Damageable target, boolean isCrit) {
            target.damage(this, ArkaceA1Info.DAMAGE_DIRECT, DamageType.NORMAL, false, true);
            return false;
        }

        private void explode(Location location) {
            CombatEntity[] targets = CombatUtil.getNearEnemies(combatUser, location, ArkaceA1Info.RADIUS,
                    combatEntity -> combatEntity instanceof Damageable && combatEntity.canPass(location), true);
            for (CombatEntity target : targets) {
                ((Damageable) target).damage(combatUser, ArkaceA1Info.DAMAGE_EXPLODE, DamageType.NORMAL, false, true);
            }

            playExplodeEffect(location);
        }

        private void playExplodeEffect(Location location) {
            SoundUtil.play(Sound.ENTITY_FIREWORK_LARGE_BLAST, location, 4F, 0.8F);
            SoundUtil.play(Sound.ENTITY_GENERIC_EXPLODE, location, 4F, 1.4F);
            SoundUtil.play("random.gun_reverb2", location, 6F, 0.9F);
            ParticleUtil.playRGB(ParticleUtil.ColoredParticle.REDSTONE, location, 200,
                    2.5F, 2.5F, 2.5F, 32, 250, 225);
            ParticleUtil.play(Particle.EXPLOSION_NORMAL, location, 40, 0.2F, 0.2F, 0.2F, 0.2F);
        }
    }
}
