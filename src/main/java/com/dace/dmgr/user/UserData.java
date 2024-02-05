package com.dace.dmgr.user;

import com.dace.dmgr.ConsoleLogger;
import com.dace.dmgr.YamlFile;
import com.dace.dmgr.game.RankUtil;
import com.dace.dmgr.game.Tier;
import com.dace.dmgr.item.gui.ChatSoundOption;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.text.MessageFormat;
import java.util.UUID;

/**
 * 유저의 데이터 정보를 관리하는 클래스.
 */
@Getter
public final class UserData extends YamlFile {
    /** 플레이어 UUID */
    @NonNull
    private final UUID playerUUID;
    /** 플레이어 이름 */
    @NonNull
    private final String playerName;
    /** 유저 개인 설정 */
    @NonNull
    private final Config config = new Config();
    /** 경험치 */
    private int xp = 0;
    /** 레벨 */
    private int level = 1;
    /** 돈 */
    private int money = 0;
    /** 랭크 점수 (RR) */
    private int rankRate = 100;
    /** 랭크게임 배치 완료 여부 */
    private boolean isRanked = false;
    /** 매치메이킹 점수 (MMR) */
    private int matchMakingRate = 100;
    /** 일반게임 플레이 횟수 */
    private int normalPlayCount = 0;
    /** 랭크게임 플레이 판 수 */
    private int rankPlayCount = 0;

    /**
     * 유저 데이터 정보 인스턴스를 생성한다.
     *
     * @param playerUUID 대상 플레이어 UUID
     */
    private UserData(@NonNull UUID playerUUID) {
        super("User/" + playerUUID);
        this.playerUUID = playerUUID;
        this.playerName = Bukkit.getOfflinePlayer(playerUUID).getName();

        UserDataRegistry.getInstance().add(playerUUID, this);
    }

    /**
     * 지정한 플레이어에 해당하는 유저 데이터 정보 인스턴스를 반환한다.
     *
     * @param player 대상 플레이어
     * @return 유저 데이터 인스턴스
     */
    @NonNull
    public static UserData fromPlayer(@NonNull Player player) {
        return fromUUID(player.getUniqueId());
    }

    /**
     * 지정한 UUID에 해당하는 플레이어의 유저 데이터 정보 인스턴스를 반환한다.
     *
     * @param playerUUID 대상 플레이어 UUID
     * @return 유저 데이터 인스턴스
     */
    @NonNull
    public static UserData fromUUID(@NonNull UUID playerUUID) {
        UserData userData = UserDataRegistry.getInstance().get(playerUUID);
        if (userData == null)
            userData = new UserData(playerUUID);

        return userData;
    }

    /**
     * 모든 유저의 데이터 정보를 반환한다.
     *
     * @return 모든 유저 데이터 정보 객체
     */
    @NonNull
    public static UserData[] getAllUserDatas() {
        return UserDataRegistry.getInstance().getAllUserDatas();
    }

    @Override
    protected void onInitFinish() {
        set("playerName", playerName);
        UserData.this.xp = (int) getLong("xp", xp);
        UserData.this.level = (int) getLong("level", level);
        UserData.this.money = (int) getLong("money", money);
        UserData.this.rankRate = (int) getLong("rankRate", rankRate);
        UserData.this.isRanked = getBoolean("isRanked", isRanked);
        UserData.this.matchMakingRate = (int) getLong("matchMakingRate", matchMakingRate);
        UserData.this.normalPlayCount = (int) getLong("normalPlayCount", normalPlayCount);
        UserData.this.rankPlayCount = (int) getLong("rankPlayCount", rankPlayCount);

        config.koreanChat = getBoolean("koreanChat", config.koreanChat);
        config.nightVision = getBoolean("nightVision", config.nightVision);
        config.chatSound = getString("chatSound", config.chatSound);

        ConsoleLogger.info("{0}의 유저 데이터 불러오기 완료", playerName);

        Player player = Bukkit.getPlayer(playerUUID);
        if (player != null)
            User.fromPlayer(player).onDataInit();
    }

    @Override
    protected void onInitError(Exception ex) {
        ConsoleLogger.severe("{0}의 유저 데이터 불러오기 실패", ex, playerName);
    }

    public void setXp(int xp) {
        boolean levelup = false;

        while (xp >= getNextLevelXp()) {
            xp -= getNextLevelXp();
            setLevel(level + 1);

            levelup = true;
        }

        this.xp = Math.max(0, xp);
        set("xp", this.xp);

        if (levelup) {
            Player player = Bukkit.getPlayer(playerUUID);
            if (player == null)
                return;

            User user = User.fromPlayer(player);
            user.playLevelUpEffect();
        }
    }

    /**
     * 현재 레벨에 따른 칭호를 반환한다.
     *
     * @return 레벨 칭호
     */
    @NonNull
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

    public void setLevel(int level) {
        this.level = Math.max(0, level);
        set("level", this.level);
    }

    public void setMoney(int money) {
        this.money = Math.max(0, money);
        set("money", this.money);
    }

    public void setRankRate(int rankRate) {
        Tier tier = getTier();

        this.rankRate = rankRate;
        set("rankRate", this.rankRate);

        Player player = Bukkit.getPlayer(playerUUID);
        if (player == null)
            return;

        User user = User.fromPlayer(player);
        if (getTier().getMinScore() > tier.getMinScore())
            user.playTierUpEffect();
        else if (getTier().getMinScore() < tier.getMinScore())
            user.playTierDownEffect();
    }

    public void setRanked(boolean ranked) {
        this.isRanked = ranked;
        set("isRanked", this.isRanked);
    }

    public void setMatchMakingRate(int matchMakingRate) {
        this.matchMakingRate = matchMakingRate;
        set("matchMakingRate", this.matchMakingRate);
    }

    public void setNormalPlayCount(int normalPlayCount) {
        this.normalPlayCount = Math.max(0, normalPlayCount);
        set("normalPlayCount", this.normalPlayCount);
    }

    public void setRankPlayCount(int rankPlayCount) {
        this.rankPlayCount = Math.max(0, rankPlayCount);
        set("rankPlayCount", this.rankPlayCount);
    }

    /**
     * 현재 랭크 점수에 따른 티어를 반환한다.
     *
     * @return 티어
     */
    @NonNull
    public Tier getTier() {
        if (!isRanked)
            return Tier.NONE;

        int rank = RankUtil.getRankIndex(RankUtil.Sector.RANK_RATE, this);

        if (rankRate <= Tier.STONE.getMaxScore())
            return Tier.STONE;
        else if (rankRate >= Tier.DIAMOND.getMinScore() && rank > 0 && rank <= 5)
            return Tier.NETHERITE;

        for (Tier tier : Tier.values()) {
            if (rankRate >= tier.getMinScore() && rankRate <= tier.getMaxScore())
                return tier;
        }

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

    /**
     * 플레이어의 표시 이름을 반환한다.
     *
     * @return 표시 이름
     */
    @NonNull
    public String getDisplayName() {
        return MessageFormat.format("{0} {1} {2}{3}§f", getTier().getPrefix(), getLevelPrefix(),
                (Bukkit.getOfflinePlayer(playerUUID).isOp() ? "§a" : "§f"), playerName);
    }

    /**
     * 유저 개인 설정.
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public final class Config {
        /** 채팅 효과음 */
        @NonNull
        private String chatSound = ChatSoundOption.ChatSound.PLING.toString();
        /** 한글 채팅 여부 */
        @Getter
        private boolean koreanChat = false;
        /** 야간 투시 여부 */
        @Getter
        private boolean nightVision = false;

        @NonNull
        public ChatSoundOption.ChatSound getChatSound() {
            return ChatSoundOption.ChatSound.valueOf(chatSound);
        }

        public void setChatSound(@NonNull ChatSoundOption.ChatSound chatSound) {
            this.chatSound = chatSound.toString();
            set("chatSound", this.chatSound);
        }

        public void setKoreanChat(boolean koreanChat) {
            this.koreanChat = koreanChat;
            set("koreanChat", this.koreanChat);
        }

        public void setNightVision(boolean nightVision) {
            this.nightVision = nightVision;
            set("nightVision", this.nightVision);
        }
    }
}
