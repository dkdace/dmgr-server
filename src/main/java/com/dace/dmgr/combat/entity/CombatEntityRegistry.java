package com.dace.dmgr.combat.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 전투 시스템의 엔티티({@link CombatEntity})를 저장하는 클래스.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class CombatEntityRegistry {
    @Getter
    private static final CombatEntityRegistry instance = new CombatEntityRegistry();
    /** 엔티티 목록 (엔티티 : 전투 시스템 엔티티) */
    private static final HashMap<Entity, CombatEntity> COMBAT_ENTITY_MAP = new HashMap<>();

    /** 게임에 소속되지 않은 모든 엔티티 캐시 값 */
    @Nullable
    private Set<CombatEntity> excludedCache = null;

    public void put(@NonNull Entity key, @NonNull CombatEntity value) {
        excludedCache = null;
        COMBAT_ENTITY_MAP.put(key, value);
    }

    @Nullable
    public CombatEntity get(@NonNull Entity key) {
        return COMBAT_ENTITY_MAP.get(key);
    }

    public void remove(@NonNull Entity key) {
        excludedCache = null;
        COMBAT_ENTITY_MAP.remove(key);
    }

    /**
     * 게임에 소속되지 않은 모든 엔티티를 반환한다.
     *
     * @return 게임에 소속되지 않은 모든 엔티티
     */
    @NonNull
    @UnmodifiableView
    public Collection<@NonNull CombatEntity> valuesExcluded() {
        if (excludedCache == null)
            excludedCache = Collections.unmodifiableSet(COMBAT_ENTITY_MAP.values().stream()
                    .filter(combatEntity -> combatEntity.getGame() == null)
                    .collect(Collectors.toSet()));
        return excludedCache;
    }
}
