package com.dace.dmgr.combat.character.jager;

import com.dace.dmgr.combat.CombatEffectUtil;
import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import com.dace.dmgr.combat.action.info.PassiveSkillInfo;
import com.dace.dmgr.combat.action.info.TraitInfo;
import com.dace.dmgr.combat.action.skill.ActiveSkill;
import com.dace.dmgr.combat.action.skill.Skill;
import com.dace.dmgr.combat.action.weapon.Swappable;
import com.dace.dmgr.combat.character.CharacterType;
import com.dace.dmgr.combat.character.Marksman;
import com.dace.dmgr.combat.character.jager.action.*;
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
        super(null, "예거", "혹한의 사냥꾼", "DVJager", '\u32D2', 3, 1000, 1.0, 1.0);
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
        switch (characterType) {
            case MAGRITTA:
                return new String[]{"망할 녀석...역시 제법이군."};
            default:
                return new String[]{
                        "별 거 없군.",
                        "체크 메이트.",
                        "사냥 완료."
                };
        }

    }

    @Override
    @NonNull
    public String @NonNull [] getDeathMent(@NonNull CharacterType characterType) {
        switch (characterType) {
            case MAGRITTA:
                return new String[]{"미안..하다..."};
            default:
                return new String[]{
                        "...제길!",
                        "젠장...",
                        "뒤를...부탁하지."
                };
        }

    }

    @Override
    @NonNull
    public String getActionbarString(@NonNull CombatUser combatUser) {
        JagerWeaponL weaponL = (JagerWeaponL) combatUser.getWeapon();
        JagerWeaponR weaponR = weaponL.getSwapModule().getSubweapon();
        JagerA1 skill1 = combatUser.getSkill(JagerA1Info.getInstance());
        JagerA3 skill3 = combatUser.getSkill(JagerA3Info.getInstance());

        StringJoiner text = new StringJoiner("    ");

        String weaponLDisplay = StringFormUtil.getActionbarProgressBar("" + TextIcon.CAPACITY, weaponL.getReloadModule().getRemainingAmmo(),
                JagerWeaponInfo.CAPACITY, JagerWeaponInfo.CAPACITY, '*');
        String weaponRDisplay = StringFormUtil.getActionbarProgressBar("" + TextIcon.CAPACITY, weaponR.getReloadModule().getRemainingAmmo(),
                JagerWeaponInfo.SCOPE.CAPACITY, JagerWeaponInfo.SCOPE.CAPACITY, '┃');
        if (weaponL.getSwapModule().getSwapState() == Swappable.SwapState.PRIMARY)
            weaponLDisplay = "§a" + weaponLDisplay;
        else if (weaponL.getSwapModule().getSwapState() == Swappable.SwapState.SECONDARY)
            weaponRDisplay = "§a" + weaponRDisplay;

        text.add(weaponLDisplay);
        text.add(weaponRDisplay);
        text.add("");
        String skill1Display = StringFormUtil.getActionbarProgressBar(JagerA1Info.getInstance().toString(),
                skill1.getStateValue(), skill1.getMaxStateValue(), 10, '■');
        if (!skill1.isDurationFinished())
            skill1Display += "  §7[" + skill1.getDefaultActionKeys()[0] + "] §f회수";
        text.add(skill1Display);
        if (!skill3.isDurationFinished() && skill3.isEnabled())
            text.add(JagerA3Info.getInstance() + "  §7[" + skill3.getDefaultActionKeys()[0] + "][" + skill3.getDefaultActionKeys()[1] + "] §f투척");

        return text.toString();
    }

    @Override
    public boolean onAttack(@NonNull CombatUser attacker, @NonNull Damageable victim, int damage, @NonNull DamageType damageType, boolean isCrit) {
        return attacker.getSkill(JagerUltInfo.getInstance()).getSummonEntity() == null;
    }

    @Override
    public void onDamage(@NonNull CombatUser victim, @Nullable Attacker attacker, int damage, @NonNull DamageType damageType, Location location, boolean isCrit) {
        CombatEffectUtil.playBleedingEffect(location, victim.getEntity(), damage);
    }

    @Override
    public void onKill(@NonNull CombatUser attacker, @NonNull Damageable victim, int score, boolean isFinalHit) {
        super.onKill(attacker, victim, score, isFinalHit);

        if (!(victim instanceof CombatUser))
            return;

        attacker.getSkill(JagerA1Info.getInstance()).applyBonusScore((CombatUser) victim, score);
        attacker.getSkill(JagerUltInfo.getInstance()).applyBonusScore((CombatUser) victim, score);
    }

    @Override
    public boolean canUseMeleeAttack(@NonNull CombatUser combatUser) {
        return !combatUser.getSkill(JagerA1Info.getInstance()).getConfirmModule().isChecking()
                && combatUser.getSkill(JagerA3Info.getInstance()).isDurationFinished();
    }

    @Override
    public boolean canSprint(@NonNull CombatUser combatUser) {
        return !((JagerWeaponL) combatUser.getWeapon()).getAimModule().isAiming();
    }

    @Override
    @NonNull
    public JagerWeaponInfo getWeaponInfo() {
        return JagerWeaponInfo.getInstance();
    }

    @Override
    @Nullable
    public TraitInfo getCharacterTraitInfo(int number) {
        if (number == 1)
            return JagerT1Info.getInstance();

        return null;
    }

    @Override
    @Nullable
    public PassiveSkillInfo<? extends Skill> getPassiveSkillInfo(int number) {
        if (number == 1)
            return JagerP1Info.getInstance();

        return null;
    }

    @Override
    @Nullable
    public ActiveSkillInfo<? extends ActiveSkill> getActiveSkillInfo(int number) {
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
