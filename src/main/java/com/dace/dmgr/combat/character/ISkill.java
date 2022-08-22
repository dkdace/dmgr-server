package com.dace.dmgr.combat.character;

public interface ISkill {
    default long getCooldown() {
        return 0;
    }

    default int getCost() {
        return 0;
    }
}
