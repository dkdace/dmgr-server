package com.dace.dmgr.combat.entity;

import com.comphenix.packetwrapper.*;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.dace.dmgr.DMGR;
import com.dace.dmgr.GeneralConfig;
import com.dace.dmgr.combat.DamageType;
import com.dace.dmgr.combat.action.Action;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.MeleeAttackAction;
import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import com.dace.dmgr.combat.action.info.PassiveSkillInfo;
import com.dace.dmgr.combat.action.info.SkillInfo;
import com.dace.dmgr.combat.action.info.WeaponInfo;
import com.dace.dmgr.combat.action.skill.Skill;
import com.dace.dmgr.combat.action.skill.UltimateSkill;
import com.dace.dmgr.combat.action.weapon.FullAuto;
import com.dace.dmgr.combat.action.weapon.Reloadable;
import com.dace.dmgr.combat.action.weapon.Swappable;
import com.dace.dmgr.combat.action.weapon.Weapon;
import com.dace.dmgr.combat.character.Character;
import com.dace.dmgr.combat.character.CharacterType;
import com.dace.dmgr.combat.character.jager.action.JagerT1Info;
import com.dace.dmgr.combat.entity.module.*;
import com.dace.dmgr.combat.entity.statuseffect.StatusEffectType;
import com.dace.dmgr.combat.interaction.FixedPitchHitbox;
import com.dace.dmgr.combat.interaction.HasCritHitbox;
import com.dace.dmgr.combat.interaction.Hitbox;
import com.dace.dmgr.game.GamePlayMode;
import com.dace.dmgr.game.GameUser;
import com.dace.dmgr.game.map.GlobalLocation;
import com.dace.dmgr.item.ItemBuilder;
import com.dace.dmgr.item.StaticItem;
import com.dace.dmgr.user.User;
import com.dace.dmgr.util.*;
import com.dace.dmgr.util.task.DelayTask;
import com.dace.dmgr.util.task.IntervalTask;
import com.dace.dmgr.util.task.TaskUtil;
import fr.minuskube.netherboard.bukkit.BPlayerBoard;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.text.MessageFormat;
import java.util.*;
import java.util.function.Function;

/**
 * 전투 시스템의 플레이어 정보를 관리하는 클래스.
 */
public final class CombatUser extends AbstractCombatEntity<Player> implements Healable, Attacker, Healer, Living, HasCritHitbox, Jumpable {
    /** 기본 이동속도 */
    public static final double DEFAULT_SPEED = 0.24;
    /** 적 처치 기여 (데미지 누적) 제한시간 (tick) */
    public static final long DAMAGE_SUM_TIME_LIMIT = 10 * 20;
    /** 암살 보너스 (첫 공격 후 일정시간 안에 적 처치) 제한시간 (tick) */
    public static final long FASTKILL_TIME_LIMIT = (long) (2.5 * 20);
    /** 획득 점수 표시 유지시간 (tick) */
    public static final long SCORE_DISPLAY_DURATION = 100;

    /** 유저 정보 객체 */
    @NonNull
    @Getter
    private final User user;
    /** 게임 유저 객체. {@code null}이면 게임에 참여중이지 않음을 나타냄 */
    @Getter
    private final GameUser gameUser;
    /** 플레이어 사이드바 */
    private final BPlayerBoard sidebar;
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
    /** 피해 모듈 */
    @NonNull
    @Getter
    private final HealModule damageModule;
    /** 이동 모듈 */
    @NonNull
    @Getter
    private final JumpModule moveModule;
    /** 치명타 히트박스 객체 */
    @NonNull
    @Getter
    private final Hitbox critHitbox;
    /** 킬 기여자별 피해량 목록. 처치 점수 분배에 사용한다. (킬 기여자 : 누적 피해량) */
    private final HashMap<@NonNull CombatUser, Integer> damageMap = new HashMap<>();
    /** 동작 사용 키 매핑 목록 (동작 사용 키 : 동작) */
    @NonNull
    @Getter
    private final EnumMap<@NonNull ActionKey, TreeSet<Action>> actionMap = new EnumMap<>(ActionKey.class);
    /** 스킬 객체 목록 (스킬 정보 : 스킬) */
    private final HashMap<@NonNull SkillInfo, Skill> skillMap = new HashMap<>();
    /** 획득 점수 목록 (항목 : 획득 점수) */
    private final HashMap<@NonNull String, Double> scoreMap = new LinkedHashMap<>();
    /** 누적 자가 피해량. 자가 피해 치유 시 궁극기 충전 방지를 위해 사용한다. */
    private int selfHarmDamage = 0;
    /** 연속으로 획득한 점수의 합 */
    private double scoreStreakSum = 0;

    /** 선택한 전투원 종류 */
    @Getter
    private CharacterType characterType = null;
    /** 선택한 전투원 */
    private Character character = null;
    /** 무기 객체 */
    @Getter
    private Weapon weapon = null;
    /** 시야각 값 */
    @Getter
    @Setter
    private double fovValue = 0;
    /** 이동 거리. 발소리 재생에 사용됨 */
    private double footstepDistance = 0;
    @Getter
    @Setter
    private long time = System.currentTimeMillis();

    /**
     * 전투 시스템의 플레이어 인스턴스를 생성한다.
     *
     * @param user 대상 플레이어
     * @throws IllegalStateException 해당 {@code user}의 CombatUser가 이미 존재하면 발생
     */
    public CombatUser(@NonNull User user) {
        super(user.getPlayer(), user.getPlayer().getName(), GameUser.fromUser(user) == null ? null : GameUser.fromUser(user).getGame(),
                new FixedPitchHitbox(user.getPlayer().getLocation(), 0.5, 0.7, 0.3, 0, 0, 0, 0, 0.35, 0),
                new FixedPitchHitbox(user.getPlayer().getLocation(), 0.8, 0.7, 0.45, 0, 0, 0, 0, 1.05, 0),
                new Hitbox(user.getPlayer().getLocation(), 0.45, 0.35, 0.45, 0, 0.225, 0, 0, 1.4, 0),
                new Hitbox(user.getPlayer().getLocation(), 0.45, 0.1, 0.45, 0, 0.4, 0, 0, 1.4, 0)
        );
        this.user = user;
        this.gameUser = GameUser.fromUser(user);
        sidebar = user.getSidebar();
        knockbackModule = new KnockbackModule(this);
        statusEffectModule = new StatusEffectModule(this);
        attackModule = new AttackModule(this);
        damageModule = new HealModule(this, true, 1000);
        moveModule = new JumpModule(this, DEFAULT_SPEED);
        critHitbox = hitboxes[3];
        sidebar.clear();
    }

    /**
     * 지정한 플레이어의 전투 시스템의 플레이어 인스턴스를 반환한다.
     *
     * @param user 대상 플레이어
     * @return 전투 시스템의 플레이어 인스턴스. 존재하지 않으면 {@code null} 반환
     */
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
    protected void onTick(long i) {
        if (!isActivated)
            return;

        character.onTick(this, i);

        if (i > 1 && !entity.hasPotionEffect(PotionEffectType.FAST_DIGGING)) {
            entity.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING,
                    99999, 40, false, false), true);

            WrapperPlayServerEntityEffect packet = new WrapperPlayServerEntityEffect();
            packet.setEntityID(entity.getEntityId());
            packet.setEffectID((byte) PotionEffectType.FAST_DIGGING.getId());
            packet.setAmplifier((byte) 40);
            packet.setDuration(-1);
            packet.setHideParticles(true);
            packet.broadcastPacket();
        }

        hitboxes[2].setAxisOffsetY(entity.isSneaking() ? 1.15 : 1.4);
        hitboxes[3].setAxisOffsetY(entity.isSneaking() ? 1.15 : 1.4);

        if (!isDead())
            onTickLive(i);

        setLowHealthScreenEffect(damageModule.isLowHealth() || isDead());
        setCanSprint();
        adjustWalkSpeed();
        changeFov(fovValue);
        onTickActionbar();

        sidebar.clear();
        if (CooldownUtil.getCooldown(this, Cooldown.SCORE_DISPLAY_DURATION) > 0)
            sendScoreSidebar();
    }

    /**
     * 플레이어의 시야각을 변경한다.
     *
     * @param value 값
     */
    private void changeFov(double value) {
        WrapperPlayServerAbilities packet = new WrapperPlayServerAbilities();

        packet.setWalkingSpeed((float) (entity.getWalkSpeed() * value));

        packet.sendPacket(entity);
    }

    /**
     * 플레이어의 이동 속도를 조정한다.
     */
    private void adjustWalkSpeed() {
        double speed = Math.max(0, moveModule.getSpeedStatus().getValue());

        if (entity.isSprinting()) {
            speed *= 0.88;
            if (!entity.isOnGround())
                speed *= speed / DEFAULT_SPEED;
        }
        if (!canMove())
            speed = 0.0001;

        entity.setWalkSpeed((float) speed);
    }

    /**
     * 플레이어가 생존 중일 때 매 틱마다 실행할 작업.
     *
     * @param i 인덱스
     */
    private void onTickLive(long i) {
        checkHealPack();
        checkJumpPad();
        checkFallZone();

        onFootstep();

        if (i % 10 == 0)
            addUltGauge(GeneralConfig.getCombatConfig().getIdleUltChargePerSecond() / 2.0);

        if (damageModule.isLowHealth())
            ParticleUtil.playBleeding(entity, 0);
    }

    /**
     * 액션바 전송 작업을 실행한다.
     */
    private void onTickActionbar() {
        StringJoiner text = new StringJoiner("    ");

        text.add(character.getActionbarString(this));

        user.sendActionBar(text.toString());
    }

    /**
     * 사이드바에 획득 점수 목록을 출력한다.
     */
    private void sendScoreSidebar() {
        if (scoreMap.isEmpty())
            return;

        int i = 14;
        boolean isNew = CooldownUtil.getCooldown(this, Cooldown.SCORE_DISPLAY_DURATION) > SCORE_DISPLAY_DURATION - 10;

        sidebar.setName(MessageFormat.format("{0}+{1}", isNew ? "§d" : "§a", (int) scoreStreakSum));
        sidebar.set("§f", i--);
        for (Map.Entry<String, Double> entry : scoreMap.entrySet())
            sidebar.set(StringUtils.center(MessageFormat.format("§f{0} §a[+{1}]", entry.getKey(), entry.getValue().intValue()), 30), i--);
    }

    /**
     * 현재 위치의 힐 팩을 확인한다.
     */
    private void checkHealPack() {
        if (gameUser == null)
            return;

        Location location = entity.getLocation().subtract(0, 0.5, 0).getBlock().getLocation();
        if (location.getBlock().getType() != GeneralConfig.getCombatConfig().getHealPackBlock())
            return;

        Arrays.stream(game.getMap().getHealPackLocations())
                .filter(globalLocation -> globalLocation.isSameLocation(location))
                .findFirst()
                .ifPresent(healPackLoc -> useHealPack(location, healPackLoc));
    }

    /**
     * 지정한 위치의 힐 팩을 사용한다.
     *
     * @param location         실제 위치
     * @param healPackLocation 힐 팩 위치
     */
    private void useHealPack(@NonNull Location location, @NonNull GlobalLocation healPackLocation) {
        if (CooldownUtil.getCooldown(healPackLocation, Cooldown.HEAL_PACK) > 0)
            return;
        if (damageModule.getHealth() == damageModule.getMaxHealth())
            return;

        CooldownUtil.setCooldown(healPackLocation, Cooldown.HEAL_PACK);
        damageModule.heal((Healer) null, GeneralConfig.getCombatConfig().getHealPackHeal(), false);
        SoundUtil.play(Sound.ENTITY_ZOMBIE_VILLAGER_CONVERTED, entity.getLocation(), 0.5, 1.2);

        Location hologramLoc = location.add(0.5, 1.7, 0.5);

        TaskUtil.addTask(game, new IntervalTask(i -> {
            long cooldown = CooldownUtil.getCooldown(healPackLocation, Cooldown.HEAL_PACK);
            if (cooldown <= 0)
                return false;

            HologramUtil.addHologram("healpack" + healPackLocation, hologramLoc,
                    MessageFormat.format("§f§l[ §6{0} {1} §f§l]", TextIcon.COOLDOWN, Math.ceil(cooldown / 20.0)));
            game.getGameUsers().forEach(gameUser2 -> HologramUtil.setHologramVisibility("healpack" + healPackLocation,
                    LocationUtil.canPass(gameUser2.getPlayer().getEyeLocation(), hologramLoc), gameUser2.getPlayer()));

            return true;
        }, isCancalled -> HologramUtil.removeHologram("healpack" + healPackLocation),
                20, GeneralConfig.getCombatConfig().getHealPackCooldown()));
    }

    /**
     * 현재 위치의 점프대를 확인한다.
     */
    private void checkJumpPad() {
        Location location = entity.getLocation().subtract(0, 0.5, 0).getBlock().getLocation();
        if (location.getBlock().getType() != GeneralConfig.getCombatConfig().getJumpPadBlock())
            return;
        if (CooldownUtil.getCooldown(this, Cooldown.JUMP_PAD) > 0)
            return;

        CooldownUtil.setCooldown(this, Cooldown.JUMP_PAD);

        push(new Vector(0, 1.4, 0), true);
        SoundUtil.play(Sound.ENTITY_PLAYER_SMALL_FALL, entity.getLocation(), 1.5, 1.5, 0.1);
        SoundUtil.play(Sound.ENTITY_ITEM_PICKUP, entity.getLocation(), 1.5, 0.8, 0.05);
        SoundUtil.play(Sound.ENTITY_ITEM_PICKUP, entity.getLocation(), 1.5, 1.4, 0.05);
    }

    /**
     * 현재 위치의 낙사 구역을 확인한다.
     */
    private void checkFallZone() {
        Location location = entity.getLocation().subtract(0, 0.5, 0).getBlock().getLocation();
        if (location.getBlock().getType() != GeneralConfig.getCombatConfig().getFallZoneBlock())
            return;

        onDeath(null);
    }

    /**
     * 매 걸음마다 실행할 작업.
     *
     * <p>주로 발소리 재생에 사용한다.</p>
     */
    private void onFootstep() {
        Location oldLoc = entity.getLocation();
        double fallDistance = entity.getFallDistance();

        TaskUtil.addTask(this, new DelayTask(() -> {
            footstepDistance += oldLoc.distance(entity.getLocation());
            if (entity.isOnGround() && footstepDistance > 1.6) {
                footstepDistance = 0;
                double volume = 1.2 + fallDistance * 0.05;

                if (fallDistance > 6) {
                    SoundUtil.play(Sound.BLOCK_STONE_STEP, entity.getLocation(), 0.5 * volume, 0.8, 0.1);
                    SoundUtil.play(Sound.BLOCK_STONE_STEP, entity.getLocation(), 0.5 * volume, 0.9, 0.1);
                    SoundUtil.play(Sound.ENTITY_PLAYER_BIG_FALL, entity.getLocation(), 0.5 * volume, 0.9, 0.1);
                } else if (fallDistance > 3) {
                    SoundUtil.play(Sound.BLOCK_STONE_STEP, entity.getLocation(), 0.4 * volume, 0.9, 0.1);
                    SoundUtil.play(Sound.ENTITY_PLAYER_SMALL_FALL, entity.getLocation(), 0.4 * volume, 0.9, 0.1);
                } else if (fallDistance > 0)
                    SoundUtil.play(Sound.BLOCK_STONE_STEP, entity.getLocation(), 0.3 * volume, 0.9, 0.1);

                if (entity.isSprinting())
                    volume = 1;
                else if (!entity.isSneaking())
                    volume = 0.8;
                else
                    volume = 0.4;

                character.onFootstep(this, volume);
            }

        }, 1));
    }

    @Override
    public void dispose() {
        super.dispose();

        if (weapon != null)
            weapon.dispose();
        skillMap.forEach((skillInfo, skill) -> skill.dispose());
        HologramUtil.removeHologram("hitHealth" + this);

        reset();
    }

    @Override
    public boolean canBeTargeted() {
        if (!isActivated)
            return false;
        if (gameUser != null && gameUser.getSpawnRegionTeam() == gameUser.getTeam())
            return false;

        return !isDead();
    }

    @Override
    @NonNull
    public String getTeamIdentifier() {
        return gameUser == null ? name : gameUser.getTeam().getName();
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
     * 플레이어가 달리기를 할 수 있는 지 확인한다.
     *
     * @return 달리기 가능 여부
     */
    private boolean canSprint() {
        if (isDead())
            return false;
        if (!character.canSprint(this))
            return false;
        if (statusEffectModule.hasStatusEffect(StatusEffectType.STUN) || statusEffectModule.hasStatusEffect(StatusEffectType.SNARE) ||
                statusEffectModule.hasStatusEffect(StatusEffectType.GROUNDING))
            return false;
        if (CooldownUtil.getCooldown(this, Cooldown.NO_SPRINT) > 0)
            return false;
        if (propertyManager.getValue(Property.FREEZE) >= JagerT1Info.NO_SPRINT)
            return false;

        return true;
    }

    @Override
    public boolean canJump() {
        if (!isActivated)
            return true;
        if (!character.canJump(this))
            return false;

        return Jumpable.super.canJump();
    }

    @Override
    public void onAttack(@NonNull Damageable victim, int damage, @NonNull DamageType damageType, boolean isCrit, boolean isUlt) {
        if (this == victim)
            return;
        if (!isActivated)
            return;

        isUlt = isUlt && character.onAttack(this, victim, damage, damageType, isCrit);

        if (!(victim instanceof Barrier))
            showHealthHologram(victim);
        if (isCrit)
            playCritAttackEffect();
        else
            playAttackEffect();

        if (victim.getDamageModule().isUltProvider() && isUlt)
            addUltGauge(damage);

        if (gameUser != null && victim instanceof CombatUser)
            gameUser.setDamage(gameUser.getDamage() + damage);
    }

    /**
     * 공격했을 때 피격자의 남은 생명력 홀로그램을 표시한다.
     */
    private void showHealthHologram(@NonNull Damageable victim) {
        if (CooldownUtil.getCooldown(this, Cooldown.HIT_HEALTH_HOLOGRAM, victim.toString()) == 0) {
            CooldownUtil.setCooldown(this, Cooldown.HIT_HEALTH_HOLOGRAM, victim.toString());

            TaskUtil.addTask(this, new IntervalTask(i -> {
                long cooldown = CooldownUtil.getCooldown(this, Cooldown.HIT_HEALTH_HOLOGRAM, victim.toString());
                if (cooldown <= 0 || victim.isDisposed())
                    return false;

                Location loc = victim.getEntity().getLocation().add(0, victim.getEntity().getHeight(), 0);
                HologramUtil.setHologramVisibility("hitHealth" + victim, LocationUtil.canPass(entity.getEyeLocation(), loc), entity);

                return true;
            }, isCancelled -> HologramUtil.setHologramVisibility("hitHealth" + victim, false, entity), 1));
        } else
            CooldownUtil.setCooldown(this, Cooldown.HIT_HEALTH_HOLOGRAM, victim.toString());
    }

    /**
     * 공격했을 때 효과를 재생한다.
     */
    private void playAttackEffect() {
        user.sendTitle("", "§f×", 0, 2, 10);
        TaskUtil.addTask(this, new DelayTask(() -> {
            SoundUtil.play(Sound.ENTITY_PLAYER_HURT, entity, 1, 1.4);
            SoundUtil.play(Sound.ENTITY_PLAYER_BIG_FALL, entity, 1, 0.7);
        }, 2));
    }

    /**
     * 공격했을 때 효과를 재생한다. (치명타)
     */
    private void playCritAttackEffect() {
        user.sendTitle("", "§c§l×", 0, 2, 10);
        TaskUtil.addTask(this, new DelayTask(() -> {
            SoundUtil.play(Sound.ENTITY_PLAYER_BIG_FALL, entity, 2, 0.7);
            SoundUtil.play(Sound.BLOCK_ANVIL_PLACE, entity, 0.4, 1.8);
        }, 2));
    }

    @Override
    public void onDamage(Attacker attacker, int damage, int reducedDamage, @NonNull DamageType damageType, Location location, boolean isCrit, boolean isUlt) {
        if (!isActivated)
            return;
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
            if (CooldownUtil.getCooldown(attacker, Cooldown.DAMAGE_SUM_TIME_LIMIT, String.valueOf(entity.getEntityId())) == 0)
                CooldownUtil.setCooldown(attacker, Cooldown.FASTKILL_TIME_LIMIT, String.valueOf(entity.getEntityId()));
            CooldownUtil.setCooldown(attacker, Cooldown.DAMAGE_SUM_TIME_LIMIT, String.valueOf(entity.getEntityId()));

            int sumDamage = damageMap.getOrDefault(attacker, 0);
            damageMap.put((CombatUser) attacker, sumDamage + damage);
        }

        if (gameUser != null)
            gameUser.setDefend(gameUser.getDefend() + reducedDamage);
    }

    @Override
    public boolean canTakeDamage() {
        if (!isActivated)
            return false;
        if (entity.getGameMode() != GameMode.SURVIVAL)
            return false;

        return true;
    }

    @Override
    public boolean canDie() {
        if (!isActivated)
            return false;
        if (LocationUtil.isInRegion(entity, "BattleTrain"))
            return false;

        return true;
    }

    @Override
    public void onGiveHeal(@NonNull Healable target, int amount, boolean isUlt) {
        isUlt = isUlt && character.onGiveHeal(this, target, amount);

        if (target.getDamageModule().isUltProvider() && isUlt) {
            int ultAmount = amount;

            if (target instanceof CombatUser && ((CombatUser) target).selfHarmDamage > 0)
                ultAmount = -((CombatUser) target).selfHarmDamage - amount;

            if (ultAmount > 0)
                addUltGauge(ultAmount);
        }

        if (gameUser != null && target instanceof CombatUser)
            gameUser.setHeal(gameUser.getHeal() + amount);
    }

    @Override
    public void onTakeHeal(Healer provider, int amount, boolean isUlt) {
        if (!isActivated)
            return;

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
    private void playTakeHealEffect(int amount) {
        if (amount >= 100 || amount / 100.0 > DMGR.getRandom().nextDouble())
            ParticleUtil.play(Particle.HEART, LocationUtil.getLocationFromOffset(entity.getLocation(),
                            0, entity.getHeight() + 0.3, 0), (int) Math.ceil(amount / 100.0),
                    0.3, 0.1, 0.3, 0);
    }

    @Override
    public void onKill(@NonNull Damageable victim) {
        if (!isActivated)
            return;
        if (this == victim)
            return;

        character.onKill(this, victim);

        playKillEffect();
        if (victim instanceof CombatUser) {
            int totalDamage = ((CombatUser) victim).damageMap.values().stream().mapToInt(Integer::intValue).sum();
            int damage = ((CombatUser) victim).damageMap.getOrDefault(this, 0);
            int score = Math.round(((float) damage / totalDamage) * 100);

            addScore(MessageFormat.format("§e{0}§f 처치", victim.getName()), score);

            if (gameUser != null) {
                gameUser.setKill(gameUser.getKill() + 1);
                if (gameUser.getGame().getGamePlayMode() == GamePlayMode.TEAM_DEATHMATCH)
                    gameUser.addTeamScore(1);
            }
        } else {
            if (victim instanceof Dummy)
                addScore(MessageFormat.format("§e{0}§f 처치", victim.getName()), 100);
        }
    }

    /**
     * 엔티티를 처치했을 때 효과를 재생한다.
     */
    private void playKillEffect() {
        user.sendTitle("", "§c" + TextIcon.POISON, 0, 2, 10);
        TaskUtil.addTask(this, new DelayTask(() -> {
            SoundUtil.play(Sound.ENTITY_EXPERIENCE_ORB_PICKUP, entity, 2, 1.25);
            SoundUtil.play(Sound.ENTITY_EXPERIENCE_ORB_PICKUP, entity, 1, 1.25);
        }, 2));
    }

    /**
     * 처치 시 킬로그를 표시한다.
     *
     * @param victim 피격자
     */
    private void broadcastPlayerKillMessage(@NonNull CombatEntity victim) {
        Map<CombatUser, Integer> victimDamageMap = ((CombatUser) victim).damageMap;

        Set<String> attackerNames = new HashSet<>();
        for (CombatUser attacker2 : victimDamageMap.keySet()) {
            String s = "§f\u3000§l" + attacker2.getName();
            attackerNames.add(s);
        }
        String victimName = "§f\u3000§l" + victim.getName();

        if (!victimDamageMap.isEmpty() && game != null) {
            game.getGameUsers().forEach(gameUser2 ->
                    User.fromPlayer(gameUser2.getPlayer()).sendMessageInfo("{0} §4§l-> {1}",
                            String.join(" ,", attackerNames), victimName));
        }
    }

    @Override
    public void onDeath(Attacker attacker) {
        if (!isActivated)
            return;
        if (CooldownUtil.getCooldown(this, Cooldown.RESPAWN_TIME) != 0)
            return;

        character.onDeath(this, attacker);

        damageModule.setHealth(damageModule.getMaxHealth());

        int totalDamage = damageMap.values().stream().mapToInt(Integer::intValue).sum();
        damageMap.forEach((CombatUser attacker2, Integer damage) -> {
            if (attacker2 != ((attacker instanceof SummonEntity) ? ((SummonEntity<?>) attacker).getOwner() : attacker)) {
                int score = Math.round(((float) damage / totalDamage) * 100);

                attacker2.addScore(MessageFormat.format("§e{0}§f 처치 도움", name), score);
                attacker2.playKillEffect();

                if (attacker2.gameUser != null)
                    attacker2.gameUser.setAssist(attacker2.gameUser.getAssist() + 1);
            }
        });

        broadcastPlayerKillMessage(this);
        selfHarmDamage = 0;
        damageMap.clear();

        if (gameUser != null)
            gameUser.setDeath(gameUser.getDeath() + 1);

        cancelAction();
        respawn();
    }

    /**
     * 사망 후 리스폰 작업을 수행한다.
     */
    private void respawn() {
        Location deadLocation;
        if (gameUser == null)
            deadLocation = LocationUtil.getLobbyLocation();
        else
            deadLocation = gameUser.getRespawnLocation();
        deadLocation.add(0, 2, 0);
        user.teleport(deadLocation);

        CooldownUtil.setCooldown(this, Cooldown.RESPAWN_TIME);
        entity.setGameMode(GameMode.SPECTATOR);
        entity.setVelocity(new Vector());

        TaskUtil.addTask(this, new IntervalTask(i -> {
            long cooldown = CooldownUtil.getCooldown(CombatUser.this, Cooldown.RESPAWN_TIME);
            if (cooldown <= 0)
                return false;

            user.sendTitle("§c§l죽었습니다!", MessageFormat.format("{0}초 후 부활합니다.",
                    String.format("%.1f", cooldown / 20.0)), 0, 5, 10);
            user.teleport(deadLocation);

            return true;
        }, isCancelled -> {
            damageModule.setHealth(damageModule.getMaxHealth());
            entity.setGameMode(GameMode.SURVIVAL);

            weapon.reset();
            skillMap.forEach((skillInfo, skill) -> skill.reset());
        }, 1));
    }

    /**
     * 플레이어가 사망한 상태인 지 확인한다.
     *
     * @return 사망 후 리스폰 대기 중이면 {@code true} 반환
     */
    public boolean isDead() {
        return CooldownUtil.getCooldown(this, Cooldown.RESPAWN_TIME) > 0;
    }

    public Skill getSkill(@NonNull SkillInfo skillInfo) {
        return skillMap.get(skillInfo);
    }

    /**
     * 플레이어의 전역 쿨타임이 끝났는 지 확인한다.
     *
     * @return 전역 쿨타임 종료 여부
     */
    public boolean isGlobalCooldownFinished() {
        return CooldownUtil.getCooldown(this, Cooldown.GLOBAL_COOLDOWN) == 0;
    }

    /**
     * 플레이어의 전역 쿨타임을 초기화한다.
     */
    public void resetGlobalCooldown() {
        CooldownUtil.setCooldown(this, Cooldown.GLOBAL_COOLDOWN, 0);
        entity.setCooldown(SkillInfo.MATERIAL, 0);
        entity.setCooldown(WeaponInfo.MATERIAL, 0);
    }

    /**
     * 플레이어의 전역 쿨타임을 설정한다.
     *
     * @param cooldown 쿨타임 (tick). {@code -1}로 설정 시 무한 지속
     */
    public void setGlobalCooldown(int cooldown) {
        if (cooldown == -1)
            cooldown = 99999;
        if (cooldown < CooldownUtil.getCooldown(this, Cooldown.GLOBAL_COOLDOWN))
            return;

        CooldownUtil.setCooldown(this, Cooldown.GLOBAL_COOLDOWN, cooldown);
        entity.setCooldown(SkillInfo.MATERIAL, cooldown);
        entity.setCooldown(WeaponInfo.MATERIAL, cooldown);
    }

    /**
     * 지정한 양만큼 플레이어의 점수를 증가시키고 사이드바 애니메이션을 표시한다.
     *
     * <p>게임 참여 중이 아니면 점수 획득 표시만 한다.</p>
     *
     * @param context 항목
     * @param score   점수 증가량
     */
    private void addScore(@NonNull String context, double score) {
        if (gameUser != null)
            gameUser.setScore(gameUser.getScore() + score);

        if (CooldownUtil.getCooldown(this, Cooldown.SCORE_DISPLAY_DURATION) == 0) {
            scoreStreakSum = 0;
            scoreMap.clear();
        }

        CooldownUtil.setCooldown(this, Cooldown.SCORE_DISPLAY_DURATION);
        if (scoreMap.size() > 5)
            scoreMap.remove(scoreMap.keySet().iterator().next());

        TaskUtil.addTask(this, new IntervalTask(i -> {
            scoreStreakSum += score / 10;
            scoreMap.put(context, scoreMap.getOrDefault(context, 0.0) + score / 10);
            return true;
        }, 1, 10));
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
     * @throws IllegalArgumentException {@code value}가 0~1 사이가 아니면 발생
     */
    public void setUltGaugePercent(double value) {
        if (value < 0 || value > 1)
            throw new IllegalArgumentException("'value'가 0에서 1 사이여야 함");

        if (!isActivated)
            value = 0;
        else {
            Skill skill = skillMap.get(character.getUltimateSkillInfo());
            if (!skill.isDurationFinished())
                value = 0;
        }
        if (value == 1) {
            onUltReady();
            value = 0.999;
        }

        entity.setExp((float) value);
        entity.setLevel((int) Math.round(value * 100));
    }

    /**
     * 플레이어의 궁극기 게이지를 증가시킨다.
     *
     * @param value 추가할 궁극기 게이지. 0~1 사이의 값. (단위: 백분율)
     */
    public void addUltGaugePercent(double value) {
        setUltGaugePercent(Math.min(getUltGaugePercent() + value, 1));
    }

    /**
     * 플레이어의 궁극기 게이지를 증가시킨다.
     *
     * @param value 추가할 궁극기 게이지
     */
    public void addUltGauge(double value) {
        UltimateSkill ultimateSkill = (UltimateSkill) getSkill(character.getUltimateSkillInfo());
        addUltGaugePercent(value / ultimateSkill.getCost());
    }

    /**
     * 궁극기 게이지 최대 충전 시 실행할 작업.
     */
    private void onUltReady() {
        Skill skill = skillMap.get(character.getUltimateSkillInfo());
        if (!skill.isCooldownFinished())
            skill.setCooldown(0);
    }

    /**
     * 플레이어의 전투원을 설정하고 무기와 스킬을 초기화한다.
     *
     * @param characterType 전투원
     */
    public void setCharacterType(@NonNull CharacterType characterType) {
        reset();
        Character realCharacter = characterType.getCharacter();

        SkinUtil.applySkin(entity, realCharacter.getSkinName()).run();
        damageModule.setMaxHealth(realCharacter.getHealth());
        damageModule.setHealth(realCharacter.getHealth());
        moveModule.getSpeedStatus().setBaseValue(DEFAULT_SPEED * realCharacter.getSpeedMultiplier());
        entity.getInventory().setItem(9, CommunicationItem.REQ_HEAL.staticItem.getItemStack());
        entity.getInventory().setItem(10, CommunicationItem.SHOW_ULT.staticItem.getItemStack());
        entity.getInventory().setItem(11, CommunicationItem.REQ_RALLY.staticItem.getItemStack());

        double hitboxMultiplier = realCharacter.getHitboxMultiplier();
        for (Hitbox hitbox : hitboxes) {
            hitbox.setSizeX(hitbox.getSizeX() * hitboxMultiplier);
            hitbox.setSizeZ(hitbox.getSizeZ() * hitboxMultiplier);
        }

        this.characterType = characterType;
        this.character = realCharacter;
        initActions();
        if (!isActivated)
            activate();
    }

    /**
     * 플레이어의 상태를 초기화한다.
     */
    public void reset() {
        entity.setHealth(entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
        entity.getInventory().setHeldItemSlot(4);
        entity.getActivePotionEffects().forEach((potionEffect ->
                entity.removePotionEffect(potionEffect.getType())));
        entity.setFlying(false);
        entity.setGameMode(GameMode.SURVIVAL);
        fovValue = 0;
        changeFov(0);
        setUltGaugePercent(0);
        setLowHealthScreenEffect(false);

        knockbackModule.getResistanceStatus().clearModifier();
        statusEffectModule.getResistanceStatus().clearModifier();
        attackModule.getDamageMultiplierStatus().clearModifier();
        damageModule.getDefenseMultiplierStatus().clearModifier();
        moveModule.getSpeedStatus().clearModifier();
    }

    /**
     * 플레이어의 동작 설정을 초기화한다.
     */
    private void initActions() {
        if (weapon != null)
            weapon.dispose();
        skillMap.forEach((skillInfo, skill) -> skill.dispose());

        actionMap.clear();
        skillMap.clear();

        for (ActionKey actionKey : ActionKey.values())
            actionMap.put(actionKey, new TreeSet<>(Comparator.comparing(Action::getPriority).reversed()));

        actionMap.get(ActionKey.SWAP_HAND).add(new MeleeAttackAction(this));

        weapon = character.getWeaponInfo().createWeapon(this);
        for (ActionKey actionKey : weapon.getDefaultActionKeys()) {
            actionMap.get(actionKey).add(weapon);
        }
        for (int i = 1; i <= 4; i++) {
            ActiveSkillInfo activeSkillInfo = character.getActiveSkillInfo(i);
            if (activeSkillInfo != null) {
                Skill skill = activeSkillInfo.createSkill(this);
                skillMap.put(activeSkillInfo, skill);
                for (ActionKey actionKey : skill.getDefaultActionKeys()) {
                    actionMap.get(actionKey).add(skill);
                }
            }
        }
        for (int i = 1; i <= 4; i++) {
            PassiveSkillInfo passiveSkillInfo = character.getPassiveSkillInfo(i);
            if (passiveSkillInfo != null) {
                Skill skill = passiveSkillInfo.createSkill(this);
                skillMap.put(passiveSkillInfo, skill);
                for (ActionKey actionKey : skill.getDefaultActionKeys()) {
                    actionMap.get(actionKey).add(skill);
                }
            }
        }
    }

    /**
     * 지정한 동작 사용 키에 해당하는 동작을 사용한다.
     *
     * @param actionKey 동작 사용 키
     */
    public void useAction(@NonNull ActionKey actionKey) {
        TreeSet<Action> actions = actionMap.get(actionKey);

        actions.forEach(action -> {
            if (isDead() || action == null)
                return;
            if (statusEffectModule.hasStatusEffect(StatusEffectType.STUN))
                return;

            if (action instanceof MeleeAttackAction && action.canUse()) {
                action.onUse(actionKey);
                return;
            }

            Weapon realWeapon = this.weapon;
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
        else if (weapon.canUse())
            weapon.onUse(actionKey);
    }

    /**
     * 연사 무기의 사용 로직을 처리한다.
     *
     * @param actionKey 동작 사용 키
     * @param weapon    무기
     */
    private void handleUseFullAutoWeapon(@NonNull ActionKey actionKey, @NonNull Weapon weapon) {
        if (CooldownUtil.getCooldown(weapon, Cooldown.WEAPON_FULLAUTO_COOLDOWN) > 0)
            return;

        CooldownUtil.setCooldown(weapon, Cooldown.WEAPON_FULLAUTO_COOLDOWN);

        TaskUtil.addTask(weapon, new IntervalTask(new Function<Long, Boolean>() {
            int j = 0;

            @Override
            public Boolean apply(Long i) {
                if (j > 0 && weapon instanceof Reloadable && ((Reloadable) weapon).getReloadModule().isReloading())
                    return true;
                if (weapon.canUse() && !isDead() && isGlobalCooldownFinished() && ((FullAuto) weapon).getFullAutoModule().isFireTick(entity.getTicksLived())) {
                    j++;
                    weapon.onUse(actionKey);
                }

                return true;
            }
        }, 1, 4));
    }

    /**
     * 스킬 사용 로직을 처리한다.
     *
     * @param actionKey 동작 사용 키
     * @param skill     스킬
     */
    private void handleUseSkill(@NonNull ActionKey actionKey, @NonNull Skill skill) {
        if (!skill.canUse() || statusEffectModule.hasStatusEffect(StatusEffectType.SILENCE))
            return;

        skill.onUse(actionKey);
    }

    /**
     * 사용 중인 모든 동작을 강제로 취소시킨다.
     */
    public void cancelAction() {
        weapon.onCancelled();
        skillMap.forEach((skillInfo, skill) -> {
            if (!skill.isDurationFinished())
                skill.onCancelled();
        });
    }

    /**
     * 플레이어에의 치명상 화면 효과 표시를 설정한다.
     *
     * @param isEnabled 활성화 여부
     */
    private void setLowHealthScreenEffect(boolean isEnabled) {
        WrapperPlayServerWorldBorder packet = new WrapperPlayServerWorldBorder();

        packet.setAction(EnumWrappers.WorldBorderAction.SET_WARNING_BLOCKS);
        packet.setWarningDistance(isEnabled ? 999999999 : 0);

        packet.sendPacket(entity);
    }

    /**
     * 플레이어에게 근접 공격 애니메이션을 재생한다.
     *
     * @param amplifier 성급함 포션 효과 레벨
     * @param duration  지속시간 (tick)
     */
    public void playMeleeAttackAnimation(int amplifier, int duration) {
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

        WrapperPlayClientArmAnimation packet3 = new WrapperPlayClientArmAnimation();
        packet3.receivePacket(entity);

        WrapperPlayServerAnimation packet4 = new WrapperPlayServerAnimation();
        packet4.setAnimation(0);
        packet4.setEntityID(entity.getEntityId());
        packet4.broadcastPacket();
    }

    @Getter
    public enum CommunicationItem {
        /** 치료 요청 */
        REQ_HEAL(Material.STAINED_GLASS_PANE, (short) 5, "§a치료 요청"),
        /** 궁극기 상태 */
        SHOW_ULT(Material.STAINED_GLASS_PANE, (short) 5, "§a궁극기 상태"),
        /** 집결 요청 */
        REQ_RALLY(Material.STAINED_GLASS_PANE, (short) 5, "§a집결 요청"),
        /** 전투원 선택 */
        SELECT_CHARACTER(Material.EMERALD, (short) 0, "§f전투원 선택");

        private final StaticItem<CommunicationItem> staticItem;

        CommunicationItem(Material material, short damage, String name) {
            ItemBuilder itemBuilder = new ItemBuilder(material)
                    .setDamage(damage)
                    .setName(name);

            this.staticItem = new StaticItem<>(this, itemBuilder.build());
        }
    }
}
