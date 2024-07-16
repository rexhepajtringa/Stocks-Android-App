package com.example.stonks;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.example.stonks.databinding.ActivityStockDetailBinding;
import com.example.stonks.stonksdatabase.StockEntity;
import com.example.stonks.stonksdatabase.StocksDatabase;
import com.example.stonks.stonksdatabase.WatchlistEntity;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StockDetailActivity extends AppCompatActivity {
    TextView tvName, tvCurrentPrice, tvPriceChange, tvOpenPrice, tvVolume, tvHigh24h, tvLow24h, tvDescription;
    LineChart chart;

    private ActivityStockDetailBinding binding;
    private StocksDatabase database;
    private StockEntity currentStock;

    private String apiResponse, name;

    private long volume, ath, atl, market_cap, market_cap_rank, total_supply, max_supply, circulating_supply, fully_diluted_valuation;

    private double priceUSD, high24h, low24h, priceChange, priceChange24hPercentage, priceChange7dPercentage, priceChange30dPercentage, priceChange1yPercentage;

    private String last_updated;

    JSONObject marketData;

    private ImageView stockImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityStockDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        chart = findViewById(R.id.chart);

        database = Room.databaseBuilder(getApplicationContext(), StocksDatabase.class, "stocks-database").allowMainThreadQueries().build();


        stockImage = findViewById(R.id.stockImage);
        tvName = findViewById(R.id.tvName);
        tvCurrentPrice = findViewById(R.id.tvCurrentPrice);
        tvOpenPrice = findViewById(R.id.tvOpenPrice);
        tvVolume = findViewById(R.id.tvVolume);
        tvHigh24h = findViewById(R.id.tvHigh24h);
        tvLow24h = findViewById(R.id.tvLow24h);
        tvPriceChange = findViewById(R.id.tvPriceChange);
//        tvDescription = findViewById(R.id.tvDescription);
        chart = findViewById(R.id.chart);

        ImageButton refreshStock = findViewById(R.id.refresh);

        refreshStock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchStockDetails(name);
            }
        });

        Stock stock = (Stock) getIntent().getSerializableExtra("stock");
        if (stock != null) {
            Toast.makeText(this, "No internet - Offline Mode", Toast.LENGTH_SHORT).show();
            displayStockInfo(stock);
        } else {
            String stockName = getIntent().getStringExtra("stockName");
            if (stockName != null) {
                fetchStockDetails(stockName);
            }
        }


        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        Button firstButton = findViewById(R.id.btn1);
        firstButton.setSelected(true);
//        firstButton.setBackgroundResource(R.drawable.btn_tab_selected);

        Button addToWatchlistButton = findViewById(R.id.addToWatchlistButton);
        addToWatchlistButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addToWatchlist(currentStock);
                navigateToWatchlistPage();
            }
        });
    }

    private void checkInDb() {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                StockEntity existingStock = database.stockDao().getStockByName(currentStock.getName());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (existingStock != null){
                            Button addToWatchlistButton = findViewById(R.id.addToWatchlistButton);
                            addToWatchlistButton.setText("Remove From Watchlist");
                        }
                    }
                });
            }
        });
    }

    private void displayStockInfo(Stock stock) {
        String name = stock.getName();
        tvName.setText(name != null ? name : "N/A");

        priceUSD = stock.getCurrentPrice();
        tvCurrentPrice.setText(priceUSD != 0 ? String.format("$%,.2f", priceUSD) : "N/A");

        priceChange = stock.getPriceChange();
        tvPriceChange.setText(priceChange != 0 ? String.format("$%,.2f", priceChange) : "N/A");

        if (stock.getImage() != null && !stock.getImage().isEmpty()) {
            Glide.with(StockDetailActivity.this)
                    .load(stock.getImage())
                    .into(stockImage);
        }
    }

    private void fetchStockDetails(String stockName) {
        String formattedStockName = stockName.toLowerCase().replace(" ", "");
        String url = "https://app-vpigadas.herokuapp.com/api/stocks/" + formattedStockName;
        StringRequest request = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            apiResponse = response;
                            JSONObject jsonObject = new JSONObject(response);

                            String imageUrl = jsonObject.getJSONObject("image").getString("thumb");
                            name = jsonObject.getString("name");
                            String symbol = jsonObject.getString("symbol");
                            marketData = jsonObject.getJSONObject("market_data");
                            JSONObject currentPrice = marketData.getJSONObject("current_price");
                            priceUSD = currentPrice.optDouble("usd", -1);
                            priceChange = marketData.optDouble("price_change_24h", -1);
                            high24h = marketData.getJSONObject("high_24h").optDouble("usd", -1);
                            low24h = marketData.getJSONObject("low_24h").optDouble("usd", -1);
                            volume = marketData.getJSONObject("total_volume").optLong("usd", -1);
                            String description = jsonObject.getJSONObject("description").getString("en");
                            ath = marketData.getJSONObject("ath").optLong("usd", -1);
                            atl = marketData.getJSONObject("atl").optLong("usd", -1);
                            market_cap = marketData.getJSONObject("market_cap").optLong("usd", -1);
                            market_cap_rank = marketData.optLong("market_cap_rank", -1);
                            total_supply = marketData.optLong("total_supply", -1);
                            max_supply = marketData.optLong("max_supply", -1);
                            circulating_supply = marketData.optLong("circulating_supply", -1);
                            last_updated = marketData.getString("last_updated");
                            fully_diluted_valuation = marketData.getJSONObject("fully_diluted_valuation").getLong("usd");


                            Glide.with(StockDetailActivity.this)
                                    .load(imageUrl)
                                    .into(stockImage);

                            tvName.setText(name != null ? name : "N/A");
                            tvCurrentPrice.setText(priceUSD != 0 ? String.format("$%,.2f", priceUSD) : "N/A");
                            tvPriceChange.setText(priceChange != 0 ? String.format("$%,.2f", priceChange) : "N/A");
                            tvOpenPrice.setText(formatVolume(fully_diluted_valuation));
                            tvHigh24h.setText(high24h != 0 ? String.format("$%,.2f", high24h) : "N/A");
                            tvLow24h.setText(low24h != 0 ? String.format("$%,.2f", low24h) : "N/A");
                            tvVolume.setText(formatVolume(volume));
//                            tvDescription.setText(description);

                            currentStock = new StockEntity();
                            currentStock.setName(name);
                            currentStock.setCurrentPrice(priceUSD);
                            currentStock.setSymbol(symbol);
                            currentStock.setHigh24h(high24h);
                            currentStock.setLow24h(low24h);
                            currentStock.setTotalVolume(volume);

                            checkInDb();
                            parseChartData("");

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Host", "app-vpigadas.herokuapp.com");
                return headers;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }


    public String formatVolume(long volume) {
        if (volume == 0) {
            return "N/A";
        } else if (volume < 1_000_000_000) {
            return new DecimalFormat("#,###").format(volume);
        } else {
            return new DecimalFormat("#.#B").format(volume / 1_000_000_000) + "";
        }
    }
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void addToWatchlist(StockEntity stock) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                StockEntity existingStock = database.stockDao().getStockByName(stock.getName());
                if (existingStock == null) {
                    database.stockDao().insertStock(stock);
                    WatchlistEntity watchlistEntity = new WatchlistEntity();
                    watchlistEntity.stockName = stock.getName();
                    try {
                        database.stockDao().addToWatchlist(watchlistEntity);
                    } catch (Exception e) {
                        Log.e("StockDetailActivity", "Error adding to watchlist", e);
                    }
                } else {
                    WatchlistEntity watchlistEntity = new WatchlistEntity();
                    watchlistEntity.stockName = stock.getName();
                    database.stockDao().removeFromWatchlist(watchlistEntity);
                }
            }
        });

    }

    private void navigateToWatchlistPage() {
        Intent intent = new Intent(this, WatchlistActivity.class);
        startActivity(intent);
    }

    private void parseChartData(String timeframe) {
        List<Entry> entries = generateChartData(timeframe);
        updateChart(entries);
    }

    private List<Entry> generateChartData(String timeframe) {
        List<Entry> entries = new ArrayList<>();
        try {

            priceChange24hPercentage = marketData.getDouble("price_change_percentage_24h");
            priceChange7dPercentage = marketData.getDouble("price_change_percentage_7d");
            priceChange30dPercentage = marketData.getDouble("price_change_percentage_30d");
            priceChange1yPercentage = marketData.getDouble("price_change_percentage_1y");

            double price24hAgo = calculateHistoricalPrice(priceUSD, priceChange24hPercentage);
            double price7dAgo = calculateHistoricalPrice(priceUSD, priceChange7dPercentage);
            double price30dAgo = calculateHistoricalPrice(priceUSD, priceChange30dPercentage);
            double price1yAgo = calculateHistoricalPrice(priceUSD, priceChange1yPercentage);

            Log.d("curr", priceUSD+"");
            Log.d("curr", priceChange24hPercentage+"");
            Log.d("curr", priceChange7dPercentage+"");
            Log.d("curr", priceChange30dPercentage+"");
            Log.d("curr", priceChange1yPercentage+"");


            switch (timeframe) {
                case "ALL":
                    entries.add(new Entry(0, (float) price1yAgo));
                    entries.add(new Entry(1, (float) price30dAgo));
                    entries.add(new Entry(2, (float) price7dAgo));
                    entries.add(new Entry(3, (float) price24hAgo));
                    entries.add(new Entry(4, (float) priceUSD));
                    break;
                case "1W":
                    entries.add(new Entry(0, (float) price30dAgo));
                    entries.add(new Entry(1, (float) price7dAgo));
                    entries.add(new Entry(2, (float) price24hAgo));
                    entries.add(new Entry(3, (float) priceUSD));
                    break;
                case "1M":
                    entries.add(new Entry(0, (float) price1yAgo));
                    entries.add(new Entry(1, (float) price30dAgo));
                    entries.add(new Entry(2, (float) price7dAgo));
                    entries.add(new Entry(3, (float) price24hAgo));
                    entries.add(new Entry(4, (float) priceUSD));
                    break;
                case "1Y":
                    entries.add(new Entry(0, (float) price1yAgo));
                    entries.add(new Entry(1, (float) price30dAgo));
                    entries.add(new Entry(2, (float) price7dAgo));
                    break;
                default:
                    entries.add(new Entry(0, (float) price7dAgo));
                    entries.add(new Entry(1, (float) price24hAgo));
                    entries.add(new Entry(2, (float) priceUSD));
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return entries;
    }

    private double calculateHistoricalPrice(double currentPrice, double changePercentage) {
        return currentPrice / (1 + changePercentage / 100);
    }




    private void updateChart(List<Entry> entries) {
        chart = findViewById(R.id.chart);

        chart.setBackgroundColor(Color.WHITE);
        chart.getDescription().setEnabled(false);
        chart.setPinchZoom(true);

        chart.setTouchEnabled(true);
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        chart.setDrawGridBackground(false);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f); 
        xAxis.setGranularityEnabled(true);
        xAxis.setDrawLabels(false);

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setDrawGridLines(false);
        chart.getAxisRight().setEnabled(false);

        LineDataSet set = new LineDataSet(entries, "Market Data");
        set.setCircleColor(Color.BLUE);
        set.setCircleRadius(5f);
        set.setValueTextSize(10f);
        set.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format("%.2f", value);
            }
        });

        LineData data = new LineData(set);
        chart.setData(data);
        chart.invalidate();
    }


    public void onTimeFrameSelected(View view) {
        String timeframe = ((Button) view).getText().toString();
        parseChartData(timeframe);
    }

    public void onTabSelected(View view) {
        Button btn1 = findViewById(R.id.btn1);
        Button btn2 = findViewById(R.id.btn2);
        Button btn3 = findViewById(R.id.btn3);

        btn1.setSelected(false);
        btn2.setSelected(false);
        btn3.setSelected(false);

        view.setSelected(true);

        int id = view.getId();

        if (id == R.id.btn1) {
            ((TextView) findViewById(R.id.tvOpenPrice)).setText(formatVolume(fully_diluted_valuation));
            ((TextView) findViewById(R.id.tvVolume)).setText(formatVolume(volume));
            ((TextView) findViewById(R.id.tvLow24h)).setText(low24h+"");
            ((TextView) findViewById(R.id.tvHigh24h)).setText(high24h+"");
            ((TextView) findViewById(R.id.tvOpenPriceLabel)).setText("Fully Diluted Valuation");
            ((TextView) findViewById(R.id.tvVolumeLabel)).setText(getString(R.string.volume));
            ((TextView) findViewById(R.id.tvLow24hLabel)).setText(getString(R.string.low_24));
            ((TextView) findViewById(R.id.tvHigh24hLabel)).setText(getString(R.string.high_24));
        } else if (id == R.id.btn2) {
            ((TextView) findViewById(R.id.tvOpenPrice)).setText(ath + "");
            ((TextView) findViewById(R.id.tvVolume)).setText(atl + "");
            ((TextView) findViewById(R.id.tvLow24h)).setText(formatVolume(market_cap));
            ((TextView) findViewById(R.id.tvHigh24h)).setText(market_cap_rank + "");
            ((TextView) findViewById(R.id.tvOpenPriceLabel)).setText("All Time High");
            ((TextView) findViewById(R.id.tvVolumeLabel)).setText("All Time Low");
            ((TextView) findViewById(R.id.tvLow24hLabel)).setText("Market Cap");
            ((TextView) findViewById(R.id.tvHigh24hLabel)).setText("Market Cap Rank");
        }else if (id == R.id.btn3) {
            ((TextView) findViewById(R.id.tvOpenPrice)).setText(formatVolume(total_supply));
            ((TextView) findViewById(R.id.tvVolume)).setText(formatVolume(max_supply));
            ((TextView) findViewById(R.id.tvLow24h)).setText(formatVolume(circulating_supply));
            ((TextView) findViewById(R.id.tvHigh24h)).setText(last_updated);
            ((TextView) findViewById(R.id.tvOpenPriceLabel)).setText("Total Supply");
            ((TextView) findViewById(R.id.tvVolumeLabel)).setText("Max Supply");
            ((TextView) findViewById(R.id.tvLow24hLabel)).setText("Circulating Supply");
            ((TextView) findViewById(R.id.tvHigh24hLabel)).setText("Last Updated");
        }

    }



}
