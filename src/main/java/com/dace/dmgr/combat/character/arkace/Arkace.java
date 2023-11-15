package com.dace.dmgr.combat.character.arkace;

import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import com.dace.dmgr.combat.action.info.PassiveSkillInfo;
import com.dace.dmgr.combat.character.Character;
import com.dace.dmgr.combat.character.Role;
import com.dace.dmgr.combat.character.arkace.action.*;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.system.TextIcon;
import com.dace.dmgr.util.SkinUtil;
import com.dace.dmgr.util.StringFormUtil;
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
        super("아케이스", SkinUtil.Skin.ARKACE, Role.MARKSMAN, 1000, 1.0F, 1.0F);
    }

    @Override
    public String getActionbarString(CombatUser combatUser) {
        ArkaceWeapon weapon = (ArkaceWeapon) combatUser.getWeapon();

        int capacity = weapon.getReloadModule().getRemainingAmmo();

        return StringFormUtil.getActionbarProgressBar("" + TextIcon.CAPACITY, capacity, ArkaceWeaponInfo.CAPACITY,
                ArkaceWeaponInfo.CAPACITY, '|');
    }

    @Override
    public ArkaceWeaponInfo getWeaponInfo() {
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
    public ArkaceUltInfo getUltimateSkillInfo() {
        return ArkaceUltInfo.getInstance();
    }
}
