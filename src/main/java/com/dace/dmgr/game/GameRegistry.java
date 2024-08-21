package com.dace.dmgr.game;

import com.dace.dmgr.Registry;
import lombok.*;

import java.util.HashMap;

/**
 * 게임 정보({@link Game})를 저장하는 클래스.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class GameRegistry extends Registry<GameRegistry.KeyPair, Game> {
    @Getter
    private static final GameRegistry instance = new GameRegistry();
    /** 게임 목록 ((랭크 여부 : 방 번호) : 게임) */
    @Getter(AccessLevel.PROTECTED)
    private final HashMap<KeyPair, Game> map = new HashMap<>();

    /**
     * 게임 정보의 키 값으로 사용되는 클래스.
     */
    @AllArgsConstructor(access = AccessLevel.PACKAGE)
    @EqualsAndHashCode
    static final class KeyPair {
        /** 랭크 여부 */
        private final boolean isRanked;
        /** 방 번호 */
        private final int number;
    }
}
