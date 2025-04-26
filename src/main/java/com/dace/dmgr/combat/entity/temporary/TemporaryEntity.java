package com.dace.dmgr.combat.entity.temporary;

import com.dace.dmgr.combat.entity.AbstractCombatEntity;
import com.dace.dmgr.combat.entity.temporary.spawnhandler.EntitySpawnHandler;
import com.dace.dmgr.combat.interaction.Hitbox;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

/**
 * 전투 시스템의 일시적인 엔티티 클래스.
 *
 * <p>설랑, 포탈 등 전투에서 일시적으로 사용하는 엔티티를 말한다.</p>
 *
 * @param <T> {@link Entity}를 상속받는 엔티티 타입
 * @see SummonEntity
 */
public abstract class TemporaryEntity<T extends Entity> extends AbstractCombatEntity<T> {
    /**
     * 일시적 엔티티 인스턴스를 생성한다.
     *
     * @param entitySpawnHandler 엔티티 생성 처리기
     * @param spawnLocation      생성 위치
     * @param name               이름
     * @param hitboxes           히트박스 목록
     * @throws IllegalStateException {@code spawnLocation}에 엔티티를 소환할 수 없으면 발생
     * @see EntitySpawnHandler
     */
    protected TemporaryEntity(@NonNull EntitySpawnHandler<T> entitySpawnHandler, @NonNull Location spawnLocation, @NonNull String name,
                              @NonNull Hitbox @NonNull ... hitboxes) {
        super(entitySpawnHandler.createEntity(spawnLocation), name, hitboxes);

        entitySpawnHandler.onSpawn(this);
        addOnRemove(entity::remove);
    }

    @Override
    public boolean canBeTargeted() {
        return true;
    }
}
