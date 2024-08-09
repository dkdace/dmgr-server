package com.dace.dmgr.combat.interaction;

import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.util.LocationUtil;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.block.Block;

import java.util.HashMap;
import java.util.function.Predicate;

/**
 * 광역 판정. 폭발 피해와 같은 광역 효과의 판정을 관리하는 클래스.
 */
public abstract class Area {
    /** 기본 판정 크기. (단위: 블록) */
    private static final double SIZE = 0.2;

    /** 발사자 엔티티 */
    @NonNull
    @Getter
    protected final CombatEntity shooter;
    /** 범위 (반지름). (단위: 블록) */
    protected final double radius;
    /** 대상 엔티티를 찾는 조건 */
    protected final Predicate<CombatEntity> condition;
    /** 피격자별 관통 가능 여부 목록. (피격자 : 관통 가능 여부) */
    private final HashMap<CombatEntity, Boolean> penetrationMap = new HashMap<>();

    /**
     * 광역 판정 인스턴스를 생성한다.
     *
     * @param shooter   발사자
     * @param radius    범위 (반지름). (단위: 블록). 0 이상의 값
     * @param condition 대상 엔티티를 찾는 조건
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    protected Area(@NonNull CombatEntity shooter, double radius, @NonNull Predicate<@NonNull CombatEntity> condition) {
        if (radius < 0)
            throw new IllegalArgumentException("'radius'가 0 이상이어야 함");

        this.shooter = shooter;
        this.radius = radius;
        this.condition = condition;
    }

    /**
     * 지정한 위치에서 광역 판정을 방출한다.
     *
     * @param center 판정 중심지
     */
    public final void emit(@NonNull Location center) {
        CombatEntity[] targets = CombatUtil.getNearCombatEntities(shooter.getGame(), center, radius, condition);
        for (CombatEntity target : targets)
            this.penetrationMap.put(target, null);

        for (CombatEntity target : targets) {
            new Hitscan(shooter, HitscanOption.builder().size(SIZE).startDistance(0).maxDistance(radius).condition(condition).build()) {
                @Override
                protected boolean onHitBlock(@NonNull Block hitBlock) {
                    return Area.this.onHitBlock(center, getLocation(), hitBlock);
                }

                @Override
                protected boolean onHitEntity(@NonNull Damageable target, boolean isCrit) {
                    Boolean canPenetrate = penetrationMap.get(target);
                    if (canPenetrate == null) {
                        canPenetrate = Area.this.onHitEntity(center, getLocation(), target);
                        penetrationMap.putIfAbsent(target, canPenetrate);
                    }

                    return canPenetrate;
                }
            }.shoot(center, LocationUtil.getDirection(center, target.getHitboxLocation().add(0, target.getEntity().getHeight() / 2, 0)));
        }
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
    protected abstract boolean onHitEntity(@NonNull Location center, @NonNull Location location, @NonNull Damageable target);
}
