package com.dace.dmgr.combat.entity.temporary.dummy;

import com.dace.dmgr.GeneralConfig;
import com.dace.dmgr.PlayerSkin;
import com.dace.dmgr.combat.CombatEffectUtil;
import com.dace.dmgr.combat.entity.*;
import com.dace.dmgr.combat.entity.module.AttackModule;
import com.dace.dmgr.combat.entity.module.HealModule;
import com.dace.dmgr.combat.entity.module.MoveModule;
import com.dace.dmgr.combat.entity.module.StatusEffectModule;
import com.dace.dmgr.combat.entity.temporary.SummonEntity;
import com.dace.dmgr.combat.entity.temporary.TemporaryPlayerEntity;
import com.dace.dmgr.combat.interaction.HasCritHitbox;
import com.dace.dmgr.combat.interaction.Hitbox;
import com.dace.dmgr.effect.FireworkEffect;
import com.dace.dmgr.effect.ParticleEffect;
import com.dace.dmgr.effect.SoundEffect;
import com.dace.dmgr.game.Game;
import lombok.Getter;
import lombok.NonNull;
import org.apache.commons.lang3.Validate;
import org.bukkit.*;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * 더미(훈련용 봇) 엔티티 클래스.
 */
public final class Dummy extends TemporaryPlayerEntity implements Attacker, Healable, Movable, HasCritHitbox, CombatEntity {
    /** 생성 입자 효과 */
    private static final FireworkEffect SPAWN_PARTICLE = FireworkEffect.builder(org.bukkit.FireworkEffect.Type.BALL,
            Color.fromRGB(255, 255, 255)).fadeColor(Color.fromRGB(255, 255, 0)).build();
    /** 사망 입자 효과 */
    private static final ParticleEffect DEATH_PARTICLE = new ParticleEffect(
            ParticleEffect.NormalParticleInfo.builder(Particle.EXPLOSION_LARGE).build(),
            ParticleEffect.NormalParticleInfo.builder(ParticleEffect.BlockParticleType.BLOCK_DUST, Material.IRON_BLOCK, 0).count(150)
                    .horizontalSpread(0.2).verticalSpread(0.2).speed(0.25).build());
    /** 피격 효과음 */
    private static final SoundEffect DAMAGE_SOUND = new SoundEffect(
            SoundEffect.SoundInfo.builder(Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR).volume(0.1).pitch(1.5).pitchVariance(0.2).build());
    /** 사망 효과음 */
    private static final SoundEffect DEATH_SOUND = new SoundEffect(
            SoundEffect.SoundInfo.builder(Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR).volume(2).pitch(0.8).build(),
            SoundEffect.SoundInfo.builder("random.metalhit").volume(2).pitch(0.7).build(),
            SoundEffect.SoundInfo.builder(Sound.ENTITY_ITEM_BREAK).volume(2).pitch(0.7).build());

    /** 공격 모듈 */
    @NonNull
    @Getter
    private final AttackModule attackModule;
    /** 피해 모듈 */
    @NonNull
    @Getter
    private final HealModule damageModule;
    /** 상태 효과 모듈 */
    @NonNull
    @Getter
    private final StatusEffectModule statusEffectModule;
    /** 이동 모듈 */
    @NonNull
    @Getter
    private final MoveModule moveModule;
    /** 행동 양식 */
    private final DummyBehavior dummyBehavior;
    /** 적 여부 */
    @Getter
    private final boolean isEnemy;

    /**
     * 더미의 행동 양식을 지정하여 더미 인스턴스를 생성한다.
     *
     * @param dummyBehavior   행동 양식
     * @param spawnLocation   생성 위치
     * @param maxHealth       최대 체력
     * @param speedMultiplier 이동속도 배수. 0 이상의 값
     * @param isEnemy         적 여부. {@code true}로 지정 시 적 더미, {@code false}로 지정 시 아군 더미
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     * @throws IllegalStateException    {@code spawnLocation}에 엔티티를 소환할 수 없으면 발생
     * @see DummyBehavior
     */
    public Dummy(@NonNull DummyBehavior dummyBehavior, @NonNull Location spawnLocation, int maxHealth, double speedMultiplier, boolean isEnemy) {
        super(PlayerSkin.fromPlayerName("Clemounours"), spawnLocation, "훈련용 봇", null, Hitbox.createDefaultPlayerHitboxes(1));
        Validate.isTrue(speedMultiplier >= 0, "speedMultiplier >= 0 (%f)", speedMultiplier);

        this.dummyBehavior = dummyBehavior;
        this.attackModule = new AttackModule();
        this.damageModule = new HealModule(this, maxHealth, true);
        this.statusEffectModule = new StatusEffectModule(this);
        this.moveModule = new MoveModule(this, GeneralConfig.getCombatConfig().getDefaultSpeed() * speedMultiplier);
        this.isEnemy = isEnemy;

        onInit();
    }

    /**
     * 더미 인스턴스를 생성한다.
     *
     * @param spawnLocation   생성 위치
     * @param maxHealth       최대 체력
     * @param speedMultiplier 이동속도 배수. 0 이상의 값
     */
    public Dummy(@NonNull Location spawnLocation, int maxHealth, double speedMultiplier, boolean isEnemy) {
        this(new DummyBehavior() {
        }, spawnLocation, maxHealth, speedMultiplier, isEnemy);
    }

    private void onInit() {
        entity.setSilent(true);
        entity.setAI(true);

        if (!isEnemy)
            entity.getEquipment().setHelmet(new ItemStack(Material.STAINED_GLASS, 1, (short) 5));

        SPAWN_PARTICLE.play(getCenterLocation());

        dummyBehavior.onInit(this);
    }

    @Override
    @Nullable
    public Game.Team getTeam() {
        return null;
    }

    @Override
    public boolean isCreature() {
        return true;
    }

    @Override
    public int getScore() {
        return 100;
    }

    @Override
    public boolean isGoalTarget() {
        return true;
    }

    @Override
    public boolean isEnemy(@NonNull CombatEntity target) {
        if (target instanceof CombatUser || target instanceof SummonEntity)
            return isEnemy;

        return !(target instanceof Dummy) || isEnemy != ((Dummy) target).isEnemy;
    }

    @Override
    @Nullable
    public Hitbox getCritHitbox() {
        return hitboxes[3];
    }

    @Override
    public void onDefaultAttack(@NonNull Damageable victim) {
        dummyBehavior.onDefaultAttack(this, victim);
    }

    @Override
    public void onAttack(@NonNull Damageable victim, double damage, boolean isCrit, boolean isUlt) {
        // 미사용
    }

    @Override
    public void onKill(@NonNull Damageable victim) {
        // 미사용
    }

    @Override
    public void onDamage(@Nullable Attacker attacker, double damage, double reducedDamage, @Nullable Location location, boolean isCrit) {
        DAMAGE_SOUND.play(getLocation(), 1 + damage * 0.001);
        CombatEffectUtil.playBreakParticle(this, location, damage);
    }

    @Override
    public void onTakeHeal(@Nullable Healer provider, double amount) {
        // 미사용
    }

    @Override
    public void onDeath(@Nullable Attacker attacker) {
        remove();

        DEATH_PARTICLE.play(getCenterLocation());
        DEATH_SOUND.play(getLocation());
    }
}
