package com.example.applinhamontagem.data.remote

import com.example.applinhamontagem.data.remote.dto.*
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

    @GET("api/ordens/{id}/resumo")
    suspend fun getOrdemResumo(@Path("id") id: Int): Response<OrdemResumoDto>

    // Iniciar ordem (cria checklists via API)
    @POST("api/ordens/{id}/iniciar")
    suspend fun iniciarOrdem(@Path("id") id: Int): Response<IniciarOrdemResponse>

    // Finalizar ordem (valida tudo: VIN, pecas, checklists)
    @POST("api/ordens/{id}/finalizar")
    suspend fun finalizarOrdem(@Path("id") id: Int): Response<FinalizarOrdemResponse>

    // --- ENCOMENDAS ---
    @GET("api/encomendas")
    suspend fun getEncomendas(): Response<List<EncomendaDto>>

    // --- MOTAS ---
    @GET("api/motas/{id}")
    suspend fun getMota(@Path("id") id: Int): Response<MotaDto>

    @GET("api/motas/by-vin/{vin}")
    suspend fun getMotaByVin(@Path("vin") vin: String): Response<MotaDto>

    @PUT("api/motas/{id}/estado")
    suspend fun updateEstadoMota(@Path("id") idMota: Int, @Body req: UpdateEstadoRequest): Response<Unit>

    // Atualizar VIN / numero de quadro
    @PUT("api/motas/{id}/identificacao")
    suspend fun updateVin(@Path("id") idMota: Int, @Body req: UpdateVinRequest): Response<UpdateVinResponse>

    // Pecas Montadas
    @GET("api/motas/{id}/pecas-sn")
    suspend fun getPecasMontadas(@Path("id") idMota: Int): Response<List<MotaPecaSnDto>>

    @POST("api/motas/{id}/pecas-sn")
    suspend fun registarPeca(@Path("id") idMota: Int, @Body req: AddPecaSnRequest): Response<AddPecaSnResponse>

    // --- MODELOS (Pecas Necessarias) ---
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
}
