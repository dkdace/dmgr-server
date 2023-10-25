package com.dace.dmgr.combat.character.jager.action;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.ChargeableSkill;
import com.dace.dmgr.combat.action.skill.HasEntity;
import com.dace.dmgr.combat.action.skill.LocationConfirmable;
import com.dace.dmgr.combat.entity.CombatEntityUtil;
import com.dace.dmgr.combat.entity.CombatUser;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Wolf;

@Getter
@Setter
public final class JagerA1 extends ChargeableSkill implements HasEntity<JagerA1Entity>, LocationConfirmable {
    /** 소환된 엔티티 */
    private JagerA1Entity summonEntity = null;
    /** 확인 중 상태 */
    private boolean checking;
    /** 현재 지정 위치 */
    private Location currentLocation;
    /** 위치 표시용 갑옷 거치대 객체 */
    private ArmorStand pointer;

    public JagerA1(CombatUser combatUser) {
        super(1, combatUser, JagerA1Info.getInstance(), 0);
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
    public ActionKey getAcceptKey() {
        return ActionKey.LEFT_CLICK;
    }

    @Override
    public ActionKey getCancelKey() {
        return ActionKey.SLOT_1;
    }

    @Override
    public int getMaxDistance() {
        return JagerA1Info.SUMMON_MAX_DISTANCE;
    }

    @Override
    public boolean canUse() {
        return super.canUse() && combatUser.getSkill(JagerA3Info.getInstance()).isDurationFinished();
    }

    @Override
    public void onRemove() {
        HasEntity.super.onRemove();
        LocationConfirmable.super.onRemove();
    }

    @Override
    public void onReset() {
        HasEntity.super.onReset();
        LocationConfirmable.super.onReset();
    }

    @Override
    public void onUse(ActionKey actionKey) {
        if (((JagerWeaponL) combatUser.getWeapon()).isAiming()) {
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
    public void onAcceptLocationConfirmable() {
        combatUser.getWeapon().setCooldown(2);
        setDuration();
        toggleCheck();

        Wolf wolf = CombatEntityUtil.spawn(Wolf.class, getCurrentLocation());
        JagerA1Entity jagerA1Entity = new JagerA1Entity(wolf, combatUser);
        jagerA1Entity.init();
        setSummonEntity(jagerA1Entity);
    }
}
