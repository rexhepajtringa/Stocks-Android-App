package com.example.stonks.stonksdatabase;
import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "stocks")
public class StockEntity {

    @NonNull
    @PrimaryKey
    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "symbol")
    public String symbol;

    @ColumnInfo(name = "image_url")
    public String image;

    @ColumnInfo(name = "current_price")
    public double currentPrice;

    @ColumnInfo(name = "market_cap")
    public long marketCap;

    @ColumnInfo(name = "market_cap_rank")
    public int marketCapRank;

    @ColumnInfo(name = "fully_diluted_valuation")
    public long fullyDilutedValuation;

    @ColumnInfo(name = "total_volume")
    public long totalVolume;

    @ColumnInfo(name = "high_24h")
    public double high24h;

    @ColumnInfo(name = "low_24h")
    public double low24h;

    @ColumnInfo(name = "price_change_24h")
    public double priceChange24h;

    @ColumnInfo(name = "price_change_percentage_24h")
    public double priceChangePercentage24h;

    @ColumnInfo(name = "market_cap_change_24h")
    public long marketCapChange24h;

    @ColumnInfo(name = "market_cap_change_percentage_24h")
    public double marketCapChangePercentage24h;

    @ColumnInfo(name = "circulating_supply")
    public double circulatingSupply;

    @ColumnInfo(name = "total_supply")
    public Double totalSupply;

    @ColumnInfo(name = "max_supply")
    public Double maxSupply;

    @ColumnInfo(name = "ath")
    public double ath;

    @ColumnInfo(name = "ath_change_percentage")
    public double athChangePercentage;

    @ColumnInfo(name = "ath_date")
    public String athDate;

    @ColumnInfo(name = "atl")
    public double atl;

    @ColumnInfo(name = "atl_change_percentage")
    public double atlChangePercentage;

    @ColumnInfo(name = "atl_date")
    public String atlDate;

    @ColumnInfo(name = "last_updated")
    public String lastUpdated;

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }


    @NonNull
    public String getName() {
        return name;
    }


    @NonNull
    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public double getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(double currentPrice) {
        this.currentPrice = currentPrice;
    }

    public long getMarketCap() {
        return marketCap;
    }

    public void setMarketCap(long marketCap) {
        this.marketCap = marketCap;
    }

    public int getMarketCapRank() {
        return marketCapRank;
    }

    public void setMarketCapRank(int marketCapRank) {
        this.marketCapRank = marketCapRank;
    }

    public long getFullyDilutedValuation() {
        return fullyDilutedValuation;
    }

    public void setFullyDilutedValuation(long fullyDilutedValuation) {
        this.fullyDilutedValuation = fullyDilutedValuation;
    }

    public long getTotalVolume() {
        return totalVolume;
    }

    public void setTotalVolume(long totalVolume) {
        this.totalVolume = totalVolume;
    }

    public double getHigh24h() {
        return high24h;
    }

    public void setHigh24h(double high24h) {
        this.high24h = high24h;
    }

    public double getLow24h() {
        return low24h;
    }

    public void setLow24h(double low24h) {
        this.low24h = low24h;
    }

    public double getPriceChange24h() {
        return priceChange24h;
    }

    public void setPriceChange24h(double priceChange24h) {
        this.priceChange24h = priceChange24h;
    }

    public double getPriceChangePercentage24h() {
        return priceChangePercentage24h;
    }

    public void setPriceChangePercentage24h(double priceChangePercentage24h) {
        this.priceChangePercentage24h = priceChangePercentage24h;
    }

    public long getMarketCapChange24h() {
        return marketCapChange24h;
    }

    public void setMarketCapChange24h(long marketCapChange24h) {
        this.marketCapChange24h = marketCapChange24h;
    }

    public double getMarketCapChangePercentage24h() {
        return marketCapChangePercentage24h;
    }

    public void setMarketCapChangePercentage24h(double marketCapChangePercentage24h) {
        this.marketCapChangePercentage24h = marketCapChangePercentage24h;
    }

    public double getCirculatingSupply() {
        return circulatingSupply;
    }

    public void setCirculatingSupply(double circulatingSupply) {
        this.circulatingSupply = circulatingSupply;
    }

    public Double getTotalSupply() {
        return totalSupply;
    }

    public void setTotalSupply(Double totalSupply) {
        this.totalSupply = totalSupply;
    }

    public Double getMaxSupply() {
        return maxSupply;
    }

    public void setMaxSupply(Double maxSupply) {
        this.maxSupply = maxSupply;
    }

    public double getAth() {
        return ath;
    }

    public void setAth(double ath) {
        this.ath = ath;
    }

    public double getAthChangePercentage() {
        return athChangePercentage;
    }

    public void setAthChangePercentage(double athChangePercentage) {
        this.athChangePercentage = athChangePercentage;
    }

    public String getAthDate() {
        return athDate;
    }

    public void setAthDate(String athDate) {
        this.athDate = athDate;
    }

    public double getAtl() {
        return atl;
    }

    public void setAtl(double atl) {
        this.atl = atl;
    }

    public double getAtlChangePercentage() {
        return atlChangePercentage;
    }

    public void setAtlChangePercentage(double atlChangePercentage) {
        this.atlChangePercentage = atlChangePercentage;
    }

    public String getAtlDate() {
        return atlDate;
    }

    public void setAtlDate(String atlDate) {
        this.atlDate = atlDate;
    }

    public String getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(String lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}
