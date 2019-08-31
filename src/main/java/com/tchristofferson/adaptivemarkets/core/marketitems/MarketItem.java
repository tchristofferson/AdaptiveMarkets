package com.tchristofferson.adaptivemarkets.core.marketitems;

import org.apache.commons.lang.Validate;

public class MarketItem {

    private double price;
    private double priceChange;
    private double minPrice;
    private double maxPrice;
    private int priceChangeCondition;

    protected MarketItem(double price, double priceChange, int priceChangeCondition, double minPrice, double maxPrice) {
        this.price = price;
        this.priceChange = priceChange;
        this.priceChangeCondition = priceChangeCondition;
        Validate.isTrue(price <= maxPrice && price >= minPrice, "Price isn't between min and max price!");
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
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
}
