package com.dace.dmgr.combat.entity.module;

import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.combat.entity.CombatRestrictions;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.module.statuseffect.StatusEffect;
import com.dace.dmgr.combat.entity.module.statuseffect.StatusEffectType;
import com.dace.dmgr.Timespan;
import com.dace.dmgr.Timestamp;
import com.dace.dmgr.util.task.IntervalTask;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.LivingEntity;

import java.util.HashMap;
import java.util.HashSet;

/**
 * 엔티티의 상태 효과 모듈 클래스.
 *
 * <p>전투 시스템 엔티티가 {@link Damageable}을 상속받는 클래스여야 하며,
 * 엔티티가 {@link LivingEntity}을 상속받는 클래스여야 한다.</p>
 *
 * @see Damageable
 */
public final class StatusEffectModule {
    /** 상태 효과 저항 기본값 */
    private static final double DEFAULT_VALUE = 1;

    /** 엔티티 객체 */
    @NonNull
    @Getter
    private final Damageable combatEntity;
    /** 상태 효과 저항 값 */
    @NonNull
    @Getter
    private final AbilityStatus resistanceStatus;
    /** 적용된 상태 효과 목록 (상태 효과 : 상태 효과 정보) */
    private final HashMap<StatusEffect, StatusEffectInfo> statusEffectMap = new HashMap<>();

    /**
     * 상태 효과 모듈 인스턴스를 생성한다.
     *
     * @param combatEntity 대상 엔티티
     * @param resistance   상태 효과 저항 기본값. 0 이상의 값
     * @throws IllegalArgumentException 인자값이 유효하지 않거나 대상 엔티티가 {@link LivingEntity}를
     *                                  상속받지 않으면 발생
     */
    public StatusEffectModule(@NonNull Damageable combatEntity, double resistance) {
        if (resistance < 0)
            throw new IllegalArgumentException("'resistance'가 0 이상이어야 함");
        if (!(combatEntity.getEntity() instanceof LivingEntity))
            throw new IllegalArgumentException("'combatEntity'의 엔티티가 LivingEntity를 상속받지 않음");

        this.combatEntity = combatEntity;
        this.resistanceStatus = new AbilityStatus(resistance);
    }

    /**
     * 상태 효과 모듈 인스턴스를 생성한다.
     *
     * @param combatEntity 대상 엔티티
     * @throws IllegalArgumentException 대상 엔티티가 {@link LivingEntity}를 상속받지 않으면 발생
     */
    public StatusEffectModule(@NonNull Damageable combatEntity) {
        this(combatEntity, DEFAULT_VALUE);
    }

    /**
     * 엔티티에게 상태 효과를 적용한다.
     *
     * <p>이미 해당 상태 효과를 가지고 있으면 새로 지정한 지속시간이
     * 남은 시간보다 길 경우에만 적용한다.</p>
     *
     * @param provider     제공자
     * @param statusEffect 적용할 상태 효과
     * @param duration     지속시간 (tick). -1로 지정 시 무한 지속
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    public void applyStatusEffect(@NonNull CombatEntity provider, @NonNull StatusEffect statusEffect, long duration) {
        if (duration < -1)
            throw new IllegalArgumentException("'duration'이 -1 이상이어야 함");

        Timespan time = duration == -1 ? Timespan.MAX : Timespan.ofTicks(duration);
        if (!statusEffect.isPositive())
            time = Timespan.ofMilliseconds((long) (time.toMilliseconds() * (Math.max(0, 2 - resistanceStatus.getValue()))));
        if (time.toMilliseconds() <= 0)
            return;

        StatusEffectInfo statusEffectInfo = statusEffectMap.computeIfAbsent(statusEffect, k -> new StatusEffectInfo(provider));

        if (statusEffectInfo.expiration.isBefore(Timestamp.now())) {
            statusEffectInfo.expiration = Timestamp.now().plus(time);

            statusEffect.onStart(combatEntity, provider);

            new IntervalTask(i -> {
                if (combatEntity.isDisposed() || getStatusEffectDuration(statusEffect) == 0 || !statusEffectMap.containsKey(statusEffect))
                    return false;

                statusEffect.onTick(combatEntity, provider, i);

                return true;
            }, () -> {
                if (statusEffectMap.remove(statusEffect) == null)
                    return;

                statusEffect.onEnd(combatEntity, provider);
            }, 1);
        } else if (getStatusEffectDuration(statusEffect) < time.toTicks())
            statusEffectInfo.expiration = Timestamp.now().plus(time);
    }

    /**
     * 엔티티의 지정한 상태 효과의 남은 시간을 반환한다.
     *
     * @param statusEffect 확인할 상태 효과
     * @return 남은 시간 (tick)
     */
    public long getStatusEffectDuration(@NonNull StatusEffect statusEffect) {
        StatusEffectInfo statusEffectInfo = statusEffectMap.get(statusEffect);
        return statusEffectInfo == null ? 0 : Math.max(0, Timestamp.now().until(statusEffectInfo.expiration).toTicks());
    }

    /**
     * 엔티티가 지정한 상태 효과 종류에 해당하는 상태 효과를 가지고 있는지 확인한다.
     *
     * @param statusEffectType 확인할 상태 효과 종류
     * @return 상태 효과를 가지고 있으면 {@code true} 반환
     */
    public boolean hasStatusEffectType(@NonNull StatusEffectType statusEffectType) {
        for (StatusEffect statusEffect : statusEffectMap.keySet()) {
            if (statusEffect.getStatusEffectType() == statusEffectType)
                return true;
        }

        return false;
    }

    /**
     * 엔티티가 지정한 상태 효과를 가지고 있는지 확인한다.
     *
     * @param statusEffect 확인할 상태 효과
     * @return 상태 효과를 가지고 있으면 {@code true} 반환
     */
    public boolean hasStatusEffect(@NonNull StatusEffect statusEffect) {
        return statusEffectMap.containsKey(statusEffect);
    }

    /**
     * 엔티티가 가지고 있는 효과들이 지정한 상태 제한들을 하나라도 제한하는지 확인한다.
     *
     * @param restrictions 확인할 상태 제한 비트마스크
     * @return 가지고 있는 어떤 상태 효과라도 지정한 상태 제한을 하나라도 포함하면 {@code true}
     * @see CombatRestrictions
     */
    public boolean hasAnyRestriction(long restrictions) {
        for (StatusEffect statusEffect : statusEffectMap.keySet()) {
            if ((statusEffect.getCombatRestrictions(combatEntity) & restrictions) != 0)
                return true;
        }

        return false;
    }

    /**
     * 엔티티가 가지고 있는 효과들이 지정한 상태 제한들을 모두 제한하는지 확인한다.
     *
     * @param restrictions 확인할 상태 제한 비트마스크
     * @return 가지고 있는 모든 상태 효과의 조합이 지정한 상태 제한을 모두 포함하면 {@code true}
     * @see CombatRestrictions
     */
    public boolean hasAllRestrictions(long restrictions) {
        long combinedRestrictions = CombatRestrictions.NONE;
        for (StatusEffect statusEffect : statusEffectMap.keySet())
            combinedRestrictions |= statusEffect.getCombatRestrictions(combatEntity);

        return (combinedRestrictions & restrictions) == restrictions;
    }

    /**
     * 엔티티의 지정한 상태 효과 종류에 해당하는 상태 효과를 제거한다.
     *
     * @param statusEffectType 제거할 상태 효과 종류
     */
    public void removeStatusEffectType(@NonNull StatusEffectType statusEffectType) {
        new HashSet<>(statusEffectMap.keySet()).forEach(statusEffect -> {
            if (statusEffect.getStatusEffectType() == statusEffectType)
                removeStatusEffect(statusEffect);
        });
    }

    /**
     * 엔티티의 상태 효과를 제거한다.
     *
     * @param statusEffect 제거할 상태 효과
     */
    public void removeStatusEffect(@NonNull StatusEffect statusEffect) {
        StatusEffectInfo statusEffectInfo = statusEffectMap.remove(statusEffect);
        if (statusEffectInfo != null)
            statusEffect.onEnd(combatEntity, statusEffectInfo.provier);
    }

    /**
     * 엔티티의 상태 효과를 모두 제거한다.
     */
    public void clearStatusEffect() {
        new HashSet<>(statusEffectMap.keySet()).forEach(this::removeStatusEffect);
    }

    /**
     * 엔티티의 이로운/해로운 상태 효과를 모두 제거한다.
     *
     * @param isPositive {@code true}로 지정 시 이로운 효과, {@code false}로 지정 시 해로운 효과만 제거
     */
    public void clearStatusEffect(boolean isPositive) {
        new HashSet<>(statusEffectMap.keySet()).forEach(statusEffect -> {
            if (statusEffect.isPositive() == isPositive)
                removeStatusEffect(statusEffect);
        });
    }

    /**
     * 적용된 상태 효과 정보 클래스.
     */
    @RequiredArgsConstructor
    private static final class StatusEffectInfo {
        /** 제공자 */
        private final CombatEntity provier;
        /** 종료 시점 */
        private Timestamp expiration = Timestamp.now();
    }
}
