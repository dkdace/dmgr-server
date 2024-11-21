package com.dace.dmgr.user;

import com.dace.dmgr.ConsoleLogger;
import com.dace.dmgr.GeneralConfig;
import com.dace.dmgr.YamlFile;
import com.dace.dmgr.combat.Core;
import com.dace.dmgr.combat.character.CharacterType;
import com.dace.dmgr.game.RankUtil;
import com.dace.dmgr.game.Tier;
import com.dace.dmgr.item.gui.ChatSoundOption;
import lombok.*;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.text.MessageFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 유저의 데이터 정보를 관리하는 클래스.
 */
public final class UserData extends YamlFile {
    /** 차단 상태에서 접속 시도 시 표시되는 메시지 */
    private static final String MESSAGE_BANNED = "§c관리자에 의해 서버에서 차단되었습니다." +
            "\n" +
            "\n§f해제 일시 : §e{0}" +
            "\n" +
            "\n§7문의 : " + GeneralConfig.getConfig().getAdminContact();

    /** 플레이어 UUID */
    @NonNull
    @Getter
    private final UUID playerUUID;
    /** 플레이어 이름 */
    @NonNull
    @Getter
    private final String playerName;
    /** 유저 개인 설정 */
    @NonNull
    @Getter
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
    /** 차단한 플레이어의 UUID 목록 */
    @Nullable
    private List<String> blockedPlayers;
    /** 경고 횟수 */
    @Getter
    private int warning = 0;

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
     * @param playerName 대상 플레이어 이름
     */
    private UserData(@NonNull UUID playerUUID, @NonNull String playerName) {
        super("User/" + playerUUID);
        this.playerUUID = playerUUID;
        this.playerName = playerName;
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
    public static UserData fromPlayer(@NonNull OfflinePlayer player) {
        UserData userData = UserDataRegistry.getInstance().get(player.getUniqueId());
        if (userData == null)
            userData = new UserData(player.getUniqueId(), player.getName());

        return userData;
    }

    /**
     * 지정한 UUID에 해당하는 플레이어의 유저 데이터 정보 인스턴스를 반환한다.
     *
     * @param playerUUID 대상 플레이어 UUID
     * @return 유저 데이터 인스턴스
     */
    @NonNull
    public static UserData fromUUID(@NonNull UUID playerUUID) {
        return fromPlayer(Bukkit.getOfflinePlayer(playerUUID));
    }

    /**
     * 모든 유저의 데이터 정보를 반환한다.
     *
     * @return 모든 유저 데이터 정보 객체
     */
    @NonNull
    @Unmodifiable
    public static Collection<@NonNull UserData> getAllUserDatas() {
        return UserDataRegistry.getInstance().getAllUserDatas();
    }

    @Override
    protected void onInitFinish() {
        set("playerName", playerName);
        this.xp = (int) getLong("xp", xp);
        this.level = (int) getLong("level", level);
        this.money = (int) getLong("money", money);
        this.warning = (int) getLong("warning", warning);
        this.rankRate = (int) getLong("rankRate", rankRate);
        this.isRanked = getBoolean("isRanked", isRanked);
        this.matchMakingRate = (int) getLong("matchMakingRate", matchMakingRate);
        this.normalPlayCount = (int) getLong("normalPlayCount", normalPlayCount);
        this.rankPlayCount = (int) getLong("rankPlayCount", rankPlayCount);
        this.winCount = (int) getLong("winCount", winCount);
        this.loseCount = (int) getLong("loseCount", loseCount);
        this.quitCount = (int) getLong("quitCount", quitCount);
        this.blockedPlayers = getStringList("blockedPlayers");

        config.koreanChat = getBoolean("koreanChat", config.koreanChat);
        config.nightVision = getBoolean("nightVision", config.nightVision);
        config.chatSound = getString("chatSound", config.chatSound);

        characterRecordMap.forEach((characterType, characterRecord) -> characterRecord.load());

        ConsoleLogger.info("{0}의 유저 데이터 불러오기 완료", playerName);
    }

    @Override
    protected void onInitError(@NonNull Exception ex) {
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

    /**
     * 플레이어의 경험치를 설정하고 필요 경험치를 충족했을 경우 레벨을 증가시킨다.
     *
     * <p>레벨이 증가했을 경우 {@link User#playLevelUpEffect()}를 호출한다.</p>
     *
     * @param xp 경험치
     */
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

    public void setWarning(int warning) {
        this.warning = Math.max(0, warning);
        set("warning", this.warning);
    }

    /**
     * 플레이어를 서버에서 차단한다.
     *
     * <p>이미 차단된 상태이면 차단 기간을 연장한다.</p>
     *
     * @param days   기간 (일). 0 이상의 값
     * @param reason 차단 사유
     * @return 차단 해제 날짜
     */
    @NonNull
    public Date ban(int days, @Nullable String reason) {
        if (reason == null)
            reason = "없음";

        BanList banList = Bukkit.getBanList(BanList.Type.NAME);
        Date startDate = Date.from(Instant.now());
        if (banList.isBanned(playerName))
            startDate = banList.getBanEntry(playerName).getExpiration();

        Date endDate = Date.from(startDate.toInstant().plus(days, ChronoUnit.DAYS));
        String finalReason = GeneralConfig.getConfig().getMessagePrefix() + MessageFormat.format(MESSAGE_BANNED,
                DateFormatUtils.format(endDate, "YYYY-MM-dd HH:mm:ss"));
        banList.addBan(playerName, reason + "\n\n" + finalReason, endDate, null);

        Player player = Bukkit.getPlayer(playerUUID);
        if (player != null)
            player.kickPlayer(finalReason);

        return endDate;
    }

    /**
     * 플레이어가 서버에서 차단된 상태인지 확인한다.
     *
     * @return 차단 여부
     */
    public boolean isBanned() {
        BanList banList = Bukkit.getBanList(BanList.Type.NAME);
        return banList.isBanned(playerName);
    }

    /**
     * 플레이어의 차단을 해제한다.
     */
    public void unban() {
        BanList banList = Bukkit.getBanList(BanList.Type.NAME);
        if (!banList.isBanned(playerName))
            return;

        banList.pardon(playerName);
    }

    /**
     * 플레이어의 랭크 점수를 설정한다.
     *
     * <p>티어가 바뀌었을 경우 {@link User#playTierUpEffect()} 또는
     * {@link User#playTierDownEffect()}를 호출한다.</p>
     *
     * @param rankRate 랭크 점수 (RR)
     */
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
        this.winCount = Math.max(0, winCount);
        set("winCount", this.winCount);
    }

    public void setLoseCount(int loseCount) {
        this.loseCount = Math.max(0, loseCount);
        set("loseCount", this.loseCount);
    }

    public void setQuitCount(int quitCount) {
        this.quitCount = Math.max(0, quitCount);
        set("quitCount", this.quitCount);
    }

    /**
     * 차단한 플레이어 목록을 반환한다.
     *
     * @return 차단한 플레이어 목록의 유저 데이터 정보
     */
    @NonNull
    public UserData @NonNull [] getBlockedPlayers() {
        Validate.notNull(blockedPlayers);
        return this.blockedPlayers.stream().map(uuid -> UserData.fromUUID(UUID.fromString(uuid))).toArray(UserData[]::new);
    }

    /**
     * 지정한 플레이어가 차단되었는지 확인한다.
     *
     * @param userData 대상 플레이어의 유저 데이터 정보
     * @return 차단 여부
     */
    public boolean isBlockedPlayer(@NonNull UserData userData) {
        Validate.notNull(blockedPlayers);
        return this.blockedPlayers.contains(userData.getPlayerUUID().toString());
    }

    /**
     * 지정한 플레이어를 차단 목록에 추가한다.
     *
     * @param userData 대상 플레이어의 유저 데이터 정보
     */
    public void addBlockedPlayer(@NonNull UserData userData) {
        Validate.notNull(blockedPlayers);

        this.blockedPlayers.add(userData.getPlayerUUID().toString());
        set("blockedPlayers", this.blockedPlayers);
    }

    /**
     * 지정한 플레이어를 차단 목록에서 제거한다.
     *
     * @param userData 대상 플레이어의 유저 데이터 정보
     */
    public void removeBlockedPlayer(@NonNull UserData userData) {
        Validate.notNull(blockedPlayers);

        this.blockedPlayers.remove(userData.getPlayerUUID().toString());
        set("blockedPlayers", this.blockedPlayers);
    }

    /**
     * 차단 목록을 초기화한다.
     */
    public void clearBlockedPlayers() {
        Validate.notNull(blockedPlayers);

        this.blockedPlayers.clear();
        set("blockedPlayers", this.blockedPlayers);
    }

    /**
     * 전체 게임 플레이 시간을 반환한다.
     *
     * @return 게임 플레이 시간 (초)
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

        int rank = RankUtil.getRankIndex(RankUtil.Indicator.RANK_RATE, this);

        if (rankRate <= Tier.STONE.getMaxScore())
            return Tier.STONE;
        else if (rankRate >= Tier.DIAMOND.getMinScore() && rank > 0 && rank <= GeneralConfig.getConfig().getNetheriteTierMinRank())
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
        /** 섹션 이름 */
        private static final String SECTION = "record";
        /** 전투원 종류 */
        @NonNull
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
        /** 적용된 코어의 이름 목록 */
        @Nullable
        private List<String> cores;

        /**
         * 데이터를 불러온다.
         */
        private void load() {
            kill = (int) getLong(SECTION + "." + characterType + ".kill", this.kill);
            death = (int) getLong(SECTION + "." + characterType + ".death", this.death);
            playTime = (int) getLong(SECTION + "." + characterType + ".playTime", this.playTime);
            cores = getStringList(SECTION + "." + characterType + ".cores");
        }

        /**
         * 적용된 코어 목록을 반환한다.
         *
         * @return 적용된 코어 목록
         */
        @NonNull
        public Set<@NonNull Core> getCores() {
            Validate.notNull(cores);
            return this.cores.stream().map(Core::valueOf).collect(Collectors.toCollection(() -> EnumSet.noneOf(Core.class)));
        }

        /**
         * 지정한 코어를 적용된 코어 목록에 추가한다.
         *
         * @param core 추가할 코어
         */
        public void addCore(@NonNull Core core) {
            Validate.notNull(cores);

            this.cores.add(core.toString());
            set(SECTION + "." + characterType + ".cores", this.cores);
        }

        /**
         * 지정한 코어를 적용된 코어 목록에서 제거한다.
         *
         * @param core 제거할 코어
         */
        public void removeCore(@NonNull Core core) {
            Validate.notNull(cores);

            this.cores.remove(core.toString());
            set(SECTION + "." + characterType + ".cores", this.cores);
        }

        public void setKill(int kill) {
            this.kill = kill;
            set(SECTION + "." + characterType + ".kill", this.kill);
        }

        public void setDeath(int death) {
            this.death = death;
            set(SECTION + "." + characterType + ".death", this.death);
        }

        public void setPlayTime(int playTime) {
            this.playTime = playTime;
            set(SECTION + "." + characterType + ".playTime", this.playTime);
        }
    }
}
