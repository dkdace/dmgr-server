package com.dace.dmgr.combat.character.jager;

import com.dace.dmgr.combat.action.skill.ActiveSkillInfo;
import com.dace.dmgr.combat.action.skill.PassiveSkillInfo;
import com.dace.dmgr.combat.action.skill.UltimateSkillInfo;
import com.dace.dmgr.combat.action.weapon.SwapModule;
import com.dace.dmgr.combat.action.weapon.WeaponInfo;
import com.dace.dmgr.combat.character.Character;
import com.dace.dmgr.combat.character.jager.action.JagerWeaponLInfo;
import com.dace.dmgr.combat.character.jager.action.JagerWeaponInfo;
import com.dace.dmgr.combat.character.jager.action.JagerWeaponL;
import com.dace.dmgr.combat.character.jager.action.JagerWeaponR;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.system.TextIcon;
import com.dace.dmgr.util.StringFormUtil;
import lombok.Getter;

import java.util.StringJoiner;

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
    public String getActionbarString(CombatUser combatUser) {
        JagerWeaponL weapon1 = (JagerWeaponL) combatUser.getWeapon();
        JagerWeaponR weapon2 = ((JagerWeaponL) combatUser.getWeapon()).getSubweapon();

        int capacity1 = weapon1.getRemainingAmmo();
        int maxCapacity1 = weapon1.getCapacity();
        int capacity2 = weapon2.getRemainingAmmo();
        int maxCapacity2 = weapon2.getCapacity();

        StringJoiner text = new StringJoiner("    ");

        String ammo1 = StringFormUtil.getActionbarProgressBar(TextIcon.CAPACITY, capacity1, maxCapacity1, maxCapacity1, '*');
        String ammo2 = StringFormUtil.getActionbarProgressBar(TextIcon.CAPACITY, capacity2, maxCapacity2, maxCapacity2, '┃');
        if (weapon1.getWeaponState() == SwapModule.WeaponState.PRIMARY)
            ammo1 = "§a" + ammo1;
        else if (weapon1.getWeaponState() == SwapModule.WeaponState.SECONDARY)
            ammo2 = "§a" + ammo2;
        text.add(ammo1);
        text.add(ammo2);

        return text.toString();
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
