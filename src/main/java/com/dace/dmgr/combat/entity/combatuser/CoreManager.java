package com.dace.dmgr.combat.entity.combatuser;

import com.dace.dmgr.item.DefinedItem;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;

import java.util.EnumSet;

/**
 * 전투 시스템 플레이어의 코어 적용을 관리하는 클래스.
 */
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public final class CoreManager {
    /** 코어 장착 인벤토리 슬롯 목록 */
    private static final int[] CORE_INVENTORY_SLOT = {27, 28, 29};
    /** 장착한 코어 목록 */
    private final EnumSet<Core> cores = EnumSet.noneOf(Core.class);
    /** 플레이어 인스턴스 */
    private final CombatUser combatUser;

    /**
     * 지정한 코어를 장착한 상태인지 확인한다.
     *
     * @param core 확인할 코어
     * @return 장착하고 있으면 {@code true} 반환
     */
    public boolean has(@NonNull Core core) {
        return cores.contains(core);
    }

    /**
     * 지정한 코어를 장착한 코어 목록에 추가한다.
     *
     * @param core 추가할 코어
     * @return 추가 성공 시 {@code true} 반환
     */
    public boolean add(@NonNull Core core) {
        if (cores.size() >= CORE_INVENTORY_SLOT.length || !cores.add(core))
            return false;

        for (int i : CORE_INVENTORY_SLOT) {
            if (combatUser.getUser().getGui().get(i) == null) {
                combatUser.getUser().getGui().set(i, core.getSelectItem());
                break;
            }
        }

        core.onAdd(combatUser);
        return true;
    }

    /**
     * 지정한 코어를 장착한 코어 목록에서 제거한다.
     *
     * @param core 제거할 코어
     * @return 제거 성공 시 {@code true} 반환
     */
    public boolean remove(@NonNull Core core) {
        if (cores.isEmpty() || !cores.remove(core))
            return false;

        for (int i : CORE_INVENTORY_SLOT) {
            DefinedItem definedItem = combatUser.getUser().getGui().get(i);
            if (definedItem == core.getSelectItem()) {
                combatUser.getUser().getGui().remove(i);
                break;
            }
        }

        core.onRemove(combatUser);
        return true;
    }

    /**
     * 장착한 코어 목록을 초기화한다.
     */
    void clear() {
        cores.clear();

        for (int i : CORE_INVENTORY_SLOT)
            combatUser.getUser().getGui().remove(i);
    }
}
