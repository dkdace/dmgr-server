package com.dace.dmgr.combat.character.jager.action;

import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.entity.*;
import com.dace.dmgr.util.ParticleUtil;
import com.dace.dmgr.util.SoundUtil;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Wolf;
import org.inventivetalent.glow.GlowAPI;

/**
 * 예거 - 설랑 클래스.
 */
public final class JagerA1Entity extends SummonEntity<Wolf> {
    /** 스킬 객체 */
    private final JagerA1 skill;

    public JagerA1Entity(Wolf entity, CombatUser owner) {
        super(
                entity,
                "§f" + owner.getName() + "의 설랑",
                new Hitbox(0.4, 0.8, 1.2, 0, 0.4, 0),
                new Hitbox(0, 0, 0, 0, 0, 0),
                false,
                JagerA1Info.HEALTH,
                owner
        );
        skill = (JagerA1) owner.getSkill(JagerA1Info.getInstance());
    }

    @Override
    public void onTick(int i) {
        super.onTick(i);

        if (i < JagerA1Info.SUMMON_DURATION)
            ParticleUtil.playRGB(ParticleUtil.ColoredParticle.SPELL_MOB, entity.getLocation(), 5, 0.2F, 0.2F, 0.2F, 255, 255, 255);
        else if (i == JagerA1Info.SUMMON_DURATION) {
            playReadySound();
            entity.setAI(true);
        }

        if (i % 10 == 0 && entity.getTarget() == null) {
            CombatEntity<?> target = CombatUtil.getNearEnemy(this, entity.getLocation(), JagerA1Info.LOW_HEALTH_DETECT_RADIUS,
                    CombatEntity::isLowHealth).getKey();
            if (target != null)
                entity.setTarget(target.getEntity());
        }
    }

    @Override
    public void onDamage(CombatEntity<?> attacker, int damage, String type, boolean isCrit, boolean isUlt) {
        playDamageSound(damage);
        skill.addStateValue(-damage);
    }

    /**
     * 피해를 입었을 때 효과음을 재생한다.
     *
     * @param damage 피해량
     */
    private void playDamageSound(float damage) {
        SoundUtil.play(Sound.ENTITY_WOLF_HURT, entity.getLocation(), (float) (0.4 + damage * 0.001), (float) (0.95 + Math.random() * 0.1));
    }

    @Override
    public void onDeath(CombatEntity<?> attacker) {
        super.onDeath(attacker);

        playDeathSound();
        skill.setStateValue(0);
        skill.setCooldown(JagerA1Info.COOLDOWN_DEATH);
        skill.getSummonEntities().clear();
    }

    /**
     * 죽었을 때 효과음을 재생한다.
     */
    private void playDeathSound() {
        SoundUtil.play(Sound.ENTITY_WOLF_DEATH, entity.getLocation(), 1F, 1F);
    }

    @Override
    protected void onInitTemporalEntity(Location location) {
        abilityStatusManager.getAbilityStatus(Ability.SPEED).setBaseValue(entity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).getBaseValue() * 1.5);
        setTeam(owner.getTeam());
        setHealth((int) skill.getStateValue());
        entity.setAI(false);
        entity.setCollarColor(DyeColor.CYAN);
        entity.setTamed(true);
        entity.setOwner(owner.getEntity());
        entity.getAttribute(Attribute.GENERIC_FOLLOW_RANGE).setBaseValue(40);
        GlowAPI.setGlowing(entity, GlowAPI.Color.WHITE, owner.getEntity());
    }

    /**
     * 준비 시 효과음을 재생한다.
     */
    private void playReadySound() {
        SoundUtil.play(Sound.ENTITY_WOLF_GROWL, entity.getLocation(), 1F, 1F);
    }
}
