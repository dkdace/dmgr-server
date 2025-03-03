package com.dace.dmgr.event.listener;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.info.WeaponInfo;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.event.EventListener;
import com.dace.dmgr.user.User;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.material.Openable;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OnPlayerInteract extends EventListener<PlayerInteractEvent> {
    @Getter
    private static final OnPlayerInteract instance = new OnPlayerInteract();

    /**
     * 지정한 블록이 상호작용할 수 있는 블록인지 확인한다.
     *
     * @param block 확인할 블록
     * @return 상호작용 가능하면 {@code true} 반환
     */
    private static boolean isInteractable(@NonNull Block block) {
        BlockState blockState = block.getState();
        if (blockState instanceof Container || blockState.getData() instanceof Openable)
            return true;

        switch (block.getType()) {
            case CAKE_BLOCK:
            case BEACON:
            case ANVIL:
            case ENDER_CHEST:
            case NOTE_BLOCK:
            case BED_BLOCK:
            case WOOD_BUTTON:
            case STONE_BUTTON:
            case LEVER:
            case DAYLIGHT_DETECTOR:
            case DAYLIGHT_DETECTOR_INVERTED:
            case DIODE_BLOCK_OFF:
            case DIODE_BLOCK_ON:
            case REDSTONE_COMPARATOR_OFF:
            case REDSTONE_COMPARATOR_ON:
                return true;
            default:
                return false;
        }
    }

    @Override
    @EventHandler
    protected void onEvent(@NonNull PlayerInteractEvent event) {
        CombatUser combatUser = CombatUser.fromUser(User.fromPlayer(event.getPlayer()));
        if (combatUser == null)
            return;

        if (event.hasBlock()) {
            if (event.getAction() == Action.LEFT_CLICK_BLOCK || isInteractable(event.getClickedBlock()))
                event.setCancelled(true);
            else if (event.getItem() != null && event.getItem().getType() == WeaponInfo.MATERIAL)
                switch (event.getClickedBlock().getType()) {
                    case GRASS:
                    case DIRT:
                    case GRASS_PATH:
                        event.setCancelled(true);
                        break;
                    default:
                        break;
                }
        }

        switch (event.getAction()) {
            case LEFT_CLICK_AIR:
            case LEFT_CLICK_BLOCK:
                combatUser.useAction(ActionKey.LEFT_CLICK);
                break;
            case RIGHT_CLICK_AIR:
            case RIGHT_CLICK_BLOCK:
                combatUser.useAction(ActionKey.RIGHT_CLICK);
                break;
            default:
                break;
        }
    }
}
