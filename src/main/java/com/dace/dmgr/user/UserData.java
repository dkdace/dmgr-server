package com.dace.dmgr.user;

import com.dace.dmgr.*;
import com.dace.dmgr.combat.combatant.CombatantType;
import com.dace.dmgr.combat.entity.combatuser.Core;
import com.dace.dmgr.effect.SoundEffect;
import com.dace.dmgr.item.ItemBuilder;
import com.dace.dmgr.menu.ChatSoundOption;
import com.dace.dmgr.util.EntityUtil;
import com.dace.dmgr.util.task.AsyncTask;
import com.dace.dmgr.util.task.DelayTask;
import com.dace.dmgr.util.task.Initializable;
import com.dace.dmgr.yaml.Serializer;
import com.dace.dmgr.yaml.TypeToken;
import com.dace.dmgr.yaml.YamlFile;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * 유저의 데이터 정보를 관리하는 클래스.
 */
public final class UserData implements Initializable<Void> {
    /** 유저 데이터 목록 (UUID : 유저 데이터 정보) */
    private static final HashMap<UUID, UserData> USER_DATA_MAP = new HashMap<>();

    /** 차단 상태에서 접속 시도 시 표시되는 메시지 */
    private static final String MESSAGE_BANNED = String.join("\n",
            "§c관리자에 의해 서버에서 차단되었습니다.",
            "",
            "§f해제 일시 : §e{0}",
            "",
            "§7문의 : {1}");
    /** Yaml 파일 경로의 디렉터리 이름 */
    private static final String DIRECTORY_NAME = "User";
    /** 레벨 업 효과음 */
    private static final SoundEffect LEVEL_UP_SOUND = new SoundEffect(
            SoundEffect.SoundInfo.builder("random.good").volume(1000).pitch(1).build());
    /** 티어 승급 효과음 */
    private static final SoundEffect TIER_UP_SOUND = new SoundEffect(
            SoundEffect.SoundInfo.builder(Sound.UI_TOAST_CHALLENGE_COMPLETE).volume(1000).pitch(1.5).build());
    /** 티어 강등 효과음 */
    private static final SoundEffect TIER_DOWN_SOUND = new SoundEffect(
            SoundEffect.SoundInfo.builder(Sound.ENTITY_BLAZE_DEATH).volume(1000).pitch(0.5).build());

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
    private final YamlFile.Section.Entry<List<UserData>> blockedPlayersEntry;
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
    private final EnumMap<CombatantType, CombatantRecord> combatantRecordMap;

    /**
     * 유저 데이터 정보 인스턴스를 생성한다.
     *
     * @param playerUUID 대상 플레이어 UUID
     * @param playerName 대상 플레이어 이름
     */
    private UserData(@NonNull UUID playerUUID, @NonNull String playerName) {
        this.playerUUID = playerUUID;
        this.playerName = playerName;
        this.yamlFile = new YamlFile(Paths.get(DIRECTORY_NAME, playerUUID + ".yml"));

        YamlFile.Section section = yamlFile.getDefaultSection();
        this.xpEntry = section.getEntry("xp", 0);
        this.levelEntry = section.getEntry("level", 1);
        this.moneyEntry = section.getEntry("money", 0);
        this.blockedPlayersEntry = section.getListEntry("blockedPlayers", new TypeToken<List<UserData>>() {
        });
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
        this.combatantRecordMap = new EnumMap<>(CombatantType.class);
        for (CombatantType combatantType : CombatantType.values())
            combatantRecordMap.put(combatantType, new CombatantRecord(combatantType));

        USER_DATA_MAP.put(playerUUID, this);
    }

    /**
     * 지정한 플레이어에 해당하는 유저 데이터 정보 인스턴스를 반환한다.
     *
     * @param player 대상 플레이어
     * @return 유저 데이터 인스턴스
     * @throws IllegalStateException 해당 {@code player}가 Citizens NPC이면 발생
     */
    @NonNull
    public static UserData fromPlayer(@NonNull OfflinePlayer player) {
        if (player instanceof Player)
            Validate.validState(!EntityUtil.isCitizensNPC((Player) player), "Citizens NPC는 UserData 인스턴스를 생성할 수 없음");

        UserData userData = USER_DATA_MAP.get(player.getUniqueId());
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
     * 지정한 이름에 해당하는 플레이어의 유저 데이터 정보 인스턴스를 반환한다.
     *
     * @param playerName 플레이어 이름
     * @return 유저 데이터 인스턴스. 존재하지 않으면 {@code null} 반환
     */
    @Nullable
    public static UserData fromPlayerName(@NonNull String playerName) {
        return getAllUserDatas().stream()
                .filter(target -> target.getPlayerName().equalsIgnoreCase(playerName))
                .findFirst()
                .orElse(null);
    }

    /**
     * 접속했던 모든 플레이어의 유저 데이터를 불러온다.
     *
     * <p>플러그인 활성화 시 호출해야 한다.</p>
     */
    public static void initAllUserDatas() {
        try (Stream<Path> userDataPaths = Files.list(DMGR.getPlugin().getDataFolder().toPath().resolve(DIRECTORY_NAME))) {
            userDataPaths.map(path -> UserDataSerializer.instance.deserialize(FilenameUtils.removeExtension(path.getFileName().toString())))
                    .filter(userData -> !userData.isInitialized())
                    .forEach(userData -> {
                        userData.yamlFile.initSync();
                        userData.onInit();
                    });
        } catch (Exception ex) {
            ConsoleLogger.severe("전체 유저 데이터를 불러올 수 없음", ex);
        }
    }

    /**
     * 모든 유저의 데이터 정보를 반환한다.
     *
     * @return 유저 데이터 정보 인스턴스 목록
     */
    @NonNull
    @UnmodifiableView
    public static Collection<@NonNull UserData> getAllUserDatas() {
        return Collections.unmodifiableCollection(USER_DATA_MAP.values());
    }

    @Override
    @NonNull
    public AsyncTask<Void> init() {
        return yamlFile.init()
                .onFinish(this::onInit)
                .onError(ex -> ConsoleLogger.severe("{0}의 유저 데이터 불러오기 실패", ex, playerName));
    }

    /**
     * 유저 데이터 초기화 완료 시 실행할 작업.
     */
    private void onInit() {
        yamlFile.getDefaultSection().getEntry("playerName", "").set(playerName);
        ConsoleLogger.info("{0}의 유저 데이터 불러오기 완료", playerName);
    }

    @Override
    public boolean isInitialized() {
        return yamlFile.isInitialized();
    }

    /**
     * 유저의 데이터 정보를 저장한다.
     */
    @Nullable
    public AsyncTask<Void> save() {
        if (DMGR.getPlugin().isEnabled())
            return yamlFile.save();

        yamlFile.saveSync();
        return null;
    }

    /**
     * 지정한 전투원의 기록 정보를 반환한다.
     *
     * @param combatantType 전투원 종류
     * @return 전투원 기록 정보
     */
    @NonNull
    public UserData.CombatantRecord getCombatantRecord(@NonNull CombatantType combatantType) {
        return combatantRecordMap.get(combatantType);
    }

    /**
     * 유저 데이터에 해당하는 유저 인스턴스를 반환한다.
     *
     * @return 유저 인스턴스. 플레이어가 접속 중이 아니면 {@code null} 반환
     */
    @Nullable
    private User getOnlineUser() {
        Player player = Bukkit.getPlayer(playerUUID);
        if (player == null)
            return null;

        return User.fromPlayer(player);
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
     * <p>레벨이 증가했을 경우 효과를 재생한다.</p>
     *
     * @param xp 경험치. 0 이상의 값
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    public void setXp(int xp) {
        Validate.isTrue(xp >= 0, "xp >= 0 (%d)", xp);
        validate();

        boolean levelup = false;

        while (xp >= getNextLevelXp()) {
            xp -= getNextLevelXp();
            levelEntry.set(levelEntry.get() + 1);

            levelup = true;
        }

        xpEntry.set(xp);

        if (levelup)
            new DelayTask(() -> {
                User user = getOnlineUser();
                if (user == null)
                    return;

                user.sendTitle(getLevelPrefix() + " §e§l달성!", "", Timespan.ofSeconds(0.4), Timespan.ofSeconds(2), Timespan.ofSeconds(1.5),
                        Timespan.ofSeconds(2));
                LEVEL_UP_SOUND.play(user.getPlayer());
            }, 100);
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
        Validate.isTrue(money >= 0, "money >= 0 (%d)", money);
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
        Validate.isTrue(warning >= 0, "warning >= 0 (%d)", warning);
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
     * <p>티어가 바뀌었을 경우 효과를 재생한다.</p>
     *
     * @param rankRate 랭크 점수 (RR)
     */
    public void setRankRate(int rankRate) {
        Tier tier = getTier();
        rankRateEntry.set(rankRate);

        new DelayTask(() -> {
            User user = getOnlineUser();
            if (user == null)
                return;

            String title = null;
            SoundEffect sound = null;
            if (getTier().getMinScore() > tier.getMinScore()) {
                title = "§b§l등급 상승";
                sound = TIER_UP_SOUND;
            } else if (getTier().getMinScore() < tier.getMinScore()) {
                title = "§c§l등급 강등";
                sound = TIER_DOWN_SOUND;
            }

            if (title == null)
                return;

            user.sendTitle(title, getTier().getPrefix(), Timespan.ofSeconds(0.4), Timespan.ofSeconds(2), Timespan.ofSeconds(1.5), Timespan.ofSeconds(2));
            sound.play(user.getPlayer());
        }, 80);
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
    @UnmodifiableView
    public Set<@NonNull UserData> getBlockedPlayers() {
        return Collections.unmodifiableSet(new HashSet<>(blockedPlayersEntry.get()));
    }

    /**
     * 지정한 플레이어가 차단되었는지 확인한다.
     *
     * @param userData 대상 플레이어의 유저 데이터 정보
     * @return 차단 여부
     */
    public boolean isBlockedPlayer(@NonNull UserData userData) {
        return blockedPlayersEntry.get().contains(userData);
    }

    /**
     * 지정한 플레이어를 차단 목록에 추가한다.
     *
     * @param userData 대상 플레이어의 유저 데이터 정보
     */
    public void addBlockedPlayer(@NonNull UserData userData) {
        List<UserData> list = blockedPlayersEntry.get();
        if (list.contains(userData))
            return;

        list.add(userData);
        blockedPlayersEntry.set(list);
    }

    /**
     * 지정한 플레이어를 차단 목록에서 제거한다.
     *
     * @param userData 대상 플레이어의 유저 데이터 정보
     */
    public void removeBlockedPlayer(@NonNull UserData userData) {
        List<UserData> list = blockedPlayersEntry.get();
        list.remove(userData);

        blockedPlayersEntry.set(list);
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
     * @param duration 차단 기간
     * @param reason   차단 사유
     * @return 차단 해제 시점
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    @NonNull
    public Timestamp ban(@NonNull Timespan duration, @Nullable String reason) {
        if (reason == null)
            reason = "없음";

        Timestamp expiration = Timestamp.now().plus(duration);
        String finalReason = MessageFormat.format(MESSAGE_BANNED,
                DateFormatUtils.format(expiration.toDate(), "yyyy-MM-dd HH:mm:ss"),
                GeneralConfig.getConfig().getAdminContact());
        Bukkit.getBanList(BanList.Type.NAME).addBan(playerName, reason + "\n\n" + finalReason, expiration.toDate(), null);

        User user = getOnlineUser();
        if (user != null)
            user.kick(finalReason);

        return expiration;
    }

    /**
     * 플레이어가 서버에서 차단된 상태인지 확인한다.
     *
     * @return 차단 여부
     */
    public boolean isBanned() {
        return Bukkit.getBanList(BanList.Type.NAME).isBanned(playerName);
    }

    /**
     * 플레이어의 서버 차단을 해제한다.
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
     * @return 게임 플레이 시간
     */
    @NonNull
    public Timespan getPlayTime() {
        return Timespan.ofSeconds(combatantRecordMap.values().stream()
                .mapToInt(combatantRecord -> combatantRecord.playTimeEntry.get())
                .sum());
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

        int rank = RankManager.getInstance().getRankIndex(RankManager.RankType.RANK_RATE, this);
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
        return MessageFormat.format("{0} {1} {2}{3}§f",
                getTier().getPrefix(),
                getLevelPrefix(),
                (Bukkit.getOfflinePlayer(playerUUID).isOp() ? "§a" : "§f"),
                playerName);
    }

    /**
     * 플레이어의 프로필 정보 아이템을 반환한다.
     *
     * @return 프로필 정보 아이템
     */
    @NonNull
    public AsyncTask<@NonNull ItemStack> getProfileItem() {
        return PlayerSkin.fromUUID(playerUUID)
                .onFinish((Function<PlayerSkin, ItemStack>) playerSkin -> new ItemBuilder(playerSkin).setName(getDisplayName()).build());
    }

    /**
     * {@link UserData}의 직렬화 처리기 클래스.
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class UserDataSerializer implements Serializer<UserData, String> {
        @Getter
        private static final UserDataSerializer instance = new UserDataSerializer();

        @Override
        @NonNull
        public String serialize(@NonNull UserData value) {
            return value.getPlayerUUID().toString();
        }

        @Override
        @NonNull
        public UserData deserialize(@NonNull String value) {
            return UserData.fromUUID(UUID.fromString(value));
        }
    }

    /**
     * 유저 개인 설정.
     */
    public final class Config {
        /** 채팅 효과음 */
        private final YamlFile.Section.Entry<ChatSoundOption.ChatSound> chatSoundEntry;
        /** 야간 투시 여부 */
        private final YamlFile.Section.Entry<Boolean> nightVisionEntry;

        private Config() {
            YamlFile.Section section = yamlFile.getDefaultSection();
            this.chatSoundEntry = section.getEntry("chatSound", ChatSoundOption.ChatSound.PLING);
            this.nightVisionEntry = section.getEntry("nightVision", false);
        }

        /**
         * @return 채팅 효과음
         */
        @NonNull
        public ChatSoundOption.ChatSound getChatSound() {
            return chatSoundEntry.get();
        }

        /**
         * @param chatSound 채팅 효과음
         */
        public void setChatSound(@NonNull ChatSoundOption.ChatSound chatSound) {
            chatSoundEntry.set(chatSound);
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
    public final class CombatantRecord {
        /** 킬 */
        private final YamlFile.Section.Entry<Integer> killEntry;
        /** 데스 */
        private final YamlFile.Section.Entry<Integer> deathEntry;
        /** 플레이 시간 (초) */
        private final YamlFile.Section.Entry<Integer> playTimeEntry;
        /** 적용된 코어의 이름 목록 */
        private final YamlFile.Section.Entry<List<Core>> coresEntry;

        private CombatantRecord(@NonNull CombatantType combatantType) {
            YamlFile.Section section = yamlFile.getDefaultSection().getSection("record").getSection(combatantType.toString());
            this.killEntry = section.getEntry("kill", 0);
            this.deathEntry = section.getEntry("death", 0);
            this.playTimeEntry = section.getEntry("playTime", 0);
            this.coresEntry = section.getListEntry("cores", new TypeToken<List<Core>>() {
            });
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
         * @return 플레이 시간
         */
        @NonNull
        public Timespan getPlayTime() {
            return Timespan.ofSeconds(playTimeEntry.get());
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
        @UnmodifiableView
        public Set<@NonNull Core> getCores() {
            EnumSet<Core> cores = EnumSet.noneOf(Core.class);
            cores.addAll(coresEntry.get());

            return cores;
        }

        /**
         * 지정한 코어를 적용된 코어 목록에 추가한다.
         *
         * @param core 추가할 코어
         */
        public void addCore(@NonNull Core core) {
            List<Core> cores = coresEntry.get();
            if (cores.contains(core))
                return;

            cores.add(core);
            coresEntry.set(cores);
        }

        /**
         * 지정한 코어를 적용된 코어 목록에서 제거한다.
         *
         * @param core 제거할 코어
         */
        public void removeCore(@NonNull Core core) {
            List<Core> cores = coresEntry.get();
            cores.remove(core);

            coresEntry.set(cores);
        }
    }
}
