package com.dace.dmgr.lobby;

import com.dace.dmgr.combat.CombatTick;
import com.dace.dmgr.system.HashMapList;
import com.dace.dmgr.system.SkinManager;
import com.dace.dmgr.util.YamlFile;
import fr.minuskube.netherboard.bukkit.BPlayerBoard;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;

import static com.dace.dmgr.system.HashMapList.combatUserMap;
import static com.dace.dmgr.system.HashMapList.userMap;

/**
 * 유저 정보를 관리하는 클래스.
 */
public class User {
    /** 로비 사이드바 */
    private final BPlayerBoard lobbySidebar;
    /** 플레이어 객체 */
    private final Player player;
    /** 유저 설정 정보 관리를 위한 객체 */
    private final UserConfig userConfig;
    /** 설정파일 관리를 위한 객체 */
    private final YamlFile yamlFile;
    /** 경험치 */
    private int xp = 0;
    /** 레벨 */
    private int level = 1;
    /** 돈 */
    private int money = 0;
    /** 랭크 점수 */
    private int rank = 100;
    /** 랭크게임 플레이 판 수 */
    private int rankPlay = 0;
    /** 랭크게임 플레이 여부 */
    private boolean isRanked = false;
    /** 매치메이킹 점수 */
    private int mmr = 100;
    /** 리소스팩 적용 여부 */
    private boolean resourcePack = false;
    /** 리소스팩 적용 상태 */
    private PlayerResourcePackStatusEvent.Status resourcePackStatus = null;

    /**
     * 유저 인스턴스를 생성하고 {@link HashMapList#userMap}에 추가한다.
     *
     * <p>플레이어가 서버에 접속할 때 호출해야 하며, 퇴장 시 {@link HashMapList#userMap}에서
     * 제거해야 한다.</p>
     *
     * @param player 대상 플레이어
     */
    public User(Player player) {
        this.player = player;
        this.userConfig = new UserConfig(player);
        this.lobbySidebar = new BPlayerBoard(player, "lobbySidebar");
        this.yamlFile = new YamlFile("User/" + player.getUniqueId().toString());
        this.xp = yamlFile.get("xp", xp);
        this.level = yamlFile.get("level", level);
        this.money = yamlFile.get("money", money);
        this.rank = yamlFile.get("rank", rank);
        this.rankPlay = yamlFile.get("rankPlay", rankPlay);
        this.isRanked = yamlFile.get("isRanked", isRanked);
        this.mmr = yamlFile.get("mmr", mmr);
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
        yamlFile.set("xp", this.xp);
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        if (level < 0) level = 0;
        this.level = level;
        yamlFile.set("level", this.level);
    }

    public int getMoney() {
        return money;
    }

    public void setMoney(int money) {
        if (money < 0) money = 0;
        this.money = money;
        yamlFile.set("money", this.money);
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
        yamlFile.set("rank", this.rank);
    }

    public int getRankPlay() {
        return rankPlay;
    }

    public void setRankPlay(int rankPlay) {
        this.rankPlay = rankPlay;
        yamlFile.set("rankPlay", this.rankPlay);
    }

    public int getMMR() {
        return mmr;
    }

    public void setMMR(int mmr) {
        this.mmr = mmr;
        yamlFile.set("mmr", this.mmr);
    }

    public boolean isRanked() {
        return isRanked;
    }

    public void setRanked(boolean ranked) {
        this.isRanked = ranked;
        yamlFile.set("isRanked", this.isRanked);
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

    /**
     * 플레이어의 체력, 이동속도 등의 모든 상태를 재설정한다.
     */
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
        CombatTick.playLowHealthScreenEffect(player, false);
    }

    /**
     * 레벨에 따른 칭호를 반환한다.
     *
     * @return 레벨 칭호
     */
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

    /**
     * 티어에 따른 칭호를 반환한다.
     *
     * @return 티어 칭호
     */
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

    /**
     * 레벨업에 필요한 경험치를 반환한다.
     *
     * @return 레벨업에 필요한 경험치
     */
    public int getNextLevelXp() {
        return 250 + (level * 50);
    }

    /**
     * 승급에 필요한 랭크 점수를 반환한다.
     *
     * @return 승급에 필요한 랭크 점수
     */
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

    /**
     * 현재 티어의 최소 랭크 점수를 반환한다.
     *
     * @return 현재 티어의 최소 랭크 점수
     */
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
