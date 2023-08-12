package com.dace.dmgr.combat.action;

import com.dace.dmgr.combat.action.skill.Skill;
import com.dace.dmgr.combat.action.weapon.Weapon;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.system.Cooldown;
import com.dace.dmgr.system.CooldownManager;
import com.dace.dmgr.system.EntityInfoRegistry;
import com.dace.dmgr.system.task.TaskTimer;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * 동작(무기, 스킬)의 상태를 관리하는 클래스.
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
     * 기본 사용 키를 반환한다.
     *
     * @return 기본 사용 키 목록
     */
    public abstract List<ActionKey> getDefaultActionKeys();

    /**
     * 기본 쿨타임을 반환한다.
     *
     * @return 기본 쿨타임 (tick)
     */
    public abstract long getDefaultCooldown();

    /**
     * 쿨타임의 남은 시간을 반환한다.
     *
     * @return 쿨타임 (tick)
     */
    public final long getCooldown() {
        return CooldownManager.getCooldown(this, Cooldown.SKILL_COOLDOWN);
    }

    /**
     * 쿨타임을 설정한다.
     *
     * @param cooldown 쿨타임 (tick). {@code -1}로 설정 시 무한 지속
     */
    public final void setCooldown(long cooldown) {
        if (isCooldownFinished()) {
            CooldownManager.setCooldown(this, Cooldown.SKILL_COOLDOWN, cooldown);
            runCooldown();
        } else
            CooldownManager.setCooldown(this, Cooldown.SKILL_COOLDOWN, cooldown);

        onCooldownSet();
    }

    /**
     * 쿨타임을 기본 쿨타임으로 설정한다.
     *
     * @see Action#getDefaultCooldown()
     */
    public final void setCooldown() {
        setCooldown(getDefaultCooldown());
    }

    /**
     * 쿨타임을 증가시킨다.
     *
     * @param cooldown 추가할 쿨타임 (tick)
     */
    public final void addCooldown(long cooldown) {
        setCooldown(getCooldown() + cooldown);
    }

    /**
     * 쿨타임 스케쥴러를 실행한다.
     */
    protected final void runCooldown() {
        new TaskTimer(1) {
            @Override
            public boolean run(int i) {
                if (EntityInfoRegistry.getCombatUser(combatUser.getEntity()) == null)
                    return false;

                onCooldownTick();

                if (isCooldownFinished()) {
                    onCooldownFinished();
                    return false;
                }

                return true;
            }
        };
    }

    /**
     * 쿨타임을 설정했을 때 실행할 작업.
     */
    protected void onCooldownSet() {
    }

    /**
     * 쿨타임이 진행할 때 (매 tick마다) 실행할 작업.
     */
    protected void onCooldownTick() {
    }

    /**
     * 쿨타임이 끝났을 때 실행할 작업.
     */
    protected void onCooldownFinished() {
    }

    /**
     * 쿨타임이 끝났는 지 확인한다.
     *
     * @return 쿨타임 종료 여부
     */
    public final boolean isCooldownFinished() {
        return getCooldown() == 0;
    }

    /**
     * 사용 시 호출되는 이벤트.
     *
     * @param actionKey 사용 키
     */
    public abstract void onUse(ActionKey actionKey);
}
