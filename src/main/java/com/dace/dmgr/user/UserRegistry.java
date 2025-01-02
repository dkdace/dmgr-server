package com.dace.dmgr.user;

import com.dace.dmgr.Registry;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

/**
 * 유저 정보({@link User})를 저장하는 클래스.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class UserRegistry extends Registry<Player, User> {
    @Getter
    private static final UserRegistry instance = new UserRegistry();
    /** 유저 목록 (플레이어 : 유저 정보) */
    @Getter(AccessLevel.PROTECTED)
    private final HashMap<Player, User> map = new HashMap<>();

    /**
     * 모든 유저 정보를 반환한다.
     *
     * @return 모든 유저 정보 인스턴스
     */
    @NonNull
    @UnmodifiableView
    public Collection<@NonNull User> getAllUsers() {
        return Collections.unmodifiableCollection(map.values());
    }
}
