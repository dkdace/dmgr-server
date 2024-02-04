package com.dace.dmgr.combat.action;

import com.dace.dmgr.Disposable;
import com.dace.dmgr.combat.action.info.ActionInfo;
import com.dace.dmgr.combat.entity.CombatUser;
import lombok.NonNull;
import org.bukkit.inventory.ItemStack;

/**
 * 동작(무기, 스킬)의 상태를 관리하는 인터페이스.
 *
 * @see AbstractAction
 */
public interface Action extends Disposable {
    /**
     * @return 플레이어 객체
     */
    @NonNull
    CombatUser getCombatUser();

    /**
     * @return 동작 정보 객체
     */
    @NonNull
    ActionInfo getActionInfo();

    /**
     * @return 아이템 객체
     */
    @NonNull
    ItemStack getItemStack();

    /**
     * 기본 사용 키 목록을 반환한다.
     *
     * @return 기본 사용 키 목록
     */
    @NonNull
    ActionKey @NonNull [] getDefaultActionKeys();

    /**
     * 기본 쿨타임을 반환한다.
     *
     * @return 기본 쿨타임 (tick)
     */
    long getDefaultCooldown();

    /**
     * 쿨타임의 남은 시간을 반환한다.
     *
     * @return 쿨타임 (tick)
     */
    long getCooldown();

    /**
     * 쿨타임을 설정한다.
     *
     * @param cooldown 쿨타임 (tick). {@code -1}로 설정 시 무한 지속
     */
    void setCooldown(long cooldown);

    /**
     * 쿨타임을 기본 쿨타임으로 설정한다.
     *
     * @see Action#getDefaultCooldown()
     */
    void setCooldown();

    /**
     * 쿨타임을 증가시킨다.
     *
     * @param cooldown 추가할 쿨타임 (tick)
     */
    void addCooldown(long cooldown);

    /**
     * 쿨타임이 끝났는 지 확인한다.
     *
     * @return 쿨타임 종료 여부
     */
    boolean isCooldownFinished();

    /**
     * 동작을 사용할 수 있는 지 확인한다.
     *
     * @return 사용 가능 여부
     */
    boolean canUse();

    /**
     * 동작 사용 시 실행할 작업.
     *
     * @param actionKey 사용 키
     */
    void onUse(@NonNull ActionKey actionKey);

    /**
     * 동작 사용이 취소당했을 때 실행할 작업.
     */
    void onCancelled();

    /**
     * 동작의 상태를 초기화한다.
     */
    void reset();
}
