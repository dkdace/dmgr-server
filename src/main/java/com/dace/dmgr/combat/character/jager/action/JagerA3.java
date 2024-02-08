package com.dace.dmgr.combat.character.jager.action;

import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.DamageType;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.ActiveSkill;
import com.dace.dmgr.combat.character.jager.JagerTrait;
import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.Property;
import com.dace.dmgr.combat.entity.statuseffect.StatusEffectType;
import com.dace.dmgr.combat.interaction.BouncingProjectile;
import com.dace.dmgr.combat.interaction.BouncingProjectileOption;
import com.dace.dmgr.combat.interaction.Projectile;
import com.dace.dmgr.combat.interaction.ProjectileOption;
import com.dace.dmgr.util.*;
import com.dace.dmgr.util.task.DelayTask;
import com.dace.dmgr.util.task.IntervalTask;
import com.dace.dmgr.util.task.TaskUtil;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

public final class JagerA3 extends ActiveSkill {
    /** 수류탄 활성화 완료 여부 */
    private boolean isEnabled = false;

    public JagerA3(CombatUser combatUser) {
        super(3, combatUser, JagerA3Info.getInstance(), 2);
    }

    @Override
    @NonNull
    public ActionKey @NonNull [] getDefaultActionKeys() {
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
    public void onUse(@NonNull ActionKey actionKey) {
        combatUser.getWeapon().onCancelled();

        if (isDurationFinished()) {
            combatUser.setGlobalCooldown((int) JagerA3Info.READY_DURATION);
            setDuration();
            playUseSound(combatUser.getEntity().getLocation());

            TaskUtil.addTask(taskRunner, new DelayTask(() -> {
                CooldownUtil.setCooldown(combatUser, Cooldown.JAGER_EXPLODE_DURATION);
                playReadySound(combatUser.getEntity().getLocation());
                isEnabled = true;

                TaskUtil.addTask(taskRunner, new IntervalTask(i -> {
                    Location loc = LocationUtil.getLocationFromOffset(combatUser.getEntity().getEyeLocation().subtract(0, 0.4, 0),
                            combatUser.getEntity().getLocation().getDirection(), 0.2, 0, 0.3);
                    playTickEffect(loc);

                    return true;
                }, isCancelled -> {
                    Location loc = LocationUtil.getLocationFromOffset(combatUser.getEntity().getEyeLocation().subtract(0, 0.4, 0),
                            combatUser.getEntity().getLocation().getDirection(), 0.2, 0, 0.3);
                    isEnabled = false;
                    onCancelled();

                    explode(loc, null);
                }, 1, JagerA3Info.EXPLODE_DURATION));
            }, JagerA3Info.READY_DURATION));
        } else {
            Location loc = LocationUtil.getLocationFromOffset(combatUser.getEntity().getEyeLocation().subtract(0, 0.4, 0),
                    combatUser.getEntity().getLocation().getDirection(), 0.2, 0, 0);
            new JagerA3Projectile().shoot(loc);

            isEnabled = false;
            onCancelled();
            playThrowSound(loc);
        }
    }

    @Override
    public void onCancelled() {
        if (!isEnabled) {
            super.onCancelled();
            setDuration(0);
        }
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
     * 시전 완료 시 효과음을 재생한다.
     *
     * @param location 사용 위치
     */
    private void playReadySound(Location location) {
        SoundUtil.play(Sound.ITEM_FLINTANDSTEEL_USE, location, 0.8, 1.2);
        SoundUtil.play("new.block.chain.place", location, 0.8, 1.2);
    }

    /**
     * 사용 중 효과를 재생한다.
     *
     * @param location 사용 위치
     */
    private void playTickEffect(Location location) {
        ParticleUtil.playRGB(ParticleUtil.ColoredParticle.REDSTONE, location, 3, 0.1, 0.1, 0.1, 120, 220, 240);
    }

    /**
     * 투척 시 효과음을 재생한다.
     *
     * @param location 사용 위치
     */
    private void playThrowSound(Location location) {
        SoundUtil.play(Sound.ENTITY_WITCH_THROW, location, 0.8, 0.8);
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
                    JagerA3Info.RADIUS / 2.0, true);
            int freeze = CombatUtil.getDistantDamage(location, target.getEntity().getLocation(), JagerA3Info.FREEZE,
                    JagerA3Info.RADIUS / 2.0, true);
            if (projectile == null)
                ((Damageable) target).getDamageModule().damage(combatUser, damage, DamageType.NORMAL, false, true);
            else
                ((Damageable) target).getDamageModule().damage(projectile, damage, DamageType.NORMAL, false, true);
            JagerTrait.addFreezeValue(target, freeze);

            if (target.getPropertyManager().getValue(Property.FREEZE) >= JagerT1Info.MAX) {
                target.applyStatusEffect(StatusEffectType.FREEZE, JagerA3Info.SNARE_DURATION);
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
        SoundUtil.play(Sound.ENTITY_FIREWORK_LARGE_BLAST, location, 4, 0.6);
        SoundUtil.play(Sound.ENTITY_GENERIC_EXPLODE, location, 4, 1.2);
        SoundUtil.play(Sound.ENTITY_ZOMBIE_VILLAGER_CURE, location, 4, 1.5);
        SoundUtil.play("random.explosion_reverb", location, 6, 1.2);
        ParticleUtil.playBlock(ParticleUtil.BlockParticle.BLOCK_DUST, Material.ICE, 0, location,
                300, 0.2, 0.2, 0.2, 0.5);
        ParticleUtil.playBlock(ParticleUtil.BlockParticle.BLOCK_DUST, Material.PACKED_ICE, 0, location,
                300, 0.2, 0.2, 0.2, 0.5);
        ParticleUtil.play(Particle.EXPLOSION_LARGE, location, 1, 0, 0, 0, 0);
        ParticleUtil.play(Particle.FIREWORKS_SPARK, location, 200, 0, 0, 0, 0.3);
    }

    private class JagerA3Projectile extends BouncingProjectile {
        public JagerA3Projectile() {
            super(JagerA3.this.combatUser, JagerA3Info.VELOCITY, -1, ProjectileOption.builder().trailInterval(8)
                    .duration(CooldownUtil.getCooldown(JagerA3.this.combatUser, Cooldown.JAGER_EXPLODE_DURATION)).hasGravity(true)
                    .condition(JagerA3.this.combatUser::isEnemy).build(), BouncingProjectileOption.builder().bounceVelocityMultiplier(0.35).build());
        }

        @Override
        public void trail(@NonNull Location location) {
            playTickEffect(location);
        }

        @Override
        public boolean onHitBlockBouncing(@NonNull Location location, @NonNull Vector direction, @NonNull Block hitBlock) {
            SoundUtil.play("random.metalhit", location, 0.1 + direction.length() * 2, 1.2, 0.1);
            SoundUtil.play(Sound.BLOCK_GLASS_BREAK, location, 0.1 + direction.length() * 2, 2);
            return false;
        }

        @Override
        public boolean onHitEntityBouncing(@NonNull Location location, @NonNull Vector direction, @NonNull Damageable target, boolean isCrit) {
            if (direction.length() > 0.05)
                target.getDamageModule().damage(this, JagerA3Info.DAMAGE_DIRECT, DamageType.NORMAL, false, true);
            return false;
        }

        @Override
        public void onDestroy(@NonNull Location location) {
            explode(location, this);
        }
    }
}
