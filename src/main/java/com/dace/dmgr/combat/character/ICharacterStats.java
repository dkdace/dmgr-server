package com.dace.dmgr.combat.character;

import com.dace.dmgr.combat.Weapon;

public interface ICharacterStats {
    int getHealth();

    float getSpeed();

    float getHitbox();

    Weapon getWeapon();

    default ISkill getPassive(int number) {
        return null;
    }

    default ISkill getActive(int number) {
        return null;
    }
}
