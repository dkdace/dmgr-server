package com.dace.dmgr.combat.character;

public interface IStats {
    int getHealth();

    float getSpeed();

    float getHitbox();

    default ISkill getPassive1() {
        return null;
    }

    default ISkill getPassive2() {
        return null;
    }

    default ISkill getPassive3() {
        return null;
    }

    default ISkill getActive1() {
        return null;
    }

    default ISkill getActive2() {
        return null;
    }

    default ISkill getActive3() {
        return null;
    }

    default ISkill getUltimate() {
        return null;
    }
}
