package com.example.stonks.stonksdatabase;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity(tableName = "watchlist",
        foreignKeys = @ForeignKey(entity = StockEntity.class,
                parentColumns = "name",
                childColumns = "stockName",
                onDelete = ForeignKey.CASCADE))
public class WatchlistEntity {
    @PrimaryKey
    @NonNull
    public String stockName;
}
