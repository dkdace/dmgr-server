package com.dace.dmgr.combat.character.silia.action;

import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.temporal.Barrier;
import com.dace.dmgr.combat.interaction.DamageType;
import com.dace.dmgr.combat.interaction.Hitscan;
import com.dace.dmgr.combat.interaction.HitscanOption;
import com.dace.dmgr.util.*;
import com.dace.dmgr.util.task.DelayTask;
import com.dace.dmgr.util.task.TaskUtil;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

import java.util.HashSet;

@UtilityClass
public final class SiliaT2 {
    /**
     * 실리아의 일격을 사용한다.
     *
     * @param combatUser 대상 플레이어
     * @param isOpposite 반대 방향 여부
     */
    static void strike(@NonNull CombatUser combatUser, boolean isOpposite) {
        combatUser.setGlobalCooldown(SiliaT2Info.GLOBAL_COOLDOWN);
        combatUser.getWeapon().setVisible(false);
        combatUser.playMeleeAttackAnimation(-2, 6, isOpposite);

        HashSet<CombatEntity> targets = new HashSet<>();

        int delay = 0;
        for (int i = 0; i < 8; i++) {
            final int index = i;

            switch (i) {
                case 1:
                case 2:
                case 6:
                case 7:
                    delay += 1;
                    break;
            }

            TaskUtil.addTask(combatUser.getWeapon().getTaskRunner(), new DelayTask(() -> {
                Location loc = combatUser.getEntity().getEyeLocation();
                Vector vector = VectorUtil.getPitchAxis(loc);
                Vector axis = VectorUtil.getYawAxis(loc);

                Vector vec = VectorUtil.getRotatedVector(vector, VectorUtil.getRollAxis(loc), isOpposite ? -30 : 30);
                axis = VectorUtil.getRotatedVector(axis, VectorUtil.getRollAxis(loc), isOpposite ? -30 : 30);

                vec = VectorUtil.getRotatedVector(vec, axis, (isOpposite ? 90 - 16 * (index - 3.5) : 90 + 16 * (index - 3.5)));
                new SiliaWeaponStrikeAttack(combatUser, targets).shoot(loc, vec);

                CombatUtil.addYawAndPitch(combatUser.getEntity(), (isOpposite ? -0.5 : 0.5), 0.15);
                if (index < 3)
                    SoundUtil.playNamedSound(NamedSound.COMBAT_SILIA_T2_USE, loc.add(vec), 1, index * 0.12);
                if (index == 7) {
                    CombatUtil.addYawAndPitch(combatUser.getEntity(), isOpposite ? 0.7 : -0.7, -0.85);
                    combatUser.getWeapon().onCancelled();
                }
            }, delay));
        }
    }

    private final class SiliaWeaponStrikeAttack extends Hitscan {
        private final CombatUser combatUser;
        private final HashSet<CombatEntity> targets;

        private SiliaWeaponStrikeAttack(CombatUser combatUser, HashSet<CombatEntity> targets) {
            super(combatUser, HitscanOption.builder().trailInterval(5).size(SiliaT2Info.SIZE).maxDistance(SiliaT2Info.DISTANCE)
                    .condition(combatUser::isEnemy).build());

            this.combatUser = combatUser;
            this.targets = targets;
        }

        @Override
        protected void trail() {
            if (location.distance(combatUser.getEntity().getEyeLocation()) <= 1)
                return;

            Location trailLoc = LocationUtil.getLocationFromOffset(location, 0, -0.3, 0);
            ParticleUtil.playRGB(ParticleUtil.ColoredParticle.REDSTONE, trailLoc, 8, 0.15, 0.15, 0.15,
                    255, 255, 255);
        }

        @Override
        protected void onHit() {
            ParticleUtil.play(Particle.EXPLOSION_NORMAL, location, 3, 0.05, 0.05, 0.05, 0.05);
        }

        @Override
        protected boolean onHitBlock(@NonNull Block hitBlock) {
            CombatUtil.playBlockHitEffect(location, hitBlock, 1.5);
            CombatUtil.playBlockHitSound(location, hitBlock, 1);

            return false;
        }

        @Override
        protected boolean onHitEntity(@NonNull Damageable target, boolean isCrit) {
            if (targets.add(target)) {
                target.getDamageModule().damage(combatUser, SiliaT2Info.DAMAGE, DamageType.NORMAL, location,
                        SiliaT1.isBackAttack(velocity, target) ? SiliaT1Info.CRIT_MULTIPLIER : 1, true);
                target.getKnockbackModule().knockback(VectorUtil.getRollAxis(combatUser.getEntity().getLocation()).multiply(SiliaT2Info.KNOCKBACK));

                ParticleUtil.play(Particle.CRIT, location, 40, 0, 0, 0, 0.4);
                SoundUtil.playNamedSound(NamedSound.COMBAT_SILIA_WEAPON_HIT_ENTITY, location);
            }

            return !(target instanceof Barrier);
        }

        @Override
        protected void onDestroy() {
            Location loc = LocationUtil.getLocationFromOffset(location, 0, -0.3, 0);
            ParticleUtil.play(Particle.CRIT, loc, 15, 0.08, 0.08, 0.08, 0.08);
        }
    }
}
