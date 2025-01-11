package com.dace.dmgr;

import com.dace.dmgr.game.Tier;
import com.dace.dmgr.util.task.AsyncTask;
import com.dace.dmgr.util.task.Initializable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.Material;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Paths;

/**
 * 전역 설정 클래스.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class GeneralConfig implements Initializable<Void> {
    @Getter
    private static final GeneralConfig instance = new GeneralConfig();

    /** Yaml 파일 관리 인스턴스 */
    private final YamlFile yamlFile = new YamlFile(Paths.get("config.yml"));
    /** 일반 설정 */
    @Nullable
    private Config config;
    /** 전투 시스템 관련 설정 */
    @Nullable
    private CombatConfig combatConfig;
    /** 게임 관련 설정 */
    @Nullable
    private GameConfig gameConfig;

    @NonNull
    public static Config getConfig() {
        instance.validate();

        if (instance.config == null)
            instance.config = new Config();

        return instance.config;
    }

    @NonNull
    public static CombatConfig getCombatConfig() {
        instance.validate();

        if (instance.combatConfig == null)
            instance.combatConfig = new CombatConfig();

        return instance.combatConfig;
    }

    @NonNull
    public static GameConfig getGameConfig() {
        instance.validate();

        if (instance.gameConfig == null)
            instance.gameConfig = new GameConfig();

        return instance.gameConfig;
    }

    @Override
    @NonNull
    public AsyncTask<Void> init() {
        return yamlFile.init()
                .onFinish(() -> ConsoleLogger.info("전역 설정 불러오기 완료"))
                .onError(ex -> ConsoleLogger.severe("전역 설정 불러오기 실패", ex));
    }

    @Override
    public boolean isInitialized() {
        return yamlFile.isInitialized();
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @Getter
    public static final class Config {
        /** Yaml 섹션 인스턴스 */
        private static final YamlFile.Section section = instance.yamlFile.getDefaultSection().getSection("default");
        /** Yaml 로비 위치 섹션 인스턴스 */
        private static final YamlFile.Section lobbySection = section.getSection("lobby_location");

        /** 리소스팩 URL */
        @NonNull
        private final String resourcePackUrl = section.getEntry("resource_pack_url", "").get();
        /** 리소스팩 적용 시간 제한 */
        @NonNull
        private final Timespan resourcePackTimeout = Timespan.ofSeconds(section.getEntry("resource_pack_timeout_seconds", 8.0).get());
        /** 채팅 쿨타임 */
        @NonNull
        private final Timespan chatCooldown = Timespan.ofSeconds(section.getEntry("chat_cooldown_seconds", 0.0).get());
        /** 명령어 쿨타임 */
        @NonNull
        private final Timespan commandCooldown = Timespan.ofSeconds(section.getEntry("command_cooldown_seconds", 0.0).get());
        /** 랭킹 업데이트 주기 */
        @NonNull
        private final Timespan rankingUpdatePeriod = Timespan.ofMinutes(section.getEntry("ranking_update_period_minutes", 5.0).get());
        /** 네더라이트({@link Tier#NETHERITE}) 티어가 되기 위한 최소 순위 */
        private final int netheriteTierMinRank = section.getEntry("netherite_tier_min_rank", 5).get();
        /** 메시지의 접두사 */
        @NonNull
        private final String messagePrefix = section.getEntry("message_prefix", "§3§l[ §bＤＭＧＲ §3§l] §f").get();
        /** 관리자 연락처 */
        @NonNull
        private final String adminContact = section.getEntry("admin_contact", "").get();
        /** 로비 위치 */
        private final Location lobbyLocation = new Location(
                DMGR.getDefaultWorld(),
                lobbySection.getEntry("x", 0.0).get(),
                lobbySection.getEntry("y", 0.0).get(),
                lobbySection.getEntry("z", 0.0).get(),
                lobbySection.getEntry("yaw", 0F).get(),
                lobbySection.getEntry("pitch", 0F).get()
        );

        /**
         * @return 로비 위치
         */
        @NonNull
        public Location getLobbyLocation() {
            return lobbyLocation.clone();
        }
    }

    /**
     * 전투 시스템 관련 설정.
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @Getter
    public static final class CombatConfig {
        /** Yaml 섹션 인스턴스 */
        private static final YamlFile.Section section = instance.yamlFile.getDefaultSection().getSection("combat");

        /** 초당 궁극기 충전량 */
        private final int idleUltChargePerSecond = section.getEntry("idle_ult_charge_per_second", 10).get();
        /** 기본 이동속도 */
        private final double defaultSpeed = section.getEntry("default_speed", 0.12).get();
        /** 리스폰 시간 */
        @NonNull
        private final Timespan respawnTime = Timespan.ofSeconds(section.getEntry("respawn_time_seconds", 10.0).get());
        /** 힐 팩에 사용되는 블록의 타입 */
        @NonNull
        private final Material healPackBlock = Material.valueOf(section.getEntry("heal_pack_block", Material.NETHERRACK.toString()).get());
        /** 힐 팩 쿨타임 */
        @NonNull
        private final Timespan healPackCooldown = Timespan.ofSeconds(section.getEntry("heal_pack_cooldown_seconds", 15.0).get());
        /** 힐 팩 회복량 */
        private final int healPackHeal = section.getEntry("heal_pack_heal", 350).get();
        /** 궁극기 팩에 사용되는 블록의 타입 */
        @NonNull
        private final Material ultPackBlock = Material.valueOf(section.getEntry("ult_pack_block", Material.QUARTZ_ORE.toString()).get());
        /** 궁극기 팩 쿨타임 */
        @NonNull
        private final Timespan ultPackCooldown = Timespan.ofSeconds(section.getEntry("ult_pack_cooldown_seconds", 120.0).get());
        /** 궁극기 팩 충전량 */
        private final int ultPackCharge = section.getEntry("ult_pack_charge", 1000).get();
        /** 점프대에 사용되는 블록의 타입 */
        @NonNull
        private final Material jumpPadBlock = Material.valueOf(section.getEntry("jump_pad_block", Material.SPONGE.toString()).get());
        /** 점프대 사용 시 속력 */
        private final double jumpPadVelocity = section.getEntry("jump_pad_velocity", 1.4).get();
        /** 낙사 구역에 사용되는 블록의 타입 */
        @NonNull
        private final Material fallZoneBlock = Material.valueOf(section.getEntry("fall_zone_block", Material.BEDROCK.toString()).get());
        /** 적 처치 기여 (데미지 누적) 제한시간 */
        @NonNull
        private final Timespan damageSumTimeLimit = Timespan.ofSeconds(section.getEntry("damage_sum_time_limit_seconds", 10.0).get());
        /** 연속 처치 제한시간 */
        @NonNull
        private final Timespan killStreakTimeLimit = Timespan.ofSeconds(section.getEntry("kill_streak_time_limit_seconds", 8.0).get());
        /** 획득 점수 표시 유지시간 */
        @NonNull
        private final Timespan scoreDisplayDuration = Timespan.ofSeconds(section.getEntry("score_display_duration_seconds", 5.0).get());
        /** 킬 로그 표시 유지시간 */
        @NonNull
        private final Timespan killLogDisplayDuration = Timespan.ofSeconds(section.getEntry("kill_log_display_duration_seconds", 4.0).get());
    }

    /**
     * 게임 관련 설정.
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @Getter
    public static final class GameConfig {
        /** Yaml 섹션 인스턴스 */
        private static final YamlFile.Section section = instance.yamlFile.getDefaultSection().getSection("game");

        /** 일반 게임과 랭크 게임의 최대 방 갯수 */
        private final int maxRoomCount = section.getEntry("max_room_count", 5).get();
        /** 게임을 시작하기 위한 최소 인원 수 (일반) */
        private final int normalMinPlayerCount = section.getEntry("normal_min_player_count", 2).get();
        /** 최대 수용 가능한 인원 수 (일반) */
        private final int normalMaxPlayerCount = section.getEntry("normal_max_player_count", 10).get();
        /** 게임을 시작하기 위한 최소 인원 수 (랭크) */
        private final int rankMinPlayerCount = section.getEntry("rank_min_player_count", 6).get();
        /** 최대 수용 가능한 인원 수 (랭크) */
        private final int rankMaxPlayerCount = section.getEntry("rank_max_player_count", 12).get();
        /** 랭크가 결정되는 배치 판 수 */
        private final int rankPlacementPlayCount = section.getEntry("rank_placement_play_count", 5).get();
        /** 게임 시작까지 필요한 대기 시간 */
        @NonNull
        private final Timespan waitingTime = Timespan.ofSeconds(section.getEntry("waiting_time_seconds", 30.0).get());
        /** 게임 시작 후 탭리스트에 플레이어의 전투원이 공개될 때 까지의 시간 */
        @NonNull
        private final Timespan headRevealTimeAfterStart = Timespan.ofSeconds(section.getEntry("head_reveal_time_after_start_seconds", 20.0).get());
        /** 스폰 지역 확인 Y 좌표 */
        private final int spawnRegionCheckYCoordinate = section.getEntry("spawn_region_check_y_coordinate", 41).get();
        /** 팀 스폰 입장 시 초당 회복량 */
        private final int teamSpawnHealPerSecond = section.getEntry("team_spawn_heal_per_second", 500).get();
        /** 상대 팀 스폰 입장 시 초당 피해량 */
        private final int oppositeSpawnDamagePerSecond = section.getEntry("opposite_spawn_damage_per_second", 250).get();
        /** 궁극기 팩 활성화 대기 시간 */
        @NonNull
        private final Timespan ultPackActivationTime = Timespan.ofSeconds(section.getEntry("ult_pack_activation_time_seconds", 60.0).get());
        /** 예상하는 플레이어의 평균 랭크 점수 */
        private final int expectedAverageRankRate = section.getEntry("expected_average_rank_rate", 400).get();
        /** 예상하는 K/DA 평균 */
        private final double expectedAverageKDARatio = section.getEntry("expected_average_kda_ratio", 2.0).get();
        /** 예상하는 분당 획득 점수의 평균 */
        private final int expectedAverageScorePerMinute = section.getEntry("expected_average_score_per_minute", 100).get();
        /** 배치로 얻을 수 있는 최대 랭크 점수 */
        private final int maxPlacementRankRate = section.getEntry("max_placement_rank_rate", Tier.EMERALD.getMinScore() - 1).get();
        /** MMR 수치에 영향을 미치는 플레이 횟수 */
        private final int mmrPlayCountThreshold = section.getEntry("mmr_play_count_threshold", 25).get();
    }
}
