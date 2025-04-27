package com.dace.dmgr.combat.combatant.palas;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.ActiveSkill;
import com.dace.dmgr.combat.action.skill.HasBonusScore;
import com.dace.dmgr.combat.action.skill.module.BonusScoreModule;
import com.dace.dmgr.combat.action.weapon.Weapon;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.DamageType;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.EntityCondition;
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

    public PalasA1(@NonNull CombatUser combatUser) {
        super(combatUser, PalasA1Info.getInstance(), PalasA1Info.COOLDOWN, Timespan.MAX, 0);

        this.bonusScoreModule = new BonusScoreModule(this, "처치 지원", PalasA1Info.ASSIST_SCORE);
        this.stun = new PalasA1Stun();
    }

    @Override
    @NonNull
    public ActionKey @NonNull [] getDefaultActionKeys() {
        return new ActionKey[]{ActionKey.SLOT_1};
    }

    @Override
    public boolean canUse(@NonNull ActionKey actionKey) {
        return super.canUse(actionKey) && isDurationFinished();
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        setDuration();
        combatUser.setGlobalCooldown(PalasA1Info.GLOBAL_COOLDOWN);

        Weapon weapon = combatUser.getWeapon();
        weapon.cancel();
        weapon.setVisible(false);

        PalasA1Info.Sounds.USE.play(combatUser.getLocation());

        addActionTask(new DelayTask(() -> {
            cancel();

            Location loc = combatUser.getArmLocation(MainHand.RIGHT);
            new PalasA1Projectile().shot(loc);

            PalasA1Info.Sounds.USE_READY.play(loc);
        }, PalasA1Info.READY_DURATION.toTicks()));
    }

    @Override
    public boolean isCancellable() {
        return !isDurationFinished();
    }

    @Override
    protected void onCancelled() {
        setDuration(Timespan.ZERO);
        combatUser.getWeapon().setVisible(true);
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
                CombatUtil.sendShake((CombatUser) combatEntity, 20, 20);

            if (i % 2 == 0) {
                PalasA1Info.Particles.TICK.play(combatEntity.getCenterLocation());
                PalasA1Info.Sounds.TICK.play(combatEntity.getLocation());
            }
        }
    }

    private final class PalasA1Projectile extends Projectile<Damageable> {
        private PalasA1Projectile() {
            super(PalasA1.this, PalasA1Info.VELOCITY, EntityCondition.enemy(combatUser));
        }

        @Override
        @NonNull
        protected IntervalHandler getIntervalHandler() {
            return createPeriodIntervalHandler(8, PalasA1Info.Particles.BULLET_TRAIL::play);
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

                        PalasA1Info.Particles.HIT_ENTITY.play(target.getCenterLocation(), target.getWidth(), target.getHeight());
                    }

                    PalasA1Info.Sounds.HIT_ENTITY.play(location);

                    if (target.isGoalTarget()) {
                        combatUser.addScore("적 기절시킴", PalasA1Info.DAMAGE_SCORE);
                        bonusScoreModule.addTarget(target, PalasA1Info.STUN_DURATION);
                    }
                }

                return false;
            };
        }
    }
}
