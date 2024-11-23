package com.dace.dmgr;

import com.dace.dmgr.game.Tier;
import com.dace.dmgr.util.task.AsyncTask;
import com.dace.dmgr.util.task.Initializable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
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
        private final YamlFile.Section section = instance.yamlFile.getDefaultSection().getSection("default");

        /** 리소스팩 URL */
        @NonNull
        private final String resourcePackUrl = section.getEntry("resourcePackUrl", "").get();
        /** 리소스팩 적용 시간 제한 (초) */
        private final int resourcePackTimeout = section.getEntry("resourcePackTimeout", 8).get();
        /** 채팅 쿨타임 (tick) */
        private final long chatCooldown = section.getEntry("chatCooldown", 0L).get();
        /** 명령어 쿨타임 (tick) */
        private final long commandCooldown = section.getEntry("commandCooldown", 0L).get();
        /** 랭킹 업데이트 주기 (분) */
        private final int rankingUpdatePeriodMinutes = section.getEntry("rankingUpdatePeriodMinutes", 5).get();
        /** 네더라이트({@link Tier#NETHERITE}) 티어가 되기 위한 최소 순위 */
        private final int netheriteTierMinRank = section.getEntry("netheriteTierMinRank", 5).get();
        /** 메시지의 접두사 */
        @NonNull
        private final String messagePrefix = section.getEntry("messagePrefix", "§3§l[ §bＤＭＧＲ §3§l] §f").get();
        /** 관리자 연락처 */
        @NonNull
        private final String adminContact = section.getEntry("adminContact", "디스코드 dkdace (DarkDace＃4671)").get();
    }

    /**
     * 전투 시스템 관련 설정.
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @Getter
    public static final class CombatConfig {
        /** Yaml 섹션 인스턴스 */
        private final YamlFile.Section section = instance.yamlFile.getDefaultSection().getSection("combat");

        /** 초당 궁극기 충전량 */
        private final int idleUltChargePerSecond = section.getEntry("idleUltChargePerSecond", 10).get();
        /** 기본 이동속도 */
        private final double defaultSpeed = section.getEntry("defaultSpeed", 0.12).get();
        /** 리스폰 시간 (tick) */
        private final long respawnTime = section.getEntry("respawnTime", 200L).get();
        /** 힐 팩에 사용되는 블록의 타입 */
        @NonNull
        private final Material healPackBlock = Material.valueOf(section.getEntry("healPackBlock", Material.NETHERRACK.toString()).get());
        /** 힐 팩 쿨타임 (tick) */
        private final long healPackCooldown = section.getEntry("healPackCooldown", 15 * 20L).get();
        /** 힐 팩 회복량 */
        private final int healPackHeal = section.getEntry("healPackHeal", 350).get();
        /** 궁극기 팩에 사용되는 블록의 타입 */
        @NonNull
        private final Material ultPackBlock = Material.valueOf(section.getEntry("ultPackBlock", Material.QUARTZ_ORE.toString()).get());
        /** 궁극기 팩 쿨타임 (tick) */
        private final long ultPackCooldown = section.getEntry("ultPackCooldown", 120 * 20L).get();
        /** 궁극기 팩 충전량 */
        private final int ultPackCharge = section.getEntry("ultPackCharge", 1000).get();
        /** 점프대에 사용되는 블록의 타입 */
        @NonNull
        private final Material jumpPadBlock = Material.valueOf(section.getEntry("jumpPadBlock", Material.SPONGE.toString()).get());
        /** 점프대 사용 시 속력 */
        private final double jumpPadVelocity = section.getEntry("jumpPadVelocity", 1.4).get();
        /** 낙사 구역에 사용되는 블록의 타입 */
        @NonNull
        private final Material fallZoneBlock = Material.valueOf(section.getEntry("fallZoneBlock", Material.BEDROCK.toString()).get());
        /** 적 처치 기여 (데미지 누적) 제한시간 (tick) */
        private final long damageSumTimeLimit = section.getEntry("damageSumTimeLimit", 10 * 20L).get();
        /** 연속 처치 제한시간 (tick) */
        private final long killStreakTimeLimit = section.getEntry("killStreakTimeLimit", 8 * 20L).get();
        /** 획득 점수 표시 유지시간 (tick) */
        private final long scoreDisplayDuration = section.getEntry("scoreDisplayDuration", 5 * 20L).get();
        /** 킬 로그 표시 유지시간 (tick) */
        private final long killLogDisplayDuration = section.getEntry("killLogDisplayDuration", 4 * 20L).get();
    }

    /**
     * 게임 관련 설정.
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @Getter
    public static final class GameConfig {
        /** Yaml 섹션 인스턴스 */
        private final YamlFile.Section section = instance.yamlFile.getDefaultSection().getSection("game");

        /** 일반 게임과 랭크 게임의 최대 방 갯수 */
        private final int maxRoomCount = section.getEntry("maxRoomCount", 5).get();
        /** 게임을 시작하기 위한 최소 인원 수 (일반) */
        private final int normalMinPlayerCount = section.getEntry("normalMinPlayerCount", 2).get();
        /** 최대 수용 가능한 인원 수 (일반) */
        private final int normalMaxPlayerCount = section.getEntry("normalMaxPlayerCount", 10).get();
        /** 게임을 시작하기 위한 최소 인원 수 (랭크) */
        private final int rankMinPlayerCount = section.getEntry("rankMinPlayerCount", 6).get();
        /** 최대 수용 가능한 인원 수 (랭크) */
        private final int rankMaxPlayerCount = section.getEntry("rankMaxPlayerCount", 12).get();
        /** 랭크가 결정되는 배치 판 수 */
        private final int rankPlacementPlayCount = section.getEntry("rankPlacementPlayCount", 5).get();
        /** 게임 시작까지 필요한 대기 시간 (초) */
        private final int waitingTimeSeconds = section.getEntry("waitingTimeSeconds", 30).get();
        /** 게임 시작 후 탭리스트에 플레이어의 전투원이 공개될 때 까지의 시간 (초) */
        private final int headRevealTimeAfterStartSeconds = section.getEntry("headRevealTimeAfterStartSeconds", 20).get();
        /** 스폰 지역 확인 Y 좌표 */
        private final int spawnRegionCheckYCoordinate = section.getEntry("spawnRegionCheckYCoordinate", 41).get();
        /** 팀 스폰 입장 시 초당 회복량 */
        private final int teamSpawnHealPerSecond = section.getEntry("teamSpawnHealPerSecond", 500).get();
        /** 상대 팀 스폰 입장 시 초당 피해량 */
        private final int oppositeSpawnDamagePerSecond = section.getEntry("oppositeSpawnDamagePerSecond", 250).get();
        /** 궁극기 팩 활성화 대기 시간 (초) */
        private final int ultPackActivationSeconds = section.getEntry("ultPackActivationSeconds", 60).get();
        /** 예상하는 플레이어의 평균 랭크 점수 */
        private final int expectedAverageRankRate = section.getEntry("expectedAverageRankRate", 400).get();
        /** 예상하는 K/DA 평균 */
        private final double expectedAverageKDARatio = section.getEntry("expectedAverageKDARatio", 2.0).get();
        /** 예상하는 분당 획득 점수의 평균 */
        private final int expectedAverageScorePerMin = section.getEntry("expectedAverageScorePerMin", 100).get();
        /** 배치로 얻을 수 있는 최대 랭크 점수 */
        private final int maxPlacementRankRate = section.getEntry("maxPlacementRankRate", Tier.EMERALD.getMinScore() - 1).get();
        /** MMR 수치에 영향을 미치는 플레이 횟수 */
        private final int mmrPlayCountThreshold = section.getEntry("mmrPlayCountThreshold", 25).get();
    }
}
