package com.example.weathercomposeapp.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.weathercomposeapp.model.Favorites
import kotlinx.coroutines.flow.Flow

@Dao
interface WeatherDao {
    @Query("Select * from fav_tbl")
    fun getFavorites(): Flow<List<Favorites>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveFavorites(favorites: Favorites)

    @Update
    suspend fun upadteFavorites(favorites: Favorites)

    @Query("Delete from fav_tbl")
    suspend fun deleteAllFav()

    @Delete
    suspend fun deleteFavCity(favorites: Favorites)


    @Query("select * from fav_tbl where city = :city")
    suspend fun getFavById(city: String): Favorites
}