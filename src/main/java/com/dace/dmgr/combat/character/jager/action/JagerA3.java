package com.dace.dmgr.combat.character.jager.action;

import com.dace.dmgr.combat.*;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.ActiveSkill;
import com.dace.dmgr.combat.character.jager.JagerTrait;
import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.Property;
import com.dace.dmgr.combat.entity.damageable.Damageable;
import com.dace.dmgr.combat.entity.statuseffect.Snare;
import com.dace.dmgr.system.Cooldown;
import com.dace.dmgr.system.CooldownManager;
import com.dace.dmgr.system.task.ActionTaskTimer;
import com.dace.dmgr.system.task.TaskManager;
import com.dace.dmgr.util.LocationUtil;
import com.dace.dmgr.util.ParticleUtil;
import com.dace.dmgr.util.SoundUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

public final class JagerA3 extends ActiveSkill {
    public JagerA3(CombatUser combatUser) {
        super(3, combatUser, JagerA3Info.getInstance(), 2);
    }

    @Override
    public ActionKey[] getDefaultActionKeys() {
        return new ActionKey[]{ActionKey.SLOT_3};
    }

    @Override
    public long getDefaultCooldown() {
        return JagerA3Info.COOLDOWN;
    }

    @Override
    public long getDefaultDuration() {
        return -1;
    }

    @Override
    public boolean canUse() {
        return super.canUse() && !((JagerA1) combatUser.getSkill(JagerA1Info.getInstance())).getConfirmModule().isChecking();
    }

    @Override
    public void onUse(ActionKey actionKey) {
        if (((JagerWeaponL) combatUser.getWeapon()).getAimModule().isAiming()) {
            ((JagerWeaponL) combatUser.getWeapon()).toggleAim();
            ((JagerWeaponL) combatUser.getWeapon()).swap();
        }

        if (isDurationFinished()) {
            combatUser.setGlobalCooldown((int) JagerA3Info.READY_DURATION);
            setDuration();
            playUseSound(combatUser.getEntity().getLocation());

            TaskManager.addTask(this, new ActionTaskTimer(combatUser, 1, JagerA3Info.READY_DURATION) {
                @Override
                public boolean onTickAction(int i) {
                    return true;
                }

                @Override
                public void onEnd(boolean cancelled) {
                    if (cancelled) {
                        setDuration(0);
                        return;
                    }

                    CooldownManager.setCooldown(combatUser, Cooldown.JAGER_EXPLODE_DURATION);
                    playReadySound(combatUser.getEntity().getLocation());

                    TaskManager.addTask(JagerA3.this, new ActionTaskTimer(combatUser, 1, JagerA3Info.EXPLODE_DURATION) {
                        @Override
                        public boolean onTickAction(int i) {
                            if (isDurationFinished())
                                return false;

                            Location loc = LocationUtil.getLocationFromOffset(combatUser.getEntity().getEyeLocation().subtract(0, 0.4, 0),
                                    combatUser.getEntity().getLocation().getDirection(), 0.2, 0, 0.3);
                            playTickEffect(loc);

                            return true;
                        }

                        @Override
                        public void onEnd(boolean cancelled) {
                            if (cancelled)
                                return;

                            Location loc = LocationUtil.getLocationFromOffset(combatUser.getEntity().getEyeLocation().subtract(0, 0.4, 0),
                                    combatUser.getEntity().getLocation().getDirection(), 0.2, 0, 0.3);
                            setDuration(0);

                            explode(loc, null);
                        }
                    });
                }
            });
        } else {
            Location loc = LocationUtil.getLocationFromOffset(combatUser.getEntity().getEyeLocation().subtract(0, 0.4, 0),
                    combatUser.getEntity().getLocation().getDirection(), 0.2, 0, 0);
            new JagerA3Projectile().shoot(loc);

            setDuration(0);
            playThrowSound(loc);
        }
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
     * 시전 완료 시 효과음을 재생한다.
     *
     * @param location 사용 위치
     */
    private void playReadySound(Location location) {
        SoundUtil.play(Sound.ITEM_FLINTANDSTEEL_USE, location, 0.8F, 1.2F);
        SoundUtil.play("new.block.chain.place", location, 0.8F, 1.2F);
    }

    /**
     * 사용 중 효과를 재생한다.
     *
     * @param location 사용 위치
     */
    private void playTickEffect(Location location) {
        ParticleUtil.playRGB(ParticleUtil.ColoredParticle.REDSTONE, location, 3, 0.1F, 0.1F, 0.1F, 120, 220, 240);
    }

    /**
     * 투척 시 효과음을 재생한다.
     *
     * @param location 사용 위치
     */
    private void playThrowSound(Location location) {
        SoundUtil.play(Sound.ENTITY_WITCH_THROW, location, 0.8F, 0.8F);
    }

    /**
     * 폭파 이벤트를 호출한다.
     *
     * @param location   사용 위치
     * @param projectile 투사체
     */
    private void explode(Location location, Projectile projectile) {
        CombatEntity[] targets = CombatUtil.getNearEnemies(combatUser, location, JagerA3Info.RADIUS,
                combatEntity -> combatEntity instanceof Damageable && combatEntity.canPass(location), true);
        for (CombatEntity target : targets) {
            int damage = CombatUtil.getDistantDamage(location, target.getEntity().getLocation(), JagerA3Info.DAMAGE_EXPLODE,
                    JagerA3Info.RADIUS / 2F, true);
            int freeze = CombatUtil.getDistantDamage(location, target.getEntity().getLocation(), JagerA3Info.FREEZE,
                    JagerA3Info.RADIUS / 2F, true);
            if (projectile == null)
                ((Damageable) target).damage(combatUser, damage, DamageType.NORMAL, false, true);
            else
                ((Damageable) target).damage(projectile, damage, DamageType.NORMAL, false, true);
            JagerTrait.addFreezeValue(target, freeze);

            if (target.getPropertyManager().getValue(Property.FREEZE) >= JagerT1Info.MAX) {
                target.applyStatusEffect(new Freeze(), JagerA3Info.SNARE_DURATION);
            }
        }

        playExplodeEffect(location);
    }

    /**
     * 폭파 시 효과를 재생한다.
     *
     * @param location 사용 위치
     */
    private void playExplodeEffect(Location location) {
        SoundUtil.play(Sound.ENTITY_FIREWORK_LARGE_BLAST, location, 4F, 0.6F);
        SoundUtil.play(Sound.ENTITY_GENERIC_EXPLODE, location, 4F, 1.2F);
        SoundUtil.play(Sound.ENTITY_ZOMBIE_VILLAGER_CURE, location, 4F, 1.5F);
        SoundUtil.play("random.explosion_reverb", location, 6F, 1.2F);
        ParticleUtil.playBlock(ParticleUtil.BlockParticle.BLOCK_DUST, Material.ICE, 0, location,
                300, 0.2F, 0.2F, 0.2F, 0.5F);
        ParticleUtil.playBlock(ParticleUtil.BlockParticle.BLOCK_DUST, Material.PACKED_ICE, 0, location,
                300, 0.2F, 0.2F, 0.2F, 0.5F);
        ParticleUtil.play(Particle.EXPLOSION_LARGE, location, 1, 0, 0, 0, 0);
        ParticleUtil.play(Particle.FIREWORKS_SPARK, location, 200, 0, 0, 0, 0.3F);
    }

    /**
     * 빙결 상태 효과 클래스.
     */
    private static class Freeze extends Snare {
        @Override
        public void onTick(CombatEntity combatEntity, int i) {
            if (combatEntity instanceof CombatUser)
                ((CombatUser) combatEntity).getEntity().sendTitle("§c§l얼어붙음!", "", 0, 2, 10);

            ParticleUtil.playRGB(ParticleUtil.ColoredParticle.REDSTONE,
                    combatEntity.getEntity().getLocation().add(0, combatEntity.getEntity().getHeight() / 2, 0), 5,
                    0.4F, 0.8F, 0.4F, 120, 220, 240);
        }
    }

    private class JagerA3Projectile extends BouncingProjectile {
        public JagerA3Projectile() {
            super(JagerA3.this.combatUser, JagerA3Info.VELOCITY, -1, ProjectileOption.builder().trailInterval(8)
                    .duration(CooldownManager.getCooldown(JagerA3.this.combatUser, Cooldown.JAGER_EXPLODE_DURATION)).hasGravity(true)
                    .condition(JagerA3.this.combatUser::isEnemy).build(), BouncingProjectileOption.builder().bounceVelocityMultiplier(0.35F).build());
        }

        @Override
        public void trail(Location location) {
            playTickEffect(location);
        }

        @Override
        public boolean onHitBlockBouncing(Location location, Vector direction, Block hitBlock) {
            SoundUtil.play("random.metalhit", location, (float) (0.1 + direction.length() * 2), (float) (1.2 + Math.random() * 0.1));
            SoundUtil.play(Sound.BLOCK_GLASS_BREAK, location, (float) (0.1 + direction.length() * 2), 2F);
            return false;
        }

        @Override
        public boolean onHitEntityBouncing(Location location, Vector direction, Damageable target, boolean isCrit) {
            if (direction.length() > 0.05)
                target.damage(this, JagerA3Info.DAMAGE_DIRECT, DamageType.NORMAL, false, true);
            return false;
        }

        @Override
        public void onDestroy(Location location) {
            explode(location, this);
        }
    }
}
