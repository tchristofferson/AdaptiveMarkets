package com.tchristofferson.adaptivemarkets.core;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.NPCRemoveEvent;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.trait.VillagerProfession;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class MerchantManager implements Listener {

    private final Map<String, Merchant> merchantTypes;//<String merchantType, Merchant merchant>
    private final Map<String, List<NPC>> spawnedMerchants;//<String merchantType, List<NPC> spawnedMerchants>
    private final Map<UUID, String> merchantsUuidAndTypes;//<UUID npcUuid, String merchantType>
    private final Map<UUID, Merchant> playersInNavigationPage;//<UUID playerUuid, Merchant merchant>, keeps track of which merchant was clicked while the player is in navigationPage
    private final Inventory navigationPage;

    public MerchantManager(Map<String, Merchant> merchantTypes, Map<String, List<NPC>> spawnedMerchants) {
        this.merchantTypes = merchantTypes;
        this.spawnedMerchants = spawnedMerchants;
        this.merchantsUuidAndTypes = new HashMap<>();
        this.playersInNavigationPage = new HashMap<>();
        this.navigationPage = Bukkit.createInventory(null, 27, ChatColor.GRAY + "Merchant Menu");

        spawnedMerchants.forEach((merchantType, npcs) -> npcs.forEach(npc -> merchantsUuidAndTypes.put(npc.getUniqueId(), merchantType)));

        ItemStack buyButton = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
        ItemMeta buyMeta = buyButton.getItemMeta();
        buyMeta.setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + "Buy Menu");
        buyButton.setItemMeta(buyMeta);

        ItemStack sellButton = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta sellMeta = sellButton.getItemMeta();
        sellMeta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "Sell Menu");
        sellButton.setItemMeta(sellMeta);

        navigationPage.setItem(12, buyButton);
        navigationPage.setItem(14, sellButton);
    }

    public Merchant getInteractingMerchant(Player player) {
        return playersInNavigationPage.get(player.getUniqueId());
    }

    public Merchant removeInteractingMerchant(Player player) {
        return playersInNavigationPage.remove(player.getUniqueId());
    }

    public Merchant getMerchant(String merchantType) {
        return merchantTypes.get(formatType(merchantType));
    }

    public Collection<String> getMerchantTypes() {
        return merchantTypes.keySet();
    }

    public Inventory getNavigationPage() {
        return navigationPage;
    }

    public boolean addMerchant(String merchantType, Merchant merchant) {
        merchantType = formatType(merchantType);
        if (merchantTypes.containsKey(merchantType)) return false;
        merchantTypes.put(merchantType, merchant);
        spawnedMerchants.put(merchantType, new ArrayList<>());
        return true;
    }

    public boolean removeMerchant(String merchantType) {
        merchantType = formatType(merchantType);
        boolean success = merchantTypes.remove(merchantType) != null;

        if (success) {
            List<NPC> spawned = spawnedMerchants.get(merchantType);

            if (spawned != null) {
                if (!spawned.isEmpty()) {
                    spawned.forEach(npc -> {
                        merchantsUuidAndTypes.remove(npc.getUniqueId());
                        npc.destroy();
                    });
                }

                spawnedMerchants.remove(merchantType);
            }
        }

        return success;
    }

    public NPC create(String merchantType, Location location) {
        Merchant merchant = merchantTypes.get(formatType(merchantType));

        if (merchant == null)
            return null;

        return create(merchant, location);
    }

    public NPC create(Merchant merchant, Location location) {
        if (!merchantTypes.containsValue(merchant))
            throw new IllegalStateException("Cannot spawn a merchant that is unregistered!");

        NPC npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.VILLAGER, merchant.getDisplayName());
        npc.setProtected(true);
        npc.getNavigator().setPaused(true);
        npc.getTrait(VillagerProfession.class).setProfession(merchant.getProfession());
        npc.spawn(location);
        Villager villager = (Villager) npc.getEntity();
        villager.setVillagerType(merchant.getType());
        spawnedMerchants.get(merchant.getMerchantType()).add(npc);
        merchantsUuidAndTypes.put(npc.getUniqueId(), merchant.getMerchantType());

        return npc;
    }

    public void destroy(NPC npc) {
        String merchantType = merchantsUuidAndTypes.remove(npc.getUniqueId());
        spawnedMerchants.get(merchantType).remove(npc);
        npc.destroy();
    }

    private String formatType(String type) {
        return type.trim().toUpperCase();
    }

    @EventHandler
    public void onNPCRemove(NPCRemoveEvent event) {
        NPC npc = event.getNPC();

        if (merchantsUuidAndTypes.containsKey(npc.getUniqueId())) {
            destroy(npc);
        }
    }

    @EventHandler
    public void onNPCRightClickEvent(NPCRightClickEvent event) {
        NPC npc = event.getNPC();

        String merchantType = merchantsUuidAndTypes.get(npc.getUniqueId());

        if (merchantType != null) {
            event.setCancelled(true);
            Player player = event.getClicker();
            playersInNavigationPage.put(player.getUniqueId(), merchantTypes.get(merchantType));
            player.openInventory(navigationPage);
        }
    }
}
