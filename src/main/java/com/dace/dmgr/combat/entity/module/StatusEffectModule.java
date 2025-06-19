package com.dace.dmgr.combat.entity.module;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.Timestamp;
import com.dace.dmgr.combat.entity.CombatRestriction;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.combat.entity.module.statuseffect.StatusEffect;
import com.dace.dmgr.util.task.IntervalTask;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;

/**
 * 엔티티의 상태 효과 모듈 클래스.
 *
 * @see StatusEffect
 * @see Damageable
 */
public final class StatusEffectModule extends CombatEntityModule<Damageable> {
    /** 적용된 상태 효과 정보 목록 (상태 효과 : 상태 효과 정보) */
    private final HashMap<StatusEffect, StatusEffectInfo> statusEffectInfoMap = new HashMap<>();

    /**
     * 상태 효과 모듈 인스턴스를 생성한다.
     *
     * @param combatEntity 대상 엔티티
     */
    public StatusEffectModule(@NonNull Damageable combatEntity) {
        super(combatEntity);
        combatEntity.addOnRemove(this::clear);
    }

    @Override
    protected double getBaseValue() {
        return 1;
    }

    /**
     * 엔티티의 지정한 상태 효과 클래스에 해당하는 상태 효과를 반환한다.
     *
     * <p>해당하는 상태 효과가 여러개일 경우 하나만 반환한다.</p>
     *
     * @param statusEffectClass 상태 효과 클래스
     * @param <T>               {@link StatusEffect}를 상속받는 상태 효과
     * @return 상태 효과
     */
    @Nullable
    @SuppressWarnings("unchecked")
    public <T extends StatusEffect> T get(@NonNull Class<T> statusEffectClass) {
        return (T) statusEffectInfoMap.keySet().stream()
                .filter(statusEffect -> statusEffect.getClass() == statusEffectClass)
                .findFirst()
                .orElse(null);
    }

    /**
     * 엔티티에게 상태 효과를 적용한다.
     *
     * @param statusEffect 적용할 상태 효과
     * @param duration     지속시간
     */
    public void apply(@NonNull StatusEffect statusEffect, @NonNull Timespan duration) {
        if (!statusEffect.isPositive())
            duration = duration.multiply(Math.max(0, 2 - getValue()));
        if (duration.isZero())
            return;

        Timestamp expiration = Timestamp.now().plus(duration);

        StatusEffectInfo statusEffectInfo = statusEffectInfoMap.computeIfAbsent(statusEffect, k ->
                new StatusEffectInfo(statusEffect, expiration));
        statusEffectInfo.expiration = expiration;
    }

    /**
     * 엔티티의 지정한 상태 효과의 남은 시간을 반환한다.
     *
     * @param statusEffect 확인할 상태 효과
     * @return 남은 시간
     */
    @NonNull
    public Timespan getDuration(@NonNull StatusEffect statusEffect) {
        StatusEffectInfo statusEffectInfo = statusEffectInfoMap.get(statusEffect);
        return statusEffectInfo == null ? Timespan.ZERO : Timestamp.now().until(statusEffectInfo.expiration);
    }

    /**
     * 엔티티가 지정한 상태 효과 클래스에 해당하는 상태 효과를 가지고 있는지 확인한다.
     *
     * @param statusEffectClass 확인할 상태 효과 클래스
     * @param <T>               {@link StatusEffect}를 상속받는 상태 효과
     * @return 상태 효과를 가지고 있으면 {@code true} 반환
     */
    public <T extends StatusEffect> boolean has(@NonNull Class<T> statusEffectClass) {
        return statusEffectInfoMap.keySet().stream().anyMatch(statusEffect -> statusEffect.getClass() == statusEffectClass);
    }

    /**
     * 엔티티가 지정한 상태 효과를 가지고 있는지 확인한다.
     *
     * @param statusEffect 확인할 상태 효과
     * @return 상태 효과를 가지고 있으면 {@code true} 반환
     */
    public boolean has(@NonNull StatusEffect statusEffect) {
        return statusEffectInfoMap.containsKey(statusEffect);
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
        return statusEffectInfoMap.keySet().stream()
                .flatMap(statusEffect -> statusEffect.getCombatRestrictions(combatEntity).stream())
                .anyMatch(value -> value.restrictionValues().contains(combatRestriction));
    }

    /**
     * 엔티티의 상태 효과를 제거한다.
     *
     * @param statusEffect 제거할 상태 효과
     */
    public void remove(@NonNull StatusEffect statusEffect) {
        StatusEffectInfo statusEffectInfo = statusEffectInfoMap.get(statusEffect);
        if (statusEffectInfo != null)
            statusEffectInfo.onFinish();
    }

    /**
     * 엔티티의 상태 효과를 모두 제거한다.
     */
    public void clear() {
        new HashSet<>(statusEffectInfoMap.keySet()).forEach(this::remove);
    }

    /**
     * 엔티티의 이로운/해로운 상태 효과를 모두 제거한다.
     *
     * @param isPositive {@code true}로 지정 시 이로운 효과, {@code false}로 지정 시 해로운 효과만 제거
     */
    public void clear(boolean isPositive) {
        new HashSet<>(statusEffectInfoMap.keySet()).forEach(statusEffect -> {
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
        /** 틱 작업을 처리하는 태스크 */
        private final IntervalTask onTickTask;
        /** 종료 시점 */
        private Timestamp expiration;

        private StatusEffectInfo(@NonNull StatusEffect statusEffect, @NonNull Timestamp expiration) {
            this.statusEffect = statusEffect;
            this.expiration = expiration;

            statusEffect.onStart(combatEntity);

            this.onTickTask = new IntervalTask(i -> {
                if (this.expiration.isBefore(Timestamp.now()) || combatEntity instanceof CombatUser && ((CombatUser) combatEntity).isDead())
                    return false;

                statusEffect.onTick(combatEntity, i);
                return true;
            }, this::onFinish, 1);

            combatEntity.addTask(onTickTask);
        }

        private void onFinish() {
            statusEffectInfoMap.remove(statusEffect);
            statusEffect.onEnd(combatEntity);

            onTickTask.stop();
        }
    }
}
