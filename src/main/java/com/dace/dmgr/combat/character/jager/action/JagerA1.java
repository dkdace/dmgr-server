package com.dace.dmgr.combat.character.jager.action;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.ChargeableSkill;
import com.dace.dmgr.combat.action.skill.HasEntity;
import com.dace.dmgr.combat.action.skill.LocationConfirmModule;
import com.dace.dmgr.combat.action.skill.LocationConfirmable;
import com.dace.dmgr.combat.entity.CombatEntityUtil;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.SummonEntity;
import com.dace.dmgr.util.SoundUtil;
import lombok.Getter;
import org.bukkit.Sound;
import org.bukkit.entity.Wolf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class JagerA1 extends ChargeableSkill implements HasEntity, LocationConfirmable {
    /** 위치 확인 모듈 객체 */
    private final LocationConfirmModule locationConfirmModule;
    /** 소환된 엔티티 목록 */
    @Getter
    private final List<JagerA1Entity> summonEntities = new ArrayList<>();

    public JagerA1(CombatUser combatUser) {
        super(1, combatUser, JagerA1Info.getInstance(), 0);
        locationConfirmModule = new LocationConfirmModule(this, ActionKey.LEFT_CLICK, ActionKey.SLOT_1);
    }

    @Override
    public List<ActionKey> getDefaultActionKeys() {
        return Arrays.asList(ActionKey.SLOT_1);
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
    public int getStateValueDecrement() {
        return 0;
    }

    @Override
    public int getStateValueIncrement() {
        return JagerA1Info.HEALTH / JagerA1Info.RECOVER_DURATION;
    }

    @Override
    public boolean isConfirming() {
        return locationConfirmModule.isToggled();
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
    public void onUse(ActionKey actionKey) {
        if (((JagerWeaponL) combatUser.getWeapon()).isAiming()) {
            ((JagerWeaponL) combatUser.getWeapon()).aim();
            ((JagerWeaponL) combatUser.getWeapon()).swap();
        }

        if (isDurationFinished()) {
            locationConfirmModule.toggle();
        } else {
            setDuration(0);
            summonEntities.forEach(SummonEntity::remove);
            summonEntities.clear();
        }
    }

    @Override
    public void confirm() {
        if (!locationConfirmModule.isValid())
            return;

        SoundUtil.play(Sound.ENTITY_ZOMBIE_VILLAGER_CONVERTED, locationConfirmModule.getLocation(), 0.8F, 1F);
        combatUser.getWeapon().setCooldown(2);
        setDuration();
        locationConfirmModule.toggle();

        Wolf wolf = CombatEntityUtil.spawn(Wolf.class, locationConfirmModule.getLocation());
        JagerA1Entity jagerA1Entity = new JagerA1Entity(wolf, combatUser);
        jagerA1Entity.init();
        summonEntities.add(jagerA1Entity);
    }
}
