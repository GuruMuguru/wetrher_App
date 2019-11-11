package com.nextGapps.guru.weather.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.nextGapps.guru.weather.R;
import com.nextGapps.guru.weather.models.CityWeather;
import com.nextGapps.guru.weather.utils.IconProvider;
import com.squareup.picasso.Picasso;


import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import butterknife.BindView;
import butterknife.ButterKnife;

public class WeatherDetails extends AppCompatActivity {

    @BindView(R.id.textViewHumidity)
    TextView textViewHumidity;
    @BindView(R.id.textViewWind)
    TextView textViewWind;
    @BindView(R.id.textViewCloudiness)
    TextView textViewCloudiness;
    @BindView(R.id.textViewPressure)
    TextView textViewPressure;

    //    private CityWeather cityWeather;
    private CityWeather cityWeather;
    String[] namesOfDays = {
            "SAT", "SUN", "MON", "TUE", "WED", "THU", "FRI",
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather_details);

        ButterKnife.bind(this);
        Bundle bundle = getIntent().getExtras();
        if (!bundle.isEmpty()) {
            cityWeather = (CityWeather) bundle.getSerializable("city");
        }
        setCardData();

    }

    private void setCardData() {
        textViewHumidity.setText((int) cityWeather.getWeeklyWeather().get(0).getHumidity() + "%");
        textViewWind.setText((int) cityWeather.getWeeklyWeather().get(0).getSpeed() + " m/s");
        textViewCloudiness.setText((int) cityWeather.getWeeklyWeather().get(0).getClouds() + "%");
        textViewPressure.setText((int) cityWeather.getWeeklyWeather().get(0).getPressure() + " hPa");


        String weatherDescription = cityWeather.getWeeklyWeather().get(0).getWeatherDetails().get(0).getShotDescription();

//        Picasso.with(this).load(IconProvider.getImageIcon(weatherDescription)).into(imageViewWeatherIcon);


    }
}
