package com.dace.dmgr.combat.entity;

import com.comphenix.packetwrapper.WrapperPlayServerUpdateHealth;
import com.dace.dmgr.combat.action.Skill;
import com.dace.dmgr.combat.action.SkillController;
import com.dace.dmgr.combat.action.WeaponController;
import com.dace.dmgr.combat.character.ICharacter;
import com.dace.dmgr.gui.ItemBuilder;
import com.dace.dmgr.gui.slot.CommunicationSlot;
import com.dace.dmgr.system.Cooldown;
import com.dace.dmgr.system.CooldownManager;
import com.dace.dmgr.system.HashMapList;
import com.dace.dmgr.system.SkinManager;
import com.dace.dmgr.util.ParticleUtil;
import com.dace.dmgr.util.SoundUtil;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.HashMap;

import static com.dace.dmgr.system.HashMapList.combatUserMap;

/**
 * 전투 시스템의 플레이어 정보를 관리하는 클래스.
 */
public class CombatUser extends CombatEntity<Player> {
    /** 보호막 (노란 체력) 목록 */
    private final HashMap<String, Integer> shield = new HashMap<>();
    /** 킬 기여자 목록. 처치 점수 분배에 사용한다. */
    private final HashMap<CombatUser, Float> damageMap = new HashMap<>();
    /** 액션바 텍스트 객체 */
    private final TextComponent actionBar = new TextComponent();
    /** 선택한 전투원 */
    private ICharacter character = null;
    /** 무기 컨트롤러 객체 */
    private WeaponController weaponController;
    /** 스킬 컨트롤러 객체 목록 */
    private HashMap<Skill, SkillController> skillControllerMap = new HashMap<>();
    /** 현재 무기 탄퍼짐 */
    private float bulletSpread = 0;

    /**
     * 전투 시스템의 플레이어 인스턴스를 생성하고 {@link HashMapList#combatUserMap}에 추가한다.
     *
     * <p>플레이어가 전투 입장 시 호출해야 하며, 퇴장 시 {@link HashMapList#combatUserMap}
     * 에서 제거해야 한다.</p>
     *
     * @param entity 대상 플레이어
     */
    public CombatUser(Player entity) {
        super(
                entity,
                entity.getName(),
                new Hitbox(entity.getLocation(), 0, entity.getHeight() / 2, 0, 0.65, 2.1, 0.5),
                new Hitbox(entity.getLocation(), 0, 2.05, 0, 0.15, 0.05, 0.15)
        );
        combatUserMap.put(entity, this);
        updateHitboxTick();
    }

    public WeaponController getWeaponController() {
        return weaponController;
    }

    public SkillController getSkillController(Skill skill) {
        return skillControllerMap.get(skill);
    }

    public HashMap<CombatUser, Float> getDamageMap() {
        return damageMap;
    }

    public float getBulletSpread() {
        return bulletSpread;
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
            SkillController skillController = skillControllerMap.get(character.getUltimate());
            if (!skillController.isCooldownFinished())
                skillController.setCooldown(0);
        }
    }

    @Override
    public boolean isDamageable() {
        return entity.getGameMode() == GameMode.SURVIVAL;
    }

    public ICharacter getCharacter() {
        return character;
    }

    /**
     * 플레이어의 전투원을 설정하고 스킬을 초기화한다.
     *
     * @param character 전투원
     */
    public void setCharacter(ICharacter character) {
        try {
            reset();
            SkinManager.applySkin(entity, character.getSkinName());
            setMaxHealth(character.getHealth());
            setHealth(character.getHealth());
            entity.getInventory().setItem(9, ItemBuilder.fromSlotItem(CommunicationSlot.REQ_HEAL).build());
            entity.getInventory().setItem(10, ItemBuilder.fromSlotItem(CommunicationSlot.SHOW_ULT).build());
            entity.getInventory().setItem(11, ItemBuilder.fromSlotItem(CommunicationSlot.REQ_RALLY).build());
            weaponController = new WeaponController(this, character.getWeapon());

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
        skillControllerMap.clear();
        character.getActionKeyMap().getAll().forEach((actionKey, action) -> {
            if (action instanceof Skill) {
                int slot = -1;
                switch (actionKey) {
                    case SLOT_1:
                        slot = 0;
                        break;
                    case SLOT_2:
                        slot = 1;
                        break;
                    case SLOT_3:
                        slot = 2;
                        break;
                    case SLOT_4:
                        slot = 3;
                        break;
                }

                skillControllerMap.put((Skill) action, new SkillController(this, (Skill) action, slot));
            }
        });
    }

    /**
     * 플레이어에게 액션바를 전송한다.
     *
     * <p>{@link com.dace.dmgr.combat.CombatTick#run(CombatUser)} 스케쥴러의 액션바를 덮어쓰며, 주로 재장전과
     * 스킬 중요 알림에 사용한다.</p>
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
     * <p>{@link com.dace.dmgr.combat.CombatTick#run(CombatUser)} 스케쥴러에서 사용한다.</p>
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
}
