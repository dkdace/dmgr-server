package com.dace.dmgr.combat.character;

import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.TraitInfo;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.module.statuseffect.Speed;
import lombok.Getter;
import lombok.NonNull;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.Nullable;

import java.text.MessageFormat;

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

    public static final class RoleTrait1Info extends TraitInfo {
        /** 궁극기 충전량 */
        public static final int ULTIMATE_CHARGE = 500;
        @Getter
        private static final RoleTrait1Info instance = new RoleTrait1Info();

        private RoleTrait1Info() {
            super("역할: 근접 - 1",
                    "",
                    "§f▍ 마지막 공격으로 적을 처치하면 " + TextIcon.ULTIMATE + " §7궁극기 충전량",
                    "§f▍ 을 추가로 얻습니다.",
                    "",
                    MessageFormat.format("§f{0} {1}", TextIcon.ULTIMATE, ULTIMATE_CHARGE));
        }
    }

    public static final class RoleTrait2Info extends TraitInfo {
        /** 이동속도 증가량 */
        public static final int SPEED = 25;
        /** 지속시간 (tick) */
        public static final long DURATION = (long) (2.5 * 20);
        @Getter
        private static final RoleTrait2Info instance = new RoleTrait2Info();

        private RoleTrait2Info() {
            super("역할: 근접 - 2",
                    "",
                    "§f▍ 적을 처치하면 §b" + TextIcon.WALK_SPEED_INCREASE + " 이동 속도§f가 빨라집니다.",
                    "",
                    MessageFormat.format("§7{0} §f{1}초", TextIcon.DURATION, DURATION / 20.0),
                    MessageFormat.format("§b{0} §f{1}%", TextIcon.WALK_SPEED_INCREASE, SPEED));
        }
    }

    /**
     * 속도 증가 상태 효과 클래스.
     */
    private static final class RoleTrait2Speed extends Speed {
        private static final RoleTrait2Speed instance = new RoleTrait2Speed();
        /** 수정자 ID */
        private static final String MODIFIER_ID = "RoleTrait2";

        private RoleTrait2Speed() {
            super(MODIFIER_ID, RoleTrait2Info.SPEED);
        }
    }
}
