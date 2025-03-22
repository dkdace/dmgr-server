package com.dace.dmgr.combat.combatant.magritta.action;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.action.ActionBarStringUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.AbstractSkill;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.module.statuseffect.StatusEffectType;
import com.dace.dmgr.combat.interaction.Area;
import com.dace.dmgr.util.task.IntervalTask;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.jetbrains.annotations.Nullable;

public final class MagrittaP1 extends AbstractSkill {
    /** 활성화 가능 여부 */
    private boolean canActivate = false;

    public MagrittaP1(@NonNull CombatUser combatUser) {
        super(combatUser, MagrittaP1Info.getInstance(), Timespan.ZERO, MagrittaP1Info.DURATION);
    }

    @Override
    @NonNull
    public ActionKey @NonNull [] getDefaultActionKeys() {
        return new ActionKey[]{ActionKey.PERIODIC_1};
    }

    @Override
    @Nullable
    public String getActionBarString() {
        return isDurationFinished() ? null : ActionBarStringUtil.getDurationBar(this);
    }

    @Override
    public boolean canUse(@NonNull ActionKey actionKey) {
        return super.canUse(actionKey) && canActivate();
    }

    /**
     * 스킬 활성화 조건을 확인한다.
     *
     * @return 활성화 조건
     */
    private boolean canActivate() {
        canActivate = false;
        new MagrittaP1Area().emit(combatUser.getLocation().add(0, 0.1, 0));

        return canActivate;
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        if (isDurationFinished()) {
            setDuration();

            addActionTask(new IntervalTask(i -> {
                combatUser.getDamageModule().heal(combatUser, MagrittaP1Info.HEAL_PER_SECOND * 2 / 20.0, false);

                return !isDurationFinished();
            }, 2));
        } else
            setDuration();
    }

    @Override
    public boolean isCancellable() {
        return combatUser.isDead();
    }

    @Override
    protected void onCancelled() {
        setDuration(Timespan.ZERO);
    }

    private final class MagrittaP1Area extends Area<Damageable> {
        private MagrittaP1Area() {
            super(combatUser, MagrittaP1Info.DETECT_RADIUS, CombatUtil.EntityCondition.enemy(combatUser)
                    .and(combatEntity -> combatEntity.getStatusEffectModule().hasType(StatusEffectType.BURNING)));
        }

        @Override
        protected boolean onHitBlock(@NonNull Location center, @NonNull Location location, @NonNull Block hitBlock) {
            return false;
        }

        @Override
        protected boolean onHitEntity(@NonNull Location center, @NonNull Location location, @NonNull Damageable target) {
            canActivate = true;
            return true;
        }
    }
}
