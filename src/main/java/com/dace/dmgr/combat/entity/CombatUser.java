package com.dace.dmgr.combat.entity;

import com.comphenix.packetwrapper.WrapperPlayServerUpdateHealth;
import com.comphenix.packetwrapper.WrapperPlayServerWorldBorder;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.dace.dmgr.combat.DamageType;
import com.dace.dmgr.combat.action.Action;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import com.dace.dmgr.combat.action.info.PassiveSkillInfo;
import com.dace.dmgr.combat.action.info.SkillInfo;
import com.dace.dmgr.combat.action.info.WeaponInfo;
import com.dace.dmgr.combat.action.skill.HasEntities;
import com.dace.dmgr.combat.action.skill.HasEntity;
import com.dace.dmgr.combat.action.skill.Skill;
import com.dace.dmgr.combat.action.skill.UltimateSkill;
import com.dace.dmgr.combat.action.weapon.Aimable;
import com.dace.dmgr.combat.action.weapon.Weapon;
import com.dace.dmgr.combat.character.Character;
import com.dace.dmgr.combat.character.jager.action.JagerT1Info;
import com.dace.dmgr.combat.entity.statuseffect.StatusEffectType;
import com.dace.dmgr.gui.item.CombatItem;
import com.dace.dmgr.lobby.Lobby;
import com.dace.dmgr.system.*;
import com.dace.dmgr.system.task.TaskTimer;
import com.dace.dmgr.util.LocationUtil;
import com.dace.dmgr.util.ParticleUtil;
import com.dace.dmgr.util.RegionUtil;
import com.dace.dmgr.util.SoundUtil;
import lombok.Getter;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 전투 시스템의 플레이어 정보를 관리하는 클래스.
 */
public final class CombatUser extends CombatEntityBase<Player> implements Healable, Living, Jumpable, HasCritHitbox {
    /** 초당 궁극기 충전량 */
    public static final int IDLE_ULT_CHARGE = 10;
    /** 기본 이동속도 */
    public static final float BASE_SPEED = 0.24F;
    /** 적 처치 기여 (데미지 누적) 제한시간 */
    public static final int DAMAGE_SUM_TIME_LIMIT = 10 * 20;
    /** 암살 보너스 (첫 공격 후 일정시간 안에 적 처치) 제한시간 */
    public static final int FASTKILL_TIME_LIMIT = (int) 2.5 * 20;
    /** 리스폰 시간 */
    public static final int RESPAWN_TIME = 10 * 20;

    /** 치명타 히트박스 객체 */
    @Getter
    private final Hitbox critHitbox;
    /** 보호막 (노란 체력) 목록 (보호막 이름 : 보호막의 양) */
    private final HashMap<String, Integer> shield = new HashMap<>();
    /** 킬 기여자 목록. 처치 점수 분배에 사용한다. (킬 기여자 : 기여도) */
    @Getter
    private final HashMap<CombatUser, Float> damageMap = new HashMap<>();
    /** 액션바 텍스트 객체 */
    private final TextComponent actionBar = new TextComponent();
    /** 동작 사용 키 매핑 목록 (동작 사용 키 : 동작) */
    @Getter
    private final EnumMap<ActionKey, Action> actionMap = new EnumMap<>(ActionKey.class);
    /** 스킬 객체 목록 (스킬 정보 : 스킬) */
    private final HashMap<SkillInfo, Skill> skillMap = new HashMap<>();
    /** 선택한 전투원 */
    @Getter
    private Character character = null;
    /** 무기 객체 */
    @Getter
    private Weapon weapon;
    /** 현재 무기 탄퍼짐 */
    @Getter
    private float bulletSpread = 0;

    /**
     * 전투 시스템의 플레이어 인스턴스를 생성한다.
     *
     * <p>{@link CombatEntityBase#init()}을 호출하여 초기화해야 한다.</p>
     *
     * @param entity 대상 플레이어
     */
    public CombatUser(Player entity) {
        super(entity, entity.getName(),
                new FixedPitchHitbox(entity.getLocation(), 0.5, 0.7, 0.3, 0, 0, 0, 0, 0.35, 0),
                new FixedPitchHitbox(entity.getLocation(), 0.8, 0.7, 0.45, 0, 0, 0, 0, 1.05, 0),
                new Hitbox(entity.getLocation(), 0.45, 0.45, 0.45, 0, 0.225, 0, 0, 1.4, 0),
                new Hitbox(entity.getLocation(), 0.45, 0.1, 0.45, 0, 0.4, 0, 0, 1.4, 0)
        );
        critHitbox = hitboxes[3];
    }

    public void onInitDamageable() {
        EntityInfoRegistry.addCombatUser(entity, this);
        abilityStatusManager.getAbilityStatus(Ability.SPEED).setBaseValue(BASE_SPEED);
    }

    @Override
    public void onTickMovable(int i) {
        character.onTick(this, i);
        entity.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING,
                99999, 0, false, false), true);

        hitboxes[2].setAxisOffsetY(entity.isSneaking() ? 1.15 : 1.4);
        hitboxes[3].setAxisOffsetY(entity.isSneaking() ? 1.15 : 1.4);

        setCanSprint(canSprint());

        if (i % 10 == 0) {
            addUltGauge((float) IDLE_ULT_CHARGE / 2);
        }

        if (isLowHealth()) {
            playBleedingEffect(1);
            setLowHealthScreenEffect(true);
        } else
            setLowHealthScreenEffect(false);

        double speed = abilityStatusManager.getAbilityStatus(Ability.SPEED).getValue();
        if (entity.isSprinting()) {
            speed *= 0.88F;
            if (!entity.isOnGround())
                speed *= speed / BASE_SPEED;
        }
        if (!canMove())
            speed = 0.0001F;

        entity.setWalkSpeed((float) speed);

        onTickActionbar();
    }

    @Override
    public int getMaxHealth() {
        return character == null ? 1000 : Healable.super.getMaxHealth();
    }

    @Override
    public boolean isUltProvider() {
        return true;
    }

    @Override
    public boolean canTakeDamage() {
        if (character == null)
            return false;
        if (entity.getGameMode() != GameMode.SURVIVAL)
            return false;

        return true;
    }

    @Override
    public boolean canDie() {
        if (character == null)
            return false;
        if (RegionUtil.isInRegion(entity, "BattleTrain"))
            return false;

        return true;
    }

    /**
     * 플레이어의 달리기 가능 여부를 설정한다.
     *
     * @param canSprint 달리기 가능 여부
     */
    public void setCanSprint(boolean canSprint) {
        WrapperPlayServerUpdateHealth packet = new WrapperPlayServerUpdateHealth();

        packet.setHealth((float) this.getEntity().getHealth());
        if (canSprint)
            packet.setFood(19);
        else
            packet.setFood(2);

        packet.sendPacket(this.getEntity());
    }

    /**
     * 플레이어가 달리기를 할 수 있는 지 확인한다.
     *
     * @return 달리기 가능 여부
     */
    private boolean canSprint() {
        if (hasStatusEffect(StatusEffectType.STUN) || hasStatusEffect(StatusEffectType.SNARE) || hasStatusEffect(StatusEffectType.GROUNDING))
            return false;
        if (CooldownManager.getCooldown(this, Cooldown.NO_SPRINT) > 0)
            return false;
        if (weapon instanceof Aimable && ((Aimable) weapon).isAiming())
            return false;
        if (propertyManager.getValue(Property.FREEZE) >= JagerT1Info.NO_SPRINT)
            return false;

        return true;
    }

    @Override
    public void onAttack(Damageable victim, int damage, DamageType damageType, boolean isCrit, boolean isUlt) {
        if (this == victim)
            return;
        if (character == null)
            return;

        character.onAttack(this, victim, damage, damageType, isCrit, isUlt);

        if (damageType == DamageType.NORMAL) {
            if (isCrit)
                playCritAttackEffect();
            else
                playAttackEffect();
        }

        if (victim.isUltProvider() && isUlt)
            addUltGauge(damage);
    }

    /**
     * 공격했을 때 효과를 재생한다.
     */
    private void playAttackEffect() {
        entity.sendTitle("", SUBTITLES.HIT, 0, 2, 10);
        SoundUtil.play("random.stab", 0.4F, 2F, entity);
        SoundUtil.play(Sound.ENTITY_GENERIC_SMALL_FALL, 0.4F, 1.5F, entity);
    }

    /**
     * 공격했을 때 효과를 재생한다. (치명타)
     */
    private void playCritAttackEffect() {
        entity.sendTitle("", SUBTITLES.CRIT, 0, 2, 10);
        SoundUtil.play(Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.6F, 1.9F, entity);
        SoundUtil.play(Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.35F, 0F, entity);
    }

    @Override
    public void onDamage(CombatEntity attacker, int damage, DamageType damageType, boolean isCrit, boolean isUlt) {
        if (this == attacker)
            return;
        if (character == null)
            return;

        character.onDamage(this, attacker, damage, damageType, isCrit, isUlt);

        if (attacker instanceof SummonEntity)
            attacker = ((SummonEntity<?>) attacker).getOwner();
        if (attacker instanceof CombatUser) {
            if (CooldownManager.getCooldown(attacker, Cooldown.DAMAGE_SUM_TIME_LIMIT, entity.getEntityId()) == 0)
                CooldownManager.setCooldown(attacker, Cooldown.FASTKILL_TIME_LIMIT, entity.getEntityId());
            CooldownManager.setCooldown(attacker, Cooldown.DAMAGE_SUM_TIME_LIMIT, entity.getEntityId());

            if (getHealth() - damage <= 0)
                damage = getHealth();
            float sumDamage = damageMap.getOrDefault(attacker, 0F);
            damageMap.put((CombatUser) attacker, Math.min(sumDamage + (float) damage / getMaxHealth(), 1));
        }
    }

    @Override
    public void onGiveHeal(Healable target, int amount, boolean isUlt) {
        if (character == null)
            return;

        character.onGiveHeal(this, target, amount, isUlt);

        if (target.isUltProvider() && isUlt)
            addUltGauge(amount);
    }

    @Override
    public void onTakeHeal(CombatEntity provider, int amount, boolean isUlt) {
        if (character == null)
            return;

        character.onTakeHeal(this, provider, amount, isUlt);

        playTakeHealEffect(amount);
    }

    /**
     * 치유를 받았을 때 효과를 재생한다.
     *
     * @param amount 치유량
     */
    private void playTakeHealEffect(int amount) {
        if (amount > 100)
            ParticleUtil.play(Particle.HEART, LocationUtil.getLocationFromOffset(entity.getLocation(),
                            0, entity.getHeight() + 0.3, 0), (int) Math.ceil(amount / 100F),
                    0.3F, 0.1F, 0.3F, 0);
        else if (amount / 100F > Math.random()) {
            ParticleUtil.play(Particle.HEART, LocationUtil.getLocationFromOffset(entity.getLocation(),
                    0, entity.getHeight() + 0.3, 0), 1, 0.3F, 0.1F, 0.3F, 0);
        }
    }

    @Override
    public void onKill(CombatEntity victim) {
        if (character == null)
            return;

        character.onKill(this, victim);

        if (victim instanceof CombatUser) {
            float damage = ((CombatUser) victim).getDamageMap().getOrDefault(this, 0F);
            int score = Math.round(damage * 100);

            playPlayerKillEffect((CombatUser) victim, score);
        } else
            playEntityKillEffect();
    }

    /**
     * 다른 플레이어를 처치했을 때 효과를 재생한다.
     *
     * @param victim 피격자
     * @param score  처치 점수
     */
    private void playPlayerKillEffect(CombatUser victim, int score) {
        entity.sendTitle("", SUBTITLES.KILL_PLAYER, 0, 2, 10);
        entity.sendMessage(SystemPrefix.CHAT + "§e§n" + victim.getName() + "§f 처치 §a§l[+" + score + "]");
        SoundUtil.play(Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1F, 1.25F, entity);
        SoundUtil.play(Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.6F, 1.25F, entity);
    }

    /**
     * 다른 플레이어 처치를 도왔을 때 효과를 재생한다.
     *
     * @param victim 피격자
     * @param score  처치 점수
     */
    private void playPlayerAssistEffect(CombatUser victim, int score) {
        entity.sendTitle("", SUBTITLES.KILL_PLAYER, 0, 2, 10);
        entity.sendMessage(SystemPrefix.CHAT + "§e§n" + victim.getName() + "§f 처치 도움 §a§l[+" + score + "]");
        SoundUtil.play(Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1F, 1.25F, entity);
        SoundUtil.play(Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.6F, 1.25F, entity);
    }

    /**
     * 처치 시 킬로그를 표시한다.
     *
     * @param victim 피격자
     */
    private void broadcastPlayerKillMessage(CombatEntity victim) {
        Map<CombatUser, Float> damageList = ((CombatUser) victim).getDamageMap();

        Set<String> attackerNames = damageList.keySet().stream().map((CombatUser attacker2) ->
                "§f\u3000§l" + attacker2.getName()).collect(Collectors.toSet());
        String victimName = "§f\u3000§l" + victim.getName();

        if (!damageList.isEmpty()) {
            Bukkit.getServer().broadcastMessage(SystemPrefix.CHAT +
                    String.join(" ,", attackerNames) + " §4§l-> " + victimName);
        }
    }

    /**
     * 플레이어 외의 엔티티를 처치했을 때 효과를 재생한다.
     */
    private void playEntityKillEffect() {
        entity.sendTitle("", SUBTITLES.KILL_ENTITY, 0, 2, 10);
        SoundUtil.play(Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1F, 1.25F, entity);
        SoundUtil.play(Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.6F, 1.25F, entity);
    }

    @Override
    public void onDeath(CombatEntity attacker) {
        if (character == null)
            return;

        character.onDeath(this, attacker);

        if (CooldownManager.getCooldown(this, Cooldown.RESPAWN_TIME) == 0) {
            setHealth(getMaxHealth());

            damageMap.forEach((CombatUser attacker2, Float damage) -> {
                if (attacker2 != ((attacker instanceof SummonEntity) ? ((SummonEntity<?>) attacker).getOwner() : attacker)) {
                    int score = Math.round(damage * 100);

                    attacker2.playPlayerAssistEffect(this, score);
                }
            });

            broadcastPlayerKillMessage(this);
            damageMap.clear();

            Location deadLocation = entity.getLocation().add(0, 0.5, 0);
            deadLocation.setPitch(90);

            CooldownManager.setCooldown(this, Cooldown.RESPAWN_TIME);
            entity.setGameMode(GameMode.SPECTATOR);
            entity.setVelocity(new Vector());

            new TaskTimer(1) {
                @Override
                public boolean run(int i) {
                    long cooldown = CooldownManager.getCooldown(CombatUser.this, Cooldown.RESPAWN_TIME);
                    if (EntityInfoRegistry.getCombatUser(entity) == null || cooldown <= 0)
                        return false;

                    entity.sendTitle("§c§l죽었습니다!",
                            String.format("%.1f", cooldown / 20F) + "초 후 부활합니다.", 0, 20, 10);
                    entity.teleport(deadLocation);

                    return true;
                }

                @Override
                public void onEnd(boolean cancelled) {
                    setHealth(getMaxHealth());
                    entity.teleport(Lobby.lobbyLocation);
                    entity.setGameMode(GameMode.SURVIVAL);
                }
            };
        }
    }

    public Skill getSkill(SkillInfo skillInfo) {
        return skillMap.get(skillInfo);
    }

    /**
     * 현재 무기 탄퍼짐을 증가시킨다.
     *
     * @param bulletSpread 무기 탄퍼짐. 최소 값은 {@code 0}, 최대 값은 {@code max}
     * @param max          무기 탄퍼짐 최대치
     */
    public void addBulletSpread(float bulletSpread, float max) {
        this.bulletSpread += bulletSpread;
        if (this.bulletSpread < 0) this.bulletSpread = 0;
        if (this.bulletSpread > max) this.bulletSpread = max;
    }

    /**
     * 플레이어의 전역 쿨타임이 끝났는 지 확인한다.
     *
     * @return 전역 쿨타임 종료 여부
     */
    public boolean isGlobalCooldownFinished() {
        return CooldownManager.getCooldown(this, Cooldown.GLOBAL_COOLDOWN) == 0;
    }

    /**
     * 플레이어의 전역 쿨타임을 설정한다.
     *
     * @param cooldown 쿨타임 (tick). {@code -1}로 설정 시 무한 지속
     */
    public void setGlobalCooldown(int cooldown) {
        CooldownManager.setCooldown(this, Cooldown.GLOBAL_COOLDOWN, cooldown == -1 ? 9999 : cooldown);
        entity.setCooldown(SkillInfo.MATERIAL, cooldown);
        entity.setCooldown(WeaponInfo.MATERIAL, cooldown);
    }

    /**
     * 플레이어의 궁극기 게이지 백분율을 반환한다.
     *
     * @return 궁극기 게이지. {@code 0 ~ 1} 사이의 값
     */
    public float getUltGaugePercent() {
        if (entity.getExp() >= 0.999)
            return 1;

        return entity.getExp();
    }

    /**
     * 플레이어의 궁극기 게이지 백분율을 설정한다.
     *
     * @param value 궁극기 게이지. {@code 0 ~ 1} 사이의 값
     */
    public void setUltGaugePercent(float value) {
        if (character == null)
            value = 0;
        if (value >= 1) {
            onUltReady();
            value = 0.999F;
        }
        entity.setExp(value);
        entity.setLevel(Math.round(value * 100));
    }

    /**
     * 플레이어의 궁극기 게이지를 증가시킨다. (백분율)
     *
     * @param value 추가할 궁극기 게이지
     */
    public void addUltGaugePercent(float value) {
        setUltGaugePercent(getUltGaugePercent() + value);
    }

    /**
     * 플레이어의 궁극기 게이지를 증가시킨다.
     *
     * @param value 추가할 궁극기 게이지
     */
    public void addUltGauge(float value) {
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
     * @param character 전투원
     */
    public void setCharacter(Character character) {
        reset();
        SkinManager.applySkin(entity, character.getSkinName());
        setMaxHealth(character.getHealth());
        setHealth(character.getHealth());
        abilityStatusManager.getAbilityStatus(Ability.SPEED).setBaseValue(BASE_SPEED * character.getSpeedMultiplier());
        entity.getInventory().setItem(9, CombatItem.REQ_HEAL.getItemStack());
        entity.getInventory().setItem(10, CombatItem.SHOW_ULT.getItemStack());
        entity.getInventory().setItem(11, CombatItem.REQ_RALLY.getItemStack());

        this.character = character;
        initActions();
    }

    /**
     * 플레이어의 상태를 초기화한다.
     */
    public void reset() {
        entity.getInventory().setHeldItemSlot(4);
        entity.getActivePotionEffects().forEach((potionEffect ->
                entity.removePotionEffect(potionEffect.getType())));
        setUltGaugePercent(0);
        setLowHealthScreenEffect(false);
        entity.setFlying(false);
        skillMap.forEach((skillInfo, skill) -> {
            if (skill instanceof HasEntities)
                ((HasEntities<?>) skill).getSummonEntities().forEach(SummonEntity::remove);
            if (skill instanceof HasEntity && ((HasEntity<?>) skill).getSummonEntity() != null)
                ((HasEntity<?>) skill).getSummonEntity().remove();
        });
    }

    /**
     * 플레이어의 동작 설정을 초기화한다. 전투원 선택 시 호출해야 한다.
     */
    private void initActions() {
        actionMap.clear();
        skillMap.clear();
        for (int i = 0; i < 4; i++) {
            entity.getInventory().clear(i);
        }

        weapon = character.getWeaponInfo().createWeapon(this);
        for (ActionKey actionKey : weapon.getDefaultActionKeys()) {
            actionMap.put(actionKey, weapon);
        }
        for (int i = 1; i <= 4; i++) {
            ActiveSkillInfo activeSkillInfo = character.getActiveSkillInfo(i);
            if (activeSkillInfo != null) {
                Skill skill = activeSkillInfo.createSkill(this);
                skillMap.put(activeSkillInfo, skill);
                for (ActionKey actionKey : skill.getDefaultActionKeys()) {
                    actionMap.put(actionKey, skill);
                }
            }
        }
        for (int i = 1; i <= 4; i++) {
            PassiveSkillInfo passiveSkillInfo = character.getPassiveSkillInfo(i);
            if (passiveSkillInfo != null) {
                Skill skill = passiveSkillInfo.createSkill(this);
                skillMap.put(passiveSkillInfo, skill);
                for (ActionKey actionKey : skill.getDefaultActionKeys()) {
                    actionMap.put(actionKey, skill);
                }
            }
        }
    }

    /**
     * 플레이어에게 액션바를 전송한다.
     *
     * @param message       메시지
     * @param overrideTicks 지속시간 (tick)
     */
    public void sendActionBar(String message, long overrideTicks) {
        if (overrideTicks > 0)
            CooldownManager.setCooldown(this, Cooldown.ACTION_BAR, overrideTicks);

        actionBar.setText(message);
        entity.spigot().sendMessage(ChatMessageType.ACTION_BAR, actionBar);
    }

    /**
     * 플레이어에게 액션바를 전송한다.
     *
     * @param message 메시지
     */
    public void sendActionBar(String message) {
        sendActionBar(message, 0);
    }

    /**
     * 출혈 효과를 재생한다.
     *
     * @param count 파티클 수
     */
    public void playBleedingEffect(int count) {
        ParticleUtil.playBlock(ParticleUtil.BlockParticle.BLOCK_DUST, Material.REDSTONE_BLOCK, 0,
                entity.getLocation().add(0, entity.getHeight() / 2, 0), count, 0.2F, 0.35F, 0.2F, 0.03F);
    }

    /**
     * 플레이어에의 치명상 화면 효과 표시를 설정한다.
     *
     * @param enable 활성화 여부
     */
    private void setLowHealthScreenEffect(boolean enable) {
        WrapperPlayServerWorldBorder packet = new WrapperPlayServerWorldBorder();

        packet.setAction(EnumWrappers.WorldBorderAction.SET_WARNING_BLOCKS);
        packet.setWarningDistance(enable ? 999999999 : 0);

        packet.sendPacket(entity);
    }

    /**
     * {@link CombatUser#onTick(int)}에서 사용하며, 액션바 전송 작업을 실행한다.
     */
    private void onTickActionbar() {
        StringJoiner text = new StringJoiner("    ");

        text.add(character.getActionbarString(this));

        sendActionBar(text.toString());
    }

    /**
     * 전투에 사용되는 자막(Subtitle) 종류.
     */
    private static class SUBTITLES {
        /** 공격 */
        static final String HIT = "§f×";
        /** 공격 (치명타) */
        static final String CRIT = "§c§l×";
        /** 처치 (플레이어) */
        static final String KILL_PLAYER = "§c§lKILL";
        /** 처치 (엔티티) */
        static final String KILL_ENTITY = "§c✔";
    }
}
