package com.dace.dmgr.combat.combatant.quaker;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.CombatEffectUtil;
import com.dace.dmgr.combat.ability.ActionBarDisplay;
import com.dace.dmgr.combat.ability.ActionKey;
import com.dace.dmgr.combat.ability.handler.RightClickHandler;
import com.dace.dmgr.combat.ability.skill.ChargeableSkill;
import com.dace.dmgr.combat.ability.skill.module.SummonModule;
import com.dace.dmgr.combat.entity.Attacker;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.combat.entity.module.Modifier;
import com.dace.dmgr.combat.entity.temporary.Barrier;
import com.dace.dmgr.combat.interaction.Hitbox;
import com.dace.dmgr.item.ItemBuilder;
import com.dace.dmgr.util.location.LocationUtil;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.util.EulerAngle;
import org.jetbrains.annotations.Nullable;

public final class QuakerA1 extends ChargeableSkill implements RightClickHandler {
    /** 수정자 */
    private static final Modifier MODIFIER = new Modifier(-QuakerA1Info.USE_SLOW);
    /** 엔티티 소환 모듈 */
    private final SummonModule<QuakerA1Entity> summonModule;

    public QuakerA1(@NonNull CombatUser combatUser, @NonNull QuakerA1Info skillInfo) {
        super(combatUser, skillInfo, QuakerA1Info.COOLDOWN, QuakerA1Info.HEALTH);
        this.summonModule = new SummonModule<>(this);
    }

    @Override
    @NonNull
    public ActionBarDisplay getActionBarDisplay() {
        ActionBarDisplay.Builder builder = ActionBarDisplay.builder(this).title().progressBar();

        if (isDurationFinished())
            return builder.build();
        else
            return builder.keyInfo("해제").build();
    }

    @Override
    public double getStateValueDecrement() {
        return 0;
    }

    @Override
    public double getStateValueIncrement() {
        return QuakerA1Info.HEALTH / QuakerA1Info.RECOVER_DURATION.toSeconds();
    }

    @Override
    @NonNull
    public ActionKey.Slot getSlot() {
        return ActionKey.Slot.SLOT_1;
    }

    @Override
    public void onSlot() {
        combatUser.getAbilityManager().getWeapon().cancel();

        if (!isDurationFinished()) {
            cancel();
            combatUser.setGlobalCooldown(Timespan.ofTicks(1));

            return;
        }

        setDuration();
        combatUser.setGlobalCooldown(QuakerA1Info.GLOBAL_COOLDOWN);
        combatUser.getMoveModule().addModifier(MODIFIER);

        QuakerA1Info.Effects.ON.play(combatUser.getLocation());

        summonModule.set(new QuakerA1Entity(combatUser.getLocation()));
    }

    @Override
    public void onRightClick() {
        onSlot();
    }

    @Override
    public boolean isCancellable() {
        return !isDurationFinished();
    }

    @Override
    protected void onCancelled() {
        setDuration(Timespan.ZERO);
        combatUser.getMoveModule().removeModifier(MODIFIER);

        summonModule.removeEntity();

        QuakerA1Info.Effects.OFF.play(combatUser.getLocation());
    }

    /**
     * 불굴의 방패 클래스.
     */
    private final class QuakerA1Entity extends Barrier {
        private QuakerA1Entity(@NonNull Location spawnLocation) {
            super(spawnLocation, "방패", combatUser, QuakerA1Info.HEALTH, QuakerA1Info.DEATH_SCORE,
                    Hitbox.builder(6, 3.5, 0.3).axisOffsetY(1.4).build());
            onInit();
        }

        private void onInit() {
            entity.setGravity(false);
            entity.setHelmet(new ItemBuilder(Material.IRON_HOE).setDamage((short) 1).build());
            damageModule.setHealth(getStateValue());

            addOnTick(this::onTick);
        }

        private void onTick(long i) {
            Location loc = LocationUtil.getLocationFromOffset(owner.getLocation().add(0, 0.5, 0), 0, 0, 1.5);
            entity.setHeadPose(new EulerAngle(Math.toRadians(loc.getPitch()), 0, 0));
            entity.teleport(loc);
        }

        @Override
        public double getWidth() {
            return 6;
        }

        @Override
        public double getHeight() {
            return 2.8;
        }

        @Override
        public void onDamage(@Nullable Attacker attacker, double damage, double reducedDamage, @Nullable Location location, boolean isCrit) {
            super.onDamage(attacker, damage, reducedDamage, location, isCrit);

            setStateValue(damageModule.getHealth());

            combatUser.addScore(QuakerA1Info.BLOCK_SCORE.multiplyScore(damage));

            QuakerA1Info.Effects.DAMAGE.apply(this, location, damage).play(CombatEffectUtil.getHitLocation(this, location));
        }

        @Override
        public void onDeath(@Nullable Attacker attacker) {
            remove();
            cancel();

            setStateValue(0);
            setCooldown(QuakerA1Info.COOLDOWN_DEATH);

            QuakerA1Info.Effects.playDeath(getCenterLocation());
        }
    }
}
