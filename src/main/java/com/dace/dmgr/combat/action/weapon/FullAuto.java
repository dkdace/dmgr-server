package com.dace.dmgr.combat.action.weapon;

import com.dace.dmgr.combat.action.ActionKey;

/**
 * 쿨타임이 5틱 이하인 연사가 가능한 무기의 인터페이스.
 */
public interface FullAuto {
    static boolean isFireTick(FireRate fireRate, int tick) {
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

    /**
     * @return 연사속도
     */
    FireRate getFireRate();

    /**
     * @return 연사 기능을 적용할 동작 사용 키
     */
    ActionKey getKey();

    /**
     * 지정할 수 있는 연사속도의 목록.
     */
    enum FireRate {
        /** 300/분 (5/초) */
        RPM_300,
        /** 360/분 (6/초) */
        RPM_360,
        /** 420/분 (7/초) */
        RPM_420,
        /** 480/분 (8/초) */
        RPM_480,
        /** 540/분 (9/초) */
        RPM_540,
        /** 600/분 (10/초) */
        RPM_600,
        /** 660/분 (11/초) */
        RPM_660,
        /** 720/분 (12/초) */
        RPM_720,
        /** 780/분 (13/초) */
        RPM_780,
        /** 840/분 (14/초) */
        RPM_840,
        /** 900/분 (15/초) */
        RPM_900,
        /** 960/분 (16/초) */
        RPM_960,
        /** 1020/분 (17/초) */
        RPM_1020,
        /** 1080/분 (18/초) */
        RPM_1080,
        /** 1140/분 (19/초) */
        RPM_1140,
        /** 1200/분 (20/초) */
        RPM_1200
    }
}