package com.dace.dmgr.game;

import com.dace.dmgr.GeneralConfig;
import com.dace.dmgr.Timespan;
import com.dace.dmgr.Timestamp;
import com.dace.dmgr.combat.combatant.CombatantType;
import com.dace.dmgr.combat.entity.Attacker;
import com.dace.dmgr.combat.entity.DamageType;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.Healer;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.game.mode.GamePlayMode;
import com.dace.dmgr.item.DefinedItem;
import com.dace.dmgr.item.ItemBuilder;
import com.dace.dmgr.user.Place;
import com.dace.dmgr.user.User;
import com.dace.dmgr.util.EntityUtil;
import com.dace.dmgr.util.task.IntervalTask;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.apache.commons.lang3.Validate;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.Nullable;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.function.Function;

/**
 * 게임 시스템의 플레이어 정보를 관리하는 클래스.
 */
public final class GameUser {
    /** 게임 유저 목록 (유저 정보 : 게임 유저 정보) */
    private static final HashMap<User, GameUser> GAME_USER_MAP = new HashMap<>();

    /** 플레이어 인스턴스 */
    @NonNull
    @Getter
    private final Player player;
    /** 유저 정보 인스턴스 */
    @NonNull
    @Getter
    private final User user;
    /** 입장한 게임 */
    @NonNull
    @Getter
    private final Game game;
    /** 팀 */
    @NonNull
    @Getter
    private final Team team;
    /** 게임 탭리스트 프로필 */
    private final GameTabListProfile gameTabListProfile;
    /** 게임 시작 시점 */
    @Getter
    private final Timestamp startTime = Timestamp.now();
    /** 틱 작업을 처리하는 태스크 */
    private final IntervalTask onTickTask;

    /** 의사소통 GUI 타임스탬프 */
    private Timestamp communicationTimestamp = Timestamp.now();
    /** 점수 */
    @Getter
    private double score = 0;
    /** 킬 */
    @Getter
    private int kill = 0;
    /** 데스 */
    @Getter
    private int death = 0;
    /** 어시스트 */
    @Getter
    private int assist = 0;
    /** 입힌 피해량 */
    @Getter
    private double damage = 0;
    /** 막은 피해량 */
    @Getter
    private double defend = 0;
    /** 입힌 치유량 */
    @Getter
    private double heal = 0;
    /** 팀 채팅 활성화 여부 */
    @Getter
    @Setter
    private boolean isTeamChat = true;

    /**
     * 게임 시스템의 플레이어 인스턴스를 생성하고, 게임의 소속 유저 목록에 추가한다.
     *
     * @param user 대상 플레이어
     * @param game 대상 게임
     * @param team 설정할 팀
     * @throws IllegalStateException 해당 {@code user}가 지정한 {@code game}의 방에 입장하지 않았거나 GameUser가 이미 존재하면 발생
     */
    GameUser(@NonNull User user, @NonNull Game game, @NonNull Team team) {
        Validate.validState(!GAME_USER_MAP.containsKey(user), "GameUser가 이미 존재함");
        Validate.validState(user.getGameRoom() != null && user.getGameRoom().getGame() == game, "user.getGameRoom().getGame() == game (false)");

        this.user = user;
        this.player = user.getPlayer();
        this.game = game;
        this.team = team;
        this.gameTabListProfile = new GameTabListProfile(this);

        user.setCurrentPlace(Place.LOBBY);

        GAME_USER_MAP.put(user, this);

        game.onAddGameUser(this);
        team.onAddGameUser(this);

        this.onTickTask = new IntervalTask(this::onTick, 1);

        onInit();
    }

    /**
     * 지정한 플레이어의 게임 유저 인스턴스를 반환한다.
     *
     * @param user 대상 플레이어
     * @return 게임 유저 인스턴스. 존재하지 않으면 {@code null} 반환
     */
    @Nullable
    public static GameUser fromUser(@NonNull User user) {
        return GAME_USER_MAP.get(user);
    }

    private void onInit() {
        CommunicationItem.updateGUI(this);

        EntityUtil.teleport(player, getSpawnLocation());
        user.clearChat();

        user.sendTitle(game.getGamePlayMode().getName(), "§b§nF키§b를 눌러 전투원을 선택하십시오.", Timespan.ofSeconds(0.5),
                game.isPlaying() ? Timespan.ofSeconds(2) : game.getGamePlayMode().getReadyDuration(), Timespan.ofSeconds(1.5), Timespan.ofSeconds(4));
        user.setTabListProfile(gameTabListProfile);
    }

    /**
     * 매 틱마다 실행할 작업.
     *
     * @param i 인덱스
     */
    private void onTick(long i) {
        if (game.isPlaying()) {
            if (isInSpawn())
                onTickTeamSpawn();
            else if (team.getOppositeTeam().isInSpawn(this))
                onTickOppositeSpawn();
        }
        if (!isInSpawn() && (!game.isPlaying() || CombatUser.fromUser(user) == null))
            EntityUtil.teleport(player, getSpawnLocation());

        if (i % 5 == 0)
            team.getTeamUsers().forEach(target -> target.getUser().getGlowingManager().setGlowing(player, team.getType().getColor()));
    }

    /**
     * 플레이어가 아군 팀 스폰에 있을 때 매 틱마다 실행할 작업.
     */
    private void onTickTeamSpawn() {
        CombatUser combatUser = CombatUser.fromUser(user);
        if (combatUser != null && combatUser.isDead())
            return;

        if (game.isPlaying())
            user.sendTitle("", (combatUser == null)
                    ? "§b§nF키§b를 눌러 전투원을 선택하십시오."
                    : "§b§nF키§b를 눌러 전투원을 변경할 수 있습니다.", Timespan.ZERO, Timespan.ofSeconds(0.5), Timespan.ofSeconds(0.5));

        if (combatUser != null)
            combatUser.getDamageModule().heal((Healer) null, GeneralConfig.getGameConfig().getTeamSpawnHealPerSecond() / 20.0, false);
    }

    /**
     * 플레이어가 상대 팀 스폰에 있을 때 매 틱마다 실행할 작업.
     */
    private void onTickOppositeSpawn() {
        CombatUser combatUser = CombatUser.fromUser(user);
        if (combatUser == null)
            return;

        if (!combatUser.isDead())
            user.sendTitle("", "§c상대 팀의 스폰 지역입니다.", Timespan.ZERO, Timespan.ofSeconds(0.5), Timespan.ofSeconds(0.5),
                    Timespan.ofSeconds(1));

        combatUser.getDamageModule().damage(combatUser, GeneralConfig.getGameConfig().getOppositeSpawnDamagePerSecond() / 20.0,
                DamageType.FIXED, null, false, false);
    }

    /**
     * 게임 유저를 제거하고, 소속된 게임에서 제거한다.
     *
     * @throws IllegalStateException 이미 제거되었으면 발생
     */
    void remove() {
        Validate.validState(GAME_USER_MAP.containsKey(user), "GameUser가 이미 제거됨");

        if (!game.getRemainingTime().isZero())
            user.getUserData().addQuitCount();

        onTickTask.stop();
        game.onRemoveGameUser(this);
        team.onRemoveGameUser(this);

        GAME_USER_MAP.remove(user);

        user.setCurrentPlace(Place.LOBBY);
    }

    /**
     * 점수를 증가시킨다.
     *
     * @param score 점수. 0 이상의 값
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    public void addScore(double score) {
        Validate.isTrue(score >= 0, "score >= 0 (%f)", score);
        this.score += score;
    }

    /**
     * 입힌 피해량을 증가시킨다.
     *
     * @param damage 입힌 피해량. 0 이상의 값
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    public void addDamage(double damage) {
        Validate.isTrue(damage >= 0, "damage >= 0 (%f)", damage);
        this.damage += damage;
    }

    /**
     * 막은 피해량을 증가시킨다.
     *
     * @param defend 막은 피해량. 0 이상의 값
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    public void addDefend(double defend) {
        Validate.isTrue(defend >= 0, "defend >= 0 (%f)", defend);
        this.defend += defend;
    }

    /**
     * 입힌 치유량을 증가시킨다.
     *
     * @param heal 입힌 치유량. 0 이상의 값
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    public void addHeal(double heal) {
        Validate.isTrue(heal >= 0, "heal >= 0 (%f)", heal);
        this.heal += heal;
    }

    /**
     * 다른 플레이어를 처치했을 때 실행할 작업.
     *
     * @param isFinalHit 결정타 여부. 마지막 공격으로 처치 시 결정타
     * @see CombatUser#onKill(Damageable)
     */
    public void onKill(boolean isFinalHit) {
        CombatantType combatantType = Validate.notNull(CombatUser.fromUser(user)).getCombatantType();

        user.getUserData().getCombatantRecord(combatantType).addKill();

        if (isFinalHit) {
            kill += 1;
            if (game.getGamePlayMode() == GamePlayMode.TEAM_DEATHMATCH)
                team.addScore();
        } else
            assist += 1;
    }

    /**
     * 죽었을 때 실행할 작업.
     *
     * @see CombatUser#onDeath(Attacker)
     */
    public void onDeath() {
        CombatantType combatantType = Validate.notNull(CombatUser.fromUser(user)).getCombatantType();

        user.getUserData().getCombatantRecord(combatantType).addDeath();
        death += 1;
    }

    /**
     * 스폰 위치를 반환한다.
     *
     * @return 스폰 위치
     * @see Game#getTeamSpawn(Team)
     */
    @NonNull
    public Location getSpawnLocation() {
        return game.getTeamSpawn(team);
    }

    /**
     * 플레이어가 스폰 지역 안에 있는지 확인한다.
     *
     * @return 플레이어가 스폰 안에 있으면 {@code true} 반환
     * @see Team#isInSpawn(GameUser)
     */
    public boolean isInSpawn() {
        return team.isInSpawn(this);
    }

    /**
     * 플레이어의 킬/데스 를 반환한다.
     *
     * <p>어시스트도 킬로 취급하며, 데스가 0이면 1로 처리한다.</p>
     *
     * @return (킬 + 어시스트) / 데스
     */
    public double getKDARatio() {
        return (double) (kill + assist) / ((death == 0) ? 1 : death);
    }

    /**
     * 전투원이 지정되지 않았을 때 기본 메시지 포맷이 적용된 채팅 메시지를 반환한다.
     *
     * @param message 메시지
     * @return 포맷이 적용된 메시지
     */
    @NonNull
    private String getFormattedChatMessage(@NonNull String message) {
        return MessageFormat.format("<{0}§l[미선택]§f{1}> {2}", team.getType().getColor(), player.getName(), message);
    }

    /**
     * 게임에 참여한 모든 플레이어에게 채팅 메시지를 전송한다.
     *
     * @param message 메시지
     * @param isTeam  {@code true}로 지정 시 팀원에게만 전송
     */
    private void broadcastChatMessage(@NonNull String message, boolean isTeam) {
        CombatUser combatUser = CombatUser.fromUser(user);
        String fullMessage = MessageFormat.format("§7§l[{0}] {1}",
                isTeam ? "팀" : "전체",
                (combatUser == null) ? getFormattedChatMessage(message) : combatUser.getFormattedChatMessage(message));

        for (GameUser gameUser : (isTeam ? team.getTeamUsers() : game.getGameUsers())) {
            User targetUser = gameUser.getUser();

            targetUser.getPlayer().sendMessage(fullMessage);
            targetUser.getUserData().getConfig().getChatSound().getSound().play(gameUser.getPlayer());
        }
    }

    /**
     * 게임에 참여한 모든 플레이어에게 채팅 메시지를 전송한다.
     *
     * @param message 메시지
     */
    public void broadcastChatMessage(@NonNull String message) {
        broadcastChatMessage(message, isTeamChat);
    }

    /**
     * 의사소통 아이템 목록.
     */
    private enum CommunicationItem {
        /** 치료 요청 */
        REQ_HEAL("§a치료 요청", 9, targetCombatUser ->
                targetCombatUser.getCombatantType().getCombatant().getReqHealMent(targetCombatUser)),
        /** 궁극기 상태 */
        SHOW_ULT("§a궁극기 상태", 10, targetCombatUser ->
                targetCombatUser.getCombatantType().getCombatant().getUltStateMent(targetCombatUser)),
        /** 집결 요청 */
        REQ_RALLY("§a집결 요청", 11, targetCombatUser ->
                targetCombatUser.getCombatantType().getCombatant().getReqRallyMent());

        /** 인벤토리 칸 번호 */
        private final int slotIndex;
        /** GUI 아이템 */
        private final DefinedItem definedItem;

        CommunicationItem(String name, int slotIndex, Function<CombatUser, String> action) {
            this.slotIndex = slotIndex;
            this.definedItem = new DefinedItem(
                    new ItemBuilder(Material.STAINED_GLASS_PANE)
                            .setDamage((short) 5)
                            .setName(name)
                            .build(),
                    new DefinedItem.ClickHandler(ClickType.LEFT, target -> {
                        CombatUser combatUser = CombatUser.fromUser(User.fromPlayer(target));
                        if (combatUser == null)
                            return false;

                        GameUser gameUser = combatUser.getGameUser();
                        if (gameUser == null)
                            return false;

                        if (!target.isOp()) {
                            if (gameUser.communicationTimestamp.isAfter(Timestamp.now()))
                                return false;

                            gameUser.communicationTimestamp = Timestamp.now().plus(GeneralConfig.getConfig().getChatCooldown());
                        }

                        gameUser.broadcastChatMessage(action.apply(combatUser), true);
                        target.closeInventory();

                        return true;
                    }));
        }

        /**
         * 의사소통 아이템 인벤토리 GUI를 업데이트한다.
         *
         * @param gameUser 대상 플레이어
         */
        private static void updateGUI(@NonNull GameUser gameUser) {
            for (CommunicationItem communicationItem : CommunicationItem.values())
                gameUser.getUser().getGui().set(communicationItem.slotIndex, communicationItem.definedItem);
        }
    }
}
