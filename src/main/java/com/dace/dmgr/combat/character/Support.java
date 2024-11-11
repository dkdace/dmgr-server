package com.dace.dmgr.combat.character;

import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActionInfoLore;
import com.dace.dmgr.combat.action.info.ActionInfoLore.Section.Format;
import com.dace.dmgr.combat.action.info.TraitInfo;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.Healable;
import com.dace.dmgr.user.User;
import com.dace.dmgr.util.CooldownUtil;
import com.dace.dmgr.util.task.IntervalTask;
import com.dace.dmgr.util.task.TaskUtil;
import lombok.NonNull;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.Nullable;

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
    protected Support(@Nullable Role subRole, @NonNull String name, @NonNull String nickname, @NonNull String skinName, char icon, int difficulty,
                      int health, double speedMultiplier, double hitboxMultiplier) {
        super(name, nickname, skinName, Role.SUPPORT, subRole, icon, difficulty, health, speedMultiplier, hitboxMultiplier);
    }

    @Override
    @MustBeInvokedByOverriders
    public void onTick(@NonNull CombatUser combatUser, long i) {
        if (i % 5 == 0) {
            boolean activate = combatUser.getEntity().getWorld().getPlayers().stream()
                    .map(target -> CombatUser.fromUser(User.fromPlayer(target)))
                    .anyMatch(target -> target != null && !target.isEnemy(combatUser)
                            && target.getDamageModule().getHealth() <= target.getDamageModule().getMaxHealth() / 2
                            && target.getEntity().getLocation().distance(combatUser.getEntity().getLocation()) >= RoleTrait1Info.DETECT_RADIUS);

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

    private static final class RoleTrait1Info extends TraitInfo {
        /** 이동속도 증가량 */
        private static final int SPEED = 20;
        /** 감지 범위 */
        private static final int DETECT_RADIUS = 20;

        private static final RoleTrait1Info instance = new RoleTrait1Info();

        private RoleTrait1Info() {
            super("역할: 지원 - 1",
                    new ActionInfoLore(ActionInfoLore.Section
                            .builder("체력이 절반 이하인 아군이 범위 밖에 있을 때 <:WALK_SPEED_INCREASE:이동 속도>가 빨라집니다.")
                            .addValueInfo(TextIcon.WALK_SPEED_INCREASE, Format.PERCENT, SPEED)
                            .addValueInfo(TextIcon.RADIUS, Format.DISTANCE, DETECT_RADIUS)
                            .build()
                    )
            );
        }
    }

    private static final class RoleTrait2Info extends TraitInfo {
        /** 초당 치유량 */
        private static final int HEAL_PER_SECOND = 50;
        /** 지속시간 (tick) */
        private static final long DURATION = 3 * 20L;

        private static final RoleTrait2Info instance = new RoleTrait2Info();

        private RoleTrait2Info() {
            super("역할: 지원 - 2",
                    new ActionInfoLore(ActionInfoLore.Section
                            .builder("아군을 치유하면 일정 시간동안 <:HEAL:회복>합니다.")
                            .addValueInfo(TextIcon.DURATION, Format.TIME, DURATION / 20.0)
                            .addValueInfo(TextIcon.HEAL, Format.PER_SECOND, HEAL_PER_SECOND)
                            .build()
                    )
            );
        }
    }
}
