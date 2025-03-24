package com.dace.dmgr.combat.interaction;

import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.action.Action;
import com.dace.dmgr.combat.entity.CombatEntity;
import lombok.Builder;
import lombok.NonNull;
import org.apache.commons.lang3.Validate;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

/**
 * 튕기는 투사체. 투사체 중 벽이나 엔티티에 튕기는 투사체를 관리하는 클래스.
 *
 * @param <T> {@link CombatEntity}를 상속받는 전투 시스템 엔티티
 */
public abstract class BouncingProjectile<T extends CombatEntity> extends Projectile<T> {
    /** 투사체가 튕겼을 때의 속력 배수 */
    private final double bounceVelocityMultiplier;
    /** 투사체가 튕기는 횟수 */
    private int bouncingCount;

    /**
     * 튕기는 투사체 인스턴스를 생성한다.
     *
     * <p>투사체의 선택적 옵션은 {@link Projectile.Option}을 통해 전달받는다.</p>
     *
     * <p>튕기는 투사체의 선택적 옵션은 {@link Option}을 통해 전달받는다.</p>
     *
     * @param shooter          발사자
     * @param action           발사자가 사용한 동작
     * @param speed            투사체의 속력. (단위: 블록/s). 0 이상의 값
     * @param entityCondition  대상 엔티티를 찾는 조건
     * @param projectileOption 투사체의 선택적 옵션
     * @param option           튕기는 투사체의 선택적 옵션
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     * @see Projectile.Option
     * @see Option
     */
    BouncingProjectile(@NonNull CombatEntity shooter, @Nullable Action action, int speed, @NonNull CombatUtil.EntityCondition<T> entityCondition,
                       @NonNull Projectile.Option projectileOption, @NonNull Option option) {
        super(shooter, action, speed, entityCondition, projectileOption);

        this.bounceVelocityMultiplier = option.bounceVelocityMultiplier;
        Validate.isTrue(bounceVelocityMultiplier <= 1, "bounceVelocityMultiplier <= 1 (%f)", bounceVelocityMultiplier);

        this.bouncingCount = option.bouncingCount;
        Validate.isTrue(bouncingCount >= 1, "bouncingCount >= 1 (%d)", bouncingCount);
    }

    /**
     * 튕기는 투사체 인스턴스를 생성한다.
     *
     * <p>투사체의 선택적 옵션은 {@link Projectile.Option}을 통해 전달받는다.</p>
     *
     * <p>튕기는 투사체의 선택적 옵션은 {@link Option}을 통해 전달받는다.</p>
     *
     * @param shooter          발사자
     * @param speed            투사체의 속력. (단위: 블록/s). 0 이상의 값
     * @param entityCondition  대상 엔티티를 찾는 조건
     * @param projectileOption 투사체의 선택적 옵션
     * @param option           튕기는 투사체의 선택적 옵션
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     * @see Projectile.Option
     * @see Option
     */
    protected BouncingProjectile(@NonNull CombatEntity shooter, int speed, @NonNull CombatUtil.EntityCondition<T> entityCondition,
                                 @NonNull Projectile.Option projectileOption, @NonNull Option option) {
        this(shooter, null, speed, entityCondition, projectileOption, option);
    }

    /**
     * 튕기는 투사체 인스턴스를 생성한다.
     *
     * <p>투사체의 선택적 옵션은 {@link Projectile.Option}을 통해 전달받는다.</p>
     *
     * <p>튕기는 투사체의 선택적 옵션은 {@link Option}을 통해 전달받는다.</p>
     *
     * @param action           발사자가 사용한 동작
     * @param speed            투사체의 속력. (단위: 블록/s). 0 이상의 값
     * @param entityCondition  대상 엔티티를 찾는 조건
     * @param projectileOption 투사체의 선택적 옵션
     * @param option           튕기는 투사체의 선택적 옵션
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     * @see Projectile.Option
     * @see Option
     */
    protected BouncingProjectile(@NonNull Action action, int speed, @NonNull CombatUtil.EntityCondition<T> entityCondition,
                                 @NonNull Projectile.Option projectileOption, @NonNull Option option) {
        this(action.getCombatUser(), action, speed, entityCondition, projectileOption, option);
    }

    /**
     * 튕기는 투사체 인스턴스를 생성한다.
     *
     * <p>투사체의 선택적 옵션은 {@link Projectile.Option}을 통해 전달받는다.</p>
     *
     * @param shooter          발사자
     * @param speed            투사체의 속력. (단위: 블록/s). 0 이상의 값
     * @param entityCondition  대상 엔티티를 찾는 조건
     * @param projectileOption 투사체의 선택적 옵션
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     * @see Projectile.Option
     */
    protected BouncingProjectile(@NonNull CombatEntity shooter, int speed, @NonNull CombatUtil.EntityCondition<T> entityCondition,
                                 @NonNull Projectile.Option projectileOption) {
        this(shooter, null, speed, entityCondition, projectileOption, Option.builder().build());
    }

    /**
     * 튕기는 투사체 인스턴스를 생성한다.
     *
     * <p>투사체의 선택적 옵션은 {@link Projectile.Option}을 통해 전달받는다.</p>
     *
     * @param action           발사자가 사용한 동작
     * @param speed            투사체의 속력. (단위: 블록/s). 0 이상의 값
     * @param entityCondition  대상 엔티티를 찾는 조건
     * @param projectileOption 투사체의 선택적 옵션
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     * @see Projectile.Option
     */
    protected BouncingProjectile(@NonNull Action action, int speed, @NonNull CombatUtil.EntityCondition<T> entityCondition,
                                 @NonNull Projectile.Option projectileOption) {
        this(action.getCombatUser(), action, speed, entityCondition, projectileOption, Option.builder().build());
    }

    /**
     * 튕기는 투사체 인스턴스를 생성한다.
     *
     * @param shooter         발사자
     * @param speed           투사체의 속력. (단위: 블록/s). 0 이상의 값
     * @param entityCondition 대상 엔티티를 찾는 조건
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    protected BouncingProjectile(@NonNull CombatEntity shooter, int speed, @NonNull CombatUtil.EntityCondition<T> entityCondition) {
        this(shooter, null, speed, entityCondition, Projectile.Option.builder().build(), Option.builder().build());
    }

    /**
     * 튕기는 투사체 인스턴스를 생성한다.
     *
     * @param action          발사자가 사용한 동작
     * @param speed           투사체의 속력. (단위: 블록/s). 0 이상의 값
     * @param entityCondition 대상 엔티티를 찾는 조건
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    protected BouncingProjectile(@NonNull Action action, int speed, @NonNull CombatUtil.EntityCondition<T> entityCondition) {
        this(action.getCombatUser(), action, speed, entityCondition, Projectile.Option.builder().build(), Option.builder().build());
    }

    @Override
    @NonNull
    protected final HitBlockHandler getHitBlockHandler() {
        return HitBlockHandler
                .chain(getPreHitBlockHandler())
                .next((location, hitBlock) -> {
                    if (bouncingCount-- > 0)
                        return handleBounce(location, hitBlock);

                    return false;
                });
    }

    /**
     * 총알이 블록에 맞았을 때, 먼저 처리할 블록 판정 처리기를 반환한다.
     *
     * <p>{@link HitBlockHandler#onHitBlock(Location, Block)}의 결과에 따라 블록에 튕기거나 소멸된다.</p>
     *
     * @return 블록 판정 처리기
     */
    @NonNull
    protected abstract HitBlockHandler getPreHitBlockHandler();

    @Override
    @NonNull
    protected final HitEntityHandler<T> getHitEntityHandler() {
        return HitEntityHandler
                .chain(getPreHitEntityHandler())
                .next((location, target) -> {
                    if (bouncingCount-- > 0) {
                        setVelocity(getVelocity().multiply(-bounceVelocityMultiplier * 0.5));
                        return true;
                    }

                    return false;
                });
    }

    /**
     * 총알이 엔티티에 맞았을 때, 먼저 처리할 엔티티 판정 처리기를 반환한다.
     *
     * <p>{@link HitEntityHandler#onHitEntity(Location, CombatEntity)}의 결과에 따라 엔티티에 튕기거나 소멸된다.</p>
     *
     * @return 엔티티 판정 처리기
     */
    @NonNull
    protected abstract HitEntityHandler<T> getPreHitEntityHandler();

    /**
     * 투사체의 블록 도탄 로직을 처리한다.
     *
     * @param location 맞은 위치
     * @param hitBlock 맞은 블록
     * @return 진행 여부
     */
    private boolean handleBounce(@NonNull Location location, @NonNull Block hitBlock) {
        BlockFace blockFace = location.getBlock().getFace(hitBlock);

        if (blockFace != null) {
            Vector velocity = getVelocity();
            setVelocity(velocity.multiply(bounceVelocityMultiplier));

            if (blockFace.getModX() != 0)
                setVelocity(velocity.setX(-velocity.getX()));
            if (blockFace.getModY() != 0)
                setVelocity(velocity.setY(-velocity.getY()));
            if (blockFace.getModZ() != 0)
                setVelocity(velocity.setZ(-velocity.getZ()));
        }

        move(location);

        return true;
    }

    /**
     * 튕기는 투사체의 선택적 옵션을 관리하는 클래스.
     */
    @Builder
    public static final class Option {
        /** 투사체가 튕겼을 때의 속력 배수. 1 이하의 값 */
        @Builder.Default
        private final double bounceVelocityMultiplier = 1;
        /** 투사체가 튕기는 횟수. 1 이상의 값 */
        @Builder.Default
        private final int bouncingCount = Integer.MAX_VALUE;
    }
}
