package com.dace.dmgr.combat.entity;

import com.comphenix.packetwrapper.*;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.dace.dmgr.DMGR;
import com.dace.dmgr.GeneralConfig;
import com.dace.dmgr.combat.CombatUtil;
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
import com.dace.dmgr.combat.action.weapon.Swappable;
import com.dace.dmgr.combat.action.weapon.Weapon;
import com.dace.dmgr.combat.character.Character;
import com.dace.dmgr.combat.character.CharacterType;
import com.dace.dmgr.combat.character.jager.action.JagerT1Info;
import com.dace.dmgr.combat.entity.module.*;
import com.dace.dmgr.combat.entity.module.statuseffect.StatusEffectType;
import com.dace.dmgr.combat.entity.temporal.Dummy;
import com.dace.dmgr.combat.entity.temporal.SummonEntity;
import com.dace.dmgr.combat.interaction.DamageType;
import com.dace.dmgr.combat.interaction.FixedPitchHitbox;
import com.dace.dmgr.combat.interaction.HasCritHitbox;
import com.dace.dmgr.combat.interaction.Hitbox;
import com.dace.dmgr.game.GamePlayMode;
import com.dace.dmgr.game.GameUser;
import com.dace.dmgr.game.map.GlobalLocation;
import com.dace.dmgr.item.ItemBuilder;
import com.dace.dmgr.item.gui.GuiItem;
import com.dace.dmgr.user.User;
import com.dace.dmgr.user.UserData;
import com.dace.dmgr.util.*;
import com.dace.dmgr.util.task.DelayTask;
import com.dace.dmgr.util.task.IntervalTask;
import com.dace.dmgr.util.task.TaskUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.boss.BarColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.text.MessageFormat;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * 전투 시스템의 플레이어 정보를 관리하는 클래스.
 */
public final class CombatUser extends AbstractCombatEntity<Player> implements Healable, Attacker, Healer, Living, HasCritHitbox, Jumpable {
    /** 기본 이동속도 */
    private static final double DEFAULT_SPEED = 0.12;
    /** 킬 로그 표시 유지시간 (tick) */
    private static final long KILL_LOG_DISPLAY_DURATION = 80;

    /** 유저 정보 객체 */
    @NonNull
    @Getter
    private final User user;
    /** 게임 유저 객체. {@code null}이면 게임에 참여중이지 않음을 나타냄 */
    @Nullable
    @Getter
    private final GameUser gameUser;
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
    private final EnumMap<@NonNull ActionKey, TreeSet<Action>> actionMap = new EnumMap<>(ActionKey.class);
    /** 스킬 객체 목록 (스킬 정보 : 스킬) */
    private final HashMap<@NonNull SkillInfo, Skill> skillMap = new HashMap<>();
    /** 획득 점수 목록 (항목 : 획득 점수) */
    private final HashMap<@NonNull String, Double> scoreMap = new LinkedHashMap<>();
    /** 누적 자가 피해량. 자가 피해 치유 시 궁극기 충전 방지를 위해 사용한다. */
    private int selfHarmDamage = 0;
    /** 연속으로 획득한 점수의 합 */
    private double scoreStreakSum = 0;
    /** 시야각 값 */
    @Getter
    @Setter
    private double fovValue = 0;
    /** 이동 거리. 발소리 재생에 사용됨 */
    private double footstepDistance = 0;
    /** 선택한 전투원 종류 */
    @Getter
    private CharacterType characterType = null;
    /** 선택한 전투원 */
    private Character character = null;
    /** 선택한 전투원 기록 정보 */
    private UserData.CharacterRecord characterRecord = null;
    /** 무기 객체 */
    @Getter
    private Weapon weapon = null;
    /** 연사 무기 사용을 처리하는 태스크 */
    private IntervalTask fullAutoTask = null;
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

        knockbackModule = new KnockbackModule(this);
        statusEffectModule = new StatusEffectModule(this);
        attackModule = new AttackModule(this);
        damageModule = new HealModule(this, true, true, 1000);
        moveModule = new JumpModule(this, DEFAULT_SPEED);
        critHitbox = hitboxes[3];
        user.clearSidebar();
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
    protected void onTick(long i) {
        if (!isActivated)
            return;

        character.onTick(this, i);

        if (!entity.hasPotionEffect(PotionEffectType.FAST_DIGGING)) {
            entity.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING,
                    Integer.MAX_VALUE, 40, false, false), true);

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

        entity.setAllowFlight(canFly());
        setLowHealthScreenEffect(damageModule.isLowHealth() || isDead());
        setCanSprint();
        adjustWalkSpeed();
        changeFov(fovValue);
    }

    /**
     * 플레이어의 시야각을 변경한다.
     *
     * @param value 값
     */
    private void changeFov(double value) {
        WrapperPlayServerAbilities packet = new WrapperPlayServerAbilities();

        packet.setCanFly(isActivated && canFly());
        packet.setWalkingSpeed((float) (moveModule.getSpeedStatus().getValue() * 2 * value));

        packet.sendPacket(entity);
    }

    /**
     * 플레이어의 이동 속도를 조정한다.
     */
    private void adjustWalkSpeed() {
        double speed = Math.max(0, DEFAULT_SPEED * character.getSpeedMultiplier());

        if (entity.isSprinting()) {
            speed *= 0.88;
            if (!entity.isOnGround())
                speed *= speed / DEFAULT_SPEED;
        }

        moveModule.getSpeedStatus().setBaseValue(speed);
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

        onTickActionbar();
        onFootstep();

        if (i % 10 == 0)
            addUltGauge(GeneralConfig.getCombatConfig().getIdleUltChargePerSecond() / 2.0);

        if (damageModule.isLowHealth())
            CombatUtil.playBleedingEffect(null, entity, 0);

        if (i % 20 == 0 && gameUser != null && gameUser.getSpawnRegionTeam() == null)
            characterRecord.setPlayTime(characterRecord.getPlayTime() + 1);
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
        if (CooldownUtil.getCooldown(healPackLocation, Cooldown.HEAL_PACK.id) > 0)
            return;
        if (damageModule.getHealth() == damageModule.getMaxHealth())
            return;

        CooldownUtil.setCooldown(healPackLocation, Cooldown.HEAL_PACK.id, Cooldown.HEAL_PACK.duration);
        damageModule.heal(this, GeneralConfig.getCombatConfig().getHealPackHeal(), false);
        character.onUseHealPack(this);
        SoundUtil.playNamedSound(NamedSound.COMBAT_USE_HEAL_PACK, entity.getLocation());

        Location hologramLoc = location.add(0.5, 1.7, 0.5);
        HologramUtil.addHologram(Cooldown.HEAL_PACK.id + healPackLocation, hologramLoc, MessageFormat.format("§f§l[ §6{0} 0 §f§l]", TextIcon.COOLDOWN));

        TaskUtil.addTask(game, new IntervalTask(i -> {
            long cooldown = CooldownUtil.getCooldown(healPackLocation, Cooldown.HEAL_PACK.id);
            if (cooldown <= 0)
                return false;

            HologramUtil.editHologram(Cooldown.HEAL_PACK.id + healPackLocation,
                    MessageFormat.format("§f§l[ §6{0} {1} §f§l]", TextIcon.COOLDOWN, Math.ceil(cooldown / 20.0)));
            game.getGameUsers().forEach(gameUser2 -> HologramUtil.setHologramVisibility(Cooldown.HEAL_PACK.id + healPackLocation,
                    LocationUtil.canPass(gameUser2.getPlayer().getEyeLocation(), hologramLoc), gameUser2.getPlayer()));

            return true;
        }, isCancalled -> HologramUtil.removeHologram(Cooldown.HEAL_PACK.id + healPackLocation), 5));
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

        push(new Vector(0, GeneralConfig.getCombatConfig().getJumpPadVelocity(), 0), true);
        SoundUtil.playNamedSound(NamedSound.COMBAT_USE_JUMP_PAD, entity.getLocation());
    }

    /**
     * 현재 위치의 낙사 구역을 확인 및 낙사 처리한다.
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

                if (fallDistance > 6)
                    SoundUtil.playNamedSound(NamedSound.COMBAT_FALL_HIGH, entity.getLocation(), volume);
                else if (fallDistance > 3)
                    SoundUtil.playNamedSound(NamedSound.COMBAT_FALL_MID, entity.getLocation(), volume);
                else if (fallDistance > 0)
                    SoundUtil.playNamedSound(NamedSound.COMBAT_FALL_LOW, entity.getLocation(), volume);

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
        if (CooldownUtil.getCooldown(this, Cooldown.WEAPON_NO_SPRINT.id) > 0)
            return false;
        if (propertyManager.getValue(Property.FREEZE) >= JagerT1Info.NO_SPRINT)
            return false;

        return true;
    }

    /**
     * 플레이어가 비행할 수 있는 지 확인한다.
     *
     * @return 비행 가능 여부
     */
    private boolean canFly() {
        if (isDead())
            return false;
        if (!character.canFly(this))
            return false;
        if (statusEffectModule.hasStatusEffect(StatusEffectType.STUN) || statusEffectModule.hasStatusEffect(StatusEffectType.SNARE) ||
                statusEffectModule.hasStatusEffect(StatusEffectType.GROUNDING))
            return false;

        return true;
    }

    @Override
    public boolean canJump() {
        if (!isActivated)
            return true;
        if (!character.canJump(this))
            return false;

        return true;
    }

    @Override
    public void onAttack(@NonNull Damageable victim, int damage, @NonNull DamageType damageType, boolean isCrit, boolean isUlt) {
        if (this == victim)
            return;
        if (!isActivated)
            return;

        isUlt = isUlt && character.onAttack(this, victim, damage, damageType, isCrit);

        if (victim.getDamageModule().isShowHealthBar())
            showHealthHologram(victim);
        if (isCrit) {
            user.sendTitle("", "§c§l×", 0, 2, 10);
            TaskUtil.addTask(this, new DelayTask(() -> SoundUtil.playNamedSound(NamedSound.COMBAT_ATTACK_CRIT, entity), 2));
        } else {
            user.sendTitle("", "§f×", 0, 2, 10);
            TaskUtil.addTask(this, new DelayTask(() -> SoundUtil.playNamedSound(NamedSound.COMBAT_ATTACK, entity), 2));
        }

        if (victim.getDamageModule().isUltProvider() && isUlt)
            addUltGauge(damage);

        if (gameUser != null && victim instanceof CombatUser)
            gameUser.setDamage(gameUser.getDamage() + damage);
    }

    /**
     * 공격했을 때 피격자의 남은 생명력 홀로그램을 표시한다.
     */
    private void showHealthHologram(@NonNull Damageable victim) {
        if (CooldownUtil.getCooldown(this, Cooldown.HIT_HEALTH_HOLOGRAM.id + victim) == 0) {
            CooldownUtil.setCooldown(this, Cooldown.HIT_HEALTH_HOLOGRAM.id + victim, Cooldown.HIT_HEALTH_HOLOGRAM.duration);

            TaskUtil.addTask(this, new IntervalTask(i -> {
                long cooldown = CooldownUtil.getCooldown(this, Cooldown.HIT_HEALTH_HOLOGRAM.id + victim);
                if (cooldown <= 0 || victim.isDisposed())
                    return false;

                Location loc = victim.getEntity().getLocation().add(0, victim.getEntity().getHeight(), 0);
                HologramUtil.setHologramVisibility(DamageModule.HEALTH_HOLOGRAM_ID + victim, LocationUtil.canPass(entity.getEyeLocation(), loc), entity);

                return true;
            }, isCancelled -> HologramUtil.setHologramVisibility(DamageModule.HEALTH_HOLOGRAM_ID + victim, false, entity), 4));
        } else
            CooldownUtil.setCooldown(this, Cooldown.HIT_HEALTH_HOLOGRAM.id + victim, Cooldown.HIT_HEALTH_HOLOGRAM.duration);
    }

    @Override
    public void onDamage(@Nullable Attacker attacker, int damage, int reducedDamage, @NonNull DamageType damageType, @Nullable Location location, boolean isCrit, boolean isUlt) {
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
            if (CooldownUtil.getCooldown(attacker, Cooldown.DAMAGE_SUM_TIME_LIMIT.id + entity) == 0) {
                CooldownUtil.setCooldown(attacker, Cooldown.FASTKILL_TIME_LIMIT.id + entity, Cooldown.FASTKILL_TIME_LIMIT.duration);
                damageMap.remove(attacker);
            }
            CooldownUtil.setCooldown(attacker, Cooldown.DAMAGE_SUM_TIME_LIMIT.id + entity, Cooldown.DAMAGE_SUM_TIME_LIMIT.duration);

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
    public void onTakeHeal(@Nullable Healer provider, int amount, boolean isUlt) {
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
            ParticleUtil.play(Particle.HEART, entity.getLocation().add(0, entity.getHeight() + 0.3, 0), (int) Math.ceil(amount / 100.0),
                    0.3, 0.1, 0.3, 0);
    }

    @Override
    public void onKill(@NonNull Damageable victim) {
        if (!isActivated)
            return;
        if (this == victim)
            return;

        character.onKill(this, victim, true);

        playKillEffect();
        if (victim instanceof CombatUser) {
            int totalDamage = ((CombatUser) victim).damageMap.values().stream().mapToInt(Integer::intValue).sum();
            int damage = ((CombatUser) victim).damageMap.getOrDefault(this, 0);
            int score = Math.round(((float) damage / totalDamage) * 100);

            addScore(MessageFormat.format("§e{0}§f 처치", victim.getName()), score);

            if (gameUser != null) {
                gameUser.setKill(gameUser.getKill() + 1);
                characterRecord.setKill(characterRecord.getKill() + 1);
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
        TaskUtil.addTask(this, new DelayTask(() -> SoundUtil.playNamedSound(NamedSound.COMBAT_KILL, entity), 2));
    }

    /**
     * 처치 시 킬로그를 표시한다.
     */
    private void broadcastPlayerKillMessage() {
        if (game == null)
            return;

        Set<String> attackerNames = new HashSet<>();
        for (CombatUser attacker2 : damageMap.keySet()) {
            ChatColor color = attacker2.getGameUser() == null ? ChatColor.WHITE : attacker2.getGameUser().getTeam().getColor();
            String s = MessageFormat.format("§f{0}{1}§l {2}", attacker2.character.getIcon(), color, attacker2.getName());
            attackerNames.add(s);
        }
        ChatColor color = gameUser == null ? ChatColor.WHITE : gameUser.getTeam().getColor();
        String victimName = MessageFormat.format("§f{0}{1}§l {2}", character.getIcon(), color, name);

        game.getGameUsers().forEach(gameUser2 -> {
            gameUser2.getUser().addBossBar("CombatKill" + this,
                    MessageFormat.format("{0} §4§l-> {1}", String.join(" ,", attackerNames), victimName),
                    BarColor.WHITE, WrapperPlayServerBoss.BarStyle.PROGRESS, 0);

            TaskUtil.addTask(gameUser2, new DelayTask(() ->
                    gameUser2.getUser().removeBossBar("CombatKill" + this), KILL_LOG_DISPLAY_DURATION));
        });
    }

    @Override
    public void onDeath(@Nullable Attacker attacker) {
        if (!isActivated)
            return;
        if (CooldownUtil.getCooldown(this, Cooldown.RESPAWN.id) != 0)
            return;

        character.onDeath(this, attacker);

        damageModule.setHealth(damageModule.getMaxHealth());
        statusEffectModule.clearStatusEffect();

        int totalDamage = damageMap.values().stream().mapToInt(Integer::intValue).sum();
        damageMap.forEach((CombatUser attacker2, Integer damage) -> {
            if (CooldownUtil.getCooldown(attacker2, Cooldown.DAMAGE_SUM_TIME_LIMIT.id + entity) > 0 &&
                    (attacker2 != ((attacker instanceof SummonEntity) ? ((SummonEntity<?>) attacker).getOwner() : attacker))) {
                int score = Math.round(((float) damage / totalDamage) * 100);

                attacker2.character.onKill(attacker2, this, false);
                attacker2.addScore(MessageFormat.format("§e{0}§f 처치 도움", name), score);
                attacker2.playKillEffect();

                if (attacker2.gameUser != null) {
                    attacker2.gameUser.setAssist(attacker2.gameUser.getAssist() + 1);
                    attacker2.characterRecord.setKill(attacker2.characterRecord.getKill() + 1);
                }
            }
        });

        broadcastPlayerKillMessage();
        selfHarmDamage = 0;
        damageMap.clear();

        if (gameUser != null) {
            gameUser.setDeath(gameUser.getDeath() + 1);
            characterRecord.setDeath(characterRecord.getDeath() + 1);
        }

        cancelAction();
        respawn();
    }

    /**
     * 사망 후 리스폰 작업을 수행한다.
     */
    private void respawn() {
        Location deadLocation = (gameUser == null ? LocationUtil.getLobbyLocation() : gameUser.getRespawnLocation()).add(0, 2, 0);
        user.teleport(deadLocation);

        CooldownUtil.setCooldown(this, Cooldown.RESPAWN.id, Cooldown.RESPAWN.duration);
        entity.setGameMode(GameMode.SPECTATOR);
        entity.setVelocity(new Vector());

        TaskUtil.addTask(this, new IntervalTask(i -> {
            long cooldown = CooldownUtil.getCooldown(CombatUser.this, Cooldown.RESPAWN.id);
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
        return CooldownUtil.getCooldown(this, Cooldown.RESPAWN.id) > 0;
    }

    /**
     * 지정한 스킬 정보에 해당하는 스킬을 반환한다.
     *
     * @param skillInfo 스킬 정보 객체
     * @return 스킬 객체
     */
    public Skill getSkill(@NonNull SkillInfo skillInfo) {
        return skillMap.get(skillInfo);
    }

    /**
     * 플레이어의 전역 쿨타임이 끝났는 지 확인한다.
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
     */
    public void setGlobalCooldown(int cooldown) {
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
     * @param score   점수 증가량
     */
    public void addScore(@NonNull String context, double score) {
        if (gameUser != null)
            gameUser.setScore(gameUser.getScore() + score);

        CooldownUtil.setCooldown(this, Cooldown.SCORE_DISPLAY_DURATION.id, Cooldown.SCORE_DISPLAY_DURATION.duration);
        if (scoreMap.size() > 5)
            scoreMap.remove(scoreMap.keySet().iterator().next());

        scoreStreakSum += score;
        scoreMap.put(context, scoreMap.getOrDefault(context, 0.0) + score);

        user.clearSidebar();
        sendScoreSidebar();
        TaskUtil.addTask(this, new IntervalTask(i -> CooldownUtil.getCooldown(CombatUser.this, Cooldown.SCORE_DISPLAY_DURATION.id) != 0,
                isCancelled -> {
                    scoreStreakSum = 0;
                    scoreMap.clear();
                    user.clearSidebar();
                }, 1));
    }

    /**
     * 사이드바에 획득 점수 목록을 출력한다.
     */
    private void sendScoreSidebar() {
        int i = 0;
        user.setSidebarName(MessageFormat.format("{0}+{1}", "§a", (int) scoreStreakSum));
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
     * @throws IllegalArgumentException {@code value}가 0~1 사이가 아니면 발생
     */
    public void setUltGaugePercent(double value) {
        if (value < 0 || value > 1)
            throw new IllegalArgumentException("'value'가 0에서 1 사이여야 함");
        if (!isActivated)
            value = 0;

        if (value == 1) {
            value = 0.999;
            Skill skill = skillMap.get(character.getUltimateSkillInfo());
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
     */
    public void addUltGaugePercent(double value) {
        Skill skill = skillMap.get(character.getUltimateSkillInfo());
        if (skill.isDurationFinished())
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
     * 플레이어의 전투원을 설정하고 무기와 스킬을 초기화한다.
     *
     * @param characterType 전투원
     */
    public void setCharacterType(@NonNull CharacterType characterType) {
        reset();
        Character realCharacter = characterType.getCharacter();

        damageModule.setMaxHealth(realCharacter.getHealth());
        damageModule.setHealth(realCharacter.getHealth());
        moveModule.getSpeedStatus().setBaseValue(DEFAULT_SPEED * realCharacter.getSpeedMultiplier());
        entity.getInventory().setItem(9, CommunicationItem.REQ_HEAL.guiItem.getItemStack());
        entity.getInventory().setItem(10, CommunicationItem.SHOW_ULT.guiItem.getItemStack());
        entity.getInventory().setItem(11, CommunicationItem.REQ_RALLY.guiItem.getItemStack());

        double hitboxMultiplier = realCharacter.getHitboxMultiplier();
        for (Hitbox hitbox : hitboxes) {
            hitbox.setSizeX(hitbox.getSizeX() * hitboxMultiplier);
            hitbox.setSizeZ(hitbox.getSizeZ() * hitboxMultiplier);
        }

        this.characterType = characterType;
        this.character = realCharacter;
        this.characterRecord = user.getUserData().getCharacterRecord(characterType);
        initActions();

        TaskUtil.addTask(this, SkinUtil.applySkin(entity, realCharacter.getSkinName()));
        TaskUtil.addTask(this, new IntervalTask(i -> {
            entity.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING,
                    1, 0, false, false), true);
            return true;
        }, 1, 10));

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
        entity.setAllowFlight(false);
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
        for (ActionKey actionKey : weapon.getDefaultActionKeys())
            actionMap.get(actionKey).add(weapon);

        for (int i = 1; i <= 4; i++) {
            ActiveSkillInfo activeSkillInfo = character.getActiveSkillInfo(i);
            if (activeSkillInfo != null) {
                Skill skill = activeSkillInfo.createSkill(this);
                skillMap.put(activeSkillInfo, skill);
                for (ActionKey actionKey : skill.getDefaultActionKeys())
                    actionMap.get(actionKey).add(skill);
            }
        }
        for (int i = 1; i <= 4; i++) {
            PassiveSkillInfo passiveSkillInfo = character.getPassiveSkillInfo(i);
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
        if (fullAutoTask == null || fullAutoTask.isDisposed())
            fullAutoTask = null;
        else if (CooldownUtil.getCooldown(weapon, Cooldown.WEAPON_FULLAUTO.id) > 0) {
            CooldownUtil.setCooldown(weapon, Cooldown.WEAPON_FULLAUTO.id, Cooldown.WEAPON_FULLAUTO.duration);
            return;
        }

        CooldownUtil.setCooldown(weapon, Cooldown.WEAPON_FULLAUTO.id, Cooldown.WEAPON_FULLAUTO.duration);

        if (fullAutoTask == null) {
            fullAutoTask = new IntervalTask(new Function<Long, Boolean>() {
                int j = 0;

                @Override
                public Boolean apply(Long i) {
                    if (CooldownUtil.getCooldown(weapon, Cooldown.WEAPON_FULLAUTO.id) == 0)
                        return false;

                    if (weapon.canUse() && !isDead() && isGlobalCooldownFinished() && ((FullAuto) weapon).getFullAutoModule().isFireTick(i)) {
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
     * @param duration  지속시간 (tick)
     * @param isRight   왼손/오른손. {@code false}로 지정 시 왼손, {@code true}로 지정 시 오른손
     */
    public void playMeleeAttackAnimation(int amplifier, int duration, boolean isRight) {
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
     * 의사소통 GUI 아이템 목록.
     */
    @Getter
    public enum CommunicationItem {
        /** 치료 요청 */
        REQ_HEAL("§a치료 요청", player -> {
        }),
        /** 궁극기 상태 */
        SHOW_ULT("§a궁극기 상태", player -> {
        }),
        /** 집결 요청 */
        REQ_RALLY("§a집결 요청", player -> {
        });

        /** GUI 아이템 객체 */
        private final GuiItem guiItem;

        CommunicationItem(String name, Consumer<Player> action) {
            ItemBuilder itemBuilder = new ItemBuilder(Material.STAINED_GLASS_PANE)
                    .setDamage((short) 5)
                    .setName(name);

            this.guiItem = new GuiItem("CombatUser" + this, itemBuilder.build()) {
                @Override
                public boolean onClick(@NonNull ClickType clickType, @NonNull ItemStack clickItem, @NonNull Player player) {
                    if (clickType != ClickType.LEFT)
                        return false;

                    action.accept(player);
                    player.closeInventory();

                    return true;
                }
            };
        }
    }

    /**
     * 쿨타임 ID 및 기본 지속시간 목록.
     */
    @AllArgsConstructor
    @Getter
    public enum Cooldown {
        /** 힐 팩 */
        HEAL_PACK("HealPack", GeneralConfig.getCombatConfig().getHealPackCooldown()),
        /** 점프대 */
        JUMP_PAD("JumpPad", 10),
        /** 무기 사용 시 달리기 금지 */
        WEAPON_NO_SPRINT("WeaponNoSprint", 7),
        /** 적 타격 시 생명력 홀로그램 */
        HIT_HEALTH_HOLOGRAM("HitHealthHologram", 20),
        /** 적 처치 기여 (데미지 누적) 제한시간 */
        DAMAGE_SUM_TIME_LIMIT("DamageSumTimeLimit", 10 * 20),
        /** 암살 보너스 (첫 공격 후 일정시간 안에 적 처치) 제한시간 (tick) */
        FASTKILL_TIME_LIMIT("FastkillTimeLimit", (long) (2.5 * 20)),
        /** 리스폰 시간 */
        RESPAWN("Respawn", GeneralConfig.getCombatConfig().getRespawnTime()),
        /** 동작 전역 쿨타임 */
        ACTION_GLOBAL_COOLDOWN("ActionGlobalCooldown", 0),
        /** 획득 점수 표시 유지시간 (tick) */
        SCORE_DISPLAY_DURATION("ScoreDisplayDuration", 5 * 20),
        /** 연사가 가능한 총기류의 쿨타임 */
        WEAPON_FULLAUTO("WeaponFullauto", 6);

        /** 쿨타임 ID */
        private final String id;
        /** 기본 지속시간 (tick) */
        private final long duration;
    }
}
