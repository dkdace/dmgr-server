package com.dace.dmgr.combat.trainingcenter;

import com.dace.dmgr.GeneralConfig;
import com.dace.dmgr.Timespan;
import com.dace.dmgr.Timestamp;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.combat.entity.temporary.dummy.ArenaMeleeDummyBehavior;
import com.dace.dmgr.combat.entity.temporary.dummy.ArenaRangedDummyBehavior;
import com.dace.dmgr.combat.entity.temporary.dummy.Dummy;
import com.dace.dmgr.combat.entity.temporary.dummy.DummyBehavior;
import com.dace.dmgr.effect.BossBarDisplay;
import com.dace.dmgr.effect.SoundEffect;
import com.dace.dmgr.util.location.LocationUtil;
import com.dace.dmgr.util.task.IntervalTask;
import lombok.*;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.Validate;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.text.MessageFormat;
import java.util.HashSet;

/**
 * 훈련장의 아레나 클래스.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Arena {
    /** 아레나 설정 */
    private static final GeneralConfig.TrainingConfig.ArenaConfig CONFIG = GeneralConfig.getTrainingConfig().getArenaConfig();
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
        return LocationUtil.isInSameBlockXZ(player.getLocation(), CONFIG.getOptionRegionCheckYCoordinate(), CONFIG.getOptionZoneBlock());
    }

    /**
     * 현재 플레이어가 아레나 지역에 있는지 확인한다.
     *
     * @return 아레나 지역 안에 있으면 {@code true} 반환
     */
    private boolean isInArena() {
        return LocationUtil.isInSameBlockXZ(Validate.notNull(combatUser).getLocation(), CONFIG.getRegionCheckYCoordinate(), CONFIG.getZoneBlock());
    }

    /**
     * 지정한 플레이어의 아레나 훈련을 시작한다.
     *
     * @param combatUser 대상 플레이어
     * @param option     아레나 설정
     * @return 시작 성공 여부
     */
    boolean start(@NonNull CombatUser combatUser, @NonNull Option option) {
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
        Location spawnLocation = CONFIG.getDummyLocation().add(RandomUtils.nextDouble(0, 8) - RandomUtils.nextDouble(0, 8), 0,
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
