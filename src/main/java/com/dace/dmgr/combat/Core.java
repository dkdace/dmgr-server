package com.dace.dmgr.combat;

import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.item.ItemBuilder;
import com.dace.dmgr.item.StaticItem;
import com.dace.dmgr.item.gui.GuiItem;
import com.dace.dmgr.user.User;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkEffectMeta;

import java.text.MessageFormat;
import java.util.Arrays;

/**
 * 전투 시스템의 코어 목록.
 */
public enum Core {
    STRENGTH("힘", "공격력 +{0}%", 227, 14, 14, 10),
    RESISTANCE("저항", "방어력 +{0}%", 212, 96, 13, 10),
    SPEED("신속", "이동 속도 +{0}%", 84, 235, 230, 7),
    ULTIMATE("궁극", "궁극기 필요 충전량 -{0}%", 37, 92, 232, 10),
    REGENERATION("재생", "초당 {0}% 체력 회복", 14, 179, 20, 0.7),
    HEALTH_DRAIN("흡혈", "입힌 피해의 {0}% 회복", 138, 12, 12, 10),
    HEALING("치유", "치유량 +{0}%", 157, 232, 65, 15),
    RESURRECTION("부활", "부활 시간 -{0}%", 232, 237, 128, 30),
    ENDURANCE("강인함", "받는 해로운 효과 시간 -{0}%", 135, 135, 135, 20);

    /** 코어 이름 */
    private final String name;
    /** 수치 값 목록 */
    @Getter
    private final double @NonNull [] values;
    /** 정적 아이템 객체 */
    @NonNull
    @Getter
    private final StaticItem staticItem;
    /** 선택용 GUI 아이템 객체 */
    @NonNull
    @Getter
    private final GuiItem selectGuiItem;

    Core(String name, String description, int red, int green, int blue, double... values) {
        this.name = name;
        this.values = values;

        ItemStack itemStack = new ItemBuilder(Material.FIREWORK_CHARGE)
                .setName(MessageFormat.format("§b{0}의 코어", name))
                .setLore("",
                        "§7장착 시 다음 효과 적용:",
                        "§9" + MessageFormat.format(description, Arrays.stream(values).boxed().toArray()))
                .build();
        FireworkEffectMeta itemMeta = (FireworkEffectMeta) itemStack.getItemMeta();
        itemMeta.setEffect(FireworkEffect.builder()
                .withColor(Color.fromRGB(red, green, blue))
                .build());
        itemStack.setItemMeta(itemMeta);

        this.staticItem = new StaticItem("Core" + this, itemStack);
        this.selectGuiItem = new GuiItem("CoreSelect" + this, new ItemBuilder(itemStack)
                .addLore("",
                        "§7§n클릭§f하여 코어를 장착하거나 제거합니다.")
                .build()) {
            @Override
            public boolean onClick(@NonNull ClickType clickType, @NonNull ItemStack clickItem, @NonNull Player player) {
                if (!player.getOpenInventory().getTitle().contains("코어 선택"))
                    return false;

                CombatUser combatUser = CombatUser.fromUser(User.fromPlayer(player));
                if (combatUser == null)
                    return false;

                boolean pass;
                if (combatUser.hasCore(Core.this))
                    pass = combatUser.removeCore(Core.this);
                else
                    pass = combatUser.addCore(Core.this);

                return pass;
            }
        };
    }

    /**
     * @return 코어 이름
     */
    @NonNull
    public String getName() {
        return name + "의 코어";
    }
}
