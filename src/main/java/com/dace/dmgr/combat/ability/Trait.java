package com.dace.dmgr.combat.ability;

import com.dace.dmgr.combat.ability.info.DynamicTraitInfo;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import lombok.NonNull;
import org.bukkit.ChatColor;

/**
 * 특성의 상태를 관리하는 클래스.
 */
public abstract class Trait extends AbstractAbility {
    /**
     * 특성 인스턴스를 생성한다.
     *
     * @param combatUser       사용자 플레이어
     * @param dynamicTraitInfo 동적 특성 정보 인스턴스
     */
    protected Trait(@NonNull CombatUser combatUser, @NonNull DynamicTraitInfo<?> dynamicTraitInfo) {
        super(combatUser, dynamicTraitInfo.getName());
    }

    @Override
    @NonNull
    protected ChatColor getDisplayNameColor() {
        return ChatColor.AQUA;
    }
}
