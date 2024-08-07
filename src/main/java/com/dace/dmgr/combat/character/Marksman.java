package com.dace.dmgr.combat.character;

import com.dace.dmgr.combat.action.info.TraitInfo;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.Damageable;
import lombok.Getter;
import lombok.NonNull;
import org.jetbrains.annotations.MustBeInvokedByOverriders;

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
     * @param skinName         스킨 이름
     * @param icon             전투원 아이콘
     * @param health           체력
     * @param speedMultiplier  이동속도 배수
     * @param hitboxMultiplier 히트박스 크기 배수
     */
    protected Marksman(@NonNull String name, @NonNull String skinName, char icon, int health, double speedMultiplier, double hitboxMultiplier) {
        super(name, skinName, Role.MARKSMAN, icon, health, speedMultiplier, hitboxMultiplier);
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
        if (!(victim instanceof CombatUser))
            return;

        if (isFinalHit)
            attacker.addUltGauge(RoleTrait1Info.ULTIMATE_CHARGE);
    }

    public static final class RoleTrait1Info extends TraitInfo {
        /** 궁극기 충전량 */
        public static final int ULTIMATE_CHARGE = 500;
        @Getter
        private static final RoleTrait1Info instance = new RoleTrait1Info();

        private RoleTrait1Info() {
            super(1, "역할: 사격 - 1");
        }
    }

    public static final class RoleTrait2Info extends TraitInfo {
        /** 이동속도 증가량 */
        public static final int SPEED = 10;
        @Getter
        private static final RoleTrait2Info instance = new RoleTrait2Info();

        private RoleTrait2Info() {
            super(2, "역할: 사격 - 2");
        }
    }
}
