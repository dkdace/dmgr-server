package com.dace.dmgr.combat.action;

import com.dace.dmgr.combat.action.info.DynamicTraitInfo;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

/**
 * 특성의 상태를 관리하는 클래스.
 */
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public abstract class Trait {
    /** 사용자 플레이어 */
    @NonNull
    protected final CombatUser combatUser;
    /** 동적 특성 정보 인스턴스 */
    @NonNull
    protected final DynamicTraitInfo<?> traitInfo;
}
