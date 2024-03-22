package com.dace.dmgr.combat.character.silia;

import com.dace.dmgr.combat.DamageType;
import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import com.dace.dmgr.combat.action.info.PassiveSkillInfo;
import com.dace.dmgr.combat.character.Character;
import com.dace.dmgr.combat.character.Role;
import com.dace.dmgr.combat.character.quaker.action.QuakerUltInfo;
import com.dace.dmgr.combat.character.silia.action.*;
import com.dace.dmgr.combat.entity.Attacker;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.util.ParticleUtil;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Location;

/**
 * 전투원 - 실리아 클래스.
 *
 * @see SiliaWeapon
 * @see SiliaP1
 * @see SiliaP2
 * @see SiliaA1
 */
public final class Silia extends Character {
    @Getter
    private static final Silia instance = new Silia();

    private Silia() {
        super("실리아", "DVSilia", Role.ASSASSIN, 1000, 1.0, 1.0);
    }

    @Override
    public String getActionbarString(@NonNull CombatUser combatUser) {
        return "";
    }

    @Override
    public void onDamage(@NonNull CombatUser victim, Attacker attacker, int damage, @NonNull DamageType damageType, Location location, boolean isCrit) {
        ParticleUtil.playBleeding(location, victim.getEntity(), damage);
    }

    @Override
    public void onKill(@NonNull CombatUser attacker, @NonNull Damageable victim) {
        SiliaA1 skill1 = (SiliaA1) attacker.getSkill(SiliaA1Info.getInstance());

        if (!skill1.isCooldownFinished() || !skill1.isDurationFinished())
            skill1.setCooldown(2);
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
