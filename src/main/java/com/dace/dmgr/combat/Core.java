package com.dace.dmgr.combat;

import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.item.DefinedItem;
import com.dace.dmgr.item.ItemBuilder;
import com.dace.dmgr.item.gui.ChestGUI;
import com.dace.dmgr.item.gui.SelectCore;
import com.dace.dmgr.user.User;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkEffectMeta;

import java.text.MessageFormat;

/**
 * 전투 시스템의 코어 목록.
 */
public enum Core {
    STRENGTH("힘", "공격력 +{0}%", Color.fromRGB(227, 14, 14), 10),
    RESISTANCE("저항", "방어력 +{0}%", Color.fromRGB(212, 96, 13), 10),
    SPEED("신속", "이동 속도 +{0}%", Color.fromRGB(84, 235, 230), 7),
    ULTIMATE("궁극", "궁극기 필요 충전량 -{0}%", Color.fromRGB(37, 92, 232), 10),
    REGENERATION("재생", "초당 {0}% 체력 회복", Color.fromRGB(14, 179, 20), 0.7),
    HEALTH_DRAIN("흡혈", "입힌 피해의 {0}% 회복", Color.fromRGB(138, 12, 12), 10),
    HEALING("치유", "치유량 +{0}%", Color.fromRGB(157, 232, 65), 15),
    RESURRECTION("부활", "부활 시간 -{0}%", Color.fromRGB(232, 237, 128), 30),
    ENDURANCE("강인함", "받는 해로운 효과 시간 -{0}%", Color.fromRGB(135, 135, 135), 20);

    /** 코어 이름 */
    private final String name;
    /** 수치 값 */
    @Getter
    private final double value;
    /** 코어 아이템 */
    private final ItemStack coreItem;
    /** 코어 선택 GUI 아이템 */
    @NonNull
    @Getter
    private final DefinedItem selectItem;

    Core(String name, String description, Color color, double value) {
        this.name = name;
        this.value = value;

        this.coreItem = new ItemBuilder(Material.FIREWORK_CHARGE)
                .editItemMeta(itemMeta ->
                        ((FireworkEffectMeta) itemMeta).setEffect(FireworkEffect.builder().withColor(color).build()))
                .setName("§b" + getName())
                .setLore("",
                        "§7장착 시 다음 효과 적용:",
                        "§9" + MessageFormat.format(description, value))
                .build();
        this.selectItem = new DefinedItem(new ItemBuilder(coreItem).addLore("", "§7§n클릭§f하여 코어를 장착하거나 제거합니다.").build(),
                new DefinedItem.ClickHandler(ClickType.LEFT, player -> {
                    ChestGUI gui = ChestGUI.fromInventory(player.getOpenInventory().getTopInventory());
                    if (!(gui instanceof SelectCore))
                        return false;

                    CombatUser combatUser = CombatUser.fromUser(User.fromPlayer(player));
                    if (combatUser == null)
                        return false;

                    boolean pass;
                    if (combatUser.hasCore(Core.this))
                        pass = combatUser.removeCore(Core.this);
                    else
                        pass = combatUser.addCore(Core.this);
                    if (pass)
                        new SelectCore(player);

                    return pass;
                }));
    }

    /**
     * @return 코어 이름
     */
    @NonNull
    public String getName() {
        return name + "의 코어";
    }

    /**
     * 코어 아이템을 반환한다.
     *
     * @return 코어 아이템
     */
    @NonNull
    public ItemStack getCoreItem() {
        return coreItem.clone();
    }
}
