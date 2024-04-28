package com.dace.dmgr.combat.character;

import com.dace.dmgr.combat.action.info.TraitInfo;
import com.dace.dmgr.combat.entity.CombatUser;
import lombok.Getter;
import lombok.NonNull;
import org.jetbrains.annotations.MustBeInvokedByOverriders;

/**
 * 역할군이 '돌격'인 전투원의 정보를 관리하는 클래스.
 */
public abstract class Vanguard extends Character {
    /** 넉백 저항 수정자 ID */
    private static final String KNOCKBACK_RESISTANCE_MODIFIER_ID = "RoleTrait2Knockback";
    /** 상태 효과 저항 수정자 ID */
    private static final String STATUS_RESISTANCE_MODIFIER_ID = "RoleTrait2Status";

    /**
     * 돌격 역할군 전투원 정보 인스턴스를 생성한다.
     *
     * @param name             이름
     * @param skinName         스킨 이름
     * @param icon             전투원 아이콘
     * @param health           체력
     * @param speedMultiplier  이동속도 배수
     * @param hitboxMultiplier 히트박스 크기 배수
     */
    protected Vanguard(@NonNull String name, @NonNull String skinName, char icon, int health, double speedMultiplier, double hitboxMultiplier) {
        super(name, skinName, Role.VANGUARD, icon, health, speedMultiplier, hitboxMultiplier);
    }

    @Override
    @MustBeInvokedByOverriders
    public void onTick(@NonNull CombatUser combatUser, long i) {
        combatUser.getKnockbackModule().getResistanceStatus().addModifier(KNOCKBACK_RESISTANCE_MODIFIER_ID, RoleTrait1Info.KNOCKBACK_RESISTANCE);
        combatUser.getStatusEffectModule().getResistanceStatus().addModifier(STATUS_RESISTANCE_MODIFIER_ID, RoleTrait1Info.STATUS_EFFECT_RESISTANCE);
    }

    @Override
    @MustBeInvokedByOverriders
    public void onUseHealPack(@NonNull CombatUser combatUser) {
        combatUser.getStatusEffectModule().clearStatusEffect(false);
    }

    public static final class RoleTrait1Info extends TraitInfo {
        /** 넉백 저항 */
        public static final int KNOCKBACK_RESISTANCE = 30;
        /** 상태 효과 저항 */
        public static final int STATUS_EFFECT_RESISTANCE = 15;
        @Getter
        private static final RoleTrait1Info instance = new RoleTrait1Info();

        private RoleTrait1Info() {
            super(1, "역할: 돌격 - 1");
        }
    }

    public static final class RoleTrait2Info extends TraitInfo {
        @Getter
        private static final RoleTrait2Info instance = new RoleTrait2Info();

        private RoleTrait2Info() {
            super(2, "역할: 돌격 - 2");
        }
    }
}
