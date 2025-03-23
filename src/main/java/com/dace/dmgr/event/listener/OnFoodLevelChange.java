package com.dace.dmgr.event.listener;

import com.dace.dmgr.event.EventListener;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.FoodLevelChangeEvent;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OnFoodLevelChange extends EventListener<FoodLevelChangeEvent> {
    @Getter
    private static final OnFoodLevelChange instance = new OnFoodLevelChange();

    @Override
    @EventHandler
    protected void onEvent(@NonNull FoodLevelChangeEvent event) {
        event.setCancelled(true);
    }
}
