package com.dace.dmgr.combat.character.jager;

import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import com.dace.dmgr.combat.action.info.PassiveSkillInfo;
import com.dace.dmgr.combat.action.weapon.Swappable;
import com.dace.dmgr.combat.character.CharacterType;
import com.dace.dmgr.combat.character.Marksman;
import com.dace.dmgr.combat.character.jager.action.*;
import com.dace.dmgr.combat.entity.Attacker;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.interaction.DamageType;
import com.dace.dmgr.util.CooldownUtil;
import com.dace.dmgr.util.StringFormUtil;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Location;
import org.jetbrains.annotations.Nullable;

import java.util.StringJoiner;

/**
 * 전투원 - 예거 클래스.
 *
 * @see JagerWeaponL
 * @see JagerWeaponR
 * @see JagerP1
 * @see JagerA1
 * @see JagerA2
 * @see JagerA3
 * @see JagerUlt
 */
public final class Jager extends Marksman {
    @Getter
    private static final Jager instance = new Jager();

    private Jager() {
        super("예거", "DVJager", '\u32D2', 1000, 1.0, 1.0);
    }

    @Override
    @NonNull
    public String @NonNull [] getReqHealMent() {
        return new String[]{
                "젠장, 의무병 녀석들은 죄다 어디로 간거야!",
                "여기는 예거, 부상이다. 속히 지원을 요청한다!",
                "여기는 예거, 지원 바람."
        };
    }

    @Override
    @NonNull
    public String @NonNull [] getUltStateMent() {
        return new String[]{
                "충전 중. 시간이 필요하다.",
                "거의 충전되었다.",
                "큰거 한 방 준비 완료!"
        };
    }

    @Override
    @NonNull
    public String @NonNull [] getReqRallyMent() {
        return new String[]{
                "여기는 예거, 집결을 요청한다!",
                "뭐하나! 어서 여기로 모여!"
        };
    }

    @Override
    @NonNull
    public String getUltUseMent() {
        return "저놈들의 머리를 좀 식혀주지.";
    }

    @Override
    @NonNull
    public String @NonNull [] getKillMent(@NonNull CharacterType characterType) {
        return new String[]{
                "별 거 없군.",
                "체크 메이트.",
                "사냥 완료."
        };
    }

    @Override
    @NonNull
    public String @NonNull [] getDeathMent(@NonNull CharacterType characterType) {
        return new String[]{
                "...제길!",
                "젠장...",
                "뒤를...부탁하지."
        };
    }

    @Override
    @NonNull
    public String getActionbarString(@NonNull CombatUser combatUser) {
        JagerWeaponL weapon1 = (JagerWeaponL) combatUser.getWeapon();
        JagerWeaponR weapon2 = ((JagerWeaponL) combatUser.getWeapon()).getSwapModule().getSubweapon();
        JagerA1 skill1 = (JagerA1) combatUser.getSkill(JagerA1Info.getInstance());
        JagerA3 skill3 = (JagerA3) combatUser.getSkill(JagerA3Info.getInstance());

        int weapon1Ammo = weapon1.getReloadModule().getRemainingAmmo();
        int weapon2Ammo = weapon2.getReloadModule().getRemainingAmmo();
        int skill1Health = skill1.getStateValue();
        int skill1MaxHealth = skill1.getMaxStateValue();

        StringJoiner text = new StringJoiner("    ");

        String weapon1Display = StringFormUtil.getActionbarProgressBar("" + TextIcon.CAPACITY, weapon1Ammo, JagerWeaponInfo.CAPACITY,
                JagerWeaponInfo.CAPACITY, '*');
        String weapon2Display = StringFormUtil.getActionbarProgressBar("" + TextIcon.CAPACITY, weapon2Ammo, JagerWeaponInfo.SCOPE.CAPACITY,
                JagerWeaponInfo.SCOPE.CAPACITY, '┃');
        String skill1Display = StringFormUtil.getActionbarProgressBar(skill1.getSkillInfo().toString(), skill1Health, skill1MaxHealth,
                10, '■');
        if (weapon1.getSwapModule().getSwapState() == Swappable.SwapState.PRIMARY)
            weapon1Display = "§a" + weapon1Display;
        else if (weapon1.getSwapModule().getSwapState() == Swappable.SwapState.SECONDARY)
            weapon2Display = "§a" + weapon2Display;

        text.add(weapon1Display);
        text.add(weapon2Display);
        text.add("");
        if (!skill1.isDurationFinished())
            skill1Display += "  §7[" + skill1.getDefaultActionKeys()[0].getName() + "] §f회수";
        text.add(skill1Display);
        if (!skill3.isDurationFinished() && skill3.isEnabled())
            text.add(skill3.getSkillInfo() + "  §7[" + skill3.getDefaultActionKeys()[0].getName() + "] §f투척");

        return text.toString();
    }

    @Override
    public boolean onAttack(@NonNull CombatUser attacker, @NonNull Damageable victim, int damage, @NonNull DamageType damageType, boolean isCrit) {
        JagerUlt skillUlt = (JagerUlt) attacker.getSkill(JagerUltInfo.getInstance());
        return skillUlt.getSummonEntity() == null;
    }

    @Override
    public void onDamage(@NonNull CombatUser victim, @Nullable Attacker attacker, int damage, @NonNull DamageType damageType, Location location, boolean isCrit) {
        CombatUtil.playBleedingEffect(location, victim.getEntity(), damage);
    }

    @Override
    public void onKill(@NonNull CombatUser attacker, @NonNull Damageable victim, int score, boolean isFinalHit) {
        super.onKill(attacker, victim, score, isFinalHit);

        if (!(victim instanceof CombatUser))
            return;

        if (CooldownUtil.getCooldown(attacker, JagerA1.KILL_SCORE_COOLDOWN_ID + victim) > 0)
            attacker.addScore("설랑 보너스", JagerA1Info.KILL_SCORE * score / 100.0);
        if (CooldownUtil.getCooldown(attacker, JagerUlt.KILL_SCORE_COOLDOWN_ID + victim) > 0)
            attacker.addScore("궁극기 보너스", JagerUltInfo.KILL_SCORE * score / 100.0);
    }

    @Override
    public boolean canUseMeleeAttack(@NonNull CombatUser combatUser) {
        return !((JagerA1) combatUser.getSkill(JagerA1Info.getInstance())).getConfirmModule().isChecking() &&
                combatUser.getSkill(JagerA3Info.getInstance()).isDurationFinished();
    }

    @Override
    public boolean canSprint(@NonNull CombatUser combatUser) {
        return !((JagerWeaponL) combatUser.getWeapon()).getAimModule().isAiming();
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
    public JagerWeaponInfo getWeaponInfo() {
        return JagerWeaponInfo.getInstance();
    }

    @Override
    @Nullable
    public PassiveSkillInfo getPassiveSkillInfo(int number) {
        switch (number) {
            case 1:
                return JagerP1Info.getInstance();
            default:
                return null;
        }
    }

    @Override
    @Nullable
    public ActiveSkillInfo getActiveSkillInfo(int number) {
        switch (number) {
            case 1:
                return JagerA1Info.getInstance();
            case 2:
                return JagerA2Info.getInstance();
            case 3:
                return JagerA3Info.getInstance();
            case 4:
                return JagerUltInfo.getInstance();
            default:
                return null;
        }
    }

    @Override
    @NonNull
    public JagerUltInfo getUltimateSkillInfo() {
        return JagerUltInfo.getInstance();
    }
}
