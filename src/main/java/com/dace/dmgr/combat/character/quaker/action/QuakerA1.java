package com.dace.dmgr.combat.character.quaker.action;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.ChargeableSkill;
import com.dace.dmgr.combat.entity.CombatEntityUtil;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.util.NamedSound;
import com.dace.dmgr.util.SoundUtil;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.entity.ArmorStand;

@Getter
public final class QuakerA1 extends ChargeableSkill {
    /** 소환한 엔티티 */
    QuakerA1Entity entity = null;

    public QuakerA1(@NonNull CombatUser combatUser) {
        super(1, combatUser, QuakerA1Info.getInstance(), 0);
    }

    @Override
    @NonNull
    public ActionKey @NonNull [] getDefaultActionKeys() {
        return new ActionKey[]{ActionKey.SLOT_1, ActionKey.RIGHT_CLICK};
    }

    @Override
    public long getDefaultCooldown() {
        return QuakerA1Info.COOLDOWN;
    }

    @Override
    public int getMaxStateValue() {
        return QuakerA1Info.HEALTH;
    }

    @Override
    public long getStateValueDecrement() {
        return 0;
    }

    @Override
    public long getStateValueIncrement() {
        return QuakerA1Info.HEALTH / QuakerA1Info.RECOVER_DURATION / 20;
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        combatUser.getWeapon().onCancelled();

        if (isDurationFinished()) {
            combatUser.setGlobalCooldown(8);
            setDuration();
            combatUser.getMoveModule().getSpeedStatus().addModifier("QuakerA1", -QuakerA1Info.USE_SPEED);
            combatUser.setFovValue(0.3);
            SoundUtil.playNamedSound(NamedSound.COMBAT_QUAKER_A1_USE, combatUser.getEntity().getLocation());

            ArmorStand armorStand = CombatEntityUtil.spawn(ArmorStand.class, combatUser.getEntity().getLocation());
            entity = new QuakerA1Entity(armorStand, combatUser);
            entity.activate();
        } else
            onCancelled();
    }

    @Override
    public void onCancelled() {
        super.onCancelled();
        setDuration(0);
        combatUser.getMoveModule().getSpeedStatus().removeModifier("QuakerA1");
        combatUser.setFovValue(0);
        SoundUtil.playNamedSound(NamedSound.COMBAT_QUAKER_A1_DISABLE, combatUser.getEntity().getLocation());

        if (entity != null)
            entity.dispose();
    }

    @Override
    public void reset() {
        super.reset();

        if (entity != null)
            entity.dispose();
    }
}
