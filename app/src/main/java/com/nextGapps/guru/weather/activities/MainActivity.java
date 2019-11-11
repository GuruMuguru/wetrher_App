package com.nextGapps.guru.weather.activities;

import android.Manifest;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.telephony.CellLocation;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;


import com.nextGapps.guru.weather.API.API;
import com.nextGapps.guru.weather.API.APIServices.WeatherServices;
import com.nextGapps.guru.weather.R;
import com.nextGapps.guru.weather.adapters.CityWeatherAdapter;
import com.nextGapps.guru.weather.database.DataBaseAdapter;
import com.nextGapps.guru.weather.models.CityWeather;
import com.nextGapps.guru.weather.interfaces.onSwipeListener;
import com.nextGapps.guru.weather.services.LocationTrack;
import com.nextGapps.guru.weather.services.SyncManager;
import com.nextGapps.guru.weather.services.SyncService;
import com.nextGapps.guru.weather.utils.ItemTouchHelperCallback;
import com.nextGapps.guru.weather.utils.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;


public class MainActivity extends AppCompatActivity implements LocationListener {


    private List<CityWeather> cities;
    @BindView(R.id.recyclerViewWeatherCards)
    RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    @BindView(R.id.swipeRefresh)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.fabAddCity)
    FloatingActionButton fabAddCity;
    private WeatherServices weatherServices;
    private MaterialTapTargetPrompt mFabPrompt;

    private ArrayList<String> permissionsToRequest;
    private ArrayList<String> permissionsRejected = new ArrayList<>();
    private ArrayList<String> permissions = new ArrayList<>();
    private final static int ALL_PERMISSIONS_RESULT = 101;
    LocationTrack locationTrack;
    protected Context context;
    Intent syncIntent;
    SyncManager manager;
    public static Context mContext;
    private double latitude, longitude;
    public LocationManager mLocManager;
    long recordInsertResult;

    DataBaseAdapter dataBaseAdapter;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        setContentView(R.layout.activity_main);
        context = this;

        recyclerView = findViewById(R.id.recyclerViewWeatherCards);
        dataBaseAdapter = DataBaseAdapter.getInstance(this);
        if (!Utils.isConnected(context)) {
            layoutManager = new LinearLayoutManager(this);
            cities = dataBaseAdapter.getAllWetherReport();
            Log.e("TAG", "City" + cities.get(0).getCity().getName());
            Log.e("TAG", "Pressure " + cities.get(0).getWeeklyWeather().get(0).getPressure());
//            Log.e("TAG", "City" + cities.get(0).getCity().getName());
//            Log.e("TAG", "City" + cities.get(0).getCity().getName());
            adapter = new CityWeatherAdapter(cities, R.layout.weather_card, this, null);
            Toast.makeText(context, "Connect To WiFi", Toast.LENGTH_SHORT).show();
            recyclerView.setHasFixedSize(true);
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setAdapter(adapter);

        } else
            startBackgroundSyncService();
        getSupportActionBar().hide();

        mLocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mLocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0,
                this);
        mLocManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0,
                0, this);
        locationUpdate();


        permissions.add(ACCESS_FINE_LOCATION);
        permissions.add(ACCESS_COARSE_LOCATION);

        permissionsToRequest = findUnAskedPermissions(permissions);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {


            if (permissionsToRequest.size() > 0)
                requestPermissions(permissionsToRequest.toArray(new String[permissionsToRequest.size()]), ALL_PERMISSIONS_RESULT);
        }
        ButterKnife.bind(this);
        locationTrack = new LocationTrack(MainActivity.this);
        cities = getCities();
        if (cities.size() == 0) {
        }

        weatherServices = API.getApi().create(WeatherServices.class);
        layoutManager = new LinearLayoutManager(this);
        adapter = new CityWeatherAdapter(cities, R.layout.weather_card, this, new CityWeatherAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(CityWeather cityWeather, int position, View view) {
                Intent intent = new Intent(MainActivity.this, WeatherDetails.class);
                ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(MainActivity.this, view, "weatherCardTransition");
                intent.putExtra("city", cityWeather);
                startActivity(intent, options.toBundle());
            }
        });


        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);


        fabAddCity.setOnClickListener(view -> {
            showAlertAddCity(false, "Add city", "Type the city you want to add");
        });

        swipeRefreshLayout.setColorSchemeResources(R.color.google_blue, R.color.google_green, R.color.google_red, R.color.google_yellow);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            refreshData();
        });


        ItemTouchHelper.Callback callback = new ItemTouchHelperCallback((onSwipeListener) adapter);
        ItemTouchHelper mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(recyclerView);

    }

    //Still needs some fix
    public void recyclerScrollTo(int pos) {
        recyclerView.scrollToPosition(pos);
    }

    public void showFabPrompt() {
        if (mFabPrompt != null) {
            return;
        }

        if (locationTrack.canGetLocation()) {

            double longitude = locationTrack.getLongitude();
            double latitude = locationTrack.getLatitude();

            Toast.makeText(getApplicationContext(), "Longitude:" + Double.toString(longitude) + "\nLatitude:" + Double.toString(latitude), Toast.LENGTH_SHORT).show();
        } else {

            locationTrack.showSettingsAlert();
        }

        mFabPrompt = new MaterialTapTargetPrompt.Builder(MainActivity.this)
                .setTarget(findViewById(R.id.fabAddCity))
                .setFocalPadding(R.dimen.dp40)
                .setPrimaryText("Add your first City")
                .setSecondaryText("Tap the add button and add your favorites cities to get weather updates")
                .setBackButtonDismissEnabled(true)
                .setAnimationInterpolator(new FastOutSlowInInterpolator())
                .setPromptStateChangeListener((prompt, state) -> {
                    if (state == MaterialTapTargetPrompt.STATE_FOCAL_PRESSED || state == MaterialTapTargetPrompt.STATE_DISMISSING) {
                        mFabPrompt = null;
                        //Do something such as storing a value so that this prompt is never shown again
                    }
                })
                .create();
        mFabPrompt.show();
    }


    private void refreshData() {
        for (int i = 0; i < cities.size(); i++) {
            updateCity(cities.get(i).getCity().getName(), i);
            System.out.println("CIUDAD #" + i);
        }
        System.out.println("TERMINE EL REFREHS!!!!");
        swipeRefreshLayout.setRefreshing(false);
    }

    private String cityToAdd = "";

    public void showAlertAddCity(boolean first, String title, String message) {
        if (first) {
            addCity("Bengaluru");
        } else {


            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            if (title != null) builder.setTitle(title);
            if (message != null) builder.setMessage(message);
            final View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_city, null);
            builder.setView(view);
            final TextView editTextAddCityName = view.findViewById(R.id.editTextAddCityName);

            final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

            builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    cityToAdd = editTextAddCityName.getText().toString();
//                    addCity(cityToAdd);
                    imm.toggleSoftInput(InputMethodManager.HIDE_NOT_ALWAYS, 0);
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                    imm.toggleSoftInput(InputMethodManager.HIDE_NOT_ALWAYS, 0);
                    Toast.makeText(MainActivity.this, "Cancel", Toast.LENGTH_LONG).show();
                }
            });
            builder.create().show();
        }
    }


    public void updateCity(String cityName, int index) {
        Call<CityWeather> cityWeather = weatherServices.getWeatherCity(cityName, API.KEY, "metric", 6);
        cityWeather.enqueue(new Callback<CityWeather>() {
            @Override
            public void onResponse(Call<CityWeather> call, Response<CityWeather> response) {
                if (response.code() == 200) {
                    CityWeather cityWeather = response.body();
                    cities.remove(index);
                    cities.add(index, cityWeather);
                    adapter.notifyItemChanged(index);
                }
            }

            @Override
            public void onFailure(Call<CityWeather> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Sorry, can't refresh right now.", Toast.LENGTH_LONG).show();
            }
        });
    }


    public void addCity(String cityName) {

        Call<CityWeather> cityWeather = weatherServices.getWeatherCity(cityName, API.KEY, "metric", 6);
        cityWeather.enqueue(new Callback<CityWeather>() {
            @Override
            public void onResponse(Call<CityWeather> call, Response<CityWeather> response) {
                if (response.code() == 200) {
                    CityWeather cityWeather = response.body();

                    recordInsertResult = dataBaseAdapter.addWetherData(cityWeather);
                    Log.e("Database ", "Insert Code" + recordInsertResult);
                    cities.add(cityWeather);
                    adapter.notifyItemInserted(cities.size() - 1);
                    recyclerView.scrollToPosition(cities.size() - 1);

                } else {
                    Toast.makeText(MainActivity.this, "Sorry, city not found", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<CityWeather> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Sorry, weather services are currently unavailable", Toast.LENGTH_LONG).show();
            }
        });
    }

    private List<CityWeather> getCities() {
        return new ArrayList<CityWeather>() {
            {
            }
        };
    }

    private ArrayList<String> findUnAskedPermissions(ArrayList<String> wanted) {
        ArrayList<String> result = new ArrayList<String>();

        for (String perm : wanted) {
            if (!hasPermission(perm)) {
                result.add(perm);
            }
        }

        return result;
    }

    private boolean hasPermission(String permission) {
        if (canMakeSmores()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED);
            }
        }
        return true;
    }

    private boolean canMakeSmores() {
        return (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        switch (requestCode) {

            case ALL_PERMISSIONS_RESULT:
                for (String perms : permissionsToRequest) {
                    if (!hasPermission(perms)) {
                        permissionsRejected.add(perms);
                    }
                }

                if (permissionsRejected.size() > 0) {


                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (shouldShowRequestPermissionRationale(permissionsRejected.get(0))) {
                            showMessageOKCancel("These permissions are mandatory for the application. Please allow access.",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                requestPermissions(permissionsRejected.toArray(new String[permissionsRejected.size()]), ALL_PERMISSIONS_RESULT);
                                            }
                                        }
                                    });
                            return;
                        }
                    }

                }

                break;
        }

    }


    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        locationTrack.stopListener();
    }

    public void startBackgroundSyncService() {
        syncIntent = new Intent(this, SyncService.class);
        startService(syncIntent);
    }


    public void locationUpdate() {
        CellLocation.requestLocationUpdate();
    }


    public void getAddress(double lat, double lng) {
        Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            Address obj = addresses.get(0);
            String add = obj.getAddressLine(0);


            String currentAddress = obj.getSubAdminArea() + ","
                    + obj.getAdminArea();
            double latitude = obj.getLatitude();
            double longitude = obj.getLongitude();
            String currentCity = obj.getSubAdminArea();
            String city = obj.getLocality();
            String currentState = obj.getAdminArea();
            add = add + "\n" + obj.getCountryName();
            add = add + "\n" + obj.getCountryCode();
            add = add + "\n" + obj.getAdminArea();
            add = add + "\n" + obj.getPostalCode();
            add = add + "\n" + obj.getSubAdminArea();
            add = add + "\n" + obj.getLocality();
            add = add + "\n" + obj.getSubThoroughfare();

            Log.v("IGA", "Address" + latitude);
            Log.v("IGA", "Address" + longitude);
            Log.v("IGA", "Address" + currentAddress);
            Log.v("IGA", "Address" + currentCity);
            Log.v("IGA", "Address" + currentState);
            Log.v("IGA", "Address" + city);
            addCity(city);
            // Toast.makeText(this, "Address=>" + add,
            // Toast.LENGTH_SHORT).show();

            // TennisAppActivity.showDialog(add);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }


    public void onLocationChanged(Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();
//        GUIStatics.latitude = location.getLatitude();
//        GUIStatics.longitude = location.getLongitude();
        Log.e("Test", "IGA" + "Lat" + latitude + "   Lng" + longitude);
        //mLocManager.r

        getAddress(latitude, longitude);
        if (location != null) {

            mLocManager.removeUpdates(this);
        }
        // Toast.makeText(this, "Lat" + latitude + "   Lng" + longitude,
        // Toast.LENGTH_SHORT).show();
    }


    public void onProviderDisabled(String arg0) {
        // TODO Auto-generated method stub
        Toast.makeText(MainActivity.this, "Gps Disabled", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(
                android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(intent);
    }


    public void onProviderEnabled(String arg0) {
        // TODO Auto-generated method stub

    }


    public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
        if (arg1 == LocationProvider.TEMPORARILY_UNAVAILABLE) {
            Toast.makeText(MainActivity.this,
                    "LocationProvider.TEMPORARILY_UNAVAILABLE",
                    Toast.LENGTH_SHORT).show();
        } else if (arg1 == LocationProvider.OUT_OF_SERVICE) {
            Toast.makeText(MainActivity.this,
                    "LocationProvider.OUT_OF_SERVICE", Toast.LENGTH_SHORT).show();
        }

    }

}
