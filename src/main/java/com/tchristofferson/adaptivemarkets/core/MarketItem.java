package com.tchristofferson.adaptivemarkets.core;

import org.apache.commons.lang.Validate;
import org.bukkit.inventory.ItemStack;

public class MarketItem {

    private final ItemStack itemStack;
    private double price;
    private double priceChange;
    private double minPrice;
    private double maxPrice;
    private int priceChangeCondition;
    private int supply;

    public MarketItem(ItemStack itemStack, double price, double priceChange, int priceChangeCondition, double minPrice, double maxPrice, int supply) {
        this.itemStack = itemStack;
        this.price = price;
        this.priceChange = priceChange;
        this.priceChangeCondition = priceChangeCondition;
        Validate.isTrue(price <= maxPrice && price >= minPrice, "Price isn't between min and max price!");
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        this.supply = supply;
        this.itemStack.setAmount(1);
    }

    public ItemStack getItemStack() {
        return itemStack;
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

    public int getPriceChangeCondition() {
        return priceChangeCondition;
    }

    public void setPriceChangeCondition(int priceChangeCondition) {
        this.priceChangeCondition = priceChangeCondition;
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

    public int getSupply() {
        return supply;
    }

    public void setSupply(int supply) {
        this.supply = supply;
    }
}