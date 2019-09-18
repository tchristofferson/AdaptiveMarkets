package com.tchristofferson.adaptivemarkets.core;

import org.bukkit.inventory.ItemStack;

public class MarketItemInfo {

    private double price;
    private double priceChange;
    private double minPrice;
    private double maxPrice;
    //Buy inventory: How many items have to be unsold for the price to lower
    //Sell inventory: If the merchant doesn't buy this many items the price rises
    private int priceChangeCondition;
    private int supply;
    private final ItemStack original;

    public MarketItemInfo(ItemStack itemStack, double price, double priceChange, double minPrice, double maxPrice, int priceChangeCondition, int supply) {
        this.price = price;
        this.priceChange = priceChange;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        this.priceChangeCondition = priceChangeCondition;
        this.supply = supply;
        this.original = itemStack;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getPriceChange() {
        return priceChange;
    }

    public void setPriceChange(double priceChange) {
        this.priceChange = priceChange;
    }

    public double getMinPrice() {
        return minPrice;
    }

    public void setMinPrice(double minPrice) {
        this.minPrice = minPrice;
    }

    public double getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(double maxPrice) {
        this.maxPrice = maxPrice;
    }

    public int getPriceChangeCondition() {
        return priceChangeCondition;
    }

    public void setPriceChangeCondition(int priceChangeCondition) {
        this.priceChangeCondition = priceChangeCondition;
    }

    public int getSupply() {
        return supply;
    }

    public void setSupply(int supply) {
        this.supply = supply;
    }

    public ItemStack getOriginal() {
        return original;
    }
}
