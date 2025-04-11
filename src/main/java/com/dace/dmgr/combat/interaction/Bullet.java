package com.dace.dmgr.combat.interaction;

import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.util.LocationUtil;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.Validate;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.function.Consumer;

/**
 * 총알. 원거리 판정(투사체, 히트스캔) 등을 관리하기 위한 클래스.
 *
 * @param <T> {@link CombatEntity}를 상속받는 전투 시스템 엔티티
 * @see Hitscan
 * @see Projectile
 */
public abstract class Bullet<T extends CombatEntity> {
    /** 궤적 상 히트박스 판정점 간 거리 기본값. (단위: 블록) */
    protected static final double HITBOX_INTERVAL = 1 / 8.0;

    /** 발사자 엔티티 */
    @NonNull
    @Getter
    protected final CombatEntity shooter;
    /** 발사 위치로부터 총알이 생성되는 거리. (단위: 블록) */
    protected final double startDistance;
    /** 총알의 최대 사거리. (단위: 블록) */
    protected final double maxDistance;
    /** 총알의 판정 크기. 판정의 엄격함에 영향을 미침. (단위: 블록) */
    protected final double size;
    /** 대상 엔티티를 찾는 조건 */
    protected final CombatUtil.EntityCondition<T> entityCondition;
    /** 피격자 목록 */
    private final HashSet<T> targets = new HashSet<>();

    /** 발사 위치 */
    @Nullable
    private Location startLocation;
    /** 총알의 현재 위치 */
    @Nullable
    private Location location;
    /** 총알의 현재 속도 */
    @Nullable
    private Vector velocity;
    /** 판정점 처리기 */
    @Nullable
    private IntervalHandler intervalHandler;
    /** 블록 판정 처리기 */
    @Nullable
    private HitBlockHandler hitBlockHandler;
    /** 엔티티 판정 처리기 */
    @Nullable
    private HitEntityHandler<T> hitEntityHandler;
    /** 현재 판정점 인덱스 */
    private int index;
    /** 총알이 이동한 거리. (단위: 블록) */
    @Getter(AccessLevel.PROTECTED)
    private double travelDistance = 0;

    /** 발사 여부 */
    private boolean isShot = false;
    /** 소멸 여부 */
    @Getter
    private boolean isDestroyed = false;

    /**
     * 총알 인스턴스를 생성한다.</p>
     *
     * @param shooter         발사자 엔티티
     * @param startDistance   발사 위치로부터 총알이 생성되는 거리. (단위: 블록). 0 이상의 값
     * @param maxDistance     총알의 최대 사거리. (단위: 블록). {@code startDistance} 이상의 값
     * @param size            총알의 판정 크기. 판정의 엄격함에 영향을 미침. (단위: 블록). 0 이상의 값
     * @param entityCondition 대상 엔티티를 찾는 조건
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    protected Bullet(@NonNull CombatEntity shooter, double startDistance, double maxDistance, double size, @NonNull CombatUtil.EntityCondition<T> entityCondition) {
        Validate.isTrue(startDistance >= 0, "startDistance >= 0 (%f)", startDistance);
        Validate.isTrue(maxDistance >= 0, "maxDistance >= %f (%f)", startDistance, maxDistance);
        Validate.isTrue(size >= 0, "size >= 0 (%f)", size);

        this.shooter = shooter;
        this.startDistance = startDistance;
        this.maxDistance = maxDistance;
        this.size = size;
        this.entityCondition = entityCondition;
    }

    /**
     * 매 판정점마다 실행될 작업을 처리하는 판정점 처리기를 반환한다.
     *
     * @return 판정점 처리기
     */
    @NonNull
    protected abstract IntervalHandler getIntervalHandler();

    /**
     * 총알이 블록에 맞았을 때 실행될 작업을 처리하는 블록 판정 처리기를 반환한다.
     *
     * @return 블록 판정 처리기
     */
    @NonNull
    protected abstract HitBlockHandler getHitBlockHandler();

    /**
     * 총알이 엔티티에 맞았을 때 실행될 작업을 처리하는 엔티티 판정 처리기를 반환한다.
     *
     * @return 엔티티 판정 처리기
     */
    @NonNull
    protected abstract HitEntityHandler<T> getHitEntityHandler();

    private void validateIsShot() {
        Validate.validState(isShot, "총알이 발사되지 않음");
    }

    /**
     * 총알의 현재 위치를 반환한다.
     *
     * @return 현재 위치
     * @throws IllegalStateException 총알이 발사되지 않았으면 발생
     */
    @NonNull
    protected final Location getLocation() {
        validateIsShot();
        return Validate.notNull(location).clone();
    }

    /**
     * 총알을 지정한 위치로 이동시킨다.
     *
     * @param location 이동할 위치
     * @throws IllegalStateException 총알이 발사되지 않았으면 발생
     */
    protected final void move(@NonNull Location location) {
        validateIsShot();
        Validate.notNull(this.location);

        this.location.setWorld(location.getWorld());
        this.location.setX(location.getX());
        this.location.setY(location.getY());
        this.location.setZ(location.getZ());
        this.location.setYaw(location.getYaw());
        this.location.setPitch(location.getPitch());
    }

    /**
     * 총알을 지정한 속도만큼 이동시킨다.
     *
     * @param velocity 이동시킬 속도
     * @throws IllegalStateException 총알이 발사되지 않았으면 발생
     */
    protected final void move(@NonNull Vector velocity) {
        validateIsShot();
        Validate.notNull(this.location).add(velocity);
    }

    /**
     * 총알의 시작 위치부터 현재 위치까지의 거리를 반환한다.
     *
     * @return 거리 (단위: 블록)
     * @throws IllegalStateException 총알이 발사되지 않았으면 발생
     */
    protected final double getDistanceFromStart() {
        validateIsShot();
        return Validate.notNull(startLocation).distance(location);
    }

    /**
     * 총알의 현재 속도를 반환한다.
     *
     * @return 현재 속도
     * @throws IllegalStateException 총알이 발사되지 않았으면 발생
     */
    @NonNull
    protected final Vector getVelocity() {
        validateIsShot();
        return Validate.notNull(velocity).clone();
    }

    /**
     * 총알의 현재 속도를 지정한 속도로 설정한다.
     *
     * @param velocity 설정할 속도
     * @throws IllegalStateException 총알이 발사되지 않았으면 발생
     */
    protected final void setVelocity(@NonNull Vector velocity) {
        validateIsShot();
        Validate.notNull(this.velocity);

        this.velocity.setX(velocity.getX());
        this.velocity.setY(velocity.getY());
        this.velocity.setZ(velocity.getZ());
    }

    /**
     * 총알의 현재 속도에 지정한 속도를 추가한다.
     *
     * @param velocity 추가할 속도
     * @throws IllegalStateException 총알이 발사되지 않았으면 발생
     */
    protected final void push(@NonNull Vector velocity) {
        validateIsShot();
        Validate.notNull(this.velocity).add(velocity);
    }

    /**
     * 총알을 소멸시키고 {@link Bullet#onDestroy(Location)}를 호출한다.
     *
     * @throws IllegalStateException 총알이 발사되지 않았거나 이미 제거되었으면 발생
     */
    public final void destroy() {
        validateIsShot();
        Validate.validState(!isDestroyed, "Bullet이 이미 제거됨");

        onDestroy(getLocation());
        isDestroyed = true;
    }

    /**
     * 지정한 위치와 방향으로 총알을 발사한다.
     *
     * @param start     발사 위치
     * @param direction 발사 방향
     */
    public final void shot(@NonNull Location start, @NonNull Vector direction) {
        if (isShot)
            return;

        isShot = true;

        intervalHandler = getIntervalHandler();
        hitBlockHandler = getHitBlockHandler();
        hitEntityHandler = getHitEntityHandler();

        startLocation = start.clone();
        location = start.clone().add(direction.clone().normalize().multiply(startDistance));
        velocity = direction.clone().normalize().multiply(HITBOX_INTERVAL);

        onShot();
    }

    /**
     * 지정한 위치에서 엔티티가 보는 방향으로 총알을 발사한다.
     *
     * @param start 발사 위치
     */
    public final void shot(@NonNull Location start) {
        shot(start, shooter.getLocation().getDirection());
    }

    /**
     * 엔티티의 눈 위치에서 지정한 방향으로 총알을 발사한다.
     *
     * @param direction 발사 방향
     */
    public final void shot(@NonNull Vector direction) {
        shot((shooter.getEntity() instanceof LivingEntity)
                ? ((LivingEntity) shooter.getEntity()).getEyeLocation()
                : shooter.getLocation(), direction);
    }

    /**
     * 엔티티의 눈 위치에서 엔티티가 보는 방향으로 총알을 발사한다.
     */
    public final void shot() {
        shot(shooter.getLocation().getDirection());
    }

    /**
     * 총알이 발사됐을 때 ({@link Bullet#shot(Location, Vector)} 호출 시) 실행될 작업.
     */
    abstract void onShot();

    /**
     * 총알을 다음 위치로 이동시키고 등록된 처리기들을 실행시킨다.
     */
    final void next() {
        if (!Validate.notNull(intervalHandler).onInterval(getLocation(), index++) || handleBlockCollision() || handleEntityCollision()) {
            destroy();
            return;
        }

        travelDistance += getVelocity().length();
        move(getVelocity());
    }

    /**
     * 총알의 블록 충돌 로직을 처리한다.
     *
     * @return 총알 소멸 여부
     */
    private boolean handleBlockCollision() {
        Location loc = getLocation();
        if (LocationUtil.isNonSolid(loc))
            return false;

        Location hitLoc = LocationUtil.getNearestAgainstEdge(loc, getVelocity().multiply(-1));
        onHit(hitLoc.clone());

        if (!Validate.notNull(hitBlockHandler).onHitBlock(hitLoc.clone(), loc.getBlock())) {
            move(hitLoc);
            return true;
        }

        return false;
    }

    /**
     * 총알 주변의 지정한 조건을 만족하는 엔티티를 찾고 피격 로직을 처리한다.
     *
     * @return 총알 소멸 여부
     */
    private boolean handleEntityCollision() {
        T target = CombatUtil.getNearCombatEntity(getLocation(), size, entityCondition);

        if (target == null || !targets.add(target))
            return false;

        onHit(getLocation());
        return !Validate.notNull(hitEntityHandler).onHitEntity(getLocation(), target);
    }

    /**
     * 총알이 어느 곳이든(블록 또는 엔티티) 맞았을 때 실행될 작업.
     *
     * @param location 맞은 위치
     */
    protected void onHit(@NonNull Location location) {
        // 미사용
    }

    /**
     * 총알이 소멸했을 때 실행될 작업.
     *
     * @param location 마지막 위치
     */
    protected void onDestroy(@NonNull Location location) {
        // 미사용
    }

    /**
     * 지정한 주기마다 작업을 실행하는 판정점 처리기를 생성한다.
     *
     * <p>주로 파티클을 남길 때 사용한다.</p>
     *
     * @param period  반복 주기 (판정점 수)
     * @param onCycle 실행될 작업
     * @return 판정점 처리기
     */
    @NonNull
    protected final IntervalHandler createPeriodIntervalHandler(int period, @NonNull Consumer<@NonNull Location> onCycle) {
        return (location, i) -> {
            if (i % period == 0)
                onCycle.accept(location);

            return true;
        };
    }

    /**
     * 바닥에 닿았을 때 총알을 제거하는 블록 판정 처리기를 생성한다.
     *
     * @param handler 총알이 블록에 맞았을 때 실행될 작업
     * @return 블록 판정 처리기
     */
    @NonNull
    protected final HitBlockHandler createDestroyOnGroundHitBlockHandler(@NonNull HitBlockHandler handler) {
        return (location, hitBlock) ->
                handler.onHitBlock(location, hitBlock) && LocationUtil.isNonSolid(location.subtract(0, HITBOX_INTERVAL, 0));
    }

    /**
     * 치명타 판정을 처리하는 엔티티 판정 처리기를 생성한다.
     *
     * @param handler 총알이 엔티티에 맞았을 때 (일반 또는 치명타) 실행될 작업
     * @return 엔티티 판정 처리기
     * @see HasCritHitbox
     */
    @NonNull
    protected final HitEntityHandler<T> createCritHitEntityHandler(@NonNull CritHitEntityHandler<T> handler) {
        return (location, target) -> {
            boolean isCrit = target instanceof HasCritHitbox
                    && ((HasCritHitbox) target).getCritHitbox() != null
                    && ((HasCritHitbox) target).getCritHitbox().isInHitbox(getLocation(), size);
            return handler.onHitEntity(location, target, isCrit);
        };
    }

    /**
     * 엔티티 치명타 판정을 처리하는 인터페이스.
     *
     * @param <T> {@link CombatEntity}를 상속받는 전투 시스템 엔티티
     */
    @FunctionalInterface
    public interface CritHitEntityHandler<T extends CombatEntity> {
        /**
         * 총알이 엔티티에 맞았을 때 실행될 작업.
         *
         * @param location 맞은 위치
         * @param target   맞은 엔티티
         * @param isCrit   치명타 여부. 엔티티가 {@link HasCritHitbox}이고 {@link HasCritHitbox#getCritHitbox()}에 맞은 경우 {@code true}
         * @return 관통 여부. {@code true} 반환 시 엔티티 관통, {@code false} 반환 시 소멸
         */
        boolean onHitEntity(@NonNull Location location, @NonNull T target, boolean isCrit);
    }

    /**
     * 매 판정점 ({@link Bullet#HITBOX_INTERVAL} 간격)마다 실행될 작업을 처리하는 인터페이스.
     */
    @FunctionalInterface
    public interface IntervalHandler {
        /**
         * 연쇄 판정점 처리기를 생성하여 반환한다.
         *
         * <p>{@link IntervalHandler#onInterval(Location, long)}에서 {@code true}를 반환한 경우 다음 판정점 처리기를 실행한다.</p>
         *
         * @param handler 판정점 처리기
         * @return {@link Chain}
         */
        @NonNull
        static Chain chain(@NonNull IntervalHandler handler) {
            return new Chain(handler);
        }

        /**
         * 매 판정점 ({@link Bullet#HITBOX_INTERVAL} 간격)마다 실행될 작업.
         *
         * @param location 현재 위치
         * @param i        인덱스 (현재 판정점)
         * @return 진행 여부. {@code true} 반환 시 계속 진행, {@code false} 반환 시 소멸
         */
        boolean onInterval(@NonNull Location location, long i);

        /**
         * 연쇄 판정점 처리기 클래스.
         */
        final class Chain extends HandlerChain<IntervalHandler, Chain> implements IntervalHandler {
            private Chain(IntervalHandler handler) {
                super(handler);
            }

            @Override
            public boolean onInterval(@NonNull Location location, long i) {
                if (super.handler.onInterval(location.clone(), i))
                    return super.nextChain == null || super.nextChain.handler.onInterval(location.clone(), i);

                return false;
            }
        }
    }

    /**
     * 총알이 블록에 맞았을 때 실행될 작업을 처리하는 인터페이스.
     */
    @FunctionalInterface
    public interface HitBlockHandler {
        /**
         * 연쇄 블록 판정 처리기를 생성하여 반환한다.
         *
         * <p>{@link HitBlockHandler#onHitBlock(Location, Block)}에서 {@code true}를 반환한 경우 다음 블록 판정 처리기를 실행한다.</p>
         *
         * @param handler 블록 판정 처리기
         * @return {@link Chain}
         */
        @NonNull
        static Chain chain(@NonNull HitBlockHandler handler) {
            return new Chain(handler);
        }

        /**
         * 총알이 블록에 맞았을 때 실행될 작업.
         *
         * @param location 맞은 위치
         * @param hitBlock 맞은 블록
         * @return 관통 여부. {@code true} 반환 시 블록 관통, {@code false} 반환 시 소멸
         */
        boolean onHitBlock(@NonNull Location location, @NonNull Block hitBlock);

        /**
         * 연쇄 블록 판정 처리기 클래스.
         */
        final class Chain extends HandlerChain<HitBlockHandler, Chain> implements HitBlockHandler {
            private Chain(HitBlockHandler handler) {
                super(handler);
            }

            @Override
            public boolean onHitBlock(@NonNull Location location, @NonNull Block hitBlock) {
                if (super.handler.onHitBlock(location.clone(), hitBlock))
                    return super.nextChain == null || super.nextChain.handler.onHitBlock(location.clone(), hitBlock);

                return false;
            }
        }
    }

    /**
     * 총알이 엔티티에 맞았을 때 실행될 작업을 처리하는 인터페이스.
     *
     * @param <T> {@link CombatEntity}를 상속받는 전투 시스템 엔티티
     */
    @FunctionalInterface
    public interface HitEntityHandler<T extends CombatEntity> {
        /**
         * 연쇄 엔티티 판정 처리기를 생성하여 반환한다.
         *
         * <p>{@link HitEntityHandler#onHitEntity(Location, CombatEntity)}에서 {@code true}를 반환한 경우 다음 엔티티 판정 처리기를 실행한다.</p>
         *
         * @param handler 엔티티 판정 처리기
         * @param <T>     {@link CombatEntity}를 상속받는 전투 시스템 엔티티
         * @return {@link Chain}
         */
        @NonNull
        static <T extends CombatEntity> Chain<T> chain(@NonNull HitEntityHandler<T> handler) {
            return new Chain<>(handler);
        }

        /**
         * 총알이 엔티티에 맞았을 때 실행될 작업.
         *
         * @param location 맞은 위치
         * @param target   맞은 엔티티
         * @return 관통 여부. {@code true} 반환 시 엔티티 관통, {@code false} 반환 시 소멸
         */
        boolean onHitEntity(@NonNull Location location, @NonNull T target);

        /**
         * 연쇄 엔티티 판정 처리기 클래스.
         *
         * @param <T> {@link CombatEntity}를 상속받는 전투 시스템 엔티티
         */
        final class Chain<T extends CombatEntity> extends HandlerChain<HitEntityHandler<T>, Chain<T>> implements HitEntityHandler<T> {
            private Chain(HitEntityHandler<T> handler) {
                super(handler);
            }

            @Override
            public boolean onHitEntity(@NonNull Location location, @NonNull T target) {
                if (super.handler.onHitEntity(location.clone(), target))
                    return super.nextChain == null || super.nextChain.handler.onHitEntity(location.clone(), target);

                return false;
            }
        }
    }

    @RequiredArgsConstructor
    private static class HandlerChain<T, U extends HandlerChain<T, U>> {
        private final T handler;
        @Nullable
        private HandlerChain<T, U> nextChain = null;

        /**
         * 다음에 실행될 처리기를 지정한다.
         *
         * @param handler 다음 처리기
         * @return 현재 처리기
         */
        @NonNull
        @SuppressWarnings("unchecked")
        public final U next(@NonNull T handler) {
            if (nextChain == null)
                nextChain = new HandlerChain<>(handler);
            else
                nextChain.next(handler);

            return (U) this;
        }
    }
}
