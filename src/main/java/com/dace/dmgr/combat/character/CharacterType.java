package com.dace.dmgr.combat.character;

import com.dace.dmgr.combat.character.arkace.Arkace;
import com.dace.dmgr.combat.character.ched.Ched;
import com.dace.dmgr.combat.character.inferno.Inferno;
import com.dace.dmgr.combat.character.jager.Jager;
import com.dace.dmgr.combat.character.magritta.Magritta;
import com.dace.dmgr.combat.character.neace.Neace;
import com.dace.dmgr.combat.character.quaker.Quaker;
import com.dace.dmgr.combat.character.silia.Silia;
import com.dace.dmgr.combat.character.vellion.Vellion;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.game.GameUser;
import com.dace.dmgr.item.ItemBuilder;
import com.dace.dmgr.item.gui.GuiItem;
import com.dace.dmgr.item.gui.SelectCharInfo;
import com.dace.dmgr.user.User;
import com.dace.dmgr.util.SkinUtil;
import lombok.Getter;
import lombok.NonNull;
import org.apache.commons.lang3.Validate;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.text.MessageFormat;
import java.util.Arrays;

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
    CHED(Ched.getInstance()),

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
                .setName(MessageFormat.format("§f{0} {1}{2} §8§o{3}", character.getIcon(), character.getRole().getColor(), character.getName(),
                        character.getNickname()))
                .setLore("",
                        "§7§n좌클릭§f하여 전투원을 선택합니다.",
                        "§7§n우클릭§f하여 전투원 정보를 확인합니다.")
                .build()) {
            @Override
            public boolean onClick(@NonNull ClickType clickType, @NonNull ItemStack clickItem, @NonNull Player player) {
                if (!player.getOpenInventory().getTitle().contains("전투원 선택"))
                    return false;

                CombatUser combatUser = CombatUser.fromUser(User.fromPlayer(player));
                if (combatUser == null)
                    return false;

                if (clickType == ClickType.LEFT) {
                    GameUser gameUser = combatUser.getGameUser();
                    boolean isDuplicated = gameUser != null && gameUser.getTeam() != null &&
                            Arrays.stream(gameUser.getTeam().getTeamUsers()).anyMatch(targetGameUser -> {
                                CombatUser targetCombatUser = CombatUser.fromUser(targetGameUser.getUser());
                                Validate.notNull(targetCombatUser);

                                return targetCombatUser.getCharacterType() == CharacterType.this;
                            });

                    if (isDuplicated)
                        return false;

                    combatUser.setCharacterType(CharacterType.this);
                    player.closeInventory();
                } else if (clickType == ClickType.RIGHT) {
                    SelectCharInfo selectCharInfo = new SelectCharInfo(CharacterType.this);
                    selectCharInfo.open(player);
                }

                return true;
            }
        };
    }
}
