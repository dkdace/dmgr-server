package com.dace.dmgr.combat.character.magritta.action;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.AbstractSkill;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.module.statuseffect.StatusEffectType;
import com.dace.dmgr.combat.interaction.Area;
import com.dace.dmgr.util.task.IntervalTask;
import com.dace.dmgr.util.task.TaskUtil;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.block.Block;

public final class MagrittaP1 extends AbstractSkill {
    /** 활성화 가능 여부 */
    private boolean canActivate = false;

    public MagrittaP1(@NonNull CombatUser combatUser) {
        super(combatUser);
    }

    @Override
    @NonNull
    public ActionKey @NonNull [] getDefaultActionKeys() {
        return new ActionKey[]{ActionKey.PERIODIC_1};
    }

    @Override
    public long getDefaultCooldown() {
        return 0;
    }

    @Override
    public long getDefaultDuration() {
        return MagrittaP1Info.DURATION;
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
        new MagrittaP1Area().emit(combatUser.getEntity().getLocation().add(0, 0.1, 0));

        return canActivate;
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        if (isDurationFinished()) {
            setDuration();

            TaskUtil.addTask(taskRunner, new IntervalTask(i -> {
                combatUser.getDamageModule().heal(combatUser, MagrittaP1Info.HEAL_PER_SECOND * 2 / 20, false);
                return !isDurationFinished();
            }, 2));
        } else
            setDuration();
    }

    @Override
    public boolean isCancellable() {
        return false;
    }

    private final class MagrittaP1Area extends Area {
        private MagrittaP1Area() {
            super(combatUser, MagrittaP1Info.DETECT_RADIUS, combatEntity -> combatEntity instanceof Damageable
                    && ((Damageable) combatEntity).getStatusEffectModule().hasStatusEffectType(StatusEffectType.BURNING)
                    && (combatEntity.isEnemy(MagrittaP1.this.combatUser) || combatEntity == MagrittaP1.this.combatUser));
        }

        @Override
        protected boolean onHitBlock(@NonNull Location center, @NonNull Location location, @NonNull Block hitBlock) {
            return false;
        }

        @Override
        public boolean onHitEntity(@NonNull Location center, @NonNull Location location, @NonNull Damageable target) {
            canActivate = true;
            return true;
        }
    }
}
