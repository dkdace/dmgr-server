package com.dace.dmgr.combat.action.info;

import com.dace.dmgr.combat.action.weapon.Weapon;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.item.ItemBuilder;
import lombok.NonNull;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * 무기 정보를 관리하는 클래스.
 */
public abstract class WeaponInfo extends ActionInfo {
    /** 무기 아이템 타입 */
    public static final Material MATERIAL = Material.DIAMOND_HOE;
    /** 무기 이름의 접두사 */
    private static final String PREFIX = "§e§l[기본무기] §f";

    /**
     * 무기 정보 인스턴스를 생성한다.
     *
     * @param resource 리소스 (내구도)
     * @param name     이름
     * @param lores    설명 목록
     */
    protected WeaponInfo(short resource, @NonNull String name, @NonNull String @NonNull ... lores) {
        super(name, new ItemBuilder(MATERIAL)
                .setName(PREFIX + name)
                .setDamage(resource)
                .setLore(lores)
                .build());

        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setUnbreakable(true);
        itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        itemStack.setItemMeta(itemMeta);
    }

    @Override
    public String toString() {
        return "§f［" + name + "］";
    }

    /**
     * 무기 인스턴스를 생성하여 반환한다.
     *
     * @param combatUser 플레이어 객체
     * @return 무기 객체
     */
    @NonNull
    public abstract Weapon createWeapon(@NonNull CombatUser combatUser);
}
