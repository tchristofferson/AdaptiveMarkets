package com.tchristofferson.adaptivemarkets.core.marketitems;

import org.bukkit.inventory.ItemStack;

public final class MarketSellItemMeta extends MarketSellItem {

    private final ItemStack itemStack;

    public MarketSellItemMeta(double price, double priceChange, int priceChangeCondition, double minPrice, double maxPrice, double money, ItemStack itemStack) {
        super(price, priceChange, priceChangeCondition, minPrice, maxPrice, money);
        this.itemStack = itemStack.clone();
        this.itemStack.setAmount(1);
    }

    public ItemStack getItemStack() {
        return itemStack;
    }
}
