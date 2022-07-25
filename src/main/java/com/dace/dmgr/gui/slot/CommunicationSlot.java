package com.dace.dmgr.gui.slot;

import org.bukkit.Material;

public enum CommunicationSlot implements ISlotItem {
    REQ_HEAL((short) 5, "§a치료 요청"),
    SHOW_ULT((short) 5, "§a궁극기 상태"),
    REQ_RALLY((short) 5, "§a집결 요청");

    private final static Material material = Material.STAINED_GLASS_PANE;
    private final String name;
    private final short damage;

    CommunicationSlot(short damage, String name) {
        this.name = name;
        this.damage = damage;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public short getDamage() {
        return damage;
    }
}
