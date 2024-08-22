package com.dace.dmgr.combat.character;

import com.dace.dmgr.combat.character.arkace.Arkace;
import com.dace.dmgr.combat.character.inferno.Inferno;
import com.dace.dmgr.combat.character.jager.Jager;
import com.dace.dmgr.combat.character.magritta.Magritta;
import com.dace.dmgr.combat.character.neace.Neace;
import com.dace.dmgr.combat.character.quaker.Quaker;
import com.dace.dmgr.combat.character.silia.Silia;
import com.dace.dmgr.combat.character.vellion.Vellion;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.item.ItemBuilder;
import com.dace.dmgr.item.gui.GuiItem;
import com.dace.dmgr.user.User;
import com.dace.dmgr.util.SkinUtil;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

/**
 * 지정할 수 있는 전투원의 목록.
 *
 * @see Character
 */
@Getter
public enum CharacterType {
    MAGRITTA(Magritta.getInstance()),
    SILIA(Silia.getInstance()),

    ARKACE(Arkace.getInstance()),
    JAGER(Jager.getInstance()),

    INFERNO(Inferno.getInstance()),

    QUAKER(Quaker.getInstance()),

    NEACE(Neace.getInstance()),

    VELLION(Vellion.getInstance());

    /** 전투원 정보 */
    @NonNull
    private final Character character;
    /** GUI 아이템 객체 */
    @NonNull
    private final GuiItem guiItem;

    CharacterType(Character character) {
        this.character = character;
        this.guiItem = new GuiItem(this.toString(), new ItemBuilder(Material.SKULL_ITEM)
                .setDamage((short) 3)
                .setSkullOwner(SkinUtil.getSkinUrl(character.getSkinName()))
                .setName("§c" + character.getName())
                .setLore("§f전투원 설명", toString())
                .build()) {
            @Override
            public boolean onClick(@NonNull ClickType clickType, @NonNull ItemStack clickItem, @NonNull Player player) {
                if (clickType != ClickType.LEFT)
                    return false;

                CombatUser combatUser = CombatUser.fromUser(User.fromPlayer(player));
                if (combatUser == null)
                    return false;

                CharacterType characterType = CharacterType.valueOf(CharacterType.this.toString());
                combatUser.setCharacterType(characterType);

                player.closeInventory();

                return true;
            }
        };
    }
}
