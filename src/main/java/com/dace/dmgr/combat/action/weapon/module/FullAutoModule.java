package com.dace.dmgr.combat.action.weapon.module;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.ActionModule;
import com.dace.dmgr.combat.action.weapon.FullAuto;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 무기의 연사 모듈 클래스.
 */
@RequiredArgsConstructor
public final class FullAutoModule implements ActionModule {
    /** 무기 객체 */
    private final FullAuto weapon;
    /** 연사 기능을 적용할 동작 사용 키 */
    @Getter
    private final ActionKey fullAutoKey;
    /** 연사속도 */
    private final FullAuto.FireRate fireRate;

    /**
     * 틱을 기준으로 발사할 수 있는 시점을 확인한다.
     *
     * @param tick 기준 틱
     * @return 발사 가능 여부
     */
    public boolean isFireTick(int tick) {
        tick = tick % 20 + 1;

        switch (fireRate) {
            case RPM_300:
                return tick % 4 == 1;
            case RPM_360:
                tick %= 7;
                return tick == 1 || tick == 4;
            case RPM_420:
                return tick % 3 == 1;
            case RPM_480:
                tick %= 5;
                return tick == 1 || tick == 3;
            case RPM_540:
                tick %= 7;
                return tick == 1 || tick == 3 || tick == 5;
            case RPM_600:
                return tick % 2 == 1;
            case RPM_660:
                return tick == 2 || tick % 2 == 1;
            case RPM_720:
                tick %= 5;
                return tick == 1 || tick == 2 || tick == 4;
            case RPM_780:
                tick %= 6;
                return tick != 2 && tick != 0;
            case RPM_840:
                return tick % 3 != 0;
            case RPM_900:
                return tick % 4 != 0;
            case RPM_960:
                return tick % 5 != 0;
            case RPM_1020:
                return tick % 6 != 0;
            case RPM_1080:
                return tick % 10 != 0;
            case RPM_1140:
                return tick != 20;
            case RPM_1200:
            default:
                return true;
        }
    }
}
