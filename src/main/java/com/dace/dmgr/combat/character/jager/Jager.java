package com.dace.dmgr.combat.character.jager;

import com.dace.dmgr.combat.action.skill.ActiveSkillInfo;
import com.dace.dmgr.combat.action.skill.PassiveSkillInfo;
import com.dace.dmgr.combat.action.skill.UltimateSkillInfo;
import com.dace.dmgr.combat.action.weapon.WeaponInfo;
import com.dace.dmgr.combat.character.Character;
import com.dace.dmgr.combat.character.jager.action.JagerWeaponLInfo;
import com.dace.dmgr.combat.character.jager.action.JagerWeaponInfo;
import lombok.Getter;

/**
 * 전투원 - 예거 클래스.
 */
public final class Jager extends Character {
    @Getter
    private static final Jager instance = new Jager();

    private Jager() {
        super("예거", "DVJager", 1000, 1.0F, 1.0F);
    }

    @Override
    public WeaponInfo getWeaponInfo() {
        return JagerWeaponInfo.getInstance();
    }

    @Override
    public PassiveSkillInfo getPassiveSkillInfo(int number) {
        switch (number) {
            default:
                return null;
        }
    }

    @Override
    public ActiveSkillInfo getActiveSkillInfo(int number) {
        switch (number) {
            default:
                return null;
        }
    }

    @Override
    public UltimateSkillInfo getUltimateSkillInfo() {
        return null;
    }
}
