package com.dace.dmgr.combat.character;

import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.TraitInfo;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.util.task.IntervalTask;
import com.dace.dmgr.util.task.TaskUtil;
import lombok.Getter;
import lombok.NonNull;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.Nullable;

import java.text.MessageFormat;

/**
 * 역할군이 '수호'인 전투원의 정보를 관리하는 클래스.
 */
public abstract class Guardian extends Character {
    /** 수정자 ID */
    private static final String MODIFIER_ID = "RoleTrait1";

    /**
     * 수호 역할군 전투원 정보 인스턴스를 생성한다.
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
    protected Guardian(@NonNull String name, @NonNull String nickname, @NonNull String skinName, char icon, int difficulty, int health,
                       double speedMultiplier, double hitboxMultiplier) {
        super(name, nickname, skinName, Role.GUARDIAN, icon, difficulty, health, speedMultiplier, hitboxMultiplier);
    }

    @Override
    @MustBeInvokedByOverriders
    public void onTick(@NonNull CombatUser combatUser, long i) {
        combatUser.getKnockbackModule().getResistanceStatus().addModifier(MODIFIER_ID, RoleTrait1Info.KNOCKBACK_RESISTANCE);
        combatUser.getDamageModule().getDefenseMultiplierStatus().addModifier(MODIFIER_ID, RoleTrait1Info.DEFENSE);
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
        /** 넉백 저항 */
        public static final int KNOCKBACK_RESISTANCE = 30;
        /** 방어력 */
        public static final int DEFENSE = 15;
        @Getter
        private static final RoleTrait1Info instance = new RoleTrait1Info();

        private RoleTrait1Info() {
            super("역할: 수호 - 1",
                    "",
                    "§f▍ 받는 §5" + TextIcon.KNOCKBACK + " 밀쳐내기 §f효과가 감소하며, 기본",
                    "§f▍ §6" + TextIcon.DEFENSE_INCREASE + " 방어력§f을 보유합니다.",
                    "",
                    MessageFormat.format("§5{0} §f{1}%", TextIcon.KNOCKBACK, KNOCKBACK_RESISTANCE),
                    MessageFormat.format("§6{0} §f{1}%", TextIcon.DEFENSE_INCREASE, DEFENSE));
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
            super("역할: 수호 - 2",
                    "",
                    "§f▍ 힐 팩을 사용하면 일정 시간동안 추가로",
                    "§f▍ §a" + TextIcon.HEAL + " 회복§f합니다.",
                    "",
                    MessageFormat.format("§7{0} §f{1}초", TextIcon.DURATION, DURATION / 20.0),
                    MessageFormat.format("§a{0} §f{1}", TextIcon.HEAL, HEAL));
        }
    }
}
