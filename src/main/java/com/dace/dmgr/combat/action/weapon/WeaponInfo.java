package com.dace.dmgr.combat.action.weapon;

import com.dace.dmgr.combat.action.ActionInfo;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.gui.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * 무기 정보를 관리하는 클래스.
 */
public abstract class WeaponInfo extends ActionInfo {
    /** 무기 아이템 타입 */
    public static final Material MATERIAL = Material.DIAMOND_HOE;
    /** 무기 이름의 접두사 */
    private static final String PREFIX = "§e§l[기본무기] §f";

    protected WeaponInfo(short resource, String name, String... lore) {
        super(name, new ItemBuilder(MATERIAL)
                .setName(PREFIX + name)
                .setDamage(resource)
                .setLore(lore)
                .build());

        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setUnbreakable(true);
        itemStack.setItemMeta(itemMeta);
    }

    /**
     * 무기 인스턴스를 생성하여 반환한다.
     *
     * @param combatUser 플레이어 객체
     * @return 무기 객체
     */
    public abstract Weapon createWeapon(CombatUser combatUser);
}
