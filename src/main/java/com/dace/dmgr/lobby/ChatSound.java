package com.dace.dmgr.lobby;

import org.bukkit.Material;

/**
 * 채팅 효과음 목록.
 */
public enum ChatSound {
    /** 음소거 */
    MUTE("음소거", "", Material.BARRIER),
    /** 플링 */
    PLING("플링", "new.block.note_block.pling", Material.GLOWSTONE),
    /** 하프 */
    HARP("하프", "new.block.note_block.harp", Material.GRASS),
    /** 더블 베이스 */
    DOUBLE_BASS("더블 베이스", "new.block.note_block.bass", Material.WOOD),
    /** 기타 */
    GUITAR("기타", "new.block.note_block.guitar", Material.WOOL),
    /** 벨 */
    BELL("벨", "new.block.note_block.bell", Material.GOLD_BLOCK),
    /** 차임벨 */
    CHIMEBELL("차임벨", "new.block.note_block.chime", Material.PACKED_ICE),
    /** 카우벨 */
    COWBELL("카우벨", "new.block.note_block.cow_bell", Material.SOUL_SAND),
    /** 플룻 */
    FLUTE("플룻", "new.block.note_block.flute", Material.CLAY),
    /** 실로폰 */
    XYLOPHONE("실로폰", "new.block.note_block.xylophone", Material.QUARTZ_BLOCK),
    /** 철 실로폰 */
    IRON_XYLOPHONE("철 실로폰", "new.block.note_block.iron_xylophone", Material.IRON_BLOCK),
    /** 디저리두 */
    DIDGERIDOO("디저리두", "new.block.note_block.didgeridoo", Material.PUMPKIN),
    /** 비트 */
    BIT("비트", "new.block.note_block.bit", Material.EMERALD_BLOCK),
    /** 벤조 */
    BANJO("벤조", "new.block.note_block.banjo", Material.HAY_BLOCK);

    /** 이름 */
    private final String name;
    /** 효과음 */
    private final String sound;
    /** 블록 (노트블록 기준) */
    private final Material material;

    ChatSound(String name, String sound, Material material) {
        this.sound = sound;
        this.name = name;
        this.material = material;
    }

    public String getName() {
        return name;
    }

    public String getSound() {
        return sound;
    }

    public Material getMaterial() {
        return material;
    }
}
