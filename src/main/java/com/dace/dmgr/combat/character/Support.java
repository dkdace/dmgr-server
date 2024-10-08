package com.dace.dmgr.combat.character;

import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.TraitInfo;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.Healable;
import com.dace.dmgr.util.CooldownUtil;
import com.dace.dmgr.util.task.IntervalTask;
import com.dace.dmgr.util.task.TaskUtil;
import lombok.Getter;
import lombok.NonNull;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.Nullable;

import java.text.MessageFormat;
import java.util.Arrays;

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
     * @param nickname         별명
     * @param skinName         스킨 이름
     * @param icon             전투원 아이콘
     * @param difficulty       난이도
     * @param health           체력
     * @param speedMultiplier  이동속도 배수
     * @param hitboxMultiplier 히트박스 크기 배수
     */
    protected Support(@NonNull String name, @NonNull String nickname, @NonNull String skinName, char icon, int difficulty, int health,
                      double speedMultiplier, double hitboxMultiplier) {
        super(name, nickname, skinName, Role.SUPPORT, icon, difficulty, health, speedMultiplier, hitboxMultiplier);
    }

    @Override
    @MustBeInvokedByOverriders
    public void onTick(@NonNull CombatUser combatUser, long i) {
        if (i % 5 == 0 && combatUser.getGame() != null && combatUser.getGameUser() != null && combatUser.getGameUser().getTeam() != null) {
            boolean activate = Arrays.stream(combatUser.getGameUser().getTeam().getTeamUsers())
                    .map(gameUser -> CombatUser.fromUser(gameUser.getUser()))
                    .anyMatch(target -> target != null && target.getDamageModule().isLowHealth() &&
                            target.getEntity().getLocation().distance(combatUser.getEntity().getLocation()) >= RoleTrait1Info.DETECT_RADIUS);

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
        /** 이동속도 증가량 */
        public static final int SPEED = 20;
        /** 감지 범위 */
        public static final int DETECT_RADIUS = 20;
        @Getter
        private static final RoleTrait1Info instance = new RoleTrait1Info();

        private RoleTrait1Info() {
            super("역할: 지원 - 1",
                    "",
                    "§f▍ 치명상인 아군이 범위 밖에 있을 때 §b" + TextIcon.WALK_SPEED_INCREASE + " 이동 속도",
                    "§f▍ 가 빨라집니다.",
                    "",
                    MessageFormat.format("§b{0} §f{1}%", TextIcon.WALK_SPEED_INCREASE, SPEED),
                    MessageFormat.format("§f{0} {1}m", TextIcon.RADIUS, DETECT_RADIUS));
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
            super("역할: 지원 - 2",
                    "",
                    "§f▍ 아군을 치유하면 일정 시간동안 §a" + TextIcon.HEAL + " 회복§f합니다.",
                    "",
                    MessageFormat.format("§7{0} §f{1}초", TextIcon.DURATION, DURATION / 20.0),
                    MessageFormat.format("§a{0} §f{1}/초", TextIcon.HEAL, HEAL_PER_SECOND));
        }
    }
}
