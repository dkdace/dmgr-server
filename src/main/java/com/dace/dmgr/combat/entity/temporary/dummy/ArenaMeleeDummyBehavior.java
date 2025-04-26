package com.dace.dmgr.combat.entity.temporary.dummy;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.DamageType;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.temporary.spawnhandler.PlayerNPCSpawnHandler;
import com.dace.dmgr.util.LocationUtil;
import lombok.NonNull;
import net.citizensnpcs.util.NMS;
import org.apache.commons.lang3.RandomUtils;
import org.bukkit.Location;
import org.bukkit.util.Vector;

/**
 * 훈련장 아레나의 근접 공격 더미의 행동 양식 클래스.
 *
 * <p>목표 플레이어를 향해 움직이며 근접 공격한다.</p>
 */
public final class ArenaMeleeDummyBehavior extends ArenaDummyBehavior {
    /** 공격력 */
    private final int damage;

    /**
     * 아레나 근접 공격 더미 행동 양식 인스턴스를 생성한다.
     *
     * @param target 목표 플레이어
     * @param damage 공격력
     */
    public ArenaMeleeDummyBehavior(@NonNull CombatUser target, int damage) {
        super(target);
        this.damage = damage;
    }

    @Override
    public void onDefaultAttack(@NonNull Dummy dummy, @NonNull Damageable victim) {
        victim.getDamageModule().damage(dummy, damage, DamageType.NORMAL, null, false, false);
    }

    @Override
    protected boolean canAttack(@NonNull Dummy dummy) {
        return dummy.getCenterLocation().distance(target.getCenterLocation()) < 2 && Math.random() < 0.3;
    }

    @Override
    protected void onAttack(@NonNull Dummy dummy) {
        NMS.attack(dummy.getEntity(), target.getEntity());
    }

    @Override
    protected Location getNavigateTargetLocation(@NonNull Dummy dummy) {
        double maxVariation = LocationUtil.canPass(dummy.getCenterLocation(), target.getCenterLocation()) ? 3 : 6;
        double x = RandomUtils.nextDouble(0, maxVariation) - RandomUtils.nextDouble(0, maxVariation);
        double z = RandomUtils.nextDouble(0, maxVariation) - RandomUtils.nextDouble(0, maxVariation);
        Location targetLocation = LocationUtil.getNearestAgainstEdge(target.getLocation().add(x, 0.1, z), new Vector(0, -1, 0), 4);

        return PlayerNPCSpawnHandler.getNPC(dummy).getNavigator().canNavigateTo(targetLocation) ? targetLocation : target.getLocation();
    }

    @Override
    protected boolean canSprint(@NonNull Dummy dummy) {
        return dummy.getCenterLocation().distance(target.getCenterLocation()) > 3;
    }

    @Override
    protected double getJumpChance(@NonNull Dummy dummy) {
        return 0.13;
    }

    @Override
    @NonNull
    protected Timespan getSneakDuration(@NonNull Dummy dummy) {
        return Timespan.ofSeconds(RandomUtils.nextDouble(0.1, 0.4));
    }

    @Override
    protected double getSneakChance(@NonNull Dummy dummy) {
        return 0.02;
    }
}
