package com.dace.dmgr.combat.character;

import com.dace.dmgr.combat.action.info.TraitInfo;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.util.task.IntervalTask;
import com.dace.dmgr.util.task.TaskUtil;
import lombok.Getter;
import lombok.NonNull;
import org.jetbrains.annotations.MustBeInvokedByOverriders;

/**
 * 역할군이 '수호'인 전투원의 정보를 관리하는 클래스.
 */
public abstract class Guardian extends Character {
    /** 넉백 저항 수정자 ID */
    private static final String KNOCKBACK_RESISTANCE_MODIFIER_ID = "RoleTrait2Knockback";
    /** 방어력 수정자 ID */
    private static final String DEFENSE_MODIFIER_ID = "RoleTrait2Defense";

    /**
     * 수호 역할군 전투원 정보 인스턴스를 생성한다.
     *
     * @param name             이름
     * @param skinName         스킨 이름
     * @param icon             전투원 아이콘
     * @param health           체력
     * @param speedMultiplier  이동속도 배수
     * @param hitboxMultiplier 히트박스 크기 배수
     */
    protected Guardian(@NonNull String name, @NonNull String skinName, char icon, int health, double speedMultiplier, double hitboxMultiplier) {
        super(name, skinName, Role.GUARDIAN, icon, health, speedMultiplier, hitboxMultiplier);
    }

    @Override
    @MustBeInvokedByOverriders
    public void onTick(@NonNull CombatUser combatUser, long i) {
        combatUser.getKnockbackModule().getResistanceStatus().addModifier(KNOCKBACK_RESISTANCE_MODIFIER_ID, RoleTrait1Info.KNOCKBACK_RESISTANCE);
        combatUser.getDamageModule().getDefenseMultiplierStatus().addModifier(DEFENSE_MODIFIER_ID, RoleTrait1Info.DEFENSE);
    }

    @Override
    @MustBeInvokedByOverriders
    public void onUseHealPack(@NonNull CombatUser combatUser) {
        TaskUtil.addTask(combatUser, new IntervalTask(i -> {
            int amount = (int) (RoleTrait2Info.HEAL / RoleTrait2Info.DURATION);
            if (i == 0)
                amount += (int) (RoleTrait2Info.HEAL % RoleTrait2Info.DURATION);
            combatUser.getDamageModule().heal(combatUser, amount, false);

            return true;
        }, 1, RoleTrait2Info.DURATION));
    }

    public static final class RoleTrait1Info extends TraitInfo {
        /** 넉백 저항 */
        public static final int KNOCKBACK_RESISTANCE = 30;
        /** 방어력 */
        public static final int DEFENSE = 15;
        @Getter
        private static final RoleTrait1Info instance = new RoleTrait1Info();

        private RoleTrait1Info() {
            super(1, "역할: 수호 - 1");
        }
    }

    public static final class RoleTrait2Info extends TraitInfo {
        /** 치유량 */
        public static final int HEAL = 300;
        /** 지속시간 (tick) */
        public static final long DURATION = 2 * 20;
        @Getter
        private static final RoleTrait2Info instance = new RoleTrait2Info();

        private RoleTrait2Info() {
            super(2, "역할: 수호 - 2");
        }
    }
}
