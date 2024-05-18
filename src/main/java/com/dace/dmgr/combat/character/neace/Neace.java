package com.dace.dmgr.combat.character.neace;

import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import com.dace.dmgr.combat.action.info.PassiveSkillInfo;
import com.dace.dmgr.combat.character.Support;
import com.dace.dmgr.combat.character.neace.action.*;
import com.dace.dmgr.combat.character.quaker.action.QuakerUltInfo;
import com.dace.dmgr.combat.entity.Attacker;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.Healable;
import com.dace.dmgr.combat.interaction.DamageType;
import com.dace.dmgr.combat.interaction.Hitscan;
import com.dace.dmgr.combat.interaction.HitscanOption;
import com.dace.dmgr.util.GlowUtil;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.StringJoiner;

/**
 * 전투원 - 니스 클래스.
 *
 * @see NeaceWeapon
 * @see NeaceP1
 * @see NeaceA1
 * @see NeaceA2
 * @see NeaceA3
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
        NeaceA2 skill2 = (NeaceA2) combatUser.getSkill(NeaceA2Info.getInstance());
        NeaceA3 skill3 = (NeaceA3) combatUser.getSkill(NeaceA3Info.getInstance());

        StringJoiner text = new StringJoiner("    ");

        if (!skill2.isDurationFinished())
            text.add(skill2.getSkillInfo() + "  §7[" + skill2.getDefaultActionKeys()[0].getName() + "] §f해제");
        if (!skill3.isDurationFinished())
            text.add(skill3.getSkillInfo() + "  §7[" + skill3.getDefaultActionKeys()[0].getName() + "] §f해제");

        return text.toString();
    }

    @Override
    public void onTick(@NonNull CombatUser combatUser, long i) {
        super.onTick(combatUser, i);

        new NeaceTarget(combatUser).shoot();
    }

    @Override
    public void onDamage(@NonNull CombatUser victim, @Nullable Attacker attacker, int damage, @NonNull DamageType damageType, Location location, boolean isCrit) {
        CombatUtil.playBleedingEffect(location, victim.getEntity(), damage);
        CooldownUtil.setCooldown(victim, NeaceP1.COOLDOWN_ID, NeaceP1Info.ACTIVATE_DURATION);
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
            case 1:
                return NeaceP1Info.getInstance();
            default:
                return null;
        }
    }

    @Override
    @Nullable
    public ActiveSkillInfo getActiveSkillInfo(int number) {
        switch (number) {
            case 1:
                return NeaceA1Info.getInstance();
            case 2:
                return NeaceA2Info.getInstance();
            case 3:
                return NeaceA3Info.getInstance();
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

    private static final class NeaceTarget extends Hitscan {
        private NeaceTarget(CombatUser combatUser) {
            super(combatUser, HitscanOption.builder().size(0.8).maxDistance(NeaceA1Info.MAX_DISTANCE)
                    .condition(combatEntity -> combatEntity instanceof Healable && !combatEntity.isEnemy(combatUser) &&
                            combatEntity != combatUser).build());
        }

        @Override
        protected boolean onHitBlock(@NonNull Block hitBlock) {
            return false;
        }

        @Override
        protected boolean onHitEntity(@NonNull Damageable target, boolean isCrit) {
            GlowUtil.setGlowing(target.getEntity(), ChatColor.GREEN, shooter.getEntity(), 3);
            return false;
        }
    }
}
