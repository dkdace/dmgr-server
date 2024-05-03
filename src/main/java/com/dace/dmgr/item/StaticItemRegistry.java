package com.dace.dmgr.item;

import com.dace.dmgr.Registry;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashMap;

/**
 * 정적 아이템({@link StaticItem})을 저장하는 클래스.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class StaticItemRegistry extends Registry<String, StaticItem> {
    @Getter
    private static final StaticItemRegistry instance = new StaticItemRegistry();
    /** 정적 아이템 목록 (식별자 : 정적 아이템) */
    @Getter(AccessLevel.PROTECTED)
    private final HashMap<String, StaticItem> map = new HashMap<>();
}
