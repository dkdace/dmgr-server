package com.dace.dmgr.combat.character.quaker.action;

import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.DamageType;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.weapon.AbstractWeapon;
import com.dace.dmgr.combat.entity.Barrier;
import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.interaction.Hitscan;
import com.dace.dmgr.combat.interaction.HitscanOption;
import com.dace.dmgr.util.LocationUtil;
import com.dace.dmgr.util.ParticleUtil;
import com.dace.dmgr.util.SoundUtil;
import com.dace.dmgr.util.VectorUtil;
import com.dace.dmgr.util.task.DelayTask;
import com.dace.dmgr.util.task.TaskUtil;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;

public final class QuakerWeapon extends AbstractWeapon {
    /** 휘두르는 방향의 시계 방향 여부 */
    private boolean isClockwise = true;

    public QuakerWeapon(@NonNull CombatUser combatUser) {
        super(combatUser, QuakerWeaponInfo.getInstance());
    }

    @Override
    @NonNull
    public ActionKey @NonNull [] getDefaultActionKeys() {
        return new ActionKey[]{ActionKey.LEFT_CLICK};
    }

    @Override
    public long getDefaultCooldown() {
        return QuakerWeaponInfo.COOLDOWN;
    }

    @Override
    public boolean canUse() {
        return super.canUse() && combatUser.getSkill(QuakerA1Info.getInstance()).isDurationFinished();
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        combatUser.setGlobalCooldown(8);
        setCooldown();
        setVisible(false);
        combatUser.playMeleeAttackAnimation(-10, 15, isClockwise);

        TaskUtil.addTask(taskRunner, new DelayTask(() -> {
            isClockwise = !isClockwise;
            Set<CombatEntity> targets = new HashSet<>();

            int delay = 0;
            for (int i = 0; i < 8; i++) {
                final int index = i;

                switch (i) {
                    case 1:
                        delay += 2;
                        break;
                    case 2:
                    case 4:
                    case 6:
                    case 7:
                        delay += 1;
                        break;
                }

                TaskUtil.addTask(taskRunner, new DelayTask(() -> {
                    Location loc = combatUser.getEntity().getEyeLocation();
                    Vector vector = VectorUtil.getPitchAxis(loc);
                    Vector axis = VectorUtil.getYawAxis(loc);

                    Vector vec = VectorUtil.getRotatedVector(vector, axis, (isClockwise ? (index + 1) * 20 : 180 - (index + 1) * 20));
                    new QuakerWeaponAttack(targets).shoot(loc, vec);
                    CombatUtil.addYawAndPitch(combatUser.getEntity(), (isClockwise ? 1 : -1) * 0.8, 0.1);

                    if (index % 2 == 0)
                        playUseSound(loc.add(vec));
                    if (index == 7) {
                        CombatUtil.addYawAndPitch(combatUser.getEntity(), isClockwise ? -1 : 1, -0.7);
                        TaskUtil.addTask(taskRunner, new DelayTask(this::onCancelled, 4));
                    }
                }, delay));
            }
        }, 2));
    }

    @Override
    public void onCancelled() {
        super.onCancelled();
        setVisible(true);
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

    private class QuakerWeaponAttack extends Hitscan {
        private final Set<CombatEntity> targets;

        public QuakerWeaponAttack(Set<CombatEntity> targets) {
            super(combatUser, HitscanOption.builder().trailInterval(6).size(0.5).maxDistance(QuakerWeaponInfo.DISTANCE)
                    .condition(combatUser::isEnemy).build());

            this.targets = targets;
        }

        @Override
        protected void trail(@NonNull Location location, @NonNull Vector direction) {
            if (location.distance(combatUser.getEntity().getEyeLocation()) <= 1)
                return;

            Location trailLoc = LocationUtil.getLocationFromOffset(location, 0, -0.3, 0);
            ParticleUtil.playRGB(ParticleUtil.ColoredParticle.REDSTONE, trailLoc, 12, 0.3, 0.3, 0.3,
                    200, 200, 200);
        }

        @Override
        protected void onHit(@NonNull Location location) {
            SoundUtil.play(Sound.ENTITY_PLAYER_ATTACK_STRONG, location, 0.8, 0.75, 0.1);
            SoundUtil.play(Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, location, 0.6, 0.85, 0.1);
        }

        @Override
        protected boolean onHitBlock(@NonNull Location location, @NonNull Vector velocity, @NonNull Block hitBlock) {
            ParticleUtil.playBlock(ParticleUtil.BlockParticle.BLOCK_DUST, hitBlock.getType(), hitBlock.getData(), location,
                    7, 0.08, 0.08, 0.08, 0.1);
            ParticleUtil.play(Particle.TOWN_AURA, location, 60, 0.1, 0.1, 0.1, 0);
            SoundUtil.playBlockHitSound(location, hitBlock);

            return false;
        }

        @Override
        protected boolean onHitEntity(@NonNull Location location, @NonNull Vector velocity, @NonNull Damageable target, boolean isCrit) {
            if (targets.add(target)) {
                target.getDamageModule().damage(combatUser, QuakerWeaponInfo.DAMAGE, DamageType.NORMAL, location, false, true);
                target.getKnockbackModule().knockback(VectorUtil.getPitchAxis(combatUser.getEntity().getLocation()).multiply(isClockwise ? -0.3 : 0.3));
                ParticleUtil.play(Particle.CRIT, location, 20, 0, 0, 0, 0.4);
                SoundUtil.play(Sound.ENTITY_PLAYER_ATTACK_STRONG, location, 1, 0.9, 0.05);
                SoundUtil.play(Sound.ENTITY_PLAYER_ATTACK_CRIT, location, 1, 1.2, 0.1);
            }

            return !(target instanceof Barrier);
        }

        @Override
        protected void onDestroy(@NonNull Location location) {
            Location trailLoc = LocationUtil.getLocationFromOffset(location, 0, -0.3, 0);
            ParticleUtil.play(Particle.CRIT, trailLoc, 30, 0.15, 0.15, 0.15, 0.05);
        }
    }
}
