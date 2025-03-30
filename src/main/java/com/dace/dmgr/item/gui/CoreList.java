package com.dace.dmgr.item.gui;

import com.dace.dmgr.GeneralConfig;
import com.dace.dmgr.combat.Core;
import com.dace.dmgr.combat.combatant.CombatantType;
import com.dace.dmgr.effect.SoundEffect;
import com.dace.dmgr.item.DefinedItem;
import com.dace.dmgr.item.ItemBuilder;
import com.dace.dmgr.user.User;
import com.dace.dmgr.user.UserData;
import lombok.NonNull;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;

/**
 * 코어 목록 GUI 클래스.
 */
public final class CoreList extends ChestGUI {
    /** 코어 구매 효과음 */
    private static final SoundEffect CORE_PURCHASE_SOUND = new SoundEffect(
            SoundEffect.SoundInfo.builder("random.craft").pitch(0.8).build(),
            SoundEffect.SoundInfo.builder(Sound.ENTITY_PLAYER_LEVELUP).pitch(2).build());

    /**
     * 코어 목록 GUI 인스턴스를 생성한다.
     *
     * @param player GUI 표시 대상 플레이어
     */
    public CoreList(@NonNull Player player) {
        super(6, "§8코어 목록", player);

        set(5, 8, new GUIItem.Previous(Menu::new));

        CombatantType[] combatantTypes = CombatantType.sortedValues();
        for (int i = 0; i < combatantTypes.length; i++) {
            CombatantType combatantType = combatantTypes[i];

            Set<Core> cores = UserData.fromPlayer(player).getCombatantRecord(combatantType).getCores();

            set(i, new DefinedItem(combatantType.getProfileItem(), (clickType, target) -> {
                if (clickType != ClickType.LEFT)
                    return false;

                onSelectCombatant(player, combatantType);
                return true;
            }), itemBuilder -> {
                itemBuilder.setLore("");

                if (cores.isEmpty())
                    itemBuilder.addLore("§c적용된 코어 없음");
                else
                    for (Core core : cores)
                        itemBuilder.addLore("§b" + core.getName());

                itemBuilder.addLore("", "§7§n클릭§f하여 코어를 추가합니다.");
            });
        }
    }

    /**
     * 코어를 추가할 전투원을 선택했을 때 실행할 작업.
     *
     * @param player        플레이어
     * @param combatantType 대상 전투원
     */
    private void onSelectCombatant(@NonNull Player player, @NonNull CombatantType combatantType) {
        clear();
        set(5, 8, new GUIItem.Previous(CoreList::new));

        UserData userData = UserData.fromPlayer(player);

        Iterator<Core> iterator = Arrays.stream(Core.values())
                .filter(core -> !userData.getCombatantRecord(combatantType).getCores().contains(core))
                .iterator();

        int i = 0;
        for (; iterator.hasNext(); i++) {
            Core core = iterator.next();
            int price = GeneralConfig.getConfig().getCorePrice();

            set(i, new DefinedItem(new ItemBuilder(core.getCoreItem())
                    .addLore("",
                            "§e▣ 가격 §7:: §6{0} CP",
                            "",
                            "§7§n클릭§f하여 코어를 구매합니다.")
                    .formatLore(price)
                    .build(),
                    (clickType, target) -> {
                        if (clickType != ClickType.LEFT)
                            return false;

                        onPurchaseCore(player, combatantType, core, price);

                        player.closeInventory();
                        return true;
                    }));
        }
    }

    /**
     * 구매할 코어를 선택했을 때 실행할 작업.
     *
     * @param player        플레이어
     * @param combatantType 대상 전투원
     * @param selectCore    선택한 코어
     * @param price         가격
     */
    private void onPurchaseCore(@NonNull Player player, @NonNull CombatantType combatantType, @NonNull Core selectCore, int price) {
        User user = User.fromPlayer(player);
        UserData userData = user.getUserData();

        int currentMoney = userData.getMoney();
        if (currentMoney < price) {
            user.sendMessageWarn("잔액이 부족합니다.");
            return;
        }

        user.getUserData().setMoney(currentMoney - price);
        userData.getCombatantRecord(combatantType).addCore(selectCore);

        user.sendMessageInfo("§e§n{0}§r의 코어 목록에 §b§n{1}§r를 추가했습니다.", combatantType.getCombatant().getName(), selectCore.getName());

        CORE_PURCHASE_SOUND.play(player);
    }
}
