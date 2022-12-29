package com.dace.dmgr.combat.action;

import org.bukkit.inventory.ItemStack;

/**
 * 상호작용(무기, 패시브 스킬, 액티브 스킬 등) 정보를 관리하는 클래스.
 */
public class Action {
    /** 이름 */
    protected final String name;
    /** 설명 아이템 객체 */
    protected final ItemStack itemStack;

    /**
     * 상호작용 정보 인스턴스를 생성한다.
     *
     * @param name      이름
     * @param itemStack 대상 아이템
     */
    protected Action(String name, ItemStack itemStack) {
        this.name = name;
        this.itemStack = itemStack;
    }

    public String getName() {
        return name;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }
}
