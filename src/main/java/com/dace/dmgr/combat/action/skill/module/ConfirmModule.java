package com.dace.dmgr.combat.action.skill.module;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.ActionModule;
import com.dace.dmgr.combat.action.skill.Confirmable;
import com.dace.dmgr.system.task.ActionTaskTimer;
import com.dace.dmgr.system.task.TaskManager;
import com.dace.dmgr.util.MessageUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.text.MessageFormat;

/**
 * 스킬의 확인 모듈 클래스.
 *
 * <p>스킬이 {@link Confirmable}을 상속받는 클래스여야 한다.</p>
 *
 * @see Confirmable
 */
@RequiredArgsConstructor
public class ConfirmModule implements ActionModule {
    /** 스킬 객체 */
    protected final Confirmable skill;
    /** 수락 키 */
    protected final ActionKey acceptKey;
    /** 취소 키 */
    protected final ActionKey cancelKey;

    /** 확인 중 상태 */
    @Getter
    protected boolean isChecking = false;

    /**
     * 스킬 확인 모드를 활성화 또는 비활성화한다.
     */
    public final void toggleCheck() {
        isChecking = !isChecking;

        if (isChecking) {
            skill.onCheckEnable();
            onCheckEnable();

            TaskManager.addTask(skill, new ActionTaskTimer(skill.getCombatUser(), 1) {
                @Override
                public boolean onTickAction(int i) {
                    if (!isChecking)
                        return false;

                    MessageUtil.sendTitle(skill.getCombatUser().getEntity(), "", MessageFormat.format(MESSAGES.CHECKING, acceptKey.getName(), cancelKey.getName()),
                            0, 5, 5);
                    skill.onCheckTick(i);
                    onCheckTick(i);

                    return true;
                }

                @Override
                public void onEnd(boolean cancelled) {
                    isChecking = false;
                    onCheckDisable();
                    skill.onCheckDisable();
                }
            });
        }
    }

    protected void onCheckEnable() {
    }

    protected void onCheckTick(int i) {
    }

    protected void onCheckDisable() {
    }

    /**
     * 메시지 목록.
     */
    private interface MESSAGES {
        /** 확인 중 메시지 */
        String CHECKING = "§7§l[{0}] §f사용     §7§l[{1}] §f취소";
    }
}
