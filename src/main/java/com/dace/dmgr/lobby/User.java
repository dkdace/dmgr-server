package com.dace.dmgr.lobby;

import com.dace.dmgr.system.SkinManager;
import com.dace.dmgr.util.YamlUtil;
import fr.minuskube.netherboard.bukkit.BPlayerBoard;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;

import static com.dace.dmgr.system.HashMapList.combatUserMap;
import static com.dace.dmgr.system.HashMapList.userMap;

public class User {
    private final BPlayerBoard lobbySidebar;
    private final Player player;
    private final UserConfig userConfig;
    private final YamlUtil yamlUtil;
    private int xp = 0;
    private int level = 1;
    private int money = 0;
    private int rank = 100;
    private int rankPlay = 0;
    private boolean isRanked = false;
    //    private int rankPlacementPlay = 0;
    private int mmr = 100;
    private boolean resourcePack = false;
    private PlayerResourcePackStatusEvent.Status resourcePackStatus = null;

    public User(Player player) {
        this.player = player;
        this.userConfig = new UserConfig(player);
        this.lobbySidebar = new BPlayerBoard(player, "lobbySidebar");
        this.yamlUtil = new YamlUtil("User", player.getUniqueId().toString());
        this.xp = yamlUtil.loadValue("xp", xp);
        this.level = yamlUtil.loadValue("level", level);
        this.money = yamlUtil.loadValue("money", money);
        this.rank = yamlUtil.loadValue("rank", rank);
        this.rankPlay = yamlUtil.loadValue("rankPlay", rankPlay);
        this.isRanked = yamlUtil.loadValue("isRanked", isRanked);
        this.mmr = yamlUtil.loadValue("mmr", mmr);
        userMap.put(player, this);
    }

    public Player getPlayer() {
        return player;
    }

    public UserConfig getUserConfig() {
        return userConfig;
    }

    public int getXp() {
        return xp;
    }

    public void setXp(int xp) {
        if (xp < 0) xp = 0;
        this.xp = xp;
        yamlUtil.saveValue("xp", this.xp);
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        if (level < 0) level = 0;
        this.level = level;
        yamlUtil.saveValue("level", this.level);
    }

    public int getMoney() {
        return money;
    }

    public void setMoney(int money) {
        if (money < 0) money = 0;
        this.money = money;
        yamlUtil.saveValue("money", this.money);
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
        yamlUtil.saveValue("rank", this.rank);
    }

    public int getRankPlay() {
        return rankPlay;
    }

    public void setRankPlay(int rankPlay) {
        this.rankPlay = rankPlay;
        yamlUtil.saveValue("rankPlay", this.rankPlay);
    }

    public int getMMR() {
        return mmr;
    }

    public void setMMR(int mmr) {
        this.mmr = mmr;
        yamlUtil.saveValue("mmr", this.mmr);
    }

    public boolean isRanked() {
        return isRanked;
    }

    public void setRanked(boolean ranked) {
        this.isRanked = ranked;
        yamlUtil.saveValue("isRanked", this.isRanked);
    }

    public BPlayerBoard getLobbySidebar() {
        return lobbySidebar;
    }

    public boolean isResourcePack() {
        return resourcePack;
    }

    public void setResourcePack(boolean resourcePack) {
        this.resourcePack = resourcePack;
    }

    public PlayerResourcePackStatusEvent.Status getResourcePackStatus() {
        return resourcePackStatus;
    }

    public void setResourcePackStatus(PlayerResourcePackStatusEvent.Status resourcePackStatus) {
        this.resourcePackStatus = resourcePackStatus;
    }

    public void reset() {
        SkinManager.resetSkin(player);
        player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20);
        player.setHealth(20);
        player.getInventory().clear();
        player.setExp(0);
        player.setLevel(0);
        player.setWalkSpeed(0.2F);
        player.getActivePotionEffects().forEach((potionEffect ->
                player.removePotionEffect(potionEffect.getType())));
        combatUserMap.remove(player);
    }

    public String getLevelPrefix() {
        String color;

        if (level <= 100)
            color = "§f§l";
        else if (level <= 200)
            color = "§a§l";
        else if (level <= 300)
            color = "§b§l";
        else if (level <= 400)
            color = "§d§l";
        else
            color = "§e§l";

        return color + "[ Lv." + level + " ]";
    }

    public String getTierPrefix() {
        if (rank <= 0)
            return "§8§l[ F ]";
        else if (rank <= 300)
            return "§7§l[ E ]";
        else if (rank <= 600)
            return "§f§l[ D ]";
        else if (rank <= 1000)
            return "§a§l[ C ]";
        else if (rank <= 1500)
            return "§b§l[ B ]";
        else if (rank <= 2000)
            return "§d§l[ A ]";
        else
            return "§e§l[ S ]";
    }

    public int getNextLevelXp() {
        return 250 + (level * 50);
    }

    public int getNextTierScore() {
        if (rank <= 0)
            return 0;
        else if (rank <= 300)
            return 300;
        else if (rank <= 600)
            return 600;
        else if (rank <= 1000)
            return 1000;
        else if (rank <= 1500)
            return 1500;
        else
            return 2000;
    }

    public int getCurrentTierScore() {
        if (rank <= 300)
            return 0;
        else if (rank <= 600)
            return 300;
        else if (rank <= 1000)
            return 600;
        else if (rank <= 1500)
            return 1000;
        else
            return 1500;
    }
}
