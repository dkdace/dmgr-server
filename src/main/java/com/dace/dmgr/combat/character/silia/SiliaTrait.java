package com.dace.dmgr.combat.character.silia;

import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.Living;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.bukkit.Location;
import org.bukkit.util.Vector;

/**
 * 전투원 - 실리아 특성 클래스.
 */
@UtilityClass
public final class SiliaTrait {
    /**
     * 공격의 백어택(치명타) 여부를 확인한다.
     *
     * @param direction 공격 방향
     * @param victim    피격자
     * @return 백어택 여부
     */
    public static boolean isBackAttack(@NonNull Vector direction, @NonNull Damageable victim) {
        Vector dir = direction.clone().normalize().setY(0).normalize();
        Location vloc = victim.getEntity().getLocation();
        vloc.setPitch(0);
        Vector vdir = vloc.getDirection();

        return victim instanceof Living && dir.distance(vdir) < 0.6;
    }
}
