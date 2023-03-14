package com.dace.dmgr.gui;

import lombok.Getter;

/**
 * 플레이어 머리 아이템 목록.
 */
@Getter
public enum PlayerSkull {
    /** 한글 채팅 */
    KOREAN_CHAT("YjAyYWYzY2EyZDVhMTYwY2ExMTE0MDQ4Yjc5NDc1OTQyNjlhZmUyYjFiNWVjMjU1ZWU3MmI2ODNiNjBiOTliOSJ9fX0=\\"),
    /** 야간 투시 */
    NIGHT_VISION("N2VmNGU1NzM1NDkwZmI5MDMyZDUxOTMwMWMzMGU1NTkxNjY2ZTg4YjZmY2I2MmM1M2Q5ZmM3Nzk2YTZmMDZhNyJ9fX0="),
    /** 조준선 설정 */
    CROSSHAIR("NzNjM2E5YmRjOGM0MGM0MmQ4NDFkYWViNzFlYTllN2QxYzU0YWIzMWEyM2EyZDkyNjU5MWQ1NTUxNDExN2U1ZCJ9fX0="),
    /** 채팅 효과음 설정 */
    CHAT_SOUND("OWIxZTIwNDEwYmI2YzdlNjk2OGFmY2QzZWM4NTU1MjBjMzdhNDBkNTRhNTRlOGRhZmMyZTZiNmYyZjlhMTkxNSJ9fX0=\\");

    /** 머리 스킨을 불러올 때 사용하는 토큰의 접두사 */
    private static final String PREFIX = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUv";
    /** 머리 스킨의 전체 URL */
    private final String url;

    PlayerSkull(String texture) {
        this.url = PREFIX + texture;
    }
}
