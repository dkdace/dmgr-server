package com.dace.dmgr.combat.entity.temporary.spawnhandler;

import com.dace.dmgr.PlayerSkin;
import com.dace.dmgr.combat.entity.temporary.TemporaryEntity;
import com.dace.dmgr.user.User;
import com.dace.dmgr.util.EntityUtil;
import lombok.NonNull;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.MemoryNPCDataStore;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.trait.SkinTrait;
import net.skinsrestorer.api.property.IProperty;
import org.apache.commons.lang3.RandomStringUtils;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

/**
 * 플레이어 NPC의 생성을 처리하는 클래스.
 *
 * <p>실제 플레이어가 아니므로 {@link User#fromPlayer(Player)}를 호출하면 안 된다.</p>
 */
public final class PlayerNPCSpawnHandler implements EntitySpawnHandler<Player> {
    /** Citizens NPC 저장소 인스턴스 */
    private static final NPCRegistry NPC_REGISTRY = CitizensAPI.createAnonymousNPCRegistry(new MemoryNPCDataStore());

    /** Citizens NPC 인스턴스 */
    private final NPC npc;
    /** 이름표 숨기기용 갑옷 거치대 인스턴스 */
    @Nullable
    private ArmorStand nameTagHider;

    /**
     * 플레이어 NPC 생성 처리기 인스턴스를 생성한다.
     *
     * @param playerSkin 플레이어 스킨
     */
    public PlayerNPCSpawnHandler(@NonNull PlayerSkin playerSkin) {
        this.npc = NPC_REGISTRY.createNPC(EntityType.PLAYER, RandomStringUtils.randomAlphanumeric(8));
        this.npc.data().set(NPC.Metadata.NAMEPLATE_VISIBLE, false);

        IProperty property = playerSkin.toProperty();
        this.npc.getOrAddTrait(SkinTrait.class).setSkinPersistent(playerSkin.toString(), property.getSignature(), property.getValue());
    }

    /**
     * 지정한 엔티티에 해당하는 Citizens NPC 인스턴스를 반환한다.
     *
     * @param combatEntity 대상 엔티티
     * @return Citizens NPC 인스턴스
     */
    @NonNull
    public static NPC getNPC(@NonNull TemporaryEntity<@NonNull Player> combatEntity) {
        return NPC_REGISTRY.getNPC(combatEntity.getEntity());
    }

    /**
     * 생성된 모든 Citizens NPC를 제거한다.
     *
     * <p>플러그인 비활성화 시 호출해야 한다.</p>
     */
    public static void clearNPC() {
        NPC_REGISTRY.deregisterAll();
    }

    @Override
    @NonNull
    public Player createEntity(@NonNull Location spawnLocation) {
        if (!npc.spawn(spawnLocation)) {
            npc.destroy();
            throw new IllegalStateException("해당 위치에 엔티티를 소환할 수 없음");
        }

        nameTagHider = EntityUtil.createTemporaryArmorStand(spawnLocation);
        npc.getEntity().addPassenger(this.nameTagHider);

        return (Player) npc.getEntity();
    }

    @Override
    public void onSpawn(@NonNull TemporaryEntity<Player> combatEntity) {
        combatEntity.addOnRemove(() -> {
            if (nameTagHider != null)
                nameTagHider.remove();

            npc.destroy();
        });
    }
}
