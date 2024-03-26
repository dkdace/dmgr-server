package com.dace.dmgr.combat.character.silia.action;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.ChargeableSkill;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.util.SoundUtil;
import com.dace.dmgr.util.task.IntervalTask;
import com.dace.dmgr.util.task.TaskUtil;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.Sound;

@Getter
public final class SiliaA3 extends ChargeableSkill {
    public SiliaA3(@NonNull CombatUser combatUser) {
        super(3, combatUser, SiliaA3Info.getInstance(), 2);
    }

    @Override
    @NonNull
    public ActionKey @NonNull [] getDefaultActionKeys() {
        return new ActionKey[]{ActionKey.SLOT_3};
    }

    @Override
    public long getDefaultCooldown() {
        return SiliaA3Info.COOLDOWN;
    }

    @Override
    public int getMaxStateValue() {
        return (int) SiliaA3Info.DURATION;
    }

    @Override
    public long getStateValueDecrement() {
        return 1;
    }

    @Override
    public long getStateValueIncrement() {
        return 1;
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        if (isDurationFinished()) {
            setDuration();
            combatUser.getMoveModule().getSpeedStatus().addModifier("SiliaA3", SiliaA3Info.SPEED);
            playUseSound(combatUser.getEntity().getLocation());

            int health = combatUser.getDamageModule().getHealth();
            TaskUtil.addTask(taskRunner, new IntervalTask(i -> {
                if (getStateValue() <= 0 || health - combatUser.getDamageModule().getHealth() >=
                        combatUser.getDamageModule().getMaxHealth() * SiliaA3Info.CANCEL_DAMAGE_RATIO)
                    return false;

                combatUser.getEntity().setFallDistance(0);

                if (i >= SiliaA3Info.ACTIVATE_DURATION && !((SiliaWeapon) combatUser.getWeapon()).isStrike) {
                    ((SiliaWeapon) combatUser.getWeapon()).isStrike = true;
                    combatUser.getWeapon().setGlowing(true);
                    combatUser.getWeapon().displayDurability(SiliaWeaponInfo.RESOURCE.EXTENDED);
                    SoundUtil.play("new.item.trident.return", combatUser.getEntity(), 1, 1.2);
                }

                return true;
            }, isCancelled -> {
                onCancelled();
                if (getStateValue() > 0)
                    setCooldown(SiliaA3Info.COOLDOWN_FORCE);
            }, 1));
        } else
            onCancelled();
    }

    @Override
    public void onCancelled() {
        super.onCancelled();
        setCooldown();
        ((SiliaWeapon) combatUser.getWeapon()).isStrike = false;
        combatUser.getWeapon().setGlowing(false);
        combatUser.getWeapon().displayDurability(SiliaWeaponInfo.RESOURCE.DEFAULT);
        combatUser.getMoveModule().getSpeedStatus().removeModifier("SiliaA3");
        playDisableSound(combatUser.getEntity().getLocation());
    }

    /**
     * 사용 시 효과음을 재생한다.
     *
     * @param location 사용 위치
     */
    private void playUseSound(Location location) {
        SoundUtil.play(Sound.ENTITY_LLAMA_SWAG, location, 0.2, 1);
        SoundUtil.play(Sound.BLOCK_LAVA_EXTINGUISH, location, 0.15, 1.5);
    }

    /**
     * 비활성화 시 효과음을 재생한다.
     *
     * @param location 사용 위치
     */
    private void playDisableSound(Location location) {
        SoundUtil.play(Sound.ENTITY_LLAMA_SWAG, location, 0.2, 1.2);
        SoundUtil.play(Sound.BLOCK_LAVA_EXTINGUISH, location, 0.15, 1.7);
    }
}
