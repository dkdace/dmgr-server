package com.dace.dmgr.combat.action;

import com.dace.dmgr.combat.action.skill.Skill;
import com.dace.dmgr.combat.action.weapon.Weapon;
import com.dace.dmgr.combat.entity.CombatUser;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;

/**
 * 상호작용(무기, 스킬)의 상태를 관리하는 클래스.
 *
 * @see Weapon
 * @see Skill
 */
@Getter
public abstract class Action {
    /** 플레이어 객체 */
    protected final CombatUser combatUser;
    /** 상호작용 정보 객체 */
    protected final ActionInfo actionInfo;
    /** 아이템 */
    protected ItemStack itemStack;

    protected Action(CombatUser combatUser, ActionInfo actionInfo) {
        this.combatUser = combatUser;
        this.actionInfo = actionInfo;
        this.itemStack = actionInfo.getItemStack().clone();
    }

    /**
     * 쿨타임을 반환한다.
     *
     * @return 쿨타임 (tick)
     */
    public abstract long getCooldown();

    /**
     * 쿨타임을 설정한다.
     *
     * @param cooldown 쿨타임 (tick).
     */
    public abstract void setCooldown(long cooldown);

    /**
     * 쿨타임이 끝났는 지 확인한다.
     *
     * @return 쿨타임 종료 여부
     */
    public abstract boolean isCooldownFinished();

    /**
     * 사용 이벤트를 호출한다.
     *
     * @param actionKey 상호작용 키
     */
    public abstract void onUse(ActionKey actionKey);
}
