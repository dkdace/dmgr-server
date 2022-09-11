package com.dace.dmgr.combat.character;

import com.dace.dmgr.combat.Weapon;
import com.dace.dmgr.combat.entity.CombatUser;

public interface ICharacter {
    IStats getStats();

    Weapon getWeapon();

    String getName();

    String getSkinName();

    void useWeaponLeft(CombatUser combatUser);

    void useWeaponRight(CombatUser combatUser);

    default void useSkill1(CombatUser combatUser) {
    }

    default void useSkill2(CombatUser combatUser) {
    }

    default void useSkill3(CombatUser combatUser) {
    }

    void useUltimate(CombatUser combatUser);
}
