package com.example.stonks;

import java.io.Serializable;

public class Stock implements Serializable {

    private String name;
    public String image;
    private double currentPrice;
    private double priceChange;
    private double priceChangePercentage;
    private boolean isFavorite; // Added favorite flag

    public Stock(String name, String image, double currentPrice, double priceChange, double priceChangePercentage, boolean isFavorite) {
        this.name = name;
        this.image = image;
        this.currentPrice = currentPrice;
        this.priceChange = priceChange;
        this.priceChangePercentage = priceChangePercentage;
        this.isFavorite = isFavorite;
    }

    public String getName() {
        return name;
    }

    public String getImage() {
        return image;
    }

    public double getCurrentPrice() {
        return currentPrice;
    }

    public double getPriceChange() {
        return priceChange;
    }

    public double getPriceChangePercentage() {
        return priceChangePercentage;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }
}
