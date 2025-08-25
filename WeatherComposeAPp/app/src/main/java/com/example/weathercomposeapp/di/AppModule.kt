package com.example.weathercomposeapp.di

import android.content.Context
import androidx.room.Room
import com.example.weathercomposeapp.data.WeatherDao
import com.example.weathercomposeapp.data.WeatherDatabase
import com.example.weathercomposeapp.network.WeatherApi
import com.example.weathercomposeapp.utils.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Provides
    @Singleton
    fun provideWeatherApi(): WeatherApi {
        return Retrofit.Builder()
            .baseUrl(Constants.BASE_URL_CURRENT)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WeatherApi::class.java)
    }

    @Provides
    @Singleton
    fun provideWeatherDao(weatherDatabase: WeatherDatabase): WeatherDao =
        weatherDatabase.weatherDao()

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): WeatherDatabase =
        Room.databaseBuilder(
            context,
            WeatherDatabase::class.java,
            "weather_database"
        ).fallbackToDestructiveMigration()
            .build()

}