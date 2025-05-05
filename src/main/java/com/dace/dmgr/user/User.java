package com.dace.dmgr.user;

import com.dace.dmgr.*;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.effect.SoundEffect;
import com.dace.dmgr.effect.TextHologram;
import com.dace.dmgr.event.listener.OnAsyncPlayerChat;
import com.dace.dmgr.event.listener.OnPlayerCommandPreprocess;
import com.dace.dmgr.event.listener.OnPlayerQuit;
import com.dace.dmgr.event.listener.OnPlayerResourcePackStatus;
import com.dace.dmgr.game.GameRoom;
import com.dace.dmgr.game.GameUser;
import com.dace.dmgr.game.SelectGame;
import com.dace.dmgr.item.DefinedItem;
import com.dace.dmgr.item.GUI;
import com.dace.dmgr.item.ItemBuilder;
import com.dace.dmgr.util.EntityUtil;
import com.dace.dmgr.util.StringFormUtil;
import com.dace.dmgr.util.task.DelayTask;
import com.dace.dmgr.util.task.IntervalTask;
import com.dace.dmgr.util.task.TaskManager;
import com.keenant.tabbed.Tabbed;
import com.keenant.tabbed.tablist.TableTabList;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.lang3.Validate;
import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.ArmorStand;
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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
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
            "§c유저 데이터를 불러오는 중 오류가 발생했습니다.",
            "",
            "§f잠시 후 다시 시도하거나, 관리자에게 문의하십시오.",
            "",
            "§7오류 문의 : {0}");
    /** 리소스팩 미적용으로 강제퇴장 시 표시되는 메시지 */
    private static final String MESSAGE_KICK_DENY = String.join("\n",
            "§c리소스팩 적용을 활성화 하십시오.",
            "",
            "§e멀티플레이 → 편집 → 서버 리소스 팩 : 사용",
            "",
            "§f다운로드가 되지 않으면, §n.minecraft → server-resource-packs§f 폴더를 생성하십시오.",
            "",
            "§7다운로드 오류 문의 : {0}");
    /** 리소스팩 적용 중 오류로 강제퇴장 시 표시되는 메시지 */
    private static final String MESSAGE_KICK_RESOURCE_ERR = String.join("\n",
            "§c리소스팩 적용 중 오류가 발생했습니다.",
            "",
            "§f§n.minecraft → server-resource-packs§f 폴더의 내용물을 전부 삭제한 뒤 재접속 하시기 바랍니다.",
            "",
            "§e문제가 해결되지 않으면 문의 바랍니다.",
            "",
            "§7다운로드 오류 문의 : {0}");
    /** 채팅의 메시지 포맷 패턴 */
    private static final String CHAT_FORMAT_PATTERN = "<{0}> {1}";
    /** Tabbed 인스턴스 */
    private static final Tabbed TABBED = new Tabbed(DMGR.getPlugin());
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
    /** 이름표 숨기기용 갑옷 거치대 인스턴스 */
    private final ArmorStand nameTagHider;
    /** 탭리스트 인스턴스 */
    private final TableTabList tabList;
    /** 로비 탭리스트 프로필 */
    private final LobbyTabListProfile lobbyTabListProfile;
    /** 유저 데이터 정보 인스턴스 */
    @NonNull
    @Getter
    private final UserData userData;
    /** 태스크 관리 인스턴스 */
    private final TaskManager taskManager = new TaskManager();
    /** 플레이어 사이드바 관리 인스턴스 */
    @NonNull
    @Getter
    private final SidebarManager sidebarManager;
    /** 발광 효과 관리 인스턴스 */
    @NonNull
    @Getter
    private final GlowingManager glowingManager;
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

    /** 탭리스트 프로필 */
    @Setter
    private TabListProfile tabListProfile;
    /** 이름표 홀로그램 */
    @Nullable
    private TextHologram nameTagHologram;
    /** 현재 핑 (ms) */
    @Getter
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
    /** 현재 장소 */
    @NonNull
    @Getter
    private Place currentPlace = Place.LOBBY;
    /** 관리자 채팅 여부 */
    @Getter
    @Setter
    private boolean isAdminChat = false;

    /**
     * 유저 인스턴스를 생성한다.
     *
     * @param player 대상 플레이어
     */
    private User(@NonNull Player player) {
        this.player = player;
        this.nameTagHider = EntityUtil.createTemporaryArmorStand(player.getLocation());
        this.tabList = TABBED.newTableTabList(player);
        this.tabList.setBatchEnabled(true);
        this.userData = UserData.fromPlayer(player);
        this.lobbyTabListProfile = new LobbyTabListProfile(this);
        this.tabListProfile = lobbyTabListProfile;
        this.sidebarManager = new SidebarManager(this);
        this.glowingManager = new GlowingManager(this);
        this.gui = new GUI(player.getInventory());

        USER_MAP.put(player, this);

        init();
    }

    /**
     * 지정한 플레이어의 유저 정보 인스턴스를 반환한다.
     *
     * @param player 대상 플레이어
     * @return 유저 인스턴스
     * @throws IllegalStateException 해당 {@code player}가 Citizens NPC이면 발생
     */
    @NonNull
    public static User fromPlayer(@NonNull Player player) {
        Validate.validState(!EntityUtil.isCitizensNPC(player), "Citizens NPC는 User 인스턴스를 생성할 수 없음");

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
        setCurrentPlace(Place.LOBBY);
        taskManager.add(new DelayTask(this::clearChat, 10));

        if (userData.isInitialized())
            onInit();
        else
            taskManager.add(userData.init()
                    .onFinish(this::onInit)
                    .onError(ex -> taskManager.add(new DelayTask(() ->
                            kick(MessageFormat.format(MESSAGE_KICK_ERR, GeneralConfig.getConfig().getAdminContact())), 60))));
    }

    /**
     * 유저 초기화 완료 시 실행할 작업.
     */
    private void onInit() {
        disableCollision();
        taskManager.add(new DelayTask(this::sendResourcePack, 10));
        taskManager.add(PlayerSkin.fromUUID(player.getUniqueId()).applySkin(player));

        nameTagHologram = new TextHologram(player, target -> target != player && CombatUser.fromUser(this) == null,
                0, userData.getDisplayName());

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
     * 플레이어에게 리소스팩을 전송하고, 적용하지 않을 시 강제 퇴장 시킨다.
     */
    private void sendResourcePack() {
        player.setResourcePack(GeneralConfig.getConfig().getResourcePackUrl());

        taskManager.add(new DelayTask(() -> {
            if (!isResourcePackAccepted)
                kick(MessageFormat.format(MESSAGE_KICK_DENY, GeneralConfig.getConfig().getAdminContact()));
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
            kick(MessageFormat.format(MESSAGE_KICK_RESOURCE_ERR, GeneralConfig.getConfig().getAdminContact()));
    }

    /**
     * 매 초마다 실행할 작업.
     */
    private void onSecond() {
        if (userData.getConfig().isNightVision())
            player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0, false, false));
        else
            player.removePotionEffect(PotionEffectType.NIGHT_VISION);

        if (!player.getPassengers().contains(nameTagHider))
            player.addPassenger(nameTagHider);

        nameTagHider.setVisible(true);
        nameTagHider.setVisible(false);

        if (CombatUser.fromUser(this) == null) {
            sendActionBar("§1메뉴를 사용하려면 §nF키§1를 누르십시오.");
            updateLobbySidebar();
        }

        updateTabList();
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
     * 탭리스트 변경 사항을 업데이트한다.
     */
    private void updateTabList() {
        tabList.setHeader(tabListProfile.getHeader());
        tabList.setFooter(tabListProfile.getFooter());

        TabListProfile.Item[][] items = new TabListProfile.Item[4][20];
        tabListProfile.updateItems(items);

        for (int i = 0; i < items.length; i++)
            for (int j = 0; j < items[i].length; j++) {
                TabListProfile.Item item = items[i][j];
                tabList.set(i, j, (item == null ? TabListProfile.Item.BLANK_ITEM : item).getTextTabItem());
            }

        tabList.batchUpdate();
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
                    gameUser.broadcastChatMessage(message);
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
        setCurrentPlace(Place.LOBBY);
        quitGame();

        nameTagHider.remove();
        taskManager.stop();
        sidebarManager.delete();
        glowingManager.clearGlowing();

        if (userData.isInitialized()) {
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
        sendMessageInfo(MessageFormat.format(message, arguments));
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
        message = message.replace("§r", "§c").replace("\n", "\n" + GeneralConfig.getConfig().getMessagePrefix());
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
        sendMessageWarn(MessageFormat.format(message, arguments));
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
     * 플레이어를 서버에서 강제 퇴장 시킨다.
     *
     * @param reason 사유 메시지
     */
    public void kick(@NonNull String reason) {
        player.kickPlayer("\n" + GeneralConfig.getConfig().getMessagePrefix() + reason);
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

        InventoryMenuItem.updateGUI(this);
    }

    /**
     * 플레이어를 현재 입장한 게임 방에서 퇴장시킨다.
     */
    public void quitGame() {
        if (gameRoom == null)
            return;

        gameRoom.onQuit(this);
        gameRoom = null;

        InventoryMenuItem.updateGUI(this);
    }

    /**
     * 플레이어의 체력, 이동속도 등의 모든 상태를 재설정한다.
     */
    private void reset() {
        player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20);
        player.setHealth(20);
        player.setExp(0);
        player.setLevel(0);
        player.setWalkSpeed(0.2F);
        player.getActivePotionEffects().forEach(potionEffect -> player.removePotionEffect(potionEffect.getType()));

        gui.clear();
        InventoryMenuItem.updateGUI(this);
        tabListProfile = lobbyTabListProfile;

        getAllUsers().forEach(target -> {
            glowingManager.removeGlowing(target.getPlayer());
            target.getGlowingManager().removeGlowing(player);
        });

        if (userData.isInitialized())
            sidebarManager.clear();

        CombatUser combatUser = CombatUser.fromUser(this);
        if (combatUser != null)
            combatUser.remove();
    }

    /**
     * 플레이어를 지정한 장소로 이동시키고 상태를 재설정한다.
     *
     * @param place 이동할 장소
     */
    public void setCurrentPlace(@NonNull Place place) {
        if (currentPlace == place && place != Place.LOBBY || GameUser.fromUser(this) != null)
            return;

        currentPlace = place;
        place.onWarp(this);

        reset();
    }

    /**
     * 인벤토리 메뉴 아이템 목록.
     */
    private enum InventoryMenuItem {
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
                target -> User.fromPlayer(target).setCurrentPlace(Place.FREE_COMBAT)),
        TRAINING("NzNjM2E5YmRjOGM0MGM0MmQ4NDFkYWViNzFlYTllN2QxYzU0YWIzMWEyM2EyZDkyNjU5MWQ1NTUxNDExN2U1ZCJ9fX0=",
                "훈련장", "훈련장에서 다양한 전투원을 체험하고 전투 기술을 훈련합니다.", 15,
                target -> User.fromPlayer(target).setCurrentPlace(Place.TRAINING_CENTER)),
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

        InventoryMenuItem(String skinUrl, String name, String lore, int slotIndex, Consumer<Player> action) {
            this.slotIndex = slotIndex;
            this.definedItem = new DefinedItem(new ItemBuilder(PlayerSkin.fromURL(skinUrl))
                    .setName("§e§l" + name)
                    .setLore("§f" + lore)
                    .build(),
                    new DefinedItem.ClickHandler(ClickType.LEFT, target -> {
                        action.accept(target);
                        return true;
                    }));
        }

        /**
         * 인벤토리 메뉴 GUI를 업데이트한다.
         *
         * @param user 대상 유저
         */
        private static void updateGUI(@NonNull User user) {
            for (InventoryMenuItem inventoryMenuItem : values())
                if (inventoryMenuItem != InventoryMenuItem.TEAM_GAME && user.getGameRoom() != null
                        || inventoryMenuItem != InventoryMenuItem.TEAM_GAME_EXIT && user.getGameRoom() == null)
                    user.getGui().set(inventoryMenuItem.slotIndex, inventoryMenuItem.definedItem);

            user.getPlayer().updateInventory();
        }
    }
}
