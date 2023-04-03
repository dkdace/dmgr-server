package com.kiwi.dmgr.match;

import com.kiwi.dmgr.game.mode.GameMode;

import java.util.ArrayList;

import static com.kiwi.dmgr.game.GameMapList.gameMatchModeList;

/**
 * 매치 타입과 그 정보를 담는 클래스
 */
public enum MatchType {

    UNRANKED(true, false, true, false),
    COMPETITIVE(true, false, true, false);

    /* 매치 타입이 가능한 게임 모드 */
    private ArrayList<GameMode> ableGameMode;

    /* 난입 가능 여부 */
    final boolean isIntrusionAble;

    /* 탈주 시 패널티 여부 */
    final boolean isEscapePenalty;

    /* 게임 결과로 인한 MMR 변동 여부 */
    final boolean isMMRChangeAble;

    /* 게임 결과로 인한 랭크 변동 여부 */
    final boolean isRankChangeAble;

    MatchType(boolean isIntrusionAble, boolean isEscapePenalty, boolean isMMRChangeAble, boolean isRankChangeAble) {
        this.isIntrusionAble = isIntrusionAble;
        this.isEscapePenalty = isEscapePenalty;
        this.isMMRChangeAble = isMMRChangeAble;
        this.isRankChangeAble = isRankChangeAble;
    }
}
