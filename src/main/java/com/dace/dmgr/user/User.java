package com.dace.dmgr.user;

import com.comphenix.packetwrapper.WrapperPlayServerEntityMetadata;
import com.comphenix.packetwrapper.WrapperPlayServerScoreboardTeam;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.dace.dmgr.*;
import com.dace.dmgr.combat.FreeCombat;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.effect.SoundEffect;
import com.dace.dmgr.effect.TextHologram;
import com.dace.dmgr.event.listener.OnAsyncPlayerChat;
import com.dace.dmgr.event.listener.OnPlayerCommandPreprocess;
import com.dace.dmgr.event.listener.OnPlayerQuit;
import com.dace.dmgr.event.listener.OnPlayerResourcePackStatus;
import com.dace.dmgr.game.GameRoom;
import com.dace.dmgr.game.GameUser;
import com.dace.dmgr.item.DefinedItem;
import com.dace.dmgr.item.ItemBuilder;
import com.dace.dmgr.item.PlayerSkullUtil;
import com.dace.dmgr.item.gui.GUI;
import com.dace.dmgr.item.gui.SelectGame;
import com.dace.dmgr.util.StringFormUtil;
import com.dace.dmgr.util.task.AsyncTask;
import com.dace.dmgr.util.task.DelayTask;
import com.dace.dmgr.util.task.IntervalTask;
import com.dace.dmgr.util.task.TaskManager;
import com.keenant.tabbed.item.TextTabItem;
import com.keenant.tabbed.tablist.TabList;
import com.keenant.tabbed.tablist.TableTabList;
import com.keenant.tabbed.util.Skin;
import com.keenant.tabbed.util.Skins;
import fr.minuskube.netherboard.bukkit.BPlayerBoard;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import net.skinsrestorer.api.PlayerWrapper;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.text.MessageFormat;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.LongConsumer;

/**
 * 유저 정보 및 상태를 관리하는 클래스.
 *
 * @see UserData
 */
public final class User {
    /** 유저 목록 (플레이어 : 유저 정보) */
    private static final HashMap<Player, User> USER_MAP = new HashMap<>();

    /** 오류 발생으로 강제퇴장 시 표시되는 메시지 */
    private static final String MESSAGE_KICK_ERR = String.join("\n",
            "{0}§c유저 데이터를 불러오는 중 오류가 발생했습니다.",
            "",
            "§f잠시 후 다시 시도하거나, 관리자에게 문의하십시오.",
            "",
            "§7오류 문의 : {1}");
    /** 리소스팩 미적용으로 강제퇴장 시 표시되는 메시지 */
    private static final String MESSAGE_KICK_DENY = String.join("\n",
            "{0}§c리소스팩 적용을 활성화 하십시오.",
            "",
            "§e멀티플레이 → 편집 → 서버 리소스 팩 : 사용",
            "",
            "§f다운로드가 되지 않으면, §n.minecraft → server-resource-packs§f 폴더를 생성하십시오.",
            "",
            "§7다운로드 오류 문의 : {1}");
    /** 리소스팩 적용 중 오류로 강제퇴장 시 표시되는 메시지 */
    private static final String MESSAGE_KICK_RESOURCE_ERR = String.join("\n",
            "{0}§c리소스팩 적용 중 오류가 발생했습니다.",
            "",
            "§f§n.minecraft → server-resource-packs§f 폴더의 내용물을 전부 삭제한 뒤 재접속 하시기 바랍니다.",
            "",
            "§e문제가 해결되지 않으면 문의 바랍니다.",
            "",
            "§7다운로드 오류 문의 : {1}");
    /** 채팅의 메시지 포맷 패턴 */
    private static final String CHAT_FORMAT_PATTERN = "<{0}> {1}";
    /** 레벨 업 효과음 */
    private static final SoundEffect LEVEL_UP_SOUND = new SoundEffect(
            SoundEffect.SoundInfo.builder("random.good").volume(1000).pitch(1).build());
    /** 티어 승급 효과음 */
    private static final SoundEffect TIER_UP_SOUND = new SoundEffect(
            SoundEffect.SoundInfo.builder(Sound.UI_TOAST_CHALLENGE_COMPLETE).volume(1000).pitch(1.5).build());
    /** 티어 강등 효과음 */
    private static final SoundEffect TIER_DOWN_SOUND = new SoundEffect(
            SoundEffect.SoundInfo.builder(Sound.ENTITY_BLAZE_DEATH).volume(1000).pitch(0.5).build());
    /** 경고 액션바 효과음 */
    private static final SoundEffect ALERT_SOUND = new SoundEffect(
            SoundEffect.SoundInfo.builder("new.block.note_block.bit").volume(0.25).pitch(0.7).build());
    /** 타자기 효과 타이틀 효과음 */
    private static final SoundEffect TYPEWRITER_TITLE_SOUND = new SoundEffect(
            SoundEffect.SoundInfo.builder("new.block.note_block.bass").volume(1).pitch(1.5).build());

    /** 플레이어 인스턴스 */
    @NonNull
    @Getter
    private final Player player;
    /** 유저 데이터 정보 인스턴스 */
    @NonNull
    @Getter
    private final UserData userData;
    /** 태스크 관리 인스턴스 */
    private final TaskManager taskManager = new TaskManager();
    /** 플레이어 탭리스트 관리 인스턴스 */
    @NonNull
    @Getter
    private final TabListManager tabListManager;
    /** 플레이어 사이드바 관리 인스턴스 */
    @NonNull
    @Getter
    private final SidebarManager sidebarManager;
    /** 발광 효과 정보 목록 (발광 엔티티 : 발광 효과 정보) */
    private final WeakHashMap<Entity, GlowingInfo> glowingInfoMap = new WeakHashMap<>();
    /** 플레이어 인벤토리 GUI */
    @NonNull
    @Getter
    private final GUI gui;

    /** 채팅 쿨타임 타임스탬프 */
    private Timestamp chatCooldownTimestamp = Timestamp.now();
    /** 명령어 쿨타임 타임스탬프 */
    private Timestamp commandCooldownTimestamp = Timestamp.now();
    /** 타자기 효과 타이틀 타임스탬프 */
    private Timestamp typewriterTitleTimestamp = Timestamp.now();
    /** 타이틀 덮어쓰기 타임스탬프 */
    private Timestamp titleOverrideTimestamp = Timestamp.now();
    /** 액션바 덮어쓰기 타임스탬프 */
    private Timestamp actionBarOverrideTimestamp = Timestamp.now();

    /** 이름표 숨기기용 갑옷 거치대 인스턴스 */
    @Nullable
    private ArmorStand nameTagHider;
    /** 이름표 홀로그램 */
    @Nullable
    private TextHologram nameTagHologram;
    /** 현재 핑 (ms) */
    @Setter
    private int ping = 0;
    /** 리소스팩 적용 수락 여부 */
    private boolean isResourcePackAccepted = false;
    /** 현재 귓속말 대상 */
    @Nullable
    @Getter
    @Setter
    private User messageTarget;
    /** 현재 입장한 게임 방 */
    @Nullable
    @Getter
    private GameRoom gameRoom;
    /** 관리자 채팅 여부 */
    @Getter
    @Setter
    private boolean isAdminChat = false;
    /** 자유 전투 입장 여부 */
    @Getter
    private boolean isInFreeCombat = false;

    /**
     * 유저 인스턴스를 생성한다.
     *
     * @param player 대상 플레이어
     */
    private User(@NonNull Player player) {
        this.player = player;
        this.userData = UserData.fromPlayer(player);
        this.tabListManager = new TabListManager(player);
        this.sidebarManager = new SidebarManager(player);
        this.gui = new GUI(player.getInventory());

        USER_MAP.put(player, this);

        init();
    }

    /**
     * 지정한 플레이어의 유저 정보 인스턴스를 반환한다.
     *
     * @param player 대상 플레이어
     * @return 유저 인스턴스
     */
    @NonNull
    public static User fromPlayer(@NonNull Player player) {
        User user = USER_MAP.get(player);
        if (user == null)
            user = new User(player);

        return user;
    }

    /**
     * 모든 유저 정보를 반환한다.
     *
     * @return 유저 정보 인스턴스 목록
     */
    @NonNull
    @UnmodifiableView
    public static Collection<@NonNull User> getAllUsers() {
        return Collections.unmodifiableCollection(USER_MAP.values());
    }

    /**
     * 유저 초기화 작업을 수행한다.
     */
    private void init() {
        reset();
        taskManager.add(new DelayTask(this::clearChat, 10));

        if (userData.isInitialized())
            onInit();
        else
            taskManager.add(userData.init()
                    .onFinish(this::onInit)
                    .onError(ex -> taskManager.add(new DelayTask(() -> {
                        String message = MessageFormat.format(MESSAGE_KICK_ERR,
                                GeneralConfig.getConfig().getMessagePrefix(),
                                GeneralConfig.getConfig().getAdminContact());
                        player.kickPlayer(message);
                    }, 60))));
    }

    /**
     * 유저 초기화 완료 시 실행할 작업.
     */
    private void onInit() {
        disableCollision();
        taskManager.add(new DelayTask(this::createNameTagHider, 1));
        taskManager.add(new DelayTask(this::sendResourcePack, 10));
        taskManager.add(resetSkin());

        nameTagHologram = new TextHologram(player, target -> target != player && CombatUser.fromUser(this) == null,
                0, userData.getDisplayName());

        if (!userData.getConfig().isKoreanChat())
            player.performCommand("kakc chmod 0");

        sendTitle("§bWelcome!", "§f메뉴를 사용하려면 §nF키§f를 누르십시오.", Timespan.ZERO, Timespan.ofSeconds(5), Timespan.ofSeconds(3));

        taskManager.add(new IntervalTask((LongConsumer) i -> onSecond(), 20));
    }

    /**
     * 플레이어끼리 밀치는 것을 비활성화한다.
     */
    private void disableCollision() {
        Scoreboard scoreBoard = player.getScoreboard();
        Team team = scoreBoard.getTeam("Default");
        if (team == null)
            team = scoreBoard.registerNewTeam("Default");
        team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
        team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);
        team.addEntry(player.getName());
    }

    /**
     * 이름표 숨기기 갑옷 거치대 인스턴스를 생성한다.
     */
    private void createNameTagHider() {
        if (nameTagHider != null)
            return;

        nameTagHider = player.getWorld().spawn(player.getLocation(), ArmorStand.class);
        nameTagHider.setCustomName(DMGR.TEMPORARY_ENTITY_CUSTOM_NAME);
        nameTagHider.setSilent(true);
        nameTagHider.setInvulnerable(true);
        nameTagHider.setGravity(false);
        nameTagHider.setAI(false);
        nameTagHider.setMarker(true);
        nameTagHider.setVisible(false);

        if (!player.getPassengers().contains(nameTagHider))
            player.addPassenger(nameTagHider);
    }

    /**
     * 플레이어에게 리소스팩을 전송하고, 적용하지 않을 시 강제 퇴장 시킨다.
     */
    private void sendResourcePack() {
        player.setResourcePack(GeneralConfig.getConfig().getResourcePackUrl());

        taskManager.add(new DelayTask(() -> {
            if (!isResourcePackAccepted) {
                String message = MessageFormat.format(MESSAGE_KICK_DENY,
                        GeneralConfig.getConfig().getMessagePrefix(),
                        GeneralConfig.getConfig().getAdminContact());
                player.kickPlayer(message);
            }
        }, GeneralConfig.getConfig().getResourcePackTimeout().toTicks()));
    }

    /**
     * 리소스팩 적용 상태가 변경되었을 때 실행할 작업.
     *
     * @param status 리소스팩 적용 상태
     * @see OnPlayerResourcePackStatus
     */
    public void onResourcePackStatus(@NonNull PlayerResourcePackStatusEvent.Status status) {
        isResourcePackAccepted = status == PlayerResourcePackStatusEvent.Status.ACCEPTED
                || status == PlayerResourcePackStatusEvent.Status.SUCCESSFULLY_LOADED;

        if (status == PlayerResourcePackStatusEvent.Status.FAILED_DOWNLOAD)
            player.kickPlayer(MessageFormat.format(MESSAGE_KICK_RESOURCE_ERR,
                    GeneralConfig.getConfig().getMessagePrefix(),
                    GeneralConfig.getConfig().getAdminContact()));
    }

    /**
     * 매 초마다 실행할 작업.
     */
    private void onSecond() {
        if (userData.getConfig().isNightVision())
            player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0, false, false));
        else
            player.removePotionEffect(PotionEffectType.NIGHT_VISION);

        if (CombatUser.fromUser(this) == null) {
            sendActionBar("§1메뉴를 사용하려면 §nF키§1를 누르십시오.");
            updateLobbySidebar();
        }

        GameUser gameUser = GameUser.fromUser(this);
        if (gameUser == null)
            updateLobbyTabList();

        tabListManager.update();
    }

    /**
     * 로비 사이드바를 업데이트한다.
     */
    private void updateLobbySidebar() {
        int reqXp = userData.getNextLevelXp();
        int rank = userData.isRanked() ? userData.getRankRate() : 0;
        int reqRank = userData.getTier().getMaxScore();
        int curRank = userData.getTier().getMinScore();

        switch (userData.getTier()) {
            case STONE:
                curRank = userData.getTier().getMaxScore();
                break;
            case DIAMOND:
            case NETHERITE:
                reqRank = userData.getTier().getMinScore();
                break;
            case NONE:
                reqRank = 1;
                curRank = 0;
                break;
            default:
                break;
        }

        String levelText = MessageFormat.format("{0} §2[{1}/{2}]",
                StringFormUtil.getProgressBar(userData.getXp(), reqXp, ChatColor.DARK_GREEN),
                userData.getXp(),
                reqXp);
        String rankText = MessageFormat.format("{0} §3[{1}/{2}]",
                StringFormUtil.getProgressBar((rank - curRank), (reqRank - curRank), ChatColor.DARK_AQUA),
                rank,
                reqRank);

        sidebarManager.setName("§b§n" + player.getName());
        sidebarManager.setAll(
                "",
                "§e보유 중인 돈",
                "§6" + String.format("%,d", userData.getMoney()),
                "",
                "§f레벨 : " + userData.getLevelPrefix(), levelText,
                "",
                "§f랭크 : " + userData.getTier().getPrefix(), rankText);
    }

    /**
     * 로비 탭리스트를 업데이트한다.
     */
    private void updateLobbyTabList() {
        long freeMemory = Runtime.getRuntime().freeMemory() / 1024 / 1024;
        long totalMemory = Runtime.getRuntime().totalMemory() / 1024 / 1024;
        long memory = totalMemory - freeMemory;
        double memoryPercent = (double) memory / totalMemory;
        double tps = DMGR.getTps();

        ChatColor memoryColor = ChatColor.RED;
        if (memoryPercent < 0.5)
            memoryColor = ChatColor.GREEN;
        else if (memoryPercent < 0.75)
            memoryColor = ChatColor.YELLOW;
        else if (memoryPercent < 0.9)
            memoryColor = ChatColor.GOLD;

        ChatColor pingColor = ChatColor.RED;
        if (ping < 70)
            pingColor = ChatColor.GREEN;
        else if (ping < 100)
            pingColor = ChatColor.YELLOW;
        else if (ping < 130)
            pingColor = ChatColor.GOLD;

        ChatColor tpsColor = ChatColor.GREEN;
        if (tps < 19)
            tpsColor = ChatColor.RED;
        else if (tps < 19.4)
            tpsColor = ChatColor.GOLD;
        else if (tps < 19.7)
            tpsColor = ChatColor.YELLOW;

        tabListManager.setHeader("\n" + GeneralConfig.getConfig().getMessagePrefix() + "§e스킬 PVP 미니게임 서버 §f:: §d§nDMGR.mcsv.kr\n");
        tabListManager.setFooter("\n§7현재 서버는 테스트 단계이며, 시스템 상 문제점이나 버그가 발생할 수 있습니다.\n");

        tabListManager.setItem(0, 0, "§f§l§n 서버 상태 ", Skins.getPlayer("TimmyTimothy"));
        tabListManager.setItem(0, 2, MessageFormat.format("§f PING §7:: {0}{1} ms", pingColor, ping),
                Skins.getPlayer("FranciRoma"));
        tabListManager.setItem(0, 3, MessageFormat.format("§f 메모리 §7:: {0}{1} §f/ {2} (MB)", memoryColor, memory, totalMemory),
                Skins.getPlayer("AddelBurgh"));
        tabListManager.setItem(0, 4, MessageFormat.format("§f TPS §7:: {0}{1} tick/s", tpsColor, tps),
                Skins.getPlayer("CommandBlock"));
        tabListManager.setItem(0, 5, MessageFormat.format("§f 접속자 수 §7:: §f{0}명", Bukkit.getOnlinePlayers().size()),
                Skins.getPlayer("MHF_Steve"));
        tabListManager.setItem(0, 7, MessageFormat.format("§f§n §e§n{0}§f§l§n님의 전적 ", player.getName()),
                Skins.getPlayer(player));
        tabListManager.setItem(0, 9, MessageFormat.format("§e 승률 §7:: §b{0}승 §f/ §c{1}패 §f({2}%)",
                        userData.getWinCount(),
                        userData.getLoseCount(),
                        (double) userData.getWinCount() / (userData.getNormalPlayCount() + userData.getRankPlayCount()) * 100),
                Skins.getPlayer("goldblock"));
        tabListManager.setItem(0, 10, MessageFormat.format("§e 탈주 §7:: §c{0}회", userData.getQuitCount()),
                Skins.getPlayer("MHF_TNT2"));
        tabListManager.setItem(0, 11, MessageFormat.format("§e 플레이 시간 §7:: §f{0}",
                        DurationFormatUtils.formatDuration(userData.getPlayTime().toMilliseconds(), "d일 H시간 m분")),
                Skins.getPlayer("Olaf_C"));

        updateLobbyTabListUsers();
    }

    /**
     * 로비 탭리스트의 접속자 목록을 업데이트한다.
     */
    private void updateLobbyTabListUsers() {
        List<User> lobbyUsers = new ArrayList<>();
        List<User> adminUsers = new ArrayList<>();
        getAllUsers().stream()
                .sorted(Comparator.comparing(target -> target.getPlayer().getName()))
                .forEach(target -> (target.getPlayer().isOp() ? adminUsers : lobbyUsers).add(target));

        tabListManager.setItem(1, 0, MessageFormat.format("§a§l§n 접속 인원 §f({0}명)", lobbyUsers.size()), Skins.getDot(ChatColor.GREEN));
        for (int i = 0; i < 38; i++) {
            int column = 1 + i % 2;
            int row = i / 2 + 1;

            if (i > lobbyUsers.size() - 1)
                tabListManager.removeItem(column, row);
            else {
                User lobbyUser = lobbyUsers.get(i);
                tabListManager.setItem(column, row, lobbyUser.getTabListPlayerName(), lobbyUser);
            }
        }

        tabListManager.setItem(3, 0, MessageFormat.format("§b§l§n 관리자 §f({0}명)", adminUsers.size()), Skins.getDot(ChatColor.AQUA));
        for (int i = 0; i < 19; i++) {
            int column = 3;
            int row = i + 1;

            if (i > adminUsers.size() - 1)
                tabListManager.removeItem(column, row);
            else {
                User adminUser = adminUsers.get(i);
                tabListManager.setItem(column, row, adminUser.getTabListPlayerName(), adminUser);
            }
        }
    }

    /**
     * 탭리스트에 사용되는 플레이어의 이름을 반환한다.
     *
     * @return 이름
     */
    @NonNull
    private String getTabListPlayerName() {
        String prefix = "§7[로비]";

        if (gameRoom != null)
            prefix = MessageFormat.format(gameRoom.isRanked() ? "§6[랭크 {0}]" : "§a[일반 {0}]", gameRoom.getNumber());
        else if (isInFreeCombat())
            prefix = "§7[자유 전투]";

        return MessageFormat.format(" {0} §f{1}", prefix, player.getName());
    }

    /**
     * 채팅을 입력했을 때 실행할 작업.
     *
     * @param message 입력 메시지
     * @see OnAsyncPlayerChat
     */
    public void onChat(@NonNull String message) {
        if (!player.isOp()) {
            if (chatCooldownTimestamp.isAfter(Timestamp.now())) {
                sendMessageWarn("채팅을 천천히 하십시오.");
                return;
            }

            chatCooldownTimestamp = Timestamp.now().plus(GeneralConfig.getConfig().getChatCooldown());
        }

        if (messageTarget == null) {
            if (isAdminChat()) {
                getAllUsers().stream()
                        .filter(target -> target.getPlayer().isOp())
                        .forEach(target -> sendChatMessage(target, ChatColor.DARK_AQUA + message));
            } else {
                GameUser gameUser = GameUser.fromUser(this);

                if (gameUser == null)
                    getAllUsers().forEach(target -> sendChatMessage(target, message));
                else
                    gameUser.broadcastChatMessage(message, gameUser.isTeamChat());
            }
        } else {
            sendChatMessage(this, ChatColor.GRAY + message);
            sendChatMessage(messageTarget, ChatColor.GRAY + message);
        }
    }

    /**
     * 수신 플레이어에게 채팅 메시지를 전송하고 효과음을 재생한다.
     *
     * @param receiver 수신 플레이어
     * @param message  메시지
     */
    private void sendChatMessage(@NonNull User receiver, @NonNull String message) {
        UserData receiverUserData = receiver.getUserData();
        if (receiverUserData.isBlockedPlayer(userData))
            return;

        String pattern = CHAT_FORMAT_PATTERN;
        String name = userData.getDisplayName();

        if (messageTarget != null) {
            if (this != receiver)
                name += "§7님의 개인 메시지§f";
        } else if (isAdminChat)
            pattern = "§7§l[관리자] §f" + pattern;

        message = MessageFormat.format(pattern, name, message);

        receiver.getPlayer().sendMessage(message);
        receiverUserData.getConfig().getChatSound().getSound().play(receiver.getPlayer());
    }

    /**
     * 서버에서 퇴장했을 때 실행할 작업.
     *
     * @see OnPlayerQuit
     */
    public void onQuit() {
        reset();

        if (gameRoom != null)
            quitGame();

        taskManager.stop();
        tabListManager.delete();
        sidebarManager.delete();
        glowingInfoMap.values().iterator().forEachRemaining(GlowingInfo::removeGlowing);

        if (userData.isInitialized()) {
            if (nameTagHider != null) {
                nameTagHider.remove();
                nameTagHider = null;
            }
            if (nameTagHologram != null) {
                nameTagHologram.remove();
                nameTagHologram = null;
            }

            userData.save();
        }

        USER_MAP.remove(player);
    }

    /**
     * 명령어를 입력했을 때 실행할 작업.
     *
     * @return 성공 여부. 입력이 유효하면 {@code true} 반환
     * @see OnPlayerCommandPreprocess
     */
    public boolean onCommand() {
        if (player.isOp())
            return true;

        if (commandCooldownTimestamp.isAfter(Timestamp.now())) {
            sendMessageWarn("동작이 너무 빠릅니다.");
            return false;
        }

        commandCooldownTimestamp = Timestamp.now().plus(GeneralConfig.getConfig().getCommandCooldown());

        return true;
    }

    /**
     * 레벨 상승 시 효과를 재생한다.
     */
    void playLevelUpEffect() {
        taskManager.add(new DelayTask(() -> {
            sendTitle(userData.getLevelPrefix() + " §e§l달성!", "", Timespan.ofSeconds(0.4), Timespan.ofSeconds(2), Timespan.ofSeconds(1.5),
                    Timespan.ofSeconds(2));
            LEVEL_UP_SOUND.play(player);
        }, 100));
    }

    /**
     * 티어 상승 시 효과를 재생한다.
     */
    void playTierUpEffect() {
        taskManager.add(new DelayTask(() -> {
            sendTitle("§b§l등급 상승", userData.getTier().getPrefix(), Timespan.ofSeconds(0.4), Timespan.ofSeconds(2), Timespan.ofSeconds(1.5),
                    Timespan.ofSeconds(2));
            TIER_UP_SOUND.play(player);
        }, 80));
    }

    /**
     * 티어 하락 시 효과를 재생한다.
     */
    void playTierDownEffect() {
        taskManager.add(new DelayTask(() -> {
            sendTitle("§c§l등급 강등", userData.getTier().getPrefix(), Timespan.ofSeconds(0.4), Timespan.ofSeconds(2), Timespan.ofSeconds(1.5),
                    Timespan.ofSeconds(2));
            TIER_DOWN_SOUND.play(player);
        }, 80));
    }

    /**
     * 플레이어의 체력, 이동속도 등의 모든 상태를 재설정하고 스폰으로 이동시킨다.
     */
    public void reset() {
        player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20);
        player.setHealth(20);
        player.setExp(0);
        player.setLevel(0);
        player.setWalkSpeed(0.2F);
        player.getActivePotionEffects().forEach(potionEffect -> player.removePotionEffect(potionEffect.getType()));

        gui.clear();
        updateGUI();

        teleport(GeneralConfig.getConfig().getLobbyLocation());
        quitFreeCombat();

        getAllUsers().forEach(target -> {
            removeGlowing(target.getPlayer());
            target.removeGlowing(this.player);
        });

        if (userData.isInitialized()) {
            sidebarManager.clear();
            tabListManager.clearItems();
        }

        CombatUser combatUser = CombatUser.fromUser(this);
        if (combatUser != null)
            combatUser.remove();
    }

    /**
     * 인벤토리 GUI를 업데이트한다.
     */
    private void updateGUI() {
        for (MenuItem menuItem : MenuItem.values())
            if (menuItem != MenuItem.TEAM_GAME && gameRoom != null || menuItem != MenuItem.TEAM_GAME_EXIT && gameRoom == null)
                gui.set(menuItem.slotIndex, menuItem.definedItem);

        player.updateInventory();
    }

    /**
     * 플레이어의 채팅창을 청소한다.
     */
    public void clearChat() {
        for (int i = 0; i < 100; i++)
            player.sendMessage("§f");
    }

    /**
     * 플레이어의 채팅창에 일반 메시지를 전송한다.
     *
     * <p>기본 색상은 흰색('§f')이며, '§r'을 사용하여 기본 색상으로 초기화할 수 있다.</p>
     *
     * <p>Example:</p>
     *
     * <pre><font color="lime">Hello, </font>World!</pre>
     * <pre><code>
     * user.sendMessageInfo("§aHello, §rWorld!");
     * </code></pre>
     *
     * @param message 메시지
     */
    public void sendMessageInfo(@NonNull String message) {
        message = message.replace("\n", "\n" + GeneralConfig.getConfig().getMessagePrefix());
        player.sendMessage(GeneralConfig.getConfig().getMessagePrefix() + message);
    }

    /**
     * 플레이어의 채팅창에 일반 메시지를 전송한다.
     *
     * <p>기본 색상은 흰색('§f')이며, '§r'을 사용하여 기본 색상으로 초기화할 수 있다.</p>
     *
     * <p>지정한 arguments의 n번째 인덱스가 메시지에 포함된 '{n}'을 치환한다.</p>
     *
     * <p>Example:</p>
     *
     * <pre><font color="lime">Hello, </font>World!</pre>
     * <pre><code>
     * user.sendMessageInfo("§aHello, §r{0}!", "World");
     * </code></pre>
     *
     * @param message   메시지
     * @param arguments 포맷에 사용할 인자 목록
     */
    public void sendMessageInfo(@NonNull String message, @NonNull Object @NonNull ... arguments) {
        message = MessageFormat.format(message, arguments).replace("\n", "\n" + GeneralConfig.getConfig().getMessagePrefix());
        player.sendMessage(GeneralConfig.getConfig().getMessagePrefix() + message);
    }

    /**
     * 플레이어의 채팅창에 경고 메시지를 전송한다.
     *
     * <p>기본 색상은 빨간색('§c')이며, '§r'을 사용하여 적용된 색상을 초기화할 수 있다.</p>
     *
     * <p>Example:</p>
     *
     * <pre><font color="lime">Hello, </font><font color="red">World!</font></pre>
     * <pre><code>
     * user.sendMessageWarn("§aHello, §rWorld!");
     * </code></pre>
     *
     * @param message 메시지
     */
    public void sendMessageWarn(@NonNull String message) {
        message = message.replace("§r", "§c")
                .replace("\n", "\n" + GeneralConfig.getConfig().getMessagePrefix());
        player.sendMessage(GeneralConfig.getConfig().getMessagePrefix() + ChatColor.RED + message);
    }

    /**
     * 플레이어의 채팅창에 경고 메시지를 전송한다.
     *
     * <p>기본 색상은 빨간색('§c')이며, '§r'을 사용하여 적용된 색상을 초기화할 수 있다.</p>
     *
     * <p>지정한 arguments의 n번째 인덱스가 메시지에 포함된 '{n}'을 치환한다.</p>
     *
     * <p>Example:</p>
     *
     * <pre><font color="lime">Hello, </font><font color="red">World!</font></pre>
     * <pre><code>
     * user.sendMessageWarn("§aHello, §r{0}!", "World");
     * </code></pre>
     *
     * @param message   메시지
     * @param arguments 포맷에 사용할 인자 목록
     */
    public void sendMessageWarn(@NonNull String message, @NonNull Object @NonNull ... arguments) {
        message = MessageFormat.format(message, arguments)
                .replace("§r", "§c")
                .replace("\n", "\n" + GeneralConfig.getConfig().getMessagePrefix());
        player.sendMessage(GeneralConfig.getConfig().getMessagePrefix() + ChatColor.RED + message);
    }

    /**
     * 플레이어에게 액션바를 전송한다.
     *
     * <p>덮어쓰기 지속시간 동안 기본 액션바 출력을 무시한다.</p>
     *
     * @param message          메시지
     * @param overrideDuration 덮어쓰기 지속시간
     * @see User#sendActionBar(String)
     */
    public void sendActionBar(@NonNull String message, @NonNull Timespan overrideDuration) {
        actionBarOverrideTimestamp = Timestamp.now().plus(overrideDuration);

        TextComponent actionBar = new TextComponent(message);
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, actionBar);
    }

    /**
     * 플레이어에게 액션바를 전송한다.
     *
     * @param message 메시지
     */
    public void sendActionBar(@NonNull String message) {
        if (actionBarOverrideTimestamp.isAfter(Timestamp.now()))
            return;

        sendActionBar(message, Timespan.ZERO);
    }

    /**
     * 플레이어에게 경고 액션바를 전송한다.
     *
     * <p>주로 특정 동작의 사용 금지 상태를 알리기 위해 사용한다.</p>
     *
     * @param message 경고 메시지
     */
    public void sendAlertActionBar(@NonNull String message) {
        ALERT_SOUND.play(player);

        taskManager.add(new IntervalTask(i -> {
            ChatColor color = ChatColor.YELLOW;
            if (i == 1)
                color = ChatColor.GOLD;
            else if (i == 2)
                color = ChatColor.RED;

            sendActionBar(color + ChatColor.stripColor(message), Timespan.ofSeconds(0.8));
        }, 1, 3));
    }

    /**
     * 플레이어에게 타이틀을 전송한다.
     *
     * <p>덮어쓰기 지속시간 동안 기본 타이틀 출력을 무시한다.</p>
     *
     * @param title            제목
     * @param subtitle         부제목
     * @param fadeInDuration   나타나는 시간
     * @param stayDuration     유지 시간
     * @param fadeOutDuration  사라지는 시간
     * @param overrideDuration 덮어쓰기 지속시간
     * @see User#sendTitle(String, String, Timespan, Timespan, Timespan)
     */
    public void sendTitle(@NonNull String title, @NonNull String subtitle, @NonNull Timespan fadeInDuration, @NonNull Timespan stayDuration,
                          @NonNull Timespan fadeOutDuration, @NonNull Timespan overrideDuration) {
        titleOverrideTimestamp = Timestamp.now().plus(overrideDuration);

        player.sendTitle(title, subtitle, (int) Math.min(Integer.MAX_VALUE, fadeInDuration.toTicks()),
                (int) Math.min(Integer.MAX_VALUE, stayDuration.toTicks()), (int) Math.min(Integer.MAX_VALUE, fadeOutDuration.toTicks()));
    }

    /**
     * 플레이어에게 타이틀을 전송한다.
     *
     * @param title           제목
     * @param subtitle        부제목
     * @param fadeInDuration  나타나는 시간
     * @param stayDuration    유지 시간
     * @param fadeOutDuration 사라지는 시간
     */
    public void sendTitle(@NonNull String title, @NonNull String subtitle, @NonNull Timespan fadeInDuration, @NonNull Timespan stayDuration,
                          @NonNull Timespan fadeOutDuration) {
        if (titleOverrideTimestamp.isAfter(Timestamp.now()))
            return;

        sendTitle(title, subtitle, fadeInDuration, stayDuration, fadeOutDuration, Timespan.ZERO);
    }

    /**
     * 플레이어에게 타자기 효과 타이틀을 전송한다.
     *
     * <p>순차적으로 한 글자씩 나타나는 효과이다.</p>
     *
     * @param prefix  접두사
     * @param message 메시지
     */
    public void sendTypewriterTitle(@NonNull String prefix, @NonNull String message) {
        int delay = 0;
        StringBuilder text = new StringBuilder(prefix + " §f");

        for (int i = 0; i < message.length(); i++) {
            char nextChar = message.charAt(i);

            taskManager.add(new DelayTask(() -> {
                text.append(nextChar);
                typewriterTitleTimestamp = Timestamp.now().plus(Timespan.ofSeconds(2.5));

                sendTitle("", text.toString(), Timespan.ZERO, Timespan.ofSeconds(2), Timespan.ofSeconds(0.5));
                TYPEWRITER_TITLE_SOUND.play(player);
            }, delay));

            if (nextChar == '.' || nextChar == ',')
                delay += 4;
            else if (nextChar == ' ')
                delay += 2;
            else
                delay += 1;
        }
    }

    /**
     * 플레이어의 타자기 효과 타이틀이 출력 중인지 확인한다.
     *
     * @return 출력 중이면 {@code true} 반환
     * @see User#sendTypewriterTitle(String, String)
     */
    public boolean isTypewriterTitlePrinting() {
        return typewriterTitleTimestamp.isAfter(Timestamp.now());
    }

    /**
     * 플레이어에게 지정한 엔티티를 지속시간동안 발광 상태로 표시한다.
     *
     * @param target   발광 효과를 적용할 엔티티
     * @param color    색상
     * @param duration 지속시간
     */
    public void setGlowing(@NonNull Entity target, @NonNull ChatColor color, @NonNull Timespan duration) {
        glowingInfoMap.computeIfAbsent(target, k -> new GlowingInfo(this, target)).setGlowing(color, duration);
    }

    /**
     * 플레이어에게 지정한 엔티티를 발광 상태로 표시한다.
     *
     * @param target 발광 효과를 적용할 엔티티
     * @param color  색상
     */
    public void setGlowing(@NonNull Entity target, @NonNull ChatColor color) {
        setGlowing(target, color, Timespan.MAX);
    }

    /**
     * 지정한 엔티티가 발광 상태인지 확인한다.
     *
     * @param target 확인할 엔티티
     * @return 플레이어에게 발광 상태면 {@code true} 반환
     */
    public boolean isGlowing(@NonNull Entity target) {
        GlowingInfo glowingInfo = glowingInfoMap.get(target);
        return glowingInfo != null && glowingInfo.expiration.isAfter(Timestamp.now());
    }

    /**
     * 지정한 엔티티의 발광 효과를 제거한다.
     *
     * @param target 발광 효과가 적용된 엔티티
     */
    public void removeGlowing(@NonNull Entity target) {
        GlowingInfo glowingInfo = glowingInfoMap.remove(target);
        if (glowingInfo != null)
            glowingInfo.removeGlowing();
    }

    /**
     * 플레이어를 지정한 위치로 순간이동 시킨다.
     *
     * <p>이름표 숨기기 기능으로 인해 기본 텔레포트가 되지 않기 때문에 사용한다.</p>
     *
     * @param location 이동할 위치
     */
    public void teleport(@NonNull Location location) {
        if (nameTagHider == null)
            player.teleport(location);
        else {
            player.removePassenger(nameTagHider);
            player.teleport(location);
            nameTagHider.teleport(location);
            player.addPassenger(nameTagHider);
        }
    }

    /**
     * 플레이어를 지정한 게임 방에 입장시킨다.
     *
     * @param gameRoom 대상 게임 방
     */
    public void joinGame(@NonNull GameRoom gameRoom) {
        if (this.gameRoom != null || !gameRoom.canJoin())
            return;

        this.gameRoom = gameRoom;
        gameRoom.onJoin(this);

        updateGUI();
    }

    /**
     * 플레이어를 현재 입장한 게임 방에서 퇴장시킨다.
     */
    public void quitGame() {
        if (gameRoom == null)
            return;

        gameRoom.onQuit(this);
        gameRoom = null;

        updateGUI();
    }

    /**
     * 플레이어를 자유 전투에 입장시킨다.
     */
    public void startFreeCombat() {
        if (isInFreeCombat || GameUser.fromUser(this) != null)
            return;

        isInFreeCombat = true;
        FreeCombat.getInstance().onStart(this);
    }

    /**
     * 플레이어를 자유 전투에서 퇴장시킨다.
     */
    public void quitFreeCombat() {
        if (!isInFreeCombat)
            return;

        isInFreeCombat = false;

        CombatUser combatUser = CombatUser.fromUser(this);
        if (combatUser != null)
            combatUser.remove();
    }

    /**
     * 플레이어의 스킨을 변경한다.
     *
     * @param skinName 적용할 스킨 이름
     */
    @NonNull
    public AsyncTask<Void> applySkin(@NonNull String skinName) {
        return new AsyncTask<>((onFinish, onError) -> {
            try {
                DMGR.getSkinsRestorerAPI().applySkin(new PlayerWrapper(player), skinName);
                onFinish.accept(null);
            } catch (Exception ex) {
                ConsoleLogger.severe("{0}의 스킨 적용 실패", ex, player.getName());
                onError.accept(ex);
            }
        });
    }

    /**
     * 플레이어의 스킨을 초기화한다.
     */
    @NonNull
    public AsyncTask<Void> resetSkin() {
        return new AsyncTask<>((onFinish, onError) -> {
            try {
                DMGR.getSkinsRestorerAPI().applySkin(new PlayerWrapper(player), player.getName());
                onFinish.accept(null);
            } catch (Exception ex) {
                ConsoleLogger.severe("{0}의 스킨 초기화 실패", ex, player.getName());
                onError.accept(ex);
            }
        });
    }

    /**
     * 메뉴 아이템 목록.
     */
    private enum MenuItem {
        TEAM_GAME("NzYxODQ2MTBjNTBjMmVmYjcyODViYzJkMjBmMzk0MzY0ZTgzNjdiYjMxNDg0MWMyMzhhNmE1MjFhMWVlMTJiZiJ9fX0=",
                "팀전 (일반/랭크)", "전장에서 다른 플레이어들과 팀을 맺어 전투하고 보상을 획득합니다.", 13, SelectGame::new),
        TEAM_GAME_EXIT("YzEwNTkxZTY5MDllNmEyODFiMzcxODM2ZTQ2MmQ2N2EyYzc4ZmEwOTUyZTkxMGYzMmI0MWEyNmM0OGMxNzU3YyJ9fX0=",
                "§c§l나가기", "현재 입장한 게임에서 나갑니다." +
                "\n" +
                "\n§c경고: 게임 진행 중 나가면 탈주 처리되며, 랭크 게임은 패널티가 적용됩니다.", 13,
                target -> {
                    User.fromPlayer(target).quitGame();
                    target.closeInventory();
                }),
        FREE_GAME("NTBkZmM4YTM1NjNiZjk5NmY1YzFiNzRiMGIwMTViMmNjZWIyZDA0Zjk0YmJjZGFmYjIyOTlkOGE1OTc5ZmFjMSJ9fX0=",
                "자유 전투", "전장에서 다른 플레이어들과 자유롭게 전투합니다.", 14,
                target -> User.fromPlayer(target).startFreeCombat()),
        TRAINING("NzNjM2E5YmRjOGM0MGM0MmQ4NDFkYWViNzFlYTllN2QxYzU0YWIzMWEyM2EyZDkyNjU5MWQ1NTUxNDExN2U1ZCJ9fX0=",
                "훈련장", "훈련장에서 다양한 전투원을 체험하고 전투 기술을 훈련합니다.", 15, target -> {
        }),
        LOBBY("OTNiZjJmYzY5M2IxNmNiOTFiOGM4N2E0YjA4OWZkOWUxODI1ZmNhMDFjZWZiMTY1YzYxODdmYzUzOWIxNTJjOSJ9fX0=",
                "로비", "로비로 이동합니다.", 17,
                target -> {
                    target.performCommand("exit");
                    target.closeInventory();
                });

        /** 인벤토리 칸 번호 */
        private final int slotIndex;
        /** GUI 아이템 */
        private final DefinedItem definedItem;

        MenuItem(String skinUrl, String name, String lore, int slotIndex, Consumer<Player> action) {
            this.slotIndex = slotIndex;
            this.definedItem = new DefinedItem(new ItemBuilder(PlayerSkullUtil.fromURL(skinUrl))
                    .setName("§e§l" + name)
                    .setLore("§f" + lore)
                    .build(),
                    (clickType, target) -> {
                        if (clickType != ClickType.LEFT)
                            return false;

                        action.accept(target);
                        return true;
                    });
        }
    }

    /**
     * 발광 효과 정보 클래스.
     */
    private static final class GlowingInfo {
        /** 발광 효과의 팀 패킷 이름 */
        private static final String GLOWING_TEAM_PACKET_NAME = "Glowing";

        /** 플레이어 */
        private final Player player;
        /** 발광 효과 적용 엔티티 */
        private final Entity target;
        /** 틱 작업을 처리하는 태스크 */
        private final IntervalTask onTickTask;
        /** 색상 */
        private ChatColor color;
        /** 종료 시점 */
        private Timestamp expiration = Timestamp.now();

        private GlowingInfo(@NonNull User user, @NonNull Entity target) {
            this.player = user.getPlayer();
            this.target = target;
            this.onTickTask = new IntervalTask(i -> {
                if (!target.isValid() || expiration.isBefore(Timestamp.now()))
                    return false;

                if (i % 4 == 0)
                    sendGlowingPacket(true);

                return true;
            }, () -> user.removeGlowing(target), 1);
        }

        /**
         * 플레이어에게 엔티티를 지속시간동안 발광 상태로 표시한다.
         *
         * @param color    색상
         * @param duration 지속시간
         */
        private void setGlowing(@NonNull ChatColor color, @NonNull Timespan duration) {
            this.color = color;

            sendRemoveTeamPacket();
            sendAddTeamPacket();

            if (expiration.isBefore(Timestamp.now()) || duration.compareTo(Timestamp.now().until(expiration)) > 0)
                expiration = Timestamp.now().plus(duration);
        }

        /**
         * 엔티티의 발광 효과를 제거한다.
         */
        public void removeGlowing() {
            sendRemoveTeamPacket();
            sendGlowingPacket(false);

            onTickTask.stop();
        }

        /**
         * 플레이어에게 발광 효과 패킷을 전송한다.
         *
         * @param isEnabled 활성화 여부
         */
        private void sendGlowingPacket(boolean isEnabled) {
            WrapperPlayServerEntityMetadata packet = new WrapperPlayServerEntityMetadata();

            packet.setEntityID(target.getEntityId());
            WrappedDataWatcher dw = WrappedDataWatcher.getEntityWatcher(target).deepClone();
            dw.setObject(0, (byte) (isEnabled ? dw.getByte(0) | 1 << 6 : dw.getByte(0) & ~(1 << 6)));
            packet.setMetadata(dw.getWatchableObjects());

            packet.sendPacket(player);
        }

        /**
         * 플레이어에게 팀 추가 패킷을 전송한다.
         */
        private void sendAddTeamPacket() {
            WrapperPlayServerScoreboardTeam packet1 = new WrapperPlayServerScoreboardTeam();
            WrapperPlayServerScoreboardTeam packet2 = new WrapperPlayServerScoreboardTeam();
            WrapperPlayServerScoreboardTeam packet3 = new WrapperPlayServerScoreboardTeam();

            packet1.setName(GLOWING_TEAM_PACKET_NAME + color.ordinal());
            packet2.setName(GLOWING_TEAM_PACKET_NAME + color.ordinal());
            packet3.setName(GLOWING_TEAM_PACKET_NAME + color.ordinal());

            packet1.setMode(0);

            packet2.setMode(2);
            packet2.setNameTagVisibility("never");
            packet2.setCollisionRule("never");
            packet2.setPrefix(color + "");
            packet2.setColor(color.ordinal());

            packet3.setMode(3);
            packet3.setPlayers(Collections.singletonList(target instanceof Player ? target.getName() : target.getUniqueId().toString()));

            packet1.sendPacket(player);
            packet2.sendPacket(player);
            packet3.sendPacket(player);
        }

        /**
         * 플레이어에게 팀 제거 패킷을 전송한다.
         */
        private void sendRemoveTeamPacket() {
            WrapperPlayServerScoreboardTeam packet = new WrapperPlayServerScoreboardTeam();

            packet.setMode(4);
            packet.setName(GLOWING_TEAM_PACKET_NAME + color.ordinal());
            packet.setPlayers(Collections.singletonList(target instanceof Player ? target.getName() : target.getUniqueId().toString()));

            packet.sendPacket(player);
        }
    }

    /**
     * 플레이어의 탭리스트 상태를 관리하는 클래스.
     */
    public static final class TabListManager {
        /** 탭리스트의 공백 항목 */
        private static final TextTabItem BLANK_TAB_ITEM = new TextTabItem("", -1);
        /** 탭리스트 인스턴스 */
        private final TableTabList tabList;

        private TabListManager(Player player) {
            TabList tableTabList = DMGR.getTabbed().getTabList(player);
            this.tabList = tableTabList == null ? DMGR.getTabbed().newTableTabList(player) : (TableTabList) tableTabList;

            this.tabList.setBatchEnabled(true);
            clearItems();
        }

        private static void validateColumnRow(int column, int row) {
            Validate.inclusiveBetween(0, 3, column, "3 >= column >= 0 (%d)", column);
            Validate.inclusiveBetween(0, 19, row, "19 >= row >= 0 (%d)", row);
        }

        /**
         * 탭리스트 헤더(상단부)의 내용을 지정한다.
         *
         * @param content 내용
         */
        public void setHeader(@NonNull String content) {
            tabList.setHeader(content);
        }

        /**
         * 탭리스트 푸터(하단부)의 내용을 지정한다.
         *
         * @param content 내용
         */
        public void setFooter(@NonNull String content) {
            tabList.setFooter(content);
        }

        /**
         * 지정한 번호의 항목을 설정한다.
         *
         * @param column  열 번호. 0~3 사이의 값
         * @param row     행 번호. 0~19 사이의 값
         * @param content 내용
         * @param skin    머리 스킨. {@code null}로 지정 시 머리 스킨 표시 안 함
         * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
         */
        public void setItem(int column, int row, @NonNull String content, @Nullable Skin skin) {
            validateColumnRow(column, row);

            tabList.set(column, row, skin == null
                    ? new TextTabItem(content, -1)
                    : new TextTabItem(content, -1, skin));
        }

        /**
         * 지정한 번호의 항목을 설정한다.
         *
         * @param column  열 번호. 0~3 사이의 값
         * @param row     행 번호. 0~19 사이의 값
         * @param content 내용
         * @param user    표시할 플레이어
         * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
         */
        public void setItem(int column, int row, @NonNull String content, @NonNull User user) {
            validateColumnRow(column, row);

            int realPing;
            if (user.ping < 50)
                realPing = 0;
            else if (user.ping < 70)
                realPing = 150;
            else if (user.ping < 100)
                realPing = 300;
            else if (user.ping < 130)
                realPing = 600;
            else
                realPing = 1000;

            tabList.set(column, row, new TextTabItem(content, realPing, Skins.getPlayer(user.getPlayer())));
        }

        /**
         * 지정한 번호의 항목을 제거한다.
         *
         * @param column 열 번호. 0~3 사이의 값
         * @param row    행 번호. 0~19 사이의 값
         * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
         */
        public void removeItem(int column, int row) {
            validateColumnRow(column, row);
            tabList.set(column, row, BLANK_TAB_ITEM);
        }

        /**
         * 탭리스트의 모든 항목을 제거한다.
         */
        public void clearItems() {
            for (int i = 0; i < 80; i++)
                tabList.set(i, BLANK_TAB_ITEM);
        }

        /**
         * 탭리스트 변경 사항을 업데이트한다.
         *
         * <p>탭리스트 내용 변경 후 호출해야 한다.</p>
         */
        private void update() {
            tabList.batchUpdate();
        }

        /**
         * 탭리스트를 제거한다.
         */
        private void delete() {
            tabList.disable();
        }
    }

    /**
     * 플레이어의 사이드바 상태를 관리하는 클래스.
     */
    public static final class SidebarManager {
        /** 사이드바 인스턴스 */
        private final BPlayerBoard sidebar;

        private SidebarManager(Player player) {
            this.sidebar = new BPlayerBoard(player, "");
            clear();
        }

        /**
         * 사이드바의 이름을 지정한다.
         *
         * @param name 사이드바 이름
         */
        public void setName(@NonNull String name) {
            sidebar.setName(name);
        }

        /**
         * 사이드바의 내용을 설정한다.
         *
         * @param line    줄 번호. 0~14 사이의 값
         * @param content 내용
         * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
         */
        public void set(int line, @NonNull String content) {
            Validate.inclusiveBetween(0, 14, line, "14 >= line >= 0 (%d)", line);

            ChatColor[] chatColors = ChatColor.values();
            sidebar.set(chatColors[line] + content, 14 - line);
        }

        /**
         * 사이드바의 전체 내용을 설정한다.
         *
         * @param contents 내용 목록
         * @throws IllegalArgumentException {@code contents}의 길이가 15를 초과하면 발생
         */
        public void setAll(@NonNull String @NonNull ... contents) {
            Validate.inclusiveBetween(0, 15, contents.length, "contents.length <= 15 (%d)", contents.length);

            for (int i = 0; i < contents.length; i++)
                set(i, contents[i]);
        }

        /**
         * 사이드바의 내용을 초기화한다.
         */
        public void clear() {
            sidebar.clear();
        }

        /**
         * 사이드바를 제거한다.
         */
        private void delete() {
            sidebar.delete();
        }
    }
}
