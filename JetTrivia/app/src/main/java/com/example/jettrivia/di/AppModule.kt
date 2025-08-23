package com.example.jettrivia.di

import androidx.compose.ui.platform.LocalContext
import com.example.jettrivia.helper.DataStoreManageer
import com.example.jettrivia.network.QuestionApi
import com.example.jettrivia.repository.QuestionRepository
import com.example.jettrivia.utils.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideQuestionRepository(api: QuestionApi) = QuestionRepository(api)


    @Singleton
    @Provides
    fun provideQuestionApi(): QuestionApi {
        return Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(QuestionApi::class.java)
    }
//
//    @Singleton
//    @Provides
//    fun provideDataStoreManager() = DataStoreManageer(LocalContext.current)
}