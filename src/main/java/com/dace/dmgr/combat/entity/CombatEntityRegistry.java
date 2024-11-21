package com.dace.dmgr.combat.entity;

import com.dace.dmgr.Registry;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 전투 시스템의 엔티티({@link CombatEntity})를 저장하는 클래스.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class CombatEntityRegistry extends Registry<Entity, CombatEntity> {
    @Getter
    private static final CombatEntityRegistry instance = new CombatEntityRegistry();
    /** 엔티티 목록 (엔티티 : 전투 시스템 엔티티) */
    @Getter(AccessLevel.PROTECTED)
    private final HashMap<Entity, CombatEntity> map = new HashMap<>();
    /** 게임에 소속되지 않은 모든 엔티티 캐시 값 */
    @Nullable
    private Set<CombatEntity> excludedCache = null;

    @Override
    public void add(@NonNull Entity key, @NonNull CombatEntity value) {
        excludedCache = null;
        super.add(key, value);
    }

    @Override
    public void remove(@NonNull Entity key) {
        excludedCache = null;
        super.remove(key);
    }

    /**
     * 게임에 소속되지 않은 모든 엔티티를 반환한다.
     *
     * @return 게임에 소속되지 않은 모든 엔티티
     */
    @NonNull
    @Unmodifiable
    public Collection<@NonNull CombatEntity> getAllExcluded() {
        if (excludedCache == null)
            excludedCache = map.values().stream().filter(combatEntity -> combatEntity.getGame() == null)
                    .collect(Collectors.toSet());
        return excludedCache;
    }
}
