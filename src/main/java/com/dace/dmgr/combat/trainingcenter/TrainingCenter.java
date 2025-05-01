package com.dace.dmgr.combat.trainingcenter;

import com.dace.dmgr.GeneralConfig;
import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.combat.entity.combatuser.CooldownBlock;
import com.dace.dmgr.combat.entity.module.statuseffect.*;
import com.dace.dmgr.combat.entity.temporary.dummy.Dummy;
import com.dace.dmgr.combat.entity.temporary.dummy.MovingBehavior;
import com.dace.dmgr.combat.entity.temporary.dummy.ShootingBehavior;
import com.dace.dmgr.effect.ParticleEffect;
import com.dace.dmgr.effect.SoundEffect;
import com.dace.dmgr.user.User;
import com.dace.dmgr.util.EntityUtil;
import com.dace.dmgr.util.task.DelayTask;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.*;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * 훈련장 클래스.
 */
public final class TrainingCenter {
    /** 훈련장 설정 */
    private static final GeneralConfig.TrainingConfig CONFIG = GeneralConfig.getTrainingConfig();
    /** 기본 더미의 위치 설정 */
    private static final GeneralConfig.TrainingConfig.DefaultDummyConfig DEFAULT_DUMMY_CONFIG = CONFIG.getDefaultDummyConfig();
    /** 훈련장 월드 인스턴스 */
    private static final World WORLD = CONFIG.getWorld();

    @Getter
    private static final TrainingCenter instance = new TrainingCenter();

    /**
     * 훈련장을 생성하고 기본 더미를 소환한다.
     */
    private TrainingCenter() {
        WORLD.setFullTime(16000);

        new EffectTestBlock();

        for (Location loc : DEFAULT_DUMMY_CONFIG.getFixedLocations())
            spawnDummy(() -> new Dummy(loc, 1000, 0, true));
        for (Location loc : DEFAULT_DUMMY_CONFIG.getFixedHeavyLocations())
            spawnDummy(() -> {
                Dummy dummy = new Dummy(loc, 3000, 0, true);

                EntityEquipment equipment = dummy.getEntity().getEquipment();
                equipment.setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
                equipment.setLeggings(new ItemStack(Material.IRON_LEGGINGS));
                equipment.setBoots(new ItemStack(Material.IRON_BOOTS));

                return dummy;
            });

        for (Location[] locs : DEFAULT_DUMMY_CONFIG.getMovingLocations())
            spawnDummy(() -> new Dummy(new MovingBehavior(locs), locs[0], 1000, 1, true));

        for (Location loc : DEFAULT_DUMMY_CONFIG.getShootingEnemyLocations())
            spawnDummy(() -> new Dummy(new ShootingBehavior(), loc, 1000, 0, true));
        for (Location loc : DEFAULT_DUMMY_CONFIG.getShootingTeamLocations())
            spawnDummy(() -> new Dummy(new ShootingBehavior(), loc, 1000, 0, false));
    }

    /**
     * 기본 더미 생성을 시도한다.
     *
     * @param onSpawnDummy 더미 생성에 실행할 작업
     */
    private void spawnDummy(@NonNull Supplier<Dummy> onSpawnDummy) {
        try {
            Dummy dummy = onSpawnDummy.get();
            dummy.addOnRemove(() -> new DelayTask(() -> spawnDummy(onSpawnDummy), DEFAULT_DUMMY_CONFIG.getRespawnTime().toTicks()));
        } catch (IllegalStateException ex) {
            new DelayTask(() -> spawnDummy(onSpawnDummy), 20);
        }
    }

    /**
     * 스폰 위치를 반환한다.
     *
     * @return 스폰 위치
     */
    @NonNull
    public Location getSpawnLocation() {
        return CONFIG.getSpawnLocation();
    }

    /**
     * 플레이어가 훈련장을 시작했을 때 실행할 작업.
     *
     * @param user 대상 플레이어
     */
    public void onStart(@NonNull User user) {
        user.sendTitle("훈련장", "§b신호기 위치에서 전투원을 선택할 수 있습니다.", Timespan.ofSeconds(0.5), Timespan.ofSeconds(2),
                Timespan.ofSeconds(1.5), Timespan.ofSeconds(4));
        EntityUtil.teleport(user.getPlayer(), getSpawnLocation());
    }

    /**
     * 디버프 실험 블록 클래스.
     */
    private static final class EffectTestBlock extends CooldownBlock {
        /** 디버프 실험 블록 설정 */
        private static final GeneralConfig.TrainingConfig.EffectTestBlockConfig CONFIG = TrainingCenter.CONFIG.getEffectTestBlockConfig();
        /** 블록 위치별 디버프 적용에 실행할 작업의 목록 (위치 : 디버프 적용에 실행할 작업) */
        private static final HashMap<Location, Consumer<CombatUser>> USE_EFFECT_MAP = new HashMap<>();

        /** 사용 입자 효과 */
        private static final ParticleEffect USE_PARTICLE = new ParticleEffect(
                ParticleEffect.NormalParticleInfo.builder(Particle.VILLAGER_ANGRY).build());
        /** 사용 효과음 */
        private static final SoundEffect USE_SOUND = new SoundEffect(
                SoundEffect.SoundInfo.builder(Sound.ENTITY_PLAYER_BREATH).volume(1).pitch(0.8).build(),
                SoundEffect.SoundInfo.builder(Sound.ENTITY_ITEM_PICKUP).volume(1).pitch(0.7).build());

        static {
            USE_EFFECT_MAP.put(CONFIG.getStunLocation(), combatUser ->
                    combatUser.getStatusEffectModule().apply(new Stun(null), Timespan.ofSeconds(1)));
            USE_EFFECT_MAP.put(CONFIG.getSnareLocation(), combatUser ->
                    combatUser.getStatusEffectModule().apply(Snare.getInstance(), Timespan.ofSeconds(1)));
            USE_EFFECT_MAP.put(CONFIG.getGroundingLocation(), combatUser ->
                    combatUser.getStatusEffectModule().apply(Grounding.getInstance(), Timespan.ofSeconds(1)));
            USE_EFFECT_MAP.put(CONFIG.getBurningLocation(), combatUser ->
                    combatUser.getStatusEffectModule().apply(new Burning(combatUser, 100, false), Timespan.ofSeconds(3)));
            USE_EFFECT_MAP.put(CONFIG.getPoisonLocation(), combatUser ->
                    combatUser.getStatusEffectModule().apply(new Poison(combatUser, 100, false), Timespan.ofSeconds(3)));
            USE_EFFECT_MAP.put(CONFIG.getHealBlockLocation(), combatUser ->
                    combatUser.getStatusEffectModule().apply(HealBlock.getInstance(), Timespan.ofSeconds(3)));
            USE_EFFECT_MAP.put(CONFIG.getSilenceLocation(), combatUser ->
                    combatUser.getStatusEffectModule().apply(new Silence(null), Timespan.ofSeconds(3)));
        }

        private EffectTestBlock() {
            super(CONFIG.getBlock(), CONFIG.getCooldown());
        }

        @Override
        protected boolean canUse(@NonNull CombatUser combatUser, @NonNull Location location) {
            return super.canUse(combatUser, location) && USE_EFFECT_MAP.containsKey(location);
        }

        @Override
        protected void onUse(@NonNull CombatUser combatUser, @NonNull Location location) {
            super.onUse(combatUser, location);

            USE_EFFECT_MAP.get(location).accept(combatUser);
            USE_PARTICLE.play(location.clone().add(0.5, 0.5, 0.5));
            USE_SOUND.play(location);
        }
    }
}
