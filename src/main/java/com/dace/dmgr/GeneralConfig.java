package com.dace.dmgr;

import com.dace.dmgr.game.Tier;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bukkit.Material;

/**
 * 전역 설정 클래스.
 */
public final class GeneralConfig extends YamlFile {
    @Getter
    private static final GeneralConfig instance = new GeneralConfig();
    /** 일반 설정 */
    @Getter
    private static final Config config = new Config();
    /** 전투 시스템 관련 설정 */
    @Getter
    private static final CombatConfig combatConfig = new CombatConfig();
    /** 게임 관련 설정 */
    @Getter
    private static final GameConfig gameConfig = new GameConfig();

    private GeneralConfig() {
        super("config", true);
    }

    @Override
    protected void onInitFinish() {
        config.resourcePackUrl = getString("resourcePackUrl", config.resourcePackUrl);
        config.chatCooldown = getLong("chatCooldown", config.chatCooldown);
        config.commandCooldown = getLong("commandCooldown", config.commandCooldown);
        config.rankingUpdatePeriodMinutes = (int) getLong("rankingUpdatePeriod", config.rankingUpdatePeriodMinutes);
        config.netheriteTierMinRank = (int) getLong("netheriteTierMinRank", config.netheriteTierMinRank);
        config.messagePrefix = getString("messagePrefix", config.messagePrefix);
        config.adminContact = getString("adminContact", config.adminContact);

        combatConfig.idleUltChargePerSecond = (int) getLong("idleUltChargePerSecond", combatConfig.idleUltChargePerSecond);
        combatConfig.respawnTime = getLong("respawnTime", combatConfig.respawnTime);
        combatConfig.healPackBlock = Material.valueOf(getString("healPackBlock", combatConfig.healPackBlock.toString()));
        combatConfig.healPackCooldown = getLong("healPackCooldown", combatConfig.healPackCooldown);
        combatConfig.healPackHeal = (int) getLong("healPackHeal", combatConfig.healPackHeal);
        combatConfig.jumpPadBlock = Material.valueOf(getString("jumpPadBlock", combatConfig.jumpPadBlock.toString()));
        combatConfig.jumpPadVelocity = getDouble("jumpPadVelocity", combatConfig.jumpPadVelocity);
        combatConfig.fallZoneBlock = Material.valueOf(getString("fallZoneBlock", combatConfig.fallZoneBlock.toString()));

        gameConfig.maxRoomCount = (int) getLong("maxRoomCount", gameConfig.maxRoomCount);
        gameConfig.normalMinPlayerCount = (int) getLong("normalMinPlayerCount", gameConfig.normalMinPlayerCount);
        gameConfig.normalMaxPlayerCount = (int) getLong("normalMaxPlayerCount", gameConfig.normalMaxPlayerCount);
        gameConfig.rankMinPlayerCount = (int) getLong("rankMinPlayerCount", gameConfig.rankMinPlayerCount);
        gameConfig.rankMaxPlayerCount = (int) getLong("rankMaxPlayerCount", gameConfig.rankMaxPlayerCount);
        gameConfig.rankPlacementPlayCount = (int) getLong("rankPlacementPlayCount", gameConfig.rankPlacementPlayCount);
        gameConfig.waitingTimeSeconds = (int) getLong("waitingTime", gameConfig.waitingTimeSeconds);
        gameConfig.teamSpawnHealPerSecond = (int) getLong("teamSpawnHealPerSecond", gameConfig.teamSpawnHealPerSecond);
        gameConfig.oppositeSpawnDamagePerSecond = (int) getLong("oppositeSpawnDamagePerSecond", gameConfig.oppositeSpawnDamagePerSecond);
        gameConfig.expectedAverageRankRate = (int) getLong("expectedAverageKDARatio", gameConfig.expectedAverageRankRate);
        gameConfig.expectedAverageKDARatio = getDouble("expectedAverageKDARatio", gameConfig.expectedAverageKDARatio);
        gameConfig.expectedAverageScorePerMin = (int) getLong("expectedAverageScorePerMin", gameConfig.expectedAverageScorePerMin);
        gameConfig.maxPlacementRankRate = (int) getLong("maxPlacementRankRate", gameConfig.maxPlacementRankRate);
        gameConfig.mmrPlayCountThreshold = (int) getLong("mmrPlayCountThreshold", gameConfig.mmrPlayCountThreshold);

        ConsoleLogger.info("전역 설정 불러오기 완료");
    }

    @Override
    protected void onInitError(Exception ex) {
        ConsoleLogger.severe("전역 설정 불러오기 실패");
    }

    /**
     * 일반 설정.
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @Getter
    public static final class Config {
        /** 리소스팩 URL */
        private String resourcePackUrl = "";
        /** 채팅 쿨타임 (tick) */
        private long chatCooldown = 0;
        /** 명령어 쿨타임 (tick) */
        private long commandCooldown = 0;
        /** 랭킹 업데이트 주기 (분) */
        private int rankingUpdatePeriodMinutes = 5;
        /** 네더라이트({@link Tier#NETHERITE}) 티어가 되기 위한 최소 순위 */
        private int netheriteTierMinRank = 5;
        /** 메시지의 접두사 */
        private String messagePrefix = "§3§l[ §bＤＭＧＲ §3§l] §f";
        /** 관리자 연락처 */
        private String adminContact = "디스코드 dkdace (DarkDace＃4671)";
    }

    /**
     * 전투 시스템 관련 설정.
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @Getter
    public static final class CombatConfig {
        /** 초당 궁극기 충전량 */
        private int idleUltChargePerSecond = 10;
        /** 리스폰 시간 (tick) */
        private long respawnTime = 200;
        /** 힐 팩에 사용되는 블록의 타입 */
        private Material healPackBlock = Material.NETHERRACK;
        /** 힐 팩 쿨타임 (tick) */
        private long healPackCooldown = 15 * 20;
        /** 힐 팩 회복량 */
        private int healPackHeal = 350;
        /** 점프대에 사용되는 블록의 타입 */
        private Material jumpPadBlock = Material.SPONGE;
        /** 점프대 사용 시 속력 */
        private double jumpPadVelocity = 1.4;
        /** 낙사 구역에 사용되는 블록의 타입 */
        private Material fallZoneBlock = Material.BEDROCK;
    }

    /**
     * 게임 관련 설정.
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @Getter
    public static final class GameConfig {
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
        /** 팀 스폰 입장 시 초당 회복량 */
        private int teamSpawnHealPerSecond = 500;
        /** 상대 팀 스폰 입장 시 초당 피해량 */
        private int oppositeSpawnDamagePerSecond = 250;
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
    }
}
