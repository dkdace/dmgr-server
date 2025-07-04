package com.dace.dmgr.combat.combatant.no7;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.ability.ActionBarDisplay;
import com.dace.dmgr.combat.ability.ActionKey;
import com.dace.dmgr.combat.ability.handler.LeftClickHandler;
import com.dace.dmgr.combat.ability.skill.ActiveSkill;
import com.dace.dmgr.combat.ability.skill.module.SummonModule;
import com.dace.dmgr.combat.entity.combatuser.AbilityManager;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.combat.entity.temporary.BulletBarrier;
import com.dace.dmgr.combat.interaction.Bullet;
import com.dace.dmgr.combat.interaction.Hitbox;
import com.dace.dmgr.util.task.IntervalTask;
import lombok.NonNull;
import org.bukkit.Location;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.function.LongConsumer;

public final class No7A2 extends ActiveSkill implements LeftClickHandler {
    /** 엔티티 소환 모듈 */
    private final SummonModule<No7A2Entity> summonModule;

    public No7A2(@NonNull CombatUser combatUser, @NonNull No7A2Info skillInfo) {
        super(combatUser, skillInfo, No7A2Info.COOLDOWN, No7A2Info.DURATION);
        this.summonModule = new SummonModule<>(this);
    }

    @Override
    @Nullable
    public ActionBarDisplay getActionBarDisplay() {
        if (isDurationFinished())
            return null;

        return ActionBarDisplay.builder(this).title().durationBar().keyInfo("해제").build();
    }

    @Override
    protected boolean canUse() {
        AbilityManager abilityManager = combatUser.getAbilityManager();
        return super.canUse() && abilityManager.getAbility(No7A1Info.getInstance()).isDurationFinished()
                && abilityManager.getAbility(No7A3Info.getInstance()).isDurationFinished();
    }

    @Override
    @NonNull
    public ActionKey.Slot getSlot() {
        return ActionKey.Slot.SLOT_2;
    }

    @Override
    public void onSlot() {
        if (!isDurationFinished()) {
            cancel();
            return;
        }

        setDuration();

        combatUser.setGlobalCooldown(Timespan.ofTicks(1));
        combatUser.getAbilityManager().getWeapon().cancel();

        Location loc = combatUser.getLocation();
        summonModule.set(new No7A2Entity(loc));

        No7A2Info.Effects.ON.play(loc);

        addActionTask(new IntervalTask((LongConsumer) i -> No7A2Info.Effects.playTick(i, combatUser.getLocation(), loc), 1,
                No7A2Info.DURATION.toTicks()));
    }

    @Override
    public void onLeftClick() {
        onSlot();
    }

    @Override
    protected void onDurationFinished() {
        super.onDurationFinished();

        summonModule.removeEntity();
        No7A2Info.Effects.OFF.play(combatUser.getLocation());
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
     * 능동방어 자기장 클래스.
     */
    private final class No7A2Entity extends BulletBarrier {
        private final HashSet<Class<?>> bulletClasses = new HashSet<>();

        private No7A2Entity(@NonNull Location spawnLocation) {
            super(spawnLocation, combatUser.getName() + "의 능동방어 자기장", combatUser,
                    Hitbox.builder(No7A2Info.SIZE, No7A2Info.SIZE, No7A2Info.SIZE).axisOffsetY(1).pitchFixed().build());
            onInit();
        }

        private void onInit() {
            entity.setGravity(false);
            addOnTick(i -> entity.teleport(owner.getLocation().add(0, 1, 0)));
        }

        @Override
        public double getWidth() {
            return No7A2Info.SIZE;
        }

        @Override
        public double getHeight() {
            return No7A2Info.SIZE;
        }

        @Override
        public void onRemove(@NonNull Bullet<?> bullet, @NonNull Location location) {
            No7A2Info.Effects.DAMAGE.play(location);

            if (bulletClasses.add(bullet.getClass()))
                combatUser.addScore("피해 흡수", No7A2Info.BLOCK_SCORE);
        }
    }
}
