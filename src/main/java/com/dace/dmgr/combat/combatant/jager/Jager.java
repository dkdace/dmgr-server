package com.dace.dmgr.combat.combatant.jager;

import com.dace.dmgr.combat.ability.info.ActiveSkillInfo;
import com.dace.dmgr.combat.ability.info.PassiveSkillInfo;
import com.dace.dmgr.combat.ability.info.TraitInfo;
import com.dace.dmgr.combat.combatant.CombatantType;
import com.dace.dmgr.combat.combatant.Marksman;
import com.dace.dmgr.combat.entity.combatuser.AbilityManager;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import lombok.Getter;
import lombok.NonNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
        super("예거", "혹한의 사냥꾼", "ch_jager", null, Species.HUMAN, '\u32D2', 3,
                1000, 1.0, 1.0);
    }

    @Override
    @NonNull
    public String getReqHealMentLow() {
        return "젠장, 의무병 녀석들은 죄다 어디로 간거야!";
    }

    @Override
    @NonNull
    public String getReqHealMentHalf() {
        return "여기는 예거, 부상이다. 속히 지원을 요청한다!";
    }

    @Override
    @NonNull
    public String getReqHealMentNormal() {
        return "여기는 예거, 지원 바람.";
    }

    @Override
    @NonNull
    public String getUltStateMentLow() {
        return "충전 중. 시간이 필요하다.";
    }

    @Override
    @NonNull
    public String getUltStateMentNearFull() {
        return "거의 충전되었다.";
    }

    @Override
    @NonNull
    public String getUltStateMentFull() {
        return "큰거 한 방 준비 완료!";
    }

    @Override
    @NonNull
    public String @NonNull [] getReqRallyMents() {
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
    public String @NonNull [] getKillMents(@NonNull CombatantType combatantType) {
        switch (combatantType) {
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
    public String @NonNull [] getDeathMents(@NonNull CombatantType combatantType) {
        switch (combatantType) {
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
    public boolean canUseMeleeAttack(@NonNull CombatUser combatUser) {
        AbilityManager abilityManager = combatUser.getAbilityManager();
        return !abilityManager.getAbility(JagerA1Info.getInstance()).getConfirmModule().isChecking()
                && abilityManager.getAbility(JagerA3Info.getInstance()).isDurationFinished();
    }

    @Override
    public boolean canSprint(@NonNull CombatUser combatUser) {
        return !((JagerWeaponL) combatUser.getAbilityManager().getWeapon()).getAimModule().isAiming();
    }

    @Override
    public boolean canChargeUlt(@NonNull CombatUser combatUser) {
        return combatUser.getAbilityManager().getAbility(JagerUltInfo.getInstance()).getEntityModule().get() == null;
    }

    @Override
    @NonNull
    public JagerWeaponInfo getWeaponInfo() {
        return JagerWeaponInfo.getInstance();
    }

    @Override
    @NonNull
    protected List<@NonNull TraitInfo<?>> getCombatantTraitInfos() {
        return Collections.singletonList(JagerT1Info.getInstance());
    }

    @Override
    @NonNull
    public List<@NonNull PassiveSkillInfo<?>> getPassiveSkillInfos() {
        return Collections.singletonList(JagerP1Info.getInstance());
    }

    @Override
    @NonNull
    public List<@NonNull ActiveSkillInfo<?>> getActiveSkillInfos() {
        return Arrays.asList(JagerA1Info.getInstance(), JagerA2Info.getInstance(), JagerA3Info.getInstance(), getUltimateSkillInfo());
    }

    @Override
    @NonNull
    public JagerUltInfo getUltimateSkillInfo() {
        return JagerUltInfo.getInstance();
    }
}
