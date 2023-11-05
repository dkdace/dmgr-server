package com.dace.dmgr.lobby;

import com.dace.dmgr.game.RankUtil;
import com.dace.dmgr.game.Tier;
import com.dace.dmgr.system.YamlFile;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.bukkit.Bukkit;

import java.util.UUID;

/**
 * 유저의 데이터 정보를 관리하는 클래스.
 */
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class UserData {
    /** 유저 설정 정보 관리를 위한 객체 */
    @Getter
    protected final UserConfig userConfig;
    /** 플레이어 UUID */
    @EqualsAndHashCode.Include
    @Getter
    private final UUID playerUUID;
    /** 플레이어 이름 */
    @Getter
    private final String playerName;
    /** 설정파일 관리를 위한 객체 */
    private final YamlFile yamlFile;
    /** 경험치 */
    @Getter
    protected int xp = 0;
    /** 레벨 */
    @Getter
    protected int level = 1;
    /** 돈 */
    @Getter
    protected int money = 0;
    /** 랭크 점수 (RR) */
    @Getter
    protected int rankRate = 100;
    /** 랭크게임 배치 완료 여부 */
    @Getter
    protected boolean isRanked = false;
    /** 매치메이킹 점수 (MMR) */
    @Getter
    protected int matchMakingRate = 100;
    /** 일반게임 플레이 횟수 */
    @Getter
    protected int normalPlayCount = 0;
    /** 랭크게임 플레이 판 수 */
    @Getter
    protected int rankPlayCount = 0;

    /**
     * 유저 데이터 정보 인스턴스를 생성한다.
     *
     * @param playerUUID 대상 플레이어 UUID
     */
    public UserData(UUID playerUUID) {
        this.playerUUID = playerUUID;
        this.playerName = Bukkit.getOfflinePlayer(playerUUID).getName();
        this.userConfig = new UserConfig(playerUUID);
        this.yamlFile = new YamlFile("User/" + playerUUID);
        this.xp = yamlFile.get("xp", xp);
        this.level = yamlFile.get("level", level);
        this.money = yamlFile.get("money", money);
        this.rankRate = yamlFile.get("rankRate", rankRate);
        this.isRanked = yamlFile.get("isRanked", isRanked);
        this.matchMakingRate = yamlFile.get("matchMakingRate", matchMakingRate);
        this.normalPlayCount = yamlFile.get("normalPlayCount", normalPlayCount);
        this.rankPlayCount = yamlFile.get("rankPlayCount", rankPlayCount);
        yamlFile.set("playerName", playerName);
    }

    /**
     * 유저 데이터 정보 인스턴스를 생성한다.
     *
     * @param playerName 대상 플레이어 이름
     */
    public UserData(String playerName) {
        this(Bukkit.getOfflinePlayer(playerName).getUniqueId());
    }

    public void setXp(int xp) {
        this.xp = Math.max(0, xp);
        yamlFile.set("xp", this.xp);
    }

    public void setLevel(int level) {
        this.level = Math.max(0, level);
        yamlFile.set("level", this.level);
    }

    public void setMoney(int money) {
        this.money = Math.max(0, money);
        yamlFile.set("money", this.money);
    }

    public void setRankRate(int rankRate) {
        this.rankRate = rankRate;
        yamlFile.set("rankRate", this.rankRate);
    }

    public void setRanked(boolean ranked) {
        this.isRanked = ranked;
        yamlFile.set("isRanked", this.isRanked);
    }

    public void setMatchMakingRate(int matchMakingRate) {
        this.matchMakingRate = matchMakingRate;
        yamlFile.set("matchMakingRate", this.matchMakingRate);
    }

    public void setNormalPlayCount(int normalPlayCount) {
        this.normalPlayCount = Math.max(0, normalPlayCount);
        yamlFile.set("normalPlayCount", this.normalPlayCount);
    }

    public void setRankPlayCount(int rankPlayCount) {
        this.rankPlayCount = Math.max(0, rankPlayCount);
        yamlFile.set("rankPlayCount", this.rankPlayCount);
    }

    /**
     * 현재 랭크 점수에 따른 티어를 반환한다.
     *
     * @return 티어
     */
    public Tier getTier() {
        if (!isRanked)
            return Tier.NONE;

        for (Tier tier : Tier.values()) {
            if (rankRate >= tier.getMinScore() && rankRate <= tier.getMaxScore())
                return tier;
        }
        int rank = RankUtil.getRankRateRank(this);
        if (rankRate >= Tier.DIAMOND.getMinScore() && rank > 0 && rank <= 5)
            return Tier.NETHERITE;

        return Tier.NONE;
    }

    /**
     * 레벨업에 필요한 경험치를 반환한다.
     *
     * @return 레벨업에 필요한 경험치
     */
    public int getNextLevelXp() {
        return 250 + (level * 50);
    }
}
