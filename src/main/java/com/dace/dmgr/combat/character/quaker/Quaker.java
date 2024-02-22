package com.dace.dmgr.combat.character.quaker;

import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import com.dace.dmgr.combat.action.info.PassiveSkillInfo;
import com.dace.dmgr.combat.character.Character;
import com.dace.dmgr.combat.character.Role;
import com.dace.dmgr.combat.character.arkace.action.ArkaceUltInfo;
import com.dace.dmgr.combat.character.quaker.action.QuakerA1;
import com.dace.dmgr.combat.character.quaker.action.QuakerA1Info;
import com.dace.dmgr.combat.character.quaker.action.QuakerWeaponInfo;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.util.StringFormUtil;
import lombok.Getter;
import lombok.NonNull;

import java.util.StringJoiner;

/**
 * 전투원 - 퀘이커 클래스.
 */
public final class Quaker extends Character {
    @Getter
    private static final Quaker instance = new Quaker();

    private Quaker() {
        super("퀘이커", "DVQuaker", Role.GUARDIAN, 2500, 0.85, 1.6);
    }

    @Override
    public String getActionbarString(@NonNull CombatUser combatUser) {
        QuakerA1 skill1 = (QuakerA1) combatUser.getSkill(QuakerA1Info.getInstance());

        double skill1Health = skill1.getStateValue();
        int skill1MaxHealth = skill1.getMaxStateValue();

        StringJoiner text = new StringJoiner("    ");

        String skill1Display = StringFormUtil.getActionbarProgressBar("§e[불굴의 방패]", (int) skill1Health, skill1MaxHealth,
                10, '■');
        text.add(skill1Display);

        return text.toString();
    }

    @Override
    public void onTick(@NonNull CombatUser combatUser, long i) {
    }

    @Override
    public boolean canUseMeleeAttack(@NonNull CombatUser combatUser) {
        return false;
    }

    @Override
    public boolean canSprint(@NonNull CombatUser combatUser) {
        return combatUser.getSkill(QuakerA1Info.getInstance()).isDurationFinished();
    }

    @Override
    @NonNull
    public QuakerWeaponInfo getWeaponInfo() {
        return QuakerWeaponInfo.getInstance();
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
                return QuakerA1Info.getInstance();
            case 4:
                return ArkaceUltInfo.getInstance();
            default:
                return null;
        }
    }

    @Override
    @NonNull
    public ArkaceUltInfo getUltimateSkillInfo() {
        return ArkaceUltInfo.getInstance();
    }
}
