package com.dace.dmgr.combat.entity;

import com.comphenix.packetwrapper.*;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.dace.dmgr.*;
import com.dace.dmgr.combat.Core;
import com.dace.dmgr.combat.FreeCombat;
import com.dace.dmgr.combat.FunctionalBlock;
import com.dace.dmgr.combat.action.Action;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.MeleeAttackAction;
import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.SkillInfo;
import com.dace.dmgr.combat.action.info.WeaponInfo;
import com.dace.dmgr.combat.action.skill.HasBonusScore;
import com.dace.dmgr.combat.action.skill.Skill;
import com.dace.dmgr.combat.action.skill.UltimateSkill;
import com.dace.dmgr.combat.action.weapon.FullAuto;
import com.dace.dmgr.combat.action.weapon.Swappable;
import com.dace.dmgr.combat.action.weapon.Weapon;
import com.dace.dmgr.combat.combatant.Combatant;
import com.dace.dmgr.combat.combatant.CombatantType;
import com.dace.dmgr.combat.combatant.silia.SiliaA3Info;
import com.dace.dmgr.combat.entity.module.*;
import com.dace.dmgr.combat.entity.temporary.SummonEntity;
import com.dace.dmgr.combat.interaction.HasCritHitbox;
import com.dace.dmgr.combat.interaction.Hitbox;
import com.dace.dmgr.effect.BossBarDisplay;
import com.dace.dmgr.effect.SoundEffect;
import com.dace.dmgr.effect.TextHologram;
import com.dace.dmgr.game.Game;
import com.dace.dmgr.game.GameUser;
import com.dace.dmgr.item.DefinedItem;
import com.dace.dmgr.item.ItemBuilder;
import com.dace.dmgr.item.gui.Menu;
import com.dace.dmgr.user.User;
import com.dace.dmgr.util.EntityUtil;
import com.dace.dmgr.util.LocationUtil;
import com.dace.dmgr.util.task.DelayTask;
import com.dace.dmgr.util.task.IntervalTask;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.MainHand;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.text.MessageFormat;
import java.util.*;
import java.util.function.LongConsumer;
import java.util.stream.Collectors;

/**
 * 전투 시스템의 플레이어 정보를 관리하는 클래스.
 */
public final class CombatUser extends AbstractCombatEntity<Player> implements Healable, Attacker, Healer, HasCritHitbox, Movable {
    /** 궁극기 차단 점수 */
    private static final int ULT_BLOCK_KILL_SCORE = 50;
    /** 결정타 점수 */
    private static final int FINAL_HIT_SCORE = 20;
    /** 추락사 점수 */
    private static final int FALL_ZONE_KILL_SCORE = 30;
    /** 연속 처치 점수 */
    private static final int KILLSTREAK_SCORE = 25;
    /** 코어 장착 인벤토리 슬롯 목록 */
    private static final int[] CORE_INVENTORY_SLOT = {27, 28, 29};

    /** 힘의 코어 수정자 */
    private static final AbilityStatus.Modifier CORE_STRENGTH_MODIFIER = new AbilityStatus.Modifier(Core.STRENGTH.getValue());
    /** 저항의 코어 수정자 */
    private static final AbilityStatus.Modifier CORE_RESISTANCE_MODIFIER = new AbilityStatus.Modifier(Core.RESISTANCE.getValue());
    /** 신속의 코어 수정자 */
    private static final AbilityStatus.Modifier CORE_SPEED_MODIFIER = new AbilityStatus.Modifier(Core.SPEED.getValue());
    /** 치유의 코어 수정자 */
    private static final AbilityStatus.Modifier CORE_HEALING_MODIFIER = new AbilityStatus.Modifier(Core.HEALING.getValue());
    /** 강인함의 코어 수정자 */
    private static final AbilityStatus.Modifier CORE_ENDURANCE_MODIFIER = new AbilityStatus.Modifier(Core.ENDURANCE.getValue());

    /** 메뉴 아이템 */
    private static final DefinedItem MENU_ITEM = new DefinedItem(new ItemBuilder(
            PlayerSkin.fromURL("ZDliMjk4M2MwMWI4ZGE3ZGMxYzBmMTJkMDJjNGFiMjBjZDhlNjg3NWU4ZGY2OWVhZTJhODY3YmFlZTYyMzZkNCJ9fX0="))
            .setName("§e§l메뉴")
            .setLore("§f메뉴 창을 엽니다.")
            .build(),
            new DefinedItem.ClickHandler(ClickType.LEFT, player -> {
                new Menu(player);
                return true;
            }));

    /** 공격 모듈 */
    @NonNull
    @Getter
    private final AttackModule attackModule;
    /** 치유 모듈 */
    @NonNull
    @Getter
    private final HealerModule healerModule;
    /** 피해 모듈 */
    @NonNull
    @Getter
    private final HealModule damageModule;
    /** 상태 효과 모듈 */
    @NonNull
    @Getter
    private final StatusEffectModule statusEffectModule;
    /** 이동 모듈 */
    @NonNull
    @Getter
    private final MoveModule moveModule;
    /** 유저 정보 인스턴스 */
    @NonNull
    @Getter
    private final User user;
    /** 게임 유저 인스턴스. {@code null}이면 게임에 참여중이지 않음을 나타냄 */
    @Nullable
    @Getter
    private final GameUser gameUser;
    /** 팀. {@code null}이면 게임에 참여중이지 않음을 나타냄 */
    @Nullable
    @Getter
    private final Game.Team team;
    /** 처치 기여자별 누적 피해량 관리 인스턴스 */
    private final KillContributorManager killContributorManager = new KillContributorManager();
    /** 처치 지원자 관리 인스턴스 */
    private final KillHelperManager killHelperManager = new KillHelperManager();
    /** 동작 사용 키 매핑 목록 (동작 사용 키 : 동작 목록) */
    private final EnumMap<ActionKey, TreeSet<Action>> actionMap = new EnumMap<>(ActionKey.class);
    /** 스킬 목록 (스킬 정보 : 스킬) */
    private final HashMap<SkillInfo<? extends Skill>, Skill> skillMap = new HashMap<>();
    /** 획득 점수 목록 (항목 : 획득 점수) */
    private final LinkedHashMap<String, Double> scoreMap = new LinkedHashMap<String, Double>() {
        @Override
        protected boolean removeEldestEntry(Map.Entry eldest) {
            return size() > 10;
        }
    };
    /** 장착한 코어 목록 */
    private final EnumSet<Core> cores = EnumSet.noneOf(Core.class);
    /** 현재 히트박스 목록 */
    private Hitbox[] currentHitboxes;
    /** 누적 자가 피해량. 자가 피해 치유 시 궁극기 충전 방지를 위해 사용 */
    private double selfHarmDamage = 0;
    /** 연속으로 획득한 점수의 합 */
    private double scoreStreakSum = 0;
    /** 연속 처치 횟수 */
    private int killStreak = 0;
    /** 시야각 값 */
    @Getter
    @Setter
    private double fovValue = 0;
    /** 이동 거리. 발소리 재생에 사용됨 */
    private double footstepDistance = 0;
    /** 사망 여부 */
    @Getter
    private boolean isDead;
    /** 선택한 전투원 종류 */
    @NonNull
    @Getter
    private CombatantType combatantType;
    /** 선택한 전투원 */
    private Combatant combatant;
    /** 무기 인스턴스 */
    @NonNull
    @Getter
    private Weapon weapon;
    /** 사망 대사 홀로그램 */
    @Nullable
    private TextHologram deathMentHologram;

    /** 총기의 초탄 반동 타임스탬프 */
    @NonNull
    @Getter
    @Setter
    private Timestamp weaponFirstRecoilTimestamp = Timestamp.now();
    /** 점프대 쿨타임 타임스탬프 */
    @NonNull
    @Getter
    @Setter
    private Timestamp jumpPadCooldownTimestamp = Timestamp.now();
    /** 낙사구역 타임스탬프 */
    @NonNull
    @Getter
    @Setter
    private Timestamp fallZoneTimestamp = Timestamp.now();
    /** 마지막 피격 시점 */
    @NonNull
    @Getter
    private Timestamp lastDamageTimestamp = Timestamp.now();
    /** 마지막 치유 시점 */
    @NonNull
    @Getter
    private Timestamp lastGiveHealTimestamp = Timestamp.now();
    /** 적 타격 효과음 타임스탬프 */
    private Timestamp hitSoundTimestamp = Timestamp.now();
    /** 연속 처치 제한시간 타임스탬프 */
    private Timestamp killStreakTimeLimitTimestamp = Timestamp.now();
    /** 동작 전역 쿨타임 타임스탬프 */
    private Timestamp actionGlobalCooldownTimestamp = Timestamp.now();
    /** 획득 점수 표시 타임스탬프 */
    private Timestamp scoreDisplayTimestamp = Timestamp.now();

    /**
     * 전투 시스템의 플레이어 인스턴스를 생성한다.
     *
     * @param combatantType 전투원 종류
     * @param user          대상 플레이어
     * @param gameUser      대상 게임 유저 인스턴스
     * @throws IllegalStateException 해당 {@code user}의 CombatUser가 이미 존재하면 발생
     */
    private CombatUser(@NonNull CombatantType combatantType, @NonNull User user, @Nullable GameUser gameUser) {
        super(user.getPlayer(), user.getPlayer().getName(), gameUser == null ? null : gameUser.getGame());

        this.user = user;
        this.gameUser = gameUser;
        this.team = gameUser == null ? null : gameUser.getTeam();
        this.combatantType = combatantType;
        this.combatant = combatantType.getCombatant();
        this.weapon = combatant.getWeaponInfo().createWeapon(this);

        this.attackModule = new AttackModule();
        this.healerModule = new HealerModule();
        this.damageModule = new HealModule(this, 1000, true);
        this.statusEffectModule = new StatusEffectModule(this);
        this.moveModule = new MoveModule(this, GeneralConfig.getCombatConfig().getDefaultSpeed());

        user.getSidebarManager().clear();
        user.getGui().set(8, MENU_ITEM);

        setCombatantType(combatantType);

        addOnTick(this::onTick);
        addOnRemove(this::onDispose);
    }

    /**
     * 전투 시스템의 플레이어 인스턴스를 생성한다.
     *
     * @param combatantType 전투원 종류
     * @param user          대상 플레이어
     * @throws IllegalStateException 해당 {@code user}의 CombatUser가 이미 존재하면 발생
     */
    public CombatUser(@NonNull CombatantType combatantType, @NonNull User user) {
        this(combatantType, user, GameUser.fromUser(user));
    }

    /**
     * 지정한 플레이어의 전투 시스템의 플레이어 인스턴스를 반환한다.
     *
     * @param user 대상 플레이어
     * @return 전투 시스템의 플레이어 인스턴스. 존재하지 않으면 {@code null} 반환
     */
    @Nullable
    public static CombatUser fromUser(@NonNull User user) {
        CombatEntity combatEntity = CombatEntity.fromEntity(user.getPlayer());
        if (combatEntity == null)
            return null;

        return (CombatUser) combatEntity;
    }

    /**
     * 매 틱마다 실행할 작업.
     *
     * @param i 인덱스
     */
    private void onTick(long i) {
        combatant.onTick(this, i);

        applyFastDiggingPotionEffect();
        setLowHealthScreenEffect(damageModule.isLowHealth() || isDead);
        setCanSprint();
        setCanFly();

        if (!isDead)
            onTickLive(i);
    }

    /**
     * 제거되었을 때 실행할 작업.
     */
    private void onDispose() {
        if (deathMentHologram != null)
            deathMentHologram.remove();

        if (DMGR.getPlugin().isEnabled())
            user.resetSkin();

        reset();
    }

    /**
     * 성급함 포션 효과를 적용한다. (좌클릭 시 주먹 애니메이션 방지).
     */
    @SuppressWarnings("deprecation")
    private void applyFastDiggingPotionEffect() {
        if (entity.hasPotionEffect(PotionEffectType.FAST_DIGGING))
            return;

        entity.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, Integer.MAX_VALUE, 39, false, false), true);

        WrapperPlayServerEntityEffect packet = new WrapperPlayServerEntityEffect();
        packet.setEntityID(entity.getEntityId());
        packet.setEffectID((byte) PotionEffectType.FAST_DIGGING.getId());
        packet.setAmplifier((byte) 39);
        packet.setDuration(-1);
        packet.setHideParticles(true);
        packet.broadcastPacket();
    }

    /**
     * 플레이어의 치명상 화면 효과 표시를 설정한다.
     *
     * @param isEnabled 활성화 여부
     */
    private void setLowHealthScreenEffect(boolean isEnabled) {
        WrapperPlayServerWorldBorder packet = new WrapperPlayServerWorldBorder();

        packet.setAction(EnumWrappers.WorldBorderAction.SET_WARNING_BLOCKS);
        packet.setWarningDistance(isEnabled ? Integer.MAX_VALUE : 0);

        packet.sendPacket(entity);
    }

    /**
     * 플레이어의 달리기 가능 여부를 설정한다.
     */
    private void setCanSprint() {
        WrapperPlayServerUpdateHealth packet = new WrapperPlayServerUpdateHealth();

        packet.setHealth((float) entity.getHealth());
        packet.setFood(canSprint() ? 19 : 2);

        packet.sendPacket(entity);

        if (isDead)
            entity.setSprinting(false);
    }

    /**
     * 플레이어가 달리기를 할 수 있는지 확인한다.
     *
     * @return 달리기 가능 여부
     */
    private boolean canSprint() {
        return !isDead && combatant.canSprint(this) && !statusEffectModule.hasRestriction(CombatRestriction.SPRINT);
    }

    /**
     * 플레이어의 비행 가능 여부를 설정한다.
     */
    private void setCanFly() {
        entity.setAllowFlight(!isDead && combatant.canFly(this) && !statusEffectModule.hasRestriction(CombatRestriction.FLY));
    }

    /**
     * 플레이어가 생존 중일 때 매 틱마다 실행할 작업.
     *
     * @param i 인덱스
     */
    private void onTickLive(long i) {
        user.sendActionBar(combatant.getActionBarString(this));

        if (currentHitboxes == hitboxes) {
            hitboxes[2].setAxisOffsetY(entity.isSneaking() ? 1.15 : 1.4);
            hitboxes[3].setAxisOffsetY(entity.isSneaking() ? 1.15 : 1.4);
        }

        FunctionalBlock.use(getLocation().subtract(0, 0.5, 0), this);
        handleFootstep();

        if (damageModule.isLowHealth())
            combatant.getSpecies().getReaction().onTickLowHealth(this);

        if (i % 10 == 0)
            addUltGauge(GeneralConfig.getCombatConfig().getIdleUltChargePerSecond() / 2.0);

        if (i % 20 == 0) {
            if (hasCore(Core.REGENERATION))
                damageModule.heal((Healer) null, damageModule.getMaxHealth() * Core.REGENERATION.getValue() / 100.0, false);

            if (gameUser != null && !gameUser.isInSpawn())
                user.getUserData().getCombatantRecord(combatantType).addPlayTime();
        }
    }

    /**
     * 발소리 재생에 사용되는 작업.
     */
    private void handleFootstep() {
        Location oldLoc = getLocation();
        double fallDistance = entity.getFallDistance();

        addTask(new DelayTask(() -> {
            Location loc = getLocation();

            footstepDistance += oldLoc.distance(loc);
            if (!entity.isOnGround() || footstepDistance <= 1.6 || combatantType == CombatantType.SILIA
                    && !getSkill(SiliaA3Info.getInstance()).isDurationFinished())
                return;

            footstepDistance = 0;
            double volume;
            if (entity.isSprinting())
                volume = 1;
            else if (!entity.isSneaking())
                volume = 0.8;
            else
                volume = 0.4;

            if (fallDistance > 0.5) {
                volume = 1.2 + fallDistance * 0.05;
                SoundEffect fallSound;
                if (fallDistance > 6)
                    fallSound = SOUND.FALL_HIGH;
                else if (fallDistance > 3)
                    fallSound = SOUND.FALL_MID;
                else
                    fallSound = SOUND.FALL_LOW;

                fallSound.play(loc, volume);
            }

            combatant.onFootstep(this, volume);
        }, 1));
    }

    @Override
    public boolean canBeTargeted() {
        if (gameUser != null && gameUser.isInSpawn())
            return false;
        if (FreeCombat.getInstance().isInFreeCombatWait(entity))
            return false;

        return !isDead;
    }

    @Override
    public boolean isEnemy(@NonNull CombatEntity target) {
        if (target == this)
            return false;
        if (target instanceof CombatUser)
            return (getTeam() == null || getTeam() != target.getTeam()) && user.getCurrentPlace() != User.Place.TRAINING_CENTER;

        return target.isEnemy(this);
    }

    @Override
    @Nullable
    public Hitbox getCritHitbox() {
        return currentHitboxes == hitboxes ? hitboxes[3] : null;
    }

    @Override
    public boolean isCreature() {
        return true;
    }

    @Override
    public boolean isGoalTarget() {
        return true;
    }

    @Override
    public boolean canJump() {
        return combatant.canJump(this);
    }

    @Override
    public void onAttack(@NonNull Damageable victim, double damage, boolean isCrit, boolean isUlt) {
        if (this == victim)
            return;

        boolean onAttackUlt = combatant.onAttack(this, victim, damage, isCrit);
        isUlt = isUlt && onAttackUlt;

        playAttackEffect(isCrit);

        if (victim.isGoalTarget()) {
            if (isUlt)
                addUltGauge(damage);

            if (hasCore(Core.HEALTH_DRAIN))
                damageModule.heal(this, damage * Core.HEALTH_DRAIN.getValue() / 100.0, false);

            if (gameUser != null && victim instanceof CombatUser)
                gameUser.addDamage(damage);
        }
    }

    /**
     * 공격했을 때 효과를 재생한다.
     *
     * @param isCrit 치명타 여부
     */
    private void playAttackEffect(boolean isCrit) {
        if (hitSoundTimestamp.isAfter(Timestamp.now()))
            return;

        hitSoundTimestamp = Timestamp.now().plus(Timespan.ofTicks(1));
        String title;
        SoundEffect sound;
        if (isCrit) {
            title = "§c§l×";
            sound = SOUND.ATTACK_CRIT;
        } else {
            title = "§f×";
            sound = SOUND.ATTACK;
        }

        user.sendTitle("", title, Timespan.ZERO, Timespan.ofTicks(2), Timespan.ofTicks(10));
        addTask(new DelayTask(() -> sound.play(entity), 2));
    }

    @Override
    public void onDamage(@Nullable Attacker attacker, double damage, double reducedDamage, @Nullable Location location, boolean isCrit) {
        if (this == attacker || attacker == null)
            selfHarmDamage += damage;

        if (damageModule.getTotalShield() == 0)
            combatant.getSpecies().getReaction().onDamage(this, damage, location);
        combatant.onDamage(this, attacker, damage, location, isCrit);

        lastDamageTimestamp = Timestamp.now();

        if (attacker instanceof SummonEntity)
            attacker = ((SummonEntity<?>) attacker).getOwner();
        if (attacker instanceof CombatUser)
            killContributorManager.addDamage((CombatUser) attacker, damage);

        if (gameUser != null && attacker != null)
            gameUser.addDefend(reducedDamage);
    }

    @Override
    public void onGiveHeal(@NonNull Healable target, double amount, boolean isUlt) {
        boolean onGiveHealUlt = combatant.onGiveHeal(this, target, amount);
        isUlt = isUlt && onGiveHealUlt;

        if (this != target)
            lastGiveHealTimestamp = Timestamp.now();

        if (isUlt) {
            double ultAmount = amount;
            if (target instanceof CombatUser && ((CombatUser) target).selfHarmDamage > 0)
                ultAmount = Math.max(0, amount - ((CombatUser) target).selfHarmDamage);

            addUltGauge(ultAmount);
        }

        if (gameUser != null && target instanceof CombatUser)
            gameUser.addHeal(amount);
    }

    @Override
    public void onTakeHeal(@Nullable Healer provider, double amount) {
        combatant.onTakeHeal(this, provider, amount);

        selfHarmDamage -= amount;
        if (selfHarmDamage < 0)
            selfHarmDamage = 0;
    }

    @Override
    public void onKill(@NonNull Damageable victim) {
        if (this == victim)
            return;

        playKillEffect();

        int score = victim instanceof CombatUser ? ((CombatUser) victim).killContributorManager.getScore(this) : victim.getScore();
        int contributeScore = victim instanceof CombatUser ? score : 100;

        combatant.onKill(this, victim, contributeScore, true);
        addScore(MessageFormat.format("§e{0}§f {1}", victim.getName(), (victim.isCreature() ? "처치" : "파괴")), score);

        if (!victim.isGoalTarget())
            return;

        addScore("결정타", FINAL_HIT_SCORE);

        handleBonusScoreSkill(victim, contributeScore);

        if (killStreakTimeLimitTimestamp.isBefore(Timestamp.now()))
            killStreak = 0;
        killStreakTimeLimitTimestamp = Timestamp.now().plus(GeneralConfig.getCombatConfig().getKillStreakTimeLimit());
        if (killStreak++ > 0)
            addScore(killStreak + "명 연속 처치", KILLSTREAK_SCORE * (killStreak - 1.0));

        if (victim instanceof CombatUser) {
            CombatUser combatUserVictim = (CombatUser) victim;

            if (!(combatUserVictim.getSkill(combatUserVictim.getCombatantType().getCombatant().getUltimateSkillInfo()).isDurationFinished()))
                addScore("궁극기 차단", ULT_BLOCK_KILL_SCORE);

            sendPlayerKillMent(combatUserVictim);
            combatUserVictim.sendPlayerDeathMent(this);

            if (gameUser != null)
                gameUser.onKill(true);
        }
    }

    /**
     * 엔티티를 처치했을 때 효과를 재생한다.
     */
    private void playKillEffect() {
        user.sendTitle("", "§c" + TextIcon.POISON, Timespan.ZERO, Timespan.ofTicks(2), Timespan.ofTicks(10));
        addTask(new DelayTask(() -> SOUND.KILL.play(entity), 2));
    }

    /**
     * 사망 시 킬로그 보스바를 표시한다.
     */
    private void broadcastPlayerKillBossBar() {
        if (game == null)
            return;

        String attackerNames = killContributorManager.getDamageMap().keySet().stream()
                .map(CombatUser::getPlayerKillBossBarName)
                .collect(Collectors.joining(", "));
        String victimName = getPlayerKillBossBarName();
        BossBarDisplay killBossBar = new BossBarDisplay(MessageFormat.format("{0} §4§l➡ {1}", attackerNames, victimName));

        game.addBossBar(killBossBar);

        new DelayTask(() -> {
            if (!game.isFinished())
                game.removeBossBar(killBossBar);
        }, GeneralConfig.getCombatConfig().getKillLogDisplayDuration().toTicks());
    }

    /**
     * 킬로그 보스바에 사용되는 플레이어의 이름을 반환한다.
     *
     * @return 이름
     */
    @NonNull
    private String getPlayerKillBossBarName() {
        ChatColor color = gameUser == null ? ChatColor.WHITE : gameUser.getTeam().getType().getColor();

        return MessageFormat.format("§f{0}{1}§l {2}", combatant.getIcon(), color, name);
    }

    /**
     * 적 처치 시 피격자에게 처치 대사를 전송한다.
     *
     * @param victim 피격자
     */
    private void sendPlayerKillMent(@NonNull CombatUser victim) {
        String[] ments = combatant.getKillMent(victim.getCombatantType());
        String ment = ments[RandomUtils.nextInt(0, ments.length)];

        sendMentMessage(this, ment);
        sendMentMessage(victim, ment);

        addTask(new DelayTask(() ->
                victim.getUser().sendTypewriterTitle(String.valueOf(combatant.getIcon()), MessageFormat.format("§f\"{0}\"", ment)), 10));
    }

    /**
     * 사망 시 공격자에게 사망 대사를 전송하고 사망 위치에 홀로그램을 표시한다.
     *
     * @param attacker 공격자
     */
    private void sendPlayerDeathMent(@NonNull CombatUser attacker) {
        String[] ments = combatant.getDeathMent(attacker.getCombatantType());
        String ment = ments[RandomUtils.nextInt(0, ments.length)];

        sendMentMessage(this, ment);
        sendMentMessage(attacker, ment);

        Location hologramLoc = LocationUtil.getNearestAgainstEdge(getLocation(), new Vector(0, -1, 0)).add(0, 1.2, 0);
        deathMentHologram = new TextHologram(hologramLoc, player -> LocationUtil.canPass(player.getEyeLocation(), hologramLoc),
                MessageFormat.format("§f{0} \"{1}\"", combatant.getIcon(), ment));
    }

    @Override
    public void onDeath(@Nullable Attacker attacker) {
        if (isDead)
            return;

        isDead = true;

        combatant.getSpecies().getReaction().onDeath(this);
        combatant.onDeath(this, attacker);

        killContributorManager.getDamageMap().keySet().forEach(target -> {
            target.killHelperManager.getScoreInfoMap().forEach((targetAttacker, scoreInfo) -> {
                for (double score : scoreInfo.getScores())
                    targetAttacker.addScore("처치 지원", score);
            });

            if (fallZoneTimestamp.isAfter(Timestamp.now()))
                target.addScore("추락사", FALL_ZONE_KILL_SCORE);

            if (target != (attacker instanceof SummonEntity ? ((SummonEntity<?>) attacker).getOwner() : attacker)) {
                int score = killContributorManager.getScore(target);

                target.combatant.onKill(target, this, score, false);
                target.addScore(MessageFormat.format("§e{0}§f 처치 도움", name), score);
                target.handleBonusScoreSkill(this, score);

                target.playKillEffect();

                if (target.getGameUser() != null)
                    target.getGameUser().onKill(false);
            }
        });

        broadcastPlayerKillBossBar();

        killContributorManager.clear();
        statusEffectModule.clear();
        damageModule.setHealth(damageModule.getMaxHealth());
        damageModule.clearShields();
        selfHarmDamage = 0;
        cancelAction(null);

        if (gameUser != null)
            gameUser.onDeath();

        respawn();
    }

    /**
     * 사망 후 리스폰 작업을 수행한다.
     */
    private void respawn() {
        Location deadLocation = (gameUser == null ? user.getCurrentPlace().getStartLocation() : gameUser.getSpawnLocation().add(0, 2, 0));
        EntityUtil.teleport(entity, deadLocation);

        Timespan duration = GeneralConfig.getCombatConfig().getRespawnTime();
        if (gameUser == null)
            duration = Timespan.ofSeconds(1);
        else if (hasCore(Core.RESURRECTION))
            duration = duration.multiply((100 - Core.RESURRECTION.getValue()) / 100.0);

        long durationTicks = duration.toTicks();

        entity.setGameMode(GameMode.SPECTATOR);
        entity.setVelocity(new Vector());

        addTask(new IntervalTask(i -> {
            if (!user.isTypewriterTitlePrinting())
                user.sendTitle("§c§l죽었습니다!", MessageFormat.format("{0}초 후 부활합니다.",
                        String.format("%.1f", Timespan.ofTicks(durationTicks - i).toSeconds())), Timespan.ZERO, Timespan.ofTicks(5), Timespan.ofTicks(10));

            EntityUtil.teleport(entity, deadLocation);
            entity.setSpectatorTarget(null);
        }, () -> {
            isDead = false;

            statusEffectModule.clear();
            entity.setGameMode(GameMode.SURVIVAL);

            weapon.reset();
            skillMap.values().forEach(Skill::reset);

            if (deathMentHologram != null) {
                deathMentHologram.remove();
                deathMentHologram = null;
            }
        }, 1, durationTicks));
    }

    /**
     * 적 처치 시 스킬의 보너스 점수 지급을 처리한다.
     *
     * @param victim 피격자
     * @param score  처치 기여 점수
     */
    private void handleBonusScoreSkill(@NonNull Damageable victim, int score) {
        for (SkillInfo<?> skillInfo : combatant.getSkillInfos()) {
            Skill skill = getSkill(skillInfo);
            if (skill instanceof HasBonusScore)
                ((HasBonusScore) skill).getBonusScoreModule().onKill(victim, score);
        }
    }

    /**
     * 공격자의 남은 적 처치 기여 제한시간을 반환한다.
     *
     * @param attacker 공격자
     * @return 남은 적 처치 기여 제한시간
     */
    @NonNull
    public Timespan getKillContributorRemainingTime(@NonNull CombatUser attacker) {
        return killContributorManager.getRemainingTime(attacker);
    }

    /**
     * 플레이어의 왼손 또는 오른손의 위치를 반환한다.
     *
     * @param hand 왼손/오른손
     * @return 해당 위치
     */
    @NonNull
    public Location getArmLocation(@NonNull MainHand hand) {
        return LocationUtil.getLocationFromOffset(entity.getEyeLocation().subtract(0, 0.4, 0),
                hand == MainHand.RIGHT ? 0.2 : -0.2, 0, 0);
    }

    /**
     * 지정한 스킬 정보에 해당하는 스킬을 반환한다.
     *
     * @param skillInfo 스킬 정보
     * @param <T>       {@link Skill}을 상속받는 스킬
     * @return 스킬 인스턴스
     * @throws NullPointerException 해당하는 스킬이 존재하지 않으면 발생
     */
    @NonNull
    @SuppressWarnings("unchecked")
    public <T extends Skill> T getSkill(@NonNull SkillInfo<T> skillInfo) {
        return Validate.notNull((T) skillMap.get(skillInfo), "일치하는 스킬이 존재하지 않음");
    }

    /**
     * 적 처치를 지원하는 플레이어를 추가한다.
     *
     * <p>플레이어가 적을 처치하면 해당 점수를 지정한 지원자에게 지급한다.</p>
     *
     * @param helper   지원자
     * @param action   사용한 동작
     * @param score    지원 점수. 0 이상의 값
     * @param duration 지속시간
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    public void addKillHelper(@NonNull CombatUser helper, @NonNull Action action, double score, @NonNull Timespan duration) {
        Validate.isTrue(score >= 0, "score >= 0 (%f)", score);
        killHelperManager.addHelper(helper, action, score, Timestamp.now().plus(duration));
    }

    /**
     * 플레이어의 전역 쿨타임이 끝났는지 확인한다.
     *
     * @return 전역 쿨타임 종료 여부
     */
    public boolean isGlobalCooldownFinished() {
        return actionGlobalCooldownTimestamp.isBefore(Timestamp.now());
    }

    /**
     * 플레이어의 전역 쿨타임을 초기화한다.
     */
    public void resetGlobalCooldown() {
        actionGlobalCooldownTimestamp = Timestamp.now();
        entity.setCooldown(SkillInfo.MATERIAL, 0);
        entity.setCooldown(WeaponInfo.MATERIAL, 0);
    }

    /**
     * 플레이어의 전역 쿨타임을 설정한다.
     *
     * @param cooldown 쿨타임
     */
    public void setGlobalCooldown(@NonNull Timespan cooldown) {
        if (cooldown.compareTo(Timestamp.now().until(actionGlobalCooldownTimestamp)) <= 0)
            return;

        actionGlobalCooldownTimestamp = Timestamp.now().plus(cooldown);

        int cooldownTicks = (int) Math.min(Integer.MAX_VALUE, cooldown.toTicks());
        entity.setCooldown(SkillInfo.MATERIAL, cooldownTicks);
        if (cooldown.compareTo(weapon.getCooldown()) > 0)
            entity.setCooldown(WeaponInfo.MATERIAL, cooldownTicks);
    }

    /**
     * 지정한 양만큼 플레이어의 점수를 증가시키고 사이드바를 표시한다.
     *
     * <p>게임 참여 중이 아니면 점수 획득 표시만 한다.</p>
     *
     * @param context 항목
     * @param score   추가할 점수. 0 이상의 값
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    public void addScore(@NonNull String context, double score) {
        Validate.isTrue(score >= 0, "score >= 0 (%f)", score);

        if (gameUser != null)
            gameUser.addScore(score);

        Timestamp expiration = Timestamp.now().plus(GeneralConfig.getCombatConfig().getScoreDisplayDuration());
        if (scoreDisplayTimestamp.isBefore(Timestamp.now())) {
            scoreDisplayTimestamp = expiration;

            addTask(new IntervalTask(i -> scoreDisplayTimestamp.isAfter(Timestamp.now()), () -> {
                scoreStreakSum = 0;
                scoreMap.clear();
                user.getSidebarManager().clear();
            }, 1));
        } else
            scoreDisplayTimestamp = expiration;

        scoreStreakSum += score;
        scoreMap.put(context, scoreMap.getOrDefault(context, 0.0) + score);

        sendScoreSidebar();
    }

    /**
     * 사이드바에 획득 점수 목록을 출력한다.
     */
    private void sendScoreSidebar() {
        user.getSidebarManager().clear();
        user.getSidebarManager().setName("§a✪ " + (int) scoreStreakSum);
        user.getSidebarManager().set(0, "");

        int i = 0;
        for (Map.Entry<String, Double> entry : scoreMap.entrySet())
            user.getSidebarManager().set(++i, StringUtils.center(MessageFormat.format("§f{0} §a[+{1}]",
                    entry.getKey(),
                    entry.getValue().intValue()), 30));
    }

    /**
     * 플레이어의 궁극기 게이지 백분율을 반환한다.
     *
     * @return 궁극기 게이지 백분율. 0~1 사이의 값
     */
    public double getUltGaugePercent() {
        return entity.getExp() >= 0.999 ? 1 : entity.getExp();
    }

    /**
     * 플레이어의 궁극기 게이지 백분율을 설정한다.
     *
     * @param value 궁극기 게이지 백분율. 0~1 사이의 값
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    public void setUltGaugePercent(double value) {
        Validate.inclusiveBetween(0.0, 1.0, value, "1 >= value >= 0 (%f)", value);

        if (value == 1) {
            value = 0.999;
            UltimateSkill skill = getSkill(combatant.getUltimateSkillInfo());
            if (!skill.isCooldownFinished())
                skill.setCooldown(Timespan.ZERO);
        }

        entity.setExp((float) value);
        entity.setLevel(value >= 0.999 ? 100 : (int) Math.floor(value * 100));
    }

    /**
     * 플레이어의 궁극기 게이지를 증가시킨다. (백분율).
     *
     * @param value 추가할 궁극기 게이지 백분율. 0~1 사이의 값
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    public void addUltGaugePercent(double value) {
        UltimateSkill skill = getSkill(combatant.getUltimateSkillInfo());
        if (skill.isDurationFinished())
            setUltGaugePercent(Math.min(getUltGaugePercent() + value, 1));
    }

    /**
     * 플레이어의 궁극기 게이지를 증가시킨다.
     *
     * @param value 추가할 궁극기 게이지. 0 이상의 값
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    public void addUltGauge(double value) {
        UltimateSkill skill = getSkill(combatant.getUltimateSkillInfo());
        int cost = skill.getCost();
        if (hasCore(Core.ULTIMATE))
            cost = (int) (cost * (100 - Core.ULTIMATE.getValue()) / 100.0);

        addUltGaugePercent(value / cost);
    }

    /**
     * 플레이어의 전투원을 설정하고 무기와 스킬을 초기화한다.
     *
     * @param combatantType 전투원
     */
    public void setCombatantType(@NonNull CombatantType combatantType) {
        reset();

        this.combatantType = combatantType;
        combatant = combatantType.getCombatant();

        damageModule.setMaxHealth(combatant.getHealth());
        damageModule.setHealth(combatant.getHealth());
        moveModule.getSpeedStatus().setBaseValue(GeneralConfig.getCombatConfig().getDefaultSpeed() * combatant.getSpeedMultiplier());

        resetHitboxes();
        initActions();

        combatant.onSet(this);

        addTask(user.applySkin(PlayerSkin.fromName(combatant.getSkinName())));
        addTask(new IntervalTask((LongConsumer) i ->
                entity.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, 1, 0, false, false), true),
                1, 10));
    }

    /**
     * 플레이어의 모든 상태를 재설정한다.
     */
    private void reset() {
        entity.getActivePotionEffects().forEach((potionEffect -> entity.removePotionEffect(potionEffect.getType())));
        entity.setAllowFlight(false);
        entity.setFlying(false);
        entity.setGravity(true);
        entity.setGameMode(GameMode.SURVIVAL);

        fovValue = 0;
        damageModule.clearShields();
        statusEffectModule.clear();
        setUltGaugePercent(0);
        setLowHealthScreenEffect(false);

        statusEffectModule.getResistanceStatus().clearModifiers();
        attackModule.getDamageMultiplierStatus().clearModifiers();
        healerModule.getHealMultiplierStatus().clearModifiers();
        damageModule.getDefenseMultiplierStatus().clearModifiers();
        moveModule.getSpeedStatus().clearModifiers();
        clearCores();

        weapon.remove();
        skillMap.values().forEach(Skill::remove);
        skillMap.clear();
        actionMap.clear();
    }

    /**
     * 플레이어의 동작 설정을 초기화한다.
     */
    private void initActions() {
        for (ActionKey actionKey : ActionKey.values())
            actionMap.put(actionKey, new TreeSet<>(Comparator.comparing(Action::getPriority).reversed()));

        actionMap.get(ActionKey.SWAP_HAND).add(new MeleeAttackAction(this));

        weapon = combatant.getWeaponInfo().createWeapon(this);
        for (ActionKey actionKey : weapon.getDefaultActionKeys())
            actionMap.get(actionKey).add(weapon);

        for (SkillInfo<?> skillInfo : combatant.getSkillInfos()) {
            Skill skill = skillInfo.createSkill(this);
            skillMap.put(skillInfo, skill);

            for (ActionKey actionKey : skill.getDefaultActionKeys())
                actionMap.get(actionKey).add(skill);
        }
    }

    /**
     * 플레이어의 히트박스를 기본 히트박스로 재설정한다.
     */
    public void resetHitboxes() {
        setHitboxes(Hitbox.createDefaultPlayerHitboxes(combatant.getHitboxMultiplier()));
        currentHitboxes = hitboxes;
    }

    /**
     * 지정한 동작 사용 키에 해당하는 동작을 사용한다.
     *
     * @param actionKey 동작 사용 키
     */
    public void useAction(@NonNull ActionKey actionKey) {
        actionMap.get(actionKey).forEach(action -> {
            if (isDead || action == null || statusEffectModule.hasRestriction(CombatRestriction.USE_ACTION))
                return;

            if (action instanceof MeleeAttackAction && action.canUse(actionKey)) {
                action.onUse(actionKey);
                return;
            }

            Weapon realWeapon = weapon;
            if (realWeapon instanceof Swappable && ((Swappable<?>) realWeapon).getSwapModule().isSwapped())
                realWeapon = ((Swappable<?>) realWeapon).getSwapModule().getSubweapon();

            if (action instanceof Weapon)
                handleUseWeapon(actionKey, realWeapon);
            else if (action instanceof Skill)
                handleUseSkill(actionKey, (Skill) action);
        });
    }

    /**
     * 무기 사용 로직을 처리한다.
     *
     * @param actionKey 동작 사용 키
     * @param weapon    무기
     */
    private void handleUseWeapon(@NonNull ActionKey actionKey, @NonNull Weapon weapon) {
        if (weapon instanceof FullAuto && (((FullAuto) weapon).getFullAutoModule().getFullAutoKey() == actionKey))
            ((FullAuto) weapon).getFullAutoModule().use();
        else if (weapon.canUse(actionKey))
            weapon.onUse(actionKey);
    }

    /**
     * 스킬 사용 로직을 처리한다.
     *
     * @param actionKey 동작 사용 키
     * @param skill     스킬
     */
    private void handleUseSkill(@NonNull ActionKey actionKey, @NonNull Skill skill) {
        if (skill.canUse(actionKey))
            skill.onUse(actionKey);
    }

    /**
     * 사용 중인 모든 동작을 강제로 취소시킨다.
     *
     * @param attacker 공격자
     */
    public void cancelAction(@Nullable CombatUser attacker) {
        weapon.cancel();
        cancelSkill(attacker);
    }

    /**
     * 사용 중인 모든 스킬을 강제로 취소시킨다.
     *
     * @param attacker 공격자
     */
    public void cancelSkill(@Nullable CombatUser attacker) {
        skillMap.values().forEach(skill -> {
            if (!skill.cancel())
                return;

            if (attacker != null && !isDead && skill instanceof UltimateSkill)
                attacker.addScore("궁극기 차단", CombatUser.ULT_BLOCK_KILL_SCORE);
        });
    }

    /**
     * 지정한 코어를 장착한 상태인지 확인한다.
     *
     * @param core 확인할 코어
     * @return 장착하고 있으면 {@code true} 반환
     */
    public boolean hasCore(@NonNull Core core) {
        return cores.contains(core);
    }

    /**
     * 지정한 코어를 장착한 코어 목록에 추가한다.
     *
     * @param core 추가할 코어
     * @return 추가 성공 시 {@code true} 반환
     */
    public boolean addCore(@NonNull Core core) {
        if (cores.size() >= CORE_INVENTORY_SLOT.length || !cores.add(core))
            return false;

        for (int i : CORE_INVENTORY_SLOT) {
            if (user.getGui().get(i) == null) {
                user.getGui().set(i, core.getSelectItem());
                break;
            }
        }

        if (core == Core.STRENGTH)
            attackModule.getDamageMultiplierStatus().addModifier(CORE_STRENGTH_MODIFIER);
        if (core == Core.RESISTANCE)
            damageModule.getDefenseMultiplierStatus().addModifier(CORE_RESISTANCE_MODIFIER);
        if (core == Core.SPEED)
            moveModule.getSpeedStatus().addModifier(CORE_SPEED_MODIFIER);
        if (core == Core.HEALING)
            healerModule.getHealMultiplierStatus().addModifier(CORE_HEALING_MODIFIER);
        if (core == Core.ENDURANCE)
            statusEffectModule.getResistanceStatus().addModifier(CORE_ENDURANCE_MODIFIER);

        return true;
    }

    /**
     * 지정한 코어를 장착한 코어 목록에서 제거한다.
     *
     * @param core 제거할 코어
     * @return 제거 성공 시 {@code true} 반환
     */
    public boolean removeCore(@NonNull Core core) {
        if (cores.isEmpty() || !cores.remove(core))
            return false;

        for (int i : CORE_INVENTORY_SLOT) {
            DefinedItem definedItem = user.getGui().get(i);
            if (definedItem == core.getSelectItem()) {
                user.getGui().remove(i);
                break;
            }
        }

        if (core == Core.STRENGTH)
            attackModule.getDamageMultiplierStatus().removeModifier(CORE_STRENGTH_MODIFIER);
        if (core == Core.RESISTANCE)
            damageModule.getDefenseMultiplierStatus().removeModifier(CORE_RESISTANCE_MODIFIER);
        if (core == Core.SPEED)
            moveModule.getSpeedStatus().removeModifier(CORE_SPEED_MODIFIER);
        if (core == Core.HEALING)
            healerModule.getHealMultiplierStatus().removeModifier(CORE_HEALING_MODIFIER);
        if (core == Core.ENDURANCE)
            statusEffectModule.getResistanceStatus().removeModifier(CORE_ENDURANCE_MODIFIER);

        return true;
    }

    /**
     * 장착한 코어 목록을 초기화한다.
     */
    private void clearCores() {
        cores.clear();

        for (int i : CORE_INVENTORY_SLOT)
            user.getGui().remove(i);
    }

    /**
     * 플레이어의 시야(yaw/pitch) 값을 설정한다.
     *
     * @param yaw   변경할 yaw
     * @param pitch 변경할 pitch
     * @see CombatUser#addYawAndPitch(double, double)
     */
    public void setYawAndPitch(double yaw, double pitch) {
        WrapperPlayServerPosition packet = new WrapperPlayServerPosition();

        packet.setX(0);
        packet.setY(0);
        packet.setZ(0);
        packet.setYaw((float) yaw);
        packet.setPitch((float) pitch);
        packet.setFlags(new HashSet<>(Arrays.asList(WrapperPlayServerPosition.PlayerTeleportFlag.X, WrapperPlayServerPosition.PlayerTeleportFlag.Y,
                WrapperPlayServerPosition.PlayerTeleportFlag.Z)));

        packet.sendPacket(entity.getPlayer());
    }

    /**
     * 플레이어의 시야(yaw/pitch) 값을 추가한다.
     *
     * @param yaw   추가할 yaw
     * @param pitch 추가할 pitch
     * @see CombatUser#setYawAndPitch(double, double)
     */
    public void addYawAndPitch(double yaw, double pitch) {
        WrapperPlayServerPosition packet = new WrapperPlayServerPosition();

        packet.setX(0);
        packet.setY(0);
        packet.setZ(0);
        packet.setYaw((float) yaw);
        packet.setPitch((float) pitch);
        packet.setFlags(new HashSet<>(Arrays.asList(WrapperPlayServerPosition.PlayerTeleportFlag.values())));

        packet.sendPacket(entity.getPlayer());
    }

    /**
     * 플레이어에게 근접 공격 애니메이션을 재생한다.
     *
     * @param amplifier 성급함 포션 효과 레벨
     * @param duration  지속시간
     * @param hand      왼손/오른손
     */
    @SuppressWarnings("deprecation")
    public void playMeleeAttackAnimation(int amplifier, @NonNull Timespan duration, @NonNull MainHand hand) {
        int durationTicks = (int) Math.min(Integer.MAX_VALUE, duration.toTicks());

        entity.removePotionEffect(PotionEffectType.FAST_DIGGING);
        entity.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, durationTicks, amplifier, false, false), true);

        WrapperPlayServerRemoveEntityEffect packet = new WrapperPlayServerRemoveEntityEffect();
        packet.setEntityID(entity.getEntityId());
        packet.setEffect(PotionEffectType.FAST_DIGGING);
        packet.broadcastPacket();

        WrapperPlayServerEntityEffect packet2 = new WrapperPlayServerEntityEffect();
        packet2.setEntityID(entity.getEntityId());
        packet2.setEffectID((byte) PotionEffectType.FAST_DIGGING.getId());
        packet2.setAmplifier((byte) amplifier);
        packet2.setDuration(durationTicks);
        packet2.setHideParticles(true);
        packet2.broadcastPacket();

        WrapperPlayServerAnimation packet3 = new WrapperPlayServerAnimation();
        packet3.setAnimation(hand == MainHand.RIGHT ? 0 : 3);
        packet3.setEntityID(entity.getEntityId());
        packet3.broadcastPacket();
    }

    /**
     * 전투 시스템의 메시지 포맷이 적용된 채팅 메시지를 반환한다.
     *
     * <p>인게임 채팅 및 전투원 대사에 사용한다.</p>
     *
     * @param message 메시지
     * @return 포맷이 적용된 메시지
     */
    @NonNull
    public String getFormattedChatMessage(@NonNull String message) {
        return MessageFormat.format("§f<{0}§l[§f{1} {0}{2}]§f{3}> {4}",
                (gameUser == null ? ChatColor.YELLOW : gameUser.getTeam().getType().getColor()),
                combatant.getIcon(),
                "§l" + combatant.getName(),
                entity.getName(),
                message);
    }

    /**
     * 수신 플레이어에게 전투원 대사 메시지를 전송한다.
     *
     * @param receiver 수신 플레이어
     * @param message  메시지
     */
    public void sendMentMessage(@NonNull CombatUser receiver, @NonNull String message) {
        receiver.getEntity().sendMessage(getFormattedChatMessage("§e" + message));
    }

    /**
     * 효과음 목록.
     */
    @UtilityClass
    private static final class SOUND {
        /** 추락 (낮음) */
        private static final SoundEffect FALL_LOW = new SoundEffect(
                SoundEffect.SoundInfo.builder(Sound.BLOCK_STONE_STEP).volume(0.3).pitch(0.9).pitchVariance(0.1).build());
        /** 추락 (중간) */
        private static final SoundEffect FALL_MID = new SoundEffect(
                SoundEffect.SoundInfo.builder(Sound.BLOCK_STONE_STEP).volume(0.4).pitch(0.9).pitchVariance(0.1).build(),
                SoundEffect.SoundInfo.builder(Sound.ENTITY_PLAYER_SMALL_FALL).volume(0.4).pitch(0.9).pitchVariance(0.1).build());
        /** 추락 (높음) */
        private static final SoundEffect FALL_HIGH = new SoundEffect(
                SoundEffect.SoundInfo.builder(Sound.BLOCK_STONE_STEP).volume(0.5).pitch(0.8).pitchVariance(0.1).build(),
                SoundEffect.SoundInfo.builder(Sound.BLOCK_STONE_STEP).volume(0.5).pitch(0.9).pitchVariance(0.1).build(),
                SoundEffect.SoundInfo.builder(Sound.ENTITY_PLAYER_SMALL_FALL).volume(0.5).pitch(0.9).pitchVariance(0.1).build());
        /** 공격 */
        private static final SoundEffect ATTACK = new SoundEffect(
                SoundEffect.SoundInfo.builder(Sound.ENTITY_PLAYER_HURT).volume(0.8).pitch(1.4).build(),
                SoundEffect.SoundInfo.builder(Sound.ENTITY_PLAYER_BIG_FALL).volume(1).pitch(0.7).build());
        /** 치명타 */
        private static final SoundEffect ATTACK_CRIT = new SoundEffect(
                SoundEffect.SoundInfo.builder(Sound.ENTITY_PLAYER_BIG_FALL).volume(2).pitch(0.7).build(),
                SoundEffect.SoundInfo.builder(Sound.BLOCK_ANVIL_PLACE).volume(0.5).pitch(1.8).build());
        /** 처치 */
        private static final SoundEffect KILL = new SoundEffect(
                SoundEffect.SoundInfo.builder(Sound.ENTITY_EXPERIENCE_ORB_PICKUP).volume(2).pitch(1.25).build(),
                SoundEffect.SoundInfo.builder(Sound.ENTITY_EXPERIENCE_ORB_PICKUP).volume(1).pitch(1.25).build());
    }

    /**
     * 처치 기여자별 누적 피해량을 관리하는 클래스.
     */
    @NoArgsConstructor
    private static final class KillContributorManager {
        /** 처치 지원 점수 비율 */
        private static final double KILL_ASSIST_SCORE_RATIO = 0.2;
        /** 킬 기여자별 누적 피해량 목록. 처치 점수 분배에 사용함 (킬 기여자 : 누적 피해량 정보) */
        private final WeakHashMap<CombatUser, DamageInfo> damageMap = new WeakHashMap<>();

        @NonNull
        private WeakHashMap<CombatUser, DamageInfo> getDamageMap() {
            damageMap.entrySet().removeIf(entry -> entry.getKey().isRemoved() || entry.getValue().isExpired());
            return damageMap;
        }

        /**
         * 지정한 공격자의 누적 피해량을 추가한다.
         *
         * @param attacker 공격자
         * @param damage   피해량
         */
        private void addDamage(@NonNull CombatUser attacker, double damage) {
            DamageInfo damageInfo = getDamageMap().computeIfAbsent(attacker, k -> new DamageInfo());
            damageInfo.damage += damage;

            attacker.killHelperManager.getScoreInfoMap().keySet().forEach(target -> {
                DamageInfo targetDamageInfo = damageMap.computeIfAbsent(attacker, k -> new DamageInfo());
                targetDamageInfo.damage += damage * KILL_ASSIST_SCORE_RATIO;
            });
        }

        /**
         * 지정한 공격자의 남은 처치 기여 제한시간을 반환한다.
         *
         * @param attacker 공격자
         * @return 남은 처치 기여 제한시간
         */
        @NonNull
        private Timespan getRemainingTime(@NonNull CombatUser attacker) {
            DamageInfo damageInfo = getDamageMap().get(attacker);
            return damageInfo == null ? Timespan.ZERO : Timestamp.now().until(damageInfo.expiration);
        }

        /**
         * 지정한 공격자의 처치 기여 점수를 반환한다.
         *
         * @param attacker 공격자
         * @return {@code attacker}의 점수 / 모든 공격자의 점수 합×100
         */
        private int getScore(@NonNull CombatUser attacker) {
            DamageInfo damageInfo = getDamageMap().get(attacker);
            double total = damageMap.values().stream().mapToDouble(targetDamageInfo -> targetDamageInfo.damage).sum();

            return (int) Math.round(damageInfo == null ? 0 : (damageInfo.damage / total * 100));
        }

        /**
         * 킬 기여자 목록을 초기화한다.
         */
        private void clear() {
            damageMap.clear();
        }

        /**
         * 누적 피해량(킬 기여) 정보 클래스.
         */
        @NoArgsConstructor
        private static final class DamageInfo {
            /** 종료 시점 */
            private final Timestamp expiration = Timestamp.now().plus(GeneralConfig.getCombatConfig().getDamageSumTimeLimit());
            /** 누적 피해량 */
            private double damage = 0;

            /**
             * 처치 기여 제한시간이 끝났는지 확인한다.
             *
             * @return 제한시간 종료 여부
             */
            private boolean isExpired() {
                return expiration.isBefore(Timestamp.now());
            }
        }
    }

    /**
     * 자신의 처치 지원자를 관리하는 클래스.
     */
    @NoArgsConstructor
    private static final class KillHelperManager {
        /** 처치 지원자별 지원 점수 목록 (지원자 : 지원 점수 정보) */
        private final WeakHashMap<CombatUser, ScoreInfo> scoreInfoMap = new WeakHashMap<>();

        @NonNull
        private WeakHashMap<CombatUser, ScoreInfo> getScoreInfoMap() {
            scoreInfoMap.entrySet().removeIf(entry -> entry.getKey().isRemoved() || entry.getValue().isExpired());
            return scoreInfoMap;
        }

        /**
         * 지정한 처치 지원자의 지원 점수를 추가한다.
         *
         * @param helper     지원자
         * @param action     사용한 동작
         * @param score      지원 점수
         * @param expiration 제한시간 종료 시점
         */
        private void addHelper(@NonNull CombatUser helper, @NonNull Action action, double score, @NonNull Timestamp expiration) {
            ScoreInfo scoreInfo = scoreInfoMap.computeIfAbsent(helper, k -> new ScoreInfo());
            scoreInfo.scoreMap.put(action, Pair.of(score, expiration));
        }

        /**
         * 지원 점수 정보 클래스.
         */
        @NoArgsConstructor
        private static final class ScoreInfo {
            /** (동작 : (지원 점수 : 종료 시점)) */
            private final WeakHashMap<Action, Pair<Double, Timestamp>> scoreMap = new WeakHashMap<>();

            /**
             * 지원 점수 목록을 반환한다.
             *
             * @return 지원 점수 목록
             */
            private double @NonNull [] getScores() {
                return scoreMap.values().stream().mapToDouble(Pair::getLeft).toArray();
            }

            /**
             * 처치 지원 제한시간이 끝났는지 확인한다.
             *
             * @return 제한시간 종료 여부
             */
            private boolean isExpired() {
                return scoreMap.values().stream().allMatch(info -> info.getRight().isBefore(Timestamp.now()));
            }
        }
    }
}
