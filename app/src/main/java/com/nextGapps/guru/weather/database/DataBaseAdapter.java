package com.nextGapps.guru.weather.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.util.Log;

import com.nextGapps.guru.weather.models.City;
import com.nextGapps.guru.weather.models.CityWeather;
import com.nextGapps.guru.weather.models.Temp;
import com.nextGapps.guru.weather.models.Weather;
import com.nextGapps.guru.weather.models.WeatherDetails;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.DoubleAccumulator;

public class DataBaseAdapter {

    private static DataBaseAdapter mInstance = null;
    SQLiteDatabase db;


    public static synchronized DataBaseAdapter getInstance(Context ctx) {
        if (mInstance == null) {
            mInstance = new DataBaseAdapter(ctx.getApplicationContext());
        }
        return mInstance;
    }

    private static String TAG = "DatabaseHandler";
    private DatabaseHandler databaseHandler;
    public static String mclassName = "DatabaseAdapter";

    private DataBaseAdapter(Context context) {
        databaseHandler = new DatabaseHandler(context);
        db = databaseHandler.getWritableDatabase();
    }

    public long addWetherData(@NonNull CityWeather cityWeather) {
        long insertRecordResult = 0;
        //       SQLiteDatabase db = databaseHandler.getWritableDatabase();

//        SQLiteDatabase DataBaseObj = null;
        try {
//            DataBaseObj = this.getWritableDatabase();
            ContentValues recordValues = new ContentValues();
            Log.i(TAG, "Inside addTransactionData ");
            recordValues.put(DatabaseHandler.KEY_CityName, cityWeather.getCity().getName());
            recordValues.put(DatabaseHandler.KEY_WeatherDescription, cityWeather.getWeeklyWeather().get(0).getWeatherDetails().get(0).getLongDescription());
            recordValues.put(DatabaseHandler.KEY_CurrentTemp, String.valueOf(cityWeather.getWeeklyWeather().get(0).getTemp().getDay()));
            recordValues.put(DatabaseHandler.KEY_MaxTemp, cityWeather.getWeeklyWeather().get(0).getTemp().getMax());
            recordValues.put(DatabaseHandler.KEY_MinTemp, cityWeather.getWeeklyWeather().get(0).getTemp().getMin());
            recordValues.put(DatabaseHandler.KEY_Humidity, cityWeather.getWeeklyWeather().get(0).getHumidity());
            recordValues.put(DatabaseHandler.KEY_Wind, cityWeather.getWeeklyWeather().get(0).getSpeed());
            recordValues.put(DatabaseHandler.KEY_Pressure, cityWeather.getWeeklyWeather().get(0).getPressure());
            recordValues.put(DatabaseHandler.KEY_Cloudiness, cityWeather.getWeeklyWeather().get(0).getClouds());


            insertRecordResult = db.insert(DatabaseHandler.TABLE_WHETHER_HISTORY, null, recordValues);
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return insertRecordResult;
    }

    public List<CityWeather> getAllWetherReport() {
        List<CityWeather> cityWeathers = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("select * from " + DatabaseHandler.TABLE_WHETHER_HISTORY, null);

            if (cursor.moveToFirst()) {
                do {

                    CityWeather weather = new CityWeather();
                    City city = new City();

                    Weather w1 = new Weather();


                    city.setName(cursor.getString(cursor.getColumnIndex(DatabaseHandler.KEY_CityName)));

                    w1.setSpeed(Float.parseFloat(cursor.getString(cursor.getColumnIndex(DatabaseHandler.KEY_Wind))));
                    w1.setClouds(Float.parseFloat(cursor.getString(cursor.getColumnIndex(DatabaseHandler.KEY_Cloudiness))));
                    w1.setPressure(Float.parseFloat(cursor.getString(cursor.getColumnIndex(DatabaseHandler.KEY_Pressure))));
                    w1.setHumidity(Float.parseFloat(cursor.getString(cursor.getColumnIndex(DatabaseHandler.KEY_Humidity))));

                    WeatherDetails details = new WeatherDetails();
                    List<WeatherDetails> weatherDetailsList = new ArrayList<>();
                    details.setLongDescription(cursor.getString(cursor.getColumnIndex(DatabaseHandler.KEY_CityName)));
                    weatherDetailsList.add(details);
                    w1.setWeatherDetails(weatherDetailsList);

                    Temp temp = new Temp();
                    temp.setDay(Float.parseFloat(cursor.getString(cursor.getColumnIndex(DatabaseHandler.KEY_CurrentTemp))));
                    temp.setMax(Float.parseFloat(cursor.getString(cursor.getColumnIndex(DatabaseHandler.KEY_MaxTemp))));
                    temp.setMin(Float.parseFloat(cursor.getString(cursor.getColumnIndex(DatabaseHandler.KEY_MinTemp))));
                    w1.setTemp(temp);

                    List<Weather> weatherList = new ArrayList<>();
                    weatherList.add(w1);

                    weather.setWeeklyWeather(weatherList);
                    weather.setCity(city);


                    cityWeathers.add(weather);

                    Log.i(mclassName, "New UPDATED Record ID " + cityWeathers.get(0).getCity().getName());

                } while (cursor.moveToNext());
                Log.i(mclassName, "Object in List " + cityWeathers.size());
            }
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            try {
                if (cursor != null) {
                    cursor.close();
                }
//                if (db != null) {
//                    db.close();
//                    db.endTransaction();
//                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
        return cityWeathers;
    }

    private static class DatabaseHandler extends SQLiteOpenHelper {

        private static final int DATABASE_VERSION = 1;
        // After modification increase the database version

        // Database Name
        private static final String DATABASE_NAME = "Mpos_Database";
        // Transaction History table name
        private static final String TABLE_WHETHER_HISTORY = "WhetherHistory";

        /*Column For wether history*/
        private static final String KEY_ID = "id";
        private static final String KEY_CityName = "CityName";
        private static final String KEY_WeatherDescription = "WeatherDescription";
        private static final String KEY_CurrentTemp = "CurrentTemp";
        private static final String KEY_MaxTemp = "MaxTemp";
        private static final String KEY_MinTemp = "MinTemp";
        private static final String KEY_Humidity = "Humidity";
        private static final String KEY_Wind = "Wind";
        private static final String KEY_Cloudiness = "Cloudiness";
        private static final String KEY_Pressure = "Pressure";


        private static final String WHETHER_HISTORY_TABLE_QUERY = "CREATE TABLE IF NOT EXISTS " + TABLE_WHETHER_HISTORY + "(" + KEY_ID + " INTEGER PRIMARY KEY," + KEY_CityName
                + " TEXT," + KEY_WeatherDescription + " TEXT," + KEY_CurrentTemp
                + " TEXT," + KEY_MaxTemp + " TEXT," + KEY_MinTemp + " TEXT," + KEY_Humidity + " TEXT," + KEY_Wind + " TEXT," + KEY_Cloudiness
                + " TEXT," + KEY_Pressure + " TEXT" + ")";

        private static final String WHETHER_TABLE_DROP_QUERY = "DROP TABLE IF EXISTS " + TABLE_WHETHER_HISTORY + " ;";

        public DatabaseHandler(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
//            Log.i(TAG, "DATABASE CREATED..");
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            try {
                db.execSQL(WHETHER_HISTORY_TABLE_QUERY);

            } catch (SQLException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            try {
                if (newVersion > oldVersion) {
                    Log.e("DatabaseAdapter", "Column Updated");
                    db.execSQL(WHETHER_TABLE_DROP_QUERY);
                }
                onCreate(db);
            } catch (SQLException e) {
                Log.e("DatabaseAdapter", "Column Updation Failed");
                e.printStackTrace();
            }
        }
    }
}
