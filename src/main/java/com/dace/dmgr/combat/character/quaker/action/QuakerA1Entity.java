package com.dace.dmgr.combat.character.quaker.action;

import com.dace.dmgr.combat.DamageType;
import com.dace.dmgr.combat.entity.Attacker;
import com.dace.dmgr.combat.entity.Barrier;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.interaction.Hitbox;
import com.dace.dmgr.item.ItemBuilder;
import com.dace.dmgr.util.LocationUtil;
import com.dace.dmgr.util.ParticleUtil;
import com.dace.dmgr.util.SoundUtil;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.EulerAngle;

/**
 * 퀘이커 - 불굴의 방패 클래스.
 */
public final class QuakerA1Entity extends Barrier<ArmorStand> {
    /** 스킬 객체 */
    private final QuakerA1 skill;

    public QuakerA1Entity(@NonNull ArmorStand entity, @NonNull CombatUser owner) {
        super(
                entity,
                owner.getName() + "의 방패",
                owner,
                QuakerA1Info.HEALTH,
                new Hitbox(entity.getLocation(), 6, 3.5, 0.3, 0, -0.3, 0, 0, 1.5, 0)
        );
        skill = (QuakerA1) owner.getSkill(QuakerA1Info.getInstance());

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

        damageModule.setHealth((int) skill.getStateValue());
    }

    @Override
    protected void onTick(long i) {
        Location loc = LocationUtil.getLocationFromOffset(owner.getEntity().getLocation(), owner.getEntity().getLocation().getDirection(),
                0, 0.8, 1.5);
        entity.setRightArmPose(new EulerAngle(Math.toRadians(loc.getPitch() - 90), 0, 0));
        entity.teleport(loc);

//        Hitbox hitbox = hitboxes[0];
//        Location[] particleLocs = new Location[4];
//        particleLocs[0] = LocationUtil.getLocationFromOffset(hitbox.getCenter(), -hitbox.getSizeX() / 2, -hitbox.getSizeY() / 2, 0);
//        particleLocs[1] = LocationUtil.getLocationFromOffset(hitbox.getCenter(), hitbox.getSizeX() / 2, -hitbox.getSizeY() / 2, 0);
//        particleLocs[2] = LocationUtil.getLocationFromOffset(hitbox.getCenter(), -hitbox.getSizeX() / 2, hitbox.getSizeY() / 2, 0);
//        particleLocs[3] = LocationUtil.getLocationFromOffset(hitbox.getCenter(), hitbox.getSizeX() / 2, hitbox.getSizeY() / 2, 0);
//
//        if (i % 4 == 0) {
//            for (Location loc2 : LocationUtil.getLine(particleLocs[0], particleLocs[1], 0.4))
//                ParticleUtil.play(Particle.CRIT, loc2, 1, 0, 0, 0, 0);
//            for (Location loc2 : LocationUtil.getLine(particleLocs[0], particleLocs[2], 0.4))
//                ParticleUtil.play(Particle.CRIT, loc2, 1, 0, 0, 0, 0);
//            for (Location loc2 : LocationUtil.getLine(particleLocs[1], particleLocs[3], 0.4))
//                ParticleUtil.play(Particle.CRIT, loc2, 1, 0, 0, 0, 0);
//            for (Location loc2 : LocationUtil.getLine(particleLocs[2], particleLocs[3], 0.4))
//                ParticleUtil.play(Particle.CRIT, loc2, 1, 0, 0, 0, 0);
//        }
    }

    @Override
    public void dispose() {
        super.dispose();

        skill.entity = null;
    }

    @Override
    public void onDamage(Attacker attacker, int damage, int reducedDamage, @NonNull DamageType damageType, Location location, boolean isCrit, boolean isUlt) {
        super.onDamage(attacker, damage, reducedDamage, damageType, location, isCrit, isUlt);

        SoundUtil.play(Sound.BLOCK_ANVIL_LAND, entity.getLocation(), 0.25 + damage * 0.001, 1.2, 0.1);
        SoundUtil.play("random.metalhit", entity.getLocation(), 0.3 + damage * 0.001, 0.85, 0.1);
        if (location != null)
            ParticleUtil.playBlock(ParticleUtil.BlockParticle.BLOCK_DUST, Material.IRON_BLOCK, 0, location, (int) Math.ceil(damage * 0.04),
                    0, 0, 0, 0.1);
        skill.addStateValue(-damage);
    }

    @Override
    public void onDeath(Attacker attacker) {
        dispose();
        SoundUtil.play(Sound.ENTITY_IRONGOLEM_HURT, entity.getLocation(), 2, 0.5);
        SoundUtil.play(Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, entity.getLocation(), 2, 0.7);
        SoundUtil.play("random.metalhit", entity.getLocation(), 2, 0.7);
        SoundUtil.play(Sound.ITEM_SHIELD_BLOCK, entity.getLocation(), 2, 0.5);
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 2; j++) {
                Location loc = LocationUtil.getLocationFromOffset(hitboxes[0].getCenter(), -1.8 + i * 1.8, -0.8 + j * 1.6, 0);
                ParticleUtil.playBlock(ParticleUtil.BlockParticle.BLOCK_DUST, Material.IRON_BLOCK, 0, loc, 50,
                        0.3, 0.3, 0.3, 0.2);
                ParticleUtil.play(Particle.CRIT, loc, 50, 0.3, 0.3, 0.3, 0.4);
            }
        }

        skill.setStateValue(0);
        skill.onCancelled();
        skill.setCooldown(QuakerA1Info.COOLDOWN_DEATH);
    }
}
