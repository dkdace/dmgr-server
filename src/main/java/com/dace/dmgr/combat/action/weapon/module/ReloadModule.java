package com.dace.dmgr.combat.action.weapon.module;

import com.dace.dmgr.combat.action.weapon.Reloadable;
import com.dace.dmgr.util.StringFormUtil;
import com.dace.dmgr.util.Timespan;
import com.dace.dmgr.util.task.IntervalTask;
import com.dace.dmgr.util.task.TaskUtil;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.bukkit.ChatColor;

import java.text.MessageFormat;

/**
 * 무기의 재장전 모듈 클래스.
 *
 * <p>무기가 {@link Reloadable}을 상속받는 클래스여야 한다.</p>
 *
 * @see Reloadable
 */
public final class ReloadModule {
    /** 무기 객체 */
    @NonNull
    private final Reloadable weapon;
    /** 장탄수 */
    private final int capacity;
    /** 장전 시간 (tick) */
    private final long reloadDuration;

    /** 남은 탄약 수 */
    @Getter
    @Setter
    private int remainingAmmo;
    /** 재장전 상태 */
    @Getter
    private boolean isReloading = false;

    /**
     * 재장전 모듈 인스턴스를 생성한다.
     *
     * @param weapon         대상 무기
     * @param capacity       장탄수. 1 이상의 값
     * @param reloadDuration 장전 시간 (tick). 0 이상의 값
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    public ReloadModule(@NonNull Reloadable weapon, int capacity, long reloadDuration) {
        if (capacity < 1)
            throw new IllegalArgumentException("'capacity'가 1 이상이어야 함");
        if (reloadDuration < 0)
            throw new IllegalArgumentException("'reloadDuration'이 0 이상이어야 함");

        this.weapon = weapon;
        this.remainingAmmo = capacity;
        this.capacity = capacity;
        this.reloadDuration = reloadDuration;
    }

    /**
     * 지정한 양만큼 무기의 탄약을 소모한다.
     *
     * <p>탄약을 전부 소진하면 {@link Reloadable#onAmmoEmpty()}를 호출한다.</p>
     *
     * @param amount 탄약 소모량. 1 이상의 값
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    public void consume(int amount) {
        if (amount < 1)
            throw new IllegalArgumentException("'amount'가 1 이상이어야 함");

        remainingAmmo = Math.max(0, remainingAmmo - amount);
        if (isReloading)
            isReloading = false;
        else if (remainingAmmo == 0)
            weapon.onAmmoEmpty();
    }

    /**
     * 무기를 재장전한다.
     */
    public void reload() {
        if (!weapon.canReload() || isReloading)
            return;

        isReloading = true;

        TaskUtil.addTask(weapon, new IntervalTask(i -> {
            if (!isReloading)
                return false;

            String time = String.format("%.1f", (reloadDuration - i) / 20.0);
            weapon.getCombatUser().getUser().sendActionBar(MessageFormat.format("§c§l재장전... {0} §f[{1}초]",
                    StringFormUtil.getProgressBar(i, reloadDuration, ChatColor.WHITE), time), Timespan.ofTicks(2));
            weapon.onReloadTick(i);

            return true;
        }, isCancelled -> {
            if (isCancelled)
                return;

            weapon.getCombatUser().getUser().sendActionBar("§a§l재장전 완료", Timespan.ofTicks(6));

            remainingAmmo = capacity;
            isReloading = false;
            weapon.onReloadFinished();
        }, 1, reloadDuration));
    }

    /**
     * 무기의 재장전을 취소한다.
     */
    public void cancel() {
        isReloading = false;
    }
}