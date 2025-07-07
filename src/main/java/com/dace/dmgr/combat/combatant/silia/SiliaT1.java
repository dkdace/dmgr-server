package com.dace.dmgr.combat.combatant.silia;

import com.dace.dmgr.combat.ability.Trait;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.util.Vector;

public final class SiliaT1 extends Trait {
    public SiliaT1(@NonNull CombatUser combatUser, @NonNull SiliaT1Info traitInfo) {
        super(combatUser, traitInfo);
    }

    /**
     * 공격의 백어택(치명타) 여부를 확인하여 치명타 배수를 반환한다.
     *
     * @param direction 공격 방향
     * @param victim    피격자
     * @return 백어택 시 {@link SiliaT1Info#CRIT_MULTIPLIER} 반환
     */
    static double getCritMultiplier(@NonNull Vector direction, @NonNull Damageable victim) {
        if (!victim.isCreature())
            return 1;

        Vector dir = direction.clone().normalize().setY(0).normalize();
        Location vloc = victim.getLocation();
        vloc.setPitch(0);
        Vector vdir = vloc.getDirection();

        return dir.distance(vdir) < 0.6 ? SiliaT1Info.CRIT_MULTIPLIER : 1;
    }

    /**
     * 다른 엔티티를 공격했을 때 실행할 작업.
     *
     * @param victim 피격자
     * @param isCrit 치명타 여부
     */
    void onAttack(@NonNull Damageable victim, boolean isCrit) {
        if (combatUser != victim && victim.isGoalTarget() && isCrit)
            combatUser.addScore(SiliaT1Info.CRIT_SCORE);
    }
}
