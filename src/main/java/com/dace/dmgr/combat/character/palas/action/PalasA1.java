package com.dace.dmgr.combat.character.palas.action;

import com.dace.dmgr.DMGR;
import com.dace.dmgr.Timespan;
import com.dace.dmgr.Timestamp;
import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.ActiveSkill;
import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.DamageType;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.module.statuseffect.Stun;
import com.dace.dmgr.combat.interaction.Projectile;
import com.dace.dmgr.util.task.DelayTask;
import com.dace.dmgr.util.task.TaskUtil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bukkit.Location;

import java.util.WeakHashMap;

public final class PalasA1 extends ActiveSkill {
    /** 처치 지원 점수 제한시간 타임스탬프 목록 (피격자 : 종료 시점) */
    private final WeakHashMap<CombatUser, Timestamp> assistScoreTimeLimitTimestampMap = new WeakHashMap<>();

    public PalasA1(@NonNull CombatUser combatUser) {
        super(combatUser, PalasA1Info.getInstance(), 0);
    }

    @Override
    @NonNull
    public ActionKey @NonNull [] getDefaultActionKeys() {
        return new ActionKey[]{ActionKey.SLOT_1};
    }

    @Override
    public long getDefaultCooldown() {
        return PalasA1Info.COOLDOWN;
    }

    @Override
    public long getDefaultDuration() {
        return -1;
    }

    @Override
    public boolean canUse(@NonNull ActionKey actionKey) {
        return super.canUse(actionKey) && isDurationFinished();
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        setDuration();
        combatUser.getWeapon().onCancelled();
        combatUser.getWeapon().setVisible(false);
        combatUser.setGlobalCooldown(PalasA1Info.GLOBAL_COOLDOWN);

        PalasA1Info.SOUND.USE.play(combatUser.getEntity().getLocation());

        TaskUtil.addTask(taskRunner, new DelayTask(() -> {
            onCancelled();

            Location loc = combatUser.getArmLocation(true);
            new PalasA1Projectile().shot(loc);

            PalasA1Info.SOUND.USE_READY.play(loc);
        }, PalasA1Info.READY_DURATION));
    }

    @Override
    public boolean isCancellable() {
        return !isDurationFinished();
    }

    @Override
    public void onCancelled() {
        super.onCancelled();

        setDuration(0);
        combatUser.getWeapon().setVisible(true);
    }

    /**
     * 플레이어에게 처치 지원 점수를 지급한다.
     *
     * @param victim 피격자
     */
    public void applyAssistScore(@NonNull CombatUser victim) {
        Timestamp expiration = assistScoreTimeLimitTimestampMap.get(victim);
        if (expiration != null && expiration.isAfter(Timestamp.now()))
            combatUser.addScore("처치 지원", PalasA1Info.ASSIST_SCORE);
    }

    /**
     * 기절 상태 효과 클래스.
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class PalasA1Stun extends Stun {
        private static final PalasA1Stun instance = new PalasA1Stun();

        @Override
        public void onTick(@NonNull Damageable combatEntity, @NonNull CombatEntity provider, long i) {
            super.onTick(combatEntity, provider, i);

            if (combatEntity instanceof CombatUser)
                CombatUtil.addYawAndPitch(combatEntity.getEntity(),
                        (DMGR.getRandom().nextDouble() - DMGR.getRandom().nextDouble()) * 20,
                        (DMGR.getRandom().nextDouble() - DMGR.getRandom().nextDouble()) * 20);

            if (i % 2 == 0) {
                PalasA1Info.PARTICLE.TICK.play(combatEntity.getCenterLocation());
                PalasA1Info.SOUND.TICK.play(combatEntity.getEntity().getLocation());
            }
        }
    }

    private final class PalasA1Projectile extends Projectile<Damageable> {
        private PalasA1Projectile() {
            super(combatUser, PalasA1Info.VELOCITY, CombatUtil.EntityCondition.enemy(combatUser));
        }

        @Override
        @NonNull
        protected IntervalHandler getIntervalHandler() {
            return createPeriodIntervalHandler(8, PalasA1Info.PARTICLE.BULLET_TRAIL::play);
        }

        @Override
        @NonNull
        protected HitBlockHandler getHitBlockHandler() {
            return (location, hitBlock) -> false;
        }

        @Override
        @NonNull
        protected HitEntityHandler<Damageable> getHitEntityHandler() {
            return (location, target) -> {
                if (target.getDamageModule().damage(this, PalasA1Info.DAMAGE, DamageType.NORMAL, location, false, true)) {
                    if (target.getDamageModule().isLiving()) {
                        target.getStatusEffectModule().applyStatusEffect(combatUser, PalasA1Stun.instance, PalasA1Info.STUN_DURATION);

                        PalasA1Info.PARTICLE.HIT_ENTITY.play(target.getCenterLocation(), target.getEntity().getWidth(), target.getEntity().getHeight());
                    }

                    PalasA1Info.SOUND.HIT_ENTITY.play(location);

                    if (target instanceof CombatUser) {
                        combatUser.addScore("적 기절시킴", PalasA1Info.DAMAGE_SCORE);
                        assistScoreTimeLimitTimestampMap.put((CombatUser) target, Timestamp.now().plus(Timespan.ofTicks(PalasA1Info.STUN_DURATION)));
                    }
                }

                return false;
            };
        }
    }
}
