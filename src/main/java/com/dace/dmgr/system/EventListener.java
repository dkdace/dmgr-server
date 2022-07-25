package com.dace.dmgr.system;

import com.dace.dmgr.DMGR;
import com.dace.dmgr.event.*;
import com.dace.dmgr.gui.menu.event.MainMenuEvent;
import com.shampaggon.crackshot.events.WeaponPreShootEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.server.TabCompleteEvent;
import org.bukkit.event.weather.WeatherChangeEvent;

public class EventListener implements Listener {
    @EventHandler
    public void onPlayerJoin(org.bukkit.event.player.PlayerJoinEvent event) {
        ServerJoin.event(event, event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(org.bukkit.event.player.PlayerQuitEvent event) {
        ServerQuit.event(event, event.getPlayer());
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Chat.event(event, event.getPlayer());
    }

    @EventHandler
    public void onPlayerResourcepack(PlayerResourcePackStatusEvent event) {
        ResourcePack.event(event, event.getPlayer());
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();

        if (!player.isOp()) event.setCancelled(true);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();

        if (!player.isOp()) event.setCancelled(true);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();

        InventoryClick.event(event, player);
        MainMenuEvent.getInstance().event(event, player);
    }

    @EventHandler
    public void onWeatherChange(WeatherChangeEvent event) {
        if (event.toWeatherState()) event.setCancelled(true);
    }

    @EventHandler
    public void onBlockBurn(BlockBurnEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onBlockFade(BlockFadeEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();

        if (!player.isOp()) event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerSwapHand(PlayerSwapHandItemsEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onTabCompleteEvent(TabCompleteEvent event) {
        if (event.getSender() instanceof Player) {
            Player player = (Player) event.getSender();

            if (!player.isOp()) {
                if (event.getBuffer().split(" ").length == 1) {
                    player.sendMessage(DMGR.CHAT_WARN_PREFIX + "금지된 행동입니다.");
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler()
    public void onPlayerCommandEvent(PlayerCommandPreprocessEvent event) {
        if (event.getMessage().equals("spawn"))
            event.setCancelled(true);
    }

    @EventHandler()
    public void onWeaponShoot(WeaponPreShootEvent event) {
        WeaponShoot.event(event, event.getPlayer());
    }

//    @EventHandler
//    public void onRegionEnter(RegionEnterEvent event) {
//        Player player = event.getPlayer();
//        ProtectedRegion region = event.getRegion();
//        player.sendMessage("Hello player, " + region.getId());
//    }
}
