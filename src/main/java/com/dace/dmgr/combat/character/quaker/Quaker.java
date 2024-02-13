package com.dace.dmgr.combat.character.quaker;

import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import com.dace.dmgr.combat.action.info.PassiveSkillInfo;
import com.dace.dmgr.combat.character.Character;
import com.dace.dmgr.combat.character.Role;
import com.dace.dmgr.combat.character.arkace.action.ArkaceUltInfo;
import com.dace.dmgr.combat.character.quaker.action.QuakerWeaponInfo;
import com.dace.dmgr.combat.entity.CombatUser;
import lombok.Getter;
import lombok.NonNull;

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
        return "";
    }

    @Override
    public void onTick(@NonNull CombatUser combatUser, long i) {
    }

    @Override
    public boolean canUseMeleeAttack(@NonNull CombatUser combatUser) {
        return false;
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
