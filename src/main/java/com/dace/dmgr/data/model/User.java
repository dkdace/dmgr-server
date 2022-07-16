package com.dace.dmgr.data.model;

import com.dace.dmgr.data.YamlModel;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;

public class User extends YamlModel {
    public boolean resourcePack = false;
    public PlayerResourcePackStatusEvent.Status resourcePackStatus = null;
    public Player player;
    public UserConfig userConfig;
    private String name;
    private int xp;
    private int level;
    private int money;
    private int rank;

    public User(Player player) {
        super("User", player.getUniqueId().toString());
        setName(player.getName());
        this.player = player;
        this.xp = getConfigInt("xp");
        this.level = getConfigInt("level");
        this.money = getConfigInt("money");
        this.rank = getConfigInt("rank");
        this.userConfig = new UserConfig(player);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        setConfig("name", this.name);
    }

    public int getXp() {
        return xp;
    }

    public void setXp(int xp) {
        if (xp < 0) xp = 0;
        this.xp = xp;
        setConfig("xp", this.xp);
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        if (level < 0) level = 0;
        this.level = level;
        setConfig("level", this.level);
    }

    public int getMoney() {
        return money;
    }

    public void setMoney(int money) {
        if (money < 0) money = 0;
        this.money = money;
        setConfig("money", this.money);
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
        setConfig("rank", this.rank);
    }

    public enum Cooldown {
        CHAT
    }
}
