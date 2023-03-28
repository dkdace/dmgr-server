package com.kiwi.dmgr.match;

import com.kiwi.dmgr.game.mode.GameMode;

import java.util.ArrayList;

public enum MatchType {

    UNRANKED(null, true, false, true, false),
    COMPETITIVE(null, true, false, true, false);

    /* 매치 타입이 가능한 게임 모드 */
    final ArrayList<GameMode> ableGameMode;

    /* 난입 가능 여부 */
    final boolean isIntrusionAble;

    /* 탈주 시 패널티 여부 */
    final boolean isEscapePenalty;

    /* 게임 결과로 인한 MMR 변동 여부 */
    final boolean isMMRChangeAble;

    /* 게임 결과로 인한 랭크 변동 여부 */
    final boolean isRankChangeAble;

    MatchType(ArrayList<GameMode> ableGameMode, boolean isIntrusionAble, boolean isEscapePenalty, boolean isMMRChangeAble, boolean isRankChangeAble) {
        this.ableGameMode = ableGameMode;
        this.isIntrusionAble = isIntrusionAble;
        this.isEscapePenalty = isEscapePenalty;
        this.isMMRChangeAble = isMMRChangeAble;
        this.isRankChangeAble = isRankChangeAble;
    }
}
