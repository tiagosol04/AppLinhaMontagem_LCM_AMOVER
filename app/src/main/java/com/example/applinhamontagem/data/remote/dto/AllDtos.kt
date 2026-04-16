package com.example.applinhamontagem.data.remote.dto

import com.google.gson.annotations.SerializedName

// --- AUTH ---
data class LoginRequest(
    @SerializedName("usernameOrEmail") val usernameOrEmail: String,
    @SerializedName("password") val password: String
)

data class LoginResponse(
    @SerializedName("token") val token: String,
    @SerializedName("userId") val userId: String,
    @SerializedName("username") val username: String,
    @SerializedName("email") val email: String,
    @SerializedName("roles") val roles: List<String>,
    @SerializedName("expiresInMinutes") val expiresInMinutes: Int? = null
)

// --- CHECKLISTS ---
data class ChecklistsStatusDto(
    @SerializedName("ordemId") val ordemId: Int,
    @SerializedName("montagem") val montagem: List<ChecklistItemDto>,
    @SerializedName("embalagem") val embalagem: List<ChecklistItemDto>,
    @SerializedName("controlo") val controlo: List<ChecklistItemDto>
)

data class ChecklistItemDto(
    @SerializedName(value = "idChecklist", alternate = ["IDChecklist", "IdChecklist"]) val idChecklist: Int, // O JSON devolve "idChecklist" (PascalCase ou camelCase, Gson gere)
    @SerializedName("nome") val nome: String,
    @SerializedName("tipo") val tipo: Int,
    @SerializedName(value = "verificado", alternate = ["value", "Value", "Verificado"]) val verificado: Int? = null,
    @SerializedName(value = "incluido", alternate = ["value", "Value", "Incluido"]) val incluido: Int? = null,
    @SerializedName(value = "controloFinal", alternate = ["value", "Value", "ControloFinal"]) val controloFinal: Int? = null
)

// Usado para atualizar flags de checklist (0 ou 1)
data class UpdateFlagRequest(
    @SerializedName("value") val value: Int
)

// --- ENCOMENDAS & ORDENS ---
data class EncomendaDto(
    @SerializedName("idEncomenda") val idEncomenda: Int,
    @SerializedName("idCliente") val idCliente: Int,
    @SerializedName("idModelo") val idModelo: Int,
    @SerializedName("quantidade") val quantidade: Int,
    @SerializedName("estado") val estado: Int,
    @SerializedName("dataCriacao") val dataCriacao: String? = null,
    @SerializedName("dataEntrega") val dataEntrega: String? = null
)

data class OrdemProducaoDto(
    @SerializedName("idOrdemProducao") val idOrdemProducao: Int,
    @SerializedName("numeroOrdem") val numeroOrdem: String,
    @SerializedName("estado") val estado: Int,
    @SerializedName("paisDestino") val paisDestino: String? = null,
    @SerializedName("idModelo") val idModelo: Int, // Mapeado do teu "idModelo" extra no controller
    @SerializedName("idCliente") val idCliente: Int? = null,
    @SerializedName("dataCriacao") val dataCriacao: String? = null,
    @SerializedName("dataConclusao") val dataConclusao: String? = null
)

data class UpdateEstadoRequest(
    @SerializedName("estado") val estado: Int
)

// --- MOTAS ---
data class MotaAtribuidaDto(
    @SerializedName("idUtilizadorMota") val idUtilizadorMota: Int,
    @SerializedName("utilizadorId") val utilizadorId: Int,
    @SerializedName("motaId") val motaId: Int,
    @SerializedName("dataCriacao") val dataCriacao: String? = null,
    @SerializedName("estadoAssociacao") val estadoAssociacao: Int? = null,
    @SerializedName("numeroIdentificacao") val numeroIdentificacao: String? = null,
    @SerializedName("cor") val cor: String? = null,
    @SerializedName("estadoMota") val estadoMota: Int? = null,
    @SerializedName("idOrdemProducao") val idOrdemProducao: Int? = null,

    // ✅ ADICIONA ESTE CAMPO para corrigir o erro na Dashboard
    @SerializedName("idModelo") val idModelo: Int? = null
)
data class MotaDto(
    @SerializedName("idMota") val idMota: Int,
    @SerializedName("idModelo") val idModelo: Int,
    @SerializedName("idOrdemProducao") val idOrdemProducao: Int,
    @SerializedName("numeroIdentificacao") val numeroIdentificacao: String? = null,
    @SerializedName("cor") val cor: String? = null,
    @SerializedName("estado") val estado: Int? = null,
    @SerializedName("quilometragem") val quilometragem: Double? = null,
    @SerializedName("dataRegisto") val dataRegisto: String? = null
)

data class CriarMotaRequest(
    @SerializedName("idModelo") val idModelo: Int,
    @SerializedName("cor") val cor: String = "N/A",
    @SerializedName("quilometragem") val quilometragem: Double = 0.0,
    @SerializedName("estado") val estado: Int = 1, // Default 1
    @SerializedName("idOrdemProducao") val idOrdemProducao: Int, // Adicionado conforme teu controller
    @SerializedName("numeroIdentificacao") val numeroIdentificacao: String = ""
)

data class CriarMotaResponse(@SerializedName("idMota") val idMota: Int)

// --- PEÇAS ---
data class ModeloPecaSnDto(
    @SerializedName("idModeloPSN") val idModeloPsn: Int, // Mapeia "idModeloPSN"
    @SerializedName("idModelo") val idModelo: Int,
    @SerializedName("idPeca") val idPeca: Int,
    @SerializedName("partNumber") val partNumber: String? = null,
    @SerializedName("descricao") val descricao: String? = null,
    @SerializedName("especificacaoPadrao") val especificacaoPadrao: String? = null
)

data class MotaPecaSnDto(
    @SerializedName("idMotasPecasSN") val idMotasPecasSN: Int,
    @SerializedName("idMota") val idMota: Int,
    @SerializedName("idPeca") val idPeca: Int,
    @SerializedName("partNumber") val partNumber: String? = null,
    @SerializedName("descricao") val descricao: String? = null,
    @SerializedName("numeroSerie") val numeroSerie: String? = null
)

data class AddPecaSnRequest(
    @SerializedName("idPeca") val idPeca: Int,
    @SerializedName("numeroSerie") val numeroSerie: String
)

data class AddPecaSnResponse(
    @SerializedName("idMotasPecasSN") val idMotasPecasSN: Int,
    @SerializedName("created") val created: Boolean
)
// --- INICIAR / FINALIZAR ORDEM ---
data class IniciarOrdemResponse(
    @SerializedName("message") val message: String? = null,
    @SerializedName("ordemId") val ordemId: Int? = null,
    @SerializedName("estado") val estado: Int? = null
)

data class FinalizarOrdemResponse(
    @SerializedName("message") val message: String? = null,
    @SerializedName("ordemId") val ordemId: Int? = null,
    @SerializedName("estado") val estado: Int? = null,
    @SerializedName("dataConclusao") val dataConclusao: String? = null
)

// --- VIN ---
data class UpdateVinRequest(
    @SerializedName("numeroIdentificacao") val numeroIdentificacao: String
)

data class UpdateVinResponse(
    @SerializedName("message") val message: String? = null,
    @SerializedName("idMota") val idMota: Int? = null,
    @SerializedName("numeroIdentificacao") val numeroIdentificacao: String? = null
)

// --- RESUMO ORDEM ---
data class OrdemResumoDto(
    @SerializedName("ordemId") val ordemId: Int = 0,
    @SerializedName("motas") val motas: Int = 0,
    @SerializedName("servicos") val servicos: Int = 0,
    @SerializedName("temMotaAssociada") val temMotaAssociada: Boolean = false,
    @SerializedName("vinPreenchido") val vinPreenchido: Boolean = false,
    @SerializedName("motaId") val motaId: Int? = null,
    @SerializedName("checklists") val checklists: OrdemResumoChecklistsDto? = null,
    @SerializedName("pecasSn") val pecasSn: OrdemResumoPecasDto? = null
)

data class OrdemResumoChecklistsDto(
    @SerializedName("prontoParaFinalizar") val prontoParaFinalizar: Boolean = false,
    @SerializedName("montagem") val montagem: ResumoSecaoDto = ResumoSecaoDto(),
    @SerializedName("embalagem") val embalagem: ResumoSecaoDto = ResumoSecaoDto(),
    @SerializedName("controlo") val controlo: ResumoSecaoDto = ResumoSecaoDto()
)

data class ResumoSecaoDto(
    @SerializedName("total") val total: Int = 0,
    @SerializedName("feitos") val feitos: Int = 0,
    @SerializedName("ok") val ok: Boolean = false,
    @SerializedName("inicializado") val inicializado: Boolean = false
)

data class OrdemResumoPecasDto(
    @SerializedName("obrigatorias") val obrigatorias: Int = 0,
    @SerializedName("preenchidas") val preenchidas: Int = 0,
    @SerializedName("ok") val ok: Boolean = false
)

// --- RESUMO PECAS SN ---
data class PecasSnResumoDto(
    @SerializedName("motaId") val motaId: Int? = null,
    @SerializedName("totalObrigatorias") val totalObrigatorias: Int = 0,
    @SerializedName("preenchidas") val preenchidas: Int = 0,
    @SerializedName("ok") val ok: Boolean = false
)

