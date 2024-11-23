package com.dace.dmgr.user;

import com.dace.dmgr.ConsoleLogger;
import com.dace.dmgr.DMGR;
import com.dace.dmgr.GeneralConfig;
import com.dace.dmgr.YamlFile;
import com.dace.dmgr.combat.Core;
import com.dace.dmgr.combat.character.CharacterType;
import com.dace.dmgr.game.RankUtil;
import com.dace.dmgr.game.Tier;
import com.dace.dmgr.item.gui.ChatSoundOption;
import com.dace.dmgr.util.task.AsyncTask;
import com.dace.dmgr.util.task.Initializable;
import lombok.Getter;
import lombok.NonNull;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.nio.file.Paths;
import java.text.MessageFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 유저의 데이터 정보를 관리하는 클래스.
 */
public final class UserData implements Initializable<Void> {
    /** 차단 상태에서 접속 시도 시 표시되는 메시지 */
    private static final String MESSAGE_BANNED = "§c관리자에 의해 서버에서 차단되었습니다." +
            "\n" +
            "\n§f해제 일시 : §e{0}" +
            "\n" +
            "\n§7문의 : {1}";

    /** 플레이어 UUID */
    @NonNull
    @Getter
    private final UUID playerUUID;
    /** 플레이어 이름 */
    @NonNull
    @Getter
    private final String playerName;

    /** Yaml 파일 관리 인스턴스 */
    private final YamlFile yamlFile;
    /** 경험치 */
    private final YamlFile.Section.Entry<Integer> xpEntry;
    /** 레벨 */
    private final YamlFile.Section.Entry<Integer> levelEntry;
    /** 돈 */
    private final YamlFile.Section.Entry<Integer> moneyEntry;
    /** 차단한 플레이어의 UUID 목록 */
    private final YamlFile.Section.ListEntry<String> blockedPlayersEntry;
    /** 경고 횟수 */
    private final YamlFile.Section.Entry<Integer> warningEntry;
    /** 랭크 점수 (RR) */
    private final YamlFile.Section.Entry<Integer> rankRateEntry;
    /** 랭크게임 배치 완료 여부 */
    private final YamlFile.Section.Entry<Boolean> isRankedEntry;
    /** 매치메이킹 점수 (MMR) */
    private final YamlFile.Section.Entry<Integer> matchMakingRateEntry;
    /** 일반게임 플레이 횟수 */
    private final YamlFile.Section.Entry<Integer> normalPlayCountEntry;
    /** 랭크게임 플레이 횟수 */
    private final YamlFile.Section.Entry<Integer> rankPlayCountEntry;
    /** 승리 횟수 */
    private final YamlFile.Section.Entry<Integer> winCountEntry;
    /** 패배 횟수 */
    private final YamlFile.Section.Entry<Integer> loseCountEntry;
    /** 탈주 횟수 */
    private final YamlFile.Section.Entry<Integer> quitCountEntry;

    /** 유저 개인 설정 */
    @NonNull
    @Getter
    private final Config config;
    /** 전투원별 전투원 기록 목록 (전투원 : 전투원 기록) */
    private final EnumMap<CharacterType, CharacterRecord> characterRecordMap;

    /**
     * 유저 데이터 정보 인스턴스를 생성한다.
     *
     * @param playerUUID 대상 플레이어 UUID
     * @param playerName 대상 플레이어 이름
     */
    private UserData(@NonNull UUID playerUUID, @NonNull String playerName) {
        this.yamlFile = new YamlFile(Paths.get("User", playerUUID + ".yml"));
        this.playerUUID = playerUUID;
        this.playerName = playerName;

        YamlFile.Section section = yamlFile.getDefaultSection();
        this.xpEntry = section.getEntry("xp", 0);
        this.levelEntry = section.getEntry("level", 1);
        this.moneyEntry = section.getEntry("money", 0);
        this.blockedPlayersEntry = section.getListEntry("blockedPlayers");
        this.warningEntry = section.getEntry("warning", 0);
        this.rankRateEntry = section.getEntry("rankRate", 100);
        this.isRankedEntry = section.getEntry("isRanked", false);
        this.matchMakingRateEntry = section.getEntry("matchMakingRate", 100);
        this.normalPlayCountEntry = section.getEntry("normalPlayCount", 0);
        this.rankPlayCountEntry = section.getEntry("rankPlayCount", 0);
        this.winCountEntry = section.getEntry("winCount", 0);
        this.loseCountEntry = section.getEntry("loseCount", 0);
        this.quitCountEntry = section.getEntry("quitCount", 0);

        this.config = new Config();
        this.characterRecordMap = new EnumMap<>(CharacterType.class);
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
    @NonNull
    public AsyncTask<Void> init() {
        return yamlFile.init()
                .onFinish(() -> {
                    yamlFile.getDefaultSection().getEntry("playerName", "").set(playerName);

                    ConsoleLogger.info("{0}의 유저 데이터 불러오기 완료", playerName);
                })
                .onError(ex -> ConsoleLogger.severe("{0}의 유저 데이터 불러오기 실패", ex, playerName));
    }

    @Override
    public boolean isInitialized() {
        return yamlFile.isInitialized();
    }

    /**
     * 유저의 데이터 정보를 저장한다.
     */
    public void save() {
        if (DMGR.getPlugin().isEnabled())
            yamlFile.save();
        else
            yamlFile.saveSync();
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
     * @return 경험치
     */
    public int getXp() {
        return xpEntry.get();
    }

    /**
     * 플레이어의 경험치를 설정하고 필요 경험치를 충족했을 경우 레벨을 증가시킨다.
     *
     * <p>레벨이 증가했을 경우 {@link User#playLevelUpEffect()}를 호출한다.</p>
     *
     * @param xp 경험치. 0 이상의 값
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    public void setXp(int xp) {
        Validate.inclusiveBetween(0, Integer.MAX_VALUE, xp);
        validate();

        boolean levelup = false;

        while (xp >= getNextLevelXp()) {
            xp -= getNextLevelXp();
            levelEntry.set(levelEntry.get() + 1);

            levelup = true;
        }

        xpEntry.set(xp);

        if (levelup) {
            Player player = Bukkit.getPlayer(playerUUID);
            if (player == null)
                return;

            User user = User.fromPlayer(player);
            user.playLevelUpEffect();
        }
    }

    /**
     * @return 레벨
     */
    public int getLevel() {
        return levelEntry.get();
    }

    /**
     * 현재 레벨에 따른 칭호를 반환한다.
     *
     * @return 레벨 칭호
     */
    @NonNull
    public String getLevelPrefix() {
        int level = levelEntry.get();

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
     * @return 돈
     */
    public int getMoney() {
        return moneyEntry.get();
    }

    /**
     * @param money 돈. 0 이상의 값
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    public void setMoney(int money) {
        Validate.inclusiveBetween(0, Integer.MAX_VALUE, money);
        moneyEntry.set(money);
    }

    /**
     * @return 경고 횟수
     */
    public int getWarning() {
        return warningEntry.get();
    }

    /**
     * @param warning 경고 횟수. 0 이상의 값
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    public void setWarning(int warning) {
        Validate.inclusiveBetween(0, Integer.MAX_VALUE, warning);
        warningEntry.set(warning);
    }

    /**
     * @return 랭크 점수 (RR)
     */
    public int getRankRate() {
        return rankRateEntry.get();
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
        rankRateEntry.set(rankRate);

        Player player = Bukkit.getPlayer(playerUUID);
        if (player == null)
            return;

        User user = User.fromPlayer(player);
        if (getTier().getMinScore() > tier.getMinScore())
            user.playTierUpEffect();
        else if (getTier().getMinScore() < tier.getMinScore())
            user.playTierDownEffect();
    }

    /**
     * @return 랭크게임 배치 완료 여부
     */
    public boolean isRanked() {
        return isRankedEntry.get();
    }

    /**
     * @param isRanked 랭크게임 배치 완료 여부
     */
    public void setRanked(boolean isRanked) {
        isRankedEntry.set(isRanked);
    }

    /**
     * @return 매치메이킹 점수 (MMR)
     */
    public int getMatchMakingRate() {
        return matchMakingRateEntry.get();
    }

    /**
     * @param matchMakingRate 매치메이킹 점수 (MMR)
     */
    public void setMatchMakingRate(int matchMakingRate) {
        matchMakingRateEntry.set(matchMakingRate);
    }

    /**
     * @return 일반게임 플레이 횟수
     */
    public int getNormalPlayCount() {
        return normalPlayCountEntry.get();
    }

    /**
     * 일반게임 플레이 횟수를 1 증가시킨다.
     */
    public void addNormalPlayCount() {
        normalPlayCountEntry.set(normalPlayCountEntry.get() + 1);
    }

    /**
     * @return 랭크게임 플레이 횟수
     */
    public int getRankPlayCount() {
        return rankPlayCountEntry.get();
    }

    /**
     * 랭크게임 플레이 횟수를 1 증가시킨다.
     */
    public void addRankPlayCount() {
        rankPlayCountEntry.set(rankPlayCountEntry.get() + 1);
    }

    /**
     * @return 승리 횟수
     */
    public int getWinCount() {
        return winCountEntry.get();
    }

    /**
     * 승리 횟수를 1 증가시킨다.
     */
    public void addWinCount() {
        winCountEntry.set(winCountEntry.get() + 1);
    }

    /**
     * @return 패배 횟수
     */
    public int getLoseCount() {
        return loseCountEntry.get();
    }

    /**
     * 패배 횟수를 1 증가시킨다.
     */
    public void addLoseCount() {
        loseCountEntry.set(loseCountEntry.get() + 1);
    }

    /**
     * @return 탈주 횟수
     */
    public int getQuitCount() {
        return quitCountEntry.get();
    }

    /**
     * 탈주 횟수를 1 증가시킨다.
     */
    public void addQuitCount() {
        quitCountEntry.set(quitCountEntry.get() + 1);
    }

    /**
     * 차단한 플레이어 목록을 반환한다.
     *
     * @return 차단한 플레이어 목록의 유저 데이터 정보
     */
    @NonNull
    public UserData @NonNull [] getBlockedPlayers() {
        return blockedPlayersEntry.get().stream()
                .map(uuid -> UserData.fromUUID(UUID.fromString(uuid)))
                .toArray(UserData[]::new);
    }

    /**
     * 지정한 플레이어가 차단되었는지 확인한다.
     *
     * @param userData 대상 플레이어의 유저 데이터 정보
     * @return 차단 여부
     */
    public boolean isBlockedPlayer(@NonNull UserData userData) {
        return blockedPlayersEntry.get().contains(userData.getPlayerUUID().toString());
    }

    /**
     * 지정한 플레이어를 차단 목록에 추가한다.
     *
     * @param userData 대상 플레이어의 유저 데이터 정보
     */
    public void addBlockedPlayer(@NonNull UserData userData) {
        if (!isBlockedPlayer(userData))
            blockedPlayersEntry.add(userData.getPlayerUUID().toString());
    }

    /**
     * 지정한 플레이어를 차단 목록에서 제거한다.
     *
     * @param userData 대상 플레이어의 유저 데이터 정보
     */
    public void removeBlockedPlayer(@NonNull UserData userData) {
        blockedPlayersEntry.remove(userData.getPlayerUUID().toString());
    }

    /**
     * 차단 목록을 초기화한다.
     */
    public void clearBlockedPlayers() {
        blockedPlayersEntry.set(null);
    }

    /**
     * 플레이어를 서버에서 차단한다.
     *
     * <p>이미 차단된 상태이면 차단 기간을 연장한다.</p>
     *
     * @param days   기간 (일). 0 이상의 값
     * @param reason 차단 사유
     * @return 차단 해제 날짜
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    @NonNull
    public Date ban(int days, @Nullable String reason) {
        Validate.inclusiveBetween(0, Integer.MAX_VALUE, days);

        if (reason == null)
            reason = "없음";

        BanList banList = Bukkit.getBanList(BanList.Type.NAME);
        Date startDate = Date.from(Instant.now());
        if (banList.isBanned(playerName))
            startDate = banList.getBanEntry(playerName).getExpiration();

        Date endDate = Date.from(startDate.toInstant().plus(days, ChronoUnit.DAYS));
        String finalReason = GeneralConfig.getConfig().getMessagePrefix() + MessageFormat.format(MESSAGE_BANNED,
                DateFormatUtils.format(endDate, "YYYY-MM-dd HH:mm:ss"), GeneralConfig.getConfig().getAdminContact());
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
     * 전체 게임 플레이 시간을 반환한다.
     *
     * @return 게임 플레이 시간 (초)
     */
    public int getPlayTime() {
        int totalPlayTime = 0;
        for (CharacterRecord characterRecord : characterRecordMap.values())
            totalPlayTime += characterRecord.playTimeEntry.get();

        return totalPlayTime;
    }

    /**
     * 현재 랭크 점수에 따른 티어를 반환한다.
     *
     * @return 티어
     */
    @NonNull
    public Tier getTier() {
        if (!isRankedEntry.get())
            return Tier.NONE;

        int rank = RankUtil.getRankIndex(RankUtil.Indicator.RANK_RATE, this);
        int rankRate = rankRateEntry.get();

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
        return 250 + (levelEntry.get() * 50);
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
    public final class Config {
        /** 채팅 효과음 */
        private final YamlFile.Section.Entry<String> chatSoundEntry;
        /** 한글 채팅 여부 */
        private final YamlFile.Section.Entry<Boolean> koreanChatEntry;
        /** 야간 투시 여부 */
        private final YamlFile.Section.Entry<Boolean> nightVisionEntry;

        private Config() {
            YamlFile.Section section = yamlFile.getDefaultSection();
            this.chatSoundEntry = section.getEntry("chatSound", ChatSoundOption.ChatSound.PLING.toString());
            this.koreanChatEntry = section.getEntry("koreanChat", false);
            this.nightVisionEntry = section.getEntry("nightVision", false);
        }

        /**
         * @return 채팅 효과음
         */
        @NonNull
        public ChatSoundOption.ChatSound getChatSound() {
            return ChatSoundOption.ChatSound.valueOf(chatSoundEntry.get());
        }

        /**
         * @param chatSound 채팅 효과음
         */
        public void setChatSound(@NonNull ChatSoundOption.ChatSound chatSound) {
            chatSoundEntry.set(chatSound.toString());
        }

        /**
         * @return 한글 채팅 여부
         */
        public boolean isKoreanChat() {
            return koreanChatEntry.get();
        }

        /**
         * @param isKoreanChat 한글 채팅 여부
         */
        public void setKoreanChat(boolean isKoreanChat) {
            koreanChatEntry.set(isKoreanChat);
        }

        /**
         * @return 야간 투시 여부
         */
        public boolean isNightVision() {
            return nightVisionEntry.get();
        }

        /**
         * @param isNightVision 야간 투시 여부
         */
        public void setNightVision(boolean isNightVision) {
            nightVisionEntry.set(isNightVision);
        }
    }

    /**
     * 전투원 기록 정보.
     */
    public final class CharacterRecord {
        /** 킬 */
        private final YamlFile.Section.Entry<Integer> killEntry;
        /** 데스 */
        private final YamlFile.Section.Entry<Integer> deathEntry;
        /** 플레이 시간 (초) */
        private final YamlFile.Section.Entry<Integer> playTimeEntry;
        /** 적용된 코어의 이름 목록 */
        private final YamlFile.Section.ListEntry<String> coresEntry;

        private CharacterRecord(@NonNull CharacterType characterType) {
            YamlFile.Section section = yamlFile.getDefaultSection().getSection("record").getSection(characterType.toString());
            this.killEntry = section.getEntry("kill", 0);
            this.deathEntry = section.getEntry("death", 0);
            this.playTimeEntry = section.getEntry("playTime", 0);
            this.coresEntry = section.getListEntry("cores");
        }

        /**
         * @return 킬
         */
        public int getKill() {
            return killEntry.get();
        }

        /**
         * 킬 수를 1 증가시킨다.
         */
        public void addKill() {
            killEntry.set(killEntry.get() + 1);
        }

        /**
         * @return 데스
         */
        public int getDeath() {
            return deathEntry.get();
        }

        /**
         * 데스 수를 1 증가시킨다.
         */
        public void addDeath() {
            deathEntry.set(deathEntry.get() + 1);
        }

        /**
         * @return 플레이 시간 (초)
         */
        public int getPlayTime() {
            return playTimeEntry.get();
        }

        /**
         * 플레이 시간을 1초 증가시킨다.
         */
        public void addPlayTime() {
            playTimeEntry.set(playTimeEntry.get() + 1);
        }

        /**
         * 적용된 코어 목록을 반환한다.
         *
         * @return 적용된 코어 목록
         */
        @NonNull
        @Unmodifiable
        public Set<@NonNull Core> getCores() {
            return coresEntry.get().stream().map(Core::valueOf).collect(Collectors.toCollection(() -> EnumSet.noneOf(Core.class)));
        }

        /**
         * 지정한 코어를 적용된 코어 목록에 추가한다.
         *
         * @param core 추가할 코어
         */
        public void addCore(@NonNull Core core) {
            if (!coresEntry.get().contains(core.toString()))
                coresEntry.add(core.toString());
        }

        /**
         * 지정한 코어를 적용된 코어 목록에서 제거한다.
         *
         * @param core 제거할 코어
         */
        public void removeCore(@NonNull Core core) {
            coresEntry.remove(core.toString());
        }
    }
}
