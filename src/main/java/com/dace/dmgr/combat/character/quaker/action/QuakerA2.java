package com.dace.dmgr.combat.character.quaker.action;

import com.dace.dmgr.DMGR;
import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.DamageType;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.ActiveSkill;
import com.dace.dmgr.combat.entity.*;
import com.dace.dmgr.combat.entity.statuseffect.StatusEffectType;
import com.dace.dmgr.combat.interaction.Hitscan;
import com.dace.dmgr.combat.interaction.HitscanOption;
import com.dace.dmgr.combat.interaction.Projectile;
import com.dace.dmgr.combat.interaction.ProjectileOption;
import com.dace.dmgr.util.LocationUtil;
import com.dace.dmgr.util.ParticleUtil;
import com.dace.dmgr.util.SoundUtil;
import com.dace.dmgr.util.VectorUtil;
import com.dace.dmgr.util.task.DelayTask;
import com.dace.dmgr.util.task.IntervalTask;
import com.dace.dmgr.util.task.TaskUtil;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;

@Getter
public final class QuakerA2 extends ActiveSkill {
    public QuakerA2(@NonNull CombatUser combatUser) {
        super(2, combatUser, QuakerA2Info.getInstance(), 1);
    }

    @Override
    @NonNull
    public ActionKey @NonNull [] getDefaultActionKeys() {
        return new ActionKey[]{ActionKey.SLOT_2};
    }

    @Override
    public long getDefaultCooldown() {
        return QuakerA2Info.COOLDOWN;
    }

    @Override
    public long getDefaultDuration() {
        return -1;
    }

    @Override
    public boolean canUse() {
        return super.canUse() && isDurationFinished() && combatUser.getSkill(QuakerA1Info.getInstance()).isDurationFinished();
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        combatUser.getWeapon().onCancelled();
        combatUser.setGlobalCooldown(-1);
        setDuration();
        combatUser.getMoveModule().getSpeedStatus().addModifier("QuakerA2", -100);
        combatUser.getWeapon().displayDurability(QuakerWeaponInfo.RESOURCE.USE);

        int delay = 0;
        for (int i = 0; i < 12; i++) {
            final int index = i;

            if (i < 2)
                delay += 1;
            else if (i < 4)
                delay += 2;
            else if (i < 10)
                delay += 1;

            TaskUtil.addTask(taskRunner, new DelayTask(() -> {
                Location loc = combatUser.getEntity().getEyeLocation();
                loc.setPitch(0);
                Vector vector = VectorUtil.getYawAxis(loc).multiply(-1);
                Vector axis = VectorUtil.getPitchAxis(loc);

                Vector vec = VectorUtil.getRotatedVector(vector, axis, (index < 2 ? -13 : -30 + index * 17));
                new QuakerA2Effect().shoot(loc, vec);

                if (index % 2 == 0)
                    playUseSound(loc.add(vec));
                if (index == 9) {
                    onCancelled();
                    onReady();
                }
            }, delay));
        }
    }

    @Override
    public void onCancelled() {
        super.onCancelled();
        setDuration(0);
        combatUser.resetGlobalCooldown();
        combatUser.setGlobalCooldown(20);
        combatUser.getMoveModule().getSpeedStatus().removeModifier("QuakerA2");
        combatUser.getWeapon().displayDurability(QuakerWeaponInfo.RESOURCE.DEFAULT);
    }

    /**
     * 사용 시 효과음을 재생한다.
     *
     * @param location 사용 위치
     */
    private void playUseSound(Location location) {
        SoundUtil.play(Sound.ENTITY_IRONGOLEM_ATTACK, location, 1, 0.5);
        SoundUtil.play("random.gun2.shovel_leftclick", location, 1, 0.6, 0.1);
    }

    /**
     * 시전 완료 시 실행할 작업.
     */
    private void onReady() {
        playReadySound(combatUser.getEntity().getLocation());
        Set<CombatEntity> targets = new HashSet<>();

        for (int i = 0; i < 7; i++) {
            Location loc = combatUser.getEntity().getLocation().add(0, 0.1, 0);
            loc.setPitch(0);
            Vector vector = VectorUtil.getPitchAxis(loc);
            Vector axis = VectorUtil.getYawAxis(loc);

            Vector vec = VectorUtil.getRotatedVector(vector, axis, 90 + 9 * (i - 3));
            new QuakerA2Projectile(targets).shoot(loc, vec);
        }
        TaskUtil.addTask(taskRunner, new IntervalTask(i -> {
            CombatUtil.setYawAndPitch(combatUser.getEntity(), (DMGR.getRandom().nextDouble() - DMGR.getRandom().nextDouble()) * 7,
                    (DMGR.getRandom().nextDouble() - DMGR.getRandom().nextDouble()) * 6);
            return true;
        }, 1, 5));
    }

    /**
     * 시전 완료 시 효과음을 재생한다.
     *
     * @param location 사용 위치
     */
    private void playReadySound(Location location) {
        SoundUtil.play(Sound.ENTITY_IRONGOLEM_HURT, location, 3, 0.5);
        SoundUtil.play(Sound.ITEM_TOTEM_USE, location, 3, 1.6);
        SoundUtil.play(Sound.ENTITY_PLAYER_ATTACK_CRIT, location, 3, 0.6);
        SoundUtil.play(Sound.ENTITY_PLAYER_ATTACK_CRIT, location, 3, 0.7);
    }

    private class QuakerA2Effect extends Hitscan {
        public QuakerA2Effect() {
            super(combatUser, HitscanOption.builder().trailInterval(6).size(0.5).maxDistance(QuakerWeaponInfo.DISTANCE)
                    .condition(combatUser::isEnemy).build());
        }

        @Override
        protected void trail(@NonNull Location location) {
            if (location.distance(combatUser.getEntity().getEyeLocation()) <= 1)
                return;

            Location trailLoc = LocationUtil.getLocationFromOffset(location, 0, -0.3, 0);
            ParticleUtil.playRGB(ParticleUtil.ColoredParticle.REDSTONE, trailLoc, 12, 0.3, 0.3, 0.3,
                    200, 200, 200);
        }

        @Override
        protected boolean onHitBlock(@NonNull Location location, @NonNull Vector direction, @NonNull Block hitBlock) {
            ParticleUtil.playBlock(ParticleUtil.BlockParticle.BLOCK_DUST, hitBlock.getType(), hitBlock.getData(), location,
                    7, 0.08, 0.08, 0.08, 0.1);
            ParticleUtil.play(Particle.TOWN_AURA, location, 60, 0.1, 0.1, 0.1, 0);

            return false;
        }

        @Override
        protected boolean onHitEntity(@NonNull Location location, @NonNull Vector direction, @NonNull Damageable target, boolean isCrit) {
            return true;
        }

        @Override
        protected void onDestroy(@NonNull Location location) {
            Location trailLoc = LocationUtil.getLocationFromOffset(location, 0, -0.3, 0);
            ParticleUtil.play(Particle.CRIT, trailLoc, 30, 0.15, 0.15, 0.15, 0.05);
        }
    }

    private class QuakerA2Projectile extends Projectile {
        private final Set<CombatEntity> targets;

        private QuakerA2Projectile(Set<CombatEntity> targets) {
            super(QuakerA2.this.combatUser, QuakerA2Info.VELOCITY, ProjectileOption.builder().trailInterval(12).size(0.5)
                    .maxDistance(QuakerA2Info.DISTANCE).isOnGround(true).condition(QuakerA2.this.combatUser::isEnemy).build());

            this.targets = targets;
        }

        @Override
        protected void trail(@NonNull Location location) {
            Block floor = location.clone().subtract(0, 0.5, 0).getBlock();
            ParticleUtil.playBlock(ParticleUtil.BlockParticle.BLOCK_DUST, floor.getType(), floor.getData(), location,
                    10, 0.2, 0.05, 0.2, 0.2);
            ParticleUtil.play(Particle.TOWN_AURA, location, 60, 0.2, 0.05, 0.2, 0);
            ParticleUtil.play(Particle.CRIT, location, 30, 0.2, 0.05, 0.2, 0.2);
        }

        @Override
        protected boolean onHitBlock(@NonNull Location location, @NonNull Vector direction, @NonNull Block hitBlock) {
            return false;
        }

        @Override
        protected boolean onHitEntity(@NonNull Location location, @NonNull Vector direction, @NonNull Damageable target, boolean isCrit) {
            if (targets.add(target)) {
                target.getDamageModule().damage(combatUser, QuakerA2Info.DAMAGE, DamageType.NORMAL, false, true);
                target.applyStatusEffect(StatusEffectType.STUN, QuakerA2Info.STUN_DURATION);
                if (target instanceof Movable) {
                    ((Movable) target).getMoveModule().getSpeedStatus().addModifier("QuakerA2", -QuakerA2Info.SLOW);
                    TaskUtil.addTask(combatUser, new DelayTask(() ->
                            ((Movable) target).getMoveModule().getSpeedStatus().removeModifier("QuakerA2"), QuakerA2Info.SLOW_DURATION));
                }

                ParticleUtil.play(Particle.CRIT, location, 40, 0, 0, 0, 0.4);
                SoundUtil.play(Sound.ENTITY_PLAYER_ATTACK_STRONG, location, 1, 0.7, 0.05);
                SoundUtil.play(Sound.ENTITY_PLAYER_ATTACK_CRIT, location, 1, 1, 0.1);
            }

            return !(target instanceof Barrier);
        }
    }
}