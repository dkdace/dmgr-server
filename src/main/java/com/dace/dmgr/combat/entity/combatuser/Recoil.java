package com.dace.dmgr.combat.entity.combatuser;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.Timestamp;
import com.dace.dmgr.util.task.IntervalTask;
import lombok.NonNull;
import org.apache.commons.lang3.Validate;

import java.util.stream.IntStream;

/**
 * 화면 반동 효과를 나타내는 클래스.
 *
 * <p>주로 총기 반동에 사용된다.</p>
 */
public final class Recoil {
    /** 수직 반동 */
    private final double up;
    /** 수평 반동 */
    private final double side;
    /** 수직 반동 분산도 */
    private final double upSpread;
    /** 수평 반동 분산도 */
    private final double sideSpread;
    /** 반동 진행 시간 (tick) */
    private final int durationTicks;
    /** 초탄 반동 배수 */
    private final double firstMultiplier;

    /**
     * 화면 반동 효과 인스턴스를 생성한다.
     *
     * @param up              수직 반동
     * @param side            수평 반동
     * @param upSpread        수직 반동 분산도
     * @param sideSpread      수평 반동 분산도
     * @param duration        반동 진행 시간
     * @param firstMultiplier 초탄 반동 배수. 1로 설정 시 차탄과 동일. 1 이상의 값
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    public Recoil(double up, double side, double upSpread, double sideSpread, @NonNull Timespan duration, double firstMultiplier) {
        Validate.isTrue(firstMultiplier >= 1, "firstMultiplier >= 1 (%f)", firstMultiplier);

        this.up = up;
        this.side = side;
        this.upSpread = upSpread;
        this.sideSpread = sideSpread;
        this.durationTicks = (int) Math.min(Integer.MAX_VALUE, duration.toTicks());
        this.firstMultiplier = firstMultiplier;
    }

    /**
     * 지정한 플레이어에게 화면 반동 효과를 전송한다.
     *
     * @param combatUser 대상 플레이어
     */
    public void send(@NonNull CombatUser combatUser) {
        double finalUpSpread = upSpread * (Math.random() - Math.random()) * 0.5;
        double finalSideSpread = sideSpread * (Math.random() - Math.random()) * 0.5;
        boolean first = combatUser.getWeaponFirstRecoilTimestamp().isBefore(Timestamp.now());
        long sum = IntStream.rangeClosed(1, durationTicks).sum();

        combatUser.setWeaponFirstRecoilTimestamp(Timestamp.now().plus(Timespan.ofTicks(4)));
        combatUser.addTask(new IntervalTask(i -> {
            double finalUp = (up + finalUpSpread) / ((double) sum / (durationTicks - i));
            double finalSide = (side + finalSideSpread) / ((double) sum / (durationTicks - i));
            if (first) {
                finalUp *= firstMultiplier;
                finalSide *= firstMultiplier;
            }

            combatUser.addYawAndPitch(finalSide, -finalUp);
        }, 1, durationTicks));
    }
}
