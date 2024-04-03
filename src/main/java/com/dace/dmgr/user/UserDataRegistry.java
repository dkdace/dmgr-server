package com.dace.dmgr.user;

import com.dace.dmgr.Registry;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.HashMap;
import java.util.UUID;

/**
 * 유저 데이터 정보({@link UserData})를 저장하는 클래스.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class UserDataRegistry extends Registry<UUID, UserData> {
    @Getter
    private static final UserDataRegistry instance = new UserDataRegistry();
    /** 유저 데이터 목록 (UUID : 유저 데이터 정보) */
    @Getter(AccessLevel.PROTECTED)
    private final HashMap<UUID, UserData> map = new HashMap<>();

    /**
     * 모든 유저의 데이터 정보를 반환한다.
     *
     * @return 모든 유저 데이터 정보 객체
     */
    @NonNull
    public UserData @NonNull [] getAllUserDatas() {
        return map.values().toArray(new UserData[0]);
    }
}
