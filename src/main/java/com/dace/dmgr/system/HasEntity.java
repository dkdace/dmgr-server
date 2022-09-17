package com.dace.dmgr.system;

import org.bukkit.entity.Entity;

public interface HasEntity<T extends Entity> {
    T getEntity();
}
