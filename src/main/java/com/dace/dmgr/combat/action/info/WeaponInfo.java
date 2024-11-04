package com.dace.dmgr.combat.action.info;

import com.dace.dmgr.ConsoleLogger;
import com.dace.dmgr.combat.action.weapon.Weapon;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.item.ItemBuilder;
import com.dace.dmgr.item.StaticItem;
import lombok.NonNull;
import org.bukkit.Material;

/**
 * 무기 정보를 관리하는 클래스.
 *
 * @param <T> {@link Weapon}을 상속받는 무기
 */
public abstract class WeaponInfo<T extends Weapon> extends ActionInfo {
    /** 무기 아이템 타입 */
    public static final Material MATERIAL = Material.DIAMOND_HOE;
    /** 무기 이름의 접두사 */
    private static final String PREFIX = "§7§l[기본무기] §f";
    /** 무기 클래스 */
    private final Class<T> weaponClass;

    /**
     * 무기 정보 인스턴스를 생성한다.
     *
     * @param weaponClass    무기 클래스
     * @param material       아이템 타입
     * @param resource       리소스 (내구도)
     * @param name           이름
     * @param actionInfoLore 동작 정보 설명
     */
    protected WeaponInfo(@NonNull Class<@NonNull T> weaponClass, @NonNull Material material, short resource, @NonNull String name,
                         @NonNull ActionInfoLore actionInfoLore) {
        super(name, new StaticItem("WeaponInfo" + name, new ItemBuilder(material)
                .setName(PREFIX + name)
                .setDamage(resource)
                .setLore(actionInfoLore.toString())
                .build()));
        this.weaponClass = weaponClass;
    }

    /**
     * 무기 정보 인스턴스를 생성한다.
     *
     * @param weaponClass    무기 클래스
     * @param resource       리소스 (내구도)
     * @param name           이름
     * @param actionInfoLore 동작 정보 설명
     */
    protected WeaponInfo(@NonNull Class<@NonNull T> weaponClass, short resource, @NonNull String name, @NonNull ActionInfoLore actionInfoLore) {
        this(weaponClass, MATERIAL, resource, name, actionInfoLore);
    }

    @Override
    public String toString() {
        return "§f［" + name + "］";
    }

    /**
     * 무기 인스턴스를 생성하여 반환한다.
     *
     * <p>무기 클래스는 다음과 같이 {@link CombatUser} 하나를 인자로 받는
     * 생성자를 가지는 형태로 구현해야 한다.</p>
     *
     * <p>Example:</p>
     *
     * <pre><code>
     * public final class TestWeapon extends AbstractWeapon {
     *     public TestWeapon(CombatUser combatUser) {
     *         super(combatUser, TestWeaponInfo.getInstance());
     *     }
     * }
     * </code></pre>
     *
     * @param combatUser 플레이어 객체
     * @return 무기 객체
     * @throws UnsupportedOperationException 해당 무기를 생성할 수 없으면 발생
     */
    @NonNull
    public final T createWeapon(@NonNull CombatUser combatUser) {
        try {
            return weaponClass.getConstructor(CombatUser.class).newInstance(combatUser);
        } catch (NoSuchMethodException ex) {
            throw new UnsupportedOperationException("해당 무기를 생성할 수 없음");
        } catch (Exception ex) {
            ConsoleLogger.severe("무기 인스턴스 생성 실패", ex);
        }

        throw new UnsupportedOperationException("무기 생성을 사용할 수 없음");
    }
}
