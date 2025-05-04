package com.dace.dmgr.combat.combatant.quaker;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.CombatEffectUtil;
import com.dace.dmgr.combat.action.ActionBarStringUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.ChargeableSkill;
import com.dace.dmgr.combat.action.skill.Summonable;
import com.dace.dmgr.combat.action.skill.module.EntityModule;
import com.dace.dmgr.combat.entity.Attacker;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.combat.entity.module.AbilityStatus;
import com.dace.dmgr.combat.entity.temporary.Barrier;
import com.dace.dmgr.combat.interaction.Hitbox;
import com.dace.dmgr.item.ItemBuilder;
import com.dace.dmgr.util.location.LocationUtil;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.util.EulerAngle;
import org.jetbrains.annotations.Nullable;

@Getter
public final class QuakerA1 extends ChargeableSkill implements Summonable<QuakerA1.QuakerA1Entity> {
    /** 수정자 */
    private static final AbilityStatus.Modifier MODIFIER = new AbilityStatus.Modifier(-QuakerA1Info.USE_SLOW);
    /** 소환 엔티티 모듈 */
    @NonNull
    private final EntityModule<QuakerA1Entity> entityModule;

    public QuakerA1(@NonNull CombatUser combatUser) {
        super(combatUser, QuakerA1Info.getInstance(), QuakerA1Info.COOLDOWN, QuakerA1Info.HEALTH, 0);
        this.entityModule = new EntityModule<>(this);
    }

    @Override
    @NonNull
    public ActionKey @NonNull [] getDefaultActionKeys() {
        return new ActionKey[]{ActionKey.SLOT_1, ActionKey.RIGHT_CLICK};
    }

    @Override
    @NonNull
    public String getActionBarString() {
        String text = ActionBarStringUtil.getProgressBar(this);
        if (!isDurationFinished())
            text += ActionBarStringUtil.getKeyInfo(this, "해제");

        return text;
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
    public void onUse(@NonNull ActionKey actionKey) {
        combatUser.getActionManager().getWeapon().cancel();

        if (!isDurationFinished()) {
            cancel();
            combatUser.setGlobalCooldown(Timespan.ofTicks(1));

            return;
        }

        setDuration();
        combatUser.setGlobalCooldown(QuakerA1Info.GLOBAL_COOLDOWN);
        combatUser.getMoveModule().getSpeedStatus().addModifier(MODIFIER);

        QuakerA1Info.Sounds.USE.play(combatUser.getLocation());

        entityModule.set(new QuakerA1Entity(combatUser.getLocation()));
    }

    @Override
    public boolean isCancellable() {
        return !isDurationFinished();
    }

    @Override
    protected void onCancelled() {
        setDuration(Timespan.ZERO);
        combatUser.getMoveModule().getSpeedStatus().removeModifier(MODIFIER);

        entityModule.disposeEntity();

        QuakerA1Info.Sounds.DISABLE.play(combatUser.getLocation());
    }

    /**
     * 불굴의 방패 클래스.
     */
    public final class QuakerA1Entity extends Barrier {
        private QuakerA1Entity(@NonNull Location spawnLocation) {
            super(spawnLocation, combatUser.getName() + "의 방패", combatUser, QuakerA1Info.HEALTH, QuakerA1Info.DEATH_SCORE,
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

            combatUser.addScore("피해 막음", damage * QuakerA1Info.BLOCK_SCORE / QuakerA1Info.HEALTH);

            QuakerA1Info.Sounds.DAMAGE.play(location == null ? getLocation() : location, 1 + damage * 0.001);
            if (location != null)
                CombatEffectUtil.playBreakParticle(this, location, damage);
        }

        @Override
        public void onDeath(@Nullable Attacker attacker) {
            remove();
            cancel();

            setStateValue(0);
            setCooldown(QuakerA1Info.COOLDOWN_DEATH);

            QuakerA1Info.Sounds.DEATH.play(getCenterLocation());
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 2; j++) {
                    Location loc = LocationUtil.getLocationFromOffset(getCenterLocation(), -1.8 + i * 1.8, -0.8 + j * 1.6, 0);
                    QuakerA1Info.Particles.DEATH.play(loc);
                }
            }
        }
    }
}
