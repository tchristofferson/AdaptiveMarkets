package com.tchristofferson.adaptivemarkets.core;

import org.apache.commons.lang.Validate;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class MarketItemStack extends ItemStack {

    private double price;
    private double priceChange;
    private double minPrice;
    private double maxPrice;
    //Buy inventory: How many items have to be unsold for the price to lower
    //Sell inventory: If the merchant doesn't buy this many items the price rises
    private int priceChangeCondition;
    private int supply;
    private List<String> originalLore;

    public MarketItemStack(ItemStack stack, double price, double priceChange, double minPrice, double maxPrice,
                           int priceChangeCondition, int supply) throws IllegalArgumentException {
        super(stack);
        this.price = price;
        this.priceChange = priceChange;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        this.priceChangeCondition = priceChangeCondition;
        this.supply = supply;
        this.originalLore = stack.getItemMeta().getLore();
        setAmount(1);
    }

    private MarketItemStack(MarketItemStack marketItemStack) {
        this(marketItemStack, marketItemStack.price, marketItemStack.priceChange, marketItemStack.minPrice,
                marketItemStack.maxPrice, marketItemStack.priceChangeCondition, marketItemStack.supply);
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

    //Returns the original item the player is trying to buy or sell
    //Returns a clone without the lore that is added for the inventory GUIs
    public MarketItemStack getOriginalStack() {
        MarketItemStack marketItemStack = clone();
        ItemMeta itemMeta = marketItemStack.getItemMeta();
        itemMeta.setLore(this.originalLore);
        marketItemStack.setItemMeta(itemMeta);

        return marketItemStack;
    }

    @Override
    public MarketItemStack clone() {
        return new MarketItemStack(this);
    }
}
