package com.dace.dmgr.lobby;

/**
 * 채팅 효과음 목록.
 */
public enum ChatSound {
    /** 음소거 */
    MUTE(""),
    /** 플링 */
    PLING("new.block.note_block.pling"),
    /** 하프 */
    HARP("new.block.note_block.harp"),
    /** 더블 베이스 */
    DOUBLE_BASS("new.block.note_block.bass"),
    /** 기타 */
    GUITAR("new.block.note_block.guitar"),
    /** 벨 */
    BELL("new.block.note_block.bell"),
    /** 차임벨 */
    CHIMEBELL("new.block.note_block.chime"),
    /** 카우벨 */
    COWBELL("new.block.note_block.cow_bell"),
    /** 플룻 */
    FLUTE("new.block.note_block.flute"),
    /** 실로폰 */
    XYLOPHONE("new.block.note_block.xylophone"),
    /** 철 실로폰 */
    IRON_XYLOPHONE("new.block.note_block.iron_xylophone"),
    /** 디저리두 */
    DIDGERIDOO("new.block.note_block.didgeridoo"),
    /** 비트 */
    BIT("new.block.note_block.bit"),
    /** 벤조 */
    BANJO("new.block.note_block.banjo");

    /** 효과음 */
    private final String sound;

    ChatSound(String sound) {
        this.sound = sound;
    }

    public String getSound() {
        return sound;
    }
}
