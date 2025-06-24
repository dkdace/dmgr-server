package com.dace.dmgr.combat.combatant.vellion;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.ability.ActionKey;
import com.dace.dmgr.combat.ability.skill.ActiveSkill;
import com.dace.dmgr.combat.ability.skill.Summonable;
import com.dace.dmgr.combat.ability.skill.module.EntityModule;
import com.dace.dmgr.combat.entity.DamageType;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.EntityCondition;
import com.dace.dmgr.combat.entity.Healable;
import com.dace.dmgr.combat.entity.combatuser.AbilityManager;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.combat.entity.module.Modifier;
import com.dace.dmgr.combat.entity.module.statuseffect.Poison;
import com.dace.dmgr.combat.entity.module.statuseffect.Snare;
import com.dace.dmgr.combat.entity.module.statuseffect.StatusEffect;
import com.dace.dmgr.combat.entity.temporary.SummonEntity;
import com.dace.dmgr.combat.entity.temporary.spawnhandler.ArmorStandSpawnHandler;
import com.dace.dmgr.combat.interaction.Area;
import com.dace.dmgr.util.location.LocationUtil;
import com.dace.dmgr.util.task.IntervalTask;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.inventory.MainHand;
import org.bukkit.util.Vector;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

public final class VellionA1 extends ActiveSkill implements Summonable<VellionA1.VellionA1Entity> {
    /** 수정자 */
    private static final Modifier MODIFIER = new Modifier(-VellionA1Info.READY_SLOW);

    /** 소환 엔티티 모듈 */
    @NonNull
    @Getter
    private final EntityModule<VellionA1Entity> entityModule;
    /** 회복 상태 효과 */
    private final VellionA1Heal heal;
    /** 독 상태 효과 */
    private final Poison poison;

    public VellionA1(@NonNull CombatUser combatUser, @NonNull VellionA1Info skillInfo) {
        super(combatUser, skillInfo, VellionA1Info.COOLDOWN, Timespan.MAX);

        this.entityModule = new EntityModule<>(this);
        this.heal = new VellionA1Heal();
        this.poison = new Poison(combatUser, VellionA1Info.POISON_DAMAGE_PER_SECOND, true);
    }

    @Override
    @NonNull
    public Set<@NonNull ActionKey> getDefaultActionKeys() {
        return EnumSet.of(ActionKey.SLOT_1, ActionKey.RIGHT_CLICK);
    }

    @Override
    public boolean canUse(@NonNull ActionKey actionKey) {
        AbilityManager abilityManager = combatUser.getAbilityManager();
        return super.canUse(actionKey) && isDurationFinished() && !abilityManager.getSkill(VellionA3Info.getInstance()).getConfirmModule().isChecking()
                && abilityManager.getSkill(VellionUltInfo.getInstance()).isDurationFinished();
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        setDuration();

        combatUser.setGlobalCooldown(VellionA1Info.GLOBAL_COOLDOWN);
        combatUser.getMoveModule().addModifier(MODIFIER);

        VellionA1Info.Effects.USE.play(combatUser.getLocation());

        addActionTask(new IntervalTask(i -> VellionA1Info.Effects.playUseTick(i, combatUser.getArmLocation(MainHand.RIGHT)), () -> {
            cancel();

            Location loc = combatUser.getArmLocation(MainHand.RIGHT);
            entityModule.set(new VellionA1Entity(loc));

            VellionA1Info.Effects.USE_READY.play(loc);
        }, 1, VellionA1Info.READY_DURATION.toTicks()));
    }

    @Override
    public boolean isCancellable() {
        return !isDurationFinished();
    }

    @Override
    protected void onCancelled() {
        setDuration(Timespan.ZERO);

        entityModule.removeEntity();

        combatUser.getMoveModule().removeModifier(MODIFIER);
    }

    /**
     * 회복 상태 효과 클래스.
     */
    private final class VellionA1Heal implements StatusEffect {
        @Override
        public boolean isPositive() {
            return true;
        }

        @Override
        public void onStart(@NonNull Damageable combatEntity) {
            // 미사용
        }

        @Override
        public void onTick(@NonNull Damageable combatEntity, long i) {
            if (combatUser.isRemoved()) {
                combatEntity.getStatusEffectModule().remove(this);
                return;
            }

            if (combatEntity instanceof Healable)
                ((Healable) combatEntity).getHealModule().heal(combatUser, VellionA1Info.HEAL_PER_SECOND / 20.0, true);
        }

        @Override
        public void onEnd(@NonNull Damageable combatEntity) {
            // 미사용
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
            super(ArmorStandSpawnHandler.getInstance(), spawnLocation, combatUser.getName() + "의 마력 응집체", combatUser, false);

            entity.setGravity(false);
            addOnTick(this::onTick);
        }

        private void onTick(long i) {
            if (returnTime-- == 0)
                targets.clear();

            Location location = combatUser.getEntity().getEyeLocation().subtract(0, 0.4, 0);
            Vector dir = returnTime < 0
                    ? LocationUtil.getDirection(getLocation(), location)
                    : getLocation().getDirection();

            Location loc = getLocation().add(dir.multiply(VellionA1Info.VELOCITY / 20));
            if (LocationUtil.isNonSolid(loc)) {
                entity.teleport(loc);

                if (returnTime < 0 && loc.distance(location) < 2)
                    remove();
            } else if (returnTime < -1)
                remove();

            new VellionA1Area().emit(loc);

            VellionA1Info.Effects.DISPLAY.play(getLocation());
        }

        private final class VellionA1Area extends Area<Damageable> {
            private VellionA1Area() {
                super(combatUser, VellionA1Info.RADIUS, EntityCondition.of(Damageable.class).and(Damageable::isCreature).exclude(combatUser));
            }

            @Override
            protected boolean onHitBlock(@NonNull Location center, @NonNull Location location, @NonNull Block hitBlock) {
                return false;
            }

            @Override
            protected boolean onHitEntity(@NonNull Location center, @NonNull Location location, @NonNull Damageable target) {
                if (targets.add(target)) {
                    if (target.isEnemy(combatUser))
                        onHitEnemy(location, target);
                    else if (target instanceof Healable)
                        target.getStatusEffectModule().apply(heal, target.getStatusEffectModule().getDuration(heal).plus(VellionA1Info.EFFECT_DURATION));

                    if (target.isGoalTarget())
                        combatUser.addScore("마력 집중", VellionA1Info.EFFECT_SCORE);
                }

                return true;
            }

            /**
             * 적이 맞았을 때 실행할 작업.
             *
             * @param location 맞은 위치
             * @param target   대상 엔티티
             */
            private void onHitEnemy(@NonNull Location location, @NonNull Damageable target) {
                if (target.getDamageModule().damage(combatUser, 0, DamageType.NORMAL, null, false, true)) {
                    target.getStatusEffectModule().apply(poison, target.getStatusEffectModule().getDuration(poison).plus(VellionA1Info.EFFECT_DURATION));
                    target.getStatusEffectModule().apply(Snare.getInstance(), VellionA1Info.SNARE_DURATION);
                }

                VellionA1Info.Effects.HIT_ENTITY.play(location);
            }
        }
    }
}
