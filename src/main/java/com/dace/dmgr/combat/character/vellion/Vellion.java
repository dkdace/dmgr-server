package com.dace.dmgr.combat.character.vellion;

import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import com.dace.dmgr.combat.action.info.PassiveSkillInfo;
import com.dace.dmgr.combat.character.Controller;
import com.dace.dmgr.combat.character.silia.action.SiliaUltInfo;
import com.dace.dmgr.combat.character.vellion.action.*;
import com.dace.dmgr.combat.entity.Attacker;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.interaction.DamageType;
import com.dace.dmgr.util.StringFormUtil;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Location;
import org.jetbrains.annotations.Nullable;

import java.util.StringJoiner;

/**
 * 전투원 - 벨리온 클래스.
 *
 * @see VellionWeapon
 * @see VellionP1
 * @see VellionP2
 * @see VellionA1
 * @see VellionA2
 * @see VellionA3
 */
public final class Vellion extends Controller {
    @Getter
    private static final Vellion instance = new Vellion();

    private Vellion() {
        super("벨리온", "DVVellion", '\u32D6', 1000, 1.0, 1.0);
    }

    @Override
    @NonNull
    public String getActionbarString(@NonNull CombatUser combatUser) {
        VellionP1 skillp1 = (VellionP1) combatUser.getSkill(VellionP1Info.getInstance());
        VellionA2 skill2 = (VellionA2) combatUser.getSkill(VellionA2Info.getInstance());

        double skillp1Cooldown = skillp1.getCooldown() / 20.0;
        double skillp1MaxCooldown = skillp1.getDefaultCooldown() / 20.0;
        double skillp1Duration = skillp1.getDuration() / 20.0;
        double skillp1MaxDuration = skillp1.getDefaultDuration() / 20.0;

        StringJoiner text = new StringJoiner("    ");

        String skillp1Display;
        if (skillp1.isDurationFinished())
            skillp1Display = StringFormUtil.getActionbarCooldownBar(skillp1.getSkillInfo().toString(), skillp1Cooldown, skillp1MaxCooldown,
                    10, '■');
        else
            skillp1Display = StringFormUtil.getActionbarDurationBar(skillp1.getSkillInfo().toString(), skillp1Duration, skillp1MaxDuration,
                    10, '■') + "  §7[" + skillp1.getDefaultActionKeys()[0].getName() + "] §f해제";
        text.add(skillp1Display);
        if (!skill2.isDurationFinished() && skill2.isEnabled())
            text.add(skill2.getSkillInfo() + "  §7[" + skill2.getDefaultActionKeys()[0].getName() + "] §f해제");

        return text.toString();
    }

    @Override
    public boolean onAttack(@NonNull CombatUser attacker, @NonNull Damageable victim, int damage, @NonNull DamageType damageType, boolean isCrit) {
        VellionP2 skillp2 = (VellionP2) attacker.getSkill(VellionP2Info.getInstance());
        skillp2.setDamageAmount(damage);
        attacker.useAction(ActionKey.PERIODIC_1);

        return true;
    }

    @Override
    public void onDamage(@NonNull CombatUser victim, @Nullable Attacker attacker, int damage, @NonNull DamageType damageType, Location location, boolean isCrit) {
        super.onDamage(victim, attacker, damage, damageType, location, isCrit);

        CombatUtil.playBleedingEffect(location, victim.getEntity(), damage);
    }

    @Override
    public boolean canUseMeleeAttack(@NonNull CombatUser combatUser) {
        return !((VellionA3) combatUser.getSkill(VellionA3Info.getInstance())).getConfirmModule().isChecking();
    }

    @Override
    public boolean canSprint(@NonNull CombatUser combatUser) {
        return !combatUser.getEntity().isFlying() && combatUser.getSkill(VellionA1Info.getInstance()).isDurationFinished();
    }

    @Override
    public boolean canFly(@NonNull CombatUser combatUser) {
        return combatUser.getSkill(VellionP1Info.getInstance()).isCooldownFinished();
    }

    @Override
    public boolean canJump(@NonNull CombatUser combatUser) {
        return combatUser.getSkill(VellionA1Info.getInstance()).isDurationFinished();
    }

    @Override
    @NonNull
    public VellionWeaponInfo getWeaponInfo() {
        return VellionWeaponInfo.getInstance();
    }

    @Override
    @Nullable
    public PassiveSkillInfo getPassiveSkillInfo(int number) {
        switch (number) {
            case 1:
                return VellionP1Info.getInstance();
            case 2:
                return VellionP2Info.getInstance();
            default:
                return null;
        }
    }

    @Override
    @Nullable
    public ActiveSkillInfo getActiveSkillInfo(int number) {
        switch (number) {
            case 1:
                return VellionA1Info.getInstance();
            case 2:
                return VellionA2Info.getInstance();
            case 3:
                return VellionA3Info.getInstance();
            case 4:
                return SiliaUltInfo.getInstance();
            default:
                return null;
        }
    }

    @Override
    @NonNull
    public SiliaUltInfo getUltimateSkillInfo() {
        return SiliaUltInfo.getInstance();
    }
}
