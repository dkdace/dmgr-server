package com.dace.dmgr.combat.character;

import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActionInfoLore;
import com.dace.dmgr.combat.action.info.ActionInfoLore.Section.Format;
import com.dace.dmgr.combat.action.info.TraitInfo;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.module.statuseffect.Speed;
import lombok.NonNull;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.Nullable;

/**
 * 역할군이 '근접'인 전투원의 정보를 관리하는 클래스.
 */
public abstract class Scuffler extends Character {
    /**
     * 근접 역할군 전투원 정보 인스턴스를 생성한다.
     *
     * @param subRole          부 역할군
     * @param name             이름
     * @param nickname         별명
     * @param skinName         스킨 이름
     * @param icon             전투원 아이콘
     * @param difficulty       난이도
     * @param health           체력
     * @param speedMultiplier  이동속도 배수
     * @param hitboxMultiplier 히트박스 크기 배수
     */
    protected Scuffler(@Nullable Role subRole, @NonNull String name, @NonNull String nickname, @NonNull String skinName, char icon, int difficulty,
                       int health, double speedMultiplier, double hitboxMultiplier) {
        super(name, nickname, skinName, Role.SCUFFLER, subRole, icon, difficulty, health, speedMultiplier, hitboxMultiplier);
    }

    @Override
    @MustBeInvokedByOverriders
    public void onKill(@NonNull CombatUser attacker, @NonNull Damageable victim, int score, boolean isFinalHit) {
        if (victim instanceof CombatUser) {
            if (isFinalHit)
                attacker.addUltGauge(RoleTrait1Info.ULTIMATE_CHARGE);

            attacker.getStatusEffectModule().applyStatusEffect(attacker, RoleTrait2Speed.instance, RoleTrait2Info.DURATION);
        }
    }

    @Override
    @Nullable
    public final TraitInfo getTraitInfo(int number) {
        if (number == 1)
            return RoleTrait1Info.instance;
        else if (number == 2)
            return RoleTrait2Info.instance;

        return getCharacterTraitInfo(number - 2);
    }

    /**
     * @see Character#getTraitInfo(int)
     */
    @Nullable
    public abstract TraitInfo getCharacterTraitInfo(int number);

    private static final class RoleTrait1Info extends TraitInfo {
        /** 궁극기 충전량 */
        private static final int ULTIMATE_CHARGE = 500;

        private static final RoleTrait1Info instance = new RoleTrait1Info();

        private RoleTrait1Info() {
            super("역할: 근접 - 1",
                    new ActionInfoLore(ActionInfoLore.Section
                            .builder("마지막 공격으로 적을 처치하면 <7:ULTIMATE:궁극기 충전량>을 추가로 얻습니다.")
                            .addValueInfo(TextIcon.ULTIMATE, ULTIMATE_CHARGE)
                            .build()
                    )
            );
        }
    }

    private static final class RoleTrait2Info extends TraitInfo {
        /** 이동속도 증가량 */
        private static final int SPEED = 25;
        /** 지속시간 (tick) */
        private static final long DURATION = (long) (2.5 * 20);

        private static final RoleTrait2Info instance = new RoleTrait2Info();

        private RoleTrait2Info() {
            super("역할: 근접 - 2",
                    new ActionInfoLore(ActionInfoLore.Section
                            .builder("적을 처치하면 <:WALK_SPEED_INCREASE:이동 속도>가 빨라집니다.")
                            .addValueInfo(TextIcon.DURATION, Format.TIME, DURATION / 20.0)
                            .addValueInfo(TextIcon.WALK_SPEED_INCREASE, Format.PERCENT, SPEED)
                            .build()
                    )
            );
        }
    }

    /**
     * 속도 증가 상태 효과 클래스.
     */
    private static final class RoleTrait2Speed extends Speed {
        /** 수정자 ID */
        private static final String MODIFIER_ID = "RoleTrait2";

        private static final RoleTrait2Speed instance = new RoleTrait2Speed();

        private RoleTrait2Speed() {
            super(MODIFIER_ID, RoleTrait2Info.SPEED);
        }
    }
}
