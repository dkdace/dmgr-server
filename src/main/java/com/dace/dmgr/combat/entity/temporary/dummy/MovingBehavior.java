package com.dace.dmgr.combat.entity.temporary.dummy;

import lombok.NonNull;
import net.citizensnpcs.api.ai.Navigator;
import org.bukkit.Location;

import java.util.function.LongConsumer;

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
        dummy.addOnTick(new LongConsumer() {
            private int index = 0;

            @Override
            public void accept(long i) {
                Navigator navigator = dummy.getNpc().getNavigator();
                if (!navigator.isNavigating())
                    navigator.setStraightLineTarget(points[index++ % points.length]);
            }
        });
    }
}
