package com.dace.dmgr.combat.action.weapon.module;

import com.dace.dmgr.combat.action.weapon.Reloadable;
import com.dace.dmgr.combat.action.weapon.Swappable;
import com.dace.dmgr.combat.action.weapon.Weapon;
import com.dace.dmgr.util.Cooldown;
import com.dace.dmgr.util.CooldownUtil;
import com.dace.dmgr.util.StringFormUtil;
import com.dace.dmgr.util.task.IntervalTask;
import com.dace.dmgr.util.task.TaskUtil;
import lombok.Getter;
import lombok.NonNull;
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
public final class SwapModule<T extends Weapon> {
    /** 무기 객체 */
    @NonNull
    private final Swappable<T> weapon;
    /** 보조무기 객체 */
    @NonNull
    @Getter
    private final T subweapon;
    /** 무기 교체시간 */
    private final long swapDuration;

    /** 무기 전환 상태 */
    @NonNull
    @Getter
    private Swappable.SwapState swapState = Swappable.SwapState.PRIMARY;

    /**
     * 이중 무기의 상태를 변경한다.
     *
     * @param targetState 변경할 상태
     */
    public void swapTo(@NonNull Swappable.SwapState targetState) {
        if (getSwapState() == targetState || swapState == Swappable.SwapState.SWAPPING)
            return;
        if (targetState == Swappable.SwapState.SWAPPING)
            return;

        if (weapon instanceof Reloadable)
            ((Reloadable) weapon).getReloadModule().setReloading(false);
        if (swapState == Swappable.SwapState.SECONDARY)
            ((Reloadable) weapon.getSwapModule().getSubweapon()).getReloadModule().setReloading(false);

        swapState = Swappable.SwapState.SWAPPING;
        CooldownUtil.setCooldown(weapon.getCombatUser(), Cooldown.WEAPON_SWAP, swapDuration);
        weapon.onSwapStart(targetState);

        TaskUtil.addTask(weapon, new IntervalTask(i -> {
            if (getSwapState() != Swappable.SwapState.SWAPPING)
                return false;

            String time = String.format("%.1f", (swapDuration - i) / 20.0);
            weapon.getCombatUser().getUser().sendActionBar(MessageFormat.format("§c§l무기 교체 중... {0} §f[{1}초]",
                    StringFormUtil.getProgressBar(i, swapDuration, ChatColor.WHITE), time), 2);

            return true;
        }, isCancelled -> {
            CooldownUtil.setCooldown(weapon.getCombatUser(), Cooldown.WEAPON_RELOAD, 0);
            if (isCancelled)
                return;

            swapState = targetState;
            weapon.getCombatUser().getUser().sendActionBar("§a§l무기 교체 완료", 8);
            weapon.onSwapFinished(targetState);
        }, 1, swapDuration));
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
}