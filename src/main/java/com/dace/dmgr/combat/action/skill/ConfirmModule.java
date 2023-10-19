package com.dace.dmgr.combat.action.skill;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.system.EntityInfoRegistry;
import com.dace.dmgr.system.task.TaskTimer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 스킬의 확인 모듈 클래스.
 *
 * <p>스킬이 {@link Confirmable}을 상속받는 클래스여야 한다.</p>
 *
 * @see Confirmable
 */
@RequiredArgsConstructor
public class ConfirmModule<T extends Skill & Confirmable> {
    /** 스킬 객체 */
    protected final T skill;
    /** 확인 키 */
    protected final ActionKey confirmKey;
    /** 취소 키 */
    protected final ActionKey cancelKey;

    /** 활성화 상태 */
    @Getter
    protected boolean toggled = false;

    /**
     * 스킬의 위치 지정 모드를 활성화한다.
     */
    public final void toggle() {
        toggled = !toggled;

        if (toggled) {
            onEnable();

            new TaskTimer(1) {
                @Override
                public boolean run(int i) {
                    if (EntityInfoRegistry.getCombatUser(skill.getCombatUser().getEntity()) == null)
                        return false;
                    if (!toggled)
                        return false;

                    onTick(i);

                    return true;
                }

                @Override
                public void onEnd(boolean cancelled) {
                    toggled = false;
                    onDisable();
                }
            };
        }
    }

    /**
     * 활성화 시 실행할 작업.
     */
    protected void onEnable() {
    }

    /**
     * {@link ConfirmModule#toggle()}에서 활성화 중에 매 tick마다 실행할 작업.
     */
    protected void onTick(int i) {
        skill.getCombatUser().getEntity().sendTitle("", "§7§l[" + confirmKey.getName() + "] §f사용     " + "§7§l[" + cancelKey.getName() + "] §f취소",
                0, 5, 5);
    }

    /**
     * 비활성화 시 실행할 작업.
     */
    protected void onDisable() {
    }
}
