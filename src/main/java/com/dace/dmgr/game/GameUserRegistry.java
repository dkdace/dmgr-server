package com.dace.dmgr.game;

import com.dace.dmgr.Registry;
import com.dace.dmgr.user.User;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashMap;

/**
 * 게임 시스템의 플레이어({@link GameUser})를 저장하는 클래스.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class GameUserRegistry extends Registry<User, GameUser> {
    @Getter
    private static final GameUserRegistry instance = new GameUserRegistry();
    /** 게임 유저 목록 (유저 정보 : 게임 유저 정보) */
    @Getter(AccessLevel.PROTECTED)
    private final HashMap<User, GameUser> map = new HashMap<>();
}
