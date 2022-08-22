package com.dace.dmgr.user;

import com.dace.dmgr.system.SkinManager;
import com.dace.dmgr.util.HasCooldown;
import com.dace.dmgr.util.YamlModel;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;

import static com.dace.dmgr.system.EntityList.combatUserList;
import static com.dace.dmgr.system.EntityList.userList;

public class User extends YamlModel implements HasCooldown {
    private final Player player;
    private final UserConfig userConfig;
    public boolean resourcePack = false;
    public PlayerResourcePackStatusEvent.Status resourcePackStatus = null;
    private String name;
    private int xp = 0;
    private int level = 0;
    private int money = 0;
    private int rank = 0;

    public User(Player player) {
        super("User", player.getUniqueId().toString());
        this.player = player;
        this.name = player.getName();
        this.userConfig = new UserConfig(player);
        this.xp = loadValue("xp", xp);
        this.level = loadValue("level", level);
        this.money = loadValue("money", money);
        this.rank = loadValue("rank", rank);
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
        player.getActivePotionEffects().forEach((potionEffect ->
                player.removePotionEffect(potionEffect.getType())));
        combatUserList.remove(player.getUniqueId());
    }

    @Override
    public String getCooldownKey() {
        return player.getUniqueId().toString();
    }
}
