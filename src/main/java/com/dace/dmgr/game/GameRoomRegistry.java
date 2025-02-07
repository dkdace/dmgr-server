package com.dace.dmgr.game;

import com.dace.dmgr.Registry;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;

/**
 * 게임 방 정보({@link GameRoom})를 저장하는 클래스.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class GameRoomRegistry extends Registry<Pair<Boolean, Integer>, GameRoom> {
    @Getter
    private static final GameRoomRegistry instance = new GameRoomRegistry();
    /** 게임 방 목록 ((랭크 여부 : 방 번호) : 게임 방) */
    @Getter(AccessLevel.PROTECTED)
    private final HashMap<Pair<Boolean, Integer>, GameRoom> map = new HashMap<>();
}
