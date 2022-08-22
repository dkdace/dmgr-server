package com.dace.dmgr.combat.character;

import com.dace.dmgr.combat.entity.CombatUser;

public interface ICharacter {
    default void useWeaponLeft(CombatUser combatUser) {
    }

    default void useWeaponRight(CombatUser combatUser) {
    }

    default void useSkill1(CombatUser combatUser) {
    }

    default void useSkill2(CombatUser combatUser) {
    }

    default void useSkill3(CombatUser combatUser) {
    }

    void useSkill4(CombatUser combatUser);
}
