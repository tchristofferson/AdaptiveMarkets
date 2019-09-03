package com.tchristofferson.adaptivemarkets.core;

import com.tchristofferson.adaptivemarkets.AdaptiveMarkets;
import com.tchristofferson.pagedinventories.IPagedInventory;
import com.tchristofferson.pagedinventories.PagedInventoryAPI;
import com.tchristofferson.pagedinventories.handlers.PagedInventoryClickHandler;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class Merchant implements Cloneable {

    private static final ItemStack NEXT_ITEMSTACK;
    private static final ItemStack PREV_ITEMSTACK;
    private static final ItemStack CLOSE_ITEMSTACK;

    private static final PagedInventoryClickHandler clickHandler;

    static {
        NEXT_ITEMSTACK = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta nextMeta = (SkullMeta) NEXT_ITEMSTACK.getItemMeta();
        nextMeta.setOwner("MHF_ArrowRight");
        NEXT_ITEMSTACK.setItemMeta(nextMeta);

        PREV_ITEMSTACK = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta prevMeta = (SkullMeta) PREV_ITEMSTACK.getItemMeta();
        prevMeta.setOwner("MHF_ArrowLeft");
        PREV_ITEMSTACK.setItemMeta(prevMeta);

        CLOSE_ITEMSTACK = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = CLOSE_ITEMSTACK.getItemMeta();
        closeMeta.setDisplayName(ChatColor.RED + "Close Menu");
        CLOSE_ITEMSTACK.setItemMeta(closeMeta);

        clickHandler = new PagedInventoryClickHandler() {
            private final AdaptiveMarkets adaptiveMarkets = (AdaptiveMarkets) Bukkit.getPluginManager().getPlugin("AdaptiveMarkets");

            @Override
            public void handle(Handler handler) {
                Player player = handler.getPlayer();
                InventoryClickEvent event = handler.getEvent();
                PlayerManager playerManager = adaptiveMarkets.getPlayerManager();
                Runnable runnable;

                if (playerManager.getListeningForBuyRemoval(player) != null) {
                    Merchant merchant = playerManager.removeListeningForBuyRemoval(player);
                    merchant.removeItem(event.getInventory(), event.getSlot(), true);
                    runnable = player::closeInventory;
                } else if (playerManager.getListeningForSellRemoval(player) != null) {
                    Merchant merchant = playerManager.removeListeningForSellRemoval(player);
                    merchant.removeItem(event.getInventory(), event.getSlot(), false);
                    runnable = player::closeInventory;
                } else {
                    Inventory clickedInventory = event.getClickedInventory();
                    ItemStack clickedItem = event.getCurrentItem();
                    if (clickedInventory == null || clickedItem == null || clickedItem.getType().name().endsWith("AIR"))
                        return;

                    Validate.isTrue(clickedItem instanceof MarketItemStack, "The clicked item wasn't an instance of MarketItemStack!");
                    String inventoryName = ChatColor.stripColor(event.getView().getTitle());
                    IPagedInventory pagedInventory = handler.getPagedInventory();
                    int page = -1;

                    for (int i = 0; i < pagedInventory.getSize(); i++) {
                        if (pagedInventory.getPage(i).equals(event.getClickedInventory())) {
                            page = i;
                            break;
                        }
                    }

                    Merchant merchant = playerManager.getCurrentMerchant(player);
                    MarketItemStack marketItemStack = (MarketItemStack) clickedItem;
                    int index = getIndex(page, event.getSlot());

                    if (inventoryName.endsWith(" - Buy")) {
                        //TODO: Use vault to take from player balance
                        if (adaptiveMarkets.getEconomy().getBalance(player) >= marketItemStack.getPrice()) {
                            Bukkit.getScheduler().runTask(adaptiveMarkets, () -> {
                                Map<Integer, ItemStack> notAdded = player.getInventory().addItem(marketItemStack);
                                if (notAdded.isEmpty()) {
                                    adaptiveMarkets.getEconomy().withdrawPlayer(player, marketItemStack.getPrice());
                                    player.sendMessage(ChatColor.GREEN + "Successfully bought item for $" + marketItemStack.getPrice());
                                }
                            });
                        }
                    } else if (inventoryName.endsWith(" - Sell") && !clickedInventory.equals(player.getInventory())) {
                        //TODO: Use vault to add to player's balance
                        Inventory playerInventory = player.getInventory();

                        for (int i = 0; i < playerInventory.getSize(); i++) {
                            ItemStack is = playerInventory.getItem(i);

                            if (marketItemStack.isSimilar(is)) {
                                is.setAmount(is.getAmount() - 1);
                                playerInventory.setItem(i, is);
                                break;
                            }
                        }
                    }

                    return;
                }

                Bukkit.getScheduler().runTask(adaptiveMarkets, runnable);
            }
        };
    }

    private final PagedInventoryAPI pagedInventoryAPI;
    private final List<MarketItemStack> buyItems;
    private final List<MarketItemStack> sellItems;
    private IPagedInventory buyInventory;
    private IPagedInventory sellInventory;
    private String merchantType;
    private String displayName;
    private Villager.Profession profession;
    private Villager.Type type;

    public Merchant(PagedInventoryAPI pagedInventoryAPI, String merchantType, String displayName, List<MarketItemStack> buyItems, List<MarketItemStack> sellItems, Villager.Profession profession, Villager.Type type) {
        this.pagedInventoryAPI = pagedInventoryAPI;
        this.buyInventory = pagedInventoryAPI.createPagedInventory(NEXT_ITEMSTACK, PREV_ITEMSTACK, CLOSE_ITEMSTACK);
        this.sellInventory = pagedInventoryAPI.createPagedInventory(NEXT_ITEMSTACK, PREV_ITEMSTACK, CLOSE_ITEMSTACK);
        this.merchantType = merchantType.trim().toUpperCase();
        this.displayName = ChatColor.translateAlternateColorCodes('&', displayName);
        this.profession = profession;
        this.type = type;
        this.buyInventory.addPage(createInventory(true));
        this.sellInventory.addPage(createInventory(false));
        this.buyItems = new ArrayList<>(buyItems);
        this.sellItems = new ArrayList<>(sellItems);

        loadInventory(this.buyItems, true);
        loadInventory(this.sellItems, false);

        this.pagedInventoryAPI.getRegistrar().addClickHandler(this.buyInventory, clickHandler);
        this.pagedInventoryAPI.getRegistrar().addClickHandler(this.sellInventory, clickHandler);
    }

    public void openBuyInventory(Player player) {
        buyInventory.open(player);
    }

    public void openSellInventory(Player player) {
        sellInventory.open(player);
    }

    public String getMerchantType() {
        return merchantType;
    }

    public void setMerchantType(String merchantType) {
        this.merchantType = merchantType;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Villager.Profession getProfession() {
        return profession;
    }

    public void setProfession(Villager.Profession profession) {
        this.profession = profession;
    }

    public Villager.Type getType() {
        return type;
    }

    public void setType(Villager.Type type) {
        this.type = type;
    }

    public void addBuyItem(MarketItemStack marketItemStack) {
        buyItems.add(marketItemStack);
        addItem(marketItemStack, true);
    }

    public void addSellItem(MarketItemStack marketItemStack) {
        sellItems.add(marketItemStack);
        addItem(marketItemStack, false);
    }

    private void removeItem(Inventory inventory, int slot, boolean isBuyInventory) {
        int index = -1;

        IPagedInventory buyOrSell = isBuyInventory ? buyInventory : sellInventory;
        for (int i = 0; i < buyOrSell.getSize(); i++) {
            if (buyOrSell.getPage(i).equals(inventory)) {
                index = getIndex(i, slot);
                break;
            }
        }

        Validate.isTrue(index != -1, "Specified inventory doesn't belong to merchant's buy inventories!");
        ItemStack itemStack = inventory.getItem(slot);
        inventory.setItem(slot, null);

        if (isBuyInventory) {
            buyItems.removeIf(marketItemStack -> marketItemStack.equals(itemStack));
            reorderBuyInventory();
        } else {
            sellItems.removeIf(marketItemStack -> marketItemStack.equals(itemStack));
            reorderSellInventory();
        }
    }

    private void addItem(MarketItemStack marketItemStack, boolean isBuyItem) {
        Inventory inventory = isBuyItem ? buyInventory.getPage(buyInventory.getSize() - 1) :
                sellInventory.getPage(sellInventory.getSize() - 1);

        if (inventory.firstEmpty() > inventory.getSize() - 10) {
            inventory = createInventory(isBuyItem);

            if (isBuyItem) {
                buyInventory.addPage(inventory);
            } else {
                sellInventory.addPage(inventory);
            }
        }

        setMeta(marketItemStack, isBuyItem);
        inventory.addItem(marketItemStack);
    }

    private void setMeta(MarketItemStack marketItemStack, boolean isBuyItem) {
        ItemMeta itemMeta = marketItemStack.getItemMeta();
        List<String> lore = itemMeta.hasLore() ? itemMeta.getLore() : new ArrayList<>(2);
        lore.add(ChatColor.GREEN + "$" + marketItemStack.getPrice());

        if (isBuyItem) {
            lore.add(ChatColor.GREEN + "" + ChatColor.BOLD + "CLICK TO BUY");
        } else {
            lore.add(ChatColor.GREEN + "" + ChatColor.BOLD + "CLICK TO SELL!");
        }

        itemMeta.setLore(lore);
        marketItemStack.setItemMeta(itemMeta);
    }

    private Inventory createInventory(boolean isBuy) {
        return Bukkit.createInventory(null, 54, isBuy ? displayName + " - Buy" : displayName + " - Sell");
    }

    private void reorderBuyInventory() {
        Map<Player, Integer> viewers = getViewers(buyInventory);
        buyInventory = pagedInventoryAPI.createPagedInventory(NEXT_ITEMSTACK, PREV_ITEMSTACK, CLOSE_ITEMSTACK);
        buyInventory.addPage(createInventory(true));
        loadInventory(this.buyItems, true);
        setViewers(viewers, buyInventory);
    }

    private void reorderSellInventory() {
        Map<Player, Integer> viewers = getViewers(sellInventory);
        sellInventory = pagedInventoryAPI.createPagedInventory(NEXT_ITEMSTACK, PREV_ITEMSTACK, CLOSE_ITEMSTACK);
        sellInventory.addPage(createInventory(false));
        loadInventory(this.sellItems, false);
        setViewers(viewers, sellInventory);
    }

    private void loadInventory(List<MarketItemStack> items, boolean isBuy) {
        if (isBuy) {
            items.forEach(this::addBuyItem);
        } else {
            items.forEach(this::addSellItem);
        }
    }

    private Map<Player, Integer> getViewers(IPagedInventory pagedInventory) {
        Map<Player, Integer> viewers = new HashMap<>();
        for (int i = 0; i < pagedInventory.getSize() - 1; i++) {
            Inventory inventory = pagedInventory.getPage(i);
            for (HumanEntity viewer : inventory.getViewers()) {
                viewers.put((Player) viewer, i);
            }
        }

        return viewers;
    }

    private void setViewers(Map<Player, Integer> viewers, IPagedInventory pagedInventory) {
        int maxPageIndex = pagedInventory.getSize() - 1;

        viewers.forEach((player, pageIndex) -> {
            if (pageIndex > maxPageIndex) pageIndex = maxPageIndex;
            pagedInventory.open(player, pageIndex);
        });
    }

    //Method to get the index of the MarketItem in buyItems/sellItems
    private static int getIndex(int page, int slot) {
        return (page * 44) + slot + page;
    }
}
