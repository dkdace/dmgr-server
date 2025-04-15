package com.dace.dmgr.combat.action.skill.module;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.Timestamp;
import com.dace.dmgr.combat.action.skill.HasBonusScore;
import com.dace.dmgr.combat.entity.Damageable;
import lombok.AllArgsConstructor;
import lombok.NonNull;

import java.util.WeakHashMap;

/**
 * 스킬의 보너스 점수 모듈 클래스.
 *
 * @see HasBonusScore
 */
@AllArgsConstructor
public final class BonusScoreModule {
    /** 스킬 인스턴스 */
    @NonNull
    private final HasBonusScore skill;
    /** 점수 항목 */
    @NonNull
    private final String scoreContext;
    /** 점수 */
    private final int bonusScore;
    /** 처치 점수 제한시간 타임스탬프 목록 (피격자 : 종료 시점) */
    private final WeakHashMap<Damageable, Timestamp> timeLimitTimestampMap = new WeakHashMap<>();

    /**
     * 지정한 대상을 제한 시간 안에 처치했을 때 보너스 점수를 지급하도록 한다.
     *
     * @param target    처치 대상
     * @param timeLimit 처치 제한 시간
     */
    public void addTarget(@NonNull Damageable target, @NonNull Timespan timeLimit) {
        timeLimitTimestampMap.put(target, Timestamp.now().plus(timeLimit));
    }

    /**
     * 적 처치 시 실행할 작업으로, 제한시간 안에 적을 처치하면 보너스 점수를 지급한다.
     *
     * @param victim 피격자
     * @param score  처치 기여 점수
     */
    public void onKill(@NonNull Damageable victim, int score) {
        if (skill.isAssistMode() && score >= 100)
            return;

        Timestamp expiration = timeLimitTimestampMap.get(victim);

        if (expiration != null && expiration.isAfter(Timestamp.now())) {
            skill.getCombatUser().addScore(scoreContext, (skill.isAssistMode() ? bonusScore : bonusScore * score / 100.0));
            timeLimitTimestampMap.remove(victim);
        }
    }
}
