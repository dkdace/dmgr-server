package com.dace.dmgr.combat.character.arkace;

import com.dace.dmgr.combat.DamageType;
import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import com.dace.dmgr.combat.action.info.PassiveSkillInfo;
import com.dace.dmgr.combat.character.Character;
import com.dace.dmgr.combat.character.Role;
import com.dace.dmgr.combat.character.arkace.action.*;
import com.dace.dmgr.combat.entity.Attacker;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.util.ParticleUtil;
import com.dace.dmgr.util.StringFormUtil;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Location;

/**
 * 전투원 - 아케이스 클래스.
 *
 * @see ArkaceWeapon
 * @see ArkaceP1
 * @see ArkaceA1
 * @see ArkaceA2
 * @see ArkaceUlt
 */
public final class Arkace extends Character {
    @Getter
    private static final Arkace instance = new Arkace();

    private Arkace() {
        super("아케이스", "DVArkace", Role.MARKSMAN, 1000, 1.0, 1.0);
    }

    @Override
    public String getActionbarString(@NonNull CombatUser combatUser) {
        ArkaceWeapon weapon = (ArkaceWeapon) combatUser.getWeapon();

        int capacity = weapon.getReloadModule().getRemainingAmmo();

        return StringFormUtil.getActionbarProgressBar("" + TextIcon.CAPACITY, capacity, ArkaceWeaponInfo.CAPACITY,
                ArkaceWeaponInfo.CAPACITY, '|');
    }

    @Override
    public void onDamage(@NonNull CombatUser victim, Attacker attacker, int damage, @NonNull DamageType damageType, Location location, boolean isCrit) {
        ParticleUtil.playBleedingEffect(location, victim.getEntity(), damage);
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
    public boolean canJump(@NonNull CombatUser combatUser) {
        return true;
    }

    @Override
    @NonNull
    public ArkaceWeaponInfo getWeaponInfo() {
        return ArkaceWeaponInfo.getInstance();
    }

    @Override
    public PassiveSkillInfo getPassiveSkillInfo(int number) {
        switch (number) {
            case 1:
                return ArkaceP1Info.getInstance();
            default:
                return null;
        }
    }

    @Override
    public ActiveSkillInfo getActiveSkillInfo(int number) {
        switch (number) {
            case 1:
                return ArkaceA1Info.getInstance();
            case 2:
                return ArkaceA2Info.getInstance();
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
