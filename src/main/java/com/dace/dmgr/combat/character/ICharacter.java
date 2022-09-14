package com.dace.dmgr.combat.character;

import com.dace.dmgr.combat.SkillController;
import com.dace.dmgr.combat.WeaponController;
import com.dace.dmgr.combat.entity.CombatUser;

public interface ICharacter {
    ICharacterStats getCharacterStats();

    String getName();

    String getSkinName();

    void useWeaponLeft(CombatUser combatUser, WeaponController weaponController);

    void useWeaponRight(CombatUser combatUser, WeaponController weaponController);

    default void usePassive(int number, CombatUser combatUser, SkillController skillController) {
    }

    default void useActive(int number, CombatUser combatUser, SkillController skillController) {
    }
}
