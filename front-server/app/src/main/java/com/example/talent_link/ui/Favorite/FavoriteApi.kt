package com.example.talent_link.ui.Favorite

import com.example.talent_link.ui.Favorite.dto.FavoriteDeleteRequest
import com.example.talent_link.ui.Favorite.dto.FavoriteRequest
import com.example.talent_link.ui.Favorite.dto.FavoriteResponse
import retrofit2.Response
import retrofit2.http.*

interface FavoriteApi {
    @GET("/api/favorite")
    suspend fun getFavoriteList(
        @Header("Authorization") token: String,
        @Query("userId") userId: String
    ): Response<List<FavoriteResponse>>

    @POST("/api/favorite/save")
    suspend fun addFavorite(
        @Header("Authorization") token: String,
        @Body request: FavoriteRequest
    ): Response<Map<String, String>>

    @POST("/api/favorite/delete")
    suspend fun deleteFavorite(
        @Header("Authorization") token: String,
        @Body request: FavoriteDeleteRequest
    ): Response<Any>
}
