package com.tchristofferson.adaptivemarkets.core;

import org.apache.commons.lang.Validate;
import org.bukkit.inventory.ItemStack;

public class MarketItem {

    private final ItemStack itemStack;
    private double price;
    private double priceChange;
    private double minPrice;
    private double maxPrice;
    //Buy inventory: How many items have to be unsold for the price to lower
    //Sell inventory: If the merchant doesn't buy this many items the price rises
    private int priceChangeCondition;
    private int supply;

    public MarketItem(ItemStack itemStack, double price, double priceChange, int priceChangeCondition, double minPrice, double maxPrice, int supply) {
        Validate.isTrue(price <= maxPrice && price >= minPrice, "Price isn't between min and max price!");
        Validate.isTrue(supply >= 0, "Supply cannot be less than zero!");
        this.itemStack = itemStack.clone();
        this.price = price;
        this.priceChange = priceChange;
        this.priceChangeCondition = priceChangeCondition;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        this.supply = supply;
        this.itemStack.setAmount(1);
    }

    public ItemStack getItemStack() {
        return itemStack.clone();
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        Validate.isTrue(price > minPrice, "Cannot set the price lower than minPrice!");
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
        Validate.isTrue(minPrice < maxPrice, "minPrice must be less than maxPrice!");
        this.minPrice = minPrice;
    }

    public double getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(double maxPrice) {
        Validate.isTrue(maxPrice > minPrice, "maxPrice must be greater than minPrice!");
        this.maxPrice = maxPrice;
    }

    public int getSupply() {
        return supply;
    }

    public void setSupply(int supply) {
        Validate.isTrue(supply >= 0, "Supply must be greater than or equal to zero!");
        this.supply = supply;
    }
}
