package com.tchristofferson.adaptivemarkets.core.marketitems;

import org.bukkit.Material;

public final class MarketSellItemMaterial extends MarketSellItem {

    private final Material material;

    public MarketSellItemMaterial(double price, double priceChange, int priceChangeCondition, double minPrice, double maxPrice, double money, Material material) {
        super(price, priceChange, priceChangeCondition, minPrice, maxPrice, money);
        this.material = material;
    }

    public Material getMaterial() {
        return material;
    }
}
