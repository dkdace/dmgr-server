package com.dace.dmgr.combat.character;

import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.TraitInfo;
import com.dace.dmgr.combat.entity.CombatUser;
import lombok.Getter;
import lombok.NonNull;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.Nullable;

import java.text.MessageFormat;

/**
 * 역할군이 '돌격'인 전투원의 정보를 관리하는 클래스.
 */
public abstract class Vanguard extends Character {
    /** 수정자 ID */
    private static final String MODIFIER_ID = "RoleTrait1";

    /**
     * 돌격 역할군 전투원 정보 인스턴스를 생성한다.
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
    protected Vanguard(@NonNull String name, @NonNull String nickname, @NonNull String skinName, char icon, int difficulty, int health,
                       double speedMultiplier, double hitboxMultiplier) {
        super(name, nickname, skinName, Role.VANGUARD, icon, difficulty, health, speedMultiplier, hitboxMultiplier);
    }

    @Override
    @MustBeInvokedByOverriders
    public void onTick(@NonNull CombatUser combatUser, long i) {
        combatUser.getKnockbackModule().getResistanceStatus().addModifier(MODIFIER_ID, RoleTrait1Info.KNOCKBACK_RESISTANCE);
        combatUser.getStatusEffectModule().getResistanceStatus().addModifier(MODIFIER_ID, RoleTrait1Info.STATUS_EFFECT_RESISTANCE);
    }

    @Override
    @MustBeInvokedByOverriders
    public void onUseHealPack(@NonNull CombatUser combatUser) {
        combatUser.getStatusEffectModule().clearStatusEffect(false);
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
        /** 상태 효과 저항 */
        public static final int STATUS_EFFECT_RESISTANCE = 15;
        @Getter
        private static final RoleTrait1Info instance = new RoleTrait1Info();

        private RoleTrait1Info() {
            super("역할: 돌격 - 1",
                    "",
                    "§f▍ 받는 모든 §5" + TextIcon.NEGATIVE_EFFECT + " 해로운 효과§f의 시간과 §5" + TextIcon.KNOCKBACK + " 밀쳐내기",
                    "§f▍ 효과가 감소합니다.",
                    "",
                    MessageFormat.format("§5{0} §f{1}%", TextIcon.NEGATIVE_EFFECT, STATUS_EFFECT_RESISTANCE),
                    MessageFormat.format("§5{0} §f{1}%", TextIcon.KNOCKBACK, KNOCKBACK_RESISTANCE));
        }
    }

    public static final class RoleTrait2Info extends TraitInfo {
        @Getter
        private static final RoleTrait2Info instance = new RoleTrait2Info();

        private RoleTrait2Info() {
            super("역할: 돌격 - 2",
                    "",
                    "§f▍ 힐 팩을 사용하면 모든 §5" + TextIcon.NEGATIVE_EFFECT + " 해로운 효과§f를",
                    "§f▍ 제거합니다.");
        }
    }
}
