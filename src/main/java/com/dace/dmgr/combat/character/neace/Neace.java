package com.dace.dmgr.combat.character.neace;

import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import com.dace.dmgr.combat.action.info.PassiveSkillInfo;
import com.dace.dmgr.combat.character.Support;
import com.dace.dmgr.combat.character.neace.action.NeaceWeapon;
import com.dace.dmgr.combat.character.neace.action.NeaceWeaponInfo;
import com.dace.dmgr.combat.character.quaker.action.QuakerUltInfo;
import com.dace.dmgr.combat.entity.Attacker;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.interaction.DamageType;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Location;
import org.jetbrains.annotations.Nullable;

import java.util.StringJoiner;

/**
 * 전투원 - 니스 클래스.
 *
 * @see NeaceWeapon
 */
public final class Neace extends Support {
    @Getter
    private static final Neace instance = new Neace();

    private Neace() {
        super("니스", "DVNis", '\u32D5', 1000, 1.0, 1.0);
    }

    @Override
    @NonNull
    public String getActionbarString(@NonNull CombatUser combatUser) {
        NeaceWeapon weapon = (NeaceWeapon) combatUser.getWeapon();

        StringJoiner text = new StringJoiner("    ");

        if (weapon.getTarget() == null && weapon.getSightTarget() != null)
            text.add(weapon.getWeaponInfo() + "  §7[" + weapon.getDefaultActionKeys()[1].getName() + "] §f치유");

        return text.toString();
    }

    @Override
    public void onTick(@NonNull CombatUser combatUser, long i) {
        super.onTick(combatUser, i);

        combatUser.useAction(ActionKey.PERIODIC_1);
    }

    @Override
    public void onDamage(@NonNull CombatUser victim, @Nullable Attacker attacker, int damage, @NonNull DamageType damageType, Location location, boolean isCrit) {
        CombatUtil.playBleedingEffect(location, victim.getEntity(), damage);
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
        return false;
    }

    @Override
    public boolean canJump(@NonNull CombatUser combatUser) {
        return true;
    }

    @Override
    @NonNull
    public NeaceWeaponInfo getWeaponInfo() {
        return NeaceWeaponInfo.getInstance();
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
