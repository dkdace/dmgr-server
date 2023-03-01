package com.dace.dmgr.combat.character;

/**
 * 전투원 정보를 관리하는 클래스.
 */
public abstract class Character implements ICharacter {
    /** 이름 */
    private final String name;
    /** 스킨 이름 */
    private final String skinName;
    /** 체력 */
    private final int health;
    /** 이동속도 */
    private final float speed;
    /** 히트박스 크기 계수 */
    private final float hitbox;

    /**
     * 전투원 정보 인스턴스를 생성한다.
     *
     * @param name     이름
     * @param skinName 스킨 이름
     * @param health   체력
     * @param speed    이동속도
     * @param hitbox   히트박스 크기 계수
     */
    public Character(String name, String skinName, int health, float speed, float hitbox) {
        this.name = name;
        this.skinName = skinName;
        this.health = health;
        this.speed = speed;
        this.hitbox = hitbox;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getSkinName() {
        return skinName;
    }

    @Override
    public int getHealth() {
        return health;
    }

    @Override
    public float getSpeed() {
        return speed;
    }

    @Override
    public float getHitbox() {
        return hitbox;
    }
}
