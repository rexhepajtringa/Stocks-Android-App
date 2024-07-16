package com.example.stonks;

public interface StockDetailCallback {
    void onStockDetailFetched(Stock stock);
    void onError();
}
