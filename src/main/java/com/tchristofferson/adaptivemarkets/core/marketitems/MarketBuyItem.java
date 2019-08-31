package com.tchristofferson.adaptivemarkets.core.marketitems;

public class MarketBuyItem extends MarketItem {

    private int supply;

    protected MarketBuyItem(double price, double priceChange, int priceChangeCondition, int supply, double minPrice, double maxPrice) {
        super(price, priceChange, priceChangeCondition, minPrice, maxPrice);
        this.supply = supply;
    }

    public int getSupply() {
        return supply;
    }

    public void setSupply(int supply) {
        this.supply = supply;
    }
}
