package com.dace.dmgr.combat.character.jager;

import com.dace.dmgr.combat.action.skill.ActiveSkillInfo;
import com.dace.dmgr.combat.action.skill.PassiveSkillInfo;
import com.dace.dmgr.combat.action.skill.UltimateSkillInfo;
import com.dace.dmgr.combat.action.weapon.SwapModule;
import com.dace.dmgr.combat.action.weapon.WeaponInfo;
import com.dace.dmgr.combat.character.Character;
import com.dace.dmgr.combat.character.arkace.action.ArkaceUltInfo;
import com.dace.dmgr.combat.character.jager.action.*;
import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.system.TextIcon;
import com.dace.dmgr.util.StringFormUtil;
import lombok.Getter;

import java.util.StringJoiner;

/**
 * 전투원 - 예거 클래스.
 *
 * @see JagerWeaponL
 * @see JagerWeaponR
 * @see JagerA1
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
        JagerA1 skill1 = (JagerA1) combatUser.getSkill(JagerA1Info.getInstance());

        int weapon1Ammo = weapon1.getRemainingAmmo();
        int weapon1Capacity = weapon1.getCapacity();
        int weapon2Ammo = weapon2.getRemainingAmmo();
        int weapon2Capacity = weapon2.getCapacity();
        float skill1Health = skill1.getStateValue();
        int skill1MaxHealth = skill1.getMaxStateValue();

        StringJoiner text = new StringJoiner("    ");

        String weapon1Display = StringFormUtil.getActionbarProgressBar("" + TextIcon.CAPACITY, weapon1Ammo, weapon1Capacity, weapon1Capacity, '*');
        String weapon2Display = StringFormUtil.getActionbarProgressBar("" + TextIcon.CAPACITY, weapon2Ammo, weapon2Capacity, weapon2Capacity, '┃');
        String skill1Display = StringFormUtil.getActionbarProgressBar("§e[설랑]", (int) skill1Health, skill1MaxHealth, 10, '■');
        if (weapon1.getWeaponState() == SwapModule.WeaponState.PRIMARY)
            weapon1Display = "§a" + weapon1Display;
        else if (weapon1.getWeaponState() == SwapModule.WeaponState.SECONDARY)
            weapon2Display = "§a" + weapon2Display;
        text.add(weapon1Display);
        text.add(weapon2Display);
        text.add("");
        text.add(skill1Display);

        return text.toString();
    }

    @Override
    public void onAttack(CombatUser attacker, CombatEntity<?> victim, int damage, String type, boolean isCrit, boolean isUlt) {
        JagerA1 skill1 = (JagerA1) attacker.getSkill(JagerA1Info.getInstance());

        if (!skill1.isDurationFinished())
            skill1.getSummonEntities().get(0).getEntity().setTarget(victim.getEntity());
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
            case 1:
                return JagerA1Info.getInstance();
            default:
                return null;
        }
    }

    @Override
    public UltimateSkillInfo getUltimateSkillInfo() {
        return null;
    }
}
