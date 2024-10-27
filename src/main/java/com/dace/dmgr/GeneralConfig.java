package com.dace.dmgr;

import com.dace.dmgr.game.Tier;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bukkit.Material;

/**
 * 전역 설정 클래스.
 */
public final class GeneralConfig extends YamlFile {
    @Getter
    private static final GeneralConfig instance = new GeneralConfig();
    /** 일반 설정 */
    private final Config config = new Config();
    /** 전투 시스템 관련 설정 */
    private final CombatConfig combatConfig = new CombatConfig();
    /** 게임 관련 설정 */
    private final GameConfig gameConfig = new GameConfig();

    private GeneralConfig() {
        super("config", true);
    }

    @NonNull
    public static Config getConfig() {
        return instance.config;
    }

    @NonNull
    public static CombatConfig getCombatConfig() {
        return instance.combatConfig;
    }

    @NonNull
    public static GameConfig getGameConfig() {
        return instance.gameConfig;
    }

    @Override
    protected void onInitFinish() {
        config.load();
        combatConfig.load();
        gameConfig.load();

        ConsoleLogger.info("전역 설정 불러오기 완료");
    }

    @Override
    protected void onInitError(@NonNull Exception ex) {
        ConsoleLogger.severe("전역 설정 불러오기 실패", ex);
    }

    /**
     * 일반 설정.
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @Getter
    public final class Config {
        /** 섹션 이름 */
        private static final String SECTION = "default";
        /** 리소스팩 URL */
        @NonNull
        private String resourcePackUrl = "";
        /** 리소스팩 적용 시간 제한 (초) */
        private int resourcePackTimeout = 8;
        /** 채팅 쿨타임 (tick) */
        private long chatCooldown = 0;
        /** 명령어 쿨타임 (tick) */
        private long commandCooldown = 0;
        /** 랭킹 업데이트 주기 (분) */
        private int rankingUpdatePeriodMinutes = 5;
        /** 네더라이트({@link Tier#NETHERITE}) 티어가 되기 위한 최소 순위 */
        private int netheriteTierMinRank = 5;
        /** 메시지의 접두사 */
        @NonNull
        private String messagePrefix = "§3§l[ §bＤＭＧＲ §3§l] §f";
        /** 관리자 연락처 */
        @NonNull
        private String adminContact = "디스코드 dkdace (DarkDace＃4671)";

        /**
         * 데이터를 불러온다.
         */
        private void load() {
            resourcePackUrl = getString(SECTION + ".resourcePackUrl", resourcePackUrl);
            resourcePackTimeout = (int) getLong(SECTION + ".resourcePackTimeout", resourcePackTimeout);
            chatCooldown = getLong(SECTION + ".chatCooldown", chatCooldown);
            commandCooldown = getLong(SECTION + ".commandCooldown", commandCooldown);
            rankingUpdatePeriodMinutes = (int) getLong(SECTION + ".rankingUpdatePeriodMinutes", rankingUpdatePeriodMinutes);
            netheriteTierMinRank = (int) getLong(SECTION + ".netheriteTierMinRank", netheriteTierMinRank);
            messagePrefix = getString(SECTION + ".messagePrefix", messagePrefix);
            adminContact = getString(SECTION + ".adminContact", adminContact);
        }
    }

    /**
     * 전투 시스템 관련 설정.
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @Getter
    public final class CombatConfig {
        /** 섹션 이름 */
        private static final String SECTION = "combat";
        /** 초당 궁극기 충전량 */
        private int idleUltChargePerSecond = 10;
        /** 기본 이동속도 */
        private double defaultSpeed = 0.12;
        /** 리스폰 시간 (tick) */
        private long respawnTime = 200;
        /** 힐 팩에 사용되는 블록의 타입 */
        @NonNull
        private Material healPackBlock = Material.NETHERRACK;
        /** 힐 팩 쿨타임 (tick) */
        private long healPackCooldown = 15 * 20;
        /** 힐 팩 회복량 */
        private int healPackHeal = 350;
        /** 궁극기 팩에 사용되는 블록의 타입 */
        @NonNull
        private Material ultPackBlock = Material.QUARTZ_ORE;
        /** 궁극기 팩 쿨타임 (tick) */
        private long ultPackCooldown = 120 * 20;
        /** 궁극기 팩 충전량 */
        private long ultPackCharge = 1000;
        /** 점프대에 사용되는 블록의 타입 */
        @NonNull
        private Material jumpPadBlock = Material.SPONGE;
        /** 점프대 사용 시 속력 */
        private double jumpPadVelocity = 1.4;
        /** 낙사 구역에 사용되는 블록의 타입 */
        @NonNull
        private Material fallZoneBlock = Material.BEDROCK;
        /** 적 처치 기여 (데미지 누적) 제한시간 (tick) */
        private long damageSumTimeLimit = 10 * 20;
        /** 연속 처치 제한시간 (tick) */
        private long killStreakTimeLimit = 8 * 20;
        /** 획득 점수 표시 유지시간 (tick) */
        private long scoreDisplayDuration = 5 * 20;
        /** 킬 로그 표시 유지시간 (tick) */
        private long killLogDisplayDuration = 4 * 20;

        /**
         * 데이터를 불러온다.
         */
        private void load() {
            idleUltChargePerSecond = (int) getLong(SECTION + ".idleUltChargePerSecond", idleUltChargePerSecond);
            defaultSpeed = getDouble(SECTION + ".defaultSpeed", defaultSpeed);
            respawnTime = getLong(SECTION + ".respawnTime", respawnTime);
            healPackBlock = Material.valueOf(getString(SECTION + ".healPackBlock", healPackBlock.toString()));
            healPackCooldown = getLong(SECTION + ".healPackCooldown", healPackCooldown);
            healPackHeal = (int) getLong(SECTION + ".healPackHeal", healPackHeal);
            ultPackBlock = Material.valueOf(getString(SECTION + ".ultPackBlock", ultPackBlock.toString()));
            ultPackCooldown = getLong(SECTION + ".ultPackCooldown", ultPackCooldown);
            ultPackCharge = (int) getLong(SECTION + ".ultPackCharge", ultPackCharge);
            jumpPadBlock = Material.valueOf(getString(SECTION + ".jumpPadBlock", jumpPadBlock.toString()));
            jumpPadVelocity = getDouble(SECTION + ".jumpPadVelocity", jumpPadVelocity);
            fallZoneBlock = Material.valueOf(getString(SECTION + ".fallZoneBlock", fallZoneBlock.toString()));
            damageSumTimeLimit = getLong(SECTION + ".damageSumTimeLimit", damageSumTimeLimit);
            killStreakTimeLimit = getLong(SECTION + ".killStreakTimeLimit", killStreakTimeLimit);
            scoreDisplayDuration = getLong(SECTION + ".scoreDisplayDuration", scoreDisplayDuration);
            killLogDisplayDuration = getLong(SECTION + ".killLogDisplayDuration", killLogDisplayDuration);
        }
    }

    /**
     * 게임 관련 설정.
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @Getter
    public final class GameConfig {
        /** 섹션 이름 */
        private static final String SECTION = "game";
        /** 일반 게임과 랭크 게임의 최대 방 갯수 */
        private int maxRoomCount = 5;
        /** 게임을 시작하기 위한 최소 인원 수 (일반) */
        private int normalMinPlayerCount = 2;
        /** 최대 수용 가능한 인원 수 (일반) */
        private int normalMaxPlayerCount = 10;
        /** 게임을 시작하기 위한 최소 인원 수 (랭크) */
        private int rankMinPlayerCount = 6;
        /** 최대 수용 가능한 인원 수 (랭크) */
        private int rankMaxPlayerCount = 12;
        /** 랭크가 결정되는 배치 판 수 */
        private int rankPlacementPlayCount = 5;
        /** 게임 시작까지 필요한 대기 시간 (초) */
        private int waitingTimeSeconds = 30;
        /** 게임 시작 후 탭리스트에 플레이어의 전투원이 공개될 때 까지의 시간 (초) */
        private int headRevealTimeAfterStartSeconds = 20;
        /** 스폰 지역 확인 Y 좌표 */
        private int spawnRegionCheckYCoordinate = 41;
        /** 팀 스폰 입장 시 초당 회복량 */
        private int teamSpawnHealPerSecond = 500;
        /** 상대 팀 스폰 입장 시 초당 피해량 */
        private int oppositeSpawnDamagePerSecond = 250;
        /** 궁극기 팩 활성화 대기 시간 (초) */
        private int ultPackActivationSeconds = 60;
        /** 예상하는 플레이어의 평균 랭크 점수 */
        private int expectedAverageRankRate = 400;
        /** 예상하는 K/DA 평균 */
        private double expectedAverageKDARatio = 2;
        /** 예상하는 분당 획득 점수의 평균 */
        private int expectedAverageScorePerMin = 100;
        /** 배치로 얻을 수 있는 최대 랭크 점수 */
        private int maxPlacementRankRate = Tier.EMERALD.getMinScore() - 1;
        /** MMR 수치에 영향을 미치는 플레이 횟수 */
        private int mmrPlayCountThreshold = 25;

        /**
         * 데이터를 불러온다.
         */
        private void load() {
            maxRoomCount = (int) getLong(SECTION + ".maxRoomCount", maxRoomCount);
            normalMinPlayerCount = (int) getLong(SECTION + ".normalMinPlayerCount", normalMinPlayerCount);
            normalMaxPlayerCount = (int) getLong(SECTION + ".normalMaxPlayerCount", normalMaxPlayerCount);
            rankMinPlayerCount = (int) getLong(SECTION + ".rankMinPlayerCount", rankMinPlayerCount);
            rankMaxPlayerCount = (int) getLong(SECTION + ".rankMaxPlayerCount", rankMaxPlayerCount);
            rankPlacementPlayCount = (int) getLong(SECTION + ".rankPlacementPlayCount", rankPlacementPlayCount);
            waitingTimeSeconds = (int) getLong(SECTION + ".waitingTime", waitingTimeSeconds);
            headRevealTimeAfterStartSeconds = (int) getLong(SECTION + ".headRevealTimeAfterStartSeconds", headRevealTimeAfterStartSeconds);
            spawnRegionCheckYCoordinate = (int) getLong(SECTION + ".spawnRegionCheckYCoordinate", spawnRegionCheckYCoordinate);
            teamSpawnHealPerSecond = (int) getLong(SECTION + ".teamSpawnHealPerSecond", teamSpawnHealPerSecond);
            oppositeSpawnDamagePerSecond = (int) getLong(SECTION + ".oppositeSpawnDamagePerSecond", oppositeSpawnDamagePerSecond);
            ultPackActivationSeconds = (int) getLong(SECTION + ".ultPackActivationSeconds", ultPackActivationSeconds);
            expectedAverageRankRate = (int) getLong(SECTION + ".expectedAverageKDARatio", expectedAverageRankRate);
            expectedAverageKDARatio = getDouble(SECTION + ".expectedAverageKDARatio", expectedAverageKDARatio);
            expectedAverageScorePerMin = (int) getLong(SECTION + ".expectedAverageScorePerMin", expectedAverageScorePerMin);
            maxPlacementRankRate = (int) getLong(SECTION + ".maxPlacementRankRate", maxPlacementRankRate);
            mmrPlayCountThreshold = (int) getLong(SECTION + ".mmrPlayCountThreshold", mmrPlayCountThreshold);
        }
    }
}
