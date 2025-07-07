package com.dace.dmgr.combat.combatant.vellion;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.ability.ActionKey;
import com.dace.dmgr.combat.ability.TextIcon;
import com.dace.dmgr.combat.ability.info.AbilityInfoLore;
import com.dace.dmgr.combat.ability.info.AbilityInfoLore.Section.Format;
import com.dace.dmgr.combat.ability.info.ActiveSkillInfo;
import com.dace.dmgr.combat.entity.combatuser.CombatScore;
import com.dace.dmgr.effect.ParticleEffect;
import com.dace.dmgr.effect.PlayableEffect;
import com.dace.dmgr.effect.SoundEffect;
import com.dace.dmgr.util.VectorUtil;
import com.dace.dmgr.util.location.LocationUtil;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.util.Vector;

public final class VellionA2Info extends ActiveSkillInfo<VellionA2> {
    /** 쿨타임 */
    public static final Timespan COOLDOWN = Timespan.ofSeconds(5);
    /** 시전 시간 */
    public static final Timespan READY_DURATION = Timespan.ofSeconds(0.8);
    /** 시전 중 이동속도 감소량 */
    public static final int READY_SLOW = 70;
    /** 초당 피해량 */
    public static final int DAMAGE_PER_SECOND = 60;
    /** 피해 범위 (단위: 블록) */
    public static final double RADIUS = 8;
    /** 최대 거리 (단위: 블록) */
    public static final int MAX_DISTANCE = 20;
    /** 방어력 감소량 */
    public static final int DEFENSE_DECREMENT = 25;
    /** 대상 위치 통과 불가 시 초기화 제한 시간 */
    public static final Timespan BLOCK_RESET_DELAY = Timespan.ofSeconds(2);

    /** 처치 지원 점수 */
    public static final CombatScore ASSIST_SCORE = new CombatScore("처치 지원", 20);

    @Getter
    private static final VellionA2Info instance = new VellionA2Info();

    private VellionA2Info() {
        super(VellionA2.class, "저주 귀속",
                new AbilityInfoLore(AbilityInfoLore.Section
                        .builder("바라보는 적에게 저주를 걸어 <:DEFENSE_DECREASE:방어력>을 감소시키고 해당 적을 제외한 주변에 지속적인 <:DAMAGE:광역 피해>를 입힙니다. " +
                                "해당 적이 시야에서 " + BLOCK_RESET_DELAY.toSeconds() + "초간 사라지거나 사거리를 벗어나면 저주가 풀립니다.")
                        .addValueInfo(TextIcon.DEFENSE_DECREASE, Format.PERCENT, DEFENSE_DECREMENT)
                        .addValueInfo(TextIcon.DISTANCE, Format.DISTANCE, MAX_DISTANCE)
                        .addValueInfo(TextIcon.DAMAGE, Format.PER_SECOND, DAMAGE_PER_SECOND)
                        .addValueInfo(TextIcon.RADIUS, Format.DISTANCE, RADIUS)
                        .addActionKeyInfo("사용", ActionKey.SLOT_2)
                        .build(),
                        new AbilityInfoLore.NamedSection("취소/재사용 시", AbilityInfoLore.Section
                                .builder("사용을 종료합니다.")
                                .addValueInfo(TextIcon.COOLDOWN, Format.TIME, COOLDOWN.toSeconds())
                                .addActionKeyInfo("해제", ActionKey.SLOT_2)
                                .build())));
    }

    /**
     * 효과 정보.
     */
    @UtilityClass
    public static final class Effects {
        /** 표식 색상 */
        public static final Color MARK_COLOR = Color.fromRGB(160, 150, 152);

        /** 사용 */
        public static final PlayableEffect USE = PlayableEffect.list(
                SoundEffect.builder(Sound.BLOCK_ENCHANTMENT_TABLE_USE).volume(2).pitch(0.8).build(),
                SoundEffect.builder(Sound.BLOCK_ENCHANTMENT_TABLE_USE).volume(2).pitch(0.8).build(),
                SoundEffect.builder("new.entity.squid.squirt").volume(2).pitch(1.2).build());
        /** 사용 시 틱 효과 - 1 */
        public static final PlayableEffect.Function<Long> USE_TICK_1 = i ->
                ParticleEffect.Colored.builder(ParticleEffect.Colored.ParticleType.REDSTONE,
                        Color.fromRGB((int) (200 - i * 4), 130, (int) (230 - i * 5))).build();
        /** 사용 시 틱 효과 - 2 */
        public static final ParticleEffect USE_TICK_2 =
                ParticleEffect.Normal.builder(Particle.SPELL_WITCH).build();
        /** 사용 준비 */
        public static final PlayableEffect USE_READY = PlayableEffect.list(
                SoundEffect.builder(Sound.ENTITY_ELDER_GUARDIAN_CURSE).volume(2).pitch(1).build(),
                SoundEffect.builder("new.block.respawn_anchor.charge").volume(2).pitch(0.8).build());
        /** 표식 */
        public static final PlayableEffect MARK = PlayableEffect.list(
                ParticleEffect.Colored.builder(ParticleEffect.Colored.ParticleType.REDSTONE, MARK_COLOR).count(4).horizontalSpread(0.2)
                        .verticalSpread(0.2).build(),
                ParticleEffect.Colored.builder(ParticleEffect.Colored.ParticleType.SPELL_MOB, MARK_COLOR).build());
        /** 발동 */
        public static final SoundEffect TRIGGER =
                SoundEffect.builder(Sound.BLOCK_END_PORTAL_FRAME_FILL).volume(0.6).pitch(0.7).pitchVariance(0.1).build();
        /** 표식 - 엔티티 타격 - 1 */
        public static final ParticleEffect MARK_HIT_ENTITY_1 =
                ParticleEffect.Normal.builder(Particle.CRIT_MAGIC).count(15).speed(0.3).build();
        /** 표식 - 엔티티 타격 - 2 */
        public static final ParticleEffect MARK_HIT_ENTITY_2 =
                ParticleEffect.Normal.builder(Particle.SMOKE_NORMAL).build();

        /**
         * 사용 시 틱 효과를 재생한다.
         *
         * @param i        인덱스
         * @param location 사용 위치
         * @param target   대상 위치
         */
        public static void playUseTick(long i, @NonNull Location location, @NonNull Location target) {
            for (Location loc : LocationUtil.getLine(location, target, 0.7))
                USE_TICK_1.apply(i).play(loc);

            location = LocationUtil.getLocationFromOffset(location, LocationUtil.getDirection(location, target), 0, 0, 1.5);

            Vector vector = VectorUtil.getYawAxis(location);
            Vector axis = VectorUtil.getRollAxis(location);

            for (long j = (i >= 6 ? i - 6 : 0); j < i; j++) {
                double angle = j * (j > 5 ? 4.0 : 12.0);

                for (int k = 0; k < 8; k++) {
                    angle += 360 / 4.0;
                    Vector vec = VectorUtil.getRotatedVector(vector, axis, k < 4 ? angle : -angle).multiply(j * 0.2);
                    Location loc = location.clone().add(vec);

                    if (i == 15)
                        USE_TICK_2.play(loc);
                    else
                        USE_TICK_1.apply(i).play(loc);
                }
            }
        }

        /**
         * 표식 - 엔티티 타격 효과를 재생한다.
         *
         * @param location 사용 위치
         * @param target   대상 위치
         */
        public static void playMarkHitEntity(@NonNull Location location, @NonNull Location target) {
            MARK_HIT_ENTITY_1.play(target);
            for (Location loc : LocationUtil.getLine(location, target, 0.4))
                MARK_HIT_ENTITY_2.play(loc);
        }
    }
}
