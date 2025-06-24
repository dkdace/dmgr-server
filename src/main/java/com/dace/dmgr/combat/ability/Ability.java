package com.dace.dmgr.combat.ability;

import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

/**
 * 능력(무기, 스킬, 특성 등)의 상태를 관리하는 인터페이스.
 */
public interface Ability {
    /**
     * 능력의 사용자를 반환한다.
     *
     * @return 사용자 플레이어
     */
    @NonNull
    CombatUser getCombatUser();

    /**
     * @return 능력 표시 이름
     */
    @NonNull
    String getDisplayName();

    /**
     * 액션바에 능력 상태를 표시하기 위한 상태 표시 처리기를 반환한다.
     *
     * @return 능력 상태 표시 처리기. {@code null} 반환 시 표시 대상에서 제외
     * @implSpec {@code null}
     * @see ActionBarDisplay
     */
    @Nullable
    default ActionBarDisplay getActionBarDisplay() {
        return null;
    }
}
