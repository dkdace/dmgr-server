package com.dace.dmgr.combat.combatant.magritta.action;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.CombatEffectUtil;
import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.action.ActionBarStringUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.UltimateSkill;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.DamageType;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.module.AbilityStatus;
import com.dace.dmgr.combat.entity.module.statuseffect.ValueStatusEffect;
import com.dace.dmgr.combat.interaction.Hitscan;
import com.dace.dmgr.util.LocationUtil;
import com.dace.dmgr.util.VectorUtil;
import com.dace.dmgr.util.task.DelayTask;
import com.dace.dmgr.util.task.IntervalTask;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.inventory.MainHand;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

public final class MagrittaUlt extends UltimateSkill {
    /** 수정자 */
    private static final AbilityStatus.Modifier MODIFIER = new AbilityStatus.Modifier(-MagrittaUltInfo.USE_SLOW);
    /** 활성화 완료 여부 */
    private boolean isEnabled = false;
    /** 블록 명중 횟수 */
    private int blockHitCount = 0;

    public MagrittaUlt(@NonNull CombatUser combatUser) {
        super(combatUser, MagrittaUltInfo.getInstance(), MagrittaUltInfo.DURATION, MagrittaUltInfo.COST);
    }

    @Override
    @Nullable
    public String getActionBarString() {
        return (isDurationFinished() || !isEnabled) ? null : ActionBarStringUtil.getDurationBar(this);
    }

    @Override
    public boolean canUse(@NonNull ActionKey actionKey) {
        return super.canUse(actionKey) && isDurationFinished() && combatUser.getSkill(MagrittaA2Info.getInstance()).isDurationFinished();
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        super.onUse(actionKey);

        setDuration();
        combatUser.getWeapon().onCancelled();
        combatUser.setGlobalCooldown(MagrittaUltInfo.READY_DURATION);
        combatUser.getMoveModule().getSpeedStatus().addModifier(MODIFIER);
        ((MagrittaWeapon) combatUser.getWeapon()).getReloadModule().resetRemainingAmmo();

        MagrittaUltInfo.SOUND.USE.play(combatUser.getLocation());

        addActionTask(new DelayTask(this::onReady, MagrittaUltInfo.READY_DURATION.toTicks()));
    }

    @Override
    public boolean isCancellable() {
        return !isDurationFinished();
    }

    @Override
    public void onCancelled() {
        super.onCancelled();

        setDuration(Timespan.ZERO);
        isEnabled = false;
        combatUser.getMoveModule().getSpeedStatus().removeModifier(MODIFIER);
    }

    /**
     * 시전 완료 시 실행할 작업.
     */
    private void onReady() {
        setDuration();
        isEnabled = true;

        addActionTask(new IntervalTask(i -> {
            Location loc = combatUser.getLocation();
            HashMap<Damageable, Integer> targets = new HashMap<>();

            new MagrittaUltHitscan(targets).shot();
            for (int j = 0; j < MagrittaWeaponInfo.PELLET_AMOUNT - 1; j++) {
                Vector dir = VectorUtil.getSpreadedVector(loc.getDirection(), MagrittaWeaponInfo.SPREAD * 1.25);
                new MagrittaUltHitscan(targets).shot(dir);
            }
            targets.forEach((target, hits) -> {
                if (hits >= MagrittaWeaponInfo.PELLET_AMOUNT / 2)
                    MagrittaT1.addShreddingValue(combatUser, target);
            });
            blockHitCount = 0;

            CombatUtil.sendRecoil(combatUser, MagrittaWeaponInfo.RECOIL.UP / 2, MagrittaWeaponInfo.RECOIL.SIDE / 2,
                    MagrittaWeaponInfo.RECOIL.UP_SPREAD / 2, MagrittaWeaponInfo.RECOIL.SIDE_SPREAD / 2, 2, 1);
            MagrittaUltInfo.SOUND.SHOOT.play(loc);
            addTask(new DelayTask(() -> CombatEffectUtil.SHOTGUN_SHELL_DROP_SOUND.play(loc), 8));
        }, () -> {
            onCancelled();
            onEnd();
        }, MagrittaUltInfo.ATTACK_COOLDOWN.toTicks(), MagrittaUltInfo.DURATION.toTicks() / 2));
    }

    /**
     * 사용 종료 시 실행할 작업.
     */
    private void onEnd() {
        combatUser.getWeapon().setCooldown(Timespan.ofTicks(combatUser.getWeapon().getDefaultCooldown().toTicks() * 2));
        combatUser.getWeapon().setVisible(false);

        Location loc = LocationUtil.getLocationFromOffset(combatUser.getArmLocation(MainHand.RIGHT), 0, 0, 0.5);

        MagrittaUltInfo.SOUND.END.play(loc);
        MagrittaUltInfo.PARTICLE.END.play(loc);

        CombatUtil.sendShake(combatUser, 10, 8, Timespan.ofTicks(7));

        addTask(new DelayTask(() -> {
            combatUser.getWeapon().setVisible(true);

            MagrittaUltInfo.SOUND.USE.play(combatUser.getLocation());
        }, MagrittaWeaponInfo.COOLDOWN.toTicks() * 2));
    }

    private final class MagrittaUltHitscan extends Hitscan<Damageable> {
        private final HashMap<Damageable, Integer> targets;

        private MagrittaUltHitscan(@NonNull HashMap<Damageable, Integer> targets) {
            super(combatUser, CombatUtil.EntityCondition.enemy(combatUser),
                    Option.builder().maxDistance(MagrittaWeaponInfo.DISTANCE).build());
            this.targets = targets;
        }

        @Override
        protected void onHit(@NonNull Location location) {
            MagrittaUltInfo.PARTICLE.HIT.play(location);
        }

        @Override
        @NonNull
        protected IntervalHandler getIntervalHandler() {
            return createPeriodIntervalHandler(15, location -> {
                Location loc = LocationUtil.getLocationFromOffset(location, 0.2, -0.2, 0);
                MagrittaUltInfo.PARTICLE.BULLET_TRAIL.play(loc);
            });
        }

        @Override
        @NonNull
        protected HitBlockHandler getHitBlockHandler() {
            return (location, hitBlock) -> {
                CombatEffectUtil.playSmallHitBlockParticle(location, hitBlock, 1);
                MagrittaUltInfo.PARTICLE.HIT_BLOCK.play(location);

                if (blockHitCount++ == 0) {
                    CombatEffectUtil.BULLET_HIT_BLOCK_SOUND.play(location);
                    CombatEffectUtil.playHitBlockSound(location, hitBlock, 1);
                }

                return false;
            };
        }

        @Override
        @NonNull
        protected HitEntityHandler<Damageable> getHitEntityHandler() {
            return (location, target) -> {
                double damage = CombatUtil.getDistantDamage(MagrittaWeaponInfo.DAMAGE, getTravelDistance(), MagrittaWeaponInfo.DISTANCE / 2.0);
                int shredding = target.getStatusEffectModule().getValueStatusEffect(ValueStatusEffect.Type.SHREDDING).getValue();
                if (shredding > 0)
                    damage = damage * (100 + MagrittaT1Info.DAMAGE_INCREMENT * shredding) / 100.0;
                if (target.getDamageModule().damage(combatUser, damage, DamageType.NORMAL, location, false, false))
                    targets.put(target, targets.getOrDefault(target, 0) + 1);

                MagrittaWeaponInfo.PARTICLE.HIT_ENTITY.play(location);

                return false;
            };
        }
    }
}
