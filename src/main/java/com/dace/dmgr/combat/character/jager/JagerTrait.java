package com.dace.dmgr.combat.character.jager;

import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.combat.entity.Property;
import com.dace.dmgr.system.Cooldown;
import com.dace.dmgr.system.CooldownManager;

/**
 * 전투원 - 예거 특성 클래스.
 */
public final class JagerTrait {
    /**
     * 피격자의 빙결 수치를 증가시킨다.
     *
     * @param victim 피격자
     * @param amount 증가량
     */
    public static void addFreezeValue(CombatEntity<?> victim, int amount) {
        victim.getPropertyManager().addValue(Property.FREEZE, amount);
        CooldownManager.setCooldown(victim, Cooldown.FREEZE_VALUE_DURATION);
    }
}
