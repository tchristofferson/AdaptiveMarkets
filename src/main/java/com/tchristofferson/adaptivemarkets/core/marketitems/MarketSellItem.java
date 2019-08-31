package com.tchristofferson.adaptivemarkets.core.marketitems;

public class MarketSellItem extends MarketItem {

    private double money;

    protected MarketSellItem(double price, double priceChange, int priceChangeCondition, double minPrice, double maxPrice, double money) {
        super(price, priceChange, priceChangeCondition, minPrice, maxPrice);
        this.money = money;
    }

    public double getMoney() {
        return money;
    }

    public void setMoney(double money) {
        this.money = money;
    }
}
