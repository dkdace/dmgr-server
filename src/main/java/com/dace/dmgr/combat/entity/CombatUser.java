package com.dace.dmgr.combat.entity;

import com.comphenix.packetwrapper.*;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.dace.dmgr.DMGR;
import com.dace.dmgr.GeneralConfig;
import com.dace.dmgr.GlobalLocation;
import com.dace.dmgr.combat.CombatEffectUtil;
import com.dace.dmgr.combat.Core;
import com.dace.dmgr.combat.FreeCombat;
import com.dace.dmgr.combat.action.Action;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.MeleeAttackAction;
import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import com.dace.dmgr.combat.action.info.PassiveSkillInfo;
import com.dace.dmgr.combat.action.info.SkillInfo;
import com.dace.dmgr.combat.action.info.WeaponInfo;
import com.dace.dmgr.combat.action.skill.ActiveSkill;
import com.dace.dmgr.combat.action.skill.Skill;
import com.dace.dmgr.combat.action.skill.UltimateSkill;
import com.dace.dmgr.combat.action.weapon.FullAuto;
import com.dace.dmgr.combat.action.weapon.Swappable;
import com.dace.dmgr.combat.action.weapon.Weapon;
import com.dace.dmgr.combat.character.Character;
import com.dace.dmgr.combat.character.CharacterType;
import com.dace.dmgr.combat.character.silia.action.SiliaA3Info;
import com.dace.dmgr.combat.entity.module.*;
import com.dace.dmgr.combat.entity.temporary.SummonEntity;
import com.dace.dmgr.combat.interaction.DamageType;
import com.dace.dmgr.combat.interaction.FixedPitchHitbox;
import com.dace.dmgr.combat.interaction.HasCritHitbox;
import com.dace.dmgr.combat.interaction.Hitbox;
import com.dace.dmgr.game.GameUser;
import com.dace.dmgr.item.StaticItem;
import com.dace.dmgr.user.User;
import com.dace.dmgr.user.UserData;
import com.dace.dmgr.util.FireworkEffect;
import com.dace.dmgr.util.*;
import com.dace.dmgr.util.task.DelayTask;
import com.dace.dmgr.util.task.IntervalTask;
import com.dace.dmgr.util.task.TaskUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.boss.BarColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.text.MessageFormat;
import java.util.*;
import java.util.function.LongConsumer;
import java.util.function.LongPredicate;
import java.util.stream.Collectors;

/**
 * 전투 시스템의 플레이어 정보를 관리하는 클래스.
 */
public final class CombatUser extends AbstractCombatEntity<Player> implements Healable, Attacker, Healer, HasCritHitbox, Jumpable {
    /** 궁극기 차단 점수 */
    private static final int ULT_BLOCK_KILL_SCORE = 50;
    /** 결정타 점수 */
    private static final int FINAL_HIT_SCORE = 20;
    /** 추락사 점수 */
    private static final int FALL_ZONE_KILL_SCORE = 30;
    /** 연속 처치 점수 */
    private static final int KILLSTREAK_SCORE = 25;
    /** 처치 지원 점수 비율 */
    private static final double KILL_SUPPORT_SCORE_RATIO = 0.2;
    /** 최대 코어 개수 */
    private static final int MAX_CORE_AMOUNT = 3;
    /** 킬 로그 보스바 ID */
    private static final String COMBAT_KILL_BOSSBAR_ID = "CombatKillBossBar";

    /** 넉백 모듈 */
    @NonNull
    @Getter
    private final KnockbackModule knockbackModule;
    /** 상태 효과 모듈 */
    @NonNull
    @Getter
    private final StatusEffectModule statusEffectModule;
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
    /** 이동 모듈 */
    @NonNull
    @Getter
    private final JumpModule moveModule;

    /** 유저 정보 객체 */
    @NonNull
    @Getter
    private final User user;
    /** 게임 유저 객체. {@code null}이면 게임에 참여중이지 않음을 나타냄 */
    @Nullable
    @Getter
    private final GameUser gameUser;

    /** 치명타 히트박스 객체 */
    @NonNull
    @Getter
    private final Hitbox critHitbox;
    /** 킬 기여자별 피해량 목록. 처치 점수 분배에 사용한다. (킬 기여자 : 누적 피해량) */
    private final HashMap<CombatUser, Double> damageMap = new HashMap<>();
    /** 적 처치를 지원하는 플레이어 목록 (기여자 : (점수 ID : 지원 점수)) */
    private final HashMap<CombatUser, HashMap<String, Double>> killAssistMap = new HashMap<>();
    /** 동작 사용 키 매핑 목록 (동작 사용 키 : 동작) */
    private final EnumMap<ActionKey, TreeSet<Action>> actionMap = new EnumMap<>(ActionKey.class);
    /** 스킬 객체 목록 (스킬 정보 : 스킬) */
    private final HashMap<SkillInfo<? extends Skill>, Skill> skillMap = new HashMap<>();
    /** 획득 점수 목록 (항목 : 획득 점수) */
    private final LinkedHashMap<String, Double> scoreMap = new LinkedHashMap<>();
    /** 장착한 코어 목록 */
    private final EnumSet<Core> cores = EnumSet.noneOf(Core.class);
    /** 임시 히트박스 객체 목록 */
    @Nullable
    @Setter
    private Hitbox @Nullable [] temporaryHitboxes;
    /** 누적 자가 피해량. 자가 피해 치유 시 궁극기 충전 방지를 위해 사용한다. */
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
    /** 무기 객체 */
    @Nullable
    private Weapon weapon;
    /** 연사 무기 사용을 처리하는 태스크 */
    @Nullable
    private IntervalTask fullAutoTask;

    /** 선택한 전투원 종류 */
    @Nullable
    @Getter
    private CharacterType characterType;
    /** 선택한 전투원 */
    @Nullable
    private Character character;
    /** 선택한 전투원 기록 정보 */
    @Nullable
    private UserData.CharacterRecord characterRecord;
    /** 사망 대사 홀로그램 */
    @Nullable
    private TextHologram deathMentHologram;

    /**
     * 전투 시스템의 플레이어 인스턴스를 생성한다.
     *
     * @param user     대상 플레이어
     * @param gameUser 대상 게임 유저 객체
     * @throws IllegalStateException 해당 {@code user}의 CombatUser가 이미 존재하면 발생
     */
    private CombatUser(@NonNull User user, @Nullable GameUser gameUser) {
        super(user.getPlayer(), user.getPlayer().getName(), gameUser == null ? null : gameUser.getGame(),
                new FixedPitchHitbox(user.getPlayer().getLocation(), 0.5, 0.7, 0.3, 0, 0, 0, 0, 0.35, 0),
                new FixedPitchHitbox(user.getPlayer().getLocation(), 0.8, 0.7, 0.45, 0, 0, 0, 0, 1.05, 0),
                new Hitbox(user.getPlayer().getLocation(), 0.45, 0.35, 0.45, 0, 0.225, 0, 0, 1.4, 0),
                new Hitbox(user.getPlayer().getLocation(), 0.45, 0.1, 0.45, 0, 0.4, 0, 0, 1.4, 0)
        );
        this.user = user;
        this.gameUser = gameUser;

        knockbackModule = new KnockbackModule(this);
        statusEffectModule = new StatusEffectModule(this);
        attackModule = new AttackModule(this);
        healerModule = new HealerModule(this);
        damageModule = new HealModule(this, true, true, true, 0, 1000);
        moveModule = new JumpModule(this, GeneralConfig.getCombatConfig().getDefaultSpeed());
        critHitbox = hitboxes[3];
        user.clearSidebar();
    }

    /**
     * 전투 시스템의 플레이어 인스턴스를 생성한다.
     *
     * @param gameUser 대상 게임 유저 객체
     * @throws IllegalStateException 해당 {@code user}의 CombatUser가 이미 존재하면 발생
     */
    public CombatUser(@NonNull GameUser gameUser) {
        this(gameUser.getUser(), gameUser);
    }

    /**
     * 전투 시스템의 플레이어 인스턴스를 생성한다.
     *
     * @param user 대상 플레이어
     * @throws IllegalStateException 해당 {@code user}의 CombatUser가 이미 존재하면 발생
     */
    public CombatUser(@NonNull User user) {
        this(user, null);
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

    @Override
    public void activate() {
        reset();
        super.activate();
    }

    @Override
    @NonNull
    public Hitbox @NonNull [] getHitboxes() {
        return temporaryHitboxes == null ? super.getHitboxes() : temporaryHitboxes;
    }

    @Override
    protected void onTick(long i) {
        if (!isActivated)
            return;
        Validate.notNull(character);

        character.onTick(this, i);

        if (!entity.hasPotionEffect(PotionEffectType.FAST_DIGGING)) {
            entity.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING,
                    Integer.MAX_VALUE, 39, false, false), true);

            WrapperPlayServerEntityEffect packet = new WrapperPlayServerEntityEffect();
            packet.setEntityID(entity.getEntityId());
            packet.setEffectID((byte) PotionEffectType.FAST_DIGGING.getId());
            packet.setAmplifier((byte) 39);
            packet.setDuration(-1);
            packet.setHideParticles(true);
            packet.broadcastPacket();
        }

        hitboxes[2].setAxisOffsetY(entity.isSneaking() ? 1.15 : 1.4);
        hitboxes[3].setAxisOffsetY(entity.isSneaking() ? 1.15 : 1.4);

        if (!isDead())
            onTickLive(i);

        entity.setAllowFlight(canFly());
        setLowHealthScreenEffect(damageModule.isLowHealth() || isDead());
        setCanSprint();
        adjustWalkSpeed();
    }

    /**
     * 플레이어의 이동 속도를 조정한다.
     */
    private void adjustWalkSpeed() {
        Validate.notNull(character);

        double speed = Math.max(0, GeneralConfig.getCombatConfig().getDefaultSpeed() * character.getSpeedMultiplier());

        if (entity.isSprinting()) {
            speed *= 0.88;
            if (!entity.isOnGround())
                speed *= speed / GeneralConfig.getCombatConfig().getDefaultSpeed();
        }

        moveModule.getSpeedStatus().setBaseValue(speed);
        entity.setFlySpeed((float) (moveModule.getSpeedStatus().getValue() * 0.4));
    }

    /**
     * 플레이어가 생존 중일 때 매 틱마다 실행할 작업.
     *
     * @param i 인덱스
     */
    private void onTickLive(long i) {
        Validate.notNull(character);

        user.sendActionBar(character.getActionbarString(this));

        checkHealPack();
        checkUltPack();
        checkJumpPad();
        checkFallZone();
        onFootstep();

        if (i % 10 == 0)
            addUltGauge(GeneralConfig.getCombatConfig().getIdleUltChargePerSecond() / 2.0);

        if (damageModule.isLowHealth())
            CombatEffectUtil.playBleedingEffect(null, this, 0);

        if (i % 20 == 0) {
            if (hasCore(Core.REGENERATION))
                damageModule.heal(this, damageModule.getMaxHealth() * Core.REGENERATION.getValues()[0] / 100.0, false);

            Validate.notNull(characterRecord);

            if (gameUser != null && gameUser.getSpawnRegionTeam() == null)
                characterRecord.addPlayTime();
        }
    }

    /**
     * 현재 위치의 힐 팩을 확인 및 사용한다.
     */
    private void checkHealPack() {
        Location location = entity.getLocation().subtract(0, 0.5, 0).getBlock().getLocation();
        if (location.getBlock().getType() != GeneralConfig.getCombatConfig().getHealPackBlock())
            return;

        GlobalLocation healPackLocation = new GlobalLocation(location.getX(), location.getY(), location.getZ());

        if (CooldownUtil.getCooldown(healPackLocation, Cooldown.HEAL_PACK.id) > 0)
            return;
        if (damageModule.getHealth() == damageModule.getMaxHealth())
            return;

        useHealPack(healPackLocation, location);
    }

    /**
     * 힐 팩 사용 시 실행할 작업.
     *
     * @param healPackLocation 힐 팩 위치
     * @param location         실제 블록 위치
     */
    private void useHealPack(@NonNull GlobalLocation healPackLocation, @NonNull Location location) {
        Validate.notNull(character);

        CooldownUtil.setCooldown(healPackLocation, Cooldown.HEAL_PACK.id, Cooldown.HEAL_PACK.duration);
        damageModule.heal(this, GeneralConfig.getCombatConfig().getHealPackHeal(), false);
        character.onUseHealPack(this);

        SOUND.HEAL_PACK.play(entity.getLocation());
        showBlockHologram(healPackLocation, location, Cooldown.HEAL_PACK);
    }

    /**
     * 현재 위치의 궁극기 팩을 확인 및 사용한다.
     */
    private void checkUltPack() {
        if (game != null && game.getRemainingTime() > game.getGamePlayMode().getPlayDuration() - GeneralConfig.getGameConfig().getUltPackActivationSeconds())
            return;

        Location location = entity.getLocation().subtract(0, 0.5, 0).getBlock().getLocation();
        if (location.getBlock().getType() != GeneralConfig.getCombatConfig().getUltPackBlock())
            return;

        GlobalLocation ultPackLocation = new GlobalLocation(location.getX(), location.getY(), location.getZ());

        if (CooldownUtil.getCooldown(ultPackLocation, Cooldown.ULT_PACK.id) > 0)
            return;
        if (getUltGaugePercent() == 1)
            return;

        useUltPack(ultPackLocation, location);
    }

    /**
     * 궁극기 팩 사용 시 실행할 작업.
     *
     * @param ultPackLocation 궁극기 팩 위치
     * @param location        실제 블록 위치
     */
    private void useUltPack(@NonNull GlobalLocation ultPackLocation, @NonNull Location location) {
        CooldownUtil.setCooldown(ultPackLocation, Cooldown.ULT_PACK.id, Cooldown.ULT_PACK.duration);
        addUltGauge(GeneralConfig.getCombatConfig().getUltPackCharge());

        PARTICLE.ULT_PACK.play(location.clone().add(0.5, 1.1, 0.5));
        SOUND.ULT_PACK.play(entity.getLocation());
        showBlockHologram(ultPackLocation, location, Cooldown.ULT_PACK);
    }

    /**
     * 기능 블록(힐 팩 및 궁극기 팩)의 홀로그램을 표시한다.
     *
     * @param blockLocation 기능 블록 위치
     * @param location      실제 블록 위치
     * @param blockCooldown 쿨타임 ID
     */
    private void showBlockHologram(@NonNull GlobalLocation blockLocation, @NonNull Location location, @NonNull Cooldown blockCooldown) {
        Location hologramLoc = location.add(0.5, 1.7, 0.5);
        TextHologram textHologram = new TextHologram(hologramLoc, player -> LocationUtil.canPass(player.getEyeLocation(), hologramLoc));

        boolean isGame = game != null;
        new IntervalTask(i -> {
            long cooldown = CooldownUtil.getCooldown(blockLocation, blockCooldown.id);
            if (cooldown <= 0)
                return false;
            if (isGame && game.isDisposed())
                return false;

            textHologram.setContent(MessageFormat.format("§f§l[ §6{0} {1} §f§l]", TextIcon.COOLDOWN, Math.ceil(cooldown / 20.0)));

            return true;
        }, textHologram::dispose, 5);
    }

    /**
     * 현재 위치의 점프대를 확인 및 사용한다.
     */
    private void checkJumpPad() {
        Location location = entity.getLocation().subtract(0, 0.5, 0).getBlock().getLocation();
        if (location.getBlock().getType() != GeneralConfig.getCombatConfig().getJumpPadBlock())
            return;
        if (CooldownUtil.getCooldown(this, Cooldown.JUMP_PAD.id) > 0)
            return;

        CooldownUtil.setCooldown(this, Cooldown.JUMP_PAD.id, Cooldown.JUMP_PAD.duration);

        moveModule.push(new Vector(0, GeneralConfig.getCombatConfig().getJumpPadVelocity(), 0), true);
        SOUND.JUMP_PAD.play(entity.getLocation());
    }

    /**
     * 현재 위치의 낙사 구역을 확인 및 낙사 처리한다.
     */
    private void checkFallZone() {
        Location location = entity.getLocation().subtract(0, 0.5, 0).getBlock().getLocation();
        if (location.getBlock().getType() != GeneralConfig.getCombatConfig().getFallZoneBlock())
            return;

        CooldownUtil.setCooldown(this, Cooldown.FALL_ZONE.id, 10);
        onDeath(null);
    }

    /**
     * 매 걸음마다 실행할 작업.
     *
     * <p>주로 발소리 재생에 사용한다.</p>
     */
    private void onFootstep() {
        Validate.notNull(character);

        Location oldLoc = entity.getLocation();
        double fallDistance = entity.getFallDistance();

        TaskUtil.addTask(this, new DelayTask(() -> {
            footstepDistance += oldLoc.distance(entity.getLocation());
            if (!entity.isOnGround() || footstepDistance <= 1.6 || (characterType == CharacterType.SILIA
                    && !getSkill(SiliaA3Info.getInstance()).isDurationFinished()))
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
                if (fallDistance > 6)
                    SOUND.FALL_HIGH.play(entity.getLocation(), volume);
                else if (fallDistance > 3)
                    SOUND.FALL_MID.play(entity.getLocation(), volume);
                else
                    SOUND.FALL_LOW.play(entity.getLocation(), volume);
            }

            character.onFootstep(this, volume);
        }, 1));
    }

    @Override
    public void dispose() {
        super.dispose();

        if (weapon != null)
            weapon.dispose();
        skillMap.forEach((skillInfo, skill) -> skill.dispose());

        if (deathMentHologram != null)
            deathMentHologram.dispose();

        if (DMGR.getPlugin().isEnabled())
            user.resetSkin();

        reset();
    }

    @Override
    public boolean canBeTargeted() {
        if (!isActivated)
            return false;
        if (gameUser != null && gameUser.getSpawnRegionTeam() == gameUser.getTeam())
            return false;
        if (LocationUtil.isInRegion(entity, "BattlePVP"))
            return false;

        return !isDead();
    }

    @Override
    @NonNull
    public String getTeamIdentifier() {
        return gameUser == null || gameUser.getTeam() == null ? name : gameUser.getTeam().getName();
    }

    /**
     * 플레이어의 달리기 가능 여부를 설정한다.
     */
    private void setCanSprint() {
        WrapperPlayServerUpdateHealth packet = new WrapperPlayServerUpdateHealth();

        packet.setHealth((float) entity.getHealth());
        packet.setFood(canSprint() ? 19 : 2);

        packet.sendPacket(entity);

        if (isDead())
            entity.setSprinting(false);
    }

    /**
     * 플레이어가 달리기를 할 수 있는지 확인한다.
     *
     * @return 달리기 가능 여부
     */
    private boolean canSprint() {
        Validate.notNull(character);
        return !isDead()
                && character.canSprint(this)
                && !statusEffectModule.hasAnyRestriction(CombatRestrictions.SPRINT);
    }

    /**
     * 플레이어가 비행할 수 있는지 확인한다.
     *
     * @return 비행 가능 여부
     */
    private boolean canFly() {
        Validate.notNull(character);
        return !isDead()
                && character.canFly(this)
                && !statusEffectModule.hasAnyRestriction(CombatRestrictions.FLY);
    }

    @Override
    public boolean canJump() {
        if (!isActivated)
            return true;
        Validate.notNull(character);

        return character.canJump(this);
    }

    @Override
    public void onAttack(@NonNull Damageable victim, double damage, @NonNull DamageType damageType, boolean isCrit, boolean isUlt) {
        if (!isActivated)
            return;
        Validate.notNull(character);

        if (this == victim)
            return;

        isUlt = isUlt && character.onAttack(this, victim, damage, damageType, isCrit);

        playAttackEffect(isCrit);

        if (victim.getDamageModule().isUltProvider() && isUlt)
            addUltGauge(damage);

        if (victim instanceof CombatUser) {
            if (hasCore(Core.HEALTH_DRAIN))
                damageModule.heal(this, damage * Core.HEALTH_DRAIN.getValues()[0] / 100.0, false);

            if (gameUser != null)
                gameUser.setDamage(gameUser.getDamage() + damage);
        }
    }

    /**
     * 공격했을 때 효과를 재생한다.
     *
     * @param isCrit 치명타 여부
     */
    private void playAttackEffect(boolean isCrit) {
        if (CooldownUtil.getCooldown(this, Cooldown.HIT_SOUND.id) > 0)
            return;

        CooldownUtil.setCooldown(this, Cooldown.HIT_SOUND.id, Cooldown.HIT_SOUND.duration);
        if (isCrit) {
            user.sendTitle("", "§c§l×", 0, 2, 10);
            TaskUtil.addTask(this, new DelayTask(() -> SOUND.ATTACK_CRIT.play(entity), 2));
        } else {
            user.sendTitle("", "§f×", 0, 2, 10);
            TaskUtil.addTask(this, new DelayTask(() -> SOUND.ATTACK.play(entity), 2));
        }
    }

    @Override
    public void onDamage(@Nullable Attacker attacker, double damage, double reducedDamage, @NonNull DamageType damageType, @Nullable Location location,
                         boolean isCrit, boolean isUlt) {
        if (!isActivated)
            return;
        Validate.notNull(character);

        if (this == attacker) {
            selfHarmDamage += damage;
            return;
        }
        if (attacker == null)
            selfHarmDamage += damage;

        character.onDamage(this, attacker, damage, damageType, location, isCrit);

        if (attacker instanceof SummonEntity)
            attacker = ((SummonEntity<?>) attacker).getOwner();
        if (attacker instanceof CombatUser) {
            if (CooldownUtil.getCooldown(attacker, Cooldown.DAMAGE_SUM_TIME_LIMIT.id + this) == 0)
                damageMap.remove(attacker);
            CooldownUtil.setCooldown(attacker, Cooldown.DAMAGE_SUM_TIME_LIMIT.id + this, Cooldown.DAMAGE_SUM_TIME_LIMIT.duration);

            double sumDamage = damageMap.getOrDefault(attacker, 0.0);
            damageMap.put((CombatUser) attacker, sumDamage + damage);

            handleAttackerKillAssists((CombatUser) attacker, damage);
        }

        if (gameUser != null)
            gameUser.setDefend(gameUser.getDefend() + reducedDamage);
    }

    /**
     * 피해를 입었을 때 공격자를 지원하는 플레이어의 적 처치 기여를 처리한다.
     *
     * @param attacker 공격자
     * @param damage   피해량
     */
    private void handleAttackerKillAssists(@NonNull CombatUser attacker, double damage) {
        attacker.killAssistMap.keySet().removeIf(target -> {
            if (CooldownUtil.getCooldown(target, Cooldown.DAMAGE_SUM_TIME_LIMIT.id + this) == 0)
                damageMap.remove(target);

            if (CooldownUtil.getCooldown(attacker, Cooldown.KILL_SUPPORT_TIME_LIMIT.id + target) == 0)
                return true;

            CooldownUtil.setCooldown(target, Cooldown.DAMAGE_SUM_TIME_LIMIT.id + this, Cooldown.DAMAGE_SUM_TIME_LIMIT.duration);
            double sumDamage = damageMap.getOrDefault(target, 0.0);
            damageMap.put(target, sumDamage + damage * KILL_SUPPORT_SCORE_RATIO);

            return false;
        });
    }

    @Override
    public boolean canTakeDamage() {
        if (!isActivated)
            return false;
        return entity.getGameMode() == GameMode.SURVIVAL;
    }

    @Override
    public boolean canDie() {
        if (!isActivated)
            return false;
        return !LocationUtil.isInRegion(entity, "BattleTrain");
    }

    @Override
    public void onGiveHeal(@NonNull Healable target, double amount, boolean isUlt) {
        Validate.notNull(character);

        isUlt = isUlt && getSkill(character.getUltimateSkillInfo()).isDurationFinished() && character.onGiveHeal(this, target, amount);

        if (target.getDamageModule().isUltProvider() && isUlt) {
            double ultAmount = amount;

            if (target instanceof CombatUser && ((CombatUser) target).selfHarmDamage > 0)
                ultAmount = -((CombatUser) target).selfHarmDamage - amount;

            if (ultAmount > 0)
                addUltGauge(ultAmount);
        }

        if (gameUser != null && target instanceof CombatUser)
            gameUser.setHeal(gameUser.getHeal() + amount);
    }

    @Override
    public void onTakeHeal(@Nullable Healer provider, double amount, boolean isUlt) {
        if (!isActivated)
            return;
        Validate.notNull(character);

        character.onTakeHeal(this, provider, amount);
        selfHarmDamage -= amount;
        if (selfHarmDamage < 0)
            selfHarmDamage = 0;

        playTakeHealEffect(amount);
    }

    /**
     * 치유를 받았을 때 효과를 재생한다.
     *
     * @param amount 치유량
     */
    private void playTakeHealEffect(double amount) {
        if (amount >= 100 || amount / 100.0 > Math.random())
            PARTICLE.HEAL.play(entity.getLocation().add(0, entity.getHeight() + 0.3, 0), amount / 100.0);
    }

    @Override
    public void onKill(@NonNull Damageable victim) {
        if (!isActivated)
            return;
        Validate.notNull(character);
        Validate.notNull(characterRecord);

        if (this == victim)
            return;

        playKillEffect();
        if (victim instanceof CombatUser) {
            Validate.notNull(((CombatUser) victim).getCharacterType());

            ((CombatUser) victim).damageMap.keySet().removeIf(target ->
                    CooldownUtil.getCooldown(target, Cooldown.DAMAGE_SUM_TIME_LIMIT.id + victim) == 0);

            double totalDamage = ((CombatUser) victim).damageMap.values().stream().mapToDouble(Double::doubleValue).sum();
            double damage = ((CombatUser) victim).damageMap.getOrDefault(this, 0.0);
            int score = (int) Math.round((damage / totalDamage) * 100);

            character.onKill(this, victim, score, true);
            addScore(MessageFormat.format("§e{0}§f 처치", victim.getName()), score);
            addScore("결정타", FINAL_HIT_SCORE);

            if (CooldownUtil.getCooldown(this, Cooldown.KILL_STREAK_TIME_LIMIT.id) == 0)
                killStreak = 0;
            CooldownUtil.setCooldown(this, Cooldown.KILL_STREAK_TIME_LIMIT.id, Cooldown.KILL_STREAK_TIME_LIMIT.duration);
            if (killStreak++ > 0)
                addScore(killStreak + "명 연속 처치", KILLSTREAK_SCORE * (killStreak - 1.0));

            if (!((CombatUser) victim).getSkill(((CombatUser) victim).getCharacterType().getCharacter().getUltimateSkillInfo()).isDurationFinished())
                addScore("궁극기 차단", ULT_BLOCK_KILL_SCORE);

            sendPlayerKillMent((CombatUser) victim);
            ((CombatUser) victim).sendPlayerDeathMent(this);

            if (gameUser != null)
                gameUser.onKill(true);
        } else {
            character.onKill(this, victim, -1, true);

            addScore(MessageFormat.format("§e{0}§f {1}", victim.getName(), (victim.getDamageModule().isLiving() ? "처치" : "파괴")),
                    victim.getDamageModule().getScore());
        }
    }

    /**
     * 엔티티를 처치했을 때 효과를 재생한다.
     */
    private void playKillEffect() {
        user.sendTitle("", "§c" + TextIcon.POISON, 0, 2, 10);
        TaskUtil.addTask(this, new DelayTask(() -> SOUND.KILL.play(entity), 2));
    }

    /**
     * 사망 시 킬로그 보스바를 표시한다.
     */
    private void broadcastPlayerKillBossBar() {
        Validate.notNull(character);
        if (game == null)
            return;

        String attackerNames = damageMap.keySet().stream().map(CombatUser::getPlayerKillBossBarName)
                .collect(Collectors.joining(", "));
        String victimName = getPlayerKillBossBarName();

        for (GameUser targetGameUser : game.getGameUsers()) {
            targetGameUser.getUser().addBossBar(COMBAT_KILL_BOSSBAR_ID + this,
                    MessageFormat.format("{0} §4§l➡ {1}", attackerNames, victimName),
                    BarColor.WHITE, WrapperPlayServerBoss.BarStyle.PROGRESS, 0);

            TaskUtil.addTask(targetGameUser, new DelayTask(() ->
                    targetGameUser.getUser().removeBossBar(COMBAT_KILL_BOSSBAR_ID + this), Cooldown.KILL_LOG_DISPLAY_DURATION.duration));
        }
    }

    /**
     * 킬로그 보스바에 사용되는 플레이어의 이름을 반환한다.
     *
     * @return 이름
     */
    @NonNull
    private String getPlayerKillBossBarName() {
        Validate.notNull(character);

        ChatColor color = gameUser == null || gameUser.getTeam() == null ? ChatColor.WHITE : gameUser.getTeam().getColor();

        return MessageFormat.format("§f{0}{1}§l {2}", character.getIcon(), color, name);
    }

    /**
     * 적 처치 시 피격자에게 처치 대사를 전송한다.
     *
     * @param victim 피격자
     */
    private void sendPlayerKillMent(@NonNull CombatUser victim) {
        Validate.notNull(character);
        Validate.notNull(victim.getCharacterType());

        String[] ments = character.getKillMent(victim.getCharacterType());
        String ment = ments[DMGR.getRandom().nextInt(ments.length)];
        String message = getFormattedMessage("§l" + ment);

        entity.sendMessage(message);
        victim.getEntity().sendMessage(message);

        TaskUtil.addTask(this, new DelayTask(() ->
                victim.getUser().sendTypewriterTitle(String.valueOf(character.getIcon()), MessageFormat.format("§f\"{0}\"", ment)), 10));
    }

    /**
     * 사망 시 공격자에게 사망 대사를 전송하고 사망 위치에 홀로그램을 표시한다.
     *
     * @param attacker 공격자
     */
    private void sendPlayerDeathMent(@NonNull CombatUser attacker) {
        Validate.notNull(character);
        Validate.notNull(attacker.getCharacterType());

        String[] ments = character.getDeathMent(attacker.getCharacterType());
        String ment = ments[DMGR.getRandom().nextInt(ments.length)];
        String message = getFormattedMessage("§l" + ment);

        entity.sendMessage(message);
        attacker.getEntity().sendMessage(message);

        Location hologramLoc = entity.getLocation();
        for (int i = 0; i < 100; i++)
            if (!LocationUtil.isNonSolid(hologramLoc.subtract(0, 0.1, 0)))
                break;

        hologramLoc.add(0, 1.2, 0);
        deathMentHologram = new TextHologram(hologramLoc, player -> LocationUtil.canPass(player.getEyeLocation(), hologramLoc),
                MessageFormat.format("§f{0} \"{1}\"", character.getIcon(), ment));
    }

    @Override
    public void onDeath(@Nullable Attacker attacker) {
        if (!isActivated)
            return;
        Validate.notNull(character);
        Validate.notNull(characterRecord);
        Validate.notNull(weapon);

        if (isDead())
            return;

        character.onDeath(this, attacker);

        damageMap.keySet().removeIf(target -> CooldownUtil.getCooldown(target, Cooldown.DAMAGE_SUM_TIME_LIMIT.id + this) == 0);
        double totalDamage = damageMap.values().stream().mapToDouble(Double::doubleValue).sum();
        damageMap.forEach((target, damage) -> {
            Validate.notNull(target.character);

            target.killAssistMap.keySet().removeIf(targetAttacker ->
                    CooldownUtil.getCooldown(target, Cooldown.KILL_SUPPORT_TIME_LIMIT.id + targetAttacker) == 0);
            target.killAssistMap.forEach((targetAttacker, scores) -> {
                scores.keySet().removeIf(scoreId ->
                        CooldownUtil.getCooldown(target, Cooldown.KILL_SUPPORT_TIME_LIMIT.id + targetAttacker + scoreId) == 0);
                scores.values().forEach(supportScore -> targetAttacker.addScore("처치 지원", supportScore));
            });

            if (CooldownUtil.getCooldown(this, Cooldown.FALL_ZONE.id) > 0)
                target.addScore("추락사", FALL_ZONE_KILL_SCORE);

            if (target != (attacker instanceof SummonEntity ? ((SummonEntity<?>) attacker).getOwner() : attacker)) {
                int score = (int) Math.round(damage / totalDamage * 100);

                target.character.onKill(target, this, score, false);
                target.addScore(MessageFormat.format("§e{0}§f 처치 도움", name), score);
                target.playKillEffect();

                if (target.gameUser != null)
                    target.gameUser.onKill(false);
            }
        });

        broadcastPlayerKillBossBar();

        statusEffectModule.clearStatusEffect();
        damageModule.setHealth(damageModule.getMaxHealth());
        damageModule.clearShield();
        selfHarmDamage = 0;
        damageMap.clear();
        cancelAction(null);

        if (gameUser != null)
            gameUser.onDeath();

        respawn();
    }

    /**
     * 사망 후 리스폰 작업을 수행한다.
     */
    private void respawn() {
        Location deadLocation = (gameUser == null ? FreeCombat.getWaitLocation() : gameUser.getRespawnLocation()).add(0, 2, 0);
        user.teleport(deadLocation);

        long duration = Cooldown.RESPAWN.duration;
        if (hasCore(Core.RESURRECTION))
            duration = (long) (duration * (100 - Core.RESURRECTION.getValues()[0]) / 100.0);

        CooldownUtil.setCooldown(this, Cooldown.RESPAWN.id, gameUser == null ? 20 : duration);
        entity.setGameMode(GameMode.SPECTATOR);
        entity.setVelocity(new Vector());

        TaskUtil.addTask(this, new IntervalTask(i -> {
            long cooldown = CooldownUtil.getCooldown(CombatUser.this, Cooldown.RESPAWN.id);
            if (cooldown <= 0)
                return false;

            if (!user.isTypewriterTitlePrinting())
                user.sendTitle("§c§l죽었습니다!", MessageFormat.format("{0}초 후 부활합니다.",
                        String.format("%.1f", cooldown / 20.0)), 0, 5, 10);
            user.teleport(deadLocation);
            entity.setSpectatorTarget(null);

            return true;
        }, () -> {
            Validate.notNull(weapon);

            statusEffectModule.clearStatusEffect();
            entity.setGameMode(GameMode.SURVIVAL);

            weapon.reset();
            skillMap.forEach((skillInfo, skill) -> skill.reset());

            if (deathMentHologram != null) {
                deathMentHologram.dispose();
                deathMentHologram = null;
            }
        }, 1));
    }

    /**
     * 플레이어가 사망한 상태인지 확인한다.
     *
     * @return 사망 후 리스폰 대기 중이면 {@code true} 반환
     */
    public boolean isDead() {
        return CooldownUtil.getCooldown(this, Cooldown.RESPAWN.id) > 0;
    }

    /**
     * 공격자의 남은 적 처치 기여 제한시간을 반환한다.
     *
     * @param attacker 공격자
     * @return 남은 적 처치 기여 제한시간
     */
    public long getDamageSumRemainingTime(CombatUser attacker) {
        return CooldownUtil.getCooldown(attacker, Cooldown.DAMAGE_SUM_TIME_LIMIT.id + this);
    }

    /**
     * 플레이어의 왼손 또는 오른손의 위치를 반환한다.
     *
     * @param isRight 왼손/오른손. {@code false}로 지정 시 왼손, {@code true}로
     *                지정 시 오른손
     * @return 해당 위치
     */
    @NonNull
    public Location getArmLocation(boolean isRight) {
        return LocationUtil.getLocationFromOffset(entity.getEyeLocation().subtract(0, 0.4, 0), isRight ? 0.2 : -0.2, 0, 0);
    }

    @NonNull
    public Weapon getWeapon() {
        return Validate.notNull(weapon);
    }

    /**
     * 지정한 스킬 정보에 해당하는 스킬을 반환한다.
     *
     * @param skillInfo 스킬 정보 객체
     * @param <T>       {@link Skill}을 상속받는 스킬
     * @return 스킬 객체
     * @throws NullPointerException 해당하는 스킬이 존재하지 않으면 발생
     */
    @NonNull
    public <T extends Skill> T getSkill(@NonNull SkillInfo<T> skillInfo) {
        T skill = (T) skillMap.get(skillInfo);
        if (skill == null)
            throw new NullPointerException("일치하는 스킬이 존재하지 않음");

        return skill;
    }

    /**
     * 적 처치를 지원하는 플레이어를 추가한다.
     *
     * <p>플레이어가 적을 처치하면 해당 점수를 지정한 플레이어에게 지급한다.</p>
     *
     * @param combatUser 대상 플레이어
     * @param id         점수 ID
     * @param score      지원 점수. 0 이상의 값
     * @param duration   지속시간 (tick). -1로 설정 시 무한 지속
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    public void addKillAssist(@NonNull CombatUser combatUser, @NonNull String id, double score, long duration) {
        if (score < 0)
            throw new IllegalArgumentException("'score'가 0 이상이어야 함");
        if (duration < -1)
            throw new IllegalArgumentException("'duration'이 -1 이상이어야 함");
        if (duration == -1)
            duration = Long.MAX_VALUE;

        killAssistMap.putIfAbsent(combatUser, new HashMap<>());
        HashMap<String, Double> scores = killAssistMap.get(combatUser);
        scores.put(id, score);

        if (CooldownUtil.getCooldown(this, Cooldown.KILL_SUPPORT_TIME_LIMIT.id + combatUser) < duration)
            CooldownUtil.setCooldown(this, Cooldown.KILL_SUPPORT_TIME_LIMIT.id + combatUser, duration);
        if (CooldownUtil.getCooldown(this, Cooldown.KILL_SUPPORT_TIME_LIMIT.id + combatUser + id) < duration)
            CooldownUtil.setCooldown(this, Cooldown.KILL_SUPPORT_TIME_LIMIT.id + combatUser + id, duration);
    }

    /**
     * 플레이어의 전역 쿨타임이 끝났는지 확인한다.
     *
     * @return 전역 쿨타임 종료 여부
     */
    public boolean isGlobalCooldownFinished() {
        return CooldownUtil.getCooldown(this, Cooldown.ACTION_GLOBAL_COOLDOWN.id) == 0;
    }

    /**
     * 플레이어의 전역 쿨타임을 초기화한다.
     */
    public void resetGlobalCooldown() {
        CooldownUtil.setCooldown(this, Cooldown.ACTION_GLOBAL_COOLDOWN.id, 0);
        entity.setCooldown(SkillInfo.MATERIAL, 0);
        entity.setCooldown(WeaponInfo.MATERIAL, 0);
    }

    /**
     * 플레이어의 전역 쿨타임을 설정한다.
     *
     * @param cooldown 쿨타임 (tick). -1로 설정 시 무한 지속
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    public void setGlobalCooldown(int cooldown) {
        Validate.notNull(weapon);
        if (cooldown < -1)
            throw new IllegalArgumentException("'cooldown'이 -1 이상이어야 함");
        if (cooldown == -1)
            cooldown = Integer.MAX_VALUE;

        if (cooldown < CooldownUtil.getCooldown(this, Cooldown.ACTION_GLOBAL_COOLDOWN.id))
            return;

        CooldownUtil.setCooldown(this, Cooldown.ACTION_GLOBAL_COOLDOWN.id, cooldown);
        entity.setCooldown(SkillInfo.MATERIAL, cooldown);
        if (cooldown > weapon.getCooldown())
            entity.setCooldown(WeaponInfo.MATERIAL, cooldown);
    }

    /**
     * 지정한 양만큼 플레이어의 점수를 증가시키고 사이드바를 표시한다.
     *
     * <p>게임 참여 중이 아니면 점수 획득 표시만 한다.</p>
     *
     * @param context 항목
     * @param score   점수 증가량. 0 이상의 값
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    public void addScore(@NonNull String context, double score) {
        if (score < 0)
            throw new IllegalArgumentException("'score'가 0 이상이어야 함");
        if (gameUser != null)
            gameUser.setScore(gameUser.getScore() + score);

        if (CooldownUtil.getCooldown(this, Cooldown.SCORE_DISPLAY_DURATION.id) == 0) {
            CooldownUtil.setCooldown(this, Cooldown.SCORE_DISPLAY_DURATION.id, Cooldown.SCORE_DISPLAY_DURATION.duration);

            TaskUtil.addTask(this, new IntervalTask(i -> CooldownUtil.getCooldown(CombatUser.this, Cooldown.SCORE_DISPLAY_DURATION.id) > 0,
                    () -> {
                        scoreStreakSum = 0;
                        scoreMap.clear();
                        user.clearSidebar();
                    }, 1));
        } else
            CooldownUtil.setCooldown(this, Cooldown.SCORE_DISPLAY_DURATION.id, Cooldown.SCORE_DISPLAY_DURATION.duration);

        if (scoreMap.size() > 5)
            scoreMap.remove(scoreMap.keySet().iterator().next());

        scoreStreakSum += score;
        scoreMap.put(context, scoreMap.getOrDefault(context, 0.0) + score);

        user.clearSidebar();
        sendScoreSidebar();
    }

    /**
     * 사이드바에 획득 점수 목록을 출력한다.
     */
    private void sendScoreSidebar() {
        int i = 0;
        user.setSidebarName("§a+" + (int) scoreStreakSum);
        user.editSidebar(i++, "");
        for (Map.Entry<String, Double> entry : scoreMap.entrySet())
            user.editSidebar(i++, StringUtils.center(MessageFormat.format("§f{0} §a[+{1}]", entry.getKey(), entry.getValue().intValue()), 30));
    }

    /**
     * 플레이어의 궁극기 게이지 백분율을 반환한다.
     *
     * @return 궁극기 게이지. 0~1 사이의 값. (단위: 백분율)
     */
    public double getUltGaugePercent() {
        if (entity.getExp() >= 0.999)
            return 1;

        return entity.getExp();
    }

    /**
     * 플레이어의 궁극기 게이지 백분율을 설정한다.
     *
     * @param value 궁극기 게이지. 0~1 사이의 값. (단위: 백분율)
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    public void setUltGaugePercent(double value) {
        if (value < 0 || value > 1)
            throw new IllegalArgumentException("'value'가 0에서 1 사이여야 함");
        if (!isActivated || character == null)
            value = 0;

        if (value == 1) {
            value = 0.999;
            UltimateSkill skill = getSkill(character.getUltimateSkillInfo());
            if (!skill.isCooldownFinished())
                skill.setCooldown(0);
        }

        entity.setExp((float) value);
        entity.setLevel(value >= 0.999 ? 100 : (int) Math.floor(value * 100));
    }

    /**
     * 플레이어의 궁극기 게이지를 증가시킨다.
     *
     * @param value 추가할 궁극기 게이지. 0~1 사이의 값. (단위: 백분율)
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    public void addUltGaugePercent(double value) {
        Validate.notNull(character);

        UltimateSkill skill = getSkill(character.getUltimateSkillInfo());
        if (skill.isDurationFinished())
            setUltGaugePercent(Math.min(getUltGaugePercent() + value, 1));
    }

    /**
     * 플레이어의 궁극기 게이지를 증가시킨다.
     *
     * @param value 추가할 궁극기 게이지
     */
    public void addUltGauge(double value) {
        Validate.notNull(character);

        UltimateSkill skill = getSkill(character.getUltimateSkillInfo());
        int cost = skill.getCost();
        if (hasCore(Core.ULTIMATE))
            cost = (int) (cost * (100 - Core.ULTIMATE.getValues()[0]) / 100.0);

        addUltGaugePercent(value / cost);
    }

    /**
     * 플레이어의 전투원을 설정하고 무기와 스킬을 초기화한다.
     *
     * @param characterType 전투원
     */
    public void setCharacterType(@NonNull CharacterType characterType) {
        reset();
        Character realCharacter = characterType.getCharacter();

        damageModule.setMaxHealth(realCharacter.getHealth());
        damageModule.setHealth(realCharacter.getHealth());
        moveModule.getSpeedStatus().setBaseValue(GeneralConfig.getCombatConfig().getDefaultSpeed() * realCharacter.getSpeedMultiplier());

        double hitboxMultiplier = realCharacter.getHitboxMultiplier();
        for (Hitbox hitbox : hitboxes)
            hitbox.setSizeMultiplier(hitboxMultiplier);

        this.characterType = characterType;
        this.character = realCharacter;
        this.characterRecord = user.getUserData().getCharacterRecord(characterType);
        initActions();

        TaskUtil.addTask(this, user.applySkin(realCharacter.getSkinName()));
        TaskUtil.addTask(this, new IntervalTask((LongConsumer) i ->
                entity.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING,
                        1, 0, false, false), true), 1, 10));

        if (!isActivated)
            activate();
    }

    /**
     * 플레이어의 상태를 초기화한다.
     */
    private void reset() {
        entity.setHealth(entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
        entity.getInventory().setHeldItemSlot(4);
        entity.getActivePotionEffects().forEach((potionEffect ->
                entity.removePotionEffect(potionEffect.getType())));
        entity.setAllowFlight(false);
        entity.setFlying(false);
        entity.setGameMode(GameMode.SURVIVAL);

        fovValue = 0;
        damageModule.clearShield();
        statusEffectModule.clearStatusEffect();
        setUltGaugePercent(0);
        setLowHealthScreenEffect(false);

        knockbackModule.getResistanceStatus().clearModifier();
        statusEffectModule.getResistanceStatus().clearModifier();
        attackModule.getDamageMultiplierStatus().clearModifier();
        healerModule.getHealMultiplierStatus().clearModifier();
        damageModule.getDefenseMultiplierStatus().clearModifier();
        moveModule.getSpeedStatus().clearModifier();
        clearCores();
    }

    /**
     * 플레이어의 동작 설정을 초기화한다.
     */
    private void initActions() {
        Validate.notNull(character);

        if (weapon != null)
            weapon.dispose();
        skillMap.forEach((skillInfo, skill) -> skill.dispose());

        actionMap.clear();
        skillMap.clear();

        for (ActionKey actionKey : ActionKey.values())
            actionMap.put(actionKey, new TreeSet<>(Comparator.comparing(Action::getPriority).reversed()));
        actionMap.get(ActionKey.SWAP_HAND).add(new MeleeAttackAction(this));

        weapon = character.getWeaponInfo().createWeapon(this);
        for (ActionKey actionKey : weapon.getDefaultActionKeys())
            actionMap.get(actionKey).add(weapon);

        for (int i = 1; i <= 4; i++) {
            ActiveSkillInfo<?> activeSkillInfo = character.getActiveSkillInfo(i);
            PassiveSkillInfo<?> passiveSkillInfo = character.getPassiveSkillInfo(i);

            if (activeSkillInfo != null) {
                ActiveSkill skill = activeSkillInfo.createSkill(this);
                skillMap.put(activeSkillInfo, skill);
                for (ActionKey actionKey : skill.getDefaultActionKeys())
                    actionMap.get(actionKey).add(skill);
            }
            if (passiveSkillInfo != null) {
                Skill skill = passiveSkillInfo.createSkill(this);
                skillMap.put(passiveSkillInfo, skill);
                for (ActionKey actionKey : skill.getDefaultActionKeys())
                    actionMap.get(actionKey).add(skill);
            }
        }
    }

    /**
     * 지정한 동작 사용 키에 해당하는 동작을 사용한다.
     *
     * @param actionKey 동작 사용 키
     */
    public void useAction(@NonNull ActionKey actionKey) {
        if (!isActivated)
            return;
        Validate.notNull(weapon);

        TreeSet<Action> actions = actionMap.get(actionKey);
        actions.forEach(action -> {
            if (isDead() || action == null)
                return;
            if (statusEffectModule.hasAllRestrictions(CombatRestrictions.USE_ACTION))
                return;

            if (action instanceof MeleeAttackAction && action.canUse(actionKey)) {
                action.onUse(actionKey);
                return;
            }

            Weapon realWeapon = weapon;
            if (realWeapon instanceof Swappable && ((Swappable<?>) realWeapon).getSwapModule().getSwapState() == Swappable.SwapState.SECONDARY)
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
            handleUseFullAutoWeapon(actionKey, weapon);
        else if (weapon.canUse(actionKey))
            weapon.onUse(actionKey);
    }

    /**
     * 연사 무기의 사용 로직을 처리한다.
     *
     * @param actionKey 동작 사용 키
     * @param weapon    무기
     */
    private void handleUseFullAutoWeapon(@NonNull ActionKey actionKey, @NonNull Weapon weapon) {
        if (fullAutoTask == null || fullAutoTask.isDisposed())
            fullAutoTask = null;
        else if (CooldownUtil.getCooldown(weapon, Cooldown.WEAPON_FULLAUTO.id) > 0) {
            CooldownUtil.setCooldown(weapon, Cooldown.WEAPON_FULLAUTO.id, Cooldown.WEAPON_FULLAUTO.duration);
            return;
        }

        CooldownUtil.setCooldown(weapon, Cooldown.WEAPON_FULLAUTO.id, Cooldown.WEAPON_FULLAUTO.duration);

        if (fullAutoTask == null) {
            fullAutoTask = new IntervalTask(new LongPredicate() {
                int j = 0;

                @Override
                public boolean test(long i) {
                    if (CooldownUtil.getCooldown(weapon, Cooldown.WEAPON_FULLAUTO.id) == 0)
                        return false;

                    if (weapon.canUse(actionKey) && !isDead() && isGlobalCooldownFinished() && ((FullAuto) weapon).getFullAutoModule().isFireTick(i)) {
                        j++;
                        weapon.onUse(actionKey);
                    }

                    return true;
                }
            }, 1);
            TaskUtil.addTask(weapon.getTaskRunner(), fullAutoTask);
        }
    }

    /**
     * 스킬 사용 로직을 처리한다.
     *
     * @param actionKey 동작 사용 키
     * @param skill     스킬
     */
    private void handleUseSkill(@NonNull ActionKey actionKey, @NonNull Skill skill) {
        if (!skill.canUse(actionKey))
            return;

        skill.onUse(actionKey);
    }

    /**
     * 사용 중인 모든 동작을 강제로 취소시킨다.
     *
     * @param attacker 공격자
     */
    public void cancelAction(@Nullable CombatUser attacker) {
        Validate.notNull(weapon);

        if (weapon.isCancellable())
            weapon.onCancelled();
        cancelSkill(attacker);
    }

    /**
     * 사용 중인 모든 스킬을 강제로 취소시킨다.
     *
     * @param attacker 공격자
     */
    public void cancelSkill(@Nullable CombatUser attacker) {
        skillMap.forEach((skillInfo, skill) -> {
            if (skill.isCancellable()) {
                skill.onCancelled();
                if (skill instanceof UltimateSkill && attacker != null && !isDead())
                    attacker.addScore("궁극기 차단", CombatUser.ULT_BLOCK_KILL_SCORE);
            }
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
     * @return 추가 가능하면 {@code true} 반환
     */
    public boolean addCore(@NonNull Core core) {
        if (cores.size() >= MAX_CORE_AMOUNT)
            return false;

        cores.add(core);

        for (int i = 27; i <= 29; i++) {
            if (entity.getInventory().getItem(i) == null) {
                entity.getInventory().setItem(i, core.getSelectGuiItem().getItemStack());
                break;
            }
        }

        if (core == Core.STRENGTH)
            attackModule.getDamageMultiplierStatus().addModifier(Core.STRENGTH.getName(), Core.STRENGTH.getValues()[0]);
        if (core == Core.RESISTANCE)
            damageModule.getDefenseMultiplierStatus().addModifier(Core.RESISTANCE.getName(), Core.RESISTANCE.getValues()[0]);
        if (core == Core.SPEED)
            moveModule.getSpeedStatus().addModifier(Core.SPEED.getName(), Core.SPEED.getValues()[0]);
        if (core == Core.HEALING)
            healerModule.getHealMultiplierStatus().addModifier(Core.HEALING.getName(), Core.HEALING.getValues()[0]);
        if (core == Core.ENDURANCE)
            statusEffectModule.getResistanceStatus().addModifier(Core.ENDURANCE.getName(), Core.ENDURANCE.getValues()[0]);

        return true;
    }

    /**
     * 지정한 코어를 장착한 코어 목록에서 제거한다.
     *
     * @param core 제거할 코어
     * @return 제거 가능하면 {@code true} 반환
     */
    public boolean removeCore(@NonNull Core core) {
        if (cores.isEmpty())
            return false;

        cores.remove(core);

        for (int i = 27; i <= 29; i++) {
            ItemStack itemStack = entity.getInventory().getItem(i);
            if (itemStack != null && StaticItem.fromItemStack(itemStack) == core.getSelectGuiItem()) {
                entity.getInventory().setItem(i, null);
                break;
            }
        }

        if (core == Core.STRENGTH)
            attackModule.getDamageMultiplierStatus().removeModifier(Core.STRENGTH.getName());
        if (core == Core.RESISTANCE)
            damageModule.getDefenseMultiplierStatus().removeModifier(Core.RESISTANCE.getName());
        if (core == Core.SPEED)
            moveModule.getSpeedStatus().removeModifier(Core.SPEED.getName());
        if (core == Core.HEALING)
            healerModule.getHealMultiplierStatus().removeModifier(Core.HEALING.getName());
        if (core == Core.ENDURANCE)
            statusEffectModule.getResistanceStatus().removeModifier(Core.RESISTANCE.getName());

        return true;
    }

    /**
     * 장착한 코어 목록을 초기화한다.
     */
    private void clearCores() {
        cores.clear();

        for (int i = 27; i <= 30; i++)
            entity.getInventory().setItem(i, new ItemStack(Material.AIR));
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
     * 플레이어에게 근접 공격 애니메이션을 재생한다.
     *
     * @param amplifier 성급함 포션 효과 레벨
     * @param duration  지속시간 (tick). 0 이상의 값
     * @param isRight   왼손/오른손. {@code false}로 지정 시 왼손, {@code true}로
     *                  지정 시 오른손
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    public void playMeleeAttackAnimation(int amplifier, int duration, boolean isRight) {
        if (duration < 0)
            throw new IllegalArgumentException("'duration'이 0 이상이어야 함");

        entity.removePotionEffect(PotionEffectType.FAST_DIGGING);
        entity.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING,
                duration, amplifier, false, false), true);

        WrapperPlayServerRemoveEntityEffect packet = new WrapperPlayServerRemoveEntityEffect();
        packet.setEntityID(entity.getEntityId());
        packet.setEffect(PotionEffectType.FAST_DIGGING);
        packet.broadcastPacket();

        WrapperPlayServerEntityEffect packet2 = new WrapperPlayServerEntityEffect();
        packet2.setEntityID(entity.getEntityId());
        packet2.setEffectID((byte) PotionEffectType.FAST_DIGGING.getId());
        packet2.setAmplifier((byte) amplifier);
        packet2.setDuration(duration);
        packet2.setHideParticles(true);
        packet2.broadcastPacket();

        WrapperPlayServerAnimation packet3 = new WrapperPlayServerAnimation();
        packet3.setAnimation(isRight ? 0 : 3);
        packet3.setEntityID(entity.getEntityId());
        packet3.broadcastPacket();
    }

    /**
     * 전투 시스템의 메시지 포맷이 적용된 메시지를 반환한다.
     *
     * <p>인게임 채팅 및 전투원 대사에 사용한다.</p>
     *
     * @param message 메시지
     * @return 포맷이 적용된 메시지
     */
    @NonNull
    public String getFormattedMessage(@NonNull String message) {
        ChatColor color = gameUser == null || gameUser.getTeam() == null ? ChatColor.YELLOW : gameUser.getTeam().getColor();

        return MessageFormat.format("§f<{0}§l[{1}]§f{2}> §f{3}", color,
                (!isActivated() || character == null ? "미선택" : "§f" + character.getIcon() + " " + color + "§l" + character.getName()),
                entity.getName(), message);
    }

    /**
     * 쿨타임 ID 및 기본 지속시간 목록.
     */
    @AllArgsConstructor
    private enum Cooldown {
        /** 힐 팩 */
        HEAL_PACK("HealPack", GeneralConfig.getCombatConfig().getHealPackCooldown()),
        /** 궁극기 팩 */
        ULT_PACK("UltPack", GeneralConfig.getCombatConfig().getUltPackCooldown()),
        /** 점프대 */
        JUMP_PAD("JumpPad", 10),
        /** 적 타격 효과음 쿨타임 */
        HIT_SOUND("HitSound", 1),
        /** 적 처치 기여 (데미지 누적) 제한시간 */
        DAMAGE_SUM_TIME_LIMIT("DamageSumTimeLimit", GeneralConfig.getCombatConfig().getDamageSumTimeLimit()),
        /** 연속 처치 제한시간 */
        KILL_STREAK_TIME_LIMIT("KillstreakTimeLimit", GeneralConfig.getCombatConfig().getKillStreakTimeLimit()),
        /** 적 처치 지원 제한시간 */
        KILL_SUPPORT_TIME_LIMIT("KillSupportTimeLimit", 0),
        /** 리스폰 시간 */
        RESPAWN("Respawn", GeneralConfig.getCombatConfig().getRespawnTime()),
        /** 추락사 */
        FALL_ZONE("FallZone", 0),
        /** 동작 전역 쿨타임 */
        ACTION_GLOBAL_COOLDOWN("ActionGlobalCooldown", 0),
        /** 획득 점수 표시 유지시간 (tick) */
        SCORE_DISPLAY_DURATION("ScoreDisplayDuration", GeneralConfig.getCombatConfig().getScoreDisplayDuration()),
        /** 킬 로그 표시 유지시간 (tick) */
        KILL_LOG_DISPLAY_DURATION("KillLogDisplayDuration", GeneralConfig.getCombatConfig().getKillLogDisplayDuration()),
        /** 연사가 가능한 총기류의 쿨타임 */
        WEAPON_FULLAUTO("WeaponFullauto", 6);

        /** 쿨타임 ID */
        private final String id;
        /** 기본 지속시간 (tick) */
        private final long duration;
    }

    /**
     * 효과음 목록.
     */
    @UtilityClass
    private static final class SOUND {
        /** 힐 팩 */
        private static final SoundEffect HEAL_PACK = new SoundEffect(
                SoundEffect.SoundInfo.builder(Sound.ENTITY_ZOMBIE_VILLAGER_CONVERTED).volume(0.5).pitch(1.2).build());
        /** 궁극기 팩 */
        private static final SoundEffect ULT_PACK = new SoundEffect(
                SoundEffect.SoundInfo.builder(Sound.BLOCK_BREWING_STAND_BREW).volume(1).pitch(2).build(),
                SoundEffect.SoundInfo.builder(Sound.ITEM_ARMOR_EQUIP_GOLD).volume(1).pitch(1.3).build()
        );
        /** 점프대 */
        private static final SoundEffect JUMP_PAD = new SoundEffect(
                SoundEffect.SoundInfo.builder(org.bukkit.Sound.ENTITY_PLAYER_SMALL_FALL).volume(1.5).pitch(1.5).pitchVariance(0.1).build(),
                SoundEffect.SoundInfo.builder(org.bukkit.Sound.ENTITY_ITEM_PICKUP).volume(1.5).pitch(0.8).pitchVariance(0.05).build(),
                SoundEffect.SoundInfo.builder(org.bukkit.Sound.ENTITY_ITEM_PICKUP).volume(1.5).pitch(1.4).pitchVariance(0.05).build()
        );
        /** 추락 (낮음) */
        private static final SoundEffect FALL_LOW = new SoundEffect(
                SoundEffect.SoundInfo.builder(Sound.BLOCK_STONE_STEP).volume(0.3).pitch(0.9).pitchVariance(0.1).build());
        /** 추락 (중간) */
        private static final SoundEffect FALL_MID = new SoundEffect(
                SoundEffect.SoundInfo.builder(Sound.BLOCK_STONE_STEP).volume(0.4).pitch(0.9).pitchVariance(0.1).build(),
                SoundEffect.SoundInfo.builder(Sound.ENTITY_PLAYER_SMALL_FALL).volume(0.4).pitch(0.9).pitchVariance(0.1).build()
        );
        /** 추락 (높음) */
        private static final SoundEffect FALL_HIGH = new SoundEffect(
                SoundEffect.SoundInfo.builder(org.bukkit.Sound.BLOCK_STONE_STEP).volume(0.5).pitch(0.8).pitchVariance(0.1).build(),
                SoundEffect.SoundInfo.builder(org.bukkit.Sound.BLOCK_STONE_STEP).volume(0.5).pitch(0.9).pitchVariance(0.1).build(),
                SoundEffect.SoundInfo.builder(org.bukkit.Sound.ENTITY_PLAYER_SMALL_FALL).volume(0.5).pitch(0.9).pitchVariance(0.1).build()
        );
        /** 공격 */
        private static final SoundEffect ATTACK = new SoundEffect(
                SoundEffect.SoundInfo.builder(org.bukkit.Sound.ENTITY_PLAYER_HURT).volume(0.8).pitch(1.4).build(),
                SoundEffect.SoundInfo.builder(org.bukkit.Sound.ENTITY_PLAYER_BIG_FALL).volume(1).pitch(0.7).build()
        );
        /** 치명타 */
        private static final SoundEffect ATTACK_CRIT = new SoundEffect(
                SoundEffect.SoundInfo.builder(org.bukkit.Sound.ENTITY_PLAYER_BIG_FALL).volume(2).pitch(0.7).build(),
                SoundEffect.SoundInfo.builder(org.bukkit.Sound.BLOCK_ANVIL_PLACE).volume(0.5).pitch(1.8).build()
        );
        /** 처치 */
        private static final SoundEffect KILL = new SoundEffect(
                SoundEffect.SoundInfo.builder(org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP).volume(2).pitch(1.25).build(),
                SoundEffect.SoundInfo.builder(org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP).volume(1).pitch(1.25).build()
        );
    }

    /**
     * 입자 효과 목록.
     */
    @UtilityClass
    private static final class PARTICLE {
        /** 궁극기 팩 */
        private static final FireworkEffect ULT_PACK = FireworkEffect.builder(org.bukkit.FireworkEffect.Type.BALL, 48, 85, 251)
                .fadeColor(255, 255, 255).build();
        /** 회복 */
        private static final ParticleEffect HEAL = new ParticleEffect(
                ParticleEffect.NormalParticleInfo.builder(Particle.HEART)
                        .count(0, 0, 1)
                        .horizontalSpread(0.3).verticalSpread(0.1).build());
    }
}
