package com.dace.dmgr.combat.character;

import com.dace.dmgr.combat.action.*;

public interface ICharacter {
    String getName();

    String getSkinName();

    int getHealth();

    float getSpeed();

    float getHitbox();

    ActionKeyMap getActionKeyMap();

    Weapon getWeapon();

    default PassiveSkill getPassive(int number) {
        return null;
    }

    default ActiveSkill getActive(int number) {
        return null;
    }

    UltimateSkill getUltimate();
}
