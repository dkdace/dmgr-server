package com.dace.dmgr.combat.combatant.magritta;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.ability.ActionBarDisplay;
import com.dace.dmgr.combat.ability.ActionKey;
import com.dace.dmgr.combat.ability.skill.PassiveSkill;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.EntityCondition;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.combat.entity.module.statuseffect.Burning;
import com.dace.dmgr.combat.interaction.Area;
import com.dace.dmgr.util.task.IntervalTask;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.jetbrains.annotations.Nullable;

public final class MagrittaP1 extends PassiveSkill {
    /** 활성화 가능 여부 */
    private boolean canActivate = false;

    public MagrittaP1(@NonNull CombatUser combatUser, @NonNull MagrittaP1Info skillInfo) {
        super(combatUser, skillInfo, Timespan.ZERO, MagrittaP1Info.DURATION);
    }

    @Override
    @Nullable
    public ActionBarDisplay getActionBarDisplay() {
        if (isDurationFinished())
            return null;

        return ActionBarDisplay.builder(this).title().durationBar().build();
    }

    @Override
    protected boolean canUse() {
        return super.canUse() && canActivate();
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
    protected void onUse() {
        if (isDurationFinished()) {
            setDuration();

            addActionTask(new IntervalTask(i -> {
                combatUser.getHealModule().heal(combatUser, MagrittaP1Info.HEAL_PER_SECOND * 2 / 20.0, false);

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

    /**
     * 매 틱마다 실행할 작업.
     *
     * @param i 인덱스
     */
    void onTick(long i) {
        if (i % 5 == 0)
            use(ActionKey.SYSTEM);
    }

    private final class MagrittaP1Area extends Area<Damageable> {
        private MagrittaP1Area() {
            super(combatUser, MagrittaP1Info.DETECT_RADIUS, EntityCondition.enemy(combatUser)
                    .and(combatEntity -> combatEntity.getStatusEffectModule().has(Burning.class)));
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
