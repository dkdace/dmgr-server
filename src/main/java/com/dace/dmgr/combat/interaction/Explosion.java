package com.dace.dmgr.combat.interaction;

import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.combat.entity.Damageable;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.Set;
import java.util.function.Predicate;

/**
 * 폭발 판정. 광역 피해의 판정을 관리하는 클래스.
 */
public abstract class Explosion extends Hitscan {
    /** 판정 반경 배수 */
    private static final double HITBOX_MULTIPLIER = 2;
    /** 피격자 목록. 중복 피격을 방지하기 위해 사용함 */
    private final Set<CombatEntity> targets;

    /**
     * 폭발 판정 인스턴스를 생성한다.
     *
     * @param shooter     발사자
     * @param maxDistance 최대 사거리
     * @param targets     피격자 목록. 중복 피격을 방지하기 위해 사용함
     * @param condition   대상 엔티티를 찾는 조건
     */
    protected Explosion(@NonNull CombatEntity shooter, double maxDistance, @NonNull Set<CombatEntity> targets, @NonNull Predicate<CombatEntity> condition) {
        super(shooter, HitscanOption.builder().hitboxMultiplier(HITBOX_MULTIPLIER).maxDistance(maxDistance).condition(condition).build());
        this.targets = targets;
    }

    @Override
    protected final boolean onHitEntity(@NonNull Location location, @NonNull Vector direction, @NonNull Damageable target, boolean isCrit) {
        if (targets.add(target))
            return onHitEntityExplosion(location, target);

        return false;
    }

    /**
     * 폭발이 엔티티에 맞았을 때 실행될 작업.
     *
     * @param location 맞은 위치
     * @param target   맞은 엔티티
     * @return 관통 여부. {@code true} 반환 시 엔티티 관통
     */
    protected abstract boolean onHitEntityExplosion(@NonNull Location location, @NonNull Damageable target);
}
