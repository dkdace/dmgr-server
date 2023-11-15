package com.dace.dmgr.combat.character.jager;

import com.dace.dmgr.combat.DamageType;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import com.dace.dmgr.combat.action.info.PassiveSkillInfo;
import com.dace.dmgr.combat.action.weapon.Swappable;
import com.dace.dmgr.combat.character.Character;
import com.dace.dmgr.combat.character.Role;
import com.dace.dmgr.combat.character.jager.action.*;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.Living;
import com.dace.dmgr.combat.event.combatuser.CombatUserActionEvent;
import com.dace.dmgr.system.TextIcon;
import com.dace.dmgr.util.SkinUtil;
import com.dace.dmgr.util.StringFormUtil;
import lombok.Getter;
import org.bukkit.Bukkit;

import java.util.StringJoiner;

/**
 * 전투원 - 예거 클래스.
 *
 * @see JagerWeaponL
 * @see JagerWeaponR
 * @see JagerP1
 * @see JagerA1
 * @see JagerA2
 * @see JagerA3
 * @see JagerUlt
 */
public final class Jager extends Character {
    @Getter
    private static final Jager instance = new Jager();

    private Jager() {
        super("예거", SkinUtil.Skin.JAGER, Role.MARKSMAN, 1000, 1.0F, 1.0F);
    }

    @Override
    public String getActionbarString(CombatUser combatUser) {
        JagerWeaponL weapon1 = (JagerWeaponL) combatUser.getWeapon();
        JagerWeaponR weapon2 = ((JagerWeaponL) combatUser.getWeapon()).getSwapModule().getSubweapon();
        JagerA1 skill1 = (JagerA1) combatUser.getSkill(JagerA1Info.getInstance());

        int weapon1Ammo = weapon1.getReloadModule().getRemainingAmmo();
        int weapon2Ammo = weapon2.getReloadModule().getRemainingAmmo();
        float skill1Health = skill1.getStateValue();
        int skill1MaxHealth = skill1.getMaxStateValue();

        StringJoiner text = new StringJoiner("    ");

        String weapon1Display = StringFormUtil.getActionbarProgressBar("" + TextIcon.CAPACITY, weapon1Ammo, JagerWeaponInfo.CAPACITY,
                JagerWeaponInfo.CAPACITY, '*');
        String weapon2Display = StringFormUtil.getActionbarProgressBar("" + TextIcon.CAPACITY, weapon2Ammo, JagerWeaponInfo.SCOPE.CAPACITY,
                JagerWeaponInfo.SCOPE.CAPACITY, '┃');
        String skill1Display = StringFormUtil.getActionbarProgressBar("§e[설랑]", (int) skill1Health, skill1MaxHealth,
                10, '■');
        if (weapon1.getSwapModule().getSwapState() == Swappable.SwapState.PRIMARY)
            weapon1Display = "§a" + weapon1Display;
        else if (weapon1.getSwapModule().getSwapState() == Swappable.SwapState.SECONDARY)
            weapon2Display = "§a" + weapon2Display;
        text.add(weapon1Display);
        text.add(weapon2Display);
        text.add("");
        text.add(skill1Display);

        return text.toString();
    }

    @Override
    public void onTick(CombatUser combatUser, int i) {
        if (i % 5 == 0) {
            CombatUserActionEvent event = new CombatUserActionEvent(combatUser, ActionKey.PERIODIC_1);

            Bukkit.getServer().getPluginManager().callEvent(event);
        }
    }

    @Override
    public boolean onAttack(CombatUser attacker, Damageable victim, int damage, DamageType damageType, boolean isCrit) {
        JagerA1 skill1 = (JagerA1) attacker.getSkill(JagerA1Info.getInstance());
        JagerUlt skillUlt = (JagerUlt) attacker.getSkill(JagerUltInfo.getInstance());

        if (!skill1.isDurationFinished() && victim instanceof Living)
            skill1.getHasEntityModule().getSummonEntity().getEntity().setTarget(victim.getEntity());

        return skillUlt.getHasEntityModule().getSummonEntity() == null;
    }

    @Override
    public JagerWeaponInfo getWeaponInfo() {
        return JagerWeaponInfo.getInstance();
    }

    @Override
    public PassiveSkillInfo getPassiveSkillInfo(int number) {
        switch (number) {
            case 1:
                return JagerP1Info.getInstance();
            default:
                return null;
        }
    }

    @Override
    public ActiveSkillInfo getActiveSkillInfo(int number) {
        switch (number) {
            case 1:
                return JagerA1Info.getInstance();
            case 2:
                return JagerA2Info.getInstance();
            case 3:
                return JagerA3Info.getInstance();
            case 4:
                return JagerUltInfo.getInstance();
            default:
                return null;
        }
    }

    @Override
    public JagerUltInfo getUltimateSkillInfo() {
        return JagerUltInfo.getInstance();
    }
}
