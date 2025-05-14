package com.dace.dmgr.combat.interaction;

import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.util.VectorUtil;
import lombok.NonNull;
import org.apache.commons.lang3.Validate;
import org.bukkit.Location;

/**
 * 산탄총. 동시에 여러 총알을 발사하는 클래스.
 *
 * @param <T> {@link CombatEntity}를 상속받는 전투 시스템 엔티티
 */
public abstract class Shotgun<T extends CombatEntity> {
    /** 산탄 수 */
    protected final double amount;
    /** 탄퍼짐 */
    protected final double spread;

    /** 실행 여부 */
    private boolean isUsed = false;

    /**
     * 산탄총 인스턴스를 생성한다.
     *
     * @param amount 산탄 수. 2 이상의 값
     * @param spread 탄퍼짐. 0 이상의 값
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    protected Shotgun(int amount, double spread) {
        Validate.isTrue(amount >= 2, "amount >= 0 (%f)", amount);
        Validate.isTrue(spread >= 0, "spread >= 0 (%f)", spread);

        this.amount = amount;
        this.spread = spread;
    }

    /**
     * 발사할 총알을 반환한다.
     *
     * @param index 발사 인덱스. 0번째 총알은 탄퍼짐 없이 발사자({@link Bullet#getShooter()})가 보는 방향으로 발사됨
     * @return 발사할 총알
     */
    @NonNull
    protected abstract Bullet<T> getBullet(int index);

    /**
     * 지정한 위치에서 엔티티가 보는 방향으로 총알을 발사한다.
     *
     * @param start 발사 위치
     */
    public final void shot(@NonNull Location start) {
        if (isUsed)
            return;

        isUsed = true;

        getBullet(0).shot(start);
        for (int i = 1; i < amount; i++)
            getBullet(i).shot(start, VectorUtil.getSpreadedVector(start.getDirection(), spread));
    }

    /**
     * 엔티티의 눈 위치에서 엔티티가 보는 방향으로 총알을 발사한다.
     */
    public final void shot() {
        if (isUsed)
            return;

        isUsed = true;

        getBullet(0).shot();
        for (int i = 1; i < amount; i++) {
            Bullet<T> bullet = getBullet(i);
            bullet.shot(VectorUtil.getSpreadedVector(bullet.getShooter().getLocation().getDirection(), spread));
        }
    }
}
