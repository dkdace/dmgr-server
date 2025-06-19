package com.dace.dmgr.combat.entity.module;

import com.dace.dmgr.combat.entity.CombatEntity;
import lombok.NonNull;

import java.util.HashSet;

/**
 * 전투 시스템 엔티티의 모듈 클래스.
 *
 * <p>능력치 값을 가지고 있으며 수정자({@link Modifier})를 통해 최종 값을 변경할 수 있다.</p>
 *
 * @param <T> {@link CombatEntity}를 상속받는 전투 시스템 엔티티
 * @see Modifier
 */
public abstract class CombatEntityModule<T extends CombatEntity> {
    /** 엔티티 인스턴스 */
    protected final T combatEntity;
    /** 값 수정자 목록 */
    private final HashSet<Modifier> modifiers = new HashSet<>();

    /**
     * 전투 시스템 엔티티의 모듈 인스턴스를 생성한다.
     *
     * @param combatEntity 대상 엔티티
     */
    CombatEntityModule(@NonNull T combatEntity) {
        this.combatEntity = combatEntity;
    }

    /**
     * 능력치의 기본값을 반환한다.
     *
     * @return 능력치 기본값
     */
    protected abstract double getBaseValue();

    /**
     * 능력치의 기본값에 모든 수정자를 적용한 최종 값을 반환한다.
     *
     * @return {@link CombatEntityModule#getBaseValue()} × (100+모든 수정자({@link CombatEntityModule#modifiers}) 값의 합)/100
     */
    public final double getValue() {
        double valueSum = modifiers.stream().mapToDouble(Modifier::getIncrement).sum();
        return getBaseValue() * (100 + valueSum) / 100;
    }

    /**
     * 능력치에 수정자를 추가한다.
     *
     * @param modifier 수정자
     */
    public final void addModifier(@NonNull Modifier modifier) {
        modifiers.add(modifier);
    }

    /**
     * 능력치에서 수정자를 제거한다.
     *
     * @param modifier 수정자
     */
    public final void removeModifier(@NonNull Modifier modifier) {
        modifiers.remove(modifier);
    }

    /**
     * 능력치의 모든 수정자를 제거한다.
     */
    public final void clearModifiers() {
        modifiers.clear();
    }
}
