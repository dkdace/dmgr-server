package com.dace.dmgr.combat.combatant.palas;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.ability.ActionKey;
import com.dace.dmgr.combat.ability.skill.ActiveSkill;
import com.dace.dmgr.combat.ability.skill.HasBonusScore;
import com.dace.dmgr.combat.ability.skill.module.BonusScoreModule;
import com.dace.dmgr.combat.ability.weapon.Weapon;
import com.dace.dmgr.combat.entity.DamageType;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.EntityCondition;
import com.dace.dmgr.combat.entity.combatuser.CombatScore;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.combat.entity.module.statuseffect.Stun;
import com.dace.dmgr.combat.interaction.Projectile;
import com.dace.dmgr.util.task.DelayTask;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.inventory.MainHand;

public final class PalasA1 extends ActiveSkill implements HasBonusScore {
    /** 보너스 점수 모듈 */
    @NonNull
    @Getter
    private final BonusScoreModule bonusScoreModule;
    /** 기절 상태 효과 */
    private final PalasA1Stun stun;

    public PalasA1(@NonNull CombatUser combatUser, @NonNull PalasA1Info skillInfo) {
        super(combatUser, skillInfo, PalasA1Info.COOLDOWN, Timespan.MAX);

        this.bonusScoreModule = new BonusScoreModule(this);
        this.stun = new PalasA1Stun();
    }

    @Override
    protected boolean canUse() {
        return super.canUse() && isDurationFinished();
    }

    @Override
    @NonNull
    public ActionKey.Slot getSlot() {
        return ActionKey.Slot.SLOT_1;
    }

    @Override
    public void onSlot() {
        setDuration();
        combatUser.setGlobalCooldown(PalasA1Info.GLOBAL_COOLDOWN);

        Weapon weapon = combatUser.getAbilityManager().getWeapon();
        weapon.cancel();
        weapon.setVisible(false);

        PalasA1Info.Effects.USE.play(combatUser.getLocation());

        addActionTask(new DelayTask(() -> {
            cancel();

            Location loc = combatUser.getArmLocation(MainHand.RIGHT);
            new PalasA1Projectile().shot(loc);

            PalasA1Info.Effects.USE_READY.play(loc);
        }, PalasA1Info.READY_DURATION.toTicks()));
    }

    @Override
    public boolean isCancellable() {
        return !isDurationFinished();
    }

    @Override
    protected void onCancelled() {
        setDuration(Timespan.ZERO);
        combatUser.getAbilityManager().getWeapon().setVisible(true);
    }

    @Override
    @NonNull
    public CombatScore getBonusCombatScore() {
        return PalasA1Info.ASSIST_SCORE;
    }

    @Override
    public boolean isAssistMode() {
        return true;
    }

    /**
     * 기절 상태 효과 클래스.
     */
    private final class PalasA1Stun extends Stun {
        private PalasA1Stun() {
            super(combatUser);
        }

        @Override
        public void onTick(@NonNull Damageable combatEntity, long i) {
            super.onTick(combatEntity, i);

            if (combatEntity instanceof CombatUser)
                PalasA1Info.SHAKE.send((CombatUser) combatEntity);

            if (i % 2 == 0)
                PalasA1Info.Effects.playStunTick(combatEntity.getLocation(), combatEntity.getCenterLocation());
        }
    }

    private final class PalasA1Projectile extends Projectile<Damageable> {
        private PalasA1Projectile() {
            super(PalasA1.this, PalasA1Info.VELOCITY, EntityCondition.enemy(combatUser));
        }

        @Override
        @NonNull
        protected IntervalHandler getIntervalHandler() {
            return createPeriodIntervalHandler(8, PalasA1Info.Effects.BULLET_TRAIL::play);
        }

        @Override
        @NonNull
        protected HitBlockHandler getHitBlockHandler() {
            return (location, hitBlock) -> false;
        }

        @Override
        @NonNull
        protected HitEntityHandler<Damageable> getHitEntityHandler() {
            return (location, target) -> {
                if (target.getDamageModule().damage(this, PalasA1Info.DAMAGE, DamageType.NORMAL, location, false, true)) {
                    if (target.isCreature()) {
                        target.getStatusEffectModule().apply(stun, PalasA1Info.STUN_DURATION);

                        PalasA1Info.Effects.HIT_ENTITY_PARTICLE.apply(target).play(target.getCenterLocation());
                    }

                    PalasA1Info.Effects.HIT_ENTITY_SOUND.play(location);

                    if (target.isGoalTarget()) {
                        combatUser.addScore(PalasA1Info.DAMAGE_SCORE);
                        bonusScoreModule.addTarget(target, PalasA1Info.STUN_DURATION);
                    }
                }

                return false;
            };
        }
    }
}
