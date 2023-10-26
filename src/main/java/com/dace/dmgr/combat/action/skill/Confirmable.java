package com.dace.dmgr.combat.action.skill;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.system.task.ActionTaskTimer;
import com.dace.dmgr.system.task.TaskManager;

import java.text.MessageFormat;

/**
 * 사용 전 확인이 필요한 스킬의 인터페이스.
 *
 * @see LocationConfirmable
 */
public interface Confirmable extends Skill {
    /**
     * @return 수락 키
     */
    ActionKey getAcceptKey();

    /**
     * @return 취소 키
     */
    ActionKey getCancelKey();

    /**
     * @return 확인 중 상태
     */
    boolean isChecking();

    /**
     * @param checking 확인 중 상태
     */
    void setChecking(boolean checking);


    /**
     * 스킬 확인 기능을 활성화 또는 비활성화한다.
     */
    default void toggleCheck() {
        setChecking(!isChecking());

        if (isChecking()) {
            onCheckEnable();

            TaskManager.addTask(this, new ActionTaskTimer(getCombatUser(), 1) {
                @Override
                public boolean onTickAction(int i) {
                    if (!isChecking())
                        return false;

                    onCheckTick(i);

                    return true;
                }

                @Override
                public void onEnd(boolean cancelled) {
                    setChecking(false);
                    onCheckDisable();
                }
            });
        }
    }

    /**
     * {@link Confirmable#toggleCheck()}에서 활성화 시 실행할 작업.
     */
    void onCheckEnable();

    /**
     * {@link Confirmable#toggleCheck()}에서 활성화 중에 매 tick마다 실행할 작업.
     *
     * @param i 인덱스
     */
    default void onCheckTick(int i) {
        getCombatUser().getEntity().sendTitle("", MessageFormat.format(MESSAGES.CHECKING, getAcceptKey().getName(), getCancelKey().getName()),
                0, 5, 5);
    }

    /**
     * {@link Confirmable#toggleCheck()}에서 비활성화 시 실행할 작업.
     */
    void onCheckDisable();

    /**
     * 수락 시 실행할 작업.
     */
    void onAccept();

    /**
     * 메시지 목록.
     */
    class MESSAGES {
        /** 확인 중 메시지 */
        static final String CHECKING = "§7§l[{0}] §f사용     §7§l[{1}] §f취소";
    }
}