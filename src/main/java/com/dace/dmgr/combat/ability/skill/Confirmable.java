package com.dace.dmgr.combat.ability.skill;

import com.dace.dmgr.combat.ability.handler.LeftClickHandler;
import com.dace.dmgr.combat.ability.handler.SlotHandler;
import com.dace.dmgr.combat.ability.skill.module.ConfirmModule;
import lombok.NonNull;

/**
 * 사용 전 확인이 필요한 스킬의 인터페이스.
 *
 * <p>{@link Confirmable#onSlot()}에서 {@link ConfirmModule#toggleCheck()}를 호출해야 한다.</p>
 *
 * <p>{@link Confirmable#onLeftClick()}에서 {@link ConfirmModule#accept()}를 호출해야 한다.</p>
 */
public interface Confirmable extends Skill, SlotHandler, LeftClickHandler {
    /**
     * @return 확인 모듈
     */
    @NonNull
    ConfirmModule getConfirmModule();

    /**
     * 확인 수락 시 실행할 작업.
     */
    void onAccept();
}