package com.dace.dmgr.combat.entity.combatuser;

import com.dace.dmgr.GeneralConfig;
import com.dace.dmgr.Timespan;
import com.dace.dmgr.Timestamp;
import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.effect.FireworkEffect;
import com.dace.dmgr.effect.SoundEffect;
import com.dace.dmgr.effect.TextHologram;
import com.dace.dmgr.game.Game;
import com.dace.dmgr.util.LocationUtil;
import com.dace.dmgr.util.task.IntervalTask;
import lombok.NonNull;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.jetbrains.annotations.MustBeInvokedByOverriders;

import java.text.MessageFormat;
import java.util.HashMap;

/**
 * 쿨타임이 있는 기능 블록을 나타내는 클래스.
 */
public abstract class CooldownBlock extends FunctionalBlock {
    /** 위치별 쿨타임 기능 블록 목록 (위치 : 쿨타임 기능 블록) */
    private static final HashMap<Location, Timestamp> BLOCK_MAP = new HashMap<>();

    static {
        new HealPack();
        new UltPack();
    }

    /** 쿨타임 */
    private final Timespan cooldown;

    /**
     * 쿨타임 기능 블록 인스턴스를 생성하고 등록한다.
     *
     * @param material 블록 타입
     * @param cooldown 쿨타임
     * @throws IllegalStateException 해당 블록 타입의 CooldownBlock이 이미 등록되었으면 발생
     */
    protected CooldownBlock(@NonNull Material material, @NonNull Timespan cooldown) {
        super(material);
        this.cooldown = cooldown;
    }

    @Override
    @MustBeInvokedByOverriders
    protected boolean canUse(@NonNull CombatUser combatUser, @NonNull Location location) {
        return !BLOCK_MAP.containsKey(location);
    }

    @MustBeInvokedByOverriders
    protected void onUse(@NonNull CombatUser combatUser, @NonNull Location location) {
        BLOCK_MAP.put(location, Timestamp.now().plus(cooldown));

        Game game = combatUser.getGame();
        Location hologramLoc = location.clone().add(0.5, 1.7, 0.5);
        TextHologram hologram = new TextHologram(hologramLoc, player -> LocationUtil.canPass(player.getEyeLocation(), hologramLoc));

        long durationTicks = cooldown.divide(5).toTicks();

        new IntervalTask(i -> {
            hologram.setContent(MessageFormat.format("§f§l[ §6{0} {1} §f§l]",
                    TextIcon.COOLDOWN,
                    Math.ceil(Timespan.ofTicks((durationTicks - i) * 5).toSeconds())));

            return game == null || !game.isFinished();
        }, isCancelled -> {
            hologram.remove();
            BLOCK_MAP.remove(location);
        }, 5, durationTicks);
    }


    /**
     * 힐 팩 블록 클래스.
     */
    private static final class HealPack extends CooldownBlock {
        /** 사용 효과음 */
        private static final SoundEffect USE_SOUND = new SoundEffect(
                SoundEffect.SoundInfo.builder(Sound.ENTITY_ZOMBIE_VILLAGER_CONVERTED).volume(0.5).pitch(1.2).build());

        private HealPack() {
            super(GeneralConfig.getCombatConfig().getHealPackBlock(), GeneralConfig.getCombatConfig().getHealPackCooldown());
        }

        @Override
        protected boolean canUse(@NonNull CombatUser combatUser, @NonNull Location location) {
            return super.canUse(combatUser, location) && !combatUser.getDamageModule().isFullHealth();
        }

        @Override
        protected void onUse(@NonNull CombatUser combatUser, @NonNull Location location) {
            super.onUse(combatUser, location);

            combatUser.getDamageModule().heal(combatUser, GeneralConfig.getCombatConfig().getHealPackHeal(), false);
            combatUser.getCombatantType().getCombatant().onUseHealPack(combatUser);

            USE_SOUND.play(location);
        }
    }

    /**
     * 궁극기 팩 블록 클래스.
     */
    private static final class UltPack extends CooldownBlock {
        /** 사용 입자 효과 */
        private static final FireworkEffect USE_PARTICLE = FireworkEffect.builder(org.bukkit.FireworkEffect.Type.BALL,
                Color.fromRGB(48, 85, 251)).fadeColor(Color.fromRGB(255, 255, 255)).build();
        /** 사용 효과음 */
        private static final SoundEffect USE_SOUND = new SoundEffect(
                SoundEffect.SoundInfo.builder(Sound.BLOCK_BREWING_STAND_BREW).volume(1).pitch(2).build(),
                SoundEffect.SoundInfo.builder(Sound.ITEM_ARMOR_EQUIP_GOLD).volume(1).pitch(1.3).build());

        private UltPack() {
            super(GeneralConfig.getCombatConfig().getUltPackBlock(), GeneralConfig.getCombatConfig().getUltPackCooldown());
        }

        @Override
        protected boolean canUse(@NonNull CombatUser combatUser, @NonNull Location location) {
            return super.canUse(combatUser, location) && (combatUser.getGame() == null || combatUser.getGame().isUltPackActivated())
                    && combatUser.getUltGaugePercent() < 1;
        }

        @Override
        protected void onUse(@NonNull CombatUser combatUser, @NonNull Location location) {
            super.onUse(combatUser, location);

            combatUser.addUltGauge(GeneralConfig.getCombatConfig().getUltPackCharge());

            USE_PARTICLE.play(location.clone().add(0.5, 1.1, 0.5));
            USE_SOUND.play(location);
        }
    }
}
