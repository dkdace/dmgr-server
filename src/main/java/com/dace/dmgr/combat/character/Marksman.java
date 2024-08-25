package com.dace.dmgr.combat.character;

import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.TraitInfo;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.Damageable;
import lombok.Getter;
import lombok.NonNull;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.Nullable;

import java.text.MessageFormat;

/**
 * 역할군이 '사격'인 전투원의 정보를 관리하는 클래스.
 */
public abstract class Marksman extends Character {
    /** 수정자 ID */
    private static final String MODIFIER_ID = "RoleTrait2";

    /**
     * 사격 역할군 전투원 정보 인스턴스를 생성한다.
     *
     * @param name             이름
     * @param nickname         별명
     * @param skinName         스킨 이름
     * @param icon             전투원 아이콘
     * @param difficulty       난이도
     * @param health           체력
     * @param speedMultiplier  이동속도 배수
     * @param hitboxMultiplier 히트박스 크기 배수
     */
    protected Marksman(@NonNull String name, @NonNull String nickname, @NonNull String skinName, char icon, int difficulty, int health,
                       double speedMultiplier, double hitboxMultiplier) {
        super(name, nickname, skinName, Role.MARKSMAN, icon, difficulty, health, speedMultiplier, hitboxMultiplier);
    }

    @Override
    @MustBeInvokedByOverriders
    public void onTick(@NonNull CombatUser combatUser, long i) {
        if (combatUser.getDamageModule().isLowHealth())
            combatUser.getMoveModule().getSpeedStatus().addModifier(MODIFIER_ID, RoleTrait2Info.SPEED);
        else
            combatUser.getMoveModule().getSpeedStatus().removeModifier(MODIFIER_ID);
    }

    @Override
    @MustBeInvokedByOverriders
    public void onKill(@NonNull CombatUser attacker, @NonNull Damageable victim, int score, boolean isFinalHit) {
        if (victim instanceof CombatUser && isFinalHit)
            attacker.addUltGauge(RoleTrait1Info.ULTIMATE_CHARGE);
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
            super("역할: 사격 - 1",
                    "",
                    "§f▍ 마지막 공격으로 적을 처치하면 " + TextIcon.ULTIMATE + " §7궁극기 충전량",
                    "§f▍ 을 추가로 얻습니다.",
                    "",
                    MessageFormat.format("§f{0} {1}", TextIcon.ULTIMATE, ULTIMATE_CHARGE));
        }
    }

    public static final class RoleTrait2Info extends TraitInfo {
        /** 이동속도 증가량 */
        public static final int SPEED = 10;
        @Getter
        private static final RoleTrait2Info instance = new RoleTrait2Info();

        private RoleTrait2Info() {
            super("역할: 사격 - 2",
                    "",
                    "§f▍ 치명상일 때 §b" + TextIcon.WALK_SPEED_INCREASE + " 이동 속도§f가 빨라집니다.",
                    "",
                    MessageFormat.format("§b{0} §f{1}%", TextIcon.WALK_SPEED_INCREASE, SPEED));
        }
    }
}
