package com.dace.dmgr.combat.character.delta.action;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.weapon.AbstractWeapon;
import com.dace.dmgr.combat.action.weapon.FullAuto;
import com.dace.dmgr.combat.action.weapon.module.FullAutoModule;
import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.interaction.DamageType;
import com.dace.dmgr.combat.interaction.Target;
import com.dace.dmgr.util.*;
import com.dace.dmgr.util.task.IntervalTask;
import com.dace.dmgr.util.task.TaskUtil;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Location;

import javax.annotation.Nullable;

public final class DeltaWeapon extends AbstractWeapon implements FullAuto {
    /** 대상 초기화 딜레이 쿨타임 ID */
    private static final String TARGET_RESET_DELAY_COOLDOWN_ID = "TargetResetDelay";
    /** 대상 위치 통과 불가 시 초기화 딜레이 쿨타임 ID */
    private static final String BLOCK_RESET_DELAY_COOLDOWN_ID = "BlockResetDelay";

    @NonNull
    @Getter
    private final FullAutoModule fullAutoModule;

    @Nullable
    private Damageable target = null;

    public DeltaWeapon(@NonNull CombatUser combatUser) {
        super(combatUser, DeltaWeaponInfo.getInstance());
        fullAutoModule = new FullAutoModule(this, ActionKey.RIGHT_CLICK, FireRate.RPM_1200);
    }

    @Override
    public @NonNull ActionKey @NonNull [] getDefaultActionKeys() {
        return new ActionKey[] {ActionKey.RIGHT_CLICK};
    }

    @Override
    public long getDefaultCooldown() {
        return 0;
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        switch (actionKey) {
            case RIGHT_CLICK:
                if (target == null) {
                    this.new DeltaTarget().shoot();
                    if (target == null)
                        return;
                }
                target.getDamageModule().damage(combatUser, DeltaWeaponInfo.DAMAGE_PER_SECOND / 20,
                        DamageType.NORMAL, target.getCenterLocation(), false, true);
                playParticles(target);
                SoundUtil.playNamedSound(NamedSound.COMBAT_DELTA_WEAPON_USE, combatUser.getEntity().getLocation());
            default:
                break;
        }
    }

    private void playParticles(@NonNull Damageable target) {
        Location location = combatUser.getArmLocation(true);
        for (Location loc : LocationUtil.getLine(location, target.getCenterLocation(), 0.8)) {
                ParticleUtil.playRGB(ParticleUtil.ColoredParticle.REDSTONE, loc, 1,
                        0, 0, 0,
                        0, 255, 0);
        }
    }

    private boolean canTarget(CombatEntity target) {
        return target instanceof Damageable && target != combatUser && target.isEnemy(combatUser);
    }

    private final class DeltaTarget extends Target {
        private DeltaTarget() {
            super(combatUser, DeltaWeaponInfo.DISTANCE, false, DeltaWeapon.this::canTarget);
        }

        @Override
        protected void onFindEntity(@NonNull Damageable target) {
            DeltaWeapon.this.target = target;
            TaskUtil.addTask(DeltaWeapon.this,
                    new IntervalTask(
                            i -> target.canBeTargeted()
                                    && !target.isDisposed()
                                    && CooldownUtil.getCooldown(combatUser, TARGET_RESET_DELAY_COOLDOWN_ID) > 0
                                    && CooldownUtil.getCooldown(combatUser, BLOCK_RESET_DELAY_COOLDOWN_ID) > 0
                                    && combatUser.getEntity().getEyeLocation().distance(target.getCenterLocation())
                                        <= DeltaWeaponInfo.DISTANCE,
                            isCancelled -> DeltaWeapon.this.target = null,
                            1
                    )
            );
        }
    }
}
