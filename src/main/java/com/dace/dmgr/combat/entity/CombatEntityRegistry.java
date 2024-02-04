package com.dace.dmgr.combat.entity;

import com.dace.dmgr.Registry;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bukkit.entity.Entity;

import java.util.HashMap;

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

    /**
     * 게임에 소속되지 않은 모든 엔티티를 반환한다.
     *
     * @return 게임에 소속되지 않은 모든 엔티티
     */
    @NonNull
    public CombatEntity[] getAllExcluded() {
        return map.values().stream().filter(combatEntity -> combatEntity.getGame() == null)
                .toArray(CombatEntity[]::new);
    }
}
