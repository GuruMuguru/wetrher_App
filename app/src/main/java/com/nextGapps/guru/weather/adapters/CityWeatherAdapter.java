package com.nextGapps.guru.weather.adapters;

import android.app.Activity;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nextGapps.guru.weather.R;
import com.nextGapps.guru.weather.interfaces.onSwipeListener;
import com.nextGapps.guru.weather.models.CityWeather;
import com.nextGapps.guru.weather.utils.IconProvider;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CityWeatherAdapter extends RecyclerView.Adapter<CityWeatherAdapter.ViewHolder> implements onSwipeListener {
    private List<CityWeather> cities;
    private int layoutReference;
    private OnItemClickListener onItemClickListener;
    private Activity activity;
    private View parentView;

    public CityWeatherAdapter(List<CityWeather> cities, int layoutReference, Activity activity, OnItemClickListener onItemClickListener) {
        this.cities = cities;
        this.layoutReference = layoutReference;
        this.activity = activity;
        this.onItemClickListener = onItemClickListener;

    }

    @Override
    public CityWeatherAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        parentView = parent;
        View view = LayoutInflater.from(activity).inflate(layoutReference, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(CityWeatherAdapter.ViewHolder holder, int position) {
        holder.bind(cities.get(position), onItemClickListener);
    }

    @Override
    public int getItemCount() {
        return cities.size();
    }

    @Override
    public void onItemDelete(final int position) {
        CityWeather tempCity = cities.get(position);
        cities.remove(position);
        notifyItemRemoved(position);

        Snackbar.make(parentView, "Removed", Snackbar.LENGTH_LONG)
                .setAction("Undo", v -> {
                    addItem(position, tempCity);
                }).show();

    }

    public void addItem(int position, CityWeather city) {
        cities.add(position, city);
        notifyItemInserted(position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.textViewCardCityName)
        TextView textViewCityName;
        @BindView(R.id.textViewCardWeatherDescription)
        TextView textViewWeatherDescription;
        @BindView(R.id.textViewCardCurrentTemp)
        TextView textViewCurrentTemp;
        @BindView(R.id.textViewCardMaxTemp)
        TextView textViewMaxTemp;
        @BindView(R.id.textViewCardMinTemp)
        TextView textViewMinTemp;
        @BindView(R.id.imageViewCardWeatherIcon)
        ImageView imageViewWeatherIcon;
//        @BindView(R.id.cardViewWeatherCard)
//        CardView cardViewWeather;

        @BindView(R.id.textViewHumidity)
        TextView textViewHumidity;
        @BindView(R.id.textViewWind)
        TextView textViewWind;
        @BindView(R.id.textViewCloudiness)
        TextView textViewCloudiness;
        @BindView(R.id.textViewPressure)
        TextView textViewPressure;


        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bind(final CityWeather cityWeather, final OnItemClickListener onItemClickListener) {
            textViewCityName.setText(cityWeather.getCity().getName() + ", " + cityWeather.getCity().getCountry());
            textViewWeatherDescription.setText(cityWeather.getWeeklyWeather().get(0).getWeatherDetails().get(0).getLongDescription());
            textViewCurrentTemp.setText(cityWeather.getWeeklyWeather().get(0).getTemp().getDay() + "°");
            textViewMaxTemp.setText(cityWeather.getWeeklyWeather().get(0).getTemp().getMax() + "°");
            textViewMinTemp.setText(cityWeather.getWeeklyWeather().get(0).getTemp().getMin() + "°");
            String weatherDescription = cityWeather.getWeeklyWeather().get(0).getWeatherDetails().get(0).getShotDescription();
            textViewHumidity.setText(cityWeather.getWeeklyWeather().get(0).getHumidity() + "%");
            textViewWind.setText(cityWeather.getWeeklyWeather().get(0).getSpeed() + " m/s");
            textViewCloudiness.setText(cityWeather.getWeeklyWeather().get(0).getClouds() + "%");
            textViewPressure.setText(cityWeather.getWeeklyWeather().get(0).getPressure() + " hPa");

            Picasso.with(activity).load(IconProvider.getImageIcon(weatherDescription)).into(imageViewWeatherIcon);


        }
    }

    public interface OnItemClickListener {
        void onItemClick(CityWeather cityWeather, int position, View view);
    }

}
