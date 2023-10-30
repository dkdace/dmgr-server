package com.dace.dmgr.combat.character.jager.action;

import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.DamageType;
import com.dace.dmgr.combat.entity.*;
import com.dace.dmgr.combat.entity.module.CombatEntityModule;
import com.dace.dmgr.combat.entity.module.DamageModule;
import com.dace.dmgr.combat.entity.module.JumpModule;
import com.dace.dmgr.combat.entity.statuseffect.StatusEffectType;
import com.dace.dmgr.util.ParticleUtil;
import com.dace.dmgr.util.SoundUtil;
import lombok.Getter;
import org.bukkit.DyeColor;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Wolf;
import org.inventivetalent.glow.GlowAPI;

/**
 * 예거 - 설랑 클래스.
 */
public final class JagerA1Entity extends SummonEntity<Wolf> implements Damageable, Attacker, Living, Movable {
    /** 스킬 객체 */
    private final JagerA1 skill;
    /** 피해 모듈 */
    @Getter
    private final DamageModule damageModule;
    /** 이동 모듈 */
    @Getter
    private final JumpModule moveModule;

    public JagerA1Entity(Wolf entity, CombatUser owner) {
        super(
                entity,
                "§f" + owner.getName() + "의 설랑",
                owner,
                new FixedPitchHitbox(entity.getLocation(), 0.4, 0.8, 1.2, 0, 0.4, 0)
        );
        skill = (JagerA1) owner.getSkill(JagerA1Info.getInstance());
        damageModule = new DamageModule(this, false, JagerA1Info.HEALTH);
        moveModule = new JumpModule(this);
    }

    @Override
    protected CombatEntityModule[] getModules() {
        return new CombatEntityModule[]{damageModule, moveModule};
    }

    @Override
    public void init() {
        super.init();

        abilityStatusManager.getAbilityStatus(Ability.SPEED).setBaseValue(entity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).getBaseValue() * 1.5);
        damageModule.setHealth((int) skill.getStateValue());
        entity.setAI(false);
        entity.setCollarColor(DyeColor.CYAN);
        entity.setTamed(true);
        entity.setOwner(owner.getEntity());
        entity.getAttribute(Attribute.GENERIC_FOLLOW_RANGE).setBaseValue(40);
        GlowAPI.setGlowing(entity, GlowAPI.Color.WHITE, owner.getEntity());
        playInitSound();
    }

    /**
     * 소환 시 효과음을 재생한다.
     */
    private void playInitSound() {
        SoundUtil.play(Sound.ENTITY_ZOMBIE_VILLAGER_CONVERTED, entity.getLocation(), 0.8F, 1F);
    }

    @Override
    public void onTick(int i) {
        super.onTick(i);

        double speed = abilityStatusManager.getAbilityStatus(Ability.SPEED).getValue();
        if (!moveModule.canMove())
            speed = 0.0001F;
        entity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(speed);

        if (i < JagerA1Info.SUMMON_DURATION)
            playSummonEffect();
        else if (i == JagerA1Info.SUMMON_DURATION) {
            playReadySound();
            entity.setAI(true);
        }

        if (i % 10 == 0 && entity.getTarget() == null) {
            Damageable target = (Damageable) CombatUtil.getNearEnemy(this, entity.getLocation(), JagerA1Info.LOW_HEALTH_DETECT_RADIUS,
                    combatEntity -> combatEntity instanceof Damageable && ((Damageable) combatEntity).getDamageModule().isLowHealth());
            if (target != null)
                entity.setTarget(target.getEntity());
        }
    }

    /**
     * 준비 대기 시간의 효과를 재생한다.
     */
    private void playSummonEffect() {
        ParticleUtil.playRGB(ParticleUtil.ColoredParticle.SPELL_MOB, entity.getLocation(), 5, 0.2F, 0.2F, 0.2F, 255, 255, 255);
    }

    /**
     * 준비 시 효과음을 재생한다.
     */
    private void playReadySound() {
        SoundUtil.play(Sound.ENTITY_WOLF_GROWL, entity.getLocation(), 1F, 1F);
    }

    @Override
    public void remove() {
        super.remove();

        skill.getHasEntityModule().setSummonEntity(null);
    }

    @Override
    public void onAttack(Damageable victim, int damage, DamageType damageType, boolean isCrit, boolean isUlt) {
        owner.onAttack(victim, damage, damageType, isCrit, isUlt);
    }

    @Override
    public void onKill(CombatEntity victim) {
        owner.onKill(victim);
    }

    @Override
    public void onDefaultAttack(Damageable victim) {
        victim.getDamageModule().damage(this, JagerA1Info.DAMAGE, DamageType.ENTITY, victim.hasStatusEffect(StatusEffectType.SNARE), true);
    }

    @Override
    public void onDamage(Attacker attacker, int damage, DamageType damageType, boolean isCrit, boolean isUlt) {
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
    public void onDeath(Attacker attacker) {
        remove();
        playDeathSound();
        skill.setStateValue(0);
        skill.setCooldown(JagerA1Info.COOLDOWN_DEATH);
    }

    /**
     * 죽었을 때 효과음을 재생한다.
     */
    private void playDeathSound() {
        SoundUtil.play(Sound.ENTITY_WOLF_DEATH, entity.getLocation(), 1F, 1F);
    }
}
