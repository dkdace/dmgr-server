package com.dace.dmgr.game;

import com.dace.dmgr.Disposable;
import com.dace.dmgr.GeneralConfig;
import com.dace.dmgr.Timespan;
import com.dace.dmgr.Timestamp;
import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.character.CharacterType;
import com.dace.dmgr.combat.entity.Attacker;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.Healer;
import com.dace.dmgr.combat.interaction.DamageType;
import com.dace.dmgr.game.mode.GamePlayMode;
import com.dace.dmgr.item.DefinedItem;
import com.dace.dmgr.item.ItemBuilder;
import com.dace.dmgr.user.User;
import com.dace.dmgr.user.UserData;
import com.dace.dmgr.util.task.IntervalTask;
import com.keenant.tabbed.util.Skin;
import com.keenant.tabbed.util.Skins;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.Validate;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.Nullable;

import java.text.MessageFormat;
import java.util.Comparator;
import java.util.Iterator;
import java.util.function.Function;

/**
 * 게임 시스템의 플레이어 정보를 관리하는 클래스.
 */
public final class GameUser implements Disposable {
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
    private final Game.Team team;
    /** 전투 플레이어 인스턴스 */
    private final CombatUser combatUser;
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
    GameUser(@NonNull User user, @NonNull Game game, @NonNull Game.Team team) {
        Validate.validState(GameUserRegistry.getInstance().get(user) == null);
        Validate.validState(user.getGameRoom() != null && user.getGameRoom().getGame() == game);

        this.user = user;
        this.player = user.getPlayer();
        this.game = game;
        this.team = team;

        CombatUser defCombatUser = CombatUser.fromUser(user);
        if (defCombatUser != null)
            defCombatUser.dispose();
        this.combatUser = new CombatUser(this);

        GameUserRegistry.getInstance().add(user, this);

        game.onAddGameUser(this);

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
        return GameUserRegistry.getInstance().get(user);
    }

    private void onInit() {
        for (CommunicationItem communicationItem : CommunicationItem.values())
            user.getGui().set(communicationItem.slotIndex, communicationItem.definedItem);

        user.teleport(getSpawnLocation());
        user.clearChat();
        user.setInFreeCombat(false);

        user.sendTitle(game.getGamePlayMode().getName(), "§b§nF키§b를 눌러 전투원을 선택하십시오.", Timespan.ofSeconds(0.5),
                game.isPlaying() ? Timespan.ofSeconds(2) : game.getGamePlayMode().getReadyDuration(), Timespan.ofSeconds(1.5), Timespan.ofSeconds(4));
        user.getTabListManager().clearItems();
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
        if (!isInSpawn() && (!game.isPlaying() || !combatUser.isActivated()))
            user.teleport(getSpawnLocation());

        if (i % 5 == 0)
            team.getTeamUsers().forEach(target -> target.getUser().setGlowing(player, ChatColor.BLUE));

        if (i % 20 == 0)
            updateGameTabList();
    }

    /**
     * 플레이어가 아군 팀 스폰에 있을 때 매 틱마다 실행할 작업.
     */
    private void onTickTeamSpawn() {
        if (game.isPlaying() && !combatUser.isDead())
            user.sendTitle("", (combatUser.getCharacterType() == null)
                    ? "§b§nF키§b를 눌러 전투원을 선택하십시오."
                    : "§b§nF키§b를 눌러 전투원을 변경할 수 있습니다.", Timespan.ZERO, Timespan.ofSeconds(0.5), Timespan.ofSeconds(0.5));

        combatUser.getDamageModule().heal((Healer) null, GeneralConfig.getGameConfig().getTeamSpawnHealPerSecond() / 20.0, false);
    }

    /**
     * 플레이어가 상대 팀 스폰에 있을 때 매 틱마다 실행할 작업.
     */
    private void onTickOppositeSpawn() {
        if (!combatUser.isDead())
            user.sendTitle("", "§c상대 팀의 스폰 지역입니다.", Timespan.ZERO, Timespan.ofSeconds(0.5), Timespan.ofSeconds(0.5),
                    Timespan.ofSeconds(1));

        combatUser.getDamageModule().damage(combatUser, GeneralConfig.getGameConfig().getOppositeSpawnDamagePerSecond() / 20.0,
                DamageType.FIXED, null, false, false);
    }

    /**
     * 게임 유저를 제거하고, 소속된 게임에서 제거한다.
     */
    @Override
    public void dispose() {
        validate();

        if (game.getRemainingTime().toMilliseconds() > 0)
            user.getUserData().addQuitCount();

        onTickTask.dispose();
        game.onRemoveGameUser(this);

        user.reset();

        GameUserRegistry.getInstance().remove(user);
    }

    @Override
    public boolean isDisposed() {
        return GameUserRegistry.getInstance().get(user) == null;
    }

    /**
     * 게임 탭리스트를 업데이트한다.
     */
    private void updateGameTabList() {
        User.TabListManager tabListManager = user.getTabListManager();

        String title = (game.getGamePlayMode().isRanked() ? "§6§l[ 랭크 ] §f" : "§a§l[ 일반 ] §f") + game.getGamePlayMode().getName();
        String teamScore = MessageFormat.format("§4-=-=-=- §c§lRED §f[ {0} ] §4-=-=-=-            §1-=-=-=- §9§lBLUE §f[ {1} ] §1-=-=-=-",
                game.getRedTeam().getScore(),
                game.getBlueTeam().getScore());
        tabListManager.setHeader("\n" + title + "\n" + teamScore);

        boolean isHeadReveal = game.isPlaying()
                && game.getElapsedTime().compareTo(GeneralConfig.getGameConfig().getHeadRevealTimeAfterStart()) > 0;

        int column = 0;
        for (Game.Team targetTeam : new Game.Team[]{game.getRedTeam(), game.getBlueTeam()}) {
            tabListManager.setItem(++column, 0, MessageFormat.format("{0}§l§n {1} §f({2}명)",
                    targetTeam.getType().getColor(),
                    targetTeam.getType().getName(),
                    targetTeam.getTeamUsers().size()), Skins.getDot(targetTeam.getType().getColor()));

            Iterator<GameUser> iterator = targetTeam.getTeamUsers().stream()
                    .sorted(Comparator.comparing(GameUser::getScore).reversed())
                    .iterator();

            for (int i = 0; i < game.getGamePlayMode().getMaxPlayer() / 2; i++) {
                int row = i * 3 + 1;

                if (i > targetTeam.getTeamUsers().size() - 1) {
                    tabListManager.removeItem(column, row);
                    tabListManager.removeItem(column, row + 1);
                } else {
                    GameUser target = iterator.next();
                    User targetUser = target.getUser();

                    if (team == targetTeam || isHeadReveal)
                        tabListManager.setItem(column, row, targetUser.getUserData().getDisplayName(), targetUser);
                    else
                        tabListManager.setItem(column, row, targetUser.getUserData().getDisplayName(), Skins.getPlayer("crashdummie99"));

                    tabListManager.setItem(column, row + 1, MessageFormat.format("§7{0} §f{1}   §7{2} §f{3}   §7{4} §f{5}   §7{6} §f{7}",
                            "✪",
                            (int) target.getScore(),
                            TextIcon.DAMAGE,
                            target.getKill(),
                            TextIcon.POISON,
                            target.getDeath(),
                            "✔",
                            target.getAssist()), (Skin) null);
                }
            }
        }
    }

    /**
     * 점수를 증가시킨다.
     *
     * @param score 점수. 0 이상의 값
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    public void addScore(double score) {
        Validate.inclusiveBetween(0, Double.MAX_VALUE, score);
        validate();

        this.score += score;
    }

    /**
     * 입힌 피해량을 증가시킨다.
     *
     * @param damage 입힌 피해량. 0 이상의 값
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    public void addDamage(double damage) {
        Validate.inclusiveBetween(0, Double.MAX_VALUE, damage);
        validate();

        this.damage += damage;
    }

    /**
     * 막은 피해량을 증가시킨다.
     *
     * @param defend 막은 피해량. 0 이상의 값
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    public void addDefend(double defend) {
        Validate.inclusiveBetween(0, Double.MAX_VALUE, defend);
        validate();

        this.defend += defend;
    }

    /**
     * 입힌 치유량을 증가시킨다.
     *
     * @param heal 입힌 치유량. 0 이상의 값
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    public void addHeal(double heal) {
        Validate.inclusiveBetween(0, Double.MAX_VALUE, heal);
        validate();

        this.heal += heal;
    }

    /**
     * 다른 플레이어를 처치했을 때 실행할 작업.
     *
     * @param isFinalHit 결정타 여부. 마지막 공격으로 처치 시 결정타
     * @see CombatUser#onKill(Damageable)
     */
    public void onKill(boolean isFinalHit) {
        validate();

        CharacterType characterType = Validate.notNull(combatUser.getCharacterType());

        UserData.CharacterRecord characterRecord = user.getUserData().getCharacterRecord(characterType);
        characterRecord.addKill();

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
        validate();

        CharacterType characterType = Validate.notNull(combatUser.getCharacterType());

        UserData.CharacterRecord characterRecord = user.getUserData().getCharacterRecord(characterType);
        characterRecord.addDeath();
        death += 1;
    }

    /**
     * 스폰 위치를 반환한다.
     *
     * @return 스폰 위치
     * @see Game.Team#getSpawn()
     */
    @NonNull
    public Location getSpawnLocation() {
        return team.getSpawn();
    }

    /**
     * 플레이어가 스폰 지역 안에 있는지 확인한다.
     *
     * @return 플레이어가 스폰 안에 있으면 {@code true} 반환
     * @see Game.Team#isInSpawn(GameUser)
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
     * 게임에 참여한 모든 플레이어에게 채팅 메시지를 전송한다.
     *
     * @param message 메시지
     * @param isTeam  {@code true}로 지정 시 팀원에게만 전송
     */
    public void broadcastChatMessage(@NonNull String message, boolean isTeam) {
        String fullMessage = MessageFormat.format("§7§l[{0}] {1}", isTeam ? "팀" : "전체", combatUser.getFormattedMessage(message));

        for (GameUser gameUser : (isTeam ? team.getTeamUsers() : game.getGameUsers())) {
            User targetUser = gameUser.getUser();

            targetUser.getPlayer().sendMessage(fullMessage);
            targetUser.getUserData().getConfig().getChatSound().getSound().play(gameUser.getPlayer());
        }
    }

    /**
     * 의사소통 아이템 목록.
     */
    private enum CommunicationItem {
        /** 치료 요청 */
        REQ_HEAL("§a치료 요청", 9, targetCombatUser -> {
            Validate.notNull(targetCombatUser.getCharacterType());

            String state;
            int index;
            if (targetCombatUser.getDamageModule().isLowHealth()) {
                state = "치명상";
                index = 0;
            } else if (targetCombatUser.getDamageModule().getHealth() <= targetCombatUser.getDamageModule().getMaxHealth() / 2.0) {
                state = "체력 낮음";
                index = 1;
            } else {
                state = "치료 요청";
                index = 2;
            }
            String ment = targetCombatUser.getCharacterType().getCharacter().getReqHealMent()[index];

            return MessageFormat.format("§7[{0}] §f§l{1}", state, ment);
        }),
        /** 궁극기 상태 */
        SHOW_ULT("§a궁극기 상태", 10, targetCombatUser -> {
            Validate.notNull(targetCombatUser.getCharacterType());

            int index;
            if (targetCombatUser.getUltGaugePercent() < 0.9)
                index = 0;
            else if (targetCombatUser.getUltGaugePercent() < 1)
                index = 1;
            else
                index = 2;
            String ment = targetCombatUser.getCharacterType().getCharacter().getUltStateMent()[index];

            return MessageFormat.format("§7[궁극기 {0}%] §f§l{1}", Math.floor(targetCombatUser.getUltGaugePercent() * 100), ment);
        }),
        /** 집결 요청 */
        REQ_RALLY("§a집결 요청", 11, targetCombatUser -> {
            Validate.notNull(targetCombatUser.getCharacterType());

            String[] ments = targetCombatUser.getCharacterType().getCharacter().getReqRallyMent();
            String ment = ments[RandomUtils.nextInt(0, ments.length)];

            return MessageFormat.format("§7[집결 요청] §f§l{0}", ment);
        });

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
                    (clickType, player) -> {
                        if (clickType != ClickType.LEFT)
                            return false;

                        CombatUser combatUser = CombatUser.fromUser(User.fromPlayer(player));
                        if (combatUser == null || !combatUser.isActivated())
                            return false;

                        GameUser gameUser = combatUser.getGameUser();
                        if (gameUser == null)
                            return false;

                        if (!player.isOp()) {
                            if (gameUser.communicationTimestamp.isAfter(Timestamp.now()))
                                return false;

                            gameUser.communicationTimestamp = Timestamp.now().plus(GeneralConfig.getConfig().getChatCooldown());
                        }

                        gameUser.broadcastChatMessage(action.apply(combatUser), true);
                        player.closeInventory();

                        return true;
                    });
        }
    }
}
