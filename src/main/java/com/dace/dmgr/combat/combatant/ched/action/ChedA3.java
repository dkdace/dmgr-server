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
import com.dace.dmgr.util.LocationUtil;
import com.dace.dmgr.util.VectorUtil;
import com.dace.dmgr.util.task.IntervalTask;
import lombok.Getter;
import lombok.NoArgsConstructor;
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

        ChedWeapon weapon = (ChedWeapon) combatUser.getWeapon();
        weapon.cancel();
        weapon.setCanShoot(false);

        ChedA3Info.SOUND.USE.play(combatUser.getLocation());

        long durationTicks = ChedA3Info.READY_DURATION.toTicks();
        EffectManager effectManager = new EffectManager();

        addActionTask(new IntervalTask(i -> {
            Location loc = LocationUtil.getLocationFromOffset(combatUser.getArmLocation(MainHand.RIGHT), 0, 0, 1.5);
            effectManager.playEffect(loc);
        }, () -> {
            cancel();

            Location location = combatUser.getArmLocation(MainHand.RIGHT);
            new ChedA3Projectile().shot(location);

            ChedA3Info.SOUND.USE_READY.play(location);

            Location loc = LocationUtil.getLocationFromOffset(location, 0, 0, 1.5);
            addActionTask(new IntervalTask((LongConsumer) i -> effectManager.playEffect(loc), 1, durationTicks));
        }, 1, durationTicks));
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
     * 효과를 재생하는 클래스.
     */
    @NoArgsConstructor
    private static final class EffectManager {
        private int index = 0;
        private int angle = 0;
        private double distance = 0;
        private double forward = 0;

        /**
         * 효과를 재생한다.
         *
         * @param location 사용 위치
         */
        private void playEffect(@NonNull Location location) {
            Vector vector = VectorUtil.getYawAxis(location);
            Vector axis = VectorUtil.getRollAxis(location);

            for (int i = 0; i < 2; i++) {
                if (index > 12) {
                    angle += 4;
                    distance -= 0.03;
                    forward += 0.2;
                } else {
                    angle += 8;
                    distance += 0.04;
                }

                for (int j = 0; j < 10; j++) {
                    angle += 360 / 5;
                    Vector vec = VectorUtil.getRotatedVector(vector, axis, angle);
                    Vector vec2 = VectorUtil.getRotatedVector(vector, axis, angle + 10.0);
                    Vector vec3 = vec.clone().multiply(distance + (j < 5 ? 0 : 1.4));

                    Location loc = location.clone().add(vec3).add(location.getDirection().multiply(forward));
                    Vector dir = LocationUtil.getDirection(location.clone().add(vec), location.clone().add(vec2));

                    ChedA3Info.PARTICLE.USE_TICK.play(loc, dir);
                }
            }

            index++;
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

                    CombatUtil.getCombatEntities(combatUser.getGame(), CombatUtil.EntityCondition.team(combatUser).exclude(combatUser)
                                    .and(CombatUser.class::isInstance))
                            .forEach(teamTarget -> ((CombatUser) teamTarget).getUser().setGlowing(target.getEntity(), ChatColor.RED,
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
