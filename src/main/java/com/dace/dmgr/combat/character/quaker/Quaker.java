package com.dace.dmgr.combat.character.quaker;

import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import com.dace.dmgr.combat.action.info.PassiveSkillInfo;
import com.dace.dmgr.combat.character.Character;
import com.dace.dmgr.combat.character.Role;
import com.dace.dmgr.combat.character.quaker.action.*;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.util.SoundUtil;
import com.dace.dmgr.util.StringFormUtil;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Sound;

import java.util.StringJoiner;

/**
 * 전투원 - 퀘이커 클래스.
 *
 * @see QuakerWeapon
 * @see QuakerA1
 * @see QuakerA2
 * @see QuakerA3
 * @see QuakerUlt
 */
public final class Quaker extends Character {
    @Getter
    private static final Quaker instance = new Quaker();

    private Quaker() {
        super("퀘이커", "DVQuaker", Role.GUARDIAN, 2500, 0.85, 1.8);
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
        combatUser.getStatusEffectModule().getResistanceStatus().addModifier("QuakerT1", QuakerT1Info.STATUS_EFFECT_RESISTANCE);
    }

    @Override
    public void onFootstep(@NonNull CombatUser combatUser, double volume) {
        if (!combatUser.getSkill(QuakerA1Info.getInstance()).isDurationFinished())
            volume = 1.4;
        SoundUtil.play(Sound.ENTITY_COW_STEP, combatUser.getEntity().getLocation(), 0.3 * volume, 0.9, 0.1);
        SoundUtil.play("new.entity.ravager.step", combatUser.getEntity().getLocation(), 0.2 * volume, 0.8, 0.1);
    }

    @Override
    public boolean canUseMeleeAttack(@NonNull CombatUser combatUser) {
        return false;
    }

    @Override
    public boolean canSprint(@NonNull CombatUser combatUser) {
        return combatUser.getSkill(QuakerA1Info.getInstance()).isDurationFinished() && combatUser.getSkill(QuakerA2Info.getInstance()).isDurationFinished();
    }

    @Override
    public boolean canJump(@NonNull CombatUser combatUser) {
        return combatUser.getSkill(QuakerA2Info.getInstance()).isDurationFinished();
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
            case 2:
                return QuakerA2Info.getInstance();
            case 3:
                return QuakerA3Info.getInstance();
            case 4:
                return QuakerUltInfo.getInstance();
            default:
                return null;
        }
    }

    @Override
    @NonNull
    public QuakerUltInfo getUltimateSkillInfo() {
        return QuakerUltInfo.getInstance();
    }
}
