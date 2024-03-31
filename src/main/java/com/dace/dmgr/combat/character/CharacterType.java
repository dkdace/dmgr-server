package com.dace.dmgr.combat.character;

import com.dace.dmgr.combat.character.arkace.Arkace;
import com.dace.dmgr.combat.character.jager.Jager;
import com.dace.dmgr.combat.character.quaker.Quaker;
import com.dace.dmgr.combat.character.silia.Silia;
import com.dace.dmgr.item.ItemBuilder;
import com.dace.dmgr.item.gui.Gui;
import com.dace.dmgr.item.gui.GuiItem;
import com.dace.dmgr.item.gui.SelectChar;
import com.dace.dmgr.util.SkinUtil;
import lombok.Getter;

/**
 * 지정할 수 있는 전투원의 목록.
 *
 * @see Character
 */
@Getter
public enum CharacterType {
    ARKACE(Arkace.getInstance()),
    JAGER(Jager.getInstance()),
    QUAKER(Quaker.getInstance()),
    SILIA(Silia.getInstance());

    /** 전투원 정보 */
    private final Character character;
    /** GUI 아이템 객체 */
    private final GuiItem<CharacterType> guiItem;

    CharacterType(Character character) {
        this.character = character;
        this.guiItem = new GuiItem<CharacterType>(this, ItemBuilder.fromPlayerSkull(SkinUtil.getSkinUrl(character.getSkinName()))
                .setName("§c" + character.getName())
                .setLore("§f전투원 설명", toString())
                .build()) {
            @Override
            public Gui getGui() {
                return SelectChar.getInstance();
            }

            @Override
            public boolean isClickable() {
                return true;
            }
        };
    }
}
