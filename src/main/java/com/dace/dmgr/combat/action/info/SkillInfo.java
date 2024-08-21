package com.dace.dmgr.combat.action.info;

import com.dace.dmgr.ConsoleLogger;
import com.dace.dmgr.combat.action.skill.Skill;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.item.ItemBuilder;
import lombok.NonNull;
import org.bukkit.Material;

/**
 * 스킬 정보를 관리하는 클래스.
 *
 * @param <T> {@link Skill}을 상속받는 스킬
 */
public abstract class SkillInfo<T extends Skill> extends ActionInfo {
    /** 스킬 아이템 타입 */
    public static final Material MATERIAL = Material.STAINED_GLASS_PANE;
    /** 스킬 이름의 접두사 */
    private static final String PREFIX = "§e§l[스킬] §c";
    /** 스킬 클래스 */
    private final Class<T> skillClass;

    /**
     * 스킬 정보 인스턴스를 생성한다.
     *
     * @param skillClass 스킬 클래스
     * @param name       이름
     * @param lores      설명 목록
     */
    protected SkillInfo(@NonNull Class<@NonNull T> skillClass, @NonNull String name, @NonNull String @NonNull ... lores) {
        super(name, new ItemBuilder(MATERIAL)
                .setName(PREFIX + name)
                .setDamage((short) 15)
                .setLore(lores)
                .build());
        this.skillClass = skillClass;
    }

    /**
     * 스킬 인스턴스를 생성하여 반환한다.
     *
     * <p>스킬 클래스는 다음과 같이 {@link CombatUser} 하나를 인자로 받는
     * 생성자를 가지는 형태로 구현해야 한다.</p>
     *
     * <p>Example:</p>
     *
     * <pre>{@code
     *
     * public final class TestSkill extends AbstractSkill {
     *     public TestSkill(CombatUser combatUser) {
     *         super(combatUser, TestSkillInfo.getInstance());
     *     }
     * }
     *
     * }</pre>
     *
     * @param combatUser 플레이어 객체
     * @return 스킬 객체
     * @throws UnsupportedOperationException 해당 스킬을 생성할 수 없으면 발생
     */
    @NonNull
    public final T createSkill(@NonNull CombatUser combatUser) {
        try {
            return skillClass.getConstructor(CombatUser.class).newInstance(combatUser);
        } catch (NoSuchMethodException ex) {
            throw new UnsupportedOperationException("해당 스킬을 생성할 수 없음");
        } catch (Exception ex) {
            ConsoleLogger.severe("스킬 인스턴스 생성 실패", ex);
        }

        throw new UnsupportedOperationException("스킬 생성을 사용할 수 없음");
    }
}
