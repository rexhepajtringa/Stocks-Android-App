package com.example.stonks;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.stonks.databinding.ActivityMainBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements NetworkHelper.NetworkStatusListener, StocksAdapter.OnFavoriteClickedListener {

    private ActivityMainBinding binding;

    private RecyclerView recyclerView;
    private StocksAdapter adapter;
    private ArrayList<Stock> stockList;

    private SearchView searchView;

    private NetworkHelper networkHelper;
    private TextView statusTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        statusTextView = findViewById(R.id.statusTextView);
        networkHelper = new NetworkHelper(this);
        networkHelper.setNetworkStatusListener(this);
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
                fetchStocks();
            }
        });


        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        stockList = new ArrayList<>();
        adapter = new StocksAdapter(stockList, this, this);
        recyclerView.setAdapter(adapter);

        fetchStocks();

        Button goToWatchlistButton = findViewById(R.id.goToWatchlistButton);
        goToWatchlistButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToWatchlistPage();
            }
        });

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.nav_home);

        bottomNav.setOnNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                return true;
            } else if (id == R.id.nav_watchlist) {
                startActivity(new Intent(getApplicationContext(), WatchlistActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });

    }

    private void filterStock(String newText) {
        List<Stock> filteredList = new ArrayList<>();
        for (Stock stock : stockList) {
            if (stock.getName().toLowerCase().contains(newText.toLowerCase())) {
                filteredList.add(stock);
            }
        }
        if (filteredList.isEmpty()) {
            Toast.makeText(this, "No data found", Toast.LENGTH_SHORT).show();
        } else {
            adapter.setFilteredList(filteredList);
        }
    }

    private void fetchStocks() {
        if (networkHelper.isNetworkAvailable()) {
            String url = "https://app-vpigadas.herokuapp.com/api/stocks/";
            StringRequest request = new StringRequest(Request.Method.GET, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                saveDataLocally(response);
                                parseStockData(response);
                            } catch (Exception e) {
                                Toast.makeText(MainActivity.this, "Error parsing JSON", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    if (error.networkResponse != null && error.networkResponse.statusCode == 400) {
                        loadDataFromCache();
                    } else {
                        Toast.makeText(MainActivity.this, "Error fetching data", Toast.LENGTH_SHORT).show();
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
        } else {
            loadDataFromCache();
        }
    }


    private void navigateToWatchlistPage() {
        Intent intent = new Intent(this, WatchlistActivity.class);
        startActivity(intent);
    }

    private void saveDataLocally(String response) {
        SharedPreferences sharedPreferences = getSharedPreferences("app_cache", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("stocks_data", response);
        editor.apply();
    }

    private void loadDataFromCache() {
        SharedPreferences sharedPreferences = getSharedPreferences("app_cache", MODE_PRIVATE);
        String response = sharedPreferences.getString("stocks_data", null);
        if (response != null) {
            try {
                parseStockData(response);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        } else {
            Toast.makeText(this, "No cached data available", Toast.LENGTH_LONG).show();
        }
    }

    private void parseStockData(String response) throws JSONException {
        JSONArray jsonArray = new JSONArray(response);
        stockList.clear();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            Stock stock = new Stock(
                    jsonObject.getString("name"),
                    jsonObject.getString("image"),
                    jsonObject.getDouble("current_price"),
                    jsonObject.getDouble("price_change_24h"),
                    jsonObject.getDouble("price_change_percentage_24h"),
                    false
            );
            stockList.add(stock);
        }
        adapter.notifyDataSetChanged();
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
            fetchStocks();
        } else {
            statusTextView.setText("No Connection");
            fetchStocks();
        }
    }

    @Override
    public void onFavoriteClicked(Stock stock) {

    }
}

