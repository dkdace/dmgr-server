package com.dace.dmgr.user;

import com.comphenix.packetwrapper.WrapperPlayServerBoss;
import com.comphenix.packetwrapper.WrapperPlayServerEntityMetadata;
import com.comphenix.packetwrapper.WrapperPlayServerScoreboardTeam;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.dace.dmgr.DMGR;
import com.dace.dmgr.Disposable;
import com.dace.dmgr.GeneralConfig;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.game.Game;
import com.dace.dmgr.game.GameUser;
import com.dace.dmgr.util.*;
import com.dace.dmgr.util.task.DelayTask;
import com.dace.dmgr.util.task.IntervalTask;
import com.dace.dmgr.util.task.TaskUtil;
import com.keenant.tabbed.item.TextTabItem;
import com.keenant.tabbed.tablist.TableTabList;
import com.keenant.tabbed.util.Skin;
import com.keenant.tabbed.util.Skins;
import fr.minuskube.netherboard.bukkit.BPlayerBoard;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.boss.BarColor;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.permissions.ServerOperator;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.Nullable;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.UUID;
import java.util.function.LongConsumer;

/**
 * 유저 정보 및 상태를 관리하는 클래스.
 *
 * @see UserData
 */
public final class User implements Disposable {
    /** 타자기 효과 타이틀 쿨타임 ID */
    private static final String TYPEWRITER_TITLE_COOLDOWN_ID = "TypewriterTitle";
    /** 타이틀 쿨타임 ID */
    private static final String TITLE_COOLDOWN_ID = "Title";
    /** 액션바 쿨타임 ID */
    private static final String ACTION_BAR_COOLDOWN_ID = "ActionBar";
    /** 발광 효과 쿨타임 ID */
    private static final String GLOW_COOLDOWN_ID = "Glow";
    /** 오류 발생으로 강제퇴장 시 표시되는 메시지 */
    private static final String MESSAGE_KICK_ERR = "§c유저 데이터를 불러오는 중 오류가 발생했습니다." +
            "\n" +
            "\n§f잠시 후 다시 시도하거나, 관리자에게 문의하십시오." +
            "\n" +
            "\n§7오류 문의 : {0}";
    /** 리소스팩 미적용으로 강제퇴장 시 표시되는 메시지 */
    private static final String MESSAGE_KICK_DENY = "§c리소스팩 적용을 활성화 하십시오." +
            "\n" +
            "\n§e멀티플레이 → 편집 → 서버 리소스 팩 : 사용" +
            "\n" +
            "\n§f다운로드가 되지 않으면, .minecraft → server-resource-packs 폴더를 생성하십시오." +
            "\n" +
            "\n§7다운로드 오류 문의 : {0}";
    /** 레벨 업 효과음 */
    private static final DefinedSound LEVEL_UP_SOUND = new DefinedSound(
            new DefinedSound.SoundEffect("random.good", 1000, 1));
    /** 티어 승급 효과음 */
    private static final DefinedSound TIER_UP_SOUND = new DefinedSound(
            new DefinedSound.SoundEffect(Sound.UI_TOAST_CHALLENGE_COMPLETE, 1000, 1.5));
    /** 티어 강등 효과음 */
    private static final DefinedSound TIER_DOWN_SOUND = new DefinedSound(
            new DefinedSound.SoundEffect(Sound.ENTITY_BLAZE_DEATH, 1000, 0.5));
    /** 경고 액션바 효과음 */
    private static final DefinedSound ALERT_SOUND = new DefinedSound(
            new DefinedSound.SoundEffect("new.block.note_block.bit", 0.25, 0.7));
    /** 타자기 효과 타이틀 효과음 */
    private static final DefinedSound TYPEWRITER_TITLE_SOUND = new DefinedSound(
            new DefinedSound.SoundEffect("new.block.note_block.bass", 1, 1.5));

    /** 플레이어 객체 */
    @NonNull
    @Getter
    private final Player player;
    /** 유저 데이터 정보 객체 */
    @NonNull
    @Getter
    private final UserData userData;
    /** 생성된 보스바 UUID 목록 (보스바 ID : UUID) */
    private final HashMap<String, UUID> bossBarMap = new HashMap<>();
    /** 발광 효과 적용 엔티티 목록 (발광 엔티티 : 색상) */
    private final HashMap<Entity, ChatColor> glowingMap = new HashMap<>();
    /** 이름표 숨기기용 갑옷 거치대 객체 */
    @Nullable
    private ArmorStand nameTagHider;
    /** 이름표 홀로그램 */
    @Nullable
    private TextHologram nameTagHologram;
    /** 플레이어 사이드바 */
    @Nullable
    private BPlayerBoard sidebar;
    /** 플레이어 탭리스트 */
    @Nullable
    private TableTabList tabList;
    /** 현재 핑 (ms) */
    @Getter
    @Setter
    private int ping = 0;
    /** 리소스팩 적용 수락 여부 */
    @Setter
    private boolean isResourcePackAccepted = false;
    /** 현재 귓속말 대상 */
    @Nullable
    @Getter
    @Setter
    private User messageTarget;
    /** 관리자 채팅 여부 */
    @Getter
    @Setter
    private boolean isAdminChat = false;
    /** 자유 전투 입장 여부 */
    @Getter
    @Setter
    private boolean isInFreeCombat = false;

    /**
     * 유저 인스턴스를 생성한다.
     *
     * @param player 대상 플레이어
     */
    private User(@NonNull Player player) {
        this.player = player;
        this.userData = UserData.fromPlayer(player);

        UserRegistry.getInstance().add(player, this);
    }

    /**
     * 지정한 플레이어의 유저 정보 인스턴스를 반환한다.
     *
     * @param player 대상 플레이어
     * @return 유저 인스턴스
     */
    @NonNull
    public static User fromPlayer(@NonNull Player player) {
        User user = UserRegistry.getInstance().get(player);
        if (user == null)
            user = new User(player);

        return user;
    }

    /**
     * 유저 초기화 작업을 수행한다.
     */
    public void init() {
        validate();

        if (userData.isInitialized())
            onInit();
        else
            TaskUtil.addTask(this, userData.init()
                    .onFinish(this::onInit)
                    .onError(ex -> TaskUtil.addTask(User.this, new DelayTask(() ->
                            player.kickPlayer(GeneralConfig.getConfig().getMessagePrefix()
                                    + MessageFormat.format(MESSAGE_KICK_ERR, GeneralConfig.getConfig().getAdminContact())), 60))));
    }

    /**
     * 유저 초기화 완료 시 실행할 작업.
     */
    private void onInit() {
        disableCollision();
        TaskUtil.addTask(this, new DelayTask(this::updateNameTagHider, 1));
        TaskUtil.addTask(this, new DelayTask(this::sendResourcePack, 10));
        TaskUtil.addTask(this, SkinUtil.resetSkin(player));

        sidebar = new BPlayerBoard(player, "lobby");
        clearSidebar();
        tabList = (TableTabList) DMGR.getTabbed().getTabList(player);
        if (tabList == null)
            tabList = DMGR.getTabbed().newTableTabList(player);
        tabList.setBatchEnabled(true);
        clearTabListItems();

        nameTagHologram = new TextHologram(player, target -> target != player && CombatUser.fromUser(this) == null,
                0, userData.getDisplayName());

        if (!userData.getConfig().isKoreanChat())
            player.performCommand("kakc chmod 0");

        TaskUtil.addTask(this, new IntervalTask((LongConsumer) i -> onSecond(), 20));
    }

    /**
     * 유저를 제거하고 등록 해제한다.
     *
     * <p>플레이어 퇴장 시 호출해야 한다.</p>
     */
    public void dispose() {
        validate();

        reset();
        TaskUtil.clearTask(this);

        GameUser gameUser = GameUser.fromUser(this);
        if (gameUser != null)
            gameUser.dispose();
        UserRegistry.getInstance().remove(player);

        if (userData.isInitialized()) {
            if (sidebar != null) {
                sidebar.delete();
                sidebar = null;
            }
            if (tabList != null) {
                tabList.disable();
                tabList = null;
            }
            if (nameTagHider != null) {
                nameTagHider.remove();
                nameTagHider = null;
            }
            if (nameTagHologram != null) {
                nameTagHologram.dispose();
                nameTagHologram = null;
            }

            userData.save();
        }
    }

    @Override
    public boolean isDisposed() {
        return !player.isOnline();
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
     * 이름표 숨기기 객체를 업데이트한다.
     */
    private void updateNameTagHider() {
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

        TaskUtil.addTask(this, new DelayTask(() -> {
            if (!isResourcePackAccepted)
                player.kickPlayer(GeneralConfig.getConfig().getMessagePrefix()
                        + MessageFormat.format(MESSAGE_KICK_DENY, GeneralConfig.getConfig().getAdminContact()));
        }, GeneralConfig.getConfig().getResourcePackTimeout() * 20L));
    }

    /**
     * 매 초마다 실행할 작업.
     */
    private void onSecond() {
        if (userData.getConfig().isNightVision())
            player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0, false, false));
        else
            player.removePotionEffect(PotionEffectType.NIGHT_VISION);

        CombatUser combatUser = CombatUser.fromUser(this);
        if (combatUser == null) {
            sendActionBar("§1메뉴를 사용하려면 §nF키§1를 누르십시오.");
            updateSidebar();
        }

        GameUser gameUser = GameUser.fromUser(this);
        if (gameUser == null || gameUser.getGame().getPhase() == Game.Phase.WAITING)
            updateTablist();
    }

    /**
     * 로비 사이드바를 업데이트한다.
     */
    private void updateSidebar() {
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

        setSidebarName("§b§n" + player.getName());
        editSidebar(
                "§f",
                "§e보유 중인 돈",
                "§6" + String.format("%,d", userData.getMoney()),
                "§f§f",
                "§f레벨 : " + userData.getLevelPrefix(),
                MessageFormat.format("{0} §2[{1}/{2}]",
                        StringFormUtil.getProgressBar(userData.getXp(), reqXp, ChatColor.DARK_GREEN), userData.getXp(), reqXp),
                "§f§f§f",
                "§f랭크 : " + userData.getTier().getPrefix(),
                MessageFormat.format("{0} §3[{1}/{2}]",
                        StringFormUtil.getProgressBar(rank - curRank, reqRank - curRank, ChatColor.DARK_AQUA), rank, reqRank)
        );
    }

    /**
     * 로비 탭리스트를 업데이트한다.
     */
    private void updateTablist() {
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

        setTabListHeader("\n" + GeneralConfig.getConfig().getMessagePrefix() + "§e스킬 PVP 미니게임 서버 §f:: §d§nDMGR.mcsv.kr\n");
        setTabListFooter("\n§7현재 서버는 테스트 단계이며, 시스템 상 문제점이나 버그가 발생할 수 있습니다.\n");

        setTabListItem(0, 0, "§f§l§n 서버 상태 ", Skins.getPlayer("TimmyTimothy"));
        setTabListItem(0, 2, MessageFormat.format("§f PING §7:: {0}{1} ms", pingColor, ping),
                Skins.getPlayer("FranciRoma"));
        setTabListItem(0, 3, MessageFormat.format("§f 메모리 §7:: {0}{1} §f/ {2} (MB)", memoryColor, memory, totalMemory),
                Skins.getPlayer("AddelBurgh"));
        setTabListItem(0, 4, MessageFormat.format("§f TPS §7:: {0}{1} tick/s", tpsColor, tps),
                Skins.getPlayer("CommandBlock"));
        setTabListItem(0, 5, MessageFormat.format("§f 접속자 수 §7:: §f{0}명", Bukkit.getOnlinePlayers().size()),
                Skins.getPlayer("MHF_Steve"));
        setTabListItem(0, 7, MessageFormat.format("§f§n §e§n{0}§f§l§n님의 전적 ", player.getName()),
                Skins.getPlayer(player));
        setTabListItem(0, 9, MessageFormat.format("§e 승률 §7:: §b{0}승 §f/ §c{1}패 §f({2}%)", userData.getWinCount(), userData.getLoseCount(),
                        (double) userData.getWinCount() / (userData.getNormalPlayCount() + userData.getRankPlayCount()) * 100),
                Skins.getPlayer("goldblock"));
        setTabListItem(0, 10, MessageFormat.format("§e 탈주 §7:: §c{0}회 §f({1}%)", userData.getQuitCount(),
                        (double) userData.getQuitCount() / (userData.getNormalPlayCount() + userData.getRankPlayCount()) * 100),
                Skins.getPlayer("MHF_TNT2"));
        setTabListItem(0, 11, MessageFormat.format("§e 플레이 시간 §7:: §f{0}",
                        DurationFormatUtils.formatDuration(userData.getPlayTime() * 1000L, "d일 H시간 m분")),
                Skins.getPlayer("Olaf_C"));

        User[] lobbyUsers = Bukkit.getOnlinePlayers().stream()
                .filter(target -> !target.isOp())
                .sorted(Comparator.comparing(HumanEntity::getName))
                .map(User::fromPlayer)
                .toArray(User[]::new);
        User[] adminUsers = Bukkit.getOnlinePlayers().stream()
                .filter(ServerOperator::isOp)
                .sorted(Comparator.comparing(HumanEntity::getName))
                .map(User::fromPlayer)
                .toArray(User[]::new);

        for (int i = 0; i < 38; i++) {
            if (i > lobbyUsers.length - 1)
                removeTabListItem(i % 2 == 0 ? 1 : 2, i / 2 + 1);
            else
                setTabListItem(i % 2 == 0 ? 1 : 2, i / 2 + 1, lobbyUsers[i].getTablistPlayerName(), lobbyUsers[i]);
        }
        setTabListItem(1, 0, MessageFormat.format("§a§l§n 접속 인원 §f({0}명)", lobbyUsers.length), Skins.getDot(ChatColor.GREEN));

        for (int i = 0; i < 19; i++) {
            if (i > adminUsers.length - 1)
                removeTabListItem(3, i + 1);
            else
                setTabListItem(3, i + 1, adminUsers[i].getTablistPlayerName(), adminUsers[i]);

            setTabListItem(3, 0, MessageFormat.format("§b§l§n 관리자 §f({0}명)", adminUsers.length), Skins.getDot(ChatColor.AQUA));
        }

        applyTabList();
    }

    /**
     * 탭리스트에 사용되는 플레이어의 이름을 반환한다.
     *
     * @return 이름
     */
    @NonNull
    public String getTablistPlayerName() {
        String prefix = "§7[로비]";

        GameUser gameUser = GameUser.fromUser(this);
        if (isInFreeCombat())
            prefix = "§7[자유 전투]";
        else if (gameUser != null) {
            Game game = gameUser.getGame();

            if (game.getGamePlayMode().isRanked())
                prefix = MessageFormat.format("§6[랭크 게임 {0}]", game.getNumber());
            else
                prefix = MessageFormat.format("§a[일반 게임 {0}]", game.getNumber());
        }

        return MessageFormat.format(" {0} §f{1}", prefix, player.getName());
    }

    /**
     * 레벨 상승 시 효과를 재생한다.
     */
    public void playLevelUpEffect() {
        TaskUtil.addTask(this, new DelayTask(() -> {
            sendTitle(userData.getLevelPrefix() + " §e§l달성!", "", 8, 40, 30, 40);
            LEVEL_UP_SOUND.play(player);
        }, 100));
    }

    /**
     * 티어 승급 시 효과를 재생한다.
     */
    public void playTierUpEffect() {
        TaskUtil.addTask(this, new DelayTask(() -> {
            sendTitle("§b§l등급 상승", userData.getTier().getPrefix(), 8, 40, 30, 40);
            TIER_UP_SOUND.play(player);
        }, 80));
    }

    /**
     * 티어 강등 시 효과를 재생한다.
     */
    public void playTierDownEffect() {
        TaskUtil.addTask(this, new DelayTask(() -> {
            sendTitle("§c§l등급 강등", userData.getTier().getPrefix(), 8, 40, 30, 40);
            TIER_DOWN_SOUND.play(player);
        }, 80));
    }

    /**
     * 플레이어의 체력, 이동속도 등의 모든 상태를 재설정하고 스폰으로 이동시킨다.
     */
    public void reset() {
        validate();

        player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20);
        player.setHealth(20);
        player.getInventory().clear();
        player.setExp(0);
        player.setLevel(0);
        player.setWalkSpeed(0.2F);
        player.getActivePotionEffects().forEach((potionEffect ->
                player.removePotionEffect(potionEffect.getType())));
        Bukkit.getOnlinePlayers().forEach(target -> User.fromPlayer(target).removeGlowing(this.player));

        clearBossBar();
        teleport(LocationUtil.getLobbyLocation());
        isInFreeCombat = false;

        if (userData.isInitialized()) {
            clearSidebar();
            clearTabListItems();
        }

        CombatUser combatUser = CombatUser.fromUser(this);
        if (combatUser != null)
            combatUser.dispose();
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
     * <p>'§r'을 사용하여 기본 색상으로 초기화할 수 있다.</p>
     *
     * <p>Example:</p>
     *
     * <pre><code>
     * // <연두색>Hello, <흰색>World!
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
     * <p>'§r'을 사용하여 기본 색상으로 초기화할 수 있다.</p>
     *
     * <p>지정한 arguments의 n번째 인덱스가 메시지에 포함된 '{n}'을 치환한다.</p>
     *
     * <p>Example:</p>
     *
     * <pre><code>
     * // <연두색>Hello, <흰색>World!
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
     * <p>'§r'을 사용하여 적용된 색상을 초기화할 수 있다.</p>
     *
     * <p>Example:</p>
     *
     * <pre><code>
     * // <연두색>Hello, <빨간색>World!
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
     * <p>'§r'을 사용하여 적용된 색상을 초기화할 수 있다.</p>
     *
     * <p>지정한 arguments의 n번째 인덱스가 메시지에 포함된 '{n}'을 치환한다.</p>
     *
     * <p>Example:</p>
     *
     * <pre><code>
     * // <연두색>Hello, <빨간색>World!
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
     * @param message       메시지
     * @param overrideTicks 덮어쓰기 지속시간 (tick). 0 이상으로 지정하면 지속시간 동안 기존 액션바 출력을 무시함
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    public void sendActionBar(@NonNull String message, long overrideTicks) {
        if (overrideTicks < 0)
            throw new IllegalArgumentException("'overrideTicks'가 0 이상이어야 함");

        if (overrideTicks > 0)
            CooldownUtil.setCooldown(this, ACTION_BAR_COOLDOWN_ID, overrideTicks);
        else if (CooldownUtil.getCooldown(this, ACTION_BAR_COOLDOWN_ID) > 0)
            return;

        TextComponent actionBar = new TextComponent(message);
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, actionBar);
    }

    /**
     * 플레이어에게 액션바를 전송한다.
     *
     * @param message 메시지
     */
    public void sendActionBar(@NonNull String message) {
        sendActionBar(message, 0);
    }

    /**
     * 플레이어에게 경고 액션바를 전송한다.
     *
     * @param message 경고 메시지
     */
    public void sendAlert(@NonNull String message) {
        ALERT_SOUND.play(player);
        TaskUtil.addTask(this, new IntervalTask(i -> {
            ChatColor color = ChatColor.YELLOW;
            if (i == 1)
                color = ChatColor.GOLD;
            else if (i == 2)
                color = ChatColor.RED;

            sendActionBar(color + ChatColor.stripColor(message), 16);
        }, 1, 3));
    }

    /**
     * 플레이어에게 타이틀을 전송한다.
     *
     * @param title         제목
     * @param subtitle      부제목
     * @param fadeIn        나타나는 시간 (tick). 0 이상의 값
     * @param stay          유지 시간 (tick). 0 이상의 값
     * @param fadeOut       사라지는 시간 (tick). 0 이상의 값
     * @param overrideTicks 덮어쓰기 지속시간 (tick). 0 이상으로 지정하면 지속시간 동안 기존 타이틀 출력을 무시함
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    public void sendTitle(@NonNull String title, @NonNull String subtitle, int fadeIn, int stay, int fadeOut, long overrideTicks) {
        if (overrideTicks < 0 || fadeIn < 0 || stay < 0 || fadeOut < 0)
            throw new IllegalArgumentException("'fadeIn', 'stay', 'fadeOut' 및 'overrideTicks'가 0 이상이어야 함");

        if (overrideTicks > 0)
            CooldownUtil.setCooldown(this, TITLE_COOLDOWN_ID, overrideTicks);
        else if (CooldownUtil.getCooldown(this, TITLE_COOLDOWN_ID) > 0)
            return;

        player.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
    }

    /**
     * 플레이어에게 타이틀을 전송한다.
     *
     * @param title    제목
     * @param subtitle 부제목
     * @param fadeIn   나타나는 시간 (tick). 0 이상의 값
     * @param stay     유지 시간 (tick). 0 이상의 값
     * @param fadeOut  사라지는 시간 (tick). 0 이상의 값
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    public void sendTitle(@NonNull String title, @NonNull String subtitle, int fadeIn, int stay, int fadeOut) {
        sendTitle(title, subtitle, fadeIn, stay, fadeOut, 0);
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
        StringBuilder text = new StringBuilder();

        for (int i = 0; i < message.length(); i++) {
            char nextChar = message.charAt(i);

            TaskUtil.addTask(this, new DelayTask(() -> {
                text.append(nextChar);
                CooldownUtil.setCooldown(this, TYPEWRITER_TITLE_COOLDOWN_ID, 50);

                sendTitle("", prefix + " §f" + text, 0, 40, 10);
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
     */
    public boolean isTypewriterTitlePrinting() {
        return CooldownUtil.getCooldown(this, TYPEWRITER_TITLE_COOLDOWN_ID) > 0;
    }

    /**
     * 플레이어의 사이드바 이름을 지정한다.
     *
     * @param name 사이드바 이름
     */
    public void setSidebarName(@NonNull String name) {
        Validate.notNull(sidebar).setName(name);
    }

    /**
     * 플레이어의 사이드바 내용을 업데이트한다.
     *
     * @param line    줄 번호. 0~14 사이의 값
     * @param content 내용
     * @throws IndexOutOfBoundsException {@code line}이 유효 범위를 초과하면 발생
     */
    public void editSidebar(int line, @NonNull String content) {
        Validate.notNull(sidebar);
        if (line < 0 || line > 14)
            throw new IndexOutOfBoundsException("'line'이 0에서 14 사이여야 함");

        ChatColor[] chatColors = ChatColor.values();
        sidebar.set(content.isEmpty() ? String.valueOf(chatColors[line]) : content, 14 - line);
    }

    /**
     * 플레이어의 사이드바 내용을 업데이트한다.
     *
     * @param contents 내용 목록
     * @throws IndexOutOfBoundsException {@code contents}의 길이가 15를 초과하면 발생
     */
    public void editSidebar(@NonNull String @NonNull ... contents) {
        Validate.notNull(sidebar);
        if (contents.length > 15)
            throw new IndexOutOfBoundsException("'contents'의 길이가 16 미만이어야 함");

        sidebar.setAll(contents);
    }

    /**
     * 플레이어의 사이드바 내용을 초기화한다.
     */
    public void clearSidebar() {
        Validate.notNull(sidebar).clear();
    }

    /**
     * 플레이어의 탭리스트 헤더(상단부)의 내용을 지정한다.
     *
     * @param content 내용
     */
    public void setTabListHeader(@NonNull String content) {
        Validate.notNull(tabList).setHeader(content);
    }

    /**
     * 플레이어의 탭리스트 푸터(하단부)의 내용을 지정한다.
     *
     * @param content 내용
     */
    public void setTabListFooter(@NonNull String content) {
        Validate.notNull(tabList).setFooter(content);
    }

    /**
     * 플레이어의 탭리스트에서 지정한 항목을 설정한다.
     *
     * @param column  열 번호. 0~3 사이의 값
     * @param row     행 번호. 0~19 사이의 값
     * @param content 내용
     * @param skin    머리 스킨. {@code null}로 지정 시 머리 스킨 표시 안 함
     * @throws IndexOutOfBoundsException {@code column} 또는 {@code row}가 유효 범위를 초과하면 발생
     */
    public void setTabListItem(int column, int row, @NonNull String content, @Nullable Skin skin) {
        Validate.notNull(tabList);
        validateColumnRow(column, row);

        tabList.set(column, row, skin == null ? new TextTabItem(content, -1) : new TextTabItem(content, -1, skin));
    }

    /**
     * 플레이어의 탭리스트에서 지정한 항목을 설정한다.
     *
     * @param column  열 번호. 0~3 사이의 값
     * @param row     행 번호. 0~19 사이의 값
     * @param content 내용
     * @param user    표시할 플레이어
     * @throws IndexOutOfBoundsException {@code column} 또는 {@code row}가 유효 범위를 초과하면 발생
     */
    public void setTabListItem(int column, int row, @NonNull String content, @NonNull User user) {
        Validate.notNull(tabList);
        validateColumnRow(column, row);

        int realPing;
        if (user.getPing() < 40)
            realPing = 0;
        else if (user.getPing() < 70)
            realPing = 150;
        else if (user.getPing() < 100)
            realPing = 300;
        else if (user.getPing() < 130)
            realPing = 600;
        else
            realPing = 1000;

        tabList.set(column, row, new TextTabItem(content, realPing, Skins.getPlayer(user.getPlayer())));
    }

    /**
     * 플레이어의 탭리스트에서 지정한 항목을 제거한다.
     *
     * @param column 열 번호. 0~3 사이의 값
     * @param row    행 번호. 0~19 사이의 값
     * @throws IndexOutOfBoundsException {@code column} 또는 {@code row}가 유효 범위를 초과하면 발생
     */
    public void removeTabListItem(int column, int row) {
        Validate.notNull(tabList);
        validateColumnRow(column, row);

        tabList.set(column, row, new TextTabItem("", -1));
    }

    /**
     * 플레이어의 탭리스트에서 모든 항목을 제거한다.
     */
    public void clearTabListItems() {
        Validate.notNull(tabList);

        for (int i = 0; i < 80; i++)
            tabList.set(i, new TextTabItem("", -1));
    }

    /**
     * 플레이어의 탭리스트 변경 사항을 적용한다.
     *
     * <p>탭리스트 내용 변경 후 호출해야 한다.</p>
     */
    public void applyTabList() {
        Validate.notNull(tabList);

        tabList.batchUpdate();
    }

    /**
     * 플레이어에게 보스바를 표시한다.
     *
     * <p>이미 해당 ID의 보스바가 존재할 경우 덮어쓴다.</p>
     *
     * @param id       보스바 ID
     * @param message  내용
     * @param color    막대 색
     * @param style    막대 스타일
     * @param progress 진행률. 0~1 사이의 값
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    public void addBossBar(@NonNull String id, @NonNull String message, @NonNull BarColor color, @NonNull WrapperPlayServerBoss.BarStyle style,
                           double progress) {
        if (progress < 0 || progress > 1)
            throw new IllegalArgumentException("'progress'가 0에서 1 사이여야 함");

        UUID uuid = bossBarMap.getOrDefault(id, UUID.randomUUID());

        if (bossBarMap.get(id) == null) {
            WrapperPlayServerBoss packet = new WrapperPlayServerBoss();

            packet.setUniqueId(uuid);
            packet.setAction(WrapperPlayServerBoss.Action.ADD);
            packet.setTitle(WrappedChatComponent.fromText(message));
            packet.setColor(color);
            packet.setStyle(style);
            packet.setHealth((float) progress);

            packet.sendPacket(player);

            bossBarMap.put(id, uuid);
        } else {
            WrapperPlayServerBoss packet1 = new WrapperPlayServerBoss();
            WrapperPlayServerBoss packet2 = new WrapperPlayServerBoss();
            WrapperPlayServerBoss packet3 = new WrapperPlayServerBoss();
            packet1.setUniqueId(uuid);
            packet2.setUniqueId(uuid);
            packet3.setUniqueId(uuid);

            packet1.setAction(WrapperPlayServerBoss.Action.UPDATE_NAME);
            packet1.setTitle(WrappedChatComponent.fromText(message));

            packet2.setAction(WrapperPlayServerBoss.Action.UPDATE_STYLE);
            packet2.setColor(color);
            packet2.setStyle(style);

            packet3.setAction(WrapperPlayServerBoss.Action.UPDATE_PCT);
            packet3.setHealth((float) progress);

            packet1.sendPacket(player);
            packet2.sendPacket(player);
            packet3.sendPacket(player);
        }
    }

    /**
     * 플레이어의 보스바를 제거한다.
     *
     * @param id 보스바 ID
     */
    public void removeBossBar(@NonNull String id) {
        UUID uuid = bossBarMap.get(id);
        if (uuid == null)
            return;

        WrapperPlayServerBoss packet = new WrapperPlayServerBoss();

        packet.setAction(WrapperPlayServerBoss.Action.REMOVE);
        packet.setUniqueId(uuid);

        packet.sendPacket(player);

        bossBarMap.remove(id, uuid);
    }

    /**
     * 플레이어의 모든 보스바를 제거한다.
     */
    public void clearBossBar() {
        bossBarMap.forEach((id, uuid) -> {
            WrapperPlayServerBoss packet = new WrapperPlayServerBoss();

            packet.setAction(WrapperPlayServerBoss.Action.REMOVE);
            packet.setUniqueId(uuid);

            packet.sendPacket(player);
        });

        bossBarMap.clear();
    }

    /**
     * 지정한 엔티티에게 지속시간동안 발광 효과를 적용한다.
     *
     * @param target   발광 효과를 적용할 엔티티
     * @param color    색상
     * @param duration 지속시간 (tick). -1로 설정 시 무한 지속
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    public void setGlowing(@NonNull Entity target, @NonNull ChatColor color, long duration) {
        if (duration < -1)
            throw new IllegalArgumentException("'duration'이 -1 이상이어야 함");
        if (duration == -1)
            duration = Long.MAX_VALUE;

        sendAddTeamPacket(target, color);

        if (CooldownUtil.getCooldown(target, GLOW_COOLDOWN_ID + this) == 0) {
            CooldownUtil.setCooldown(target, GLOW_COOLDOWN_ID + this, duration);

            new IntervalTask(i -> {
                if (isDisposed() || !target.isValid() || CooldownUtil.getCooldown(target, GLOW_COOLDOWN_ID + this) == 0)
                    return false;

                if (i % 4 == 0)
                    sendGlowingPacket(target, true);

                return true;
            }, () -> {
                sendGlowingPacket(target, false);
                sendRemoveTeamPacket(target);
            }, 1);
        } else if (CooldownUtil.getCooldown(target, GLOW_COOLDOWN_ID + this) < duration)
            CooldownUtil.setCooldown(target, GLOW_COOLDOWN_ID + this, duration);
    }

    /**
     * 지정한 엔티티에게 발광 효과를 적용한다.
     *
     * @param target 발광 효과를 적용할 엔티티
     * @param color  색상
     */
    public void setGlowing(@NonNull Entity target, @NonNull ChatColor color) {
        setGlowing(target, color, -1);
    }

    /**
     * 지정한 엔티티가 발광 상태인지 확인한다.
     *
     * @param target 확인할 엔티티
     * @return 플레이어에게 발광 상태면 {@code true} 반환
     */
    public boolean isGlowing(@NonNull Entity target) {
        return CooldownUtil.getCooldown(target, GLOW_COOLDOWN_ID + this) > 0;
    }

    /**
     * 지정한 엔티티의 발광 효과를 제거한다.
     *
     * @param target 발광 효과가 적용된 엔티티
     */
    public void removeGlowing(@NonNull Entity target) {
        CooldownUtil.setCooldown(target, GLOW_COOLDOWN_ID + this, 0);
    }

    /**
     * 플레이어에게 발광 효과 패킷을 전송한다.
     *
     * @param target    발광 효과를 적용할 엔티티
     * @param isEnabled 활성화 여부
     */
    private void sendGlowingPacket(@NonNull Entity target, boolean isEnabled) {
        WrapperPlayServerEntityMetadata packet = new WrapperPlayServerEntityMetadata();

        packet.setEntityID(target.getEntityId());
        WrappedDataWatcher dw = WrappedDataWatcher.getEntityWatcher(target).deepClone();
        dw.setObject(0, isEnabled ? (byte) (dw.getByte(0) | (1 << 6)) : (byte) (dw.getByte(0) & ~(1 << 6)));
        packet.setMetadata(dw.getWatchableObjects());

        packet.sendPacket(player);
    }

    /**
     * 플레이어에게 팀 추가 패킷을 전송한다.
     *
     * @param target 발광 효과를 적용할 엔티티
     * @param color  색상
     */
    private void sendAddTeamPacket(@NonNull Entity target, @NonNull ChatColor color) {
        sendRemoveTeamPacket(target);

        glowingMap.put(target, color);

        WrapperPlayServerScoreboardTeam packet1 = new WrapperPlayServerScoreboardTeam();
        WrapperPlayServerScoreboardTeam packet2 = new WrapperPlayServerScoreboardTeam();
        WrapperPlayServerScoreboardTeam packet3 = new WrapperPlayServerScoreboardTeam();
        packet1.setName(GLOW_COOLDOWN_ID + color.ordinal());
        packet2.setName(GLOW_COOLDOWN_ID + color.ordinal());
        packet3.setName(GLOW_COOLDOWN_ID + color.ordinal());

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
     *
     * @param target 발광 효과가 적용된 엔티티
     */
    private void sendRemoveTeamPacket(@NonNull Entity target) {
        ChatColor color = glowingMap.get(target);
        if (color == null)
            return;

        glowingMap.remove(target);

        WrapperPlayServerScoreboardTeam packet = new WrapperPlayServerScoreboardTeam();

        packet.setMode(4);
        packet.setName(GLOW_COOLDOWN_ID + color.ordinal());
        packet.setPlayers(Collections.singletonList(target instanceof Player ? target.getName() : target.getUniqueId().toString()));

        packet.sendPacket(player);
    }

    /**
     * 플레이어를 지정한 위치로 순간이동 시킨다.
     *
     * <p>이름표 숨기기 기능으로 인해 기본 텔레포트가 되지 않기 때문에 사용한다.</p>
     *
     * @param location 이동할 위치
     */
    public void teleport(@NonNull Location location) {
        validate();

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
     * 열 및 행 인자값이 유효하지 않으면 예외를 발생시킨다.
     *
     * @param column 열 번호
     * @param row    행 번호
     */
    private void validateColumnRow(int column, int row) {
        if (column < 0 || column > 3)
            throw new IndexOutOfBoundsException("'column'이 0에서 3 사이여야 함");
        if (row < 0 || row > 19)
            throw new IndexOutOfBoundsException("'row'가 0에서 19 사이여야 함");
    }
}
