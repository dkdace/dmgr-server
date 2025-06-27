package com.dace.dmgr.combat.ability.info;

import com.dace.dmgr.ConsoleLogger;
import com.dace.dmgr.combat.ability.Ability;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.item.DefinedItem;
import com.dace.dmgr.item.ItemBuilder;
import com.dace.dmgr.util.ReflectionUtil;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.inventory.ItemStack;

/**
 * 능력(무기, 스킬, 특성 등)의 정보 표시 아이템을 관리하는 클래스.
 *
 * @param <T> {@link Ability}를 상속받는 능력
 * @see WeaponInfo
 * @see SkillInfo
 * @see TraitInfo
 */
public class AbilityInfo<T extends Ability> {
    /** 능력 클래스 */
    private final Class<T> abilityClass;
    /** 이름 */
    @NonNull
    @Getter
    private final String name;
    /** 설명 GUI 아이템 인스턴스 */
    @NonNull
    @Getter
    private final DefinedItem definedItem;

    /**
     * 능력 정보 인스턴스를 생성한다.
     *
     * @param abilityClass    능력 클래스
     * @param name            이름
     * @param itemStack       설명 아이템
     * @param abilityInfoLore 능력 정보 설명
     */
    protected AbilityInfo(@NonNull Class<T> abilityClass, @NonNull String name, @NonNull ItemStack itemStack, @NonNull AbilityInfoLore abilityInfoLore) {
        this.abilityClass = abilityClass;
        this.name = name;
        this.definedItem = new DefinedItem(new ItemBuilder(itemStack).setLore(abilityInfoLore.toString()).build());
    }

    /**
     * 능력 인스턴스를 생성하여 반환한다.
     *
     * <p>능력 클래스는 다음과 같이 {@link CombatUser}와 {@link AbilityInfo}를 상속받는 클래스를 인자로 받는 생성자를 가지는 형태로 구현해야 한다.</p>
     *
     * <p>Example:</p>
     *
     * <pre><code>
     * public final class TestTrait extends Trait {
     *     public TestTrait(CombatUser combatUser, TestTraitInfo traitInfo) {
     *         super(combatUser, traitInfo);
     *     }
     * }
     * </code></pre>
     *
     * @param combatUser 사용자 플레이어
     * @return 능력 인스턴스
     * @throws UnsupportedOperationException 해당 능력을 생성할 수 없으면 발생
     */
    @NonNull
    public final T create(@NonNull CombatUser combatUser) {
        try {
            return ReflectionUtil.getConstructor(abilityClass, CombatUser.class, getClass()).newInstance(combatUser, this);
        } catch (Exception ex) {
            ConsoleLogger.severe("능력 인스턴스 생성 실패", ex);
        }

        throw new UnsupportedOperationException();
    }
}
