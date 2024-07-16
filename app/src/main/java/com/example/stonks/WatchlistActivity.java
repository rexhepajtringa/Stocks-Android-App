package com.example.stonks;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.stonks.stonksdatabase.StockEntity;
import com.example.stonks.stonksdatabase.StocksDatabase;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WatchlistActivity extends AppCompatActivity implements NetworkHelper.NetworkStatusListener, StocksAdapter.OnFavoriteClickedListener{
    private StocksDatabase database;
    private List<StockEntity> watchlistStocks;
    private RecyclerView recyclerView;
    private StocksAdapter adapter;

    private List<Stock> stocks;
    private SearchView searchView;

    private NetworkHelper networkHelper;
    private TextView statusTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watchlist);
        statusTextView = findViewById(R.id.statusTextView);
        networkHelper = new NetworkHelper(this);
        networkHelper.setNetworkStatusListener(this);

        database = Room.databaseBuilder(getApplicationContext(), StocksDatabase.class, "stocks-database").allowMainThreadQueries().build();

        searchView = findViewById(R.id.searchView);
        searchView.clearFocus();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterStock(newText);
                return false;
            }
        });

        ImageButton refreshStock = findViewById(R.id.refresh);

        refreshStock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadWatchlist();
            }
        });


        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        stocks = new ArrayList<>();
        adapter = new StocksAdapter(stocks, this, this);
        recyclerView.setAdapter(adapter);

        loadWatchlist();

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.nav_watchlist);

        bottomNav.setOnNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_watchlist) {
                return true;
            }
            return false;
        });

    }

    private void filterStock(String newText) {
        List<Stock> filteredList = new ArrayList<>();
        for (Stock stock : stocks) {
            if(stock.getName().toLowerCase().contains(newText.toLowerCase())){
                filteredList.add(stock);
            }
        }
        adapter.setFilteredList(filteredList);
        if(filteredList.isEmpty()){
            Toast.makeText(this, "No data found", Toast.LENGTH_SHORT).show();
        }
    }


    private List<Stock> convertToStockList(List<StockEntity> stockEntities) {
        List<Stock> stocks = new ArrayList<>();
        for (StockEntity entity : stockEntities) {
            Stock stock = new Stock(
                    entity.getName(),
                    entity.getImage(),
                    entity.getCurrentPrice(),
                    entity.getPriceChange24h(),
                    entity.getPriceChangePercentage24h(),
                    false
            );
            stocks.add(stock);
        }
        return stocks;
    }


    private void loadWatchlist() {
        if (networkHelper.isNetworkAvailable()) {
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            fetchStocksForWatchlist();
                        }
                    });
                }
            });
        } else {
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    List<StockEntity> stockEntities = database.stockDao().getWatchlistedStocks();
                    stocks = convertToStockList(stockEntities);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            updateUI(stocks);
                        }
                    });
                }
            });
        }
    }

    private void fetchStocksForWatchlist() {
        String url = "https://app-vpigadas.herokuapp.com/api/stocks/";
        StringRequest request = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            filterStocksForWatchlist(response);
                        } catch (Exception e) {
                            Toast.makeText(WatchlistActivity.this, "Error parsing JSON", Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse != null && error.networkResponse.statusCode == 400) {
                    AsyncTask.execute(new Runnable() {
                        @Override
                        public void run() {
                            List<StockEntity> stockEntities = database.stockDao().getWatchlistedStocks();
                            stocks = convertToStockList(stockEntities);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    updateUI(stocks);
                                }
                            });
                        }
                    });
                } else {
                    Toast.makeText(WatchlistActivity.this, "Error fetching data", Toast.LENGTH_SHORT).show();
                }
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

    private void filterStocksForWatchlist(String response) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                List<StockEntity> watchlistEntities = database.stockDao().getWatchlistedStocks();
                try {
                    JSONArray jsonArray = new JSONArray(response);
                    final List<Stock> filteredStocks = new ArrayList<>();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        for (StockEntity entity : watchlistEntities) {
                            if (entity.getName().equalsIgnoreCase(jsonObject.getString("name"))) {
                                Stock stock = new Stock(
                                        jsonObject.getString("name"),
                                        jsonObject.getString("image"),
                                        jsonObject.getDouble("current_price"),
                                        jsonObject.getDouble("price_change_24h"),
                                        jsonObject.getDouble("price_change_percentage_24h"),
                                        true
                                );
                                filteredStocks.add(stock);
                                break;
                            }
                        }
                    }
                    runOnUiThread(() -> updateUI(filteredStocks));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void updateUI(List<Stock> stocks) {
        adapter = new StocksAdapter(stocks, this, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        networkHelper.startNetworkCallback();
    }

    @Override
    protected void onPause() {
        super.onPause();
        networkHelper.stopNetworkCallback();
    }

    @Override
    public void onNetworkChanged(boolean isConnected) {
        if (isConnected) {
            statusTextView.setText("Connected");
            loadWatchlist();
        } else {
            statusTextView.setText("No Connection");
            loadWatchlist();
        }
    }

    @Override
    public void onFavoriteClicked(Stock stock) {
        AsyncTask.execute(() -> {
            StockEntity entity = database.stockDao().getStockByName(stock.getName());
            if (entity != null) {
                database.stockDao().deleteStock(entity);
                runOnUiThread(() -> {
                    loadWatchlist();
                    Toast.makeText(WatchlistActivity.this, "Removed from watchlist", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
}
