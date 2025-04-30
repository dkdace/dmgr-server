package com.dace.dmgr.combat.entity.combatuser;

import com.dace.dmgr.GeneralConfig;
import com.dace.dmgr.Timespan;
import com.dace.dmgr.Timestamp;
import com.dace.dmgr.effect.SoundEffect;
import lombok.NonNull;
import org.apache.commons.lang3.Validate;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

import java.util.EnumMap;

/**
 * 플레이어가 밟았을 때 특정 기능이 실행되는 블록(힐 팩, 궁극기 팩, 점프대 등)을 나타내는 클래스.
 *
 * @see CooldownBlock
 */
public abstract class FunctionalBlock {
    /** 블록 타입별 기능 블록 목록 (블록 타입 : 기능 블록) */
    private static final EnumMap<Material, FunctionalBlock> FUNCTIONAL_BLOCK_MAP = new EnumMap<>(Material.class);

    static {
        new JumpPad();
        new FallZone();
    }

    /**
     * 기능 블록 인스턴스를 생성하고 등록한다.
     *
     * @param material 블록 타입
     * @throws IllegalStateException 해당 블록 타입의 FunctionalBlock이 이미 등록되었으면 발생
     */
    protected FunctionalBlock(@NonNull Material material) {
        Validate.validState(FUNCTIONAL_BLOCK_MAP.put(material, this) == null, "FunctionalBlock이 이미 등록됨");
    }

    /**
     * 지정한 위치에 기능 블록이 존재하면 해당 기능 블록을 사용한다.
     *
     * @param location   대상 위치
     * @param combatUser 사용한 플레이어
     */
    public static void use(@NonNull Location location, @NonNull CombatUser combatUser) {
        Block block = location.getBlock();
        location = block.getLocation();

        FunctionalBlock functionalBlock = FUNCTIONAL_BLOCK_MAP.get(block.getType());

        if (functionalBlock != null && functionalBlock.canUse(combatUser, location))
            functionalBlock.onUse(combatUser, location);
    }

    /**
     * 기능 블록을 사용할 수 있는지 확인한다.
     *
     * @param combatUser 사용한 플레이어
     * @param location   블록 위치
     * @return 사용 가능 여부
     */
    protected abstract boolean canUse(@NonNull CombatUser combatUser, @NonNull Location location);

    /**
     * 기능 블록을 사용했을 때 실행할 작업.
     *
     * @param combatUser 사용한 플레이어
     * @param location   블록 위치
     */
    protected abstract void onUse(@NonNull CombatUser combatUser, @NonNull Location location);

    /**
     * 점프대 블록 클래스.
     */
    private static final class JumpPad extends FunctionalBlock {
        /** 사용 효과음 */
        private static final SoundEffect USE_SOUND = new SoundEffect(
                SoundEffect.SoundInfo.builder(Sound.ENTITY_PLAYER_SMALL_FALL).volume(1.5).pitch(1.5).pitchVariance(0.1).build(),
                SoundEffect.SoundInfo.builder(Sound.ENTITY_ITEM_PICKUP).volume(1.5).pitch(0.8).pitchVariance(0.05).build(),
                SoundEffect.SoundInfo.builder(Sound.ENTITY_ITEM_PICKUP).volume(1.5).pitch(1.4).pitchVariance(0.05).build());

        private JumpPad() {
            super(GeneralConfig.getCombatConfig().getJumpPadBlock());
        }

        @Override
        protected boolean canUse(@NonNull CombatUser combatUser, @NonNull Location location) {
            return combatUser.getJumpPadCooldownTimestamp().isBefore(Timestamp.now());
        }

        @Override
        protected void onUse(@NonNull CombatUser combatUser, @NonNull Location location) {
            combatUser.setJumpPadCooldownTimestamp(Timestamp.now().plus(Timespan.ofTicks(10)));

            combatUser.getMoveModule().push(new Vector(0, GeneralConfig.getCombatConfig().getJumpPadVelocity(), 0), true);
            USE_SOUND.play(location);
        }
    }

    /**
     * 낙사 구역 블록 클래스.
     */
    private static final class FallZone extends FunctionalBlock {
        private FallZone() {
            super(GeneralConfig.getCombatConfig().getFallZoneBlock());
        }

        @Override
        protected boolean canUse(@NonNull CombatUser combatUser, @NonNull Location location) {
            return true;
        }

        @Override
        protected void onUse(@NonNull CombatUser combatUser, @NonNull Location location) {
            combatUser.setFallZoneTimestamp(Timestamp.now().plus(Timespan.ofTicks(10)));
            combatUser.onDeath(null);
        }
    }
}
