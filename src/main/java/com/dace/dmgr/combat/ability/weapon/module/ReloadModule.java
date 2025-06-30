package com.dace.dmgr.combat.ability.weapon.module;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.ability.weapon.Reloadable;
import com.dace.dmgr.util.StringFormUtil;
import com.dace.dmgr.util.task.IntervalTask;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.apache.commons.lang3.Validate;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.Nullable;

import java.text.MessageFormat;

/**
 * 무기의 재장전 모듈 클래스.
 *
 * @see Reloadable
 */
public final class ReloadModule {
    /** 무기 인스턴스 */
    private final Reloadable weapon;

    /** 재장전 작업을 처리하는 태스크 */
    @Nullable
    private IntervalTask reloadTask;
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
     * @param weapon 대상 무기
     */
    public ReloadModule(@NonNull Reloadable weapon) {
        this.weapon = weapon;
        this.remainingAmmo = weapon.getCapacity();

        weapon.addOnReset(this::resetRemainingAmmo);
    }

    /**
     * 지정한 양만큼 무기의 탄약을 소모한다.
     *
     * <p>탄약을 전부 소진하면 {@link Reloadable#onAmmoEmpty()}를 호출한다.</p>
     *
     * @param amount 탄약 소모량. 0 이상의 값
     * @return 탄약 소모 시 {@code true} 반환
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    public boolean consume(int amount) {
        Validate.isTrue(amount >= 0, "amount >= 0 (%d)", amount);

        if (remainingAmmo > 0 && remainingAmmo - amount >= 0) {
            remainingAmmo -= amount;

            if (isReloading)
                cancel();
            else if (remainingAmmo == 0)
                weapon.onAmmoEmpty();

            return true;
        }

        weapon.onAmmoEmpty();
        return false;
    }

    /**
     * 무기를 재장전한다.
     */
    public void reload() {
        if (!weapon.canReload() || isReloading)
            return;

        weapon.cancel();
        isReloading = true;

        long durationTicks = weapon.getReloadDuration().toTicks();

        reloadTask = new IntervalTask(i -> {
            String message = MessageFormat.format("§c§l재장전... {0} §f[{1}초]",
                    StringFormUtil.getProgressBar(i, durationTicks, ChatColor.WHITE),
                    String.format("%.1f", Timespan.ofTicks(durationTicks - i).toSeconds()));

            weapon.getCombatUser().getUser().sendActionBar(message, Timespan.ofTicks(2));
            weapon.onReloadTick(i);
        }, () -> {
            cancel();
            resetRemainingAmmo();

            weapon.getCombatUser().getUser().sendActionBar("§a§l재장전 완료", Timespan.ofTicks(6));
            weapon.onReloadFinished();
        }, 1, durationTicks);

        weapon.addTask(reloadTask);
    }

    /**
     * 무기의 남은 탄약 수를 최대 장탄수로 초기화한다.
     */
    public void resetRemainingAmmo() {
        remainingAmmo = weapon.getCapacity();
    }

    /**
     * 무기의 재장전을 취소한다.
     */
    public void cancel() {
        isReloading = false;

        if (reloadTask != null)
            reloadTask.stop();
    }
}
