package com.dace.dmgr.combat;

import com.dace.dmgr.combat.entity.ICombatEntity;

import static com.dace.dmgr.combat.Bullet.TRAIL_INTERVAL;

public class ProjectileParam {
    ICombatEntity shooter;
    boolean penetrating;
    int trailInterval;
    float hitboxMultiplier;
    int velocity;
    boolean hasGravity;
    boolean bouncing;

    protected ProjectileParam(ICombatEntity shooter, int velocity) {
        this.shooter = shooter;
        this.velocity = velocity;
    }

    static public class Builder {
        protected ProjectileParam instance;

        public Builder(ICombatEntity shooter, int velocity) {
            instance = new ProjectileParam(shooter, velocity);
            instance.penetrating = false;
            instance.trailInterval = TRAIL_INTERVAL;
            instance.hitboxMultiplier = 1.0f;
            instance.hasGravity = false;
            instance.bouncing = false;
        }

        public Builder penetrating(boolean v) {
            instance.penetrating = v;
            return this;
        }

        public Builder trailInterval(int v) {
            instance.trailInterval = v;
            return this;
        }

        public Builder hitboxMultiplier(int v) {
            instance.hitboxMultiplier = v;
            return this;
        }

        public Builder hasGravity(boolean v) {
            instance.hasGravity = v;
            return this;
        }

        public Builder bouncing(boolean v) {
            instance.bouncing = v;
            return this;
        }

        public ProjectileParam build() {
            return instance;
        }
    }
}
