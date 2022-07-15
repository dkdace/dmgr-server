package com.dace.dmgr.system;

import com.dace.dmgr.DMGR;
import com.dace.dmgr.data.model.User;
import com.dace.dmgr.gui.menu.MainMenu;
import com.dace.dmgr.gui.menu.event.MainMenuEvent;
import com.dace.dmgr.lobby.Chat;
import com.dace.dmgr.lobby.ResourcePack;
import com.dace.dmgr.lobby.ServerJoin;
import com.dace.dmgr.lobby.ServerQuit;
import com.mewin.WGRegionEvents.events.RegionEnterEvent;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.event.server.TabCompleteEvent;
import org.bukkit.event.weather.WeatherChangeEvent;

import static com.dace.dmgr.system.EntityList.userList;

public class EventListener implements Listener {
    @EventHandler
    public void onPlayerJoin(org.bukkit.event.player.PlayerJoinEvent event) {
        Player player = event.getPlayer();
        User user = new User(player);
        userList.put(player.getUniqueId(), user);

        ServerJoin.event(event, user);
    }

    @EventHandler
    public void onPlayerQuit(org.bukkit.event.player.PlayerQuitEvent event) {
        Player player = event.getPlayer();
        User user = userList.get(player.getUniqueId());
        userList.remove(player.getUniqueId());

        ServerQuit.event(event, user);
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        User user = userList.get(event.getPlayer().getUniqueId());

        Chat.event(event, user);
    }

    @EventHandler
    public void onPlayerResourcepack(PlayerResourcePackStatusEvent event) {
        User user = userList.get(event.getPlayer().getUniqueId());

        ResourcePack.event(event, user);
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
        User user = userList.get(event.getWhoClicked().getUniqueId());

        new MainMenuEvent().event(event, user);
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

    @EventHandler
    public void onRegionEnter(RegionEnterEvent event) {
        Player player = event.getPlayer();
        ProtectedRegion region = event.getRegion();
        player.sendMessage("Hello player, " + region.getId());
    }
}
