package com.dace.dmgr.user;

import com.dace.dmgr.system.SkinManager;
import com.dace.dmgr.util.YamlModel;
import fr.minuskube.netherboard.bukkit.BPlayerBoard;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;

import static com.dace.dmgr.system.EntityList.combatUserList;
import static com.dace.dmgr.system.EntityList.userList;

public class User extends YamlModel {
    public final BPlayerBoard lobbySidebar;
    private final Player player;
    private final UserConfig userConfig;
    public boolean resourcePack = false;
    public PlayerResourcePackStatusEvent.Status resourcePackStatus = null;
    private String name;
    private int xp = 0;
    private int level = 1;
    private int money = 0;
    private int rank = 100;

    public User(Player player) {
        super("User", player.getUniqueId().toString());
        this.player = player;
        this.name = player.getName();
        this.userConfig = new UserConfig(player);
        this.xp = loadValue("xp", xp);
        this.level = loadValue("level", level);
        this.money = loadValue("money", money);
        this.rank = loadValue("rank", rank);
        this.lobbySidebar = new BPlayerBoard(player, "lobbySidebar");
        userList.put(player.getUniqueId(), this);
        saveConfig();
    }

    private void saveConfig() {
        saveValue("name", name);
        saveValue("xp", xp);
        saveValue("level", level);
        saveValue("money", money);
        saveValue("rank", rank);
    }

    public Player getPlayer() {
        return player;
    }

    public UserConfig getUserConfig() {
        return userConfig;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        saveValue("name", this.name);
    }

    public int getXp() {
        return xp;
    }

    public void setXp(int xp) {
        if (xp < 0) xp = 0;
        this.xp = xp;
        saveValue("xp", this.xp);
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        if (level < 0) level = 0;
        this.level = level;
        saveValue("level", this.level);
    }

    public int getMoney() {
        return money;
    }

    public void setMoney(int money) {
        if (money < 0) money = 0;
        this.money = money;
        saveValue("money", this.money);
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
        saveValue("rank", this.rank);
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
        combatUserList.remove(player.getUniqueId());
    }

    public String getLevelPrefix() {
        String color = "";

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
