package com.dace.dmgr.combat.character.quaker;

import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import com.dace.dmgr.combat.action.info.PassiveSkillInfo;
import com.dace.dmgr.combat.character.Character;
import com.dace.dmgr.combat.character.Role;
import com.dace.dmgr.combat.character.quaker.action.*;
import com.dace.dmgr.combat.entity.Attacker;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.interaction.DamageType;
import com.dace.dmgr.util.NamedSound;
import com.dace.dmgr.util.SoundUtil;
import com.dace.dmgr.util.StringFormUtil;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Location;
import org.jetbrains.annotations.Nullable;

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
        super("퀘이커", "DVQuaker", Role.GUARDIAN, '\u32D3', 2500, 0.85, 1.8);
    }

    @Override
    @NonNull
    public String getActionbarString(@NonNull CombatUser combatUser) {
        QuakerA1 skill1 = (QuakerA1) combatUser.getSkill(QuakerA1Info.getInstance());

        int skill1Health = skill1.getStateValue();
        int skill1MaxHealth = skill1.getMaxStateValue();

        StringJoiner text = new StringJoiner("    ");

        String skill1Display = StringFormUtil.getActionbarProgressBar(skill1.getSkillInfo().toString(), skill1Health, skill1MaxHealth,
                10, '■');

        if (!skill1.isDurationFinished())
            skill1Display += "  §7[" + skill1.getDefaultActionKeys()[0].getName() + "][" + skill1.getDefaultActionKeys()[1].getName() + "] §f해제";
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
        SoundUtil.playNamedSound(NamedSound.COMBAT_QUAKER_FOOTSTEP, combatUser.getEntity().getLocation(), volume);
    }

    @Override
    public void onDamage(@NonNull CombatUser victim, @Nullable Attacker attacker, int damage, @NonNull DamageType damageType, Location location, boolean isCrit) {
        CombatUtil.playBleedingEffect(location, victim.getEntity(), damage);
    }

    @Override
    public boolean canUseMeleeAttack(@NonNull CombatUser combatUser) {
        return false;
    }

    @Override
    public boolean canSprint(@NonNull CombatUser combatUser) {
        return combatUser.getSkill(QuakerA1Info.getInstance()).isDurationFinished() && combatUser.getSkill(QuakerA2Info.getInstance()).isDurationFinished() &&
                combatUser.getSkill(QuakerA3Info.getInstance()).isDurationFinished() && combatUser.getSkill(QuakerUltInfo.getInstance()).isDurationFinished();
    }

    @Override
    public boolean canFly(@NonNull CombatUser combatUser) {
        return false;
    }

    @Override
    public boolean canJump(@NonNull CombatUser combatUser) {
        return combatUser.getSkill(QuakerA2Info.getInstance()).isDurationFinished() && combatUser.getSkill(QuakerA3Info.getInstance()).isDurationFinished() &&
                combatUser.getSkill(QuakerUltInfo.getInstance()).isDurationFinished();
    }

    @Override
    @NonNull
    public QuakerWeaponInfo getWeaponInfo() {
        return QuakerWeaponInfo.getInstance();
    }

    @Override
    @Nullable
    public PassiveSkillInfo getPassiveSkillInfo(int number) {
        switch (number) {
            default:
                return null;
        }
    }

    @Override
    @Nullable
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
