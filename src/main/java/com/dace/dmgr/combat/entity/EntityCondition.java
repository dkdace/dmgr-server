package com.dace.dmgr.combat.entity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

/**
 * 전투 시스템의 엔티티 탐색 조건을 나타내는 클래스.
 *
 * @param <T> {@link CombatEntity}를 상속받는 전투 시스템 엔티티
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class EntityCondition<T extends CombatEntity> {
    /** 대상 엔티티의 클래스 인스턴스 */
    private final Class<T> targetClass;
    /** 대상 엔티티를 찾는 조건 */
    private final Predicate<T> targetCondition;

    /**
     * 지정한 클래스와 조건으로 엔티티 탐색 조건을 생성한다.
     *
     * @param targetClass     대상 엔티티의 클래스 인스턴스
     * @param targetCondition 대상 엔티티를 찾는 조건
     * @param <T>             {@link CombatEntity}를 상속받는 전투 시스템 엔티티
     * @return {@link EntityCondition}
     */
    @NonNull
    public static <T extends CombatEntity> EntityCondition<T> of(@NonNull Class<T> targetClass, @NonNull Predicate<T> targetCondition) {
        return new EntityCondition<>(targetClass, targetCondition);
    }

    /**
     * 지정한 클래스로 엔티티 탐색 조건을 생성한다.
     *
     * @param targetClass 대상 엔티티의 클래스 인스턴스
     * @param <T>         {@link CombatEntity}를 상속받는 전투 시스템 엔티티
     * @return {@link EntityCondition}
     */
    @NonNull
    public static <T extends CombatEntity> EntityCondition<T> of(@NonNull Class<T> targetClass) {
        return of(targetClass, combatEntity -> true);
    }

    /**
     * 모든 엔티티를 포함하는, 조건이 지정되지 않은 엔티티 탐색 조건을 생성한다.
     *
     * @return {@link EntityCondition}
     */
    @NonNull
    public static EntityCondition<@NonNull CombatEntity> all() {
        return of(CombatEntity.class);
    }

    /**
     * 지정한 엔티티의 적을 포함하는 엔티티 탐색 조건을 생성한다.
     *
     * @param target 대상 엔티티
     * @return {@link EntityCondition}
     * @see CombatEntity#isEnemy(CombatEntity)
     */
    @NonNull
    public static EntityCondition<@NonNull Damageable> enemy(@NonNull Damageable target) {
        return of(Damageable.class, combatEntity -> combatEntity.isEnemy(target));
    }

    /**
     * 지정한 엔티티의 아군을 포함하는 엔티티 탐색 조건을 생성한다.
     *
     * @param target 대상 엔티티
     * @return {@link EntityCondition}
     * @see CombatEntity#isEnemy(CombatEntity)
     */
    @NonNull
    public static EntityCondition<@NonNull Healable> team(@NonNull Healable target) {
        return of(Healable.class, combatEntity -> !combatEntity.isEnemy(target));
    }

    /**
     * 지정한 엔티티가 {@link EntityCondition#targetClass}의 인스턴스이면 캐스팅하고, 아니면 {@code null}을 반환한다.
     *
     * @param combatEntity 대상 엔티티
     * @return 캐스팅된 {@code combatEntity}
     */
    @Nullable
    public T cast(@NonNull CombatEntity combatEntity) {
        return targetClass.isInstance(combatEntity) ? targetClass.cast(combatEntity) : null;
    }

    /**
     * 지정한 엔티티가 조건을 만족하는지 확인한다.
     *
     * @param target 대상 엔티티
     * @return 조건을 만족하면 {@code true} 반환
     */
    public boolean test(@NonNull T target) {
        return targetCondition.test(target);
    }

    /**
     * 현재 조건과 지정한 조건을 모두 만족하는 AND 조건을 생성한다.
     *
     * @param targetCondition 대상 엔티티를 찾는 조건
     * @return {@link EntityCondition}
     */
    @NonNull
    public EntityCondition<T> and(@NonNull Predicate<T> targetCondition) {
        return of(targetClass, combatEntity -> this.targetCondition.test(combatEntity) && targetCondition.test(combatEntity));
    }

    /**
     * 현재 조건과 지정한 엔티티 탐색 조건을 모두 만족하는 AND 조건을 생성한다.
     *
     * @param entityCondition 엔티티 탐색 조건
     * @param <U>             {@link T} 또는 하위 엔티티 타입
     * @return {@link EntityCondition}
     */
    @NonNull
    public <U extends T> EntityCondition<U> and(@NonNull EntityCondition<U> entityCondition) {
        return entityCondition.and(targetCondition::test);
    }

    /**
     * 현재 조건 또는 지정한 조건을 만족하는 OR 조건을 생성한다.
     *
     * @param targetCondition 대상 엔티티를 찾는 조건
     * @return {@link EntityCondition}
     */
    @NonNull
    public EntityCondition<T> or(@NonNull Predicate<T> targetCondition) {
        return of(targetClass, combatEntity -> this.targetCondition.test(combatEntity) || targetCondition.test(combatEntity));
    }

    /**
     * 현재 조건 또는 지정한 엔티티 탐색 조건을 만족하는 OR 조건을 생성한다.
     *
     * @param entityCondition 엔티티 탐색 조건
     * @param <U>             {@link T} 또는 하위 엔티티 타입
     * @return {@link EntityCondition}
     */
    @NonNull
    public <U extends T> EntityCondition<T> or(@NonNull EntityCondition<U> entityCondition) {
        return or(combatEntity -> entityCondition.targetClass.isInstance(combatEntity)
                && entityCondition.targetCondition.test(entityCondition.targetClass.cast(combatEntity)));
    }

    /**
     * 현재 조건에 대상 엔티티를 포함한다.
     *
     * <p>다음과 동일한 결과를 나타냄:</p>
     *
     * <pre><code>
     * EntityCondition.or(combatEntity -&gt; combatEntity == target);
     * </code></pre>
     *
     * @param target 포함할 엔티티
     * @return {@link EntityCondition}
     */
    @NonNull
    public EntityCondition<T> include(@NonNull T target) {
        return or(combatEntity -> combatEntity == target);
    }

    /**
     * 현재 조건에서 대상 엔티티를 제외한다.
     *
     * <p>다음과 동일한 결과를 나타냄:</p>
     *
     * <pre><code>
     * EntityCondition.and(combatEntity -&gt; combatEntity != target);
     * </code></pre>
     *
     * @param target 제외할 엔티티
     * @return {@link EntityCondition}
     */
    @NonNull
    public EntityCondition<T> exclude(@NonNull T target) {
        return and(combatEntity -> combatEntity != target);
    }
}
