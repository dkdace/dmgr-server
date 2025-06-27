package com.dace.dmgr.combat.ability.info;

import com.dace.dmgr.combat.ability.weapon.Weapon;
import com.dace.dmgr.item.ItemBuilder;
import lombok.NonNull;
import org.bukkit.Material;

/**
 * 무기 정보를 관리하는 클래스.
 *
 * @param <T> {@link Weapon}을 상속받는 무기
 */
public abstract class WeaponInfo<T extends Weapon> extends AbilityInfo<T> {
    /** 무기 아이템 타입 */
    public static final Material MATERIAL = Material.DIAMOND_HOE;
    /** 무기 이름의 접두사 */
    private static final String PREFIX = "§7§l[기본무기] §f";

    /**
     * 무기 정보 인스턴스를 생성한다.
     *
     * @param weaponClass     무기 클래스
     * @param material        아이템 타입
     * @param resource        리소스 (내구도)
     * @param name            이름
     * @param abilityInfoLore 능력 정보 설명
     */
    protected WeaponInfo(@NonNull Class<@NonNull T> weaponClass, @NonNull Material material, short resource, @NonNull String name,
                         @NonNull AbilityInfoLore abilityInfoLore) {
        super(weaponClass, name, new ItemBuilder(material)
                .setName(PREFIX + name)
                .setDamage(resource)
                .build(), abilityInfoLore);
    }

    /**
     * 무기 정보 인스턴스를 생성한다.
     *
     * @param weaponClass     무기 클래스
     * @param resource        리소스 (내구도)
     * @param name            이름
     * @param abilityInfoLore 능력 정보 설명
     */
    protected WeaponInfo(@NonNull Class<@NonNull T> weaponClass, short resource, @NonNull String name, @NonNull AbilityInfoLore abilityInfoLore) {
        this(weaponClass, MATERIAL, resource, name, abilityInfoLore);
    }
}
