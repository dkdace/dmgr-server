package com.dace.dmgr.combat.combatant.vellion;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.ability.ActionKey;
import com.dace.dmgr.combat.ability.handler.LeftClickHandler;
import com.dace.dmgr.combat.ability.handler.SlotHandler;
import com.dace.dmgr.combat.ability.skill.ActiveSkill;
import com.dace.dmgr.combat.ability.skill.Confirmable;
import com.dace.dmgr.combat.ability.skill.HasBonusScore;
import com.dace.dmgr.combat.ability.skill.module.BonusScoreModule;
import com.dace.dmgr.combat.ability.skill.module.LocationConfirmModule;
import com.dace.dmgr.combat.entity.DamageType;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.EntityCondition;
import com.dace.dmgr.combat.entity.combatuser.CombatScore;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.combat.entity.module.Modifier;
import com.dace.dmgr.combat.entity.module.statuseffect.HealBlock;
import com.dace.dmgr.combat.entity.module.statuseffect.Silence;
import com.dace.dmgr.combat.interaction.Area;
import com.dace.dmgr.util.task.IntervalTask;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.inventory.MainHand;

public final class VellionA3 extends ActiveSkill implements HasBonusScore, Confirmable, SlotHandler, LeftClickHandler {
    /** 수정자 */
    private static final Modifier MODIFIER = new Modifier(-VellionA3Info.READY_SLOW);

    /** 위치 확인 모듈 */
    @NonNull
    @Getter
    private final LocationConfirmModule confirmModule;
    /** 보너스 점수 모듈 */
    @NonNull
    @Getter
    private final BonusScoreModule bonusScoreModule;
    /** 침묵 상태 효과 */
    private final Silence silence;

    public VellionA3(@NonNull CombatUser combatUser, @NonNull VellionA3Info skillInfo) {
        super(combatUser, skillInfo, VellionA3Info.COOLDOWN, Timespan.MAX);

        this.confirmModule = new LocationConfirmModule(this, VellionA3Info.MAX_DISTANCE);
        this.bonusScoreModule = new BonusScoreModule(this);
        this.silence = new Silence(combatUser);
    }

    @Override
    public int getPriority() {
        return 1;
    }

    @Override
    protected boolean canUse() {
        return super.canUse() && isDurationFinished() && combatUser.getAbilityManager().getAbility(VellionUltInfo.getInstance()).isDurationFinished();
    }

    @Override
    @NonNull
    public ActionKey.Slot getSlot() {
        return ActionKey.Slot.SLOT_3;
    }

    @Override
    public void onSlot() {
        confirmModule.toggleCheck();
    }

    @Override
    public void onLeftClick() {
        confirmModule.accept();
    }

    @Override
    public void onAccept() {
        setDuration();

        combatUser.setGlobalCooldown(VellionA3Info.READY_DURATION);
        combatUser.getMoveModule().addModifier(MODIFIER);

        Location location = confirmModule.getCurrentLocation();

        VellionA3Info.Effects.USE.play(combatUser.getLocation());

        addActionTask(new IntervalTask(i -> VellionA3Info.Effects.playUseTick(combatUser.getArmLocation(MainHand.RIGHT), location), () -> {
            cancel();

            Location loc = location.clone().add(0, 0.1, 0);

            VellionA3Info.Effects.USE_READY.play(loc);

            addTask(new IntervalTask(i -> {
                if (i % 4 == 0)
                    new VellionA3Area().emit(loc);

                VellionA3Info.Effects.playTick(i, loc);
            }, 1, VellionA3Info.DURATION.toTicks()));
        }, 1, VellionA3Info.READY_DURATION.toTicks()));
    }

    @Override
    public boolean isCancellable() {
        return confirmModule.isChecking() || !isDurationFinished();
    }

    @Override
    protected void onCancelled() {
        confirmModule.cancel();

        if (!isDurationFinished()) {
            setDuration(Timespan.ZERO);
            combatUser.getMoveModule().removeModifier(MODIFIER);
        }
    }

    @Override
    @NonNull
    public CombatScore getBonusCombatScore() {
        return VellionA3Info.ASSIST_SCORE;
    }

    @Override
    public boolean isAssistMode() {
        return true;
    }

    private final class VellionA3Area extends Area<Damageable> {
        private VellionA3Area() {
            super(combatUser, VellionA3Info.RADIUS, EntityCondition.enemy(combatUser));
        }

        @Override
        protected boolean onHitBlock(@NonNull Location center, @NonNull Location location, @NonNull Block hitBlock) {
            return false;
        }

        @Override
        protected boolean onHitEntity(@NonNull Location center, @NonNull Location location, @NonNull Damageable target) {
            if (target.getDamageModule().damage(combatUser, 0, DamageType.NORMAL, null, false, true)) {
                target.getStatusEffectModule().apply(HealBlock.getInstance(), Timespan.ofTicks(10));
                target.getStatusEffectModule().apply(silence, Timespan.ofTicks(10));

                if (target.isGoalTarget()) {
                    combatUser.addScore(VellionA3Info.EFFECT_SCORE_PER_SECOND.multiplyScore(4.0 / 20));
                    bonusScoreModule.addTarget(target, Timespan.ofTicks(10));
                }
            }

            return true;
        }
    }
}
