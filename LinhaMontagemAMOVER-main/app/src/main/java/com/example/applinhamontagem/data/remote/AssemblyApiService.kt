package com.example.applinhamontagem.data.remote

import com.example.applinhamontagem.data.remote.dto.*
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface AssemblyApiService {

    // --- AUTH ---
    @POST("api/Auth/login")
    suspend fun login(@Body req: LoginRequest): Response<LoginResponse>

    // --- ORDENS ---
    @GET("api/ordens")
    suspend fun getOrdens(@Query("estado") estado: Int? = null): Response<List<OrdemProducaoDto>>

    @GET("api/ordens/{id}")
    suspend fun getOrdem(@Path("id") id: Int): Response<OrdemProducaoDto>

    @GET("api/ordens/{id}/motas")
    suspend fun getMotasDaOrdem(@Path("id") idOrdem: Int): Response<List<MotaDto>>

    @POST("api/ordens/{id}/motas")
    suspend fun criarMotaNaOrdem(@Path("id") idOrdem: Int, @Body req: CriarMotaRequest): Response<CriarMotaResponse>

    @PUT("api/ordens/{id}/estado")
    suspend fun updateEstadoOrdem(@Path("id") idOrdem: Int, @Body req: UpdateEstadoRequest): Response<Unit>

    // --- ENCOMENDAS ---
    @GET("api/encomendas")
    suspend fun getEncomendas(): Response<List<EncomendaDto>>

    // --- MOTAS ---
    @GET("api/motas/{id}")
    suspend fun getMota(@Path("id") id: Int): Response<MotaDto>

    @GET("api/motas/by-vin/{vin}")
    suspend fun getMotaByVin(@Path("vin") vin: String): Response<MotaDto>

    // Peças Montadas
    @GET("api/motas/{id}/pecas-sn")
    suspend fun getPecasMontadas(@Path("id") idMota: Int): Response<List<MotaPecaSnDto>>

    @POST("api/motas/{id}/pecas-sn")
    suspend fun registarPeca(@Path("id") idMota: Int, @Body req: AddPecaSnRequest): Response<AddPecaSnResponse>

    // --- MODELOS (Peças Necessárias) ---
    @GET("api/modelos/{id}/pecas-sn")
    suspend fun getPecasNecessarias(@Path("id") idModelo: Int): Response<List<ModeloPecaSnDto>>

    // --- CHECKLISTS ---
    @GET("api/ordens/{id}/checklists")
    suspend fun getChecklistsDaOrdem(@Path("id") idOrdem: Int): Response<ChecklistsStatusDto>

    @PUT("api/ordens/{ordemId}/checklists/montagem/{checkId}")
    suspend fun updateMontagem(@Path("ordemId") ordemId: Int, @Path("checkId") checkId: Int, @Body req: UpdateFlagRequest): Response<Unit>

    @PUT("api/ordens/{ordemId}/checklists/embalagem/{checkId}")
    suspend fun updateEmbalagem(@Path("ordemId") ordemId: Int, @Path("checkId") checkId: Int, @Body req: UpdateFlagRequest): Response<Unit>

    @PUT("api/ordens/{ordemId}/checklists/controlo/{checkId}")
    suspend fun updateControlo(@Path("ordemId") ordemId: Int, @Path("checkId") checkId: Int, @Body req: UpdateFlagRequest): Response<Unit>

    // --- UTILIZADORES ---
    @GET("api/utilizadores/{id}/motas")
    suspend fun getMotasDoUtilizadorById(@Path("id") idUtilizador: Int): Response<List<MotaAtribuidaDto>>

    // ... outros métodos ...

    // ✅ NOVO: Atualizar estado da mota (Finalizar)
    @PUT("api/motas/{id}/estado")
    suspend fun updateEstadoMota(
        @Path("id") idMota: Int,
        @Body req: UpdateEstadoRequest // Podemos reutilizar o DTO que já tinhas
    ): Response<Unit>
}