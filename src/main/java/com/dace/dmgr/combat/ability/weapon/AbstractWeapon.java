package com.dace.dmgr.combat.ability.weapon;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.ability.AbstractAction;
import com.dace.dmgr.combat.ability.ActionKey;
import com.dace.dmgr.combat.ability.info.WeaponInfo;
import com.dace.dmgr.combat.entity.CombatRestriction;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import lombok.NonNull;
import org.apache.commons.lang3.Validate;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.MustBeInvokedByOverriders;

/**
 * {@link Weapon}의 기본 구현체, 모든 무기의 기반 클래스.
 */
public abstract class AbstractWeapon extends AbstractAction implements Weapon {
    /** 투명 아이템의 내구도 */
    private static final short INVISIBLE_ITEM_DURABILITY = 1561;
    /** 아이템 인벤토리 칸 번호 */
    private static final int ITEM_SLOT_INDEX = 4;

    /** 무기 아이템 인스턴스 */
    private final ItemStack itemStack;
    /** 무기 아이템 표시 여부 */
    private boolean isVisible = true;

    /**
     * 무기 인스턴스를 생성한다.
     *
     * @param combatUser      사용자 플레이어
     * @param weaponInfo      무기 정보 인스턴스
     * @param defaultCooldown 기본 쿨타임
     */
    protected AbstractWeapon(@NonNull CombatUser combatUser, @NonNull WeaponInfo<?> weaponInfo, @NonNull Timespan defaultCooldown) {
        super(combatUser, weaponInfo.getName(), defaultCooldown);

        this.itemStack = weaponInfo.getDefinedItem().getItemStack();

        combatUser.getEntity().getInventory().setHeldItemSlot(ITEM_SLOT_INDEX);
        combatUser.getEntity().getInventory().setItem(ITEM_SLOT_INDEX, itemStack);

        addOnRemove(() -> combatUser.getEntity().getInventory().clear(ITEM_SLOT_INDEX));
    }

    @Override
    @NonNull
    protected ChatColor getDisplayNameColor() {
        return ChatColor.WHITE;
    }

    @Override
    @MustBeInvokedByOverriders
    protected void onCooldownSet() {
        combatUser.getEntity().setCooldown(WeaponInfo.MATERIAL, (int) Math.min(Integer.MAX_VALUE, getCooldown().toTicks()));
    }

    @Override
    @MustBeInvokedByOverriders
    public boolean canUse(@NonNull ActionKey actionKey) {
        return super.canUse(actionKey)
                && combatUser.isGlobalCooldownFinished()
                && !combatUser.getStatusEffectModule().hasRestriction(CombatRestriction.USE_WEAPON);
    }

    @Override
    public final void setMaterial(@NonNull Material material) {
        itemStack.setType(material);
        display();
    }

    @Override
    public final void setDurability(short durability) {
        Validate.isTrue(durability >= 0, "durability >= 0 (%d)", durability);

        itemStack.setDurability(durability);
        display();
    }

    @Override
    public final void setGlowing(boolean isGlowing) {
        if (isGlowing)
            itemStack.addUnsafeEnchantment(Enchantment.LOOT_BONUS_BLOCKS, 1);
        else
            itemStack.removeEnchantment(Enchantment.LOOT_BONUS_BLOCKS);

        display();
    }

    @Override
    public final void setVisible(boolean isVisible) {
        this.isVisible = isVisible;
        display();
    }

    /**
     * 무기 설명 아이템을 적용한다.
     */
    private void display() {
        ItemStack slotItem = combatUser.getEntity().getInventory().getItem(ITEM_SLOT_INDEX);
        if (slotItem == null)
            return;

        if (!isVisible) {
            ItemStack invisibleItem = itemStack.clone();
            invisibleItem.setDurability(INVISIBLE_ITEM_DURABILITY);

            combatUser.getEntity().getInventory().setItem(ITEM_SLOT_INDEX, invisibleItem);
            return;
        }

        if (!slotItem.equals(itemStack))
            combatUser.getEntity().getInventory().setItem(ITEM_SLOT_INDEX, itemStack);
    }
}
