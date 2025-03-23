package com.dace.dmgr.combat.combatant.palas;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.ActiveSkill;
import com.dace.dmgr.combat.action.skill.HasBonusScore;
import com.dace.dmgr.combat.action.skill.module.BonusScoreModule;
import com.dace.dmgr.combat.action.weapon.Weapon;
import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.DamageType;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.module.statuseffect.Stun;
import com.dace.dmgr.combat.interaction.Projectile;
import com.dace.dmgr.util.task.DelayTask;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.inventory.MainHand;

@Getter
public final class PalasA1 extends ActiveSkill implements HasBonusScore {
    /** 보너스 점수 모듈 */
    @NonNull
    private final BonusScoreModule bonusScoreModule;

    public PalasA1(@NonNull CombatUser combatUser) {
        super(combatUser, PalasA1Info.getInstance(), PalasA1Info.COOLDOWN, Timespan.MAX, 0);
        this.bonusScoreModule = new BonusScoreModule(this, "처치 지원", PalasA1Info.ASSIST_SCORE);
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

        PalasA1Info.SOUND.USE.play(combatUser.getLocation());

        addActionTask(new DelayTask(() -> {
            cancel();

            Location loc = combatUser.getArmLocation(MainHand.RIGHT);
            new PalasA1Projectile().shot(loc);

            PalasA1Info.SOUND.USE_READY.play(loc);
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
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class PalasA1Stun extends Stun {
        private static final PalasA1Stun instance = new PalasA1Stun();

        @Override
        public void onTick(@NonNull Damageable combatEntity, @NonNull CombatEntity provider, long i) {
            super.onTick(combatEntity, provider, i);

            if (combatEntity instanceof CombatUser)
                CombatUtil.sendShake((CombatUser) combatEntity, 20, 20);

            if (i % 2 == 0) {
                PalasA1Info.PARTICLE.TICK.play(combatEntity.getCenterLocation());
                PalasA1Info.SOUND.TICK.play(combatEntity.getLocation());
            }
        }
    }

    private final class PalasA1Projectile extends Projectile<Damageable> {
        private PalasA1Projectile() {
            super(combatUser, PalasA1Info.VELOCITY, CombatUtil.EntityCondition.enemy(combatUser));
        }

        @Override
        @NonNull
        protected IntervalHandler getIntervalHandler() {
            return createPeriodIntervalHandler(8, PalasA1Info.PARTICLE.BULLET_TRAIL::play);
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
                        target.getStatusEffectModule().apply(PalasA1Stun.instance, combatUser, PalasA1Info.STUN_DURATION);

                        PalasA1Info.PARTICLE.HIT_ENTITY.play(target.getCenterLocation(), target.getWidth(), target.getHeight());
                    }

                    PalasA1Info.SOUND.HIT_ENTITY.play(location);

                    if (target instanceof CombatUser) {
                        combatUser.addScore("적 기절시킴", PalasA1Info.DAMAGE_SCORE);
                        bonusScoreModule.addTarget((CombatUser) target, PalasA1Info.STUN_DURATION);
                    }
                }

                return false;
            };
        }
    }
}
