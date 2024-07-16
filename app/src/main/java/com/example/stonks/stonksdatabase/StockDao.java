package com.example.stonks.stonksdatabase;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface StockDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertStock(StockEntity stock);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<StockEntity> stocks);

    @Query("SELECT * FROM stocks WHERE name = :name")
    StockEntity getStockByName(String name);

    @Query("SELECT * FROM stocks")
    List<StockEntity> getAllStocks();

    @Update
    void updateStock(StockEntity stock);

    @Delete
    void deleteStock(StockEntity stock);

    @Query("DELETE FROM stocks")
    void deleteAllStock();

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void addToWatchlist(WatchlistEntity watchlistItem);

    @Delete
    void removeFromWatchlist(WatchlistEntity watchlistItem);

    @Query("SELECT * FROM stocks INNER JOIN watchlist ON stocks.name = watchlist.stockName")
    List<StockEntity> getWatchlistedStocks();

}
