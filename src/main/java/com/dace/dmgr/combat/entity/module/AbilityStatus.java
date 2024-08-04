package com.dace.dmgr.combat.entity.module;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

/**
 * 엔티티의 능력치 값을 관리하는 클래스.
 *
 * <p>최종 값 계산 방식 : {@link AbilityStatus#baseValue} +
 * (100+모든 수정자({@link AbilityStatus#modifiers}) 값의 합)/100</p>
 */
public final class AbilityStatus {
    /** 값 수정자 목록 (수정자 ID : 증가량) */
    private final HashMap<String, Double> modifiers = new HashMap<>();
    /** 기본값 */
    @Getter
    @Setter
    private double baseValue;

    /**
     * 기본값을 지정하여 능력치 인스턴스를 생성한다.
     *
     * @param baseValue 기본값
     */
    AbilityStatus(double baseValue) {
        this.baseValue = baseValue;
    }

    /**
     * 기본값에 모든 수정자를 적용한 최종 값을 반환한다.
     *
     * @return {@link AbilityStatus#baseValue} + (100+모든 수정자({@link AbilityStatus#modifiers}) 값의 합)/100
     */
    public double getValue() {
        double valueSum = 0;
        for (double value : modifiers.values())
            valueSum += value;

        return baseValue * (100 + valueSum) / 100;
    }

    /**
     * 모든 수정자를 반환한다.
     *
     * @return 수정자 목록
     */
    @NonNull
    public Map<@NonNull String, Double> getModifiers() {
        return new HashMap<>(modifiers);
    }

    /**
     * 수정자를 추가한다.
     *
     * <p>이미 해당 ID의 수정자가 존재할 경우 덮어쓴다.</p>
     *
     * @param id        수정자 ID
     * @param increment 수치 증가량
     */
    public void addModifier(@NonNull String id, double increment) {
        modifiers.put(id, increment);
    }

    /**
     * 수정자를 제거한다.
     *
     * @param id 수정자 ID
     */
    public void removeModifier(@NonNull String id) {
        modifiers.remove(id);
    }

    /**
     * 모든 수정자를 제거한다.
     */
    public void clearModifier() {
        modifiers.clear();
    }
}
