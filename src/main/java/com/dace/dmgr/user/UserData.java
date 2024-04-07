package com.dace.dmgr.user;

import com.dace.dmgr.ConsoleLogger;
import com.dace.dmgr.YamlFile;
import com.dace.dmgr.combat.character.CharacterType;
import com.dace.dmgr.game.RankUtil;
import com.dace.dmgr.game.Tier;
import com.dace.dmgr.item.gui.ChatSoundOption;
import lombok.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.text.MessageFormat;
import java.util.EnumMap;
import java.util.UUID;

/**
 * 유저의 데이터 정보를 관리하는 클래스.
 */
public final class UserData extends YamlFile {
    /** 플레이어 UUID */
    @Getter
    @NonNull
    private final UUID playerUUID;
    /** 플레이어 이름 */
    @Getter
    @NonNull
    private final String playerName;
    /** 유저 개인 설정 */
    @Getter
    @NonNull
    private final Config config = new Config();
    /** 전투원별 전투원 기록 목록 (전투원 : 전투원 기록) */
    private final EnumMap<CharacterType, CharacterRecord> characterRecordMap = new EnumMap<>(CharacterType.class);
    /** 경험치 */
    @Getter
    private int xp = 0;
    /** 레벨 */
    @Getter
    private int level = 1;
    /** 돈 */
    @Getter
    private int money = 0;
    /** 랭크 점수 (RR) */
    @Getter
    private int rankRate = 100;
    /** 랭크게임 배치 완료 여부 */
    @Getter
    private boolean isRanked = false;
    /** 매치메이킹 점수 (MMR) */
    @Getter
    private int matchMakingRate = 100;
    /** 일반게임 플레이 횟수 */
    @Getter
    private int normalPlayCount = 0;
    /** 랭크게임 플레이 판 수 */
    @Getter
    private int rankPlayCount = 0;
    /** 승리 횟수 */
    @Getter
    private int winCount = 0;
    /** 패배 횟수 */
    @Getter
    private int loseCount = 0;
    /** 탈주 횟수 */
    @Getter
    private int quitCount = 0;

    /**
     * 유저 데이터 정보 인스턴스를 생성한다.
     *
     * @param playerUUID 대상 플레이어 UUID
     */
    private UserData(@NonNull UUID playerUUID) {
        super("User/" + playerUUID);
        this.playerUUID = playerUUID;
        this.playerName = Bukkit.getOfflinePlayer(playerUUID).getName();
        for (CharacterType characterType : CharacterType.values())
            characterRecordMap.put(characterType, new CharacterRecord(characterType));

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
        UserData.this.winCount = (int) getLong("winCount", winCount);
        UserData.this.loseCount = (int) getLong("loseCount", loseCount);
        UserData.this.quitCount = (int) getLong("quitCount", quitCount);

        config.koreanChat = getBoolean("koreanChat", config.koreanChat);
        config.nightVision = getBoolean("nightVision", config.nightVision);
        config.chatSound = getString("chatSound", config.chatSound);

        characterRecordMap.forEach((characterType, characterRecord) -> characterRecord.load());

        ConsoleLogger.info("{0}의 유저 데이터 불러오기 완료", playerName);

        Player player = Bukkit.getPlayer(playerUUID);
        if (player != null)
            User.fromPlayer(player).onDataInit();
    }

    @Override
    protected void onInitError(Exception ex) {
        ConsoleLogger.severe("{0}의 유저 데이터 불러오기 실패", ex, playerName);
    }

    /**
     * 지정한 전투원의 기록 정보를 반환한다.
     *
     * @param characterType 전투원 종류
     * @return 전투원 기록 정보
     */
    @NonNull
    public CharacterRecord getCharacterRecord(@NonNull CharacterType characterType) {
        return characterRecordMap.get(characterType);
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

    public void setWinCount(int winCount) {
        this.winCount = winCount;
        set("winCount", this.winCount);
    }

    public void setLoseCount(int loseCount) {
        this.loseCount = loseCount;
        set("loseCount", this.loseCount);
    }

    public void setQuitCount(int quitCount) {
        this.quitCount = quitCount;
        set("quitCount", this.quitCount);
    }

    /**
     * 전체 게임 플레이 시간을 반환한다.
     *
     * @return 게임 플레이 시간
     */
    public int getPlayTime() {
        int totalPlayTime = 0;
        for (CharacterRecord characterRecord : characterRecordMap.values())
            totalPlayTime += characterRecord.playTime;

        return totalPlayTime;
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

    /**
     * 전투원 기록 정보.
     */
    @RequiredArgsConstructor
    public final class CharacterRecord {
        /** 접두사 */
        private static final String PREFIX = "record_";
        /** 전투원 종류 */
        private final CharacterType characterType;
        /** 킬 */
        @Getter
        private int kill = 0;
        /** 데스 */
        @Getter
        private int death = 0;
        /** 플레이 시간 (초) */
        @Getter
        private int playTime = 0;

        /**
         * 데이터를 불러온다.
         */
        private void load() {
            kill = (int) getLong(PREFIX + characterType + "_kill", this.kill);
            death = (int) getLong(PREFIX + characterType + "_death", this.death);
            playTime = (int) getLong(PREFIX + characterType + "_playTime", this.playTime);
        }

        public void setKill(int kill) {
            this.kill = kill;
            set(PREFIX + characterType + "_kill", this.kill);
        }

        public void setDeath(int death) {
            this.death = death;
            set(PREFIX + characterType + "_death", this.death);
        }

        public void setPlayTime(int playTime) {
            this.playTime = playTime;
            set(PREFIX + characterType + "_playTime", this.playTime);
        }
    }
}
