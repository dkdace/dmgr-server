package com.dace.dmgr.combat.action.weapon.module;

import com.dace.dmgr.combat.action.ActionModule;
import com.dace.dmgr.combat.action.weapon.Reloadable;
import com.dace.dmgr.combat.action.weapon.Swappable;
import com.dace.dmgr.combat.action.weapon.Weapon;
import com.dace.dmgr.system.Cooldown;
import com.dace.dmgr.system.CooldownManager;
import com.dace.dmgr.system.task.ActionTaskTimer;
import com.dace.dmgr.system.task.TaskManager;
import com.dace.dmgr.util.StringFormUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.ChatColor;

import java.text.MessageFormat;

/**
 * 무기의 2중 무기 모듈 클래스.
 *
 * <p>무기가 {@link Swappable}을 상속받는 클래스여야 한다.</p>
 *
 * @see Swappable
 */
@RequiredArgsConstructor
public final class SwapModule<T extends Weapon> implements ActionModule {
    /** 무기 객체 */
    private final Swappable<T> weapon;
    /** 보조무기 객체 */
    @Getter
    private final T subweapon;
    /** 무기 교체시간 */
    private final long swapDuration;

    /** 무기 전환 상태 */
    @Getter
    private Swappable.SwapState swapState = Swappable.SwapState.PRIMARY;

    /**
     * 이중 무기의 상태를 변경한다.
     *
     * @param targetState 변경할 상태
     */
    public void swapTo(Swappable.SwapState targetState) {
        if (getSwapState() == targetState || swapState == Swappable.SwapState.SWAPPING)
            return;
        if (targetState == Swappable.SwapState.SWAPPING)
            return;

        if (weapon instanceof Reloadable)
            ((Reloadable) weapon).getReloadModule().setReloading(false);
        if (swapState == Swappable.SwapState.SECONDARY)
            ((Reloadable) weapon.getSwapModule().getSubweapon()).getReloadModule().setReloading(false);

        swapState = Swappable.SwapState.SWAPPING;
        CooldownManager.setCooldown(weapon.getCombatUser(), Cooldown.WEAPON_SWAP, swapDuration);
        weapon.onSwapStart(targetState);

        TaskManager.addTask(weapon, new ActionTaskTimer(weapon.getCombatUser(), 1, swapDuration) {
            @Override
            public boolean onTickAction(int i) {
                if (getSwapState() != Swappable.SwapState.SWAPPING)
                    return false;

                String time = String.format("%.1f", (float) (repeat - i) / 20);
                weapon.getCombatUser().sendActionBar(MessageFormat.format(MESSAGES.SWAPPING, StringFormUtil.getProgressBar(i,
                        swapDuration, ChatColor.WHITE), time), 2);

                return true;
            }

            @Override
            public void onEnd(boolean cancelled) {
                CooldownManager.setCooldown(weapon.getCombatUser(), Cooldown.WEAPON_RELOAD, 0);
                if (cancelled)
                    return;

                swapState = targetState;
                weapon.getCombatUser().sendActionBar(MESSAGES.SWAP_COMPLETE, 8);
                weapon.onSwapFinished(targetState);
            }
        });
    }

    /**
     * 이중 무기의 모드를 반대 무기로 교체한다.
     */
    public void swap() {
        if (swapState == Swappable.SwapState.PRIMARY)
            swapTo(Swappable.SwapState.SECONDARY);
        else if (swapState == Swappable.SwapState.SECONDARY)
            swapTo(Swappable.SwapState.PRIMARY);
    }

    /**
     * 메시지 목록.
     */
    private interface MESSAGES {
        /** 교체 중 메시지 */
        String SWAPPING = "§c§l무기 교체 중... {0} §f[{1}초]";
        /** 교체 완료 메시지 */
        String SWAP_COMPLETE = "§a§l무기 교체 완료";
    }
}