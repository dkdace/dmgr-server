package com.dace.dmgr.combat.combatant.magritta;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.CombatEffectUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActionInfoLore;
import com.dace.dmgr.combat.action.info.ActionInfoLore.Section.Format;
import com.dace.dmgr.combat.action.info.WeaponInfo;
import com.dace.dmgr.combat.entity.DistantDamage;
import com.dace.dmgr.combat.entity.combatuser.ScreenRecoil;
import com.dace.dmgr.effect.ParticleEffect;
import com.dace.dmgr.effect.PlayableEffect;
import com.dace.dmgr.effect.SoundEffect;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;

public final class MagrittaWeaponInfo extends WeaponInfo<MagrittaWeapon> {
    /** 쿨타임 */
    public static final Timespan COOLDOWN = Timespan.ofSeconds(0.5);
    /** 피해량 */
    public static final int DAMAGE = 40;
    /** 사거리 (단위: 블록) */
    public static final int DISTANCE = 16;
    /** 거리별 피해량 */
    public static final DistantDamage DISTANT_DAMAGE = new DistantDamage(DAMAGE, DISTANCE / 2.0);
    /** 산탄 수 */
    public static final int PELLET_AMOUNT = 8;
    /** 탄퍼짐 */
    public static final double SPREAD = 18;
    /** 장탄수 */
    public static final int CAPACITY = 8;
    /** 재장전 시간 */
    public static final Timespan RELOAD_DURATION = Timespan.ofSeconds(1.8);
    /** 반동 */
    public static final ScreenRecoil RECOIL = new ScreenRecoil(9, 0, 1, 3.2, Timespan.ofTicks(3), 1);

    @Getter
    private static final MagrittaWeaponInfo instance = new MagrittaWeaponInfo();

    private MagrittaWeaponInfo() {
        super(MagrittaWeapon.class, Resource.DEFAULT, "데스페라도",
                new ActionInfoLore(ActionInfoLore.Section
                        .builder("근거리에 강력한 피해를 입히는 산탄총입니다. " +
                                "사격하여 <:DAMAGE:피해>를 입힙니다. " +
                                "산탄이 " + PELLET_AMOUNT / 2 + "발 이상 적중하면 적에게 <d::파쇄>를 적용합니다.")
                        .addValueInfo(TextIcon.DAMAGE, Format.VARIABLE_WITH_DISTANCE + " (×{4})",
                                DAMAGE, DAMAGE / 2, DISTANCE / 2, DISTANCE, PELLET_AMOUNT)
                        .addValueInfo(TextIcon.ATTACK_SPEED, Format.TIME, COOLDOWN.toSeconds())
                        .addValueInfo(TextIcon.CAPACITY, Format.CAPACITY, CAPACITY)
                        .addActionKeyInfo("사격", ActionKey.LEFT_CLICK)
                        .build()));
    }

    /**
     * 리소스별 아이템 내구도 정보.
     */
    @UtilityClass
    public static final class Resource {
        /** 기본 */
        public static final short DEFAULT = 13;
    }

    /**
     * 효과 정보.
     */
    @UtilityClass
    public static final class Effects {
        /** 사용 */
        public static final PlayableEffect USE = PlayableEffect.list(
                SoundEffect.builder("random.gun2.xm1014_1").volume(3).pitch(1).build(),
                SoundEffect.builder("random.gun2.xm1014_1").volume(3).pitch(0.8).build(),
                SoundEffect.builder("random.gun2.spas_12_1").volume(3).pitch(1).build(),
                SoundEffect.builder("random.gun_reverb").volume(5).pitch(0.9).build(),
                SoundEffect.builder("random.gun_reverb").volume(5).pitch(0.8).build());
        /** 탄피 효과음 */
        public static final PlayableEffect BULLET_SHELL =
                CombatEffectUtil.SHOTGUN_SHELL_DROP_SOUND.apply(1.0);
        /** 타격 */
        public static final ParticleEffect HIT =
                ParticleEffect.Normal.builder(Particle.LAVA).build();
        /** 엔티티 타격 */
        public static final ParticleEffect HIT_ENTITY =
                ParticleEffect.Normal.builder(ParticleEffect.BlockParticleType.BLOCK_DUST, Material.BONE_BLOCK, 0).count(4).speed(0.08).build();
        /** 블록 타격 효과음 */
        public static final PlayableEffect.Function<Block> HIT_BLOCK_SOUND = block -> PlayableEffect.list(
                CombatEffectUtil.BULLET_HIT_BLOCK_SOUND,
                CombatEffectUtil.HIT_BLOCK_SOUND.apply(block, 1.0));
        /** 블록 타격 입자 효과 */
        public static final PlayableEffect.Function<Block> HIT_BLOCK_PARTICLE = block ->
                CombatEffectUtil.HIT_BLOCK_SMALL_PARTICLE.apply(block, 1.0);
        /** 재장전 */
        public static final PlayableEffect.Function<Long> RELOAD = i -> {
            switch (i.intValue()) {
                case 3:
                    return SoundEffect.builder(Sound.BLOCK_PISTON_EXTEND).volume(0.6).pitch(1.3).build();
                case 5:
                    return SoundEffect.builder(Sound.ENTITY_VILLAGER_NO).volume(0.6).pitch(1.3).build();
                case 20:
                    return SoundEffect.builder(Sound.ENTITY_PLAYER_HURT).volume(0.6).pitch(0.5).build();
                case 21:
                    return SoundEffect.builder(Sound.ITEM_FLINTANDSTEEL_USE).volume(0.6).pitch(0.8).build();
                case 22:
                    return SoundEffect.builder(Sound.ENTITY_WOLF_SHAKE).volume(0.6).pitch(0.7).build();
                case 28:
                    return SoundEffect.builder(Sound.ENTITY_WOLF_HOWL).volume(0.6).pitch(0.9).build();
                case 33:
                    return SoundEffect.builder(Sound.ENTITY_WOLF_SHAKE).volume(0.6).pitch(0.9).build();
                default:
                    return SoundEffect.NONE;
            }
        };
    }
}
