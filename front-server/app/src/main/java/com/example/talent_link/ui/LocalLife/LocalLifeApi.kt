package com.example.talent_link.ui.LocalLife

import com.example.talent_link.ui.LocalLife.dto.LikeRequest
import com.example.talent_link.ui.LocalLife.dto.LikeStatusDto
import com.example.talent_link.ui.LocalLife.dto.LocalComment
import com.example.talent_link.ui.LocalLife.dto.LocalCommentRequest
import com.example.talent_link.ui.LocalLife.dto.LocalPost
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface LocalLifeApi {

    @GET("/api/localposts")
    suspend fun getPosts(
        @Header("Authorization") token: String
    ): Response<List<LocalPost>>

    // 게시글 목록 조회
    @GET("/api/localposts")
    suspend fun getAllPosts(
        @Header("Authorization") jwt: String
    ): List<LocalPost>

    // 게시글 상세 조회
    @GET("/api/localposts/{postId}")
    suspend fun getPost(
        @Path("postId") postId: Long,
        @Header("Authorization") jwt: String
    ): Response<LocalPost>

    // 좋아요 상태/개수 조회
    @GET("/api/localposts/{postId}/likes/me")
    suspend fun getMyLike(
        @Path("postId") postId: Long,
        @Query("userId") userId: Long,
        @Header("Authorization") jwt: String
    ): LikeStatusDto

    // 좋아요 누르기
    @POST("/api/localposts/{postId}/likes")
    suspend fun likePost(
        @Path("postId") postId: Long,
        @Body req: LikeRequest,
        @Header("Authorization") jwt: String
    ): Response<Unit>

    // 좋아요 취소
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