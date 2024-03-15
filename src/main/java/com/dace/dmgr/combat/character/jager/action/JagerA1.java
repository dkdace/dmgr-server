package com.dace.dmgr.combat.character.jager.action;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.ChargeableSkill;
import com.dace.dmgr.combat.action.skill.Confirmable;
import com.dace.dmgr.combat.action.skill.module.LocationConfirmModule;
import com.dace.dmgr.combat.entity.CombatEntityUtil;
import com.dace.dmgr.combat.entity.CombatUser;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.entity.Wolf;

@Getter
public final class JagerA1 extends ChargeableSkill implements Confirmable {
    /** 위치 확인 모듈 */
    @NonNull
    private final LocationConfirmModule confirmModule;
    /** 소환한 엔티티 */
    JagerA1Entity entity = null;

    public JagerA1(@NonNull CombatUser combatUser) {
        super(1, combatUser, JagerA1Info.getInstance(), 0);
        confirmModule = new LocationConfirmModule(this, ActionKey.LEFT_CLICK, ActionKey.SLOT_1, JagerA1Info.SUMMON_MAX_DISTANCE);
    }

    @Override
    @NonNull
    public ActionKey @NonNull [] getDefaultActionKeys() {
        return new ActionKey[]{ActionKey.SLOT_1};
    }

    @Override
    public long getDefaultCooldown() {
        return JagerA1Info.COOLDOWN;
    }

    @Override
    public int getMaxStateValue() {
        return JagerA1Info.HEALTH;
    }

    @Override
    public long getStateValueDecrement() {
        return 0;
    }

    @Override
    public long getStateValueIncrement() {
        return JagerA1Info.HEALTH / JagerA1Info.RECOVER_DURATION / 20;
    }

    @Override
    public boolean canUse() {
        return super.canUse() && combatUser.getSkill(JagerA3Info.getInstance()).isDurationFinished();
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        combatUser.getWeapon().onCancelled();

        if (isDurationFinished())
            confirmModule.toggleCheck();
        else {
            setDuration(0);
            if (entity != null)
                entity.dispose();
        }
    }

    @Override
    public void onCancelled() {
        super.onCancelled();
        if (confirmModule.isChecking())
            confirmModule.toggleCheck();
    }

    @Override
    public void onCheckEnable() {
        // 미사용
    }

    @Override
    public void onCheckTick(long i) {
        // 미사용
    }

    @Override
    public void onCheckDisable() {
        // 미사용
    }

    @Override
    public void onAccept() {
        if (!confirmModule.isValid())
            return;

        combatUser.getWeapon().setCooldown(2);
        setDuration();
        confirmModule.toggleCheck();

        Wolf wolf = CombatEntityUtil.spawn(Wolf.class, confirmModule.getCurrentLocation());
        entity = new JagerA1Entity(wolf, combatUser);
        entity.activate();
    }

    @Override
    public void reset() {
        super.reset();

        if (entity != null)
            entity.dispose();
    }
}
