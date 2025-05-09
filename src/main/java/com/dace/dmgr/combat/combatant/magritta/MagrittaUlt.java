package com.dace.dmgr.combat.combatant.magritta;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.CombatEffectUtil;
import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.action.ActionBarStringUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.HasBonusScore;
import com.dace.dmgr.combat.action.skill.UltimateSkill;
import com.dace.dmgr.combat.action.skill.module.BonusScoreModule;
import com.dace.dmgr.combat.action.weapon.Weapon;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.combat.entity.module.AbilityStatus;
import com.dace.dmgr.util.location.LocationUtil;
import com.dace.dmgr.util.task.DelayTask;
import com.dace.dmgr.util.task.IntervalTask;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.inventory.MainHand;
import org.jetbrains.annotations.Nullable;

public final class MagrittaUlt extends UltimateSkill implements HasBonusScore {
    /** 수정자 */
    private static final AbilityStatus.Modifier MODIFIER = new AbilityStatus.Modifier(-MagrittaUltInfo.USE_SLOW);

    /** 보너스 점수 모듈 */
    @NonNull
    @Getter
    private final BonusScoreModule bonusScoreModule;
    /** 활성화 완료 여부 */
    private boolean isEnabled = false;

    public MagrittaUlt(@NonNull CombatUser combatUser) {
        super(combatUser, MagrittaUltInfo.getInstance(), MagrittaUltInfo.DURATION, MagrittaUltInfo.COST);
        this.bonusScoreModule = new BonusScoreModule(this, "궁극기 보너스", MagrittaUltInfo.KILL_SCORE);
    }

    @Override
    @Nullable
    public String getActionBarString() {
        return (isDurationFinished() || !isEnabled) ? null : ActionBarStringUtil.getDurationBar(this);
    }

    @Override
    public boolean canUse(@NonNull ActionKey actionKey) {
        return super.canUse(actionKey) && isDurationFinished() && combatUser.getActionManager().getSkill(MagrittaA2Info.getInstance()).isDurationFinished();
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        super.onUse(actionKey);

        setDuration(Timespan.MAX);

        combatUser.setGlobalCooldown(MagrittaUltInfo.READY_DURATION);
        combatUser.getMoveModule().getSpeedStatus().addModifier(MODIFIER);

        MagrittaWeapon weapon = (MagrittaWeapon) combatUser.getActionManager().getWeapon();
        weapon.cancel();
        weapon.getReloadModule().resetRemainingAmmo();

        MagrittaUltInfo.Sounds.USE.play(combatUser.getLocation());

        addActionTask(new DelayTask(() -> {
            isEnabled = true;
            setDuration();

            addActionTask(new IntervalTask(i -> {
                weapon.shot(true);

                CombatUtil.sendRecoil(combatUser, MagrittaWeaponInfo.Recoil.UP / 2, MagrittaWeaponInfo.Recoil.SIDE / 2,
                        MagrittaWeaponInfo.Recoil.UP_SPREAD / 2, MagrittaWeaponInfo.Recoil.SIDE_SPREAD / 2, 2, 1);

                Location loc = combatUser.getLocation();
                MagrittaUltInfo.Sounds.SHOOT.play(loc);

                addTask(new DelayTask(() -> CombatEffectUtil.SHOTGUN_SHELL_DROP_SOUND.play(loc), 8));
            }, this::onEnd, MagrittaUltInfo.ATTACK_COOLDOWN.toTicks(), MagrittaUltInfo.DURATION.divide(2).toTicks()));
        }, MagrittaUltInfo.READY_DURATION.toTicks()));
    }

    @Override
    protected void onDurationFinished() {
        super.onDurationFinished();

        isEnabled = false;
        combatUser.getMoveModule().getSpeedStatus().removeModifier(MODIFIER);
    }

    @Override
    public boolean isCancellable() {
        return !isDurationFinished();
    }

    @Override
    protected void onCancelled() {
        setDuration(Timespan.ZERO);
    }

    /**
     * 사용 종료 시 실행할 작업.
     */
    private void onEnd() {
        Weapon weapon = combatUser.getActionManager().getWeapon();
        Timespan weaponCooldown = weapon.getDefaultCooldown().multiply(2);

        weapon.setCooldown(weaponCooldown);
        weapon.setVisible(false);

        Location loc = LocationUtil.getLocationFromOffset(combatUser.getArmLocation(MainHand.RIGHT), 0, 0, 0.5);

        MagrittaUltInfo.Sounds.END.play(loc);
        MagrittaUltInfo.Particles.END.play(loc);
        CombatUtil.sendShake(combatUser, 10, 8, Timespan.ofTicks(7));

        addTask(new DelayTask(() -> {
            weapon.setVisible(true);

            MagrittaUltInfo.Sounds.USE.play(combatUser.getLocation());
        }, weaponCooldown.toTicks()));
    }
}
