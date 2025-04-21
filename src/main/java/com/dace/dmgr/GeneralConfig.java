package com.dace.dmgr;

import com.dace.dmgr.game.Tier;
import com.dace.dmgr.util.task.AsyncTask;
import com.dace.dmgr.util.task.Initializable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.apache.commons.lang3.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Paths;
import java.util.List;

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
    /** 자유 전투 설정 */
    @Nullable
    private FreeCombatConfig freeCombatConfig;
    /** 훈련장 설정 */
    @Nullable
    private TrainingConfig trainingConfig;
    /** 전투 시스템 관련 설정 */
    @Nullable
    private CombatConfig combatConfig;
    /** 게임 관련 설정 */
    @Nullable
    private GameConfig gameConfig;

    /**
     * @return 일반 설정
     */
    @NonNull
    public static Config getConfig() {
        instance.validate();
        return Validate.notNull(instance.config);
    }

    /**
     * @return 자유 전투 설정
     */
    @NonNull
    public static FreeCombatConfig getFreeCombatConfig() {
        instance.validate();
        return Validate.notNull(instance.freeCombatConfig);
    }

    /**
     * @return 훈련장 설정
     */
    @NonNull
    public static TrainingConfig getTrainingConfig() {
        instance.validate();
        return Validate.notNull(instance.trainingConfig);
    }

    /**
     * @return 전투 시스템 관련 설정
     */
    @NonNull
    public static CombatConfig getCombatConfig() {
        instance.validate();
        return Validate.notNull(instance.combatConfig);
    }

    /**
     * @return 게임 관련 설정
     */
    @NonNull
    public static GameConfig getGameConfig() {
        instance.validate();
        return Validate.notNull(instance.gameConfig);
    }

    @NonNull
    private static Location @NonNull [] getLocationsFromGlobalLocationList(@NonNull World world, @NonNull List<GlobalLocation> globalLocations) {
        return globalLocations.stream().map(globalLocation -> globalLocation.toLocation(world)).toArray(Location[]::new);
    }

    @Override
    @NonNull
    public AsyncTask<Void> init() {
        return yamlFile.init()
                .onFinish(() -> {
                    config = new Config();
                    freeCombatConfig = new FreeCombatConfig();
                    trainingConfig = new TrainingConfig();
                    combatConfig = new CombatConfig();
                    gameConfig = new GameConfig();

                    ConsoleLogger.info("전역 설정 불러오기 완료");
                })
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

        /** 리소스팩 URL */
        @NonNull
        private final String resourcePackUrl = section.getEntry("resource_pack_url", "").get();
        /** 리소스팩 적용 시간 제한 */
        @NonNull
        private final Timespan resourcePackTimeout = section.getEntry("resource_pack_timeout", Timespan.ofSeconds(8)).get();
        /** 채팅 쿨타임 */
        @NonNull
        private final Timespan chatCooldown = section.getEntry("chat_cooldown", Timespan.ofSeconds(0)).get();
        /** 명령어 쿨타임 */
        @NonNull
        private final Timespan commandCooldown = section.getEntry("command_cooldown", Timespan.ofSeconds(0)).get();
        /** 랭킹 업데이트 주기 */
        @NonNull
        private final Timespan rankingUpdatePeriod = section.getEntry("ranking_update_period", Timespan.ofMinutes(5)).get();
        /** 네더라이트({@link Tier#NETHERITE}) 티어가 되기 위한 최소 순위 */
        private final int netheriteTierMinRank = section.getEntry("netherite_tier_min_rank", 5).get();
        /** 메시지의 접두사 */
        @NonNull
        private final String messagePrefix = section.getEntry("message_prefix", "§3§l[ §bＤＭＧＲ §3§l] §f").get();
        /** 관리자 연락처 */
        @NonNull
        private final String adminContact = section.getEntry("admin_contact", "").get();
        /** 디스코드 주소 */
        @NonNull
        private final String discord = section.getEntry("discord", "").get();
        /** 마인리스트 주소 */
        @NonNull
        private final String minelist = section.getEntry("minelist", "").get();
        /** 코어 가격 */
        private final int corePrice = section.getEntry("core_price", 0).get();
        /** 월드 */
        @NonNull
        private final World world = Bukkit.getWorld(section.getEntry("world", "DMGR").get());
        /** 로비 위치 */
        private final GlobalLocation lobbyLocation = section.getEntry("lobby_location", GlobalLocation.ZERO).get();

        /**
         * @return 로비 위치
         */
        @NonNull
        public Location getLobbyLocation() {
            return lobbyLocation.toLocation(world);
        }
    }

    /**
     * 자유 전투 관련 설정.
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @Getter
    public static final class FreeCombatConfig {
        /** Yaml 섹션 인스턴스 */
        private static final YamlFile.Section section = instance.yamlFile.getDefaultSection().getSection("free_combat");

        /** 월드 */
        @NonNull
        private final World world = Bukkit.getWorld(section.getEntry("world", "FreeCombat").get());
        /** 대기실 위치 */
        private final GlobalLocation waitLocation = section.getEntry("wait_location", GlobalLocation.ZERO).get();
        /** 대기실 지역 이름 */
        @NonNull
        private final String waitRegionName = section.getEntry("wait_region_name", "BattlePVP").get();
        /** 이동 지역 이름 */
        @NonNull
        private final String warpRegionName = section.getEntry("warp_region_name", "BattlePVPWarp").get();
        /** 스폰 위치 목록 */
        private final List<GlobalLocation> spawnLocations = section.getListEntry("spawn_locations",
                new YamlFile.TypeToken<List<GlobalLocation>>() {
                }).get();

        /**
         * @return 대기실 위치
         */
        @NonNull
        public Location getWaitLocation() {
            return waitLocation.toLocation(world);
        }

        /**
         * @return 스폰 위치 목록
         */
        @NonNull
        public Location @NonNull [] getSpawnLocations() {
            return getLocationsFromGlobalLocationList(world, spawnLocations);
        }
    }

    /**
     * 훈련장 관련 설정.
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @Getter
    public static final class TrainingConfig {
        /** Yaml 섹션 인스턴스 */
        private static final YamlFile.Section section = instance.yamlFile.getDefaultSection().getSection("training");

        /** 월드 */
        @NonNull
        private final World world = Bukkit.getWorld(section.getEntry("world", "Training").get());
        /** 스폰 위치 */
        private final GlobalLocation spawnLocation = section.getEntry("spawn_location", GlobalLocation.ZERO).get();
        /** 전투원 선택 지역 확인 Y 좌표 */
        private final int selectCharRegionCheckYCoordinate = section.getEntry("select_char_region_check_y_coordinate", 208).get();
        /** 전투원 선택 지역 식별 블록 타입 */
        @NonNull
        private final Material selectCharZoneBlock = section.getEntry("select_char_zone_block", Material.ENDER_PORTAL_FRAME).get();

        /** 기본 더미의 위치 설정 */
        @NonNull
        private final DefaultDummyConfig defaultDummyConfig = new DefaultDummyConfig();
        /** 아레나 설정 */
        @NonNull
        private final ArenaConfig arenaConfig = new ArenaConfig();
        /** 디버프 실험 블록 설정 */
        @NonNull
        private final EffectTestBlockConfig effectTestBlockConfig = new EffectTestBlockConfig();

        /**
         * @return 스폰 위치
         */
        @NonNull
        public Location getSpawnLocation() {
            return spawnLocation.toLocation(world);
        }

        /**
         * 기본 더미의 위치 설정.
         */
        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        @Getter
        public static final class DefaultDummyConfig {
            /** Yaml 섹션 인스턴스 */
            private static final YamlFile.Section section = TrainingConfig.section.getSection("default_dummy");

            /** 리스폰 시간 */
            @NonNull
            private final Timespan respawnTime = section.getEntry("respawn_time", Timespan.ofSeconds(3)).get();
            /** 고정형 더미의 위치 목록 */
            private final List<GlobalLocation> fixedLocations = section.getListEntry("fixed_locations",
                    new YamlFile.TypeToken<List<GlobalLocation>>() {
                    }).get();
            /** 고정형 중량 더미의 위치 목록 */
            private final List<GlobalLocation> fixedHeavyLocations = section.getListEntry("fixed_heavy_locations",
                    new YamlFile.TypeToken<List<GlobalLocation>>() {
                    }).get();
            /** 이동형 더미의 위치 목록 */
            private final List<List<GlobalLocation>> movingLocations = section.getListEntry("moving_locations",
                    new YamlFile.TypeToken<List<List<GlobalLocation>>>() {
                    }).get();
            /** 공격형 적 더미의 위치 목록 */
            private final List<GlobalLocation> shootingEnemyLocations = section.getListEntry("shooting_enemy_locations",
                    new YamlFile.TypeToken<List<GlobalLocation>>() {
                    }).get();
            /** 공격형 아군 더미의 위치 목록 */
            private final List<GlobalLocation> shootingTeamLocations = section.getListEntry("shooting_team_locations",
                    new YamlFile.TypeToken<List<GlobalLocation>>() {
                    }).get();

            /**
             * @return 고정형 더미의 위치 목록
             */
            @NonNull
            public Location @NonNull [] getFixedLocations() {
                return getLocationsFromGlobalLocationList(getTrainingConfig().getWorld(), fixedLocations);
            }

            /**
             * @return 고정형 중량 더미의 위치 목록
             */
            @NonNull
            public Location @NonNull [] getFixedHeavyLocations() {
                return getLocationsFromGlobalLocationList(getTrainingConfig().getWorld(), fixedHeavyLocations);
            }

            /**
             * @return 이동형 더미의 위치 목록
             */
            @NonNull
            public Location @NonNull [] @NonNull [] getMovingLocations() {
                return movingLocations.stream().map(map -> getLocationsFromGlobalLocationList(getTrainingConfig().getWorld(), map))
                        .toArray(Location[][]::new);
            }

            /**
             * @return 공격형 적 더미의 위치 목록
             */
            @NonNull
            public Location @NonNull [] getShootingEnemyLocations() {
                return getLocationsFromGlobalLocationList(getTrainingConfig().getWorld(), shootingEnemyLocations);
            }

            /**
             * @return 공격형 아군 더미의 위치 목록
             */
            @NonNull
            public Location @NonNull [] getShootingTeamLocations() {
                return getLocationsFromGlobalLocationList(getTrainingConfig().getWorld(), shootingTeamLocations);
            }
        }

        /**
         * 아레나 설정.
         */
        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        @Getter
        public static final class ArenaConfig {
            /** Yaml 섹션 인스턴스 */
            private static final YamlFile.Section section = TrainingConfig.section.getSection("arena");

            /** 지역 확인 Y 좌표 */
            private final int regionCheckYCoordinate = section.getEntry("region_check_y_coordinate", 208).get();
            /** 지역 식별 블록 타입 */
            @NonNull
            private final Material zoneBlock = section.getEntry("zone_block", Material.GOLD_ORE).get();
            /** 설정 지역 확인 Y 좌표 */
            private final int optionRegionCheckYCoordinate = section.getEntry("option_region_check_y_coordinate", 209).get();
            /** 설정 지역 식별 블록 타입 */
            @NonNull
            private final Material optionZoneBlock = section.getEntry("option_zone_block", Material.ENCHANTMENT_TABLE).get();
            /** 더미 생성 위치 */
            private final GlobalLocation dummyLocation = section.getEntry("dummy_location", GlobalLocation.ZERO).get();

            /**
             * @return 더미 생성 위치
             */
            @NonNull
            public Location getDummyLocation() {
                return dummyLocation.toLocation(getTrainingConfig().getWorld());
            }
        }

        /**
         * 디버프 실험 블록 설정.
         */
        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        @Getter
        public static final class EffectTestBlockConfig {
            /** Yaml 섹션 인스턴스 */
            private static final YamlFile.Section section = TrainingConfig.section.getSection("effect_test_block");

            /** 블록의 타입 */
            @NonNull
            private final Material block = section.getEntry("block", Material.STRUCTURE_BLOCK).get();
            /** 쿨타임 */
            @NonNull
            private final Timespan cooldown = section.getEntry("cooldown", Timespan.ofSeconds(3)).get();

            /** 기절 위치 */
            private final GlobalLocation stunLocation = section.getEntry("stun_location", GlobalLocation.ZERO).get();
            /** 속박 위치 */
            private final GlobalLocation snareLocation = section.getEntry("snare_location", GlobalLocation.ZERO).get();
            /** 고정 위치 */
            private final GlobalLocation groundingLocation = section.getEntry("grounding_location", GlobalLocation.ZERO).get();
            /** 화염 위치 */
            private final GlobalLocation burningLocation = section.getEntry("burning_location", GlobalLocation.ZERO).get();
            /** 독 위치 */
            private final GlobalLocation poisonLocation = section.getEntry("poison_location", GlobalLocation.ZERO).get();
            /** 회복 차단 위치 */
            private final GlobalLocation healBlockLocation = section.getEntry("heal_block_location", GlobalLocation.ZERO).get();
            /** 침묵 위치 */
            private final GlobalLocation silenceLocation = section.getEntry("silence_location", GlobalLocation.ZERO).get();

            /**
             * @return 기절 위치
             */
            @NonNull
            public Location getStunLocation() {
                return stunLocation.toLocation(getTrainingConfig().getWorld());
            }

            /**
             * @return 속박 위치
             */
            @NonNull
            public Location getSnareLocation() {
                return snareLocation.toLocation(getTrainingConfig().getWorld());
            }

            /**
             * @return 고정 위치
             */
            @NonNull
            public Location getGroundingLocation() {
                return groundingLocation.toLocation(getTrainingConfig().getWorld());
            }

            /**
             * @return 화염 위치
             */
            @NonNull
            public Location getBurningLocation() {
                return burningLocation.toLocation(getTrainingConfig().getWorld());
            }

            /**
             * @return 독 위치
             */
            @NonNull
            public Location getPoisonLocation() {
                return poisonLocation.toLocation(getTrainingConfig().getWorld());
            }

            /**
             * @return 회복 차단 위치
             */
            @NonNull
            public Location getHealBlockLocation() {
                return healBlockLocation.toLocation(getTrainingConfig().getWorld());
            }

            /**
             * @return 침묵 위치
             */
            @NonNull
            public Location getSilenceLocation() {
                return silenceLocation.toLocation(getTrainingConfig().getWorld());
            }
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
        private final Timespan respawnTime = section.getEntry("respawn_time", Timespan.ofSeconds(10)).get();
        /** 힐 팩에 사용되는 블록의 타입 */
        @NonNull
        private final Material healPackBlock = section.getEntry("heal_pack_block", Material.NETHERRACK).get();
        /** 힐 팩 쿨타임 */
        @NonNull
        private final Timespan healPackCooldown = section.getEntry("heal_pack_cooldown", Timespan.ofSeconds(15)).get();
        /** 힐 팩 회복량 */
        private final int healPackHeal = section.getEntry("heal_pack_heal", 350).get();
        /** 궁극기 팩에 사용되는 블록의 타입 */
        @NonNull
        private final Material ultPackBlock = section.getEntry("ult_pack_block", Material.QUARTZ_ORE).get();
        /** 궁극기 팩 쿨타임 */
        @NonNull
        private final Timespan ultPackCooldown = section.getEntry("ult_pack_cooldown", Timespan.ofSeconds(120)).get();
        /** 궁극기 팩 충전량 */
        private final int ultPackCharge = section.getEntry("ult_pack_charge", 1000).get();
        /** 점프대에 사용되는 블록의 타입 */
        @NonNull
        private final Material jumpPadBlock = section.getEntry("jump_pad_block", Material.SPONGE).get();
        /** 점프대 사용 시 속력 */
        private final double jumpPadVelocity = section.getEntry("jump_pad_velocity", 1.4).get();
        /** 낙사 구역에 사용되는 블록의 타입 */
        @NonNull
        private final Material fallZoneBlock = section.getEntry("fall_zone_block", Material.BEDROCK).get();
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
        private final Timespan waitingTime = section.getEntry("waiting_time", Timespan.ofSeconds(30)).get();
        /** 게임 시작 후 탭리스트에 플레이어의 전투원이 공개될 때 까지의 시간 */
        @NonNull
        private final Timespan headRevealTimeAfterStart = section.getEntry("head_reveal_time_after_start", Timespan.ofSeconds(20)).get();
        /** 스폰 지역 확인 Y 좌표 */
        private final int spawnRegionCheckYCoordinate = section.getEntry("spawn_region_check_y_coordinate", 41).get();
        /** 팀 스폰 입장 시 초당 회복량 */
        private final int teamSpawnHealPerSecond = section.getEntry("team_spawn_heal_per_second", 500).get();
        /** 상대 팀 스폰 입장 시 초당 피해량 */
        private final int oppositeSpawnDamagePerSecond = section.getEntry("opposite_spawn_damage_per_second", 250).get();
        /** 레드 팀 스폰 식별 블록 타입 */
        @NonNull
        private final Material redTeamSpawnBlock = section.getEntry("red_team_spawn_block", Material.REDSTONE_ORE).get();
        /** 블루 팀 스폰 식별 블록 타입 */
        @NonNull
        private final Material blueTeamSpawnBlock = section.getEntry("blue_team_spawn_block", Material.LAPIS_ORE).get();
        /** 궁극기 팩 활성화 대기 시간 */
        @NonNull
        private final Timespan ultPackActivationTime = section.getEntry("ult_pack_activation_time", Timespan.ofSeconds(60)).get();
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
