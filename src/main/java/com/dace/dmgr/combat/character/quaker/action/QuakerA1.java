package com.dace.dmgr.combat.character.quaker.action;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.CombatEffectUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.ChargeableSkill;
import com.dace.dmgr.combat.entity.Attacker;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.temporary.Barrier;
import com.dace.dmgr.combat.interaction.Hitbox;
import com.dace.dmgr.item.ItemBuilder;
import com.dace.dmgr.util.LocationUtil;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.util.EulerAngle;
import org.jetbrains.annotations.Nullable;

public final class QuakerA1 extends ChargeableSkill {
    /** 수정자 ID */
    private static final String MODIFIER_ID = "QuakerA1";
    /** 소환한 엔티티 */
    private QuakerA1Entity summonEntity = null;

    public QuakerA1(@NonNull CombatUser combatUser) {
        super(combatUser, QuakerA1Info.getInstance(), 0);
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
    public int getStateValueDecrement() {
        return 0;
    }

    @Override
    public int getStateValueIncrement() {
        return QuakerA1Info.HEALTH / QuakerA1Info.RECOVER_DURATION;
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        combatUser.getWeapon().onCancelled();

        if (isDurationFinished()) {
            setDuration();
            combatUser.setGlobalCooldown(Timespan.ofTicks(QuakerA1Info.GLOBAL_COOLDOWN));
            combatUser.getMoveModule().getSpeedStatus().addModifier(MODIFIER_ID, -QuakerA1Info.USE_SLOW);

            QuakerA1Info.SOUND.USE.play(combatUser.getLocation());

            summonEntity = new QuakerA1Entity(combatUser.getLocation());
        } else
            onCancelled();
    }

    @Override
    public boolean isCancellable() {
        return !isDurationFinished();
    }

    @Override
    public void onCancelled() {
        super.onCancelled();

        setDuration(0);
        combatUser.getMoveModule().getSpeedStatus().removeModifier(MODIFIER_ID);
        if (summonEntity != null)
            summonEntity.dispose();

        QuakerA1Info.SOUND.DISABLE.play(combatUser.getLocation());
    }

    @Override
    public void reset() {
        super.reset();

        if (summonEntity != null)
            summonEntity.dispose();
    }

    /**
     * 불굴의 방패 클래스.
     */
    private final class QuakerA1Entity extends Barrier {
        private QuakerA1Entity(@NonNull Location spawnLocation) {
            super(
                    spawnLocation,
                    combatUser.getName() + "의 방패",
                    combatUser,
                    QuakerA1Info.DEATH_SCORE,
                    QuakerA1Info.HEALTH,
                    Hitbox.builder(6, 3.5, 0.3).offsetY(-0.3).axisOffsetY(1.5).build()
            );

            onInit();
        }

        private void onInit() {
            entity.setGravity(false);
            entity.setItemInHand(new ItemBuilder(Material.IRON_HOE).setDamage((short) 1).build());
            damageModule.setHealth(getStateValue());
        }

        @Override
        protected void onTick(long i) {
            Location loc = LocationUtil.getLocationFromOffset(owner.getLocation(), owner.getLocation().getDirection(),
                    0, 0.8, 1.5);
            entity.setRightArmPose(new EulerAngle(Math.toRadians(loc.getPitch() - 90), 0, 0));
            entity.teleport(loc);
        }

        @Override
        protected void onDispose() {
            super.onDispose();
            summonEntity = null;
        }

        @Override
        public double getWidth() {
            return 6;
        }

        @Override
        public double getHeight() {
            return 3.5;
        }

        @Override
        public void onDamage(@Nullable Attacker attacker, double damage, double reducedDamage, @Nullable Location location,
                             boolean isCrit) {
            super.onDamage(attacker, damage, reducedDamage, location, isCrit);

            setStateValue((int) damageModule.getHealth());

            combatUser.addScore("피해 막음", damage * QuakerA1Info.BLOCK_SCORE / QuakerA1Info.HEALTH);

            QuakerA1Info.SOUND.DAMAGE.play(getLocation(), 1 + damage * 0.001);
            if (location != null)
                CombatEffectUtil.playBreakParticle(this, location, damage);
        }

        @Override
        public void onDeath(@Nullable Attacker attacker) {
            dispose();

            setStateValue(0);
            onCancelled();
            setCooldown(QuakerA1Info.COOLDOWN_DEATH);

            QuakerA1Info.SOUND.DEATH.play(getLocation());
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 2; j++) {
                    Location loc = LocationUtil.getLocationFromOffset(getCenterLocation(), -1.8 + i * 1.8, -0.8 + j * 1.6, 0);
                    QuakerA1Info.PARTICLE.DEATH.play(loc);
                }
            }
        }
    }
}
