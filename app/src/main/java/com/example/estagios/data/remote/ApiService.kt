package com.example.estagios.data.remote

import retrofit2.http.GET

interface ApiService {

    @GET("health")
    suspend fun checkHealth(): HealthResponse

    @GET("roles")
    suspend fun getRoles(): List<RoleResponse>
}