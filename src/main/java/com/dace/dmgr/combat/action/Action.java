package com.dace.dmgr.combat.action;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;

/**
 * 상호작용(무기, 패시브 스킬, 액티브 스킬 등) 정보를 관리하는 클래스.
 */
@AllArgsConstructor
@Getter
public class Action {
    /** 이름 */
    protected final String name;
    /** 설명 아이템 객체 */
    protected final ItemStack itemStack;
}
