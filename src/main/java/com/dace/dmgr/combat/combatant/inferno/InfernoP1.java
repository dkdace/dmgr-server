package com.dace.dmgr.combat.combatant.inferno;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.ability.ActionBarDisplay;
import com.dace.dmgr.combat.ability.ActionKey;
import com.dace.dmgr.combat.ability.skill.PassiveSkill;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.EntityCondition;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.combat.entity.module.Modifier;
import com.dace.dmgr.combat.entity.module.statuseffect.Burning;
import com.dace.dmgr.combat.interaction.Area;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.Set;

public final class InfernoP1 extends PassiveSkill {
    /** 수정자 */
    private static final Modifier MODIFIER = new Modifier(InfernoP1Info.DEFENSE_INCREMENT);
    /** 활성화 가능 여부 */
    private boolean canActivate = false;

    public InfernoP1(@NonNull CombatUser combatUser, @NonNull InfernoP1Info skillInfo) {
        super(combatUser, skillInfo, Timespan.ZERO, InfernoP1Info.DURATION);
    }

    @Override
    @NonNull
    public Set<@NonNull ActionKey> getDefaultActionKeys() {
        return EnumSet.of(ActionKey.PERIODIC_1);
    }

    @Override
    @Nullable
    public ActionBarDisplay getActionBarDisplay() {
        if (isDurationFinished())
            return null;

        return ActionBarDisplay.builder(this).title().durationBar().build();
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
        new InfernoP1Area().emit(combatUser.getLocation().add(0, 0.1, 0));

        return canActivate;
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        setDuration();
        combatUser.getDamageModule().addModifier(MODIFIER);
    }

    @Override
    protected void onDurationFinished() {
        super.onDurationFinished();
        combatUser.getDamageModule().removeModifier(MODIFIER);
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
            combatUser.getAbilityManager().useAction(ActionKey.PERIODIC_1);
    }

    private final class InfernoP1Area extends Area<Damageable> {
        private InfernoP1Area() {
            super(combatUser, InfernoP1Info.DETECT_RADIUS, EntityCondition.enemy(combatUser)
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
