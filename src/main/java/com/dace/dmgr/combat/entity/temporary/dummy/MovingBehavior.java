package com.dace.dmgr.combat.entity.temporary.dummy;

import com.dace.dmgr.util.EntityUtil;
import lombok.NonNull;
import net.citizensnpcs.api.ai.Navigator;
import net.citizensnpcs.api.ai.tree.BehaviorGoalAdapter;
import net.citizensnpcs.api.ai.tree.BehaviorStatus;
import org.bukkit.Location;

/**
 * 지정된 위치를 따라 움직이는 더미의 행동 양식 클래스.
 */
public final class MovingBehavior implements DummyBehavior {
    /** 이동할 위치 목록 */
    private final Location[] points;

    /**
     * 위치 이동 행동 양식 인스턴스를 생성한다.
     *
     * @param points 이동할 위치 목록
     */
    public MovingBehavior(@NonNull Location @NonNull ... points) {
        this.points = points;
    }

    @Override
    public void onInit(@NonNull Dummy dummy) {
        dummy.getNpc().getDefaultGoalController().addGoal(new BehaviorGoalAdapter() {
            private int i = 0;

            @Override
            public void reset() {
                dummy.getNpc().getNavigator().cancelNavigation();
                EntityUtil.teleport(dummy.getEntity(), getCurrentTarget());
            }

            @Override
            public BehaviorStatus run() {
                Navigator navigator = dummy.getNpc().getNavigator();

                navigator.getDefaultParameters().stuckAction((npc, targetNavigator) -> false);

                if (shouldExecute()) {
                    navigator.setTarget(getCurrentTarget());
                    i++;
                }

                return BehaviorStatus.RUNNING;
            }

            @Override
            public boolean shouldExecute() {
                return !dummy.getNpc().getNavigator().isNavigating();
            }

            /**
             * 현재 목적지를 반환한다.
             *
             * @return 현재 목적지 위치
             */
            @NonNull
            private Location getCurrentTarget() {
                return points[i % points.length];
            }
        }, 1);
    }
}
