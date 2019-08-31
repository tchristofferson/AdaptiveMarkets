package com.tchristofferson.adaptivemarkets.core.marketitems;

import org.bukkit.inventory.ItemStack;

public class MarketBuyItemMeta extends MarketBuyItem {

    private final ItemStack itemStack;

    public MarketBuyItemMeta(double price, double priceChange, int priceChangeCondition, int supply, double minPrice, double maxPrice, ItemStack itemStack) {
        super(price, priceChange, priceChangeCondition, supply, minPrice, maxPrice);
        this.itemStack = itemStack.clone();
        this.itemStack.setAmount(1);
    }

    public ItemStack getItemStack() {
        return itemStack;
    }
}
