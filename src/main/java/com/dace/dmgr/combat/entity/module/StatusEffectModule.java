package com.dace.dmgr.combat.entity.module;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.Timestamp;
import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.combat.entity.CombatRestriction;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.module.statuseffect.StatusEffect;
import com.dace.dmgr.combat.entity.module.statuseffect.StatusEffectType;
import com.dace.dmgr.combat.entity.module.statuseffect.ValueStatusEffect;
import com.dace.dmgr.util.task.IntervalTask;
import lombok.Getter;
import lombok.NonNull;
import org.apache.commons.lang3.Validate;
import org.bukkit.entity.LivingEntity;

import java.util.HashMap;
import java.util.HashSet;

/**
 * 엔티티의 상태 효과 모듈 클래스.
 *
 * <p>엔티티가 {@link LivingEntity}을 상속받는 클래스여야 한다.</p>
 *
 * @see StatusEffect
 * @see Damageable
 */
public final class StatusEffectModule {
    /** 상태 효과 저항 기본값 */
    private static final double DEFAULT_VALUE = 1;

    /** 엔티티 인스턴스 */
    private final Damageable combatEntity;
    /** 해로운 상태 효과에 대한 저항 값 */
    @NonNull
    @Getter
    private final AbilityStatus resistanceStatus;
    /** 상태 변수 상태 효과 목록 (상태 변수 종류 : 상태 효과) */
    private final HashMap<ValueStatusEffect.Type<?>, ValueStatusEffect> valueStatusEffectMap = new HashMap<>();
    /** 적용된 상태 효과 목록 (상태 효과 : 상태 효과 정보) */
    private final HashMap<StatusEffect, StatusEffectInfo> statusEffectMap = new HashMap<>();

    /**
     * 상태 효과 모듈 인스턴스를 생성한다.
     *
     * @param combatEntity 대상 엔티티
     * @throws IllegalArgumentException 대상 엔티티가 {@link LivingEntity}를 상속받지 않으면 발생
     */
    public StatusEffectModule(@NonNull Damageable combatEntity) {
        Validate.isTrue(combatEntity.getEntity() instanceof LivingEntity, "combatEntity.getEntity()가 LivingEntity를 상속받지 않음");

        this.combatEntity = combatEntity;
        this.resistanceStatus = new AbilityStatus(DEFAULT_VALUE);
        for (ValueStatusEffect.Type<?> type : ValueStatusEffect.Type.values())
            this.valueStatusEffectMap.put(type, type.createStatusEffect());

        combatEntity.addOnDispose(this::clear);
    }

    /**
     * 지정한 상태 변수 종류에 해당하는 상태 효과를 반환한다.
     *
     * @param type 상태 변수 종류
     * @param <T>  {@link ValueStatusEffect}를 상속받는 상태 변수를 가진 상태 효과
     * @return 상태 변수를 가진 상태 효과
     * @see ValueStatusEffect.Type
     */
    @NonNull
    @SuppressWarnings("unchecked")
    public <T extends ValueStatusEffect> T getValueStatusEffect(@NonNull ValueStatusEffect.Type<T> type) {
        return (T) valueStatusEffectMap.get(type);
    }

    /**
     * 엔티티에게 상태 효과를 적용한다.
     *
     * <p>이미 해당 상태 효과를 가지고 있으면 새로 지정한 지속시간이 남은 시간보다 길 경우에만 적용한다.</p>
     *
     * @param statusEffect 적용할 상태 효과
     * @param provider     제공자
     * @param duration     지속시간
     */
    public void apply(@NonNull StatusEffect statusEffect, @NonNull CombatEntity provider, @NonNull Timespan duration) {
        if (combatEntity.isDisposed())
            return;

        if (!statusEffect.isPositive())
            duration = duration.multiply(Math.max(0, 2 - resistanceStatus.getValue()));
        if (duration.isZero())
            return;

        Timestamp expiration = Timestamp.now().plus(duration);

        StatusEffectInfo statusEffectInfo = statusEffectMap.computeIfAbsent(statusEffect, k ->
                new StatusEffectInfo(statusEffect, provider, expiration));
        statusEffectInfo.expiration = expiration;
    }

    /**
     * 엔티티에게 상태 변수를 가진 상태 효과를 적용한다.
     *
     * <p>이미 해당 상태 효과를 가지고 있으면 새로 지정한 지속시간이 남은 시간보다 길 경우에만 적용한다.</p>
     *
     * @param type     상태 변수 종류
     * @param provider 제공자
     * @param duration 지속시간
     * @param <T>      {@link ValueStatusEffect}를 상속받는 상태 변수를 가진 상태 효과
     * @return 적용된 상태 효과
     * @see ValueStatusEffect.Type
     */
    @NonNull
    public <T extends ValueStatusEffect> T apply(@NonNull ValueStatusEffect.Type<T> type, @NonNull CombatEntity provider, @NonNull Timespan duration) {
        T valueStatusEffect = getValueStatusEffect(type);
        apply(valueStatusEffect, provider, duration);

        return valueStatusEffect;
    }

    /**
     * 엔티티의 지정한 상태 효과의 남은 시간을 반환한다.
     *
     * @param statusEffect 확인할 상태 효과
     * @return 남은 시간
     */
    @NonNull
    public Timespan getDuration(@NonNull StatusEffect statusEffect) {
        StatusEffectInfo statusEffectInfo = statusEffectMap.get(statusEffect);
        return statusEffectInfo == null ? Timespan.ZERO : Timestamp.now().until(statusEffectInfo.expiration);
    }

    /**
     * 지정한 상태 변수 종류에 해당하는 상태 효과의 남은 시간을 반환한다.
     *
     * @param type 상태 변수 종류
     * @return 남은 시간
     * @see ValueStatusEffect.Type
     */
    @NonNull
    public Timespan getDuration(@NonNull ValueStatusEffect.Type<?> type) {
        return getDuration(getValueStatusEffect(type));
    }

    /**
     * 엔티티가 지정한 상태 효과 유형에 해당하는 상태 효과를 가지고 있는지 확인한다.
     *
     * @param statusEffectType 확인할 상태 효과 유형
     * @return 상태 효과를 가지고 있으면 {@code true} 반환
     */
    public boolean hasType(@NonNull StatusEffectType statusEffectType) {
        return statusEffectMap.keySet().stream().anyMatch(statusEffect -> statusEffect.getStatusEffectType() == statusEffectType);
    }

    /**
     * 엔티티가 지정한 상태 효과를 가지고 있는지 확인한다.
     *
     * @param statusEffect 확인할 상태 효과
     * @return 상태 효과를 가지고 있으면 {@code true} 반환
     */
    public boolean has(@NonNull StatusEffect statusEffect) {
        return statusEffectMap.containsKey(statusEffect);
    }

    /**
     * 지정한 상태 변수 종류에 해당하는 상태 효과를 가지고 있는지 확인한다.
     *
     * @param type 상태 변수 종류
     * @return 상태 효과를 가지고 있으면 {@code true} 반환
     * @see ValueStatusEffect.Type
     */
    public boolean has(@NonNull ValueStatusEffect.Type<?> type) {
        return has(getValueStatusEffect(type));
    }

    /**
     * 엔티티에 적용된 상태 효과들이 지정한 행동 제한을 하나라도 포함하는지 확인한다.
     *
     * @param combatRestriction 확인할 행동 제한
     * @return 적용된 상태 효과들이 지정한 행동 제한을 포함하면 {@code true} 반환
     * @see StatusEffect#getCombatRestrictions(Damageable)
     * @see CombatRestriction
     */
    public boolean hasRestriction(@NonNull CombatRestriction combatRestriction) {
        return statusEffectMap.keySet().stream()
                .flatMap(statusEffect -> statusEffect.getCombatRestrictions(combatEntity).stream())
                .anyMatch(value -> value.restrictionValues().contains(combatRestriction));
    }

    /**
     * 엔티티의 지정한 상태 효과 유형에 해당하는 상태 효과를 제거한다.
     *
     * @param statusEffectType 제거할 상태 효과 유형
     */
    public void removeType(@NonNull StatusEffectType statusEffectType) {
        new HashSet<>(statusEffectMap.keySet()).forEach(statusEffect -> {
            if (statusEffect.getStatusEffectType() == statusEffectType)
                remove(statusEffect);
        });
    }

    /**
     * 엔티티의 상태 효과를 제거한다.
     *
     * @param statusEffect 제거할 상태 효과
     */
    public void remove(@NonNull StatusEffect statusEffect) {
        StatusEffectInfo statusEffectInfo = statusEffectMap.get(statusEffect);
        if (statusEffectInfo != null)
            statusEffectInfo.onFinish();
    }

    /**
     * 지정한 상태 변수 종류에 해당하는 상태 효과를 제거한다.
     *
     * @param type 상태 변수 종류
     * @see ValueStatusEffect.Type
     */
    public void remove(@NonNull ValueStatusEffect.Type<?> type) {
        remove(getValueStatusEffect(type));
    }

    /**
     * 엔티티의 상태 효과를 모두 제거한다.
     */
    public void clear() {
        new HashSet<>(statusEffectMap.keySet()).forEach(this::remove);
    }

    /**
     * 엔티티의 이로운/해로운 상태 효과를 모두 제거한다.
     *
     * @param isPositive {@code true}로 지정 시 이로운 효과, {@code false}로 지정 시 해로운 효과만 제거
     */
    public void clear(boolean isPositive) {
        new HashSet<>(statusEffectMap.keySet()).forEach(statusEffect -> {
            if (statusEffect.isPositive() == isPositive)
                remove(statusEffect);
        });
    }

    /**
     * 적용된 상태 효과 정보 클래스.
     */
    private final class StatusEffectInfo {
        /** 상태 효과 */
        private final StatusEffect statusEffect;
        /** 제공자 */
        private final CombatEntity provider;
        /** 틱 작업을 처리하는 태스크 */
        private final IntervalTask onTickTask;
        /** 종료 시점 */
        private Timestamp expiration;

        private StatusEffectInfo(@NonNull StatusEffect statusEffect, @NonNull CombatEntity provider, @NonNull Timestamp expiration) {
            this.statusEffect = statusEffect;
            this.provider = provider;
            this.expiration = expiration;

            statusEffect.onStart(combatEntity, provider);

            this.onTickTask = new IntervalTask(i -> {
                if (this.expiration.isBefore(Timestamp.now()))
                    return false;

                statusEffect.onTick(combatEntity, provider, i);
                return true;
            }, this::onFinish, 1);

            combatEntity.addTask(onTickTask);
        }

        private void onFinish() {
            statusEffectMap.remove(statusEffect);
            statusEffect.onEnd(combatEntity, provider);

            if (!onTickTask.isDisposed())
                onTickTask.dispose();
        }
    }
}
