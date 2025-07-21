package com.dace.dmgr.combat.combatant.magritta;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.ability.ActionBarDisplay;
import com.dace.dmgr.combat.ability.skill.HasBonusScore;
import com.dace.dmgr.combat.ability.skill.UltimateSkill;
import com.dace.dmgr.combat.ability.skill.module.BonusScoreModule;
import com.dace.dmgr.combat.ability.weapon.Weapon;
import com.dace.dmgr.combat.entity.combatuser.CombatScore;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.combat.entity.module.Modifier;
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
    private static final Modifier MODIFIER = new Modifier(-MagrittaUltInfo.USE_SLOW);

    /** 보너스 점수 모듈 */
    @NonNull
    @Getter
    private final BonusScoreModule bonusScoreModule;
    /** 활성화 완료 여부 */
    private boolean isEnabled = false;

    public MagrittaUlt(@NonNull CombatUser combatUser, @NonNull MagrittaUltInfo skillInfo) {
        super(combatUser, skillInfo, MagrittaUltInfo.DURATION, MagrittaUltInfo.COST);
        this.bonusScoreModule = new BonusScoreModule(this);
    }

    @Override
    @Nullable
    public ActionBarDisplay getActionBarDisplay() {
        if (isDurationFinished() || !isEnabled)
            return null;

        return ActionBarDisplay.builder(this).title().durationBar().build();
    }

    @Override
    protected boolean canUse() {
        return super.canUse() && isDurationFinished() && combatUser.getAbilityManager().getAbility(MagrittaA2Info.getInstance()).isDurationFinished();
    }

    @Override
    public void onSlot() {
        setDuration(Timespan.MAX);

        combatUser.setGlobalCooldown(MagrittaUltInfo.READY_DURATION);
        combatUser.getMoveModule().addModifier(MODIFIER);

        MagrittaWeapon weapon = combatUser.getAbilityManager().getWeapon();
        weapon.cancel();
        weapon.getReloadModule().resetRemainingAmmo();

        MagrittaUltInfo.Effects.USE.play(combatUser.getLocation());

        addActionTask(new DelayTask(() -> {
            isEnabled = true;
            setDuration();

            addActionTask(new IntervalTask(i -> {
                weapon.shot(true);

                MagrittaUltInfo.RECOIL.send(combatUser);

                Location loc = combatUser.getLocation();
                MagrittaUltInfo.Effects.SHOOT.play(loc);

                addTask(new DelayTask(() -> MagrittaWeaponInfo.Effects.BULLET_SHELL.play(loc), 8));
            }, this::onEnd, MagrittaUltInfo.ATTACK_COOLDOWN.toTicks(), MagrittaUltInfo.DURATION.divide(2).toTicks()));
        }, MagrittaUltInfo.READY_DURATION.toTicks()));
    }

    @Override
    protected void onDurationFinished() {
        super.onDurationFinished();

        isEnabled = false;
        combatUser.getMoveModule().removeModifier(MODIFIER);
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
        Weapon weapon = combatUser.getAbilityManager().getWeapon();
        Timespan weaponCooldown = weapon.getDefaultCooldown().multiply(2);

        weapon.setCooldown(weaponCooldown);
        weapon.setVisible(false);

        Location loc = LocationUtil.getLocationFromOffset(combatUser.getArmLocation(MainHand.RIGHT), 0, 0, 0.5);

        MagrittaUltInfo.Effects.END.play(loc);
        MagrittaUltInfo.SHAKE.send(combatUser);

        addTask(new DelayTask(() -> {
            weapon.setVisible(true);

            MagrittaUltInfo.Effects.USE.play(combatUser.getLocation());
        }, weaponCooldown.toTicks()));
    }

    @Override
    @NonNull
    public CombatScore getBonusCombatScore() {
        return MagrittaUltInfo.KILL_SCORE;
    }
}
