package com.dace.dmgr.combat.action.weapon;

import com.dace.dmgr.combat.action.weapon.module.FullAutoModule;

/**
 * 쿨타임이 5틱 이하인 연사가 가능한 무기의 인터페이스.
 */
public interface FullAuto extends Weapon {
    /**
     * @return 연사 모듈
     */
    FullAutoModule getFullAutoModule();

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