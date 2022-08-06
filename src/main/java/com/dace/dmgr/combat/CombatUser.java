package com.dace.dmgr.combat;

import com.dace.dmgr.combat.character.Character;
import com.dace.dmgr.combat.character.GunCharacter;
import com.dace.dmgr.gui.ItemGenerator;
import com.dace.dmgr.gui.slot.CommunicationSlot;
import com.dace.dmgr.system.SkinManager;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

import static com.dace.dmgr.system.EntityList.combatUserList;

public class CombatUser implements CombatEntity {
    private final Map<String, Integer> shield = new HashMap<>();
    private final Player player;
    private Character character = null;
    private String team = "";

    public CombatUser(Player player) {
        this.player = player;
        combatUserList.put(player.getUniqueId(), this);
    }

    public Player getPlayer() {
        return player;
    }

    public void remove() {
        combatUserList.remove(player.getUniqueId());
    }

    @Override
    public String getTeam() {
        return team;
    }

    @Override
    public void setTeam(String team) {
        this.team = team;
    }

    @Override
    public int getHealth() {
        return (int) (player.getHealth() * 50);
    }

    @Override
    public void setHealth(int health) {
        double realHealth = health / 50.0;
        if (realHealth < 0) realHealth = 0;
        if (realHealth > getMaxHealth()) realHealth = getMaxHealth();
        player.setHealth(realHealth);
    }

    @Override
    public int getMaxHealth() {
        return (int) (player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue());
    }

    @Override
    public void setMaxHealth(int health) {
        player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(health / 50.0);
    }

    public float getUlt() {
        if (player.getExp() >= 0.999)
            return 1;
        return player.getExp();
    }

    public void setUlt(float value) {
        if (value >= 1) value = 0.999F;
        player.setExp(value);
        player.setLevel(Math.round(value * 100));
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
            SkinManager.applySkin(player, character.getSkinName());
            setMaxHealth(character.getHealth());
            setHealth(character.getHealth());
            player.getInventory().setItem(4, character.getWeapon());
            player.getInventory().setItem(9, ItemGenerator.getSlotItem(CommunicationSlot.REQ_HEAL));
            player.getInventory().setItem(10, ItemGenerator.getSlotItem(CommunicationSlot.SHOW_ULT));
            player.getInventory().setItem(11, ItemGenerator.getSlotItem(CommunicationSlot.REQ_RALLY));
            this.character = character;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void reset() {
        player.getInventory().setHeldItemSlot(4);
        player.getActivePotionEffects().forEach((potionEffect ->
                player.removePotionEffect(potionEffect.getType())));
        setUlt(0);
        player.setFlying(false);
    }

    public void onLeftClick() {

    }

    public void onRightClick() {

    }

    public void onWeaponShoot() {
        if (character != null)
            ((GunCharacter) character).useWeaponShoot(this);
    }
}
