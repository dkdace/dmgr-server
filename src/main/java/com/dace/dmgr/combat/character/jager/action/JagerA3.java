package com.dace.dmgr.combat.character.jager.action;

import com.dace.dmgr.combat.*;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.ActiveSkill;
import com.dace.dmgr.combat.character.jager.JagerTrait;
import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.Property;
import com.dace.dmgr.combat.entity.statuseffect.Snare;
import com.dace.dmgr.system.Cooldown;
import com.dace.dmgr.system.CooldownManager;
import com.dace.dmgr.system.task.TaskTimer;
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
        return super.canUse() && !((JagerA1) combatUser.getSkill(JagerA1Info.getInstance())).isConfirming();
    }

    @Override
    public void onUse(ActionKey actionKey) {
        if (((JagerWeaponL) combatUser.getWeapon()).isAiming()) {
            ((JagerWeaponL) combatUser.getWeapon()).aim();
            ((JagerWeaponL) combatUser.getWeapon()).swap();
        }

        if (isDurationFinished()) {
            combatUser.setGlobalCooldown((int) JagerA3Info.READY_DURATION);
            Location location = combatUser.getEntity().getLocation();
            SoundUtil.play(Sound.ENTITY_CAT_PURREOW, location, 0.5F, 1.6F);
            setDuration();

            new TaskTimer(1, JagerA3Info.READY_DURATION) {
                @Override
                public boolean run(int i) {
                    return true;
                }

                @Override
                public void onEnd(boolean cancelled) {
                    if (cancelled) {
                        setDuration(0);
                        return;
                    }

                    CooldownManager.setCooldown(combatUser, Cooldown.JAGER_EXPLODE_DURATION);
                    Location location = combatUser.getEntity().getLocation();
                    SoundUtil.play(Sound.ITEM_FLINTANDSTEEL_USE, location, 0.8F, 1.2F);
                    SoundUtil.play("new.block.chain.place", location, 0.8F, 1.2F);

                    new TaskTimer(1, JagerA3Info.EXPLODE_DURATION) {
                        @Override
                        public boolean run(int i) {
                            if (isDurationFinished())
                                return false;

                            Location loc = LocationUtil.getLocationFromOffset(combatUser.getEntity().getEyeLocation().subtract(0, 0.4, 0),
                                    combatUser.getEntity().getLocation().getDirection(), 0.2, 0, 0.3);
                            ParticleUtil.playRGB(ParticleUtil.ColoredParticle.REDSTONE, loc, 3,
                                    0.1F, 0.1F, 0.1F, 120, 220, 240);

                            return true;
                        }

                        @Override
                        public void onEnd(boolean cancelled) {
                            if (!cancelled) {
                                setDuration(0);

                                Location loc = LocationUtil.getLocationFromOffset(combatUser.getEntity().getEyeLocation().subtract(0, 0.4, 0),
                                        combatUser.getEntity().getLocation().getDirection(), 0.2, 0, 0.3);
                                explode(combatUser, loc, null);
                            }
                        }
                    };
                }
            };
        } else {
            setDuration(0);
            Location location = LocationUtil.getLocationFromOffset(combatUser.getEntity().getEyeLocation().subtract(0, 0.4, 0),
                    combatUser.getEntity().getLocation().getDirection(), 0.2, 0, 0);
            SoundUtil.play(Sound.ENTITY_WITCH_THROW, location, 0.8F, 0.8F);

            new BouncingProjectile(combatUser, JagerA3Info.VELOCITY, -1, ProjectileOption.builder().trailInterval(8)
                    .duration(CooldownManager.getCooldown(combatUser, Cooldown.JAGER_EXPLODE_DURATION)).hasGravity(true)
                    .condition(combatUser::isEnemy).build(), BouncingProjectileOption.builder().bounceVelocityMultiplier(0.35F).build()) {
                @Override
                public void trail(Location location) {
                    ParticleUtil.playRGB(ParticleUtil.ColoredParticle.REDSTONE, location, 3,
                            0.1F, 0.1F, 0.1F, 120, 220, 240);
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
                    explode(combatUser, location, this);
                }
            }.shoot(location);
        }
    }

    private void explode(CombatUser combatUser, Location location, Projectile projectile) {
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
                target.applyStatusEffect(new Snare() {
                    @Override
                    public void onTick(CombatEntity combatEntity, int i) {
                        if (combatEntity instanceof CombatUser)
                            ((CombatUser) combatEntity).getEntity().sendTitle("§c§l얼어붙음!", "", 0, 2, 10);

                        ParticleUtil.playRGB(ParticleUtil.ColoredParticle.REDSTONE,
                                combatEntity.getEntity().getLocation().add(0, combatEntity.getEntity().getHeight() / 2, 0), 5,
                                0.4F, 0.8F, 0.4F, 120, 220, 240);
                    }
                }, JagerA3Info.SNARE_DURATION);
            }
        }
    }
}
