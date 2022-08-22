package com.dace.dmgr.combat.character.arkace;

import com.dace.dmgr.combat.character.ISkill;
import com.dace.dmgr.combat.character.IStats;

public class ArkaceStats implements IStats {
    private static final int HEALTH = 1000;
    private static final float SPEED = 1.0F;
    private static final float HITBOX = 1.0F;

    @Override
    public int getHealth() {
        return HEALTH;
    }

    @Override
    public float getSpeed() {
        return SPEED;
    }

    @Override
    public float getHitbox() {
        return HITBOX;
    }

    @Override
    public ISkill getPassive1() {
        return new Passive1();
    }

    @Override
    public ISkill getActive2() {
        return new Active2();
    }

    @Override
    public ISkill getActive3() {
        return new Active3();
    }

    @Override
    public ISkill getUltimate() {
        return new Ultimate();
    }

    static class Normal {
        static int DAMAGE = 75;
        static int DAMAGE_DISTANCE = 25;
    }

    static class Passive1 implements ISkill {
        static int SPRINT_SPEED = 30;
    }

    static class Active2 implements ISkill {
        static long COOLDOWN = 7 * 20;
        static int DAMAGE_DIRECT = 50;
        static float RADIUS = 3.5F;
        static int DAMAGE_EXPLODE = 100;

        @Override
        public long getCooldown() {
            return COOLDOWN;
        }
    }

    static class Active3 implements ISkill {
        static long COOLDOWN = 12 * 20;
        static int HEAL = 350;
        static long DURATION = (long) (2.5 * 20);

        @Override
        public long getCooldown() {
            return COOLDOWN;
        }
    }

    static class Ultimate implements ISkill {
        static int COST = 7000;
        static long DURATION = 12 * 20;

        @Override
        public int getCost() {
            return COST;
        }
    }
}
