package com.dace.dmgr.combat.character;

import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActionInfoLore;
import com.dace.dmgr.combat.action.info.ActionInfoLore.Section.Format;
import com.dace.dmgr.combat.action.info.TraitInfo;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.module.AbilityStatus;
import com.dace.dmgr.util.task.IntervalTask;
import lombok.NonNull;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.Nullable;

import java.util.function.LongConsumer;

/**
 * 역할군이 '수호'인 전투원의 정보를 관리하는 클래스.
 */
public abstract class Guardian extends Character {
    /** 넉백 저항 수정자 */
    private static final AbilityStatus.Modifier KNOCKBACK_RESISTANCE_MODIFIER = new AbilityStatus.Modifier(RoleTrait1Info.KNOCKBACK_RESISTANCE);
    /** 방어력 수정자 */
    private static final AbilityStatus.Modifier DEFENSE_MODIFIER = new AbilityStatus.Modifier(RoleTrait1Info.DEFENSE);

    /**
     * 수호 역할군 전투원 정보 인스턴스를 생성한다.
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
    protected Guardian(@Nullable Role subRole, @NonNull String name, @NonNull String nickname, @NonNull String skinName, char icon, int difficulty,
                       int health, double speedMultiplier, double hitboxMultiplier) {
        super(name, nickname, skinName, Role.GUARDIAN, subRole, icon, difficulty, health, speedMultiplier, hitboxMultiplier);
    }

    @Override
    @MustBeInvokedByOverriders
    public void onTick(@NonNull CombatUser combatUser, long i) {
        combatUser.getMoveModule().getResistanceStatus().addModifier(KNOCKBACK_RESISTANCE_MODIFIER);
        combatUser.getDamageModule().getDefenseMultiplierStatus().addModifier(DEFENSE_MODIFIER);
    }

    @Override
    @MustBeInvokedByOverriders
    public void onUseHealPack(@NonNull CombatUser combatUser) {
        combatUser.addTask(new IntervalTask((LongConsumer) i ->
                combatUser.getDamageModule().heal(combatUser, (double) RoleTrait2Info.HEAL / RoleTrait2Info.DURATION, false),
                1, RoleTrait2Info.DURATION));
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
        /** 넉백 저항 */
        private static final int KNOCKBACK_RESISTANCE = 30;
        /** 방어력 */
        private static final int DEFENSE = 15;

        private static final RoleTrait1Info instance = new RoleTrait1Info();

        private RoleTrait1Info() {
            super("역할: 수호 - 1",
                    new ActionInfoLore(ActionInfoLore.Section
                            .builder("받는 <:KNOCKBACK:밀쳐내기> 효과가 감소하며, 기본 <:DEFENSE_INCREASE:방어력>을 보유합니다.")
                            .addValueInfo(TextIcon.KNOCKBACK, Format.PERCENT, KNOCKBACK_RESISTANCE)
                            .addValueInfo(TextIcon.DEFENSE_INCREASE, Format.PERCENT, DEFENSE)
                            .build()
                    )
            );
        }
    }

    private static final class RoleTrait2Info extends TraitInfo {
        /** 치유량 */
        private static final int HEAL = 300;
        /** 지속시간 (tick) */
        private static final long DURATION = 2 * 20L;

        private static final RoleTrait2Info instance = new RoleTrait2Info();

        private RoleTrait2Info() {
            super("역할: 수호 - 2",
                    new ActionInfoLore(ActionInfoLore.Section
                            .builder("힐 팩을 사용하면 일정 시간동안 추가로 <:HEAL:회복>합니다.")
                            .addValueInfo(TextIcon.DURATION, Format.TIME, DURATION / 20.0)
                            .addValueInfo(TextIcon.HEAL, HEAL)
                            .build()
                    )
            );
        }
    }
}
