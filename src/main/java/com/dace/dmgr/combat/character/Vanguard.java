package com.dace.dmgr.combat.character;

import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActionInfoLore;
import com.dace.dmgr.combat.action.info.ActionInfoLore.Section.Format;
import com.dace.dmgr.combat.action.info.TraitInfo;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.Damageable;
import lombok.NonNull;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.Nullable;

/**
 * 역할군이 '돌격'인 전투원의 정보를 관리하는 클래스.
 */
public abstract class Vanguard extends Character {
    /** 수정자 ID */
    private static final String MODIFIER_ID = "RoleTrait1";

    /**
     * 돌격 역할군 전투원 정보 인스턴스를 생성한다.
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
    protected Vanguard(@Nullable Role subRole, @NonNull String name, @NonNull String nickname, @NonNull String skinName, char icon, int difficulty,
                       int health, double speedMultiplier, double hitboxMultiplier) {
        super(name, nickname, skinName, Role.VANGUARD, subRole, icon, difficulty, health, speedMultiplier, hitboxMultiplier);
    }

    @Override
    @MustBeInvokedByOverriders
    public void onTick(@NonNull CombatUser combatUser, long i) {
        combatUser.getKnockbackModule().getResistanceStatus().addModifier(MODIFIER_ID, RoleTrait1Info.KNOCKBACK_RESISTANCE);
        combatUser.getStatusEffectModule().getResistanceStatus().addModifier(MODIFIER_ID, RoleTrait1Info.STATUS_EFFECT_RESISTANCE);
    }

    @Override
    @MustBeInvokedByOverriders
    public void onKill(@NonNull CombatUser attacker, @NonNull Damageable victim, int score, boolean isFinalHit) {
        if (victim instanceof CombatUser)
            attacker.getStatusEffectModule().clearStatusEffect(false);
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
        /** 상태 효과 저항 */
        private static final int STATUS_EFFECT_RESISTANCE = 15;
        /** 넉백 저항 */
        private static final int KNOCKBACK_RESISTANCE = 30;

        private static final RoleTrait1Info instance = new RoleTrait1Info();

        private RoleTrait1Info() {
            super("역할: 돌격 - 1",
                    new ActionInfoLore(ActionInfoLore.Section
                            .builder("받는 모든 <:NEGATIVE_EFFECT:해로운 효과>의 시간과 <:KNOCKBACK:밀쳐내기> 효과가 감소합니다.")
                            .addValueInfo(TextIcon.NEGATIVE_EFFECT, Format.PERCENT, STATUS_EFFECT_RESISTANCE)
                            .addValueInfo(TextIcon.KNOCKBACK, Format.PERCENT, KNOCKBACK_RESISTANCE)
                            .build()
                    )
            );
        }
    }

    private static final class RoleTrait2Info extends TraitInfo {
        private static final RoleTrait2Info instance = new RoleTrait2Info();

        private RoleTrait2Info() {
            super("역할: 돌격 - 2",
                    new ActionInfoLore(ActionInfoLore.Section
                            .builder("적을 처치하면 모든 <:NEGATIVE_EFFECT:해로운 효과>를 제거합니다.")
                            .build()
                    )
            );
        }
    }
}
