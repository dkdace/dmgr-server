package com.dace.dmgr.combat.action.info;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;

/**
 * 동작(무기, 패시브 스킬, 액티브 스킬 등) 정보를 관리하는 클래스.
 *
 * @see WeaponInfo
 * @see SkillInfo
 * @see TraitInfo
 */
@AllArgsConstructor
@Getter
public class ActionInfo {
    /** 이름 */
    protected final String name;
    /** 설명 아이템 객체 */
    protected final ItemStack itemStack;
}
