package com.tchristofferson.adaptivemarkets;

import com.tchristofferson.adaptivemarkets.citizentraits.MerchantTrait;
import com.tchristofferson.pagedinventories.IPagedInventory;
import com.tchristofferson.pagedinventories.PagedInventoryAPI;
import com.tchristofferson.pagedinventories.navigationitems.CloseNavigationItem;
import com.tchristofferson.pagedinventories.navigationitems.NavigationItem;
import com.tchristofferson.pagedinventories.navigationitems.NextNavigationItem;
import com.tchristofferson.pagedinventories.navigationitems.PreviousNavigationItem;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.SpawnReason;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.HashMap;
import java.util.Map;

public class Merchant {

    private static final int INV_SIZE = 54;

    private final String merchantType;
    private String displayName;
    private final PagedInventoryAPI pagedInventoryAPI;
    private final IPagedInventory buyInventory;
    private final IPagedInventory sellInventory;

    public Merchant(String merchantType, String displayName, PagedInventoryAPI pagedInventoryAPI) {
        this.merchantType = merchantType;
        this.displayName = displayName;
        this.pagedInventoryAPI = pagedInventoryAPI;

        Map<Integer, NavigationItem> navigation = getNavigation();

        this.buyInventory = pagedInventoryAPI.createPagedInventory(navigation);
        this.sellInventory = pagedInventoryAPI.createPagedInventory(navigation);

        this.buyInventory.addPage(Bukkit.createInventory(null, INV_SIZE));
        this.sellInventory.addPage(Bukkit.createInventory(null, INV_SIZE));
    }

    public String getMerchantType() {
        return merchantType;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public PagedInventoryAPI getPagedInventoryAPI() {
        return pagedInventoryAPI;
    }

    public IPagedInventory getBuyInventory() {
        return buyInventory;
    }

    public IPagedInventory getSellInventory() {
        return sellInventory;
    }

    //TODO: Add NPC to merchant registry
    public Villager spawn(Location location) {
        NPC npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.VILLAGER, displayName);
        npc.addTrait(new MerchantTrait());
        npc.spawn(location, SpawnReason.PLUGIN);
        setMerchantProperties(npc);

        return (Villager) npc.getEntity();
    }

    public void openBuyInventory(Player player) {
        buyInventory.open(player);
    }

    public void openSellInventory(Player player) {
        sellInventory.open(player);
    }

    public void addBuyItem(ItemStack itemStack) {
        //TODO
    }

    public void addSellItem(ItemStack itemStack) {
        //TODO
    }

    public void removeBuyItem(int slot) {
        //TODO
    }

    public void removeSellItem(int slot) {
        //TODO
    }

    private void setMerchantProperties(NPC npc) {
        npc.setProtected(true);
        npc.getNavigator().setPaused(true);
    }

    private Map<Integer, NavigationItem> getNavigation() {
        ItemStack previous = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta previousMeta = (SkullMeta) previous.getItemMeta();
        previousMeta.setOwner("MHF_ArrowLeft");
        previousMeta.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + "<--- Previous Page");
        previous.setItemMeta(previousMeta);

        ItemStack next = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta nextMeta = (SkullMeta) next.getItemMeta();
        nextMeta.setOwner("MHF_ArrowRight");
        nextMeta.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + "Next Page --->");
        next.setItemMeta(nextMeta);

        ItemStack close = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = close.getItemMeta();
        closeMeta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "Close");
        close.setItemMeta(closeMeta);

        Map<Integer, NavigationItem> navigation = new HashMap<>();
        navigation.put(Merchant.INV_SIZE - 9, new PreviousNavigationItem(previous));
        navigation.put(Merchant.INV_SIZE - 5, new CloseNavigationItem(close));
        navigation.put(Merchant.INV_SIZE - 1, new NextNavigationItem(next));

        return navigation;
    }
}
