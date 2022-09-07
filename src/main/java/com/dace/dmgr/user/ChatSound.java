package com.dace.dmgr.user;

public enum ChatSound {
    MUTE(""),
    PLING("new.block.note_block.pling"),
    HARP("new.block.note_block.harp"),
    DOUBLE_BASS("new.block.note_block.bass"),
    GUITAR("new.block.note_block.guitar"),
    BELL("new.block.note_block.bell"),
    CHIMEBELL("new.block.note_block.chime"),
    COWBELL("new.block.note_block.cow_bell"),
    FLUTE("new.block.note_block.flute"),
    XYLOPHONE("new.block.note_block.xylophone"),
    IRON_XYLOPHONE("new.block.note_block.iron_xylophone"),
    DIDGERIDOO("new.block.note_block.didgeridoo"),
    BIT("new.block.note_block.bit"),
    BANJO("new.block.note_block.banjo");

    private final String sound;

    ChatSound(String sound) {
        this.sound = sound;
    }

    public String getSound() {
        return sound;
    }
}
