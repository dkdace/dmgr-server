package com.dace.dmgr.combat.entity.temporary.dummy;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.util.LocationUtil;
import com.dace.dmgr.util.VectorUtil;
import lombok.NonNull;
import org.apache.commons.lang3.RandomUtils;
import org.bukkit.Location;
import org.bukkit.util.Vector;

/**
 * 훈련장 아레나의 원거리 공격 더미의 행동 양식 클래스.
 *
 * <p>자유롭게 움직이며 목표 플레이어를 향해 총알을 발사하여 공격한다.</p>
 */
public final class ArenaRangedDummyBehavior extends ArenaDummyBehavior {
    /** 공격력 */
    private final int damage;

    /**
     * 아레나 원거리 공격 더미 행동 양식 인스턴스를 생성한다.
     *
     * @param target 목표 플레이어
     * @param damage 공격력
     */
    public ArenaRangedDummyBehavior(@NonNull CombatUser target, int damage) {
        super(target);
        this.damage = damage;
    }

    @Override
    protected boolean canAttack(@NonNull Dummy dummy) {
        return LocationUtil.canPass(dummy.getCenterLocation(), target.getCenterLocation()) && Math.random() < 0.2;
    }

    @Override
    protected void onAttack(@NonNull Dummy dummy) {
        Vector dir = VectorUtil.getSpreadedVector(LocationUtil.getDirection(dummy.getEntity().getEyeLocation(), target.getHitboxCenter()), 30);
        new DummyProjectile(dummy, damage).shot(dir);
    }

    @Override
    protected Location getNavigateTargetLocation(@NonNull Dummy dummy) {
        boolean canPass = LocationUtil.canPass(dummy.getCenterLocation(), target.getCenterLocation());
        double maxVariation = 4;
        double x = RandomUtils.nextDouble(0, maxVariation) - RandomUtils.nextDouble(0, maxVariation);
        double z = RandomUtils.nextDouble(0, maxVariation) - RandomUtils.nextDouble(0, maxVariation);

        return LocationUtil.getNearestAgainstEdge((canPass ? dummy.getLocation() : target.getLocation()).add(x, 0.1, z),
                new Vector(0, -1, 0), 4);
    }

    @Override
    protected boolean canSprint(@NonNull Dummy dummy) {
        return false;
    }

    @Override
    protected double getJumpChance(@NonNull Dummy dummy) {
        return 0.08;
    }

    @Override
    @NonNull
    protected Timespan getSneakDuration(@NonNull Dummy dummy) {
        return Timespan.ofSeconds(RandomUtils.nextDouble(0.3, 0.9));
    }

    @Override
    protected double getSneakChance(@NonNull Dummy dummy) {
        return 0.03;
    }
}
