package com.dace.dmgr.combat.character.silia;

import com.dace.dmgr.combat.DamageType;
import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import com.dace.dmgr.combat.action.info.PassiveSkillInfo;
import com.dace.dmgr.combat.character.Character;
import com.dace.dmgr.combat.character.Role;
import com.dace.dmgr.combat.character.silia.action.*;
import com.dace.dmgr.combat.entity.Attacker;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.util.ParticleUtil;
import com.dace.dmgr.util.StringFormUtil;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Location;

import java.util.StringJoiner;

/**
 * 전투원 - 실리아 클래스.
 *
 * @see SiliaWeapon
 * @see SiliaP1
 * @see SiliaP2
 * @see SiliaA1
 * @see SiliaA2
 * @see SiliaA3
 * @see SiliaUlt
 */
public final class Silia extends Character {
    @Getter
    private static final Silia instance = new Silia();

    private Silia() {
        super("실리아", "DVSilia", Role.ASSASSIN, 1000, 1.0, 1.0);
    }

    @Override
    public String getActionbarString(@NonNull CombatUser combatUser) {
        SiliaA3 skill3 = (SiliaA3) combatUser.getSkill(SiliaA3Info.getInstance());

        double skill3Duration = skill3.getStateValue() / 20;
        double skill3MaxDuration = skill3.getMaxStateValue() / 20.0;

        StringJoiner text = new StringJoiner("    ");

        String skill3Display = StringFormUtil.getActionbarDurationBar("§e[폭풍전야]", skill3Duration, skill3MaxDuration,
                10, '■');
        text.add(skill3Display);

        return text.toString();
    }

    @Override
    public void onDamage(@NonNull CombatUser victim, Attacker attacker, int damage, @NonNull DamageType damageType, Location location, boolean isCrit) {
        ParticleUtil.playBleedingEffect(location, victim.getEntity(), damage);
    }

    @Override
    public void onKill(@NonNull CombatUser attacker, @NonNull Damageable victim) {
        SiliaA1 skill1 = (SiliaA1) attacker.getSkill(SiliaA1Info.getInstance());
        SiliaUlt skillUlt = (SiliaUlt) attacker.getSkill(SiliaUltInfo.getInstance());

        if (!skill1.isCooldownFinished() || !skill1.isDurationFinished())
            skill1.setCooldown(2);
        if (!skillUlt.isDurationFinished())
            skillUlt.addDuration(SiliaUltInfo.DURATION_ADD_ON_KILL);
    }

    @Override
    public boolean canUseMeleeAttack(@NonNull CombatUser combatUser) {
        return true;
    }

    @Override
    public boolean canSprint(@NonNull CombatUser combatUser) {
        return true;
    }

    @Override
    public boolean canFly(@NonNull CombatUser combatUser) {
        return combatUser.getSkill(SiliaP1Info.getInstance()).canUse();
    }

    @Override
    public boolean canJump(@NonNull CombatUser combatUser) {
        return true;
    }

    @Override
    @NonNull
    public SiliaWeaponInfo getWeaponInfo() {
        return SiliaWeaponInfo.getInstance();
    }

    @Override
    public PassiveSkillInfo getPassiveSkillInfo(int number) {
        switch (number) {
            case 1:
                return SiliaP1Info.getInstance();
            case 2:
                return SiliaP2Info.getInstance();
            default:
                return null;
        }
    }

    @Override
    public ActiveSkillInfo getActiveSkillInfo(int number) {
        switch (number) {
            case 1:
                return SiliaA1Info.getInstance();
            case 2:
                return SiliaA2Info.getInstance();
            case 3:
                return SiliaA3Info.getInstance();
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
