package com.dace.dmgr.combat.entity.temporary.dummy;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.Timestamp;
import com.dace.dmgr.combat.entity.CombatRestriction;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.combat.entity.temporary.spawnhandler.PlayerNPCSpawnHandler;
import com.dace.dmgr.util.LocationUtil;
import com.dace.dmgr.util.task.DelayTask;
import com.dace.dmgr.util.task.IntervalTask;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.citizensnpcs.api.ai.Navigator;
import net.citizensnpcs.util.NMS;
import org.apache.commons.lang3.RandomUtils;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.MustBeInvokedByOverriders;

import java.util.function.Function;
import java.util.function.LongConsumer;

/**
 * 훈련장 아레나 더미의 행동 양식 클래스.
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class ArenaDummyBehavior implements DummyBehavior {
    /** 목표 플레이어 */
    @NonNull
    protected final CombatUser target;

    /** 공격 타임스탬프 */
    private Timestamp attackTimestamp = Timestamp.now();
    /** 길찾기 타임스탬프 */
    private Timestamp navigateTimestamp = Timestamp.now();

    @Override
    @MustBeInvokedByOverriders
    public void onInit(@NonNull Dummy dummy) {
        navigate(dummy);

        dummy.addOnTick(new LongConsumer() {
            private Vector lookDirection = dummy.getLocation().getDirection();

            @Override
            public void accept(long i) {
                dummy.getEntity().setSprinting(!dummy.getStatusEffectModule().hasRestriction(CombatRestriction.SPRINT) && canSprint(dummy));

                if (!dummy.getStatusEffectModule().hasRestriction(CombatRestriction.USE_WEAPON) && attackTimestamp.isBefore(Timestamp.now())
                        && canAttack(dummy)) {
                    attackTimestamp = Timestamp.now().plus(Timespan.ofSeconds(1));
                    onAttack(dummy);
                }

                if (!dummy.getStatusEffectModule().hasRestriction(CombatRestriction.JUMP) && Math.random() < getJumpChance(dummy))
                    jump(dummy);

                if (LocationUtil.canPass(dummy.getCenterLocation(), target.getCenterLocation())) {
                    if (Math.random() < 0.15)
                        lookDirection = LocationUtil.getDirection(dummy.getLocation(), target.getLocation());

                    Location lookLocation = dummy.getLocation().setDirection(lookDirection);
                    NMS.setHeadYaw(dummy.getEntity(), lookLocation.getYaw());
                    NMS.setPitch(dummy.getEntity(), lookLocation.getPitch());

                    if (Math.random() < getSneakChance(dummy))
                        sneak(dummy);
                }
            }
        });
    }

    /**
     * 더미가 공격할 수 있는지 확인한다.
     *
     * @param dummy 대상 더미
     * @return 공격 가능 여부
     */
    protected abstract boolean canAttack(@NonNull Dummy dummy);

    /**
     * 더미가 공격 시 실행할 작업.
     *
     * @param dummy 대상 더미
     */
    protected abstract void onAttack(@NonNull Dummy dummy);

    /**
     * 목표 길을 찾아 이동한다.
     *
     * @param dummy 대상 더미
     */
    private void navigate(@NonNull Dummy dummy) {
        Navigator navigator = PlayerNPCSpawnHandler.getNPC(dummy).getNavigator();

        navigator.setTarget(target.getEntity(), false);
        navigator.getLocalParameters().entityTargetLocationMapper(new Function<Entity, Location>() {
            private Location targetLocation = target.getLocation();

            @Override
            public Location apply(Entity entity) {
                if (PlayerNPCSpawnHandler.getNPC(dummy).getNavigator().isNavigating() && navigateTimestamp.isAfter(Timestamp.now()))
                    return targetLocation;

                navigateTimestamp = Timestamp.now().plus(Timespan.ofSeconds(RandomUtils.nextDouble(0.5, 1)));
                targetLocation = getNavigateTargetLocation(dummy);

                return navigator.canNavigateTo(targetLocation) ? targetLocation : target.getLocation();
            }
        });
    }

    /**
     * 이동할 목표 위치를 반환한다.
     *
     * @param dummy 대상 더미
     * @return 목표 위치
     */
    protected abstract Location getNavigateTargetLocation(@NonNull Dummy dummy);

    /**
     * 더미가 달리기를 할 수 있는지 확인한다.
     *
     * @param dummy 대상 더미
     * @return 달리기 가능 여부
     */
    protected abstract boolean canSprint(@NonNull Dummy dummy);

    /**
     * 더미의 점프를 실행한다.
     *
     * @param dummy 대상 더미
     */
    private void jump(@NonNull Dummy dummy) {
        if (NMS.shouldJump(dummy.getEntity()) || !NMS.isOnGround(dummy.getEntity()))
            return;

        NMS.setShouldJump(dummy.getEntity());

        dummy.addTask(new DelayTask(() -> dummy.addTask(new IntervalTask(i -> !NMS.isOnGround(dummy.getEntity()),
                () -> navigateTimestamp = Timestamp.now(), 1)), 2));
    }

    /**
     * 더미가 틱당 점프할 확률을 반환한다.
     *
     * @param dummy 대상 더미
     * @return 틱당 점프할 확률
     */
    protected abstract double getJumpChance(@NonNull Dummy dummy);

    /**
     * 더미의 웅크리기를 실행한다.
     *
     * @param dummy 대상 더미
     */
    private void sneak(@NonNull Dummy dummy) {
        dummy.getEntity().setSneaking(true);
        dummy.addTask(new DelayTask(() -> dummy.getEntity().setSneaking(false), getSneakDuration(dummy).toTicks()));
    }

    /**
     * 더미의 웅크리기 지속시간을 반환한다.
     *
     * @param dummy 대상 더미
     * @return 웅크리기 지속시간
     */
    @NonNull
    protected abstract Timespan getSneakDuration(@NonNull Dummy dummy);

    /**
     * 더미가 틱당 웅크릴 확률을 반환한다.
     *
     * @param dummy 대상 더미
     * @return 웅크릴 확률
     */
    protected abstract double getSneakChance(@NonNull Dummy dummy);
}
