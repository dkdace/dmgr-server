package com.dace.dmgr.combat.action.weapon;

import com.dace.dmgr.system.Cooldown;
import com.dace.dmgr.system.CooldownManager;
import com.dace.dmgr.system.task.ActionTaskTimer;
import com.dace.dmgr.system.task.TaskManager;
import com.dace.dmgr.util.StringFormUtil;
import org.bukkit.ChatColor;

/**
 * 주무기와 보조무기의 전환이 가능한 2중 무기의 인터페이스.
 *
 * @param <T> {@link Weapon}을 상속받는 보조무기
 */
public interface Swappable<T extends Weapon> extends Weapon {
    /**
     * 보조무기를 반환한다.
     *
     * @return 보조무기 객체
     */
    T getSubweapon();

    /**
     * 무기 교체시간을 반환한다.
     *
     * @return 무기 교체시간 (tick)
     */
    long getSwapDuration();

    /**
     * @return 무기 전환 상태
     */
    SwapState getSwapState();

    /**
     * @param swapState 무기 전환 상태
     */
    void setSwapState(SwapState swapState);

    /**
     * 이중 무기의 전환 상태를 변경한다.
     *
     * @param targetState 변경할 상태
     */
    default void swapTo(SwapState targetState) {
        if (getSwapState() == targetState || getSwapState() == SwapState.SWAPPING)
            return;
        if (targetState == SwapState.SWAPPING)
            return;

        if (this instanceof Reloadable)
            ((Reloadable) this).setReloading(false);
        if (getSwapState() == SwapState.SECONDARY)
            ((Reloadable) this.getSubweapon()).setReloading(false);
        setSwapState(SwapState.SWAPPING);
        CooldownManager.setCooldown(getCombatUser(), Cooldown.WEAPON_SWAP, getSwapDuration());
        onSwapStart(targetState);

        TaskManager.addTask(this, new ActionTaskTimer(getCombatUser(), 1, getSwapDuration()) {
            @Override
            public boolean onTickAction(int i) {
                if (getSwapState() != SwapState.SWAPPING)
                    return false;

                String time = String.format("%.1f", (float) (repeat - i) / 20);
                getCombatUser().sendActionBar("§c§l무기 교체 중... " + StringFormUtil.getProgressBar(i, getSwapDuration(),
                        ChatColor.WHITE) + " §f[" + time + "초]", 2);

                return true;
            }

            @Override
            public void onEnd(boolean cancelled) {
                CooldownManager.setCooldown(getCombatUser(), Cooldown.WEAPON_RELOAD, 0);
                if (cancelled)
                    return;

                getCombatUser().sendActionBar("§a§l무기 교체 완료", 8);
                setSwapState(targetState);
                onSwapFinished(targetState);
            }
        });
    }

    /**
     * 이중 무기의 모드를 반대 무기로 교체한다.
     */
    default void swap() {
        if (getSwapState() == SwapState.PRIMARY)
            swapTo(SwapState.SECONDARY);
        else if (getSwapState() == SwapState.SECONDARY)
            swapTo(SwapState.PRIMARY);
    }

    /**
     * {@link Swappable#swap()}에서 전환을 시작할 때 실행할 작업.
     *
     * @param swapState 변경할 상태
     */
    void onSwapStart(SwapState swapState);

    /**
     * {@link Swappable#swap()}에서 전환이 끝났을 때 실행할 작업.
     *
     * @param swapState 변경할 상태
     */
    void onSwapFinished(SwapState swapState);

    /**
     * 무기 전환 상태 목록.
     */
    enum SwapState {
        /** 주무기 사용 중 */
        PRIMARY,
        /** 보조무기 사용 중 */
        SECONDARY,
        /** 교체 중 */
        SWAPPING,
    }
}