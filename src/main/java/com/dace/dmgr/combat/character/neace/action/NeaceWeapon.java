package com.dace.dmgr.combat.character.neace.action;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.weapon.AbstractWeapon;
import com.dace.dmgr.combat.action.weapon.FullAuto;
import com.dace.dmgr.combat.action.weapon.module.FullAutoModule;
import com.dace.dmgr.combat.character.neace.Neace;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.Healable;
import com.dace.dmgr.combat.interaction.DamageType;
import com.dace.dmgr.combat.interaction.Projectile;
import com.dace.dmgr.combat.interaction.ProjectileOption;
import com.dace.dmgr.combat.interaction.Target;
import com.dace.dmgr.util.*;
import com.dace.dmgr.util.task.IntervalTask;
import com.dace.dmgr.util.task.TaskUtil;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.jetbrains.annotations.Nullable;

import java.text.MessageFormat;

public final class NeaceWeapon extends AbstractWeapon implements FullAuto {
    /** 대상 초기화 딜레이 쿨타임 ID */
    private static final String TARGET_RESET_DELAY_COOLDOWN_ID = "TargetResetDelay";
    /** 대상 위치 통과 불가 시 초기화 딜레이 쿨타임 ID */
    private static final String BLOCK_RESET_DELAY_COOLDOWN_ID = "BlockResetDelay";

    /** 연사 모듈 */
    @NonNull
    @Getter
    private final FullAutoModule fullAutoModule;
    /** 현재 사용 대상 */
    @Nullable
    private Healable target = null;

    public NeaceWeapon(@NonNull CombatUser combatUser) {
        super(combatUser, NeaceWeaponInfo.getInstance());
        fullAutoModule = new FullAutoModule(this, ActionKey.RIGHT_CLICK, FireRate.RPM_1200);
    }

    @Override
    @NonNull
    public ActionKey @NonNull [] getDefaultActionKeys() {
        return new ActionKey[]{ActionKey.LEFT_CLICK, ActionKey.RIGHT_CLICK};
    }

    @Override
    public long getDefaultCooldown() {
        return NeaceWeaponInfo.COOLDOWN;
    }

    @Override
    public boolean canUse(@NonNull ActionKey actionKey) {
        return super.canUse(actionKey) && combatUser.getSkill(NeaceUltInfo.getInstance()).isDurationFinished();
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        switch (actionKey) {
            case LEFT_CLICK: {
                setCooldown();
                combatUser.playMeleeAttackAnimation(-3, 6, true);

                new NeaceWeaponProjectile().shoot();

                SoundUtil.playNamedSound(NamedSound.COMBAT_NEACE_WEAPON_USE, combatUser.getEntity().getLocation());

                break;
            }
            case RIGHT_CLICK: {
                if (target == null) {
                    new NeaceTarget().shoot();

                    if (target == null)
                        return;
                }

                CooldownUtil.setCooldown(combatUser, TARGET_RESET_DELAY_COOLDOWN_ID, 4);
                if (LocationUtil.canPass(combatUser.getEntity().getEyeLocation(), target.getCenterLocation()))
                    CooldownUtil.setCooldown(combatUser, BLOCK_RESET_DELAY_COOLDOWN_ID, NeaceWeaponInfo.HEAL.BLOCK_RESET_DELAY);

                SoundUtil.playNamedSound(NamedSound.COMBAT_NEACE_WEAPON_USE_HEAL, combatUser.getEntity().getLocation());
                combatUser.getUser().sendTitle("", MessageFormat.format("{0} : {1}§e{2}",
                                (combatUser.getSkill(NeaceA2Info.getInstance()).isDurationFinished() ?
                                        "§a" + TextIcon.HEAL + " §f치유 중" : "§b" + TextIcon.DAMAGE_INCREASE + " §f강화 중"),
                                (target instanceof CombatUser && ((CombatUser) target).getCharacterType() != null ?
                                        ((CombatUser) target).getCharacterType().getCharacter().getIcon() + " " : ""),
                                target.getName()),
                        0, 5, 5);

                healTarget(target);

                break;
            }
            default:
                break;
        }
    }

    /**
     * 대상에게 치유 광선을 사용한다.
     *
     * @param target 치유 대상
     */
    void healTarget(@NonNull Healable target) {
        boolean isAmplifying = !combatUser.getSkill(NeaceA2Info.getInstance()).isDurationFinished();

        if (isAmplifying) {
            target.getStatusEffectModule().applyStatusEffect(combatUser, NeaceA2.NeaceA2Buff.instance, 4);
            if (target instanceof CombatUser)
                ((CombatUser) target).addKillAssist(combatUser, NeaceA2.ASSIST_SCORE_COOLDOWN_ID, NeaceA2Info.ASSIST_SCORE, 4);
        } else if (!target.getStatusEffectModule().hasStatusEffect(NeaceA1.NeaceA1Mark.instance))
            target.getDamageModule().heal(combatUser, NeaceWeaponInfo.HEAL.HEAL_PER_SECOND / 20, true);

        Location location = combatUser.getArmLocation(true);
        for (Location loc : LocationUtil.getLine(location, target.getCenterLocation(), 0.8)) {
            if (isAmplifying)
                ParticleUtil.playRGB(ParticleUtil.ColoredParticle.REDSTONE, loc, 1, 0, 0, 0,
                        140, 255, 245);
            else
                ParticleUtil.playRGB(ParticleUtil.ColoredParticle.REDSTONE, loc, 1, 0, 0, 0,
                        255, 255, 140);
        }
    }

    private final class NeaceTarget extends Target {
        private NeaceTarget() {
            super(combatUser, NeaceWeaponInfo.HEAL.MAX_DISTANCE, false,
                    combatEntity -> Neace.getTargetedActionCondition(NeaceWeapon.this.combatUser, combatEntity));
        }

        @Override
        protected void onFindEntity(@NonNull Damageable target) {
            NeaceWeapon.this.target = (Healable) target;
            CooldownUtil.setCooldown(combatUser, BLOCK_RESET_DELAY_COOLDOWN_ID, NeaceWeaponInfo.HEAL.BLOCK_RESET_DELAY);

            TaskUtil.addTask(NeaceWeapon.this, new IntervalTask(i -> target.canBeTargeted() && !target.isDisposed() &&
                    CooldownUtil.getCooldown(combatUser, TARGET_RESET_DELAY_COOLDOWN_ID) > 0 &&
                    CooldownUtil.getCooldown(combatUser, BLOCK_RESET_DELAY_COOLDOWN_ID) > 0 &&
                    combatUser.getEntity().getEyeLocation().distance(target.getCenterLocation()) <= NeaceWeaponInfo.HEAL.MAX_DISTANCE,
                    isCancelled -> NeaceWeapon.this.target = null, 1));
        }
    }

    private final class NeaceWeaponProjectile extends Projectile {
        private NeaceWeaponProjectile() {
            super(combatUser, NeaceWeaponInfo.VELOCITY, ProjectileOption.builder().trailInterval(10).size(NeaceWeaponInfo.SIZE)
                    .maxDistance(NeaceWeaponInfo.DISTANCE).condition(combatUser::isEnemy).build());
        }

        @Override
        protected void onTrailInterval() {
            Location loc = LocationUtil.getLocationFromOffset(getLocation(), 0.2, -0.2, 0);
            ParticleUtil.playRGB(ParticleUtil.ColoredParticle.SPELL_MOB, loc, 1, 0.05, 0.05, 0.05,
                    255, 255, 235);
            ParticleUtil.playRGB(ParticleUtil.ColoredParticle.REDSTONE, loc, 3, 0.1, 0.1, 0.1,
                    255, 255, 200);
        }

        @Override
        protected void onHit() {
            ParticleUtil.playRGB(ParticleUtil.ColoredParticle.SPELL_MOB, getLocation(), 15, 0.2, 0.2, 0.2,
                    255, 255, 200);
        }

        @Override
        protected boolean onHitBlock(@NonNull Block hitBlock) {
            return false;
        }

        @Override
        protected boolean onHitEntity(@NonNull Damageable target, boolean isCrit) {
            target.getDamageModule().damage(this, NeaceWeaponInfo.DAMAGE, DamageType.NORMAL, getLocation(), isCrit, true);
            return false;
        }
    }
}
