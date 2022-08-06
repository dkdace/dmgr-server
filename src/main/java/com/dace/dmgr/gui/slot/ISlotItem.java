package com.dace.dmgr.gui.slot;

import org.bukkit.Material;

public interface ISlotItem {
    Material getMaterial();

    String getName();

    short getDamage();
}
