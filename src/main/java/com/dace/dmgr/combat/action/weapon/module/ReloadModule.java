package com.dace.dmgr.combat.action.weapon.module;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.action.ActionBarStringUtil;
import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.weapon.Reloadable;
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
    /** 장탄수 */
    @Getter
    private final int capacity;
    /** 장전 시간 */
    private final Timespan reloadDuration;

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
     * @param weapon         대상 무기
     * @param capacity       장탄수. 1 이상의 값
     * @param reloadDuration 장전 시간
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    public ReloadModule(@NonNull Reloadable weapon, int capacity, @NonNull Timespan reloadDuration) {
        Validate.isTrue(capacity >= 1, "capacity >= 1 (%d)", capacity);

        this.weapon = weapon;
        this.remainingAmmo = capacity;
        this.capacity = capacity;
        this.reloadDuration = reloadDuration;

        weapon.addOnReset(this::resetRemainingAmmo);
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
        Validate.isTrue(amount >= 1, "amount >= 1 (%d)", amount);

        remainingAmmo = Math.max(0, remainingAmmo - amount);
        if (isReloading)
            cancel();
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

        long durationTicks = reloadDuration.toTicks();

        reloadTask = new IntervalTask(i -> {
            String message = MessageFormat.format("§c§l재장전... {0} §f[{1}초]",
                    StringFormUtil.getProgressBar(i, durationTicks, ChatColor.WHITE),
                    String.format("%.1f", Timespan.ofTicks(durationTicks - i).toSeconds()));

            weapon.getCombatUser().getUser().sendActionBar(message, Timespan.ofTicks(2));
            weapon.onReloadTick(i);
        }, () -> {
            cancel();

            weapon.getCombatUser().getUser().sendActionBar("§a§l재장전 완료", Timespan.ofTicks(6));

            resetRemainingAmmo();
            weapon.onReloadFinished();
        }, 1, durationTicks);

        weapon.addTask(reloadTask);
    }

    /**
     * 무기의 남은 탄약 수를 최대 장탄수로 초기화한다.
     */
    public void resetRemainingAmmo() {
        remainingAmmo = capacity;
    }

    /**
     * 무기의 재장전을 취소한다.
     */
    public void cancel() {
        isReloading = false;

        if (reloadTask != null)
            reloadTask.stop();
    }

    /**
     * 액션바에 무기의 탄약 상태를 표시하기 위한 진행 막대를 반환한다.
     *
     * @param length 진행 막대 길이 (글자 수). 1 이상의 값
     * @param symbol 막대 기호
     * @return 탄약 표시 진행 막대 문자열
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    @NonNull
    public String getActionBarProgressBar(int length, char symbol) {
        Validate.isTrue(length >= 1, "length >= 1 (%d)", length);
        return ActionBarStringUtil.getProgressBar(TextIcon.CAPACITY.toString(), remainingAmmo, capacity, length, symbol);
    }
}