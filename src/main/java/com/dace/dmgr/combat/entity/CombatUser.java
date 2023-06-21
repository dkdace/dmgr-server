package com.dace.dmgr.combat.entity;

import com.comphenix.packetwrapper.WrapperPlayServerUpdateHealth;
import com.dace.dmgr.DMGR;
import com.dace.dmgr.combat.CombatTick;
import com.dace.dmgr.combat.action.skill.*;
import com.dace.dmgr.combat.action.weapon.Weapon;
import com.dace.dmgr.combat.character.Character;
import com.dace.dmgr.gui.item.CombatItem;
import com.dace.dmgr.lobby.Lobby;
import com.dace.dmgr.system.Cooldown;
import com.dace.dmgr.system.CooldownManager;
import com.dace.dmgr.system.HashMapList;
import com.dace.dmgr.system.SkinManager;
import com.dace.dmgr.system.task.TaskTimer;
import com.dace.dmgr.util.ParticleUtil;
import com.dace.dmgr.util.SoundUtil;
import lombok.Getter;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.dace.dmgr.system.HashMapList.combatUserMap;

/**
 * 전투 시스템의 플레이어 정보를 관리하는 클래스.
 */
public class CombatUser extends CombatEntity<Player> {
    /** 적 처치 기여 (데미지 누적) 제한시간 */
    public static final int DAMAGE_SUM_TIME_LIMIT = 10 * 20;
    /** 암살 보너스 (첫 공격 후 일정시간 안에 적 처치) 제한시간 */
    public static final int FASTKILL_TIME_LIMIT = (int) 2.5 * 20;
    /** 리스폰 시간 */
    public static final int RESPAWN_TIME = 10 * 20;

    /** 보호막 (노란 체력) 목록 (보호막 이름 : 보호막의 양) */
    private final HashMap<String, Integer> shield = new HashMap<>();
    /** 킬 기여자 목록. 처치 점수 분배에 사용한다. (킬 기여자 : 기여도) */
    @Getter
    private final HashMap<CombatUser, Float> damageMap = new HashMap<>();
    /** 액션바 텍스트 객체 */
    private final TextComponent actionBar = new TextComponent();
    /** 선택한 전투원 */
    @Getter
    private Character character = null;
    /** 무기 객체 */
    @Getter
    private Weapon weapon;
    /** 스킬 객체 목록 (스킬 정보 : 스킬) */
    private HashMap<SkillInfo, Skill> skillMap = new HashMap<>();
    /** 현재 무기 탄퍼짐 */
    @Getter
    private float bulletSpread = 0;

    /**
     * 전투 시스템의 플레이어 인스턴스를 생성하고 {@link HashMapList#combatUserMap}에 추가한다.
     *
     * <p>플레이어가 전투 입장 시 호출해야 하며, 퇴장 시 {@link HashMapList#combatUserMap}
     * 에서 제거해야 한다.</p>
     *
     * @param entity 대상 플레이어
     * @see HashMapList#combatUserMap
     */
    public CombatUser(Player entity) {
        super(
                entity,
                entity.getName(),
                new Hitbox(0, entity.getHeight() / 2, 0, 0.65, 2.1, 0.5),
                new Hitbox(0, 2.05, 0, 0.15, 0.05, 0.15),
                false
        );
        combatUserMap.put(entity, this);
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
     * 플레이어의 달리기 가능 여부를 설정한다.
     *
     * @param allow 달리기 가능 여부
     */
    public void allowSprint(boolean allow) {
        WrapperPlayServerUpdateHealth packet = new WrapperPlayServerUpdateHealth();

        packet.setHealth((float) this.getEntity().getHealth());
        if (allow)
            packet.setFood(19);
        else
            packet.setFood(2);

        packet.sendPacket(this.getEntity());
    }

    /**
     * 플레이어의 궁극기 게이지를 반환한다.
     *
     * @return 궁극기 게이지. {@code 0 ~ 1} 사이의 값
     */
    public float getUlt() {
        if (entity.getExp() >= 0.999)
            return 1;
        return entity.getExp();
    }

    /**
     * 플레이어의 궁극기 게이지를 설정한다.
     *
     * @param value 궁극기 게이지. {@code 0 ~ 1} 사이의 값
     */
    public void setUlt(float value) {
        if (character == null) value = 0;
        if (value >= 1) {
            chargeUlt();
            value = 0.999F;
        }
        entity.setExp(value);
        entity.setLevel(Math.round(value * 100));
    }

    /**
     * 플레이어의 궁극기 게이지를 증가시킨다.
     *
     * @param value 추가할 궁극기 게이지
     */
    public void addUlt(float value) {
        setUlt(getUlt() + value);
    }

    /**
     * 궁극기 사용 이벤트를 호출한다. 궁극기 사용 시 호출해야 한다.
     */
    public void useUlt() {
        setUlt(0);
        SoundUtil.play(Sound.ENTITY_WITHER_SPAWN, entity.getLocation(), 10F, 2F);
    }

    @Override
    public boolean isUltProvider() {
        return true;
    }

    /**
     * 플레이어의 궁극기 스킬을 충전한다.
     */
    private void chargeUlt() {
        if (character != null) {
            Skill skill = skillMap.get(character.getUltimateSkillInfo());
            if (!skill.isCooldownFinished())
                skill.setCooldown(0);
        }
    }

    @Override
    public boolean isDamageable() {
        return entity.getGameMode() == GameMode.SURVIVAL;
    }

    /**
     * 플레이어의 전투원을 설정하고 스킬을 초기화한다.
     *
     * @param character 전투원
     */
    public void setCharacter(Character character) {
        try {
            reset();
            SkinManager.applySkin(entity, character.getSkinName());
            setMaxHealth(character.getHealth());
            setHealth(character.getHealth());
            entity.getInventory().setItem(9, CombatItem.REQ_HEAL.getItemStack());
            entity.getInventory().setItem(10, CombatItem.SHOW_ULT.getItemStack());
            entity.getInventory().setItem(11, CombatItem.REQ_RALLY.getItemStack());
            weapon = character.getWeaponInfo().createWeapon(this);

            this.character = character;
            resetSkills();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 플레이어의 상태를 재설정한다. 전투원 선택 시 호출해야 한다.
     */
    private void reset() {
        entity.getInventory().setHeldItemSlot(4);
        entity.getActivePotionEffects().forEach((potionEffect ->
                entity.removePotionEffect(potionEffect.getType())));
        setUlt(0);
        entity.setFlying(false);
    }

    /**
     * 플레이어의 스킬을 재설정한다. 전투원 선택 시 호출해야 한다.
     */
    private void resetSkills() {
        skillMap.clear();

        for (int i = 1; i <= 4; i++) {
            ActiveSkillInfo activeSkillInfo = character.getActiveSkillInfo(i);
            if (activeSkillInfo != null)
                skillMap.put(activeSkillInfo, activeSkillInfo.createSkill(this));
        }
        for (int i = 1; i <= 4; i++) {
            PassiveSkillInfo passiveSkillInfo = character.getPassiveSkillInfo(i);
            if (passiveSkillInfo != null)
                skillMap.put(passiveSkillInfo, passiveSkillInfo.createSkill(this));
        }
    }

    /**
     * 플레이어에게 액션바를 전송한다.
     *
     * <p>{@link CombatTick#run(CombatUser)} 스케쥴러의 액션바를 덮어쓰며, 주로 재장전과
     * 스킬 중요 알림에 사용한다.</p>
     *
     * @param message       메시지
     * @param overrideTicks 지속시간 (tick)
     * @see CombatTick#run(CombatUser)
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
     * <p>{@link CombatTick#run(CombatUser)} 스케쥴러에서 사용한다.</p>
     *
     * @param message 메시지
     * @see CombatTick#run(CombatUser)
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

    @Override
    public void onAttack(CombatEntity<?> victim, int damage, String type, boolean isCrit, boolean isUlt) {
        if (this == victim || !victim.isUltProvider() || !isUlt)
            return;

        if (isCrit) {
            entity.sendTitle("", SUBTITLES.CRIT, 0, 2, 10);
            SoundUtil.play(Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.6F, 1.9F, entity);
            SoundUtil.play(Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.35F, 0F, entity);
        } else {
            entity.sendTitle("", SUBTITLES.HIT, 0, 2, 10);
            SoundUtil.play("random.stab", 0.4F, 2F, entity);
            SoundUtil.play(Sound.ENTITY_GENERIC_SMALL_FALL, 0.4F, 1.5F, entity);
        }
        if (!getSkill(character.getUltimateSkillInfo()).isUsing())
            addUlt((float) damage / ((HasCost) getSkill(character.getUltimateSkillInfo())).getCost());
    }

    @Override
    public void onDamage(CombatEntity<?> attacker, int damage, String type, boolean isCrit, boolean isUlt) {
        if (this == attacker)
            return;

        if (attacker instanceof SummonEntity)
            attacker = ((SummonEntity<?>) attacker).getOwner();
        if (attacker instanceof CombatUser) {
            if (CooldownManager.getCooldown(attacker, Cooldown.DAMAGE_SUM_TIME_LIMIT, entity.getEntityId()) == 0) {
                CooldownManager.setCooldown(attacker, Cooldown.FASTKILL_TIME_LIMIT, entity.getEntityId());
            }
            CooldownManager.setCooldown(attacker, Cooldown.DAMAGE_SUM_TIME_LIMIT, entity.getEntityId());

            if (getHealth() - damage <= 0)
                damage = getHealth();
            float sumDamage = damageMap.getOrDefault(attacker, 0F);
            damageMap.put(this, sumDamage + (float) damage / getMaxHealth());
            if (sumDamage > 1)
                damageMap.put(this, 1F);
        }
    }

    @Override
    public void onHeal(CombatEntity<?> victim, int amount, boolean isUlt) {
        if (isUlt)
            addUlt((float) amount / ((HasCost) getSkill(character.getUltimateSkillInfo())).getCost());
    }

    @Override
    public void onKill(CombatEntity<?> victim) {
        if (victim instanceof CombatUser) {
            victim.setHealth(victim.getMaxHealth());

            if (CooldownManager.getCooldown(victim, Cooldown.RESPAWN_TIME) == 0) {
                Map<CombatUser, Float> damageList = ((CombatUser) victim).getDamageMap();
                Set<String> attackerNames = damageList.keySet().stream().map((CombatUser _attacker) ->
                        "§f　§l" + _attacker.getName()).collect(Collectors.toSet());
                String victimName = "§f　§l" + victim.getName();

                damageList.forEach((CombatUser _attacker, Float damage) -> {
                    Player _attackerEntity = _attacker.getEntity();

                    int score = Math.round(damage * 100);

                    _attackerEntity.sendTitle("", SUBTITLES.KILL_PLAYER, 0, 2, 10);
                    if (score > 30) {
                        _attackerEntity.sendMessage(DMGR.PREFIX.CHAT + "§e§n" + victim.getName() + "§f 처치 §a§l[+" + score + "]");
                    } else {
                        _attackerEntity.sendMessage(DMGR.PREFIX.CHAT + "§e§n" + victim.getName() + "§f 처치 도움 §a§l[+" + score + "]");
                    }
                    playKillEffect();
                });

                if (damageList.size() > 0) {
                    Bukkit.getServer().broadcastMessage(DMGR.PREFIX.CHAT +
                            String.join(" ,", attackerNames) + " §4§l-> " + victimName);

                    damageList.clear();
                }
            }
        } else {
            entity.sendTitle("", SUBTITLES.KILL_ENTITY, 0, 2, 10);
            playKillEffect();

            ((TemporalEntity<?>) victim).remove();
        }
    }

    @Override
    public void onDeath(CombatEntity<?> attacker) {
        Location deadLocation = entity.getLocation().add(0, 0.5, 0);
        deadLocation.setPitch(90);

        CooldownManager.setCooldown(this, Cooldown.RESPAWN_TIME);
        entity.setGameMode(GameMode.SPECTATOR);
        entity.setVelocity(new Vector());

        new TaskTimer(1) {
            @Override
            public boolean run(int i) {
                long cooldown = CooldownManager.getCooldown(CombatUser.this, Cooldown.RESPAWN_TIME);
                if (combatUserMap.get(entity) == null || cooldown <= 0)
                    return false;

                entity.sendTitle("§c§l죽었습니다!",
                        String.format("%.1f", (float) cooldown / 20F) + "초 후 부활합니다.", 0, 20, 10);
                entity.teleport(deadLocation);

                return true;
            }

            @Override
            public void onEnd(boolean cancelled) {
                setHealth(getMaxHealth());
                entity.teleport(Lobby.lobby);
                entity.setGameMode(GameMode.SURVIVAL);
            }
        };
    }

    /**
     * 플레이어에게 처치 효과를 재생한다.
     */
    private void playKillEffect() {
        SoundUtil.play(Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1F, 1.25F, entity);
        SoundUtil.play(Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.6F, 1.25F, entity);
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
