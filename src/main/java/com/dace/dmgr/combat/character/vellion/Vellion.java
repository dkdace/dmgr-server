package com.dace.dmgr.combat.character.vellion;

import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import com.dace.dmgr.combat.action.info.PassiveSkillInfo;
import com.dace.dmgr.combat.character.Controller;
import com.dace.dmgr.combat.character.silia.action.SiliaUltInfo;
import com.dace.dmgr.combat.character.vellion.action.VellionWeapon;
import com.dace.dmgr.combat.character.vellion.action.VellionWeaponInfo;
import com.dace.dmgr.combat.entity.Attacker;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.interaction.DamageType;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Location;
import org.jetbrains.annotations.Nullable;

import java.util.StringJoiner;

/**
 * 전투원 - 벨리온 클래스.
 *
 * @see VellionWeapon
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
        StringJoiner text = new StringJoiner("    ");

        return text.toString();
    }

    @Override
    public void onDamage(@NonNull CombatUser victim, @Nullable Attacker attacker, int damage, @NonNull DamageType damageType, Location location, boolean isCrit) {
        super.onDamage(victim, attacker, damage, damageType, location, isCrit);

        CombatUtil.playBleedingEffect(location, victim.getEntity(), damage);
    }

    @Override
    public boolean canUseMeleeAttack(@NonNull CombatUser combatUser) {
        return true;
    }

    @Override
    public boolean canSprint(@NonNull CombatUser combatUser) {
        return !combatUser.getEntity().isFlying();
    }

    @Override
    public boolean canFly(@NonNull CombatUser combatUser) {
        return true;
    }

    @Override
    public boolean canJump(@NonNull CombatUser combatUser) {
        return true;
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
            default:
                return null;
        }
    }

    @Override
    @Nullable
    public ActiveSkillInfo getActiveSkillInfo(int number) {
        switch (number) {
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
