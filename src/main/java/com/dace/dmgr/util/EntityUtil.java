package com.dace.dmgr.util;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.bukkit.entity.Entity;

/**
 * 엔티티 관련 기능을 제공하는 클래스.
 */
@UtilityClass
public final class EntityUtil {
    /**
     * 지정한 엔티티가 Citizens NPC인지 확인한다.
     *
     * @param entity 확인할 엔티티
     * @return NPC 여부
     */
    public static boolean isCitizensNPC(@NonNull Entity entity) {
        return entity.hasMetadata("NPC");
    }
}
