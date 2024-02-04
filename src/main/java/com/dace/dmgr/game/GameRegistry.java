package com.dace.dmgr.game;

import com.dace.dmgr.Registry;
import lombok.AccessLevel;
import lombok.Getter;

import java.util.HashMap;

/**
 * 게임 정보({@link Game})를 저장하는 클래스.
 */
final class GameRegistry extends Registry<Game.KeyPair, Game> {
    @Getter
    private static final GameRegistry instance = new GameRegistry();
    /** 게임 목록 ((랭크 여부 : 방 번호) : 게임) */
    @Getter(AccessLevel.PROTECTED)
    private final HashMap<Game.KeyPair, Game> map = new HashMap<>();
}
