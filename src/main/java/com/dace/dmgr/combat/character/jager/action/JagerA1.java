package com.dace.dmgr.combat.character.jager.action;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.ChargeableSkill;
import com.dace.dmgr.combat.action.skill.HasEntity;
import com.dace.dmgr.combat.action.skill.LocationConfirmable;
import com.dace.dmgr.combat.action.skill.module.LocationConfirmModule;
import com.dace.dmgr.combat.entity.CombatEntityUtil;
import com.dace.dmgr.combat.entity.CombatUser;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Wolf;

@Getter
@Setter
public final class JagerA1 extends ChargeableSkill implements HasEntity<JagerA1Entity>, LocationConfirmable {
    /** 위치 확인 모듈 */
    private final LocationConfirmModule confirmModule;
    /** 소환된 엔티티 */
    private JagerA1Entity summonEntity = null;

    public JagerA1(CombatUser combatUser) {
        super(1, combatUser, JagerA1Info.getInstance(), 0);
        confirmModule = new LocationConfirmModule(this, ActionKey.LEFT_CLICK, ActionKey.SLOT_1, JagerA1Info.SUMMON_MAX_DISTANCE);
    }

    @Override
    public ActionKey[] getDefaultActionKeys() {
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
    public void reset() {
        super.reset();
        HasEntity.super.reset();
        LocationConfirmable.super.reset();
    }

    @Override
    public void remove() {
        HasEntity.super.remove();
        LocationConfirmable.super.remove();
    }

    @Override
    public void onUse(ActionKey actionKey) {
        if (((JagerWeaponL) combatUser.getWeapon()).getAimModule().isAiming()) {
            ((JagerWeaponL) combatUser.getWeapon()).toggleAim();
            ((JagerWeaponL) combatUser.getWeapon()).swap();
        }

        if (isDurationFinished())
            toggleCheck();
        else {
            setDuration(0);
            removeSummonEntity();
        }
    }

    @Override
    public void toggleCheck() {
        confirmModule.toggleCheck();
    }

    @Override
    public void onAccept() {
        combatUser.getWeapon().setCooldown(2);
        setDuration();
        toggleCheck();

        Wolf wolf = CombatEntityUtil.spawn(Wolf.class, confirmModule.getCurrentLocation());
        JagerA1Entity jagerA1Entity = new JagerA1Entity(wolf, combatUser);
        jagerA1Entity.init();
        setSummonEntity(jagerA1Entity);
    }
}
