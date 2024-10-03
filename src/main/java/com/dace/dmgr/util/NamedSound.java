package com.dace.dmgr.util;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Sound;

/**
 * 이름이 지정된 일련의 효과음 목록.
 */
@Getter(AccessLevel.PACKAGE)
public enum NamedSound {
    /** 성공 */
    GENERAL_SUCCESS(new DefinedSound("random.good", 1000, 1)),
    /** GUI 클릭 */
    GENERAL_GUI_CLICK(new DefinedSound(Sound.UI_BUTTON_CLICK, 1, 1)),
    /** 경고 액션바 */
    GENERAL_ALERT(new DefinedSound("new.block.note_block.bit", 0.25, 0.7)),
    /** 타자기 효과 타이틀 */
    TYPEWRITER_TITLE(new DefinedSound("new.block.note_block.bass", 1, 1.5)),

    /** 게임 - 타이머 효과음 */
    GAME_TIMER(new DefinedSound(Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1000, 1)),
    /** 게임 - 전투 시작 */
    GAME_ON_PLAY(new DefinedSound(Sound.ENTITY_WITHER_SPAWN, 1000, 1)),
    /** 게임 - 승리 */
    GAME_WIN(new DefinedSound(Sound.UI_TOAST_CHALLENGE_COMPLETE, 1000, 1.5)),
    /** 게임 - 패배 */
    GAME_LOSE(new DefinedSound(Sound.ENTITY_BLAZE_DEATH, 1000, 0.5)),
    /** 게임 - 무승부 */
    GAME_DRAW(new DefinedSound(Sound.ENTITY_PLAYER_LEVELUP, 1000, 1)),

    /** 전투 - 액티브 스킬 준비 */
    COMBAT_ACTIVE_SKILL_READY(new DefinedSound(Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.2, 2)),
    /** 전투 - 궁극기 준비 */
    COMBAT_ULTIMATE_SKILL_READY(new DefinedSound(Sound.ENTITY_PLAYER_LEVELUP, 0.5, 2)),
    /** 전투 - 궁극기 사용 */
    COMBAT_ULTIMATE_SKILL_USE(new DefinedSound(Sound.ENTITY_WITHER_SPAWN, 1000, 2)),
    /** 전투 - 힐 팩 사용 */
    COMBAT_USE_HEAL_PACK(new DefinedSound(Sound.ENTITY_ZOMBIE_VILLAGER_CONVERTED, 0.5, 1.2)),
    /** 전투 - 점프대 사용 */
    COMBAT_USE_JUMP_PAD(
            new DefinedSound(Sound.ENTITY_PLAYER_SMALL_FALL, 1.5, 1.5, 0.1),
            new DefinedSound(Sound.ENTITY_ITEM_PICKUP, 1.5, 0.8, 0.05),
            new DefinedSound(Sound.ENTITY_ITEM_PICKUP, 1.5, 1.4, 0.05)
    ),
    /** 전투 - 추락 (낮음) */
    COMBAT_FALL_LOW(new DefinedSound(Sound.BLOCK_STONE_STEP, 0.3, 0.9, 0.1)),
    /** 전투 - 추락 (중간) */
    COMBAT_FALL_MID(
            new DefinedSound(Sound.BLOCK_STONE_STEP, 0.4, 0.9, 0.1),
            new DefinedSound(Sound.ENTITY_PLAYER_SMALL_FALL, 0.4, 0.9, 0.1)
    ),
    /** 전투 - 추락 (높음) */
    COMBAT_FALL_HIGH(
            new DefinedSound(Sound.BLOCK_STONE_STEP, 0.5, 0.8, 0.1),
            new DefinedSound(Sound.BLOCK_STONE_STEP, 0.5, 0.9, 0.1),
            new DefinedSound(Sound.ENTITY_PLAYER_SMALL_FALL, 0.5, 0.9, 0.1)
    ),
    /** 전투 - 공격 성공 */
    COMBAT_ATTACK(
            new DefinedSound(Sound.ENTITY_PLAYER_HURT, 0.8, 1.4),
            new DefinedSound(Sound.ENTITY_PLAYER_BIG_FALL, 1, 0.7)
    ),
    /** 전투 - 공격 성공 (치명타) */
    COMBAT_ATTACK_CRIT(
            new DefinedSound(Sound.ENTITY_PLAYER_BIG_FALL, 2, 0.7),
            new DefinedSound(Sound.BLOCK_ANVIL_PLACE, 0.5, 1.8)
    ),
    /** 전투 - 처치 */
    COMBAT_KILL(
            new DefinedSound(Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 2, 1.25),
            new DefinedSound(Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1.25)
    ),
    /** 전투 - 피해 (화염) */
    COMBAT_DAMAGE_BURNING(new DefinedSound(Sound.ENTITY_PLAYER_HURT_ON_FIRE, 0.7, 1, 0.1)),
    /** 전투 - 근접 공격 - 사용 */
    COMBAT_MELEE_ATTACK_USE(new DefinedSound(Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.6, 1.1, 0.1)),
    /** 전투 - 근접 공격 - 블록 타격 */
    COMBAT_MELEE_ATTACK_HIT_BLOCK(new DefinedSound(Sound.ENTITY_PLAYER_ATTACK_WEAK, 1, 0.9, 0.05)),
    /** 전투 - 근접 공격 - 엔티티 타격 */
    COMBAT_MELEE_ATTACK_HIT_ENTITY(
            new DefinedSound(Sound.ENTITY_PLAYER_ATTACK_KNOCKBACK, 1, 1.1, 0.1),
            new DefinedSound(Sound.ENTITY_PLAYER_ATTACK_KNOCKBACK, 1, 1.1, 0.1)
    ),
    /** 전투 - 총알 - 블록 타격 */
    COMBAT_GUN_HIT_BLOCK(new DefinedSound("random.gun.ricochet", 0.8, 0.975, 0.05)),
    /** 전투 - 총기 탄피 */
    COMBAT_GUN_SHELL_DROP(new DefinedSound(Sound.ENTITY_MAGMACUBE_JUMP, 0.8, 1, 0.1)),
    /** 전투 - 산탄총 탄피 */
    COMBAT_SHOTGUN_SHELL_DROP(new DefinedSound(Sound.ENTITY_ZOMBIE_HORSE_DEATH, 1, 1, 0.1)),
    /** 전투 - 엔티티 소환 */
    COMBAT_ENTITY_SUMMON(new DefinedSound(Sound.ENTITY_ZOMBIE_VILLAGER_CONVERTED, 0.8, 1)),
    /** 전투 - 투척 */
    COMBAT_THROW(new DefinedSound(Sound.ENTITY_WITCH_THROW, 0.8, 0.8)),
    /** 전투 - 투척물 튕김 */
    COMBAT_THROW_BOUNCE(
            new DefinedSound("random.metalhit", 0.1, 1.2, 0.1),
            new DefinedSound(Sound.BLOCK_GLASS_BREAK, 0.1, 2)
    ),
    /** 전투 - '아케이스' 무기 - 사용 */
    COMBAT_ARKACE_WEAPON_USE(
            new DefinedSound("random.gun2.scarlight_1", 3, 1),
            new DefinedSound("random.gun_reverb", 5, 1.2)
    ),
    /** 전투 - '아케이스' 무기 - 사용 (궁극기) */
    COMBAT_ARKACE_WEAPON_USE_ULT(
            new DefinedSound("new.block.beacon.deactivate", 4, 2),
            new DefinedSound("random.energy", 4, 1.6),
            new DefinedSound("random.gun_reverb", 5, 1.2)
    ),
    /** 전투 - '아케이스' 액티브 1번 - 사용 */
    COMBAT_ARKACE_A1_USE(
            new DefinedSound("random.gun.grenade", 3, 1.5),
            new DefinedSound(Sound.ENTITY_SHULKER_SHOOT, 3, 1.2)
    ),
    /** 전투 - '아케이스' 액티브 1번 - 폭발 */
    COMBAT_ARKACE_A1_EXPLODE(
            new DefinedSound(Sound.ENTITY_FIREWORK_LARGE_BLAST, 4, 0.8),
            new DefinedSound(Sound.ENTITY_GENERIC_EXPLODE, 4, 1.4),
            new DefinedSound("random.gun_reverb2", 6, 0.9)
    ),
    /** 전투 - '아케이스' 액티브 2번 - 사용 */
    COMBAT_ARKACE_A2_USE(
            new DefinedSound(Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.5, 0.9),
            new DefinedSound(Sound.ITEM_ARMOR_EQUIP_DIAMOND, 1.5, 1.4),
            new DefinedSound(Sound.ITEM_ARMOR_EQUIP_DIAMOND, 1.5, 1.2)
    ),
    /** 전투 - '예거' 무기 - 사용 */
    COMBAT_JAGER_WEAPON_USE(
            new DefinedSound("random.gun2.m16_1", 0.8, 1.2),
            new DefinedSound(Sound.BLOCK_FIRE_EXTINGUISH, 0.8, 1.7)
    ),
    /** 전투 - '예거' 무기 - 전환 활성화 */
    COMBAT_JAGER_WEAPON_SWAP_ON(new DefinedSound(Sound.ENTITY_WOLF_HOWL, 0.6, 1.9)),
    /** 전투 - '예거' 무기 - 전환 비활성화 */
    COMBAT_JAGER_WEAPON_SWAP_OFF(new DefinedSound(Sound.ENTITY_WOLF_SHAKE, 0.6, 1.9)),
    /** 전투 - '예거' 무기 - 사용 (정조준) */
    COMBAT_JAGER_WEAPON_USE_SCOPE(
            new DefinedSound("random.gun2.psg_1_1", 3.5, 1),
            new DefinedSound("random.gun2.m16_1", 3.5, 1),
            new DefinedSound("random.gun.reverb", 5.5, 0.95)
    ),
    /** 전투 - '예거' 액티브 1번 - 소환 준비 */
    COMBAT_JAGER_A1_SUMMON_READY(new DefinedSound(Sound.ENTITY_WOLF_GROWL, 1, 1)),
    /** 전투 - '예거' 액티브 1번 - 적 감지 */
    COMBAT_JAGER_A1_ENEMY_DETECT(new DefinedSound(Sound.ENTITY_WOLF_GROWL, 2, 0.85)),
    /** 전투 - '예거' 액티브 1번 - 피격 */
    COMBAT_JAGER_A1_DAMAGE(new DefinedSound(Sound.ENTITY_WOLF_HURT, 0.4, 1, 0.1)),
    /** 전투 - '예거' 액티브 1번 - 사망 */
    COMBAT_JAGER_A1_DEATH(new DefinedSound(Sound.ENTITY_WOLF_DEATH, 1, 1, 0.1)),
    /** 전투 - '예거' 액티브 2번 - 사용 */
    COMBAT_JAGER_A2_USE(new DefinedSound(Sound.ENTITY_CAT_PURREOW, 0.5, 1.6)),
    /** 전투 - '예거' 액티브 2번 - 소환 */
    COMBAT_JAGER_A2_SUMMON(
            new DefinedSound(Sound.ENTITY_HORSE_ARMOR, 0.5, 1.6),
            new DefinedSound("random.craft", 0.5, 1.3),
            new DefinedSound(Sound.ENTITY_PLAYER_HURT, 0.5, 0.5)
    ),
    /** 전투 - '예거' 액티브 2번 - 소환 준비 */
    COMBAT_JAGER_A2_SUMMON_READY(new DefinedSound(Sound.ENTITY_PLAYER_HURT, 0.5, 0.5)),
    /** 전투 - '예거' 액티브 2번 - 발동 */
    COMBAT_JAGER_A2_TRIGGER(
            new DefinedSound(Sound.ENTITY_SHEEP_SHEAR, 2, 1.2),
            new DefinedSound("new.entity.player.hurt_sweet_berry_bush", 2, 0.8),
            new DefinedSound("random.metalhit", 2, 1.2)
    ),
    /** 전투 - '예거' 액티브 2번 - 피격 */
    COMBAT_JAGER_A2_DAMAGE(new DefinedSound("random.metalhit", 0.4, 1.1, 0.1)),
    /** 전투 - '예거' 액티브 2번 - 파괴 */
    COMBAT_JAGER_A2_DEATH(
            new DefinedSound(Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 1, 0.8),
            new DefinedSound("random.metalhit", 1, 0.8),
            new DefinedSound(Sound.ENTITY_ITEM_BREAK, 1, 0.8)
    ),
    /** 전투 - '예거' 액티브 3번 - 사용 */
    COMBAT_JAGER_A3_USE(new DefinedSound(Sound.ENTITY_CAT_PURREOW, 0.5, 1.6)),
    /** 전투 - '예거' 액티브 3번 - 사용 준비 */
    COMBAT_JAGER_A3_USE_READY(
            new DefinedSound(Sound.ITEM_FLINTANDSTEEL_USE, 0.8, 1.2),
            new DefinedSound("new.block.chain.place", 0.8, 1.2)
    ),
    /** 전투 - '예거' 액티브 3번 - 폭발 */
    COMBAT_JAGER_A3_EXPLODE(
            new DefinedSound(Sound.ENTITY_FIREWORK_LARGE_BLAST, 4, 0.6),
            new DefinedSound(Sound.ENTITY_GENERIC_EXPLODE, 4, 1.2),
            new DefinedSound(Sound.ENTITY_ZOMBIE_VILLAGER_CURE, 4, 1.5),
            new DefinedSound("random.explosion_reverb", 6, 1.2)
    ),
    /** 전투 - '예거' 궁극기 - 사용 */
    COMBAT_JAGER_ULT_USE(new DefinedSound(Sound.ENTITY_CAT_PURREOW, 0.5, 1.6)),
    /** 전투 - '예거' 궁극기 - 소환 */
    COMBAT_JAGER_ULT_SUMMON(new DefinedSound(Sound.ENTITY_PLAYER_HURT, 0.5, 0.5)),
    /** 전투 - '예거' 궁극기 - 소환 준비 대기 */
    COMBAT_JAGER_ULT_SUMMON_BEFORE_READY(new DefinedSound(Sound.BLOCK_FIRE_EXTINGUISH, 0.8, 1.7)),
    /** 전투 - '예거' 궁극기 - 틱 효과음 */
    COMBAT_JAGER_ULT_TICK(
            new DefinedSound(Sound.ITEM_ELYTRA_FLYING, 3, 1.3, 0.2),
            new DefinedSound(Sound.ITEM_ELYTRA_FLYING, 3, 1.7, 0.2)
    ),
    /** 전투 - '예거' 궁극기 - 피격 */
    COMBAT_JAGER_ULT_DAMAGE(new DefinedSound("random.metalhit", 0.4, 1.1, 0.1)),
    /** 전투 - '예거' 궁극기 - 파괴 */
    COMBAT_JAGER_ULT_DEATH(
            new DefinedSound(Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 2, 0.7),
            new DefinedSound(Sound.ENTITY_ITEM_BREAK, 2, 0.7),
            new DefinedSound(Sound.ENTITY_GENERIC_EXPLODE, 2, 1.2)
    ),
    /** 전투 - '퀘이커' 발소리 */
    COMBAT_QUAKER_FOOTSTEP(
            new DefinedSound(Sound.ENTITY_COW_STEP, 0.3, 0.9, 0.1),
            new DefinedSound("new.entity.ravager.step", 0.2, 0.8, 0.1)
    ),
    /** 전투 - '퀘이커' 무기 - 사용 */
    COMBAT_QUAKER_WEAPON_USE(
            new DefinedSound(Sound.ENTITY_IRONGOLEM_ATTACK, 1, 0.5),
            new DefinedSound("random.gun2.shovel_leftclick", 1, 0.6, 0.1)
    ),
    /** 전투 - '퀘이커' 무기 - 타격 */
    COMBAT_QUAKER_WEAPON_HIT(
            new DefinedSound(Sound.ENTITY_PLAYER_ATTACK_STRONG, 0.8, 0.75, 0.1),
            new DefinedSound(Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 0.6, 0.85, 0.1)
    ),
    /** 전투 - '퀘이커' 무기 - 엔티티 타격 */
    COMBAT_QUAKER_WEAPON_HIT_ENTITY(
            new DefinedSound(Sound.ENTITY_PLAYER_ATTACK_STRONG, 1, 0.9, 0.05),
            new DefinedSound(Sound.ENTITY_PLAYER_ATTACK_CRIT, 1, 1.2, 0.1)
    ),
    /** 전투 - '퀘이커' 액티브 1번 - 사용 */
    COMBAT_QUAKER_A1_USE(
            new DefinedSound(Sound.ENTITY_ENDERDRAGON_FLAP, 1, 0.6),
            new DefinedSound(Sound.BLOCK_SHULKER_BOX_OPEN, 1, 0.7)
    ),
    /** 전투 - '퀘이커' 액티브 1번 - 해제 */
    COMBAT_QUAKER_A1_DISABLE(new DefinedSound(Sound.BLOCK_SHULKER_BOX_CLOSE, 1, 1.4)),
    /** 전투 - '퀘이커' 액티브 1번 - 피격 */
    COMBAT_QUAKER_A1_DAMAGE(
            new DefinedSound(Sound.BLOCK_ANVIL_LAND, 0.25, 1.2, 0.1),
            new DefinedSound("random.metalhit", 0.3, 0.85, 0.1)
    ),
    /** 전투 - '퀘이커' 액티브 1번 - 파괴 */
    COMBAT_QUAKER_A1_DEATH(
            new DefinedSound(Sound.ENTITY_IRONGOLEM_HURT, 2, 0.5),
            new DefinedSound(Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 2, 0.7),
            new DefinedSound("random.metalhit", 2, 0.7),
            new DefinedSound(Sound.ITEM_SHIELD_BLOCK, 2, 0.5)
    ),
    /** 전투 - '퀘이커' 액티브 2번 - 사용 */
    COMBAT_QUAKER_A2_USE(
            new DefinedSound(Sound.ENTITY_IRONGOLEM_ATTACK, 1, 0.5),
            new DefinedSound("random.gun2.shovel_leftclick", 1, 0.5, 0.1)
    ),
    /** 전투 - '퀘이커' 액티브 2번 - 사용 준비 */
    COMBAT_QUAKER_A2_USE_READY(
            new DefinedSound(Sound.ENTITY_IRONGOLEM_HURT, 3, 0.5),
            new DefinedSound(Sound.ITEM_TOTEM_USE, 3, 1.6),
            new DefinedSound(Sound.ENTITY_PLAYER_ATTACK_CRIT, 3, 0.6),
            new DefinedSound(Sound.ENTITY_PLAYER_ATTACK_CRIT, 3, 0.7)
    ),
    /** 전투 - '퀘이커' 액티브 3번 - 사용 */
    COMBAT_QUAKER_A3_USE(
            new DefinedSound(Sound.BLOCK_LAVA_EXTINGUISH, 1, 0.8),
            new DefinedSound("random.gun2.shovel_leftclick", 1, 0.5),
            new DefinedSound("random.gun2.shovel_leftclick", 1, 0.8)
    ),
    /** 전투 - '퀘이커' 액티브 3번 - 사용 준비 */
    COMBAT_QUAKER_A3_USE_READY(
            new DefinedSound(Sound.ENTITY_GHAST_SHOOT, 2, 0.5),
            new DefinedSound("new.item.trident.throw", 2, 0.7),
            new DefinedSound(Sound.ENTITY_PLAYER_ATTACK_SWEEP, 2, 0.7)
    ),
    /** 전투 - '퀘이커' 액티브 3번 - 틱 효과음 */
    COMBAT_QUAKER_A3_TICK(new DefinedSound(Sound.ENTITY_GHAST_SHOOT, 0.6, 0.5)),
    /** 전투 - '퀘이커' 액티브 3번 - 타격 */
    COMBAT_QUAKER_A3_HIT(
            new DefinedSound(Sound.ENTITY_PLAYER_ATTACK_KNOCKBACK, 2, 0.6),
            new DefinedSound(Sound.ENTITY_PLAYER_ATTACK_KNOCKBACK, 2, 0.7),
            new DefinedSound(Sound.ENTITY_PLAYER_ATTACK_CRIT, 2, 0.7)
    ),
    /** 전투 - '퀘이커' 궁극기 - 사용 준비 */
    COMBAT_QUAKER_ULT_USE_READY(
            new DefinedSound(Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 5, 0.5),
            new DefinedSound(Sound.ENTITY_IRONGOLEM_DEATH, 5, 0.7),
            new DefinedSound(Sound.ENTITY_GENERIC_EXPLODE, 5, 0.7),
            new DefinedSound(Sound.BLOCK_ANVIL_PLACE, 5, 0.5),
            new DefinedSound("random.explosion_reverb", 7, 1.4)
    ),
    /** 전투 - '실리아' 무기 - 사용 */
    COMBAT_SILIA_WEAPON_USE(
            new DefinedSound("random.gun2.knife_leftclick", 0.8, 1),
            new DefinedSound("random.swordhit", 0.7, 1.2),
            new DefinedSound("new.item.trident.riptide_1", 0.6, 1.3)
    ),
    /** 전투 - '실리아' 무기 - 엔티티 타격 */
    COMBAT_SILIA_WEAPON_HIT_ENTITY(new DefinedSound("random.stab", 1, 0.8, 0.05)),
    /** 전투 - '실리아' 특성 2번 - 사용 */
    COMBAT_SILIA_T2_USE(
            new DefinedSound(Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.5, 1),
            new DefinedSound(Sound.ENTITY_IRONGOLEM_ATTACK, 1.5, 0.8),
            new DefinedSound("random.swordhit", 1.5, 0.7)
    ),
    /** 전투 - '실리아' 패시브 1번 - 사용 */
    COMBAT_SILIA_P1_USE(
            new DefinedSound(Sound.ENTITY_LLAMA_SWAG, 0.8, 1.2),
            new DefinedSound(Sound.BLOCK_CLOTH_STEP, 0.8, 1.2)
    ),
    /** 전투 - '실리아' 패시브 2번 - 사용 */
    COMBAT_SILIA_P2_USE(new DefinedSound(Sound.BLOCK_STONE_STEP, 0.9, 0.55, 0.05)),
    /** 전투 - '실리아' 액티브 1번 - 사용 */
    COMBAT_SILIA_A1_USE(
            new DefinedSound("new.item.trident.throw", 1.5, 0.8),
            new DefinedSound("random.swordhit", 1.5, 0.8),
            new DefinedSound("random.swordhit", 1.5, 0.8)
    ),
    /** 전투 - '실리아' 액티브 2번 - 사용 */
    COMBAT_SILIA_A2_USE(new DefinedSound(Sound.BLOCK_LAVA_EXTINGUISH, 1, 1)),
    /** 전투 - '실리아' 액티브 2번 - 사용 준비 */
    COMBAT_SILIA_A2_USE_READY(
            new DefinedSound("random.swing", 1.5, 0.6),
            new DefinedSound("new.item.trident.riptide_3", 1.5, 0.8)
    ),
    /** 전투 - '실리아' 액티브 2번 - 엔티티 타격 */
    COMBAT_SILIA_A2_HIT_ENTITY(
            new DefinedSound("random.swing", 1, 0.7, 0.05),
            new DefinedSound("new.item.trident.riptide_2", 1, 0.9, 0.05)
    ),
    /** 전투 - '실리아' 액티브 3번 - 사용 */
    COMBAT_SILIA_A3_USE(
            new DefinedSound(Sound.ENTITY_LLAMA_SWAG, 0.2, 1),
            new DefinedSound(Sound.BLOCK_LAVA_EXTINGUISH, 0.15, 1.5)
    ),
    /** 전투 - '실리아' 액티브 3번 - 해제 */
    COMBAT_SILIA_A3_DISABLE(
            new DefinedSound(Sound.ENTITY_LLAMA_SWAG, 0.2, 1.2),
            new DefinedSound(Sound.BLOCK_LAVA_EXTINGUISH, 0.15, 1.7)
    ),
    /** 전투 - '실리아' 액티브 3번 - 활성화 */
    COMBAT_SILIA_A3_ACTIVATE(new DefinedSound("new.item.trident.return", 1, 1.2)),
    /** 전투 - '실리아' 궁극기 - 사용 준비 */
    COMBAT_SILIA_ULT_USE_READY(
            new DefinedSound("random.swordhit", 2, 1),
            new DefinedSound("random.swordhit", 2, 0.7),
            new DefinedSound("new.item.trident.return", 2.5, 1.4),
            new DefinedSound("new.item.trident.return", 2.5, 1.2)
    ),
    /** 전투 - '니스' 무기 - 사용 */
    COMBAT_NEACE_WEAPON_USE(
            new DefinedSound(Sound.BLOCK_ENCHANTMENT_TABLE_USE, 0.8, 1.8),
            new DefinedSound(Sound.ENTITY_GHAST_SHOOT, 1, 1.5)
    ),
    /** 전투 - '니스' 무기 - 사용 (치유 광선) */
    COMBAT_NEACE_WEAPON_USE_HEAL(new DefinedSound(Sound.ENTITY_GUARDIAN_ATTACK, 0.2, 2)),
    /** 전투 - '니스' 액티브 1번 - 사용 */
    COMBAT_NEACE_A1_USE(
            new DefinedSound(Sound.ENTITY_EVOCATION_ILLAGER_CAST_SPELL, 2, 1.6),
            new DefinedSound("new.block.respawn_anchor.charge", 2, 1.4),
            new DefinedSound("new.block.note_block.chime", 2, 1.6),
            new DefinedSound("new.block.note_block.chime", 2, 1.2)
    ),
    /** 전투 - '니스' 액티브 2번 - 사용 */
    COMBAT_NEACE_A2_USE(
            new DefinedSound(Sound.ENTITY_EVOCATION_ILLAGER_CAST_SPELL, 2, 1.5),
            new DefinedSound(Sound.BLOCK_ENCHANTMENT_TABLE_USE, 2, 1.4),
            new DefinedSound(Sound.BLOCK_ENCHANTMENT_TABLE_USE, 2, 1.4),
            new DefinedSound(Sound.ENTITY_ZOMBIE_VILLAGER_CONVERTED, 0.5, 1.4)
    ),
    /** 전투 - '니스' 액티브 3번 - 사용 */
    COMBAT_NEACE_A3_USE(
            new DefinedSound(Sound.ENTITY_EVOCATION_ILLAGER_CAST_SPELL, 1.2, 1.8),
            new DefinedSound(Sound.ENTITY_ENDERDRAGON_FLAP, 1.2, 1.6),
            new DefinedSound(Sound.ENTITY_FIREWORK_LAUNCH, 1.2, 0.7)
    ),
    /** 전투 - '니스' 궁극기 - 사용 */
    COMBAT_NEACE_ULT_USE(
            new DefinedSound(Sound.BLOCK_ENCHANTMENT_TABLE_USE, 2, 1.2),
            new DefinedSound(Sound.BLOCK_ENCHANTMENT_TABLE_USE, 2, 1.2),
            new DefinedSound("new.block.respawn_anchor.charge", 2, 0.7)
    ),
    /** 전투 - '니스' 궁극기 - 사용 준비 */
    COMBAT_NEACE_ULT_USE_READY(
            new DefinedSound(Sound.ENTITY_EVOCATION_ILLAGER_PREPARE_SUMMON, 3, 1.1),
            new DefinedSound(Sound.ENTITY_EVOCATION_ILLAGER_PREPARE_SUMMON, 3, 1.1)
    ),
    /** 전투 - '벨리온' 무기 - 사용 */
    COMBAT_VELLION_WEAPON_USE(
            new DefinedSound(Sound.ENTITY_ENDERDRAGON_HURT, 0.8, 0.5),
            new DefinedSound(Sound.BLOCK_END_PORTAL_FRAME_FILL, 1, 0.8),
            new DefinedSound(Sound.BLOCK_END_PORTAL_FRAME_FILL, 1, 0.9)
    ),
    /** 전투 - '벨리온' 패시브 1번 - 사용 */
    COMBAT_VELLION_P1_USE(
            new DefinedSound("new.entity.phantom.flap", 1, 1.4),
            new DefinedSound("new.entity.phantom.flap", 1, 1.6),
            new DefinedSound(Sound.ENTITY_EVOCATION_ILLAGER_CAST_SPELL, 0.6, 0.7)
    ),
    /** 전투 - '벨리온' 패시브 1번 - 해제 */
    COMBAT_VELLION_P1_DISABLE(
            new DefinedSound("new.entity.phantom.flap", 1, 1.5),
            new DefinedSound("new.entity.phantom.flap", 1, 1.7)
    ),
    /** 전투 - '벨리온' 액티브 1번 - 사용 */
    COMBAT_VELLION_A1_USE(
            new DefinedSound(Sound.ENTITY_ENDEREYE_DEATH, 2, 0.8, 0.1),
            new DefinedSound(Sound.ENTITY_ENDEREYE_DEATH, 2, 0.8, 0.1),
            new DefinedSound(Sound.ENTITY_ZOMBIE_VILLAGER_CONVERTED, 2, 1.5)
    ),
    /** 전투 - '벨리온' 액티브 1번 - 사용 준비 */
    COMBAT_VELLION_A1_USE_READY(
            new DefinedSound(Sound.ENTITY_SHULKER_SHOOT, 2, 0.7),
            new DefinedSound(Sound.ENTITY_ILLUSION_ILLAGER_PREPARE_MIRROR, 2, 1.2),
            new DefinedSound(Sound.ENTITY_ELDER_GUARDIAN_DEATH, 2, 1.8)
    ),
    /** 전투 - '벨리온' 액티브 1번 - 엔티티 타격 */
    COMBAT_VELLION_A1_HIT_ENTITY(new DefinedSound(Sound.ENTITY_ZOMBIE_INFECT, 1, 0.7, 0.05)),
    /** 전투 - '벨리온' 액티브 2번 - 사용 */
    COMBAT_VELLION_A2_USE(
            new DefinedSound(Sound.BLOCK_ENCHANTMENT_TABLE_USE, 2, 0.8),
            new DefinedSound(Sound.BLOCK_ENCHANTMENT_TABLE_USE, 2, 0.8),
            new DefinedSound("new.entity.squid.squirt", 2, 1.2)
    ),
    /** 전투 - '벨리온' 액티브 2번 - 사용 준비 */
    COMBAT_VELLION_A2_USE_READY(
            new DefinedSound(Sound.ENTITY_ELDER_GUARDIAN_CURSE, 2, 1),
            new DefinedSound("new.block.respawn_anchor.charge", 2, 0.8)
    ),
    /** 전투 - '벨리온' 액티브 2번 - 발동 */
    COMBAT_VELLION_A2_TRIGGER(new DefinedSound(Sound.BLOCK_END_PORTAL_FRAME_FILL, 0.6, 0.7, 0.1)),
    /** 전투 - '벨리온' 액티브 3번 - 사용 */
    COMBAT_VELLION_A3_USE(
            new DefinedSound(Sound.BLOCK_ENCHANTMENT_TABLE_USE, 2, 0.7),
            new DefinedSound(Sound.BLOCK_ENCHANTMENT_TABLE_USE, 2, 0.7),
            new DefinedSound(Sound.ENTITY_GUARDIAN_HURT, 2, 2)
    ),
    /** 전투 - '벨리온' 액티브 3번 - 사용 준비 */
    COMBAT_VELLION_A3_USE_READY(
            new DefinedSound(Sound.ENTITY_ILLUSION_ILLAGER_PREPARE_BLINDNESS, 2, 0.9),
            new DefinedSound("new.block.respawn_anchor.set_spawn", 2, 0.6),
            new DefinedSound("new.block.respawn_anchor.set_spawn", 2, 0.7)
    ),
    /** 전투 - '벨리온' 궁극기 - 사용 */
    COMBAT_VELLION_ULT_USE(
            new DefinedSound(Sound.BLOCK_ENCHANTMENT_TABLE_USE, 2, 0.8),
            new DefinedSound(Sound.BLOCK_ENCHANTMENT_TABLE_USE, 2, 0.8),
            new DefinedSound(Sound.ENTITY_GUARDIAN_HURT, 2, 1.8)
    ),
    /** 전투 - '벨리온' 궁극기 - 사용 준비 */
    COMBAT_VELLION_ULT_USE_READY(
            new DefinedSound(Sound.ENTITY_ILLUSION_ILLAGER_PREPARE_BLINDNESS, 3, 0.7),
            new DefinedSound(Sound.ENTITY_ILLUSION_ILLAGER_PREPARE_BLINDNESS, 3, 0.8),
            new DefinedSound(Sound.ENTITY_EVOCATION_ILLAGER_PREPARE_ATTACK, 3, 0.85),
            new DefinedSound(Sound.ENTITY_EVOCATION_ILLAGER_PREPARE_SUMMON, 3, 0.7)
    ),
    /** 전투 - '벨리온' 궁극기 - 폭발 */
    COMBAT_VELLION_ULT_EXPLODE(
            new DefinedSound("new.block.conduit.deactivate", 3, 0.6),
            new DefinedSound("new.block.respawn_anchor.deplete", 3, 0.6),
            new DefinedSound("new.block.respawn_anchor.deplete", 3, 0.8)
    ),
    /** 전투 - '인페르노' 발소리 */
    COMBAT_INFERNO_FOOTSTEP(
            new DefinedSound("new.entity.panda.step", 0.4, 0.9, 0.1),
            new DefinedSound(Sound.ENTITY_LLAMA_STEP, 0.3, 0.7, 0.1)
    ),
    /** 전투 - '인페르노' 무기 - 사용 */
    COMBAT_INFERNO_WEAPON_USE(
            new DefinedSound(Sound.ENTITY_HORSE_BREATHE, 1.5, 0.7),
            new DefinedSound(Sound.ENTITY_HORSE_BREATHE, 1.5, 1.3),
            new DefinedSound("new.block.soul_sand.fall", 1.5, 0.5)
    ),
    /** 전투 - '인페르노' 무기 - 사용 (화염탄) */
    COMBAT_INFERNO_WEAPON_USE_FIREBALL(
            new DefinedSound(Sound.ENTITY_SHULKER_SHOOT, 2, 1.5),
            new DefinedSound(Sound.ENTITY_GHAST_SHOOT, 2, 1.1),
            new DefinedSound("random.gun.grenade", 2, 0.9)
    ),
    /** 전투 - '인페르노' 무기 - 폭발 (화염탄) */
    COMBAT_INFERNO_WEAPON_FIREBALL_EXPLODE(
            new DefinedSound(Sound.BLOCK_FIRE_EXTINGUISH, 3, 0.8),
            new DefinedSound(Sound.ENTITY_GENERIC_EXPLODE, 3, 1.4),
            new DefinedSound("random.gun_reverb2", 5, 1)
    ),
    /** 전투 - '인페르노' 액티브 1번 - 사용 */
    COMBAT_INFERNO_A1_USE(
            new DefinedSound(Sound.ENTITY_WITHER_SHOOT, 3, 0.8),
            new DefinedSound(Sound.ENTITY_GHAST_SHOOT, 3, 0.8),
            new DefinedSound(Sound.BLOCK_LAVA_EXTINGUISH, 3, 0.6)
    ),
    /** 전투 - '인페르노' 액티브 1번 - 착지 */
    COMBAT_INFERNO_A1_LAND(
            new DefinedSound(Sound.BLOCK_LAVA_EXTINGUISH, 3, 0.5),
            new DefinedSound(Sound.ENTITY_PLAYER_ATTACK_KNOCKBACK, 3, 0.7),
            new DefinedSound(Sound.ENTITY_PLAYER_ATTACK_KNOCKBACK, 3, 0.5),
            new DefinedSound(Sound.ENTITY_GENERIC_EXPLODE, 3, 1.3)
    ),
    /** 전투 - '인페르노' 액티브 2번 - 사용 */
    COMBAT_INFERNO_A2_USE(
            new DefinedSound(Sound.BLOCK_PISTON_CONTRACT, 2, 0.5),
            new DefinedSound(Sound.BLOCK_PISTON_CONTRACT, 2, 0.6)
    ),
    /** 전투 - '인페르노' 액티브 2번 - 틱 효과음 */
    COMBAT_INFERNO_A2_TICK(
            new DefinedSound(Sound.BLOCK_FIRE_EXTINGUISH, 2, 0.55, 0.1),
            new DefinedSound(Sound.BLOCK_FIRE_AMBIENT, 2, 0.6, 0.1)
    ),
    /** 전투 - '인페르노' 궁극기 - 사용 */
    COMBAT_INFERNO_ULT_USE(new DefinedSound("new.block.respawn_anchor.ambient", 3, 1.2)),
    /** 전투 - '인페르노' 궁극기 - 틱 효과음 */
    COMBAT_INFERNO_ULT_TICK(new DefinedSound(Sound.BLOCK_LAVA_AMBIENT, 2, 0.9, 0.1)),
    /** 전투 - '인페르노' 궁극기 - 피격 */
    COMBAT_INFERNO_ULT_DAMAGE(new DefinedSound(Sound.BLOCK_LAVA_POP, 0.3, 1.2, 0.1)),
    /** 전투 - '인페르노' 궁극기 - 파괴 */
    COMBAT_INFERNO_ULT_DEATH(
            new DefinedSound(Sound.ENTITY_GENERIC_EXPLODE, 3, 0.8),
            new DefinedSound(Sound.BLOCK_LAVA_EXTINGUISH, 3, 0.5),
            new DefinedSound("new.block.conduit.deactivate", 3, 0.8)
    ),
    /** 전투 - '마그리타' 무기 - 사용 */
    COMBAT_MAGRITTA_WEAPON_USE(
            new DefinedSound("random.gun2.xm1014_1", 3, 1),
            new DefinedSound("random.gun2.xm1014_1", 3, 0.8),
            new DefinedSound("random.gun2.spas_12_1", 3, 1),
            new DefinedSound("random.gun_reverb", 5, 0.9),
            new DefinedSound("random.gun_reverb", 5, 0.8)
    ),
    /** 전투 - '마그리타' 특성 1번 - 사용 */
    COMBAT_MAGRITTA_T1_USE(new DefinedSound("new.item.trident.hit", 2, 0.8, 0.1)),
    /** 전투 - '마그리타' 특성 1번 - 최대치 */
    COMBAT_MAGRITTA_T1_MAX(new DefinedSound(Sound.ENTITY_WITHER_SKELETON_DEATH, 2, 1.5, 0.1)),
    /** 전투 - '마그리타' 액티브 1번 - 사용 */
    COMBAT_MAGRITTA_A1_USE(new DefinedSound(Sound.ENTITY_CAT_PURREOW, 0.5, 1.6)),
    /** 전투 - '마그리타' 액티브 1번 - 부착 */
    COMBAT_MAGRITTA_A1_STUCK(
            new DefinedSound(Sound.ENTITY_PLAYER_HURT, 0.8, 0.5),
            new DefinedSound(Sound.ITEM_FLINTANDSTEEL_USE, 0.8, 1.5)
    ),
    /** 전투 - '마그리타' 액티브 1번 - 틱 효과음 */
    COMBAT_MAGRITTA_A1_TICK(new DefinedSound(Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.5, 1.8)),
    /** 전투 - '마그리타' 액티브 1번 - 폭발 */
    COMBAT_MAGRITTA_A1_EXPLODE(
            new DefinedSound(Sound.ENTITY_GENERIC_EXPLODE, 4, 0.8),
            new DefinedSound(Sound.ENTITY_FIREWORK_LARGE_BLAST, 4, 0.6),
            new DefinedSound(Sound.BLOCK_FIRE_EXTINGUISH, 4, 0.8),
            new DefinedSound(Sound.BLOCK_FIRE_EXTINGUISH, 4, 0.5),
            new DefinedSound("random.explosion_reverb", 6, 1.2)
    ),
    /** 전투 - '마그리타' 액티브 2번 - 사용 */
    COMBAT_MAGRITTA_A2_USE(
            new DefinedSound(Sound.BLOCK_FIRE_EXTINGUISH, 1.5, 2),
            new DefinedSound(Sound.ENTITY_ENDERDRAGON_FLAP, 1.5, 0.5)
    ),
    /** 전투 - '마그리타' 궁극기 - 사용 */
    COMBAT_MAGRITTA_ULT_USE(
            new DefinedSound(Sound.ENTITY_WOLF_SHAKE, 1, 0.6),
            new DefinedSound(Sound.ENTITY_WOLF_SHAKE, 1, 0.6),
            new DefinedSound(Sound.BLOCK_LAVA_EXTINGUISH, 1, 0.6)
    ),
    /** 전투 - '마그리타' 궁극기 - 사격 */
    COMBAT_MAGRITTA_ULT_SHOOT(
            new DefinedSound(Sound.BLOCK_FIRE_EXTINGUISH, 2, 0.8, 0.1),
            new DefinedSound("random.gun2.xm1014_1", 3, 1),
            new DefinedSound("random.gun2.spas_12_1", 3, 1),
            new DefinedSound("random.gun_reverb", 5, 0.9),
            new DefinedSound("random.gun_reverb", 5, 0.8)
    ),
    /** 전투 - '마그리타' 궁극기 - 사용 종료 */
    COMBAT_MAGRITTA_ULT_END(
            new DefinedSound(Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 2, 0.8),
            new DefinedSound(Sound.ENTITY_ITEM_BREAK, 3, 0.8),
            new DefinedSound(Sound.ENTITY_GENERIC_EXPLODE, 3, 1.4)
    ),
    /** 전투 - '체드' 무기 - 충전 */
    COMBAT_CHED_WEAPON_CHARGE(new DefinedSound("new.item.crossbow.loading_middle", 0.6, 1)),
    /** 전투 - '체드' 무기 - 사용 */
    COMBAT_CHED_WEAPON_USE(
            new DefinedSound("new.item.crossbow.shoot", 0.5, 1.2),
            new DefinedSound("random.gun.bow", 0.5, 0.8),
            new DefinedSound("random.gun2.shovel_leftclick", 0.6, 0.85)
    ),
    /** 전투 - '체드' 무기 - 타격 */
    COMBAT_CHED_WEAPON_HIT(new DefinedSound("random.gun.arrowhit", 0.5, 1)),
    /** 전투 - '체드' 패시브 1번 - 사용 */
    COMBAT_CHED_P1_USE(new DefinedSound(Sound.BLOCK_STONE_STEP, 1, 0.5, 0.05)),
    /** 전투 - '체드' 패시브 1번 - 사용 (매달리기) */
    COMBAT_CHED_P1_USE_HANG(
            new DefinedSound("new.entity.phantom.flap", 1, 1.7),
            new DefinedSound(Sound.ENTITY_EVOCATION_ILLAGER_CAST_SPELL, 0.6, 0.85)
    ),
    /** 전투 - '체드' 패시브 1번 - 해제 (매달리기) */
    COMBAT_CHED_P1_DISABLE_HANG(
            new DefinedSound("new.entity.phantom.flap", 1, 1.8),
            new DefinedSound(Sound.ENTITY_LLAMA_SWAG, 0.6, 1.4)
    ),
    /** 전투 - '체드' 액티브 1번 - 사용 */
    COMBAT_CHED_A1_USE(
            new DefinedSound("new.item.crossbow.loading_end", 0.7, 1.4),
            new DefinedSound(Sound.ENTITY_CAT_PURREOW, 0.7, 2)
    ),
    /** 전투 - '체드' 액티브 1번 - 사격 */
    COMBAT_CHED_A1_SHOOT(
            new DefinedSound("new.item.crossbow.shoot", 1.4, 1.6),
            new DefinedSound("random.gun.bow", 1.4, 1.2),
            new DefinedSound(Sound.ENTITY_GHAST_SHOOT, 1.6, 1.4)
    ),
    /** 전투 - '체드' 액티브 2번 - 사용 */
    COMBAT_CHED_A2_USE(
            new DefinedSound(Sound.ENTITY_ENDERDRAGON_FLAP, 1, 1.3),
            new DefinedSound(Sound.ENTITY_LLAMA_SWAG, 1, 1)
    ),
    /** 전투 - '체드' 액티브 3번 - 사용 */
    COMBAT_CHED_A3_USE(
            new DefinedSound(Sound.ENTITY_EVOCATION_ILLAGER_CAST_SPELL, 2, 1.6),
            new DefinedSound(Sound.BLOCK_ENCHANTMENT_TABLE_USE, 2, 0.7),
            new DefinedSound(Sound.BLOCK_ENCHANTMENT_TABLE_USE, 2, 0.7)
    ),
    /** 전투 - '체드' 액티브 3번 - 사용 준비 */
    COMBAT_CHED_A3_USE_READY(
            new DefinedSound(Sound.ENTITY_ENDERDRAGON_FLAP, 1.5, 1.4),
            new DefinedSound(Sound.ENTITY_VEX_CHARGE, 1.5, 1.3),
            new DefinedSound(Sound.ENTITY_VEX_AMBIENT, 1.5, 1.7),
            new DefinedSound(Sound.ENTITY_VEX_AMBIENT, 1.5, 1.5)
    ),
    /** 전투 - '체드' 액티브 3번 - 틱 효과음 */
    COMBAT_CHED_A3_TICK(new DefinedSound("new.entity.phantom.flap", 1, 1.3)),
    /** 전투 - '체드' 궁극기 - 사용 */
    COMBAT_CHED_ULT_USE(
            new DefinedSound(Sound.ENTITY_EVOCATION_ILLAGER_CAST_SPELL, 2, 1.4),
            new DefinedSound(Sound.ENTITY_ILLUSION_ILLAGER_PREPARE_MIRROR, 2, 0.8),
            new DefinedSound("new.entity.squid.squirt", 2, 0.7)
    ),
    /** 전투 - '체드' 궁극기 - 사용 준비 */
    COMBAT_CHED_ULT_USE_READY(
            new DefinedSound("new.entity.phantom.death", 3, 0.7),
            new DefinedSound("new.entity.phantom.death", 3, 0.7),
            new DefinedSound(Sound.ENTITY_WITHER_SHOOT, 3, 0.5),
            new DefinedSound(Sound.ENTITY_VEX_CHARGE, 3, 0.85)
    ),
    /** 전투 - '체드' 궁극기 - 틱 효과음 */
    COMBAT_CHED_ULT_TICK(new DefinedSound(Sound.ENTITY_ENDERDRAGON_FLAP, 1.5, 1.2)),
    /** 전투 - '체드' 궁극기 - 폭발 */
    COMBAT_CHED_ULT_EXPLODE(
            new DefinedSound(Sound.ITEM_TOTEM_USE, 5, 1.3),
            new DefinedSound(Sound.ENTITY_GENERIC_EXPLODE, 5, 0.7),
            new DefinedSound(Sound.ENTITY_GHAST_SHOOT, 5, 0.6),
            new DefinedSound(Sound.ENTITY_GHAST_SHOOT, 5, 0.8),
            new DefinedSound("random.explosion_reverb", 7, 0.6)
    ),
    /** 전투 - '체드' 궁극기 - 화염 지대 틱 효과음 */
    COMBAT_CHED_ULT_FIRE_FLOOR_TICK(new DefinedSound(Sound.BLOCK_FIRE_AMBIENT, 2, 0.75, 0.1));

    /** 지정된 효과음 목록 */
    @NonNull
    private final DefinedSound @NonNull [] definedSounds;

    NamedSound(DefinedSound @NonNull ... definedSounds) {
        this.definedSounds = definedSounds;
    }

    /**
     * 지정된 효과음 클래스.
     */
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @Getter
    static class DefinedSound {
        /** 소리 이름 */
        @NonNull
        private final String sound;
        /** 음량 */
        private final double volume;
        /** 음정 */
        private final double pitch;
        /** 음정의 분산도 */
        private final double pitchSpreadRange;

        private DefinedSound(@NonNull String sound, double volume, double pitch) {
            this(sound, volume, pitch, 0);
        }

        private DefinedSound(@NonNull Sound sound, double volume, double pitch) {
            this(sound.toString(), volume, pitch, 0);
        }

        private DefinedSound(@NonNull Sound sound, double volume, double pitch, double pitchSpreadRange) {
            this(sound.toString(), volume, pitch, pitchSpreadRange);
        }
    }
}
