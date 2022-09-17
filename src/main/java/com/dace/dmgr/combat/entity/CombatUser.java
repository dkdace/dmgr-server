package com.dace.dmgr.combat.entity;

import com.dace.dmgr.combat.SkillController;
import com.dace.dmgr.combat.SkillTrigger;
import com.dace.dmgr.combat.WeaponController;
import com.dace.dmgr.combat.character.HasCSWeapon;
import com.dace.dmgr.combat.character.ICharacter;
import com.dace.dmgr.gui.ItemBuilder;
import com.dace.dmgr.gui.slot.CommunicationSlot;
import com.dace.dmgr.system.SkinManager;
import com.dace.dmgr.util.VectorUtil;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class CombatUser extends CombatEntity<Player> {
    private final Map<String, Integer> shield = new HashMap<>();
    private final Map<CombatUser, Float> damageList = new HashMap<>();
    private final SkillController[] passiveSkillControllers = new SkillController[3];
    private final SkillController[] activeSkillControllers = new SkillController[4];
    private WeaponController weaponController = null;
    private ICharacter character = null;

    public CombatUser(Player entity) {
        super(entity, entity.getName());
    }

    public SkillController getPassiveSkillController(int index) {
        return passiveSkillControllers[index];
    }

    public SkillController getActiveSkillController(int index) {
        return activeSkillControllers[index];
    }

    public WeaponController getWeaponController() {
        return weaponController;
    }

    public Map<CombatUser, Float> getDamageList() {
        return damageList;
    }

    public float getUlt() {
        if (entity.getExp() >= 0.999)
            return 1;
        return entity.getExp();
    }

    public void setUlt(float value) {
        if (value >= 1) value = 0.999F;
        entity.setExp(value);
        entity.setLevel(Math.round(value * 100));
    }

    public void addUlt(float value) {
        setUlt(getUlt() + value);
    }

    public ICharacter getCharacter() {
        return character;
    }

    public void setCharacter(ICharacter character) {
        try {
            reset();
            SkinManager.applySkin(entity, character.getSkinName());
            setMaxHealth(character.getCharacterStats().getHealth());
            setHealth(character.getCharacterStats().getHealth());
            weaponController = new WeaponController(this, character.getCharacterStats().getWeapon());
            entity.getInventory().setItem(9, ItemBuilder.fromSlotItem(CommunicationSlot.REQ_HEAL).build());
            entity.getInventory().setItem(10, ItemBuilder.fromSlotItem(CommunicationSlot.SHOW_ULT).build());
            entity.getInventory().setItem(11, ItemBuilder.fromSlotItem(CommunicationSlot.REQ_RALLY).build());

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
        for (int i = 0; i < passiveSkillControllers.length; i++) {
            if (passiveSkillControllers[i] != null) {
                passiveSkillControllers[i].clear();
                passiveSkillControllers[i] = null;
            }
            if (character.getCharacterStats().getPassive(i + 1) != null) {
                passiveSkillControllers[i] = new SkillController(this, character.getCharacterStats().getPassive(i + 1));
            }
        }
        for (int i = 0; i < activeSkillControllers.length; i++) {
            if (activeSkillControllers[i] != null) {
                activeSkillControllers[i].clear();
                activeSkillControllers[i] = null;
            }
            if (character.getCharacterStats().getActive(i + 1) != null)
                activeSkillControllers[i] = new SkillController(this, character.getCharacterStats().getActive(i + 1), i);
        }
    }

    public void onLeftClick() {
        if (character != null)
            character.useWeaponLeft(this, weaponController);
    }

    public void onRightClick() {
        if (character != null)
            character.useWeaponRight(this, weaponController);
    }

    public void onWeaponShoot() {
        if (character != null && character instanceof HasCSWeapon)
            ((HasCSWeapon) character).useWeaponShoot(this);
    }

    public void onItemHeld(int slot) {
        if (slot >= 0 && slot <= 3)
            if (character != null && activeSkillControllers[slot] != null)
                character.useActive(slot + 1, this, activeSkillControllers[slot]);
    }

    public void onToggleSprint(boolean sprint) {
        if (character != null) {
            for (int i = 0; i < passiveSkillControllers.length; i++) {
                if (passiveSkillControllers[i] != null && passiveSkillControllers[i].getSkill().getSkillTrigger() == SkillTrigger.SPRINT)
                    character.usePassive(i + 1, this, passiveSkillControllers[i]);
            }
        }
    }

    public Location getLeftHand() {
        return entity.getEyeLocation().subtract(0, 0.2, 0)
                .add(VectorUtil.getPitchAxis(entity.getLocation()).multiply(0.2));
    }

    public Location getRightHand() {
        return entity.getEyeLocation().subtract(0, 0.2, 0)
                .add(VectorUtil.getPitchAxis(entity.getLocation()).multiply(-0.2));
    }
}
