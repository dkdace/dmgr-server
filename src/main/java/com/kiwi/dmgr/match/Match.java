package com.kiwi.dmgr.match;

import com.kiwi.dmgr.game.mode.GameMode;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Queue;

@Getter
public abstract class Match {

    /* 매치 대기열 */
    public Queue<Player> matchQueue;

    /* 매치 타입이 가능한 게임 모드 */
    public final ArrayList<GameMode> ableGameMode;

    /* 난입 가능 여부 */
    final boolean isIntrusionAble;

    /* 탈주 시 패널티 여부 */
    final boolean isEscapePenalty;

    /* 게임 결과로 인한 MMR 변동 여부 */
    final boolean isMMRChangeAble;

    /* 게임 결과로 인한 랭크 변동 여부 */
    final boolean isRankChangeAble;

    public Match(ArrayList<GameMode> ableGameMode, boolean isIntrusionAble, boolean isEscapePenalty, boolean isMMRChangeAble, boolean isRankChangeAble) {
        this.ableGameMode = ableGameMode;
        this.isIntrusionAble = isIntrusionAble;
        this.isEscapePenalty = isEscapePenalty;
        this.isMMRChangeAble = isMMRChangeAble;
        this.isRankChangeAble = isRankChangeAble;
    }
}
