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
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;

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
                if (handler.getEvent().getClick() == ClickType.DOUBLE_CLICK) return;
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

                    String inventoryName = ChatColor.stripColor(event.getView().getTitle());
                    Merchant merchant = playerManager.getCurrentMerchant(player);

                    if (inventoryName.endsWith(" - Buy")) {
                        MarketItemInfo marketItemInfo = merchant.buyItems.get(clickedItem);
                        if (marketItemInfo.getSupply() == 0) {
                            player.sendMessage(ChatColor.RED + "That item is out of stock!");
                            return;
                        }

                        if (adaptiveMarkets.getEconomy().getBalance(player) >= marketItemInfo.getPrice()) {
                            Bukkit.getScheduler().runTask(adaptiveMarkets, () -> {
                                Map<Integer, ItemStack> notAdded = player.getInventory().addItem(marketItemInfo.getOriginal());
                                if (notAdded.isEmpty()) {
                                    marketItemInfo.setSupply(marketItemInfo.getSupply() - 1);
                                    adaptiveMarkets.getEconomy().withdrawPlayer(player, marketItemInfo.getPrice());
                                    player.sendMessage(ChatColor.GREEN + "Successfully bought item for $" + marketItemInfo.getPrice());
                                } else {
                                    player.sendMessage(ChatColor.RED + "You don't have enough inventory space!");
                                }
                            });
                        } else {
                            player.sendMessage(ChatColor.RED + "Insufficient funds!");
                        }
                    } else if (inventoryName.endsWith(" - Sell") && !clickedInventory.equals(player.getInventory())) {//FIXME: Won't sell
                        //TODO: Check supply
                        Inventory playerInventory = player.getInventory();
                        MarketItemInfo marketItemInfo = merchant.sellItems.get(clickedItem);

                        for (int i = 0; i < playerInventory.getSize(); i++) {
                            ItemStack is = playerInventory.getItem(i);

                            if (marketItemInfo.getOriginal().isSimilar(is)) {
                                if (is.getAmount() >= clickedItem.getAmount()) {
                                    adaptiveMarkets.getEconomy().depositPlayer(player, marketItemInfo.getPrice());
                                    is.setAmount(is.getAmount() - clickedItem.getAmount());
                                    playerInventory.setItem(i, is);
                                    player.sendMessage(ChatColor.GREEN + "Successfully sold item(s) for $" + marketItemInfo.getPrice());
                                } else {
                                    player.sendMessage(ChatColor.RED + "You don't have enough of that item to sell!");
                                }

                                return;
                            }
                        }

                        player.sendMessage(ChatColor.RED + "You can't sell what you don't have!");
                    }

                    return;
                }

                Bukkit.getScheduler().runTask(adaptiveMarkets, runnable);
            }
        };
    }

    private final PagedInventoryAPI pagedInventoryAPI;
    private final Map<ItemStack, MarketItemInfo> buyItems;
    private final Map<ItemStack, MarketItemInfo> sellItems;
    private IPagedInventory buyInventory;
    private IPagedInventory sellInventory;
    private String merchantType;
    private String displayName;
    private Villager.Profession profession;
    private Villager.Type type;

    public Merchant(PagedInventoryAPI pagedInventoryAPI, String merchantType, String displayName, Map<ItemStack, MarketItemInfo> buyItems, Map<ItemStack, MarketItemInfo> sellItems, Villager.Profession profession, Villager.Type type) {
        this.pagedInventoryAPI = pagedInventoryAPI;
        this.buyInventory = pagedInventoryAPI.createPagedInventory(NEXT_ITEMSTACK, PREV_ITEMSTACK, CLOSE_ITEMSTACK);
        this.sellInventory = pagedInventoryAPI.createPagedInventory(NEXT_ITEMSTACK, PREV_ITEMSTACK, CLOSE_ITEMSTACK);
        this.merchantType = merchantType.trim().toUpperCase();
        this.displayName = ChatColor.translateAlternateColorCodes('&', displayName);
        this.profession = profession;
        this.type = type;
        this.buyInventory.addPage(createInventory(true));
        this.sellInventory.addPage(createInventory(false));
        this.buyItems = new HashMap<>(buyItems);
        this.sellItems = new HashMap<>(sellItems);

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

    public boolean containsBuyItem(ItemStack itemStack) {
        return containsItem(itemStack, true);
    }

    public boolean containsSellItem(ItemStack itemStack) {
        return containsItem(itemStack, false);
    }

    private boolean containsItem(ItemStack itemStack, boolean isBuy) {
        IPagedInventory iPagedInventory = isBuy ? buyInventory : sellInventory;

        for (Inventory inventory : iPagedInventory) {
            for (ItemStack stack : inventory) {
                MarketItemInfo marketItemInfo = isBuy ? buyItems.get(stack) : sellItems.get(stack);
                if (marketItemInfo.getOriginal().isSimilar(itemStack))
                    return true;
            }
        }

        return false;
    }

    public void addBuyItem(ItemStack marketItemStack, MarketItemInfo marketItemInfo, boolean addLore) {
        if (addLore)
            setMeta(marketItemStack, marketItemInfo, true);
        buyItems.put(marketItemStack, marketItemInfo);
        addItem(marketItemStack, true);
    }

    public void addSellItem(ItemStack marketItemStack, MarketItemInfo marketItemInfo, boolean addLore) {
        if (addLore)
            setMeta(marketItemStack, marketItemInfo, false);
        sellItems.put(marketItemStack, marketItemInfo);
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
            buyItems.keySet().removeIf(is -> is.equals(itemStack));
            reorderBuyInventory();
        } else {
            sellItems.keySet().removeIf(is -> is.equals(itemStack));
            reorderSellInventory();
        }
    }

    private void addItem(ItemStack marketItemStack, boolean isBuyItem) {
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

        inventory.addItem(marketItemStack);
    }

    private void setMeta(ItemStack itemStack, MarketItemInfo marketItemInfo, boolean isBuyItem) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        List<String> lore = itemMeta.hasLore() ? itemMeta.getLore() : new ArrayList<>(2);
        lore.add(ChatColor.GREEN + "$" + marketItemInfo.getPrice());

        if (isBuyItem) {
            lore.add(ChatColor.GREEN + "" + ChatColor.BOLD + "CLICK TO BUY");
        } else {
            lore.add(ChatColor.GREEN + "" + ChatColor.BOLD + "CLICK TO SELL!");
        }

        itemMeta.setLore(lore);
        itemStack.setItemMeta(itemMeta);
    }

    private Inventory createInventory(boolean isBuy) {
        return Bukkit.createInventory(null, 54, isBuy ? displayName + " - Buy" : displayName + " - Sell");
    }

    private void reorderBuyInventory() {
        Map<Player, Integer> viewers = getViewers(buyInventory);
        pagedInventoryAPI.getRegistrar().unregisterHandlers(buyInventory);
        buyInventory = pagedInventoryAPI.createPagedInventory(NEXT_ITEMSTACK, PREV_ITEMSTACK, CLOSE_ITEMSTACK);
        buyInventory.addPage(createInventory(true));
        loadInventory(buyItems, true);
        pagedInventoryAPI.getRegistrar().addClickHandler(buyInventory, clickHandler);
        setViewers(viewers, buyInventory);
    }

    private void reorderSellInventory() {
        Map<Player, Integer> viewers = getViewers(sellInventory);
        pagedInventoryAPI.getRegistrar().unregisterHandlers(sellInventory);
        sellInventory = pagedInventoryAPI.createPagedInventory(NEXT_ITEMSTACK, PREV_ITEMSTACK, CLOSE_ITEMSTACK);
        sellInventory.addPage(createInventory(false));
        loadInventory(this.sellItems, false);
        setViewers(viewers, sellInventory);
    }

    private void loadInventory(Map<ItemStack, MarketItemInfo> items, boolean isBuy) {
        if (isBuy) {
            items.forEach((itemStack, marketItemInfo) -> addBuyItem(itemStack, marketItemInfo, false));
        } else {
            items.forEach((itemStack, marketItemInfo) -> addSellItem(itemStack, marketItemInfo, false));
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
