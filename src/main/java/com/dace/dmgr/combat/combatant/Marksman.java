package com.dace.dmgr.combat.combatant;

import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActionInfoLore;
import com.dace.dmgr.combat.action.info.ActionInfoLore.Section.Format;
import com.dace.dmgr.combat.action.info.TraitInfo;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.combat.entity.module.AbilityStatus;
import lombok.NonNull;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.Nullable;

/**
 * 역할군이 '사격'인 전투원의 정보를 관리하는 클래스.
 */
public abstract class Marksman extends Combatant {
    /** 수정자 */
    private static final AbilityStatus.Modifier MODIFIER = new AbilityStatus.Modifier(RoleTrait2Info.SPEED);

    /**
     * 사격 역할군 전투원 정보 인스턴스를 생성한다.
     *
     * @param subRole          부 역할군
     * @param name             이름
     * @param nickname         별명
     * @param skinName         스킨 이름
     * @param species          종족 유형
     * @param icon             전투원 아이콘
     * @param difficulty       난이도
     * @param health           체력
     * @param speedMultiplier  이동속도 배수
     * @param hitboxMultiplier 히트박스 크기 배수
     */
    protected Marksman(@Nullable Role subRole, @NonNull String name, @NonNull String nickname, @NonNull String skinName, @NonNull Species species,
                       char icon, int difficulty, int health, double speedMultiplier, double hitboxMultiplier) {
        super(name, nickname, skinName, Role.MARKSMAN, subRole, species, icon, difficulty, health, speedMultiplier, hitboxMultiplier);
    }

    @Override
    @MustBeInvokedByOverriders
    public void onTick(@NonNull CombatUser combatUser, long i) {
        super.onTick(combatUser, i);

        if (combatUser.getDamageModule().isLowHealth())
            combatUser.getMoveModule().getSpeedStatus().addModifier(MODIFIER);
        else
            combatUser.getMoveModule().getSpeedStatus().removeModifier(MODIFIER);
    }

    @Override
    @MustBeInvokedByOverriders
    public void onKill(@NonNull CombatUser attacker, @NonNull Damageable victim, double contributionScore, boolean isFinalHit) {
        if (victim.isGoalTarget() && isFinalHit)
            attacker.addUltGauge(RoleTrait1Info.ULTIMATE_CHARGE);
    }

    @Override
    @NonNull
    final TraitInfo @NonNull [] getDefaultTraitInfos() {
        return new TraitInfo[]{RoleTrait1Info.instance, RoleTrait2Info.instance};
    }

    /**
     * 특성 1번 정보 클래스.
     */
    private static final class RoleTrait1Info extends TraitInfo {
        /** 궁극기 충전량 */
        private static final int ULTIMATE_CHARGE = 500;

        private static final RoleTrait1Info instance = new RoleTrait1Info();

        private RoleTrait1Info() {
            super("역할: 사격 - 1",
                    new ActionInfoLore(ActionInfoLore.Section
                            .builder("마지막 공격으로 적을 처치하면 <7:ULTIMATE:궁극기 충전량>을 추가로 얻습니다.")
                            .addValueInfo(TextIcon.ULTIMATE, ULTIMATE_CHARGE)
                            .build()));
        }
    }

    /**
     * 특성 2번 정보 클래스.
     */
    private static final class RoleTrait2Info extends TraitInfo {
        /** 이동속도 증가량 */
        private static final int SPEED = 10;

        private static final RoleTrait2Info instance = new RoleTrait2Info();

        private RoleTrait2Info() {
            super("역할: 사격 - 2",
                    new ActionInfoLore(ActionInfoLore.Section
                            .builder("치명상일 때 <:WALK_SPEED_INCREASE:이동 속도>가 빨라집니다.")
                            .addValueInfo(TextIcon.WALK_SPEED_INCREASE, Format.PERCENT, SPEED)
                            .build()));
        }
    }
}
