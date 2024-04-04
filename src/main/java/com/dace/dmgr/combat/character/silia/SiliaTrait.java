package com.dace.dmgr.combat.character.silia;

import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.DamageType;
import com.dace.dmgr.combat.character.silia.action.SiliaT1Info;
import com.dace.dmgr.combat.character.silia.action.SiliaT2Info;
import com.dace.dmgr.combat.entity.*;
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
import java.util.Set;

/**
 * 전투원 - 실리아 특성 클래스.
 */
@UtilityClass
public final class SiliaTrait {
    /**
     * 공격의 백어택(치명타) 여부를 확인한다.
     *
     * @param direction 공격 방향
     * @param victim    피격자
     * @return 백어택 여부
     */
    public static boolean isBackAttack(@NonNull Vector direction, @NonNull Damageable victim) {
        Vector dir = direction.clone().normalize().setY(0).normalize();
        Location vloc = victim.getEntity().getLocation();
        vloc.setPitch(0);
        Vector vdir = vloc.getDirection();

        return victim instanceof Living && dir.distance(vdir) < 0.6;
    }

    /**
     * 실리아의 일격을 사용한다.
     *
     * @param combatUser 대상 플레이어
     * @param isOpposite 반대 방향 여부
     */
    public static void strike(@NonNull CombatUser combatUser, boolean isOpposite) {
        combatUser.setGlobalCooldown(6);
        combatUser.getWeapon().setVisible(false);
        combatUser.playMeleeAttackAnimation(-2, 6, isOpposite);

        Set<CombatEntity> targets = new HashSet<>();

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

    private class SiliaWeaponStrikeAttack extends Hitscan {
        private final CombatUser combatUser;
        private final Set<CombatEntity> targets;

        public SiliaWeaponStrikeAttack(CombatUser combatUser, Set<CombatEntity> targets) {
            super(combatUser, HitscanOption.builder().trailInterval(5).size(0.5).maxDistance(SiliaT2Info.DISTANCE)
                    .condition(combatUser::isEnemy).build());

            this.combatUser = combatUser;
            this.targets = targets;
        }

        @Override
        protected void trail(@NonNull Location location, @NonNull Vector direction) {
            if (location.distance(combatUser.getEntity().getEyeLocation()) <= 1)
                return;

            Location trailLoc = LocationUtil.getLocationFromOffset(location, 0, -0.3, 0);
            ParticleUtil.playRGB(ParticleUtil.ColoredParticle.REDSTONE, trailLoc, 8, 0.15, 0.15, 0.15,
                    255, 255, 255);
        }

        @Override
        protected void onHit(@NonNull Location location) {
            ParticleUtil.play(Particle.EXPLOSION_NORMAL, location, 3, 0.05, 0.05, 0.05, 0.05);
        }

        @Override
        protected boolean onHitBlock(@NonNull Location location, @NonNull Vector velocity, @NonNull Block hitBlock) {
            ParticleUtil.playBlockHitEffect(location, hitBlock, 1.5);
            SoundUtil.playBlockHitSound(location, hitBlock, 1);

            return false;
        }

        @Override
        protected boolean onHitEntity(@NonNull Location location, @NonNull Vector velocity, @NonNull Damageable target, boolean isCrit) {
            if (targets.add(target)) {
                target.getDamageModule().damage(combatUser, SiliaT2Info.DAMAGE, DamageType.NORMAL, location,
                        SiliaTrait.isBackAttack(velocity, target) ? SiliaT1Info.CRIT_MULTIPLIER : 1, true);
                target.getKnockbackModule().knockback(VectorUtil.getRollAxis(combatUser.getEntity().getLocation()));

                ParticleUtil.play(Particle.CRIT, location, 40, 0, 0, 0, 0.4);
                SoundUtil.playNamedSound(NamedSound.COMBAT_SILIA_WEAPON_HIT_ENTITY, location);
            }

            return !(target instanceof Barrier);
        }

        @Override
        protected void onDestroy(@NonNull Location location) {
            Location trailLoc = LocationUtil.getLocationFromOffset(location, 0, -0.3, 0);
            ParticleUtil.play(Particle.CRIT, trailLoc, 15, 0.08, 0.08, 0.08, 0.08);
        }
    }
}
