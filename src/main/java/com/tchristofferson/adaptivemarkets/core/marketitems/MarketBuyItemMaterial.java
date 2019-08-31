package com.tchristofferson.adaptivemarkets.core.marketitems;

import org.bukkit.Material;

public class MarketBuyItemMaterial extends MarketBuyItem {

    private final Material material;

    public MarketBuyItemMaterial(double price, double priceChange, int priceChangeCondition, int supply, double minPrice, double maxPrice, Material material) {
        super(price, priceChange, priceChangeCondition, supply, minPrice, maxPrice);
        this.material = material;
    }

    public Material getMaterial() {
        return material;
    }
}
