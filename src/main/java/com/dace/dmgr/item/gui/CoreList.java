package com.dace.dmgr.item.gui;

import com.dace.dmgr.combat.Core;
import com.dace.dmgr.combat.character.CharacterType;
import com.dace.dmgr.user.UserData;
import lombok.NonNull;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Set;

/**
 * 코어 목록 GUI 클래스.
 */
public final class CoreList extends Gui {
    /** 이전 버튼 GUI 아이템 객체 */
    private static final GuiItem buttonLeft = new ButtonItem.Left("CoreListLeft") {
        @Override
        public boolean onClick(@NonNull ClickType clickType, @NonNull ItemStack clickItem, @NonNull Player player) {
            player.performCommand("메뉴");
            return true;
        }
    };

    /** 대상 플레이어 데이터 정보 */
    private final UserData userData;

    /**
     * 지정한 플레이어의 코어 목록 GUI 인스턴스를 생성한다.
     *
     * @param userData 대상 플레이어 데이터 정보
     */
    public CoreList(@NonNull UserData userData) {
        super(6, "§8코어 목록");
        this.userData = userData;
    }

    @Override
    public void onOpen(@NonNull Player player, @NonNull GuiController guiController) {
        guiController.set(53, buttonLeft);

        CharacterType[] characterTypes = Arrays.stream(CharacterType.values())
                .sorted(Comparator.comparing(characterType -> characterType.getCharacter().getName()))
                .toArray(CharacterType[]::new);
        for (int i = 0; i < characterTypes.length; i++) {
            Set<Core> cores = userData.getCharacterRecord(characterTypes[i]).getCores();

            guiController.set(i, CharacterType.valueOf(characterTypes[i].toString()).getGuiItem(), itemBuilder -> {
                itemBuilder.setLore("");
                if (cores.isEmpty())
                    itemBuilder.addLore("§8적용된 코어 없음");
                else {
                    for (Core core : cores)
                        itemBuilder.addLore("§b" + core.getName());
                }
            });
        }
    }
}
