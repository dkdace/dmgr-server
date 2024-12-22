package com.dace.dmgr.combat.character.ched.action;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.ActiveSkill;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.interaction.DamageType;
import com.dace.dmgr.combat.interaction.Projectile;
import com.dace.dmgr.combat.interaction.ProjectileOption;
import com.dace.dmgr.user.User;
import com.dace.dmgr.util.LocationUtil;
import com.dace.dmgr.util.Timespan;
import com.dace.dmgr.util.Timestamp;
import com.dace.dmgr.util.VectorUtil;
import com.dace.dmgr.util.task.IntervalTask;
import com.dace.dmgr.util.task.TaskUtil;
import lombok.NonNull;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

import java.util.WeakHashMap;
import java.util.function.LongConsumer;

public final class ChedA3 extends ActiveSkill {
    /** 수정자 ID */
    private static final String MODIFIER_ID = "ChedA3";
    /** 처치 점수 제한시간 타임스탬프 목록 (피격자 : 종료 시점) */
    private final WeakHashMap<CombatUser, Timestamp> killScoreTimeLimitTimestampMap = new WeakHashMap<>();

    public ChedA3(@NonNull CombatUser combatUser) {
        super(combatUser, ChedA3Info.getInstance(), 2);
    }

    @Override
    @NonNull
    public ActionKey @NonNull [] getDefaultActionKeys() {
        return new ActionKey[]{ActionKey.SLOT_3};
    }

    @Override
    public long getDefaultCooldown() {
        return ChedA3Info.COOLDOWN;
    }

    @Override
    public long getDefaultDuration() {
        return -1;
    }

    @Override
    public boolean canUse(@NonNull ActionKey actionKey) {
        ChedP1 skillp1 = combatUser.getSkill(ChedP1Info.getInstance());
        return super.canUse(actionKey) && isDurationFinished() && (skillp1.isDurationFinished() || skillp1.isHanging());
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        setDuration();
        combatUser.setGlobalCooldown((int) ChedA3Info.READY_DURATION);
        combatUser.getMoveModule().getSpeedStatus().addModifier(MODIFIER_ID, -ChedA3Info.READY_SLOW);
        combatUser.getWeapon().onCancelled();
        ((ChedWeapon) combatUser.getWeapon()).setCanShoot(false);

        ChedA3Info.SOUND.USE.play(combatUser.getEntity().getLocation());

        ChedP1 skillp1 = combatUser.getSkill(ChedP1Info.getInstance());

        TaskUtil.addTask(taskRunner, new IntervalTask(i -> {
            if (!skillp1.isDurationFinished() && !skillp1.isHanging())
                return false;

            Location loc = LocationUtil.getLocationFromOffset(combatUser.getArmLocation(true), 0, 0, 1.5);
            playUseTickEffect(loc, i);

            return true;
        }, isCancelled -> {
            onCancelled();
            if (isCancelled)
                return;

            Location location = combatUser.getArmLocation(true);
            new ChedA3Projectile().shoot(location);

            ChedA3Info.SOUND.USE_READY.play(location);
            Location loc = LocationUtil.getLocationFromOffset(location, 0, 0, 1.5);

            TaskUtil.addTask(taskRunner, new IntervalTask((LongConsumer) i -> playUseTickEffect(loc, i + ChedA3Info.READY_DURATION),
                    1, ChedA3Info.READY_DURATION));
        }, 1, ChedA3Info.READY_DURATION));
    }

    @Override
    public boolean isCancellable() {
        return !isDurationFinished();
    }

    @Override
    public void onCancelled() {
        super.onCancelled();

        setDuration(0);
        combatUser.getMoveModule().getSpeedStatus().removeModifier(MODIFIER_ID);
    }

    /**
     * 사용 시 효과를 재생한다.
     *
     * @param location 사용 위치
     * @param i        인덱스
     */
    private void playUseTickEffect(@NonNull Location location, long i) {
        Vector vector = VectorUtil.getYawAxis(location);
        Vector axis = VectorUtil.getRollAxis(location);

        for (int j = 0; j < 2; j++) {
            long index = i * 2 + j;
            long angle = index * (i > 12 ? 4 : 8);
            double distance = index * 0.04;
            double forward = 0;
            if (i > 12) {
                forward = (index - 24) * 0.2;
                distance = 48 * 0.04 - index * 0.03;
            }

            for (int k = 0; k < 10; k++) {
                angle += 72;
                Vector vec = VectorUtil.getRotatedVector(vector, axis, angle);
                Vector vec2 = VectorUtil.getRotatedVector(vector, axis, angle + 10.0);
                Vector vec3 = vec.clone().multiply(distance + (k < 5 ? 0 : 1.4));
                Vector dir = LocationUtil.getDirection(location.clone().add(vec), location.clone().add(vec2));
                Location loc2 = location.clone().add(vec3).add(location.getDirection().multiply(forward));

                ChedA3Info.PARTICLE.USE_TICK.play(loc2, dir);
            }
        }
    }

    /**
     * 플레이어에게 보너스 점수를 지급한다.
     *
     * @param victim 피격자
     * @param score  점수 (처치 기여도)
     */
    public void applyBonusScore(@NonNull CombatUser victim, int score) {
        Timestamp expiration = killScoreTimeLimitTimestampMap.get(victim);
        if (expiration != null && expiration.isAfter(Timestamp.now()))
            combatUser.addScore("탐지 보너스", ChedA3Info.KILL_SCORE * score / 100.0);
    }

    private final class ChedA3Projectile extends Projectile {
        private ChedA3Projectile() {
            super(combatUser, ChedA3Info.VELOCITY, ProjectileOption.builder().trailInterval(18).size(ChedA3Info.SIZE)
                    .condition(combatEntity -> ((Damageable) combatEntity).getDamageModule().isLiving()
                            && combatEntity.isEnemy(ChedA3.this.combatUser)).build());
        }

        @Override
        protected void onTrailInterval() {
            playTickEffect();
            ChedA3Info.SOUND.TICK.play(getLocation());
        }

        private void playTickEffect() {
            Location loc = getLocation().clone();
            loc.setPitch(0);

            ChedA3Info.PARTICLE.BULLET_TRAIL_CORE.play(loc);

            ChedA3Info.PARTICLE.BULLET_TRAIL_SHAPE.play(LocationUtil.getLocationFromOffset(loc, 0, -0.5, -0.6),
                    0.2, 0.12);
            ChedA3Info.PARTICLE.BULLET_TRAIL_SHAPE.play(LocationUtil.getLocationFromOffset(loc, 0, -0.7, -1.2),
                    0.16, 0.08);
            ChedA3Info.PARTICLE.BULLET_TRAIL_SHAPE.play(LocationUtil.getLocationFromOffset(loc, 0, -0.9, -1.8),
                    0.12, 0.04);

            ChedA3Info.PARTICLE.BULLET_TRAIL_SHAPE.play(LocationUtil.getLocationFromOffset(loc, 0, 0.4, 0.8),
                    0.1, 0.16);
            ChedA3Info.PARTICLE.BULLET_TRAIL_SHAPE.play(LocationUtil.getLocationFromOffset(loc, 0, 0.6, 1),
                    0.1, 0.16);
            ChedA3Info.PARTICLE.BULLET_TRAIL_SHAPE.play(LocationUtil.getLocationFromOffset(loc, 0, 0.8, 1.4),
                    0.18, 0.16);
            ChedA3Info.PARTICLE.BULLET_TRAIL_SHAPE.play(LocationUtil.getLocationFromOffset(loc, 0, 0.8, 1.6),
                    0.24, 0.16);

            for (int i = 0; i < 6; i++) {
                ChedA3Info.PARTICLE.BULLET_TRAIL_SHAPE.play(
                        LocationUtil.getLocationFromOffset(loc, 0.7 + i * 0.4, 0.3 + i * (i < 3 ? 0.2 : 0.25), 0),
                        0.1, 0.1 + i * 0.04);
                ChedA3Info.PARTICLE.BULLET_TRAIL_SHAPE.play(
                        LocationUtil.getLocationFromOffset(loc, -0.7 - i * 0.4, 0.3 + i * (i < 3 ? 0.2 : 0.25), 0),
                        0.1, 0.1 + i * 0.04);
            }
        }

        @Override
        protected boolean onHitBlock(@NonNull Block hitBlock) {
            return true;
        }

        @Override
        protected boolean onHitEntity(@NonNull Damageable target, boolean isCrit) {
            if (target.getDamageModule().damage(this, 0, DamageType.NORMAL, getLocation(), false, true)) {
                combatUser.getUser().setGlowing(target.getEntity(), ChatColor.RED, ChedA3Info.DETECT_DURATION);
                combatUser.getEntity().getWorld().getPlayers().stream()
                        .map(teamTarget -> CombatUser.fromUser(User.fromPlayer(teamTarget)))
                        .filter(teamTarget -> teamTarget != null && teamTarget != combatUser && !teamTarget.isEnemy(combatUser))
                        .forEach(teamTarget -> teamTarget.getUser().setGlowing(target.getEntity(), ChatColor.RED, ChedA3Info.DETECT_DURATION));

                if (target instanceof CombatUser) {
                    combatUser.addScore("적 탐지", ChedA3Info.DETECT_SCORE);
                    killScoreTimeLimitTimestampMap.put((CombatUser) target, Timestamp.now().plus(Timespan.ofTicks(ChedA3Info.KILL_SCORE_TIME_LIMIT)));
                }
            }

            return true;
        }
    }
}
