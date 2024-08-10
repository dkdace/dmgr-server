package com.dace.dmgr.combat.action.weapon.module;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.weapon.FullAuto;
import com.dace.dmgr.util.CooldownUtil;
import lombok.NonNull;

/**
 * 무기의 점진적 탄퍼짐 모듈 클래스.
 *
 * <p>연사를 지속할 수록 탄퍼짐이 증가하는 시스템을 말한다.</p>
 *
 * <p>무기가 {@link FullAuto}를 상속받는 클래스여야 한다.</p>
 *
 * @see FullAuto
 */
public final class GradualSpreadModule extends FullAutoModule {
    /** 쿨타임 ID */
    private static final String COOLDOWN_ID = "SpreadRecovery";

    /** 사용 횟수당 탄퍼짐 증가량 */
    private final double spreadIncrement;
    /** 점진적 탄퍼짐이 적용되는 시점 (사용 횟수) */
    private final int shotsToStartSpread;
    /** 최대 탄퍼짐에 도달하는 시점 (사용 횟수) */
    private final int shotsToReachMaxSpread;

    /** 연속으로 무기를 사용한 횟수 */
    private int continuousShots = 0;

    /**
     * 점진적 탄퍼짐 모듈 인스턴스를 생성한다.
     *
     * @param weapon                대상 무기
     * @param fullAutoKey           연사 기능을 적용할 동작 사용 키
     * @param fireRate              연사속도
     * @param spreadIncrement       사용 횟수당 탄퍼짐 증가량. 0 이상의 값
     * @param shotsToStartSpread    점진적 탄퍼짐이 적용되는 시점 (사용 횟수). 0 이상의 값
     * @param shotsToReachMaxSpread 최대 탄퍼짐에 도달하는 시점 (사용 횟수). 0 이상의 값
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    public GradualSpreadModule(@NonNull FullAuto weapon, @NonNull ActionKey fullAutoKey, @NonNull FullAuto.FireRate fireRate,
                               double spreadIncrement, int shotsToStartSpread, int shotsToReachMaxSpread) {
        super(weapon, fullAutoKey, fireRate);
        if (spreadIncrement < 0 || shotsToStartSpread < 0 || shotsToReachMaxSpread < 0)
            throw new IllegalArgumentException("'spreadIncrement', 'shotsToStartSpread' 및 'shotsToReachMaxSpread'가 0 이상이어야 함");

        this.spreadIncrement = spreadIncrement;
        this.shotsToStartSpread = shotsToStartSpread;
        this.shotsToReachMaxSpread = shotsToReachMaxSpread;
    }

    /**
     * 무기 사용 횟수를 증가시키고 현재 탄퍼짐을 반환한다.
     *
     * @return 탄퍼짐 수치. 연속으로 사용하지 않으면 자동으로 0이 됨
     */
    public double increaseSpread() {
        if (CooldownUtil.getCooldown(weapon, COOLDOWN_ID) == 0)
            continuousShots = 0;

        continuousShots++;
        CooldownUtil.setCooldown(weapon, COOLDOWN_ID, 4);

        int shots = Math.min(continuousShots, shotsToReachMaxSpread) - shotsToStartSpread;
        if (shots <= 0)
            return 0;
        return shots * spreadIncrement;
    }
}
