package com.dace.dmgr.combat.ability.handler;

import com.dace.dmgr.combat.ability.ActionKey;
import com.dace.dmgr.combat.ability.skill.ActiveSkill;
import lombok.NonNull;

/**
 * 슬롯 키의 동작 사용 이벤트를 처리하는 인터페이스.
 *
 * @see ActionKey#SLOT_1
 * @see ActionKey#SLOT_2
 * @see ActionKey#SLOT_3
 * @see ActionKey#SLOT_4
 * @see ActiveSkill
 */
public interface SlotHandler {
    /**
     * 슬롯 키를 반환한다.
     *
     * @return 슬롯 키
     */
    @NonNull
    ActionKey.Slot getSlot();

    /**
     * 슬롯 키를 눌렀을 때 실행할 작업.
     */
    void onSlot();
}
