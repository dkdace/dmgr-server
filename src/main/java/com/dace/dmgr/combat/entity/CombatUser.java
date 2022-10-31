package com.dace.dmgr.combat.entity;

import com.comphenix.packetwrapper.WrapperPlayServerUpdateHealth;
import com.dace.dmgr.combat.action.Skill;
import com.dace.dmgr.combat.action.SkillController;
import com.dace.dmgr.combat.action.WeaponController;
import com.dace.dmgr.combat.character.ICharacter;
import com.dace.dmgr.gui.ItemBuilder;
import com.dace.dmgr.gui.slot.CommunicationSlot;
import com.dace.dmgr.system.SkinManager;
import com.dace.dmgr.util.SoundUtil;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.HashMap;

import static com.dace.dmgr.system.HashMapList.combatUserMap;

public class CombatUser extends CombatEntity<Player> {
    private final HashMap<String, Integer> shield = new HashMap<>();
    private final HashMap<CombatUser, Float> damageMap = new HashMap<>();
    private ICharacter character = null;
    private WeaponController weaponController;
    private HashMap<Skill, SkillController> skillControllerMap = new HashMap<>();
    private float bulletSpread = 0;

    public CombatUser(Player entity) {
        super(entity, entity.getName(), new Hitbox(entity.getLocation(), 0, entity.getHeight() / 2, 0,
                0.65, 2.1, 0.5));
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

    public void addBulletSpread(float bulletSpread, float max) {
        this.bulletSpread += bulletSpread;
        if (this.bulletSpread < 0) this.bulletSpread = 0;
        if (this.bulletSpread > max) this.bulletSpread = max;
    }

    public void allowSprint(boolean allow) {
        WrapperPlayServerUpdateHealth packet = new WrapperPlayServerUpdateHealth();

        packet.setHealth((float) this.getEntity().getHealth());
        if (allow)
            packet.setFood(19);
        else
            packet.setFood(2);

        packet.sendPacket(this.getEntity());
    }

    public float getUlt() {
        if (entity.getExp() >= 0.999)
            return 1;
        return entity.getExp();
    }

    public void setUlt(float value) {
        if (character == null) value = 0;
        if (value >= 1) {
            chargeUlt();
            value = 0.999F;
        }
        entity.setExp(value);
        entity.setLevel(Math.round(value * 100));
    }

    public void addUlt(float value) {
        setUlt(getUlt() + value);
    }

    public void useUlt() {
        setUlt(0);
        SoundUtil.play(Sound.ENTITY_WITHER_SPAWN, entity.getLocation(), 10F, 2F);
    }

    @Override
    public boolean isUltChargeable() {
        return true;
    }

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

    private void reset() {
        entity.getInventory().setHeldItemSlot(4);
        entity.getActivePotionEffects().forEach((potionEffect ->
                entity.removePotionEffect(potionEffect.getType())));
        setUlt(0);
        entity.setFlying(false);
    }

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
}
