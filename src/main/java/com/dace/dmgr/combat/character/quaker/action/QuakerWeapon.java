package com.dace.dmgr.combat.character.quaker.action;

import com.dace.dmgr.combat.CombatEffectUtil;
import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.weapon.AbstractWeapon;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.interaction.DamageType;
import com.dace.dmgr.combat.interaction.Hitscan;
import com.dace.dmgr.combat.interaction.HitscanOption;
import com.dace.dmgr.util.*;
import com.dace.dmgr.util.task.DelayTask;
import com.dace.dmgr.util.task.TaskUtil;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

import java.util.HashSet;

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
    public boolean canUse(@NonNull ActionKey actionKey) {
        return super.canUse(actionKey) && combatUser.getSkill(QuakerA1Info.getInstance()).isDurationFinished();
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        setCooldown();
        setVisible(false);
        combatUser.setGlobalCooldown(QuakerWeaponInfo.GLOBAL_COOLDOWN);
        combatUser.playMeleeAttackAnimation(-10, 15, isClockwise);

        TaskUtil.addTask(taskRunner, new DelayTask(() -> {
            isClockwise = !isClockwise;
            HashSet<Damageable> targets = new HashSet<>();

            int delay = 0;
            for (int i = 0; i < 8; i++) {
                int index = i;

                if (i == 1)
                    delay += 2;
                else if (i == 2 || i == 4 || i == 6 || i == 7)
                    delay += 1;

                TaskUtil.addTask(taskRunner, new DelayTask(() -> {
                    Location loc = combatUser.getEntity().getEyeLocation();
                    Vector vector = VectorUtil.getPitchAxis(loc);
                    Vector axis = VectorUtil.getYawAxis(loc);

                    Vector vec = VectorUtil.getRotatedVector(vector, axis, (isClockwise ? (index + 1) * 20 : 180 - (index + 1) * 20));
                    new QuakerWeaponAttack(targets).shoot(loc, vec);

                    CombatUtil.addYawAndPitch(combatUser.getEntity(), (isClockwise ? 0.8 : -0.8), 0.1);
                    if (index % 2 == 0)
                        SoundUtil.playNamedSound(NamedSound.COMBAT_QUAKER_WEAPON_USE, loc.add(vec));
                    if (index == 7) {
                        CombatUtil.addYawAndPitch(combatUser.getEntity(), (isClockwise ? -1 : 1), -0.7);
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

    private final class QuakerWeaponAttack extends Hitscan {
        private final HashSet<Damageable> targets;

        private QuakerWeaponAttack(HashSet<Damageable> targets) {
            super(combatUser, HitscanOption.builder().trailInterval(6).size(QuakerWeaponInfo.SIZE).maxDistance(QuakerWeaponInfo.DISTANCE)
                    .condition(combatUser::isEnemy).build());
            this.targets = targets;
        }

        @Override
        protected void onTrailInterval() {
            if (getLocation().distance(combatUser.getEntity().getEyeLocation()) <= 1)
                return;

            Location loc = LocationUtil.getLocationFromOffset(getLocation(), 0, -0.3, 0);
            ParticleUtil.playRGB(ParticleUtil.ColoredParticle.REDSTONE, loc, 12, 0.3, 0.3, 0.3,
                    200, 200, 200);
        }

        @Override
        protected void onHit() {
            SoundUtil.playNamedSound(NamedSound.COMBAT_QUAKER_WEAPON_HIT, getLocation());
        }

        @Override
        protected boolean onHitBlock(@NonNull Block hitBlock) {
            CombatEffectUtil.playBlockHitEffect(getLocation(), hitBlock, 2);
            CombatEffectUtil.playBlockHitSound(getLocation(), hitBlock, 1);

            return false;
        }

        @Override
        protected boolean onHitEntity(@NonNull Damageable target, boolean isCrit) {
            if (targets.add(target)) {
                if (target.getDamageModule().damage(combatUser, QuakerWeaponInfo.DAMAGE, DamageType.NORMAL, getLocation(), false, true))
                    target.getKnockbackModule().knockback(VectorUtil.getPitchAxis(combatUser.getEntity().getLocation()).multiply(isClockwise ?
                            -QuakerWeaponInfo.KNOCKBACK : QuakerWeaponInfo.KNOCKBACK));

                ParticleUtil.play(Particle.CRIT, getLocation(), 20, 0, 0, 0, 0.4);
                SoundUtil.playNamedSound(NamedSound.COMBAT_QUAKER_WEAPON_HIT_ENTITY, getLocation());
            }

            return true;
        }

        @Override
        protected void onDestroy() {
            Location loc = LocationUtil.getLocationFromOffset(getLocation(), 0, -0.3, 0);
            ParticleUtil.play(Particle.CRIT, loc, 30, 0.15, 0.15, 0.15, 0.05);
        }
    }
}
