package com.dace.dmgr.combat;

import com.dace.dmgr.GeneralConfig;
import com.dace.dmgr.Timespan;
import com.dace.dmgr.Timestamp;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.module.statuseffect.*;
import com.dace.dmgr.combat.entity.temporary.dummy.*;
import com.dace.dmgr.effect.BossBarDisplay;
import com.dace.dmgr.effect.ParticleEffect;
import com.dace.dmgr.effect.SoundEffect;
import com.dace.dmgr.user.User;
import com.dace.dmgr.util.EntityUtil;
import com.dace.dmgr.util.LocationUtil;
import com.dace.dmgr.util.task.DelayTask;
import com.dace.dmgr.util.task.IntervalTask;
import lombok.*;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.Validate;
import org.bukkit.*;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * 훈련장 클래스.
 */
public final class TrainingCenter {
    /** 훈련장 월드 인스턴스 */
    private static final World WORLD = Bukkit.getWorld("Training");
    /** 스폰 위치 */
    private static final Location SPAWN_LOCATION = new Location(WORLD, 95.5, 220, 111.5, 270, 0);

    /** 고정형 더미의 위치 목록 */
    private static final Location[] FIXED_DUMMY_LOCATIONS = {
            new Location(WORLD, 119.5, 220, 121.5, 90, 0),
            new Location(WORLD, 119.5, 220, 128.5, 90, 0),
            new Location(WORLD, 126.5, 220, 121.5, 90, 0),
            new Location(WORLD, 126.5, 220, 128.5, 90, 0),
            new Location(WORLD, 135.5, 223, 66.5, -45, 0),
            new Location(WORLD, 140.5, 225, 73.5, -90, 0),

            new Location(WORLD, 166.5, 219, 86.5, 180, 0),
            new Location(WORLD, 164.5, 219, 96.5, 180, 0),
            new Location(WORLD, 162.5, 219, 106.5, 180, 0),
            new Location(WORLD, 160.5, 219, 116.5, 180, 0),
            new Location(WORLD, 158.5, 219, 126.5, 180, 0),
            new Location(WORLD, 156.5, 219, 136.5, 180, 0),
            new Location(WORLD, 154.5, 219, 146.5, 180, 0),
    };
    /** 고정형 중량 더미의 위치 목록 */
    private static final Location[] FIXED_HEAVY_DUMMY_LOCATIONS = {
            new Location(WORLD, 110.5, 220, 133.5, 180, 0),
            new Location(WORLD, 102.5, 220, 133.5, 180, 0),
    };
    /** 이동형 더미의 위치 목록 */
    private static final Location[][] FIXED_MOVING_DUMMY_LOCATIONS = {
            {new Location(WORLD, 96.5, 220, 122), new Location(WORLD, 79.5, 220, 117.5)},
            {new Location(WORLD, 93.5, 220, 118.5), new Location(WORLD, 82.5, 220, 124.5)},
            {new Location(WORLD, 91.5, 220, 113.5), new Location(WORLD, 86.5, 220, 128.5)},

            {new Location(WORLD, 120.5, 220, 89.5), new Location(WORLD, 130.5, 220, 103.5)},
            {new Location(WORLD, 117.5, 220, 92.5), new Location(WORLD, 125.5, 220, 104.5)},
            {new Location(WORLD, 113.5, 220, 93.5), new Location(WORLD, 119.5, 220, 103.5)},
    };
    /** 공격형 적 더미의 위치 목록 */
    private static final Location[] SHOOTING_ENEMY_DUMMY_LOCATIONS = {
            new Location(WORLD, 84.5, 220, 102.5, -135, 2),
            new Location(WORLD, 89.5, 220, 107.5, -135, 2)
    };
    /** 공격형 아군 더미의 위치 목록 */
    private static final Location[] SHOOTING_TEAM_DUMMY_LOCATIONS = {
            new Location(WORLD, 93.5, 220, 93.5, 45, 2),
            new Location(WORLD, 97.5, 220, 99.5, 45, 2)
    };
    /** 아레나 더미의 생성 위치 */
    private static final Location ARENA_DUMMY_LOCATION = new Location(WORLD, 66.5, 219, 71.5);

    @Getter
    private static final TrainingCenter instance = new TrainingCenter();

    /**
     * 훈련장을 생성하고 기본 더미를 소환한다.
     */
    private TrainingCenter() {
        WORLD.setFullTime(16000);

        new EffectTestBlock();

        for (Location loc : FIXED_DUMMY_LOCATIONS)
            spawnDummy(() -> new Dummy(loc, 1000, 0, true));
        for (Location loc : FIXED_HEAVY_DUMMY_LOCATIONS)
            spawnDummy(() -> new Dummy(loc, 3000, 0, true));

        for (Location[] locs : FIXED_MOVING_DUMMY_LOCATIONS)
            spawnDummy(() -> new Dummy(new MovingBehavior(locs), locs[0], 1000, 1, true));

        for (Location loc : SHOOTING_ENEMY_DUMMY_LOCATIONS)
            spawnDummy(() -> new Dummy(new ShootingBehavior(), loc, 1000, 0, true));
        for (Location loc : SHOOTING_TEAM_DUMMY_LOCATIONS)
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
            dummy.addOnRemove(() -> new DelayTask(() -> spawnDummy(onSpawnDummy), GeneralConfig.getTrainingConfig().getDefaultDummyRespawnTime().toTicks()));
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
        return SPAWN_LOCATION.clone();
    }

    /**
     * 지정한 플레이어가 전투원 선택 지역에 있는지 확인한다.
     *
     * @param player 확인할 플레이어
     * @return 전투원 선택 지역 안에 있으면 {@code true} 반환
     */
    public boolean isInSelectCharZone(@NonNull Player player) {
        return LocationUtil.isInSameBlockXZ(player.getLocation(), GeneralConfig.getTrainingConfig().getSelectCharRegionCheckYCoordinate(),
                GeneralConfig.getTrainingConfig().getSelectCharZoneBlock());
    }

    /**
     * 플레이어가 훈련장을 시작했을 때 실행할 작업.
     *
     * @param user 대상 플레이어
     */
    public void onStart(@NonNull User user) {
        user.sendTitle("훈련장", "§b신호기 위치에서 전투원을 선택할 수 있습니다.", Timespan.ofSeconds(0.5), Timespan.ofSeconds(2),
                Timespan.ofSeconds(1.5), Timespan.ofSeconds(4));
        EntityUtil.teleport(user.getPlayer(), SPAWN_LOCATION);
    }

    /**
     * 디버프 실험 블록 클래스.
     */
    private static final class EffectTestBlock extends FunctionalBlock.CooldownBlock {
        /** 블록 위치별 디버프 적용에 실행할 작업의 목록 (위치 : 디버프 적용에 실행할 작업) */
        private static final HashMap<Location, Consumer<CombatUser>> USE_EFFECT_MAP = new HashMap<>();

        /** 사용 입자 효과 */
        private static final ParticleEffect USE_PARTICLE = new ParticleEffect(
                ParticleEffect.NormalParticleInfo.builder(Particle.VILLAGER_ANGRY).build());
        /** 사용 효과음 */
        private static final SoundEffect USE_SOUND = new SoundEffect(
                SoundEffect.SoundInfo.builder(Sound.ENTITY_PLAYER_BREATH).volume(1).pitch(0.8).build(),
                SoundEffect.SoundInfo.builder(Sound.ENTITY_ITEM_PICKUP).volume(1).pitch(0.7).build());

        private EffectTestBlock() {
            super(GeneralConfig.getTrainingConfig().getEffectTestBlock(), GeneralConfig.getTrainingConfig().getEffectTestCooldown());

            USE_EFFECT_MAP.put(new Location(WORLD, 124, 233, 107), combatUser ->
                    combatUser.getStatusEffectModule().apply(new Stun(null), Timespan.ofSeconds(1)));
            USE_EFFECT_MAP.put(new Location(WORLD, 126, 233, 107), combatUser ->
                    combatUser.getStatusEffectModule().apply(Snare.getInstance(), Timespan.ofSeconds(1)));
            USE_EFFECT_MAP.put(new Location(WORLD, 128, 233, 109), combatUser ->
                    combatUser.getStatusEffectModule().apply(Grounding.getInstance(), Timespan.ofSeconds(1)));
            USE_EFFECT_MAP.put(new Location(WORLD, 128, 233, 111), combatUser ->
                    combatUser.getStatusEffectModule().apply(new Burning(combatUser, 100, false), Timespan.ofSeconds(3)));
            USE_EFFECT_MAP.put(new Location(WORLD, 128, 233, 113), combatUser ->
                    combatUser.getStatusEffectModule().apply(new Poison(combatUser, 100, false), Timespan.ofSeconds(3)));
            USE_EFFECT_MAP.put(new Location(WORLD, 126, 233, 115), combatUser ->
                    combatUser.getStatusEffectModule().apply(HealBlock.getInstance(), Timespan.ofSeconds(3)));
            USE_EFFECT_MAP.put(new Location(WORLD, 124, 233, 115), combatUser ->
                    combatUser.getStatusEffectModule().apply(new Silence(null), Timespan.ofSeconds(3)));
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

    /**
     * 훈련장의 아레나 클래스.
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Arena {
        /** 타이머 효과음 */
        private static final SoundEffect TIMER_SOUND = new SoundEffect(
                SoundEffect.SoundInfo.builder(Sound.ENTITY_EXPERIENCE_ORB_PICKUP).volume(1000).pitch(1).build());
        /** 시작 효과음 */
        private static final SoundEffect ON_PLAY_SOUND = new SoundEffect(
                SoundEffect.SoundInfo.builder(Sound.ENTITY_WITHER_SPAWN).volume(1000).pitch(1).build());
        /** 종료 효과음 */
        private static final SoundEffect ON_FINISH_SOUND = new SoundEffect(
                SoundEffect.SoundInfo.builder(Sound.ENTITY_PLAYER_LEVELUP).volume(1000).pitch(1).build());
        @Getter
        private static final Arena instance = new Arena();

        /** 타이머 보스바 */
        private final BossBarDisplay timerBossBar = new BossBarDisplay("", BarColor.RED, BarStyle.SOLID, 1);
        /** 생성된 더미 목록 */
        private final HashSet<Dummy> dummies = new HashSet<>();

        /** 현재 사용자 */
        @Nullable
        private CombatUser combatUser;
        /** 실행 확인 작업을 처리하는 태스크 */
        @Nullable
        private IntervalTask runCheckTask;
        /** 준비 및 진행 작업을 처리하는 태스크 */
        @Nullable
        private IntervalTask onTickTask;
        /** 더미 생성 타임스탬프 */
        private Timestamp dummySpawnTimestamp = Timestamp.now();

        /**
         * 지정한 플레이어가 아레나 훈련 중인지 확인한다.
         *
         * @param combatUser 확인할 플레이어
         * @return 아레나 훈련 중 여부
         */
        public boolean isUsing(@NonNull CombatUser combatUser) {
            return this.combatUser == combatUser;
        }

        /**
         * 지정한 플레이어가 아레나 설정 지역에 있는지 확인한다.
         *
         * @param player 확인할 플레이어
         * @return 아레나 설정 지역 안에 있으면 {@code true} 반환
         */
        public boolean isInOptionZone(@NonNull Player player) {
            return LocationUtil.isInSameBlockXZ(player.getLocation(), GeneralConfig.getTrainingConfig().getArenaOptionRegionCheckYCoordinate(),
                    GeneralConfig.getTrainingConfig().getArenaOptionZoneBlock());
        }

        /**
         * 현재 플레이어가 아레나 지역에 있는지 확인한다.
         *
         * @return 아레나 지역 안에 있으면 {@code true} 반환
         */
        private boolean isInArena() {
            return LocationUtil.isInSameBlockXZ(Validate.notNull(combatUser).getLocation(), GeneralConfig.getTrainingConfig().getArenaRegionCheckYCoordinate(),
                    GeneralConfig.getTrainingConfig().getArenaZoneBlock());
        }

        /**
         * 지정한 플레이어의 아레나 훈련을 시작한다.
         *
         * @param combatUser 대상 플레이어
         * @param option     아레나 설정
         * @return 시작 성공 여부
         */
        public boolean start(@NonNull CombatUser combatUser, @NonNull Option option) {
            if (this.combatUser != null)
                return false;

            this.combatUser = combatUser;

            runCheckTask = new IntervalTask(i -> {
                if (!combatUser.isRemoved() && isInArena())
                    return true;

                finish();
                return false;
            }, 1);

            onTickTask = new IntervalTask(i -> {
                TIMER_SOUND.play(combatUser.getEntity());
                combatUser.getUser().sendTitle("§f" + (5 - i), "", Timespan.ZERO, Timespan.ofSeconds(0.25),
                        Timespan.ofSeconds(0.5), Timespan.ofSeconds(0.5));
            }, () -> onPlay(option), 20, 6);

            combatUser.addTask(onTickTask);
            return true;
        }

        /**
         * 아레나 훈련 진행 시 실행할 작업.
         *
         * @param option 아레나 설정
         */
        private void onPlay(@NonNull Option option) {
            Validate.notNull(combatUser);

            timerBossBar.show(combatUser.getEntity());

            ON_PLAY_SOUND.play(combatUser.getEntity());
            combatUser.getUser().sendTitle("§c§l훈련 시작", "", Timespan.ZERO, Timespan.ofSeconds(2), Timespan.ofSeconds(1),
                    Timespan.ofSeconds(2));

            long durationTicks = option.duration.toTicks();

            onTickTask = new IntervalTask(i -> onTickPlaying(option, Timespan.ofTicks(durationTicks - i).toSeconds()), () -> {
                combatUser.getUser().sendTitle("§e§l훈련 종료", "", Timespan.ofSeconds(0.4), Timespan.ofSeconds(2), Timespan.ofSeconds(1.5),
                        Timespan.ofSeconds(2));
                ON_FINISH_SOUND.play(combatUser.getEntity());

                finish();
            }, 1, durationTicks);

            combatUser.addTask(onTickTask);
        }

        /**
         * 아레나 훈련 진행 중 매 틱마다 실행할 작업.
         *
         * @param option           아레나 설정
         * @param remainingSeconds 남은 시간 (초)
         */
        private void onTickPlaying(@NonNull Option option, double remainingSeconds) {
            Validate.notNull(combatUser);

            timerBossBar.setTitle(MessageFormat.format("§c남은 시간 : §c§l{0}초", String.format("%.1f", remainingSeconds)));
            timerBossBar.setProgress(remainingSeconds / option.duration.toSeconds());

            if (dummySpawnTimestamp.isAfter(Timestamp.now()) || dummies.size() >= option.maxCount)
                return;

            dummySpawnTimestamp = Timestamp.now().plus(option.spawnPeriod);

            AttackMethod attackMethod = option.attackMethod;
            if (attackMethod == AttackMethod.RANDOM)
                attackMethod = RandomUtils.nextBoolean() ? AttackMethod.MELEE : AttackMethod.RANGED;

            DummyBehavior dummyBehavior = attackMethod == AttackMethod.MELEE
                    ? new ArenaMeleeDummyBehavior(combatUser, option.damage)
                    : new ArenaRangedDummyBehavior(combatUser, option.damage);
            Location spawnLocation = ARENA_DUMMY_LOCATION.clone().add(RandomUtils.nextDouble(0, 8) - RandomUtils.nextDouble(0, 8), 0,
                    RandomUtils.nextDouble(0, 8) - RandomUtils.nextDouble(0, 8));

            Dummy dummy = new Dummy(dummyBehavior, spawnLocation, option.health, option.speedMultiplier, true);
            dummy.addOnRemove(() -> dummies.remove(dummy));

            dummies.add(dummy);
        }

        /**
         * 아레나 훈련을 종료한다.
         */
        public void finish() {
            Validate.notNull(combatUser);

            timerBossBar.hide(combatUser.getEntity());

            if (onTickTask != null) {
                onTickTask.stop();
                onTickTask = null;
            }
            if (runCheckTask != null) {
                runCheckTask.stop();
                runCheckTask = null;
            }

            combatUser = null;
            new HashSet<>(dummies).forEach(Dummy::remove);
        }

        /**
         * 아레나 더미의 공격 방식 종류.
         */
        @AllArgsConstructor
        public enum AttackMethod {
            /** 근접 */
            MELEE("근접"),
            /** 원거리 */
            RANGED("원거리"),
            /** 무작위 */
            RANDOM("무작위");

            /** 이름 */
            private final String name;

            @Override
            public String toString() {
                return name;
            }
        }

        /**
         * 아레나의 설정을 관리하는 클래스.
         */
        @AllArgsConstructor
        public static final class Option {
            /** 더미 생명력 */
            private final int health;
            /** 더미 공격력 */
            private final int damage;
            /** 더미 이동속도 배수 */
            private final double speedMultiplier;
            /** 더미 생성 주기 */
            @NonNull
            private final Timespan spawnPeriod;
            /** 최대 더미 수 */
            private final int maxCount;
            /** 진행 시간 */
            @NonNull
            private final Timespan duration;
            /** 공격 방식 */
            @NonNull
            private final AttackMethod attackMethod;
        }
    }
}
