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
import com.dace.dmgr.game.Game;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * 더미(훈련용 봇) 엔티티 클래스.
 */
public final class Dummy extends TemporaryPlayerEntity implements Attacker, Healable, Movable, HasCritHitbox, CombatEntity {
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
     * @param dummyBehavior 행동 양식
     * @param spawnLocation 생성 위치
     * @param maxHealth     최대 체력
     * @param isEnemy       적 여부. {@code true}로 지정 시 적 더미, {@code false}로 지정 시 아군 더미
     * @throws IllegalStateException {@code spawnLocation}에 엔티티를 소환할 수 없으면 발생
     * @see DummyBehavior
     */
    public Dummy(@NonNull DummyBehavior dummyBehavior, @NonNull Location spawnLocation, int maxHealth, boolean isEnemy) {
        super(PlayerSkin.fromPlayerName("Clemounours"), spawnLocation, "훈련용 봇", null,
                Hitbox.builder(0.5, 0.7, 0.3).axisOffsetY(0.35).pitchFixed().build(),
                Hitbox.builder(0.8, 0.7, 0.45).axisOffsetY(1.05).pitchFixed().build(),
                Hitbox.builder(0.45, 0.35, 0.45).offsetY(0.225).axisOffsetY(1.4).build(),
                Hitbox.builder(0.45, 0.1, 0.45).offsetY(0.4).axisOffsetY(1.4).build());

        this.dummyBehavior = dummyBehavior;
        this.attackModule = new AttackModule();
        this.damageModule = new HealModule(this, maxHealth, true);
        this.statusEffectModule = new StatusEffectModule(this);
        this.moveModule = new MoveModule(this, GeneralConfig.getCombatConfig().getDefaultSpeed());
        this.isEnemy = isEnemy;

        onInit();
    }

    /**
     * 더미 인스턴스를 생성한다.
     *
     * @param spawnLocation 생성 위치
     * @param maxHealth     최대 체력
     */
    public Dummy(@NonNull Location spawnLocation, int maxHealth, boolean isEnemy) {
        this(new DummyBehavior() {
        }, spawnLocation, maxHealth, isEnemy);
    }

    private void onInit() {
        entity.setSilent(true);
        entity.setAI(true);

        if (!isEnemy)
            entity.getEquipment().setHelmet(new ItemStack(Material.STAINED_GLASS, 1, (short) 5));

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
    public void onAttack(@NonNull Damageable victim, double damage, boolean isCrit, boolean isUlt) {
        // 미사용
    }

    @Override
    public void onKill(@NonNull Damageable victim) {
        // 미사용
    }

    @Override
    public void onDamage(@Nullable Attacker attacker, double damage, double reducedDamage, @Nullable Location location, boolean isCrit) {
        CombatEffectUtil.playBreakParticle(this, location, damage);
    }

    @Override
    public void onTakeHeal(@Nullable Healer provider, double amount) {
        // 미사용
    }

    @Override
    public void onDeath(@Nullable Attacker attacker) {
        remove();
    }
}
