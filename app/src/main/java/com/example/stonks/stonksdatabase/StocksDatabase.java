package com.example.stonks.stonksdatabase;
import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {StockEntity.class, WatchlistEntity.class}, version = 2, exportSchema = false)
public abstract class StocksDatabase extends RoomDatabase {
    public abstract StockDao stockDao();
}
