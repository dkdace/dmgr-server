package com.dace.dmgr.combat.combatant.palas;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.ability.ActionKey;
import com.dace.dmgr.combat.ability.TextIcon;
import com.dace.dmgr.combat.ability.info.AbilityInfoLore;
import com.dace.dmgr.combat.ability.info.AbilityInfoLore.Section.Format;
import com.dace.dmgr.combat.ability.info.ActiveSkillInfo;
import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.combat.entity.combatuser.CombatScore;
import com.dace.dmgr.combat.entity.combatuser.ScreenShake;
import com.dace.dmgr.effect.ParticleEffect;
import com.dace.dmgr.effect.PlayableEffect;
import com.dace.dmgr.effect.SoundEffect;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;

public final class PalasA1Info extends ActiveSkillInfo<PalasA1> {
    /** 쿨타임 */
    public static final Timespan COOLDOWN = Timespan.ofSeconds(12);
    /** 전역 쿨타임 */
    public static final Timespan GLOBAL_COOLDOWN = Timespan.ofSeconds(1.2);
    /** 시전 시간 */
    public static final Timespan READY_DURATION = Timespan.ofSeconds(0.3);
    /** 피해량 */
    public static final int DAMAGE = 10;
    /** 투사체 속력 (단위: 블록/s) */
    public static final int VELOCITY = 50;
    /** 기절 시간 */
    public static final Timespan STUN_DURATION = Timespan.ofSeconds(1.8);
    /** 흔들림 */
    public static final ScreenShake SHAKE = new ScreenShake(20, 20, Timespan.ofTicks(1));

    /** 피해 점수 */
    public static final CombatScore DAMAGE_SCORE = new CombatScore("적 기절시킴", 8);
    /** 처치 지원 점수 */
    public static final CombatScore ASSIST_SCORE = new CombatScore("처치 지원", 20);

    @Getter
    private static final PalasA1Info instance = new PalasA1Info();

    private PalasA1Info() {
        super(PalasA1.class, "테이저건",
                new AbilityInfoLore(AbilityInfoLore.Section
                        .builder("테이저건을 발사하여 약간의 <:DAMAGE:피해>를 입히고 <:STUN:기절>시킵니다.")
                        .addValueInfo(TextIcon.COOLDOWN, Format.TIME, COOLDOWN.toSeconds())
                        .addValueInfo(TextIcon.DAMAGE, DAMAGE)
                        .addValueInfo(TextIcon.STUN, Format.TIME, STUN_DURATION.toSeconds())
                        .addActionKeyInfo("사용", ActionKey.SLOT_1)
                        .build()));
    }

    /**
     * 효과 정보.
     */
    @UtilityClass
    public static final class Effects {
        /** 색상 */
        public static final Color COLOR = Color.fromRGB(240, 230, 50);

        /** 사용 */
        public static final SoundEffect USE =
                SoundEffect.builder(Sound.ENTITY_CAT_PURREOW).volume(0.5).pitch(1.6).build();
        /** 사용 준비 */
        public static final PlayableEffect USE_READY = PlayableEffect.list(
                SoundEffect.builder(Sound.ENTITY_ARROW_SHOOT).volume(1.5).pitch(0.5).build(),
                SoundEffect.builder("random.gun.m1911_silencer").volume(1.5).pitch(0.8).build());
        /** 총알 궤적 */
        public static final PlayableEffect BULLET_TRAIL = PlayableEffect.list(
                ParticleEffect.Normal.builder(Particle.CRIT).build(),
                ParticleEffect.Colored.builder(ParticleEffect.Colored.ParticleType.REDSTONE, COLOR).build());
        /** 엔티티 타격 효과음 */
        public static final PlayableEffect HIT_ENTITY_SOUND = PlayableEffect.list(
                SoundEffect.builder(Sound.ENTITY_FIREWORK_TWINKLE).volume(2).pitch(1.8).build(),
                SoundEffect.builder(Sound.ENTITY_FIREWORK_BLAST).volume(2).pitch(1.6).build(),
                SoundEffect.builder("random.stab").volume(2).pitch(2).build());
        /** 엔티티 타격 입자 효과 */
        public static final PlayableEffect.Function<CombatEntity> HIT_ENTITY_PARTICLE = combatEntity ->
                ParticleEffect.Colored.builder(ParticleEffect.Colored.ParticleType.REDSTONE, COLOR).count(20)
                        .horizontalSpread(combatEntity.getWidth() * 0.5).verticalSpread(combatEntity.getHeight() * 0.5).build();
        /** 기절 - 틱 효과 - 1 */
        public static final PlayableEffect STUN_TICK_1 = PlayableEffect.list(
                SoundEffect.builder(Sound.ENTITY_FIREWORK_BLAST).volume(2).pitch(1.6).build(),
                SoundEffect.builder(Sound.ENTITY_FIREWORK_BLAST).volume(2).pitch(1.8).build());
        /** 기절 - 틱 효과 - 2 */
        public static final ParticleEffect STUN_TICK_2 =
                ParticleEffect.Normal.builder(Particle.CRIT).count(20).speed(0.6).build();

        /**
         * 기절 - 틱 효과를 재생한다.
         *
         * @param location 사용 위치
         * @param center   중심 위치
         */
        public static void playStunTick(@NonNull Location location, @NonNull Location center) {
            STUN_TICK_1.play(location);
            STUN_TICK_2.play(center);
        }
    }
}
