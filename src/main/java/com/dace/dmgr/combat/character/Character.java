package com.dace.dmgr.combat.character;

public abstract class Character implements ICharacter {
    private final String name;
    private final String skinName;
    private final int health;
    private final float speed;
    private final float hitbox;

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
