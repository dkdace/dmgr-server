package com.dace.dmgr.combat.action.weapon.module;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.Timestamp;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.weapon.FullAuto;
import lombok.NonNull;
import org.apache.commons.lang3.Validate;

/**
 * 무기의 점진적 탄퍼짐 모듈 클래스.
 *
 * <p>연사를 지속할 수록 탄퍼짐이 증가하는 시스템을 말한다.</p>
 *
 * @see FullAuto
 */
public final class GradualSpreadModule extends FullAutoModule {
    /** 사용 횟수당 탄퍼짐 증가량 */
    private final double spreadIncrement;
    /** 점진적 탄퍼짐이 적용되는 시점 (사용 횟수) */
    private final int shotsToStartSpread;
    /** 최대 탄퍼짐에 도달하는 시점 (사용 횟수) */
    private final int shotsToReachMaxSpread;

    /** 탄퍼짐 복구 타임스탬프 */
    private Timestamp recoveryTimestamp = Timestamp.now();
    /** 연속으로 무기를 사용한 횟수 */
    private int continuousShots = 0;

    /**
     * 점진적 탄퍼짐 모듈 인스턴스를 생성한다.
     *
     * @param weapon                대상 무기
     * @param fullAutoKey           연사 기능을 적용할 동작 사용 키
     * @param fireRate              연사속도
     * @param spreadIncrement       사용 횟수당 탄퍼짐 증가량. 0 이상의 값
     * @param shotsToStartSpread    점진적 탄퍼짐이 적용되는 시점 (사용 횟수). 1 이상의 값
     * @param shotsToReachMaxSpread 최대 탄퍼짐에 도달하는 시점 (사용 횟수). 1 이상의 값
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    public GradualSpreadModule(@NonNull FullAuto weapon, @NonNull ActionKey fullAutoKey, @NonNull FullAuto.FireRate fireRate,
                               double spreadIncrement, int shotsToStartSpread, int shotsToReachMaxSpread) {
        super(weapon, fullAutoKey, fireRate);
        Validate.isTrue(spreadIncrement >= 0, "spreadIncrement >= 0 (%f)", spreadIncrement);
        Validate.isTrue(shotsToStartSpread >= 1, "shotsToStartSpread >= 1 (%d)", shotsToStartSpread);
        Validate.isTrue(shotsToReachMaxSpread >= 1, "shotsToReachMaxSpread >= 1 (%d)", shotsToReachMaxSpread);

        this.spreadIncrement = spreadIncrement;
        this.shotsToStartSpread = shotsToStartSpread;
        this.shotsToReachMaxSpread = shotsToReachMaxSpread;
    }

    /**
     * 현재 탄퍼짐 수치를 증가시키고 반환한다.
     *
     * @return 탄퍼짐 수치. 연속으로 사용하지 않으면 자동으로 초기화됨
     */
    public double increaseSpread() {
        if (recoveryTimestamp.isBefore(Timestamp.now()))
            continuousShots = 0;

        continuousShots++;
        recoveryTimestamp = Timestamp.now().plus(Timespan.ofTicks(4));

        int shots = Math.max(0, Math.min(continuousShots, shotsToReachMaxSpread) - shotsToStartSpread);
        return shots * spreadIncrement;
    }
}
