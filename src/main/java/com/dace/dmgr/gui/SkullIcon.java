package com.dace.dmgr.gui;

public enum SkullIcon {
    KOREAN_CHAT("YjAyYWYzY2EyZDVhMTYwY2ExMTE0MDQ4Yjc5NDc1OTQyNjlhZmUyYjFiNWVjMjU1ZWU3MmI2ODNiNjBiOTliOSJ9fX0=\\"),
    NIGHT_VISION("N2VmNGU1NzM1NDkwZmI5MDMyZDUxOTMwMWMzMGU1NTkxNjY2ZTg4YjZmY2I2MmM1M2Q5ZmM3Nzk2YTZmMDZhNyJ9fX0="),
    CROSSHAIR("NzNjM2E5YmRjOGM0MGM0MmQ4NDFkYWViNzFlYTllN2QxYzU0YWIzMWEyM2EyZDkyNjU5MWQ1NTUxNDExN2U1ZCJ9fX0="),
    CHAT_SOUND("OWIxZTIwNDEwYmI2YzdlNjk2OGFmY2QzZWM4NTU1MjBjMzdhNDBkNTRhNTRlOGRhZmMyZTZiNmYyZjlhMTkxNSJ9fX0=\\");

    public static final String PREFIX = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUv";
    private final String url;

    SkullIcon(String texture) {
        this.url = PREFIX + texture;
    }

    public String getUrl() {
        return url;
    }
}
