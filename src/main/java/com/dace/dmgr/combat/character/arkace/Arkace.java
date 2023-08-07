package com.dace.dmgr.combat.character.arkace;

import com.dace.dmgr.combat.action.skill.ActiveSkillInfo;
import com.dace.dmgr.combat.action.skill.PassiveSkillInfo;
import com.dace.dmgr.combat.action.skill.UltimateSkillInfo;
import com.dace.dmgr.combat.action.weapon.WeaponInfo;
import com.dace.dmgr.combat.character.Character;
import com.dace.dmgr.combat.character.arkace.action.*;
import lombok.Getter;

/**
 * 전투원 - 아케이스 클래스.
 *
 * @see ArkaceWeapon
 * @see ArkaceP1
 * @see ArkaceA1
 * @see ArkaceA2
 * @see ArkaceUlt
 */
public final class Arkace extends Character {
    @Getter
    private static final Arkace instance = new Arkace();

    private Arkace() {
        super("아케이스", "DVArkace", 1000, 1.0F, 1.0F);
    }

    @Override
    public WeaponInfo getWeaponInfo() {
        return ArkaceWeaponInfo.getInstance();
    }

    @Override
    public PassiveSkillInfo getPassiveSkillInfo(int number) {
        switch (number) {
            case 1:
                return ArkaceP1Info.getInstance();
            default:
                return null;
        }
    }

    @Override
    public ActiveSkillInfo getActiveSkillInfo(int number) {
        switch (number) {
            case 1:
                return ArkaceA1Info.getInstance();
            case 2:
                return ArkaceA2Info.getInstance();
            case 4:
                return ArkaceUltInfo.getInstance();
            default:
                return null;
        }
    }

    @Override
    public UltimateSkillInfo getUltimateSkillInfo() {
        return ArkaceUltInfo.getInstance();
    }
}
