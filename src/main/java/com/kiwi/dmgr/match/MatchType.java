package com.kiwi.dmgr.match;

import com.kiwi.dmgr.game.mode.EnumGameMode;
import com.kiwi.dmgr.game.mode.GameMode;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * 매치 타입과 그 정보를 담는 클래스
 */
public enum MatchType {

    UNRANKED(new ArrayList<>(Arrays.asList(EnumGameMode.TeamDeathMatch)), true, false, true, false),
    COMPETITIVE(new ArrayList<>(Arrays.asList(EnumGameMode.TeamDeathMatch)), true, false, true, false);

    /* 매치 타입이 가능한 게임 모드 */
    final private ArrayList<EnumGameMode> ableGameMode;

    /* 난입 가능 여부 */
    final boolean isIntrusionAble;

    /* 탈주 시 패널티 여부 */
    final boolean isEscapePenalty;

    /* 게임 결과로 인한 MMR 변동 여부 */
    final boolean isMMRChangeAble;

    /* 게임 결과로 인한 랭크 변동 여부 */
    final boolean isRankChangeAble;

    MatchType(ArrayList<EnumGameMode> ableGameMode, boolean isIntrusionAble, boolean isEscapePenalty, boolean isMMRChangeAble, boolean isRankChangeAble) {
        this.ableGameMode = ableGameMode;
        this.isIntrusionAble = isIntrusionAble;
        this.isEscapePenalty = isEscapePenalty;
        this.isMMRChangeAble = isMMRChangeAble;
        this.isRankChangeAble = isRankChangeAble;
    }
}
