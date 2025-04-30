package com.dace.dmgr.combat.action.info;

import com.dace.dmgr.ConsoleLogger;
import com.dace.dmgr.combat.action.Trait;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.util.ReflectionUtil;
import lombok.NonNull;
import org.apache.commons.lang3.Validate;

/**
 * 특성 인스턴스({@link Trait})가 존재하는 동적 특성 정보를 관리하는 클래스.
 *
 * @param <T> {@link Trait}을 상속받는 특성
 */
public abstract class DynamicTraitInfo<T extends Trait> extends TraitInfo {
    /** 특성 클래스 */
    @NonNull
    private final Class<T> traitClass;

    /**
     * 동적 특성 정보 인스턴스를 생성한다.
     *
     * @param traitClass     특성 클래스
     * @param name           이름
     * @param actionInfoLore 동작 정보 설명
     */
    protected DynamicTraitInfo(@NonNull Class<@NonNull T> traitClass, @NonNull String name, @NonNull ActionInfoLore actionInfoLore) {
        super(name, actionInfoLore);
        this.traitClass = traitClass;
    }

    /**
     * 특성 인스턴스를 생성하여 반환한다.
     *
     * <p>특성 클래스는 다음과 같이 {@link CombatUser} 하나를 인자로 받는 생성자를 가지는 형태로 구현해야 한다.</p>
     *
     * <p>Example:</p>
     *
     * <pre><code>
     * public final class TestTrait extends Trait {
     *     public TestTrait(CombatUser combatUser) {
     *         super(combatUser, TestTraitInfo.getInstance());
     *     }
     * }
     * </code></pre>
     *
     * @param combatUser 사용자 플레이어
     * @return 특성 인스턴스
     * @throws UnsupportedOperationException 해당 특성을 생성할 수 없으면 발생
     */
    @NonNull
    public final T createTrait(@NonNull CombatUser combatUser) {
        try {
            return ReflectionUtil.getConstructor(Validate.notNull(traitClass), CombatUser.class).newInstance(combatUser);
        } catch (Exception ex) {
            ConsoleLogger.severe("특성 인스턴스 생성 실패", ex);
        }

        throw new UnsupportedOperationException();
    }
}
