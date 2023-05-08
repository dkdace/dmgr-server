package com.dace.dmgr.combat.entity;

import lombok.Getter;
import org.bukkit.entity.LivingEntity;

/**
 * 전투에서 일시적으로 사용하는 엔티티 중 플레이어가 소환할 수 있는 엔티티 클래스.
 */
public abstract class SummonEntity<T extends LivingEntity> extends TemporalEntity<T> {
    /** 엔티티를 소환한 플레이어 */
    @Getter
    protected CombatUser owner;

    /**
     * @param name       이름
     * @param hitbox     히트박스
     * @param critHitbox 치명타 히트박스
     * @param isFixed    위치 고정 여부
     * @param maxHealth  최대 체력
     * @param owner      엔티티를 소환한 플레이어
     */
    protected SummonEntity(String name, Hitbox hitbox, Hitbox critHitbox, boolean isFixed, int maxHealth, CombatUser owner) {
        super(name, hitbox, critHitbox, isFixed, maxHealth);
        this.owner = owner;
    }
}
