package com.dace.dmgr.combat.entity.module;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.util.HashSet;

/**
 * 엔티티의 능력치 값을 관리하는 클래스.
 *
 * <p>최종 값 계산 방식 : {@link AbilityStatus#baseValue} × (100+모든 수정자({@link AbilityStatus#modifiers}) 값의 합)/100</p>
 */
public final class AbilityStatus {
    /** 값 수정자 목록 */
    private final HashSet<Modifier> modifiers = new HashSet<>();
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
     * @return {@link AbilityStatus#baseValue} × (100+모든 수정자({@link AbilityStatus#modifiers}) 값의 합)/100
     */
    public double getValue() {
        double valueSum = modifiers.stream().mapToDouble(modifier -> modifier.increment).sum();
        return baseValue * (100 + valueSum) / 100;
    }

    /**
     * 수정자를 추가한다.
     *
     * @param modifier 수정자
     */
    public void addModifier(@NonNull Modifier modifier) {
        modifiers.add(modifier);
    }

    /**
     * 수정자를 제거한다.
     *
     * @param modifier 수정자
     */
    public void removeModifier(@NonNull Modifier modifier) {
        modifiers.remove(modifier);
    }

    /**
     * 모든 수정자를 제거한다.
     */
    public void clearModifiers() {
        modifiers.clear();
    }

    /**
     * 수정자 값 클래스.
     */
    @AllArgsConstructor
    @Getter
    @Setter
    public static final class Modifier {
        /** 수치 증가량 */
        private double increment;
    }
}
