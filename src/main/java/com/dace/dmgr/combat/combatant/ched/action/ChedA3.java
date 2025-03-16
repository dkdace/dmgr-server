package com.dace.dmgr.combat.combatant.ched.action;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.ActiveSkill;
import com.dace.dmgr.combat.action.skill.HasBonusScore;
import com.dace.dmgr.combat.action.skill.module.BonusScoreModule;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.DamageType;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.module.AbilityStatus;
import com.dace.dmgr.combat.interaction.Projectile;
import com.dace.dmgr.user.User;
import com.dace.dmgr.util.LocationUtil;
import com.dace.dmgr.util.VectorUtil;
import com.dace.dmgr.util.task.IntervalTask;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.inventory.MainHand;
import org.bukkit.util.Vector;

import java.util.function.LongConsumer;

@Getter
public final class ChedA3 extends ActiveSkill implements HasBonusScore {
    /** 수정자 */
    private static final AbilityStatus.Modifier MODIFIER = new AbilityStatus.Modifier(-ChedA3Info.READY_SLOW);
    /** 보너스 점수 모듈 */
    @NonNull
    private final BonusScoreModule bonusScoreModule;

    public ChedA3(@NonNull CombatUser combatUser) {
        super(combatUser, ChedA3Info.getInstance(), ChedA3Info.COOLDOWN, Timespan.MAX, 2);
        this.bonusScoreModule = new BonusScoreModule(this, "탐지 보너스", ChedA3Info.KILL_SCORE);
    }

    @Override
    @NonNull
    public ActionKey @NonNull [] getDefaultActionKeys() {
        return new ActionKey[]{ActionKey.SLOT_3};
    }

    @Override
    public boolean canUse(@NonNull ActionKey actionKey) {
        ChedP1 skillp1 = combatUser.getSkill(ChedP1Info.getInstance());
        return super.canUse(actionKey) && isDurationFinished() && (skillp1.isDurationFinished() || skillp1.isHanging());
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        setDuration();
        combatUser.setGlobalCooldown(ChedA3Info.READY_DURATION);
        combatUser.getMoveModule().getSpeedStatus().addModifier(MODIFIER);
        combatUser.getWeapon().cancel();
        ((ChedWeapon) combatUser.getWeapon()).setCanShoot(false);

        ChedA3Info.SOUND.USE.play(combatUser.getLocation());

        ChedP1 skillp1 = combatUser.getSkill(ChedP1Info.getInstance());

        addActionTask(new IntervalTask(i -> {
            if (!skillp1.isDurationFinished() && !skillp1.isHanging())
                return false;

            Location loc = LocationUtil.getLocationFromOffset(combatUser.getArmLocation(MainHand.RIGHT), 0, 0, 1.5);
            playUseTickEffect(loc, i);

            return true;
        }, isCancelled -> {
            cancel();
            if (isCancelled)
                return;

            Location location = combatUser.getArmLocation(MainHand.RIGHT);
            new ChedA3Projectile().shot(location);

            ChedA3Info.SOUND.USE_READY.play(location);
            Location loc = LocationUtil.getLocationFromOffset(location, 0, 0, 1.5);

            addActionTask(new IntervalTask((LongConsumer) i -> playUseTickEffect(loc, i + ChedA3Info.READY_DURATION.toTicks()),
                    1, ChedA3Info.READY_DURATION.toTicks()));
        }, 1, ChedA3Info.READY_DURATION.toTicks()));
    }

    @Override
    public boolean isCancellable() {
        return !isDurationFinished();
    }

    @Override
    protected void onCancelled() {
        setDuration(Timespan.ZERO);
        combatUser.getMoveModule().getSpeedStatus().removeModifier(MODIFIER);
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

    private final class ChedA3Projectile extends Projectile<Damageable> {
        private ChedA3Projectile() {
            super(combatUser, ChedA3Info.VELOCITY, CombatUtil.EntityCondition.enemy(combatUser).and(Damageable::isCreature),
                    Option.builder().size(ChedA3Info.SIZE).build());
        }

        @Override
        @NonNull
        protected IntervalHandler getIntervalHandler() {
            return createPeriodIntervalHandler(18, location -> {
                location.setPitch(0);

                ChedA3Info.SOUND.TICK.play(location);

                ChedA3Info.PARTICLE.BULLET_TRAIL_CORE.play(location);

                ChedA3Info.PARTICLE.BULLET_TRAIL_SHAPE.play(LocationUtil.getLocationFromOffset(location, 0, -0.5, -0.6),
                        0.2, 0.12);
                ChedA3Info.PARTICLE.BULLET_TRAIL_SHAPE.play(LocationUtil.getLocationFromOffset(location, 0, -0.7, -1.2),
                        0.16, 0.08);
                ChedA3Info.PARTICLE.BULLET_TRAIL_SHAPE.play(LocationUtil.getLocationFromOffset(location, 0, -0.9, -1.8),
                        0.12, 0.04);

                ChedA3Info.PARTICLE.BULLET_TRAIL_SHAPE.play(LocationUtil.getLocationFromOffset(location, 0, 0.4, 0.8),
                        0.1, 0.16);
                ChedA3Info.PARTICLE.BULLET_TRAIL_SHAPE.play(LocationUtil.getLocationFromOffset(location, 0, 0.6, 1),
                        0.1, 0.16);
                ChedA3Info.PARTICLE.BULLET_TRAIL_SHAPE.play(LocationUtil.getLocationFromOffset(location, 0, 0.8, 1.4),
                        0.18, 0.16);
                ChedA3Info.PARTICLE.BULLET_TRAIL_SHAPE.play(LocationUtil.getLocationFromOffset(location, 0, 0.8, 1.6),
                        0.24, 0.16);

                for (int i = 0; i < 6; i++) {
                    ChedA3Info.PARTICLE.BULLET_TRAIL_SHAPE.play(
                            LocationUtil.getLocationFromOffset(location, 0.7 + i * 0.4, 0.3 + i * (i < 3 ? 0.2 : 0.25), 0),
                            0.1, 0.1 + i * 0.04);
                    ChedA3Info.PARTICLE.BULLET_TRAIL_SHAPE.play(
                            LocationUtil.getLocationFromOffset(location, -0.7 - i * 0.4, 0.3 + i * (i < 3 ? 0.2 : 0.25), 0),
                            0.1, 0.1 + i * 0.04);
                }
            });
        }

        @Override
        @NonNull
        protected HitBlockHandler getHitBlockHandler() {
            return (location, hitBlock) -> true;
        }

        @Override
        @NonNull
        protected HitEntityHandler<Damageable> getHitEntityHandler() {
            return (location, target) -> {
                if (target.getDamageModule().damage(this, 0, DamageType.NORMAL, location, false, true)) {
                    combatUser.getUser().setGlowing(target.getEntity(), ChatColor.RED, ChedA3Info.DETECT_DURATION);
                    combatUser.getEntity().getWorld().getPlayers().stream()
                            .map(teamTarget -> CombatUser.fromUser(User.fromPlayer(teamTarget)))
                            .filter(teamTarget -> teamTarget != null && teamTarget != combatUser && !teamTarget.isEnemy(combatUser))
                            .forEach(teamTarget -> teamTarget.getUser().setGlowing(target.getEntity(), ChatColor.RED,
                                    ChedA3Info.DETECT_DURATION));

                    if (target instanceof CombatUser) {
                        combatUser.addScore("적 탐지", ChedA3Info.DETECT_SCORE);
                        bonusScoreModule.addTarget((CombatUser) target, ChedA3Info.KILL_SCORE_TIME_LIMIT);
                    }
                }

                return true;
            };
        }
    }
}
