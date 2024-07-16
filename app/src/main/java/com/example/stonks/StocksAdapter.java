package com.example.stonks;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.stonks.stonksdatabase.StockEntity;

import java.util.List;

public class StocksAdapter extends RecyclerView.Adapter<StocksAdapter.StockViewHolder> {
    private List<Stock> stockList;
    private Context context;
    private List<Stock> filteredList;

    private OnFavoriteClickedListener favoriteClickedListener;

    public interface OnFavoriteClickedListener {
        void onFavoriteClicked(Stock stock);
    }

    public StocksAdapter(List<Stock> stockList, Context context, OnFavoriteClickedListener listener) {
        this.stockList = stockList;
        this.context = context;
        this.favoriteClickedListener = listener;
    }

    public void setFilteredList(List<Stock> filteredList){
        this.stockList = filteredList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public StockViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.stock_item, parent, false);
        return new StockViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull StockViewHolder holder, int position) {
        Stock stock = stockList.get(position);

        Glide.with(context)
                .load(stock.getImage())
                .into(holder.stockImage);

        holder.stockName.setText(stock.getName());
        holder.stockPrice.setText(String.format("$%.2f", stock.getCurrentPrice()));
        String changeText = String.format("%.2f (%.2f%%)", stock.getPriceChange(), stock.getPriceChangePercentage());
        holder.stockChange.setText(changeText);
        holder.stockChange.setTextColor(stock.getPriceChange() >= 0 ? 0xFF4CAF50 : 0xFFE53935);
        holder.stockFavorite.setImageResource(stock.isFavorite() ? R.drawable.ic_star_filled : R.drawable.ic_star_outline);

        holder.stockFavorite.setImageResource(stock.isFavorite() ? R.drawable.ic_star_filled : R.drawable.ic_star_outline);

        holder.stockFavorite.setOnClickListener(v -> {
            boolean isCurrentlyFavorite = stock.isFavorite();
            stock.setFavorite(!isCurrentlyFavorite);
            notifyItemChanged(position);
        });

        holder.itemView.setOnClickListener(v -> {
            if (isNetworkAvailable()) {
                Intent intent = new Intent(context, StockDetailActivity.class);
                intent.putExtra("stockName", stock.getName());
                context.startActivity(intent);
            } else {
                Intent intent = new Intent(context, StockDetailActivity.class);
                intent.putExtra("stock", stock);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return stockList.size();
    }

    public static class StockViewHolder extends RecyclerView.ViewHolder {
        TextView stockName, stockPrice, stockChange;
        ImageView stockFavorite, stockImage;

        public StockViewHolder(View view) {
            super(view);
            stockName = view.findViewById(R.id.stockName);
            stockPrice = view.findViewById(R.id.stockPrice);
            stockChange = view.findViewById(R.id.stockChange);
            stockFavorite = view.findViewById(R.id.stockFavorite);
            stockImage = view.findViewById(R.id.stockImage);
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        }
        return false;
    }

    private StockEntity convertToStockEntity(Stock stock) {

        StockEntity stockEntity = new StockEntity();
        stockEntity.setName(stock.getName());
        stockEntity.setCurrentPrice(stock.getCurrentPrice());
        stockEntity.setPriceChangePercentage24h(stock.getPriceChangePercentage());
        stockEntity.setPriceChange24h(stock.getPriceChange());

        return stockEntity;
    }
}
