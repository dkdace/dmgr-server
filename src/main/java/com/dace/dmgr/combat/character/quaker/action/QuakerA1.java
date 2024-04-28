package com.dace.dmgr.combat.character.quaker.action;

import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.ChargeableSkill;
import com.dace.dmgr.combat.entity.Attacker;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.temporal.Barrier;
import com.dace.dmgr.combat.interaction.DamageType;
import com.dace.dmgr.combat.interaction.Hitbox;
import com.dace.dmgr.item.ItemBuilder;
import com.dace.dmgr.util.LocationUtil;
import com.dace.dmgr.util.NamedSound;
import com.dace.dmgr.util.ParticleUtil;
import com.dace.dmgr.util.SoundUtil;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.ArmorStand;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.EulerAngle;
import org.jetbrains.annotations.Nullable;

@Getter
public final class QuakerA1 extends ChargeableSkill {
    /** 수정자 ID */
    private static final String MODIFIER_ID = "QuakerA1";
    /** 소환한 엔티티 */
    private QuakerA1Entity summonEntity = null;

    QuakerA1(@NonNull CombatUser combatUser) {
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
            combatUser.setGlobalCooldown(QuakerA1Info.GLOBAL_COOLDOWN);
            combatUser.getMoveModule().getSpeedStatus().addModifier(MODIFIER_ID, -QuakerA1Info.USE_SLOW);
            combatUser.setFovValue(0.3);

            SoundUtil.playNamedSound(NamedSound.COMBAT_QUAKER_A1_USE, combatUser.getEntity().getLocation());

            ArmorStand armorStand = CombatUtil.spawnEntity(ArmorStand.class, combatUser.getEntity().getLocation());
            summonEntity = new QuakerA1Entity(armorStand, combatUser);
            summonEntity.activate();
        } else
            onCancelled();
    }

    @Override
    public void onCancelled() {
        super.onCancelled();

        setDuration(0);
        combatUser.getMoveModule().getSpeedStatus().removeModifier(MODIFIER_ID);
        combatUser.setFovValue(0);
        if (summonEntity != null)
            summonEntity.dispose();

        SoundUtil.playNamedSound(NamedSound.COMBAT_QUAKER_A1_DISABLE, combatUser.getEntity().getLocation());
    }

    @Override
    public void reset() {
        super.reset();

        if (summonEntity != null)
            summonEntity.dispose();
    }

    /**
     * 퀘이커 - 불굴의 방패 클래스.
     */
    public final class QuakerA1Entity extends Barrier<ArmorStand> {
        private QuakerA1Entity(@NonNull ArmorStand entity, @NonNull CombatUser owner) {
            super(
                    entity,
                    owner.getName() + "의 방패",
                    owner,
                    QuakerA1Info.HEALTH,
                    new Hitbox(entity.getLocation(), 6, 3.5, 0.3, 0, -0.3, 0, 0, 1.5, 0)
            );

            onInit();
        }

        private void onInit() {
            entity.setSilent(true);
            entity.setInvulnerable(true);
            entity.setGravity(false);
            entity.setAI(false);
            entity.setMarker(true);
            entity.setVisible(false);
            entity.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 99999, 0, false,
                    false), true);
            entity.setItemInHand(new ItemBuilder(Material.IRON_HOE).setDamage((short) 1).build());
            damageModule.setHealth(getStateValue());
        }

        @Override
        protected void onTick(long i) {
            Location loc = LocationUtil.getLocationFromOffset(owner.getEntity().getLocation(), owner.getEntity().getLocation().getDirection(),
                    0, 0.8, 1.5);
            entity.setRightArmPose(new EulerAngle(Math.toRadians(loc.getPitch() - 90), 0, 0));
            entity.teleport(loc);
        }

        @Override
        public void dispose() {
            super.dispose();

            summonEntity = null;
        }

        @Override
        public void onDamage(@Nullable Attacker attacker, int damage, int reducedDamage, @NonNull DamageType damageType, @Nullable Location location, boolean isCrit, boolean isUlt) {
            super.onDamage(attacker, damage, reducedDamage, damageType, location, isCrit, isUlt);

            addStateValue(-damage);

            SoundUtil.playNamedSound(NamedSound.COMBAT_QUAKER_A1_DAMAGE, entity.getLocation(), 1 + damage * 0.001);
            if (location != null)
                CombatUtil.playBreakEffect(location, entity, damage);
        }

        @Override
        public void onDeath(@Nullable Attacker attacker) {
            dispose();

            setStateValue(0);
            onCancelled();
            setCooldown(QuakerA1Info.COOLDOWN_DEATH);

            SoundUtil.playNamedSound(NamedSound.COMBAT_QUAKER_A1_DEATH, entity.getLocation());
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 2; j++) {
                    Location loc = LocationUtil.getLocationFromOffset(hitboxes[0].getCenter(), -1.8 + i * 1.8, -0.8 + j * 1.6, 0);
                    ParticleUtil.playBlock(ParticleUtil.BlockParticle.BLOCK_DUST, Material.IRON_BLOCK, 0, loc, 50,
                            0.3, 0.3, 0.3, 0.2);
                    ParticleUtil.play(Particle.CRIT, loc, 50, 0.3, 0.3, 0.3, 0.4);
                }
            }
        }
    }
}
