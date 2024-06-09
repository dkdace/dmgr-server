package com.dace.dmgr.combat.character;

import com.dace.dmgr.combat.action.info.TraitInfo;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.Healable;
import com.dace.dmgr.util.CooldownUtil;
import com.dace.dmgr.util.task.IntervalTask;
import com.dace.dmgr.util.task.TaskUtil;
import lombok.Getter;
import lombok.NonNull;
import org.jetbrains.annotations.MustBeInvokedByOverriders;

/**
 * 역할군이 '지원'인 전투원의 정보를 관리하는 클래스.
 */
public abstract class Support extends Character {
    /** 수정자 ID */
    private static final String MODIFIER_ID = "RoleTrait1";
    /** 회복 쿨타임 ID */
    private static final String HEAL_COOLDOWN_ID = "RoleTrait2Heal";

    /**
     * 지원 역할군 전투원 정보 인스턴스를 생성한다.
     *
     * @param name             이름
     * @param skinName         스킨 이름
     * @param icon             전투원 아이콘
     * @param health           체력
     * @param speedMultiplier  이동속도 배수
     * @param hitboxMultiplier 히트박스 크기 배수
     */
    protected Support(@NonNull String name, @NonNull String skinName, char icon, int health, double speedMultiplier, double hitboxMultiplier) {
        super(name, skinName, Role.SUPPORT, icon, health, speedMultiplier, hitboxMultiplier);
    }

    @Override
    @MustBeInvokedByOverriders
    public void onTick(@NonNull CombatUser combatUser, long i) {
        if (i % 5 == 0 && combatUser.getGame() != null && combatUser.getGameUser() != null) {
            boolean activate = combatUser.getGame().getTeamUserMap().get(combatUser.getGameUser().getTeam()).stream()
                    .map(gameUser -> CombatUser.fromUser(gameUser.getUser()))
                    .anyMatch(combatUser2 -> combatUser2 != null && combatUser2.getDamageModule().isLowHealth() &&
                            combatUser2.getEntity().getLocation().distance(combatUser.getEntity().getLocation()) >= RoleTrait1Info.DETECT_RADIUS);

            if (activate)
                combatUser.getMoveModule().getSpeedStatus().addModifier(MODIFIER_ID, RoleTrait1Info.SPEED);
            else
                combatUser.getMoveModule().getSpeedStatus().removeModifier(MODIFIER_ID);
        }
    }

    @Override
    @MustBeInvokedByOverriders
    public boolean onGiveHeal(@NonNull CombatUser provider, @NonNull Healable target, int amount) {
        if (provider != target) {
            if (CooldownUtil.getCooldown(provider, HEAL_COOLDOWN_ID) == 0) {
                CooldownUtil.setCooldown(provider, HEAL_COOLDOWN_ID, RoleTrait2Info.DURATION);

                TaskUtil.addTask(provider, new IntervalTask(i -> {
                    provider.getDamageModule().heal(provider, RoleTrait2Info.HEAL_PER_SECOND * 2 / 20, false);
                    return CooldownUtil.getCooldown(provider, HEAL_COOLDOWN_ID) > 0;
                }, 2));
            } else
                CooldownUtil.setCooldown(provider, HEAL_COOLDOWN_ID, RoleTrait2Info.DURATION);
        }

        return true;
    }

    public static final class RoleTrait1Info extends TraitInfo {
        /** 이동속도 증가량 */
        public static final int SPEED = 20;
        /** 감지 범위 */
        public static final int DETECT_RADIUS = 20;
        @Getter
        private static final RoleTrait1Info instance = new RoleTrait1Info();

        private RoleTrait1Info() {
            super(1, "역할: 지원 - 1");
        }
    }

    public static final class RoleTrait2Info extends TraitInfo {
        /** 초당 치유량 */
        public static final int HEAL_PER_SECOND = 50;
        /** 지속시간 (tick) */
        public static final long DURATION = 3 * 20;
        @Getter
        private static final RoleTrait2Info instance = new RoleTrait2Info();

        private RoleTrait2Info() {
            super(2, "역할: 지원 - 2");
        }
    }
}
