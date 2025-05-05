package com.dace.dmgr.combat.interaction;

import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.combat.entity.EntityCondition;
import com.dace.dmgr.util.location.LocationUtil;
import lombok.Getter;
import lombok.NonNull;
import org.apache.commons.lang3.Validate;
import org.bukkit.Location;
import org.bukkit.block.Block;

import java.util.HashMap;
import java.util.Set;

/**
 * 광역 판정. 폭발 피해와 같은 광역 효과의 판정을 관리하는 클래스.
 *
 * @param <T> {@link CombatEntity}를 상속받는 전투 시스템 엔티티
 */
public abstract class Area<T extends CombatEntity> {
    /** 기본 판정 크기. (단위: 블록) */
    private static final double SIZE = 0.2;

    /** 발사자 엔티티 */
    @NonNull
    @Getter
    protected final CombatEntity shooter;
    /** 범위 (반지름). (단위: 블록) */
    protected final double radius;
    /** 대상 엔티티를 찾는 조건 */
    protected final EntityCondition<T> entityCondition;
    /** 피격자별 관통 가능 여부 목록 (피격자 : 관통 가능 여부) */
    private final HashMap<T, Boolean> penetrationMap = new HashMap<>();

    /** 실행 여부 */
    private boolean isUsed = false;

    /**
     * 광역 판정 인스턴스를 생성한다.
     *
     * @param shooter         발사자
     * @param radius          범위 (반지름). (단위: 블록). 0 이상의 값
     * @param entityCondition 대상 엔티티를 찾는 조건
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    protected Area(@NonNull CombatEntity shooter, double radius, @NonNull EntityCondition<T> entityCondition) {
        Validate.isTrue(radius >= 0, "radius >= 0 (%f)", radius);

        this.shooter = shooter;
        this.radius = radius;
        this.entityCondition = entityCondition;
    }

    /**
     * 지정한 위치에서 광역 판정을 실행한다.
     *
     * @param center 판정 중심지
     */
    public final void emit(@NonNull Location center) {
        if (isUsed)
            return;

        isUsed = true;
        Set<T> targets = CombatUtil.getNearCombatEntities(center, radius, entityCondition);
        for (T target : targets)
            penetrationMap.put(target, null);

        for (T target : targets)
            new Hitscan<T>(shooter, entityCondition, Hitscan.Option.builder().size(SIZE).startDistance(0).maxDistance(radius).build()) {
                @Override
                @NonNull
                protected IntervalHandler getIntervalHandler() {
                    return (location, i) -> true;
                }

                @Override
                @NonNull
                protected HitBlockHandler getHitBlockHandler() {
                    return (location, hitBlock) -> onHitBlock(center.clone(), location.clone(), hitBlock);
                }

                @Override
                @NonNull
                protected HitEntityHandler<T> getHitEntityHandler() {
                    return (location, areaTarget) -> {
                        Boolean canPenetrate = penetrationMap.get(areaTarget);
                        if (canPenetrate == null) {
                            canPenetrate = onHitEntity(center.clone(), location.clone(), areaTarget);
                            penetrationMap.putIfAbsent(areaTarget, canPenetrate);
                        }

                        return canPenetrate;
                    };
                }
            }.shot(center, LocationUtil.getDirection(center, target.getHitboxCenter()));
    }

    /**
     * 광역 판정이 블록에 맞았을 때 실행될 작업.
     *
     * @param center   판정 중심지
     * @param location 맞은 위치
     * @param hitBlock 맞은 블록
     * @return 관통 여부. {@code true} 반환 시 블록 관통
     */
    protected abstract boolean onHitBlock(@NonNull Location center, @NonNull Location location, @NonNull Block hitBlock);

    /**
     * 광역 판정이 엔티티에 맞았을 때 실행될 작업.
     *
     * @param center   판정 중심지
     * @param location 맞은 위치
     * @param target   맞은 엔티티
     * @return 관통 여부. {@code true} 반환 시 엔티티 관통
     */
    protected abstract boolean onHitEntity(@NonNull Location center, @NonNull Location location, @NonNull T target);
}
