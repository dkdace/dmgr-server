package com.dace.dmgr.combat.entity.combatuser;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

import java.text.MessageFormat;

/**
 * 전투 시스템에 사용되는 전투 점수 클래스.
 */
@AllArgsConstructor
@Getter
public final class CombatScore {
    /** 항목 이름 */
    private final String name;
    /** 점수 값 */
    private final double score;

    /**
     * 이름에 포맷을 적용하여 반환한다.
     *
     * <p>지정한 arguments의 n번째 인덱스가 이름에 포함된 '{n}'을 치환한다.</p>
     */
    @NonNull
    public CombatScore formatName(@NonNull Object @NonNull ... arguments) {
        return new CombatScore(MessageFormat.format(name, arguments), score);
    }

    /**
     * 점수 값을 설정하여 반환한다.
     *
     * @param score 점수 값
     * @return 새로운 {@link CombatScore}
     */
    @NonNull
    public CombatScore setScore(double score) {
        return new CombatScore(name, score);
    }

    /**
     * 현재 점수 값에 지정한 값을 곱하여 반환한다.
     *
     * @param value 곱할 값
     * @return 새로운 {@link CombatScore}
     */
    @NonNull
    public CombatScore multiplyScore(double value) {
        return new CombatScore(name, score * value);
    }

    /**
     * 현재 점수 값에 지정한 값을 나누어 반환한다.
     *
     * @param value 나눌 값
     * @return 새로운 {@link CombatScore}
     */
    @NonNull
    public CombatScore divideScore(double value) {
        return new CombatScore(name, score / value);
    }
}
