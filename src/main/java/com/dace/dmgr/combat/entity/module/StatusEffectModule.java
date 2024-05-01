package com.dace.dmgr.combat.entity.module;

import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.combat.entity.module.statuseffect.StatusEffect;
import com.dace.dmgr.combat.entity.module.statuseffect.StatusEffectType;
import com.dace.dmgr.util.Cooldown;
import com.dace.dmgr.util.CooldownUtil;
import com.dace.dmgr.util.task.IntervalTask;
import com.dace.dmgr.util.task.TaskUtil;
import lombok.Getter;
import lombok.NonNull;

/**
 * 엔티티의 상태 효과 모듈 클래스.
 */
@Getter
public final class StatusEffectModule {
    /** 상태 효과 저항 기본값 */
    private static final double DEFAULT_VALUE = 1;
    /** 엔티티 객체 */
    @NonNull
    private final CombatEntity combatEntity;
    /** 상태 효과 저항 값 */
    @NonNull
    private final AbilityStatus resistanceStatus;

    /**
     * 상태 효과 모듈 인스턴스를 생성한다.
     *
     * @param combatEntity 대상 엔티티
     * @param resistance   상태 효과 저항 기본값
     */
    public StatusEffectModule(@NonNull CombatEntity combatEntity, double resistance) {
        this.combatEntity = combatEntity;
        this.resistanceStatus = new AbilityStatus(resistance);
    }

    /**
     * 상태 효과 모듈 인스턴스를 생성한다.
     *
     * @param combatEntity 대상 엔티티
     */
    public StatusEffectModule(@NonNull CombatEntity combatEntity) {
        this(combatEntity, DEFAULT_VALUE);
    }

    /**
     * 엔티티에게 상태 효과를 적용한다.
     *
     * <p>이미 해당 상태 효과를 가지고 있으면 새로 지정한 지속시간이
     * 남은 시간보다 길 경우에만 적용한다.</p>
     *
     * @param statusEffectType 적용할 상태 효과 종류
     * @param statusEffect     적용할 상태 효과 정보
     * @param duration         지속시간 (tick)
     */
    public void applyStatusEffect(@NonNull StatusEffectType statusEffectType, @NonNull StatusEffect statusEffect, long duration) {
        long finalDuration = (long) (duration * (Math.max(0, 2 - resistanceStatus.getValue())));
        if (finalDuration == 0)
            return;

        if (!hasStatusEffect(statusEffectType) || getStatusEffectDuration(statusEffectType) < finalDuration)
            CooldownUtil.setCooldown(this, Cooldown.STATUS_EFFECT, statusEffectType.toString(), finalDuration);

        if (getStatusEffectDuration(statusEffectType, statusEffect) == 0) {
            CooldownUtil.setCooldown(this, Cooldown.STATUS_EFFECT, statusEffectType + statusEffect.toString(), finalDuration);

            statusEffect.onStart(combatEntity);

            TaskUtil.addTask(combatEntity, new IntervalTask(i -> {
                if (combatEntity.isDisposed())
                    return false;
                if (getStatusEffectDuration(statusEffectType) == 0 || getStatusEffectDuration(statusEffectType, statusEffect) == 0)
                    return false;

                statusEffect.onTick(combatEntity, i);

                return true;
            }, isCancelled -> statusEffect.onEnd(combatEntity), 1));
        } else if (getStatusEffectDuration(statusEffectType, statusEffect) < finalDuration)
            CooldownUtil.setCooldown(this, Cooldown.STATUS_EFFECT, statusEffectType + statusEffect.toString(), finalDuration);
    }

    /**
     * 엔티티에게 상태 효과를 적용한다.
     *
     * <p>이미 해당 상태 효과를 가지고 있으면 새로 지정한 지속시간이
     * 남은 시간보다 길 경우에만 적용한다.</p>
     *
     * @param statusEffectType 적용할 상태 효과 종류
     * @param duration         지속시간 (tick)
     */
    public void applyStatusEffect(@NonNull StatusEffectType statusEffectType, long duration) {
        applyStatusEffect(statusEffectType, statusEffectType.getStatusEffect(), duration);
    }

    /**
     * 엔티티의 지정한 상태 효과의 남은 시간을 반환한다.
     *
     * @param statusEffectType 확인할 상태 효과 종류
     * @return 남은 시간 (tick)
     */
    public long getStatusEffectDuration(@NonNull StatusEffectType statusEffectType) {
        return CooldownUtil.getCooldown(this, Cooldown.STATUS_EFFECT, statusEffectType.toString());
    }

    /**
     * 엔티티의 지정한 상태 효과의 남은 시간을 반환한다.
     *
     * @param statusEffectType 확인할 상태 효과 종류
     * @param statusEffect     확인할 상태 효과 정보
     * @return 남은 시간 (tick)
     */
    private long getStatusEffectDuration(@NonNull StatusEffectType statusEffectType, @NonNull StatusEffect statusEffect) {
        return CooldownUtil.getCooldown(this, Cooldown.STATUS_EFFECT, statusEffectType + statusEffect.toString());
    }

    /**
     * 엔티티가 지정한 상태 효과를 가지고 있는 지 확인한다.
     *
     * @param statusEffectType 확인할 상태 효과 종류
     * @return 상태 효과를 가지고 있으면 {@code true} 반환
     */
    public boolean hasStatusEffect(@NonNull StatusEffectType statusEffectType) {
        return getStatusEffectDuration(statusEffectType) > 0;
    }

    /**
     * 엔티티의 상태 효과를 제거한다.
     *
     * @param statusEffectType 제거할 상태 효과 종류
     */
    public void removeStatusEffect(@NonNull StatusEffectType statusEffectType) {
        CooldownUtil.setCooldown(this, Cooldown.STATUS_EFFECT, statusEffectType.toString());
    }

    /**
     * 엔티티의 상태 효과를 모두 제거한다.
     */
    public void clearStatusEffect() {
        for (StatusEffectType statusEffectType : StatusEffectType.values()) {
            CooldownUtil.setCooldown(this, Cooldown.STATUS_EFFECT, statusEffectType.toString());
        }
    }

    /**
     * 엔티티의 이로운/해로운 상태 효과를 모두 제거한다.
     *
     * @param isPositive {@code true}로 지정 시 이로운 효과, {@code false}로 지정 시 해로운 효과만 제거
     */
    public void clearStatusEffect(boolean isPositive) {
        for (StatusEffectType statusEffectType : StatusEffectType.values()) {
            if (statusEffectType.getStatusEffect().isPositive() == isPositive)
                CooldownUtil.setCooldown(this, Cooldown.STATUS_EFFECT, statusEffectType.toString());
        }
    }
}
