package com.dace.dmgr.combat.character.vellion.action;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.ActiveSkill;
import com.dace.dmgr.combat.action.skill.Confirmable;
import com.dace.dmgr.combat.action.skill.module.LocationConfirmModule;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.module.statuseffect.HealBlock;
import com.dace.dmgr.combat.entity.module.statuseffect.Silence;
import com.dace.dmgr.combat.interaction.Area;
import com.dace.dmgr.combat.interaction.DamageType;
import com.dace.dmgr.util.*;
import com.dace.dmgr.util.task.IntervalTask;
import com.dace.dmgr.util.task.TaskUtil;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

@Getter
public final class VellionA3 extends ActiveSkill implements Confirmable {
    /** 처치 지원 점수 제한시간 쿨타임 ID */
    private static final String ASSIST_SCORE_COOLDOWN_ID = "VellionA3AssistScoreTimeLimit";
    /** 수정자 ID */
    private static final String MODIFIER_ID = "VellionA3";

    /** 위치 확인 모듈 */
    @NonNull
    private final LocationConfirmModule confirmModule;

    public VellionA3(@NonNull CombatUser combatUser) {
        super(combatUser, VellionA3Info.getInstance(), 2);
        confirmModule = new LocationConfirmModule(this, ActionKey.LEFT_CLICK, ActionKey.SLOT_3, VellionA3Info.MAX_DISTANCE);
    }

    @Override
    public int getPriority() {
        return 1;
    }

    @Override
    @NonNull
    public ActionKey @NonNull [] getDefaultActionKeys() {
        return new ActionKey[]{ActionKey.SLOT_3, ActionKey.LEFT_CLICK};
    }

    @Override
    public long getDefaultCooldown() {
        return VellionA3Info.COOLDOWN;
    }

    @Override
    public long getDefaultDuration() {
        return -1;
    }

    @Override
    public boolean canUse(@NonNull ActionKey actionKey) {
        return super.canUse(actionKey) && isDurationFinished() && combatUser.getSkill(VellionUltInfo.getInstance()).isDurationFinished();
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        switch (actionKey) {
            case SLOT_3: {
                combatUser.getWeapon().onCancelled();
                confirmModule.toggleCheck();

                break;
            }
            case LEFT_CLICK: {
                onUse();

                break;
            }
            default:
                break;
        }
    }

    @Override
    public boolean isCancellable() {
        return confirmModule.isChecking() || !isDurationFinished();
    }

    @Override
    public void onCancelled() {
        super.onCancelled();

        confirmModule.cancel();
        if (!isDurationFinished()) {
            setDuration(0);
            combatUser.getMoveModule().getSpeedStatus().removeModifier(MODIFIER_ID);
        }
    }

    @Override
    public void onCheckEnable() {
        // 미사용
    }

    @Override
    public void onCheckTick(long i) {
        // 미사용
    }

    @Override
    public void onCheckDisable() {
        // 미사용
    }

    /**
     * 사용 시 실행할 작업.
     */
    private void onUse() {
        if (!confirmModule.isValid())
            return;

        setDuration();
        combatUser.setGlobalCooldown((int) VellionA3Info.READY_DURATION);
        confirmModule.toggleCheck();
        combatUser.getWeapon().setCooldown(1);
        combatUser.getMoveModule().getSpeedStatus().addModifier(MODIFIER_ID, -VellionA3Info.READY_SLOW);
        Location location = confirmModule.getCurrentLocation();

        SoundUtil.playNamedSound(NamedSound.COMBAT_VELLION_A3_USE, combatUser.getEntity().getLocation());

        TaskUtil.addTask(taskRunner, new IntervalTask(i -> {
            Location loc = combatUser.getArmLocation(true);
            for (Location loc2 : LocationUtil.getLine(loc, location, 0.7))
                ParticleUtil.playRGB(ParticleUtil.ColoredParticle.REDSTONE, loc2, 1,
                        0, 0, 0, 156, 60, 130);
            ParticleUtil.play(Particle.SMOKE_LARGE, location, 30, 0.5, 0.3, 0.5, 0.15);
            ParticleUtil.play(Particle.SPELL_WITCH, location, 70, 1, 0.5, 1, 0.2);

            return true;
        }, isCancelled -> {
            onCancelled();
            onReady(location);
        }, 1, VellionA3Info.READY_DURATION));
    }

    /**
     * 시전 완료 시 실행할 작업.
     *
     * @param location 대상 위치
     */
    private void onReady(@NonNull Location location) {
        Location loc = location.clone().add(0, 0.1, 0);

        SoundUtil.playNamedSound(NamedSound.COMBAT_VELLION_A3_USE_READY, loc);

        TaskUtil.addTask(VellionA3.this, new IntervalTask(i -> {
            if (i % 4 == 0)
                new VellionA3Area().emit(loc);

            ParticleUtil.playRGB(ParticleUtil.ColoredParticle.SPELL_MOB, loc, 3, 0.4, 0, 0.4,
                    156, 60, 130);
            playTickEffect(i, loc);

            return true;
        }, 1, VellionA3Info.DURATION));
    }

    /**
     * 범위 표시 효과를 재생한다.
     *
     * @param i        인덱스
     * @param location 사용 위치
     */
    private void playTickEffect(long i, @NonNull Location location) {
        location.setYaw(0);
        location.setPitch(0);
        Vector vector = VectorUtil.getRollAxis(location);
        Vector axis = VectorUtil.getYawAxis(location);

        for (int j = 0; j < 2; j++) {
            long index = i * 2 + j;
            long angle = index * 3;
            double distance = index * 0.2 % 5;

            for (int k = 0; k < 12; k++) {
                angle += distance > 3 ? 90 : 60;
                Vector vec = VectorUtil.getRotatedVector(vector, axis, k < 6 ? angle : -angle);
                Location loc = location.clone().add(vec.clone().multiply(distance));

                ParticleUtil.play(Particle.SMOKE_NORMAL, loc, 0, vec.getX(), 0.4, vec.getZ(), 0.1);
                ParticleUtil.playRGB(ParticleUtil.ColoredParticle.REDSTONE, loc, 1,
                        0, 0, 0, 156, 60, 130);
            }
            double distance2 = index * 0.1 % 2.5;
            long angle2 = index * 44;
            for (int k = 0; k < 3; k++) {
                angle2 += 120;

                Vector vec1 = VectorUtil.getRotatedVector(vector, axis, angle2);
                Vector vec2 = VectorUtil.getRotatedVector(vector, axis, angle2 + 10.0);
                Vector dir = LocationUtil.getDirection(location.clone().add(vec1), location.clone().add(vec2));

                ParticleUtil.play(Particle.SMOKE_LARGE, location.clone().add(vec1.clone().multiply(5)).add(0, distance2 * 0.5, 0),
                        0, dir.getX(), distance2 * 0.1, dir.getZ(), 0.3);
            }
        }
    }

    /**
     * 플레이어에게 처치 지원 점수를 지급한다.
     *
     * @param victim 피격자
     */
    public void applyAssistScore(@NonNull CombatUser victim) {
        if (CooldownUtil.getCooldown(combatUser, ASSIST_SCORE_COOLDOWN_ID + victim) > 0)
            combatUser.addScore("처치 지원", VellionA3Info.ASSIST_SCORE);
    }

    private final class VellionA3Area extends Area {
        private VellionA3Area() {
            super(combatUser, VellionA3Info.RADIUS, combatUser::isEnemy);
        }

        @Override
        protected boolean onHitBlock(@NonNull Location center, @NonNull Location location, @NonNull Block hitBlock) {
            return false;
        }

        @Override
        public boolean onHitEntity(@NonNull Location center, @NonNull Location location, @NonNull Damageable target) {
            if (target.getDamageModule().damage(combatUser, 0, DamageType.NORMAL, null,
                    false, true)) {
                target.getStatusEffectModule().applyStatusEffect(combatUser, HealBlock.getInstance(), 10);
                target.getStatusEffectModule().applyStatusEffect(combatUser, Silence.getInstance(), 10);

                if (target instanceof CombatUser) {
                    combatUser.addScore("적 침묵", (double) (VellionA3Info.EFFECT_SCORE_PER_SECOND * 4) / 20);
                    CooldownUtil.setCooldown(combatUser, ASSIST_SCORE_COOLDOWN_ID + target, 10);
                }
            }

            return true;
        }
    }
}
