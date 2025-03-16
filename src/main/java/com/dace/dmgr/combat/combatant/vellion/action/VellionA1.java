package com.dace.dmgr.combat.combatant.vellion.action;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.ActiveSkill;
import com.dace.dmgr.combat.action.skill.Summonable;
import com.dace.dmgr.combat.action.skill.module.EntityModule;
import com.dace.dmgr.combat.entity.*;
import com.dace.dmgr.combat.entity.module.AbilityStatus;
import com.dace.dmgr.combat.entity.module.statuseffect.Poison;
import com.dace.dmgr.combat.entity.module.statuseffect.Snare;
import com.dace.dmgr.combat.entity.module.statuseffect.StatusEffect;
import com.dace.dmgr.combat.entity.temporary.SummonEntity;
import com.dace.dmgr.combat.interaction.Area;
import com.dace.dmgr.combat.interaction.Hitbox;
import com.dace.dmgr.util.LocationUtil;
import com.dace.dmgr.util.VectorUtil;
import com.dace.dmgr.util.task.IntervalTask;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.inventory.MainHand;
import org.bukkit.util.Vector;

import java.util.HashSet;

@Getter
public final class VellionA1 extends ActiveSkill implements Summonable<VellionA1.VellionA1Entity> {
    /** 수정자 */
    private static final AbilityStatus.Modifier MODIFIER = new AbilityStatus.Modifier(-VellionA1Info.READY_SLOW);
    /** 소환 엔티티 모듈 */
    @NonNull
    private final EntityModule<VellionA1Entity> entityModule;

    public VellionA1(@NonNull CombatUser combatUser) {
        super(combatUser, VellionA1Info.getInstance(), VellionA1Info.COOLDOWN, Timespan.MAX, 0);
        entityModule = new EntityModule<>(this);
    }

    @Override
    @NonNull
    public ActionKey @NonNull [] getDefaultActionKeys() {
        return new ActionKey[]{ActionKey.SLOT_1, ActionKey.RIGHT_CLICK};
    }

    @Override
    public boolean canUse(@NonNull ActionKey actionKey) {
        return super.canUse(actionKey) && isDurationFinished() && !combatUser.getSkill(VellionA3Info.getInstance()).getConfirmModule().isChecking()
                && combatUser.getSkill(VellionUltInfo.getInstance()).isDurationFinished();
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        setDuration();
        combatUser.setGlobalCooldown(VellionA1Info.GLOBAL_COOLDOWN);
        combatUser.getMoveModule().getSpeedStatus().addModifier(MODIFIER);

        VellionA1Info.SOUND.USE.play(combatUser.getLocation());

        addActionTask(new IntervalTask(this::playUseTickEffect, () -> {
            cancel();

            Location loc = combatUser.getArmLocation(MainHand.RIGHT);
            entityModule.set(new VellionA1Entity(loc));

            VellionA1Info.SOUND.USE_READY.play(loc);
        }, 1, VellionA1Info.READY_DURATION.toTicks()));
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
     * @param i 인덱스
     */
    private void playUseTickEffect(long i) {
        Location loc = LocationUtil.getLocationFromOffset(combatUser.getArmLocation(MainHand.RIGHT), 0, 0, 1.5);
        Vector vector = VectorUtil.getYawAxis(loc);
        Vector axis = VectorUtil.getRollAxis(loc);

        for (int j = 0; j < i; j++) {
            int angle = j * 8;

            for (int k = 0; k < 12; k++) {
                angle += 60;
                Vector vec = VectorUtil.getRotatedVector(vector, axis, k < 6 ? angle : -angle).multiply(0.8 + j * 0.25);
                Location loc2 = loc.clone().add(vec);

                if (i != 9)
                    VellionA1Info.PARTICLE.USE_TICK_1.play(loc2, i / 8.0);
                else
                    VellionA1Info.PARTICLE.USE_TICK_2.play(loc2);
            }
        }
    }

    /**
     * 회복 상태 효과 클래스.
     */
    private static final class VellionA1Heal extends StatusEffect {
        private static final VellionA1Heal instance = new VellionA1Heal();

        private VellionA1Heal() {
            super(true);
        }

        @Override
        public void onStart(@NonNull Damageable combatEntity, @NonNull CombatEntity provider) {
            // 미사용
        }

        @Override
        public void onTick(@NonNull Damageable combatEntity, @NonNull CombatEntity provider, long i) {
            if (combatEntity instanceof Healable && provider instanceof Healer)
                ((Healable) combatEntity).getDamageModule().heal((Healer) provider, VellionA1Info.HEAL_PER_SECOND / 20.0, true);
        }

        @Override
        public void onEnd(@NonNull Damageable combatEntity, @NonNull CombatEntity provider) {
            // 미사용
        }
    }

    /**
     * 독 상태 효과 클래스.
     */
    private static final class VellionA1Poison extends Poison {
        private static final VellionA1Poison instance = new VellionA1Poison();

        private VellionA1Poison() {
            super(VellionA1Info.POISON_DAMAGE_PER_SECOND, true);
        }
    }

    /**
     * 마력 응집체 클래스.
     */
    public final class VellionA1Entity extends SummonEntity<ArmorStand> {
        /** 피격자 목록 */
        private final HashSet<Damageable> targets = new HashSet<>();
        /** 회수 시간 */
        private long returnTime = VellionA1Info.RETURN_DURATION.toTicks();

        private VellionA1Entity(@NonNull Location spawnLocation) {
            super(
                    ArmorStand.class,
                    spawnLocation,
                    combatUser.getName() + "의 마력 응집체",
                    combatUser,
                    false, true,
                    Hitbox.builder(1, 1, 1).offsetY(0.5).build()
            );

            entity.setGravity(false);
            addOnTick(this::onTick);
        }

        private void onTick(long i) {
            if (returnTime-- == 0)
                targets.clear();

            Location location = combatUser.getEntity().getEyeLocation().subtract(0, 0.4, 0);
            Vector dir = getLocation().getDirection();
            if (returnTime < 0)
                dir = LocationUtil.getDirection(getLocation(), location);

            Location loc = getLocation().add(dir.multiply(VellionA1Info.VELOCITY / 20));
            if (LocationUtil.isNonSolid(loc)) {
                entity.teleport(loc);
                if (returnTime < 0 && (loc.distance(location) < 2 || combatUser.isDead()))
                    remove();
            } else if (returnTime < -1)
                remove();

            VellionA1Info.PARTICLE.DISPLAY.play(getLocation());

            new VellionA1Area().emit(loc);
        }

        private final class VellionA1Area extends Area<Damageable> {
            private VellionA1Area() {
                super(combatUser, VellionA1Info.RADIUS, CombatUtil.EntityCondition.of(Damageable.class).and(Damageable::isCreature).exclude(combatUser));
            }

            @Override
            protected boolean onHitBlock(@NonNull Location center, @NonNull Location location, @NonNull Block hitBlock) {
                return false;
            }

            @Override
            protected boolean onHitEntity(@NonNull Location center, @NonNull Location location, @NonNull Damageable target) {
                if (targets.add(target)) {
                    if (target.isEnemy(combatUser)) {
                        if (target.getDamageModule().damage(combatUser, 0, DamageType.NORMAL, null,
                                false, true)) {
                            target.getStatusEffectModule().apply(VellionA1Poison.instance, combatUser,
                                    target.getStatusEffectModule().getDuration(VellionA1Poison.instance).plus(VellionA1Info.EFFECT_DURATION));

                            target.getStatusEffectModule().apply(Snare.getInstance(), combatUser, VellionA1Info.SNARE_DURATION);
                        }

                        VellionA1Info.PARTICLE.HIT_ENTITY.play(location);
                        VellionA1Info.SOUND.HIT_ENTITY.play(location);
                    } else if (target instanceof Healable)
                        target.getStatusEffectModule().apply(VellionA1Heal.instance, combatUser,
                                target.getStatusEffectModule().getDuration(VellionA1Heal.instance).plus(VellionA1Info.EFFECT_DURATION));

                    if (target instanceof CombatUser)
                        combatUser.addScore("마력 집중", VellionA1Info.EFFECT_SCORE);
                }

                return true;
            }
        }
    }
}
