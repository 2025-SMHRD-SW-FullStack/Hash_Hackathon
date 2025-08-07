package com.example.talent_link.ui.LocalLife

import com.example.talent_link.ui.LocalLife.dto.LikeRequest
import com.example.talent_link.ui.LocalLife.dto.LikeStatusDto
import com.example.talent_link.ui.LocalLife.dto.LocalComment
import com.example.talent_link.ui.LocalLife.dto.LocalCommentRequest
import com.example.talent_link.ui.LocalLife.dto.LocalPost
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface LocalLifeApi {

    @GET("/api/localposts")
    suspend fun getPosts(
        @Header("Authorization") token: String
    ): Response<List<LocalPost>>

    // ✅ 이미지와 글을 함께 업로드하는 함수 추가
    @Multipart
    @POST("/api/localposts")
    suspend fun uploadPost(
        @Header("Authorization") token: String,
        @Part("request") request: RequestBody,
        @Part image: MultipartBody.Part?
    ): Response<LocalPost>


    // --- 아래는 기존 코드 ---
    @GET("/api/localposts")
    suspend fun getAllPosts(
        @Header("Authorization") jwt: String
    ): List<LocalPost>

    @GET("/api/localposts/{postId}")
    suspend fun getPost(
        @Path("postId") postId: Long,
        @Header("Authorization") jwt: String
    ): Response<LocalPost>

    @GET("/api/localposts/{postId}/likes/me")
    suspend fun getMyLike(
        @Path("postId") postId: Long,
        @Query("userId") userId: Long,
        @Header("Authorization") jwt: String
    ): LikeStatusDto

    @POST("/api/localposts/{postId}/likes")
    suspend fun likePost(
        @Path("postId") postId: Long,
        @Body req: LikeRequest,
        @Header("Authorization") jwt: String
    ): Response<Unit>

    @HTTP(method = "DELETE", path = "/api/localposts/{postId}/likes", hasBody = true)
    suspend fun unlikePost(
        @Path("postId") postId: Long,
        @Body req: LikeRequest,
        @Header("Authorization") jwt: String
    ): Response<Unit>

    @GET("/api/posts/{postId}/comments")
    suspend fun getComments(
        @Path("postId") postId: Long,
        @Header("Authorization") jwt: String
    ): Response<List<LocalComment>>

    @POST("/api/posts/{postId}/comments")
    suspend fun addComment(
        @Path("postId") postId: Long,
        @Body comment: LocalCommentRequest,
        @Header("Authorization") jwt: String
    ): Response<LocalComment>
}