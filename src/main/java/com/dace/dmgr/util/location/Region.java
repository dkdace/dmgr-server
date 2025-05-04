package com.dace.dmgr.util.location;

import lombok.NonNull;
import org.bukkit.entity.Entity;

/**
 * 지역을 나타내는 인터페이스.
 */
public interface Region {
    /**
     * 지정한 엔티티가 지역 안에 있는지 확인한다.
     *
     * @param entity 확인할 엔티티
     * @return {@code entity}가 지역 내부에 있으면 {@code true} 반환
     */
    boolean isIn(@NonNull Entity entity);
}
