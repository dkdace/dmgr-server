package com.dace.dmgr.combat.entity;

import com.dace.dmgr.combat.character.Character;
import com.dace.dmgr.combat.character.GunCharacter;
import com.dace.dmgr.gui.ItemGenerator;
import com.dace.dmgr.gui.slot.CommunicationSlot;
import com.dace.dmgr.system.SkinManager;
import com.dace.dmgr.util.HasCooldown;
import com.dace.dmgr.util.VectorUtil;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

import static com.dace.dmgr.system.EntityList.combatUserList;

public class CombatUser extends CombatEntity<Player> implements HasCooldown {
    private final Map<String, Integer> shield = new HashMap<>();
    private final Map<CombatUser, Float> damageList = new HashMap<>();
    private Character character = null;

    public CombatUser(Player entity) {
        super(entity, entity.getName());
        combatUserList.put(entity.getUniqueId(), this);
    }

    public Map<CombatUser, Float> getDamageList() {
        return damageList;
    }

    public void onDamage(Entity attacker, Entity victim, int damage) {
        if (!victim.isDead()) {
            int rdamage = damage;

            if (victim.getType() != EntityType.ZOMBIE && victim.getType() != EntityType.PLAYER)
                return;
        }
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

    public Character getCharacter() {
        return character;
    }

    public void setCharacter(Character character) {
        try {
            reset();
            SkinManager.applySkin(entity, character.getSkinName());
            setMaxHealth(character.getStats().getHealth());
            setHealth(character.getStats().getHealth());
            entity.getInventory().setItem(4, character.getWeapon());
            entity.getInventory().setItem(9, ItemGenerator.getSlotItem(CommunicationSlot.REQ_HEAL));
            entity.getInventory().setItem(10, ItemGenerator.getSlotItem(CommunicationSlot.SHOW_ULT));
            entity.getInventory().setItem(11, ItemGenerator.getSlotItem(CommunicationSlot.REQ_RALLY));
            this.character = character;
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

    public void onLeftClick() {

    }

    public void onRightClick() {

    }

    public void onWeaponShoot() {
        if (character != null)
            ((GunCharacter) character).useWeaponShoot(this);
    }

    public Location getLeftHand() {
        return entity.getEyeLocation().subtract(0, 0.2, 0)
                .add(VectorUtil.getPitchAxis(entity.getLocation()).multiply(0.2));
    }

    public Location getRightHand() {
        return entity.getEyeLocation().subtract(0, 0.2, 0)
                .add(VectorUtil.getPitchAxis(entity.getLocation()).multiply(-0.2));
    }

    @Override
    public String getCooldownKey() {
        return entity.getUniqueId().toString();
    }
}
