package com.dace.dmgr.util;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
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

    /** 게임 - 타이머 효과음 */
    GAME_TIMER(new DefinedSound(Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 10, 1)),
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
    COMBAT_ULTIMATE_SKILL_USE(new DefinedSound(Sound.ENTITY_WITHER_SPAWN, 10, 2)),
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
    COMBAT_QUAKER_A1_DISABLE(
            new DefinedSound(Sound.BLOCK_SHULKER_BOX_CLOSE, 1, 1.4)
    ),
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
    COMBAT_NEACE_WEAPON_USE_HEAL(
            new DefinedSound(Sound.ENTITY_GUARDIAN_ATTACK, 0.2, 2)
    ),
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
    );

    /** 지정된 효과음 목록 */
    private final DefinedSound[] definedSounds;

    NamedSound(DefinedSound... definedSounds) {
        this.definedSounds = definedSounds;
    }

    /**
     * 지정된 효과음 클래스.
     */
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @Getter
    static class DefinedSound {
        /** 소리 이름 */
        private final String sound;
        /** 음량 */
        private final double volume;
        /** 음정 */
        private final double pitch;
        /** 음정의 분산도 */
        private final double pitchSpreadRange;

        private DefinedSound(String sound, double volume, double pitch) {
            this(sound, volume, pitch, 0);
        }

        private DefinedSound(Sound sound, double volume, double pitch) {
            this(sound.toString(), volume, pitch, 0);
        }

        private DefinedSound(Sound sound, double volume, double pitch, double pitchSpreadRange) {
            this(sound.toString(), volume, pitch, pitchSpreadRange);
        }
    }
}
