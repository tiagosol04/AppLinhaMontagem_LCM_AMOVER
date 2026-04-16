package com.example.applinhamontagem.data.repository

import com.example.applinhamontagem.data.remote.RetrofitClient
import com.example.applinhamontagem.data.remote.dto.*
import retrofit2.Response

class FactoryRepository {

    private val api = RetrofitClient.api

    enum class ChecklistTipo { MONTAGEM, EMBALAGEM, CONTROLO }

    private fun <T> errorMsg(resp: Response<T>, default: String) =
        "Erro ${resp.code()}: $default"

    // --- ORDENS ---
    suspend fun getEncomendas(): Result<List<EncomendaDto>> = runCatching {
        val res = api.getEncomendas()
        if (res.isSuccessful) res.body() ?: emptyList() else throw Exception(errorMsg(res, "Erro ao carregar Encomendas"))
    }

    suspend fun getOrdens(estado: Int? = null): Result<List<OrdemProducaoDto>> = runCatching {
        val res = api.getOrdens(estado)
        if (res.isSuccessful) res.body() ?: emptyList() else throw Exception(errorMsg(res, "Erro ao carregar Ordens"))
    }

    // --- MOTAS (Identificação e Atribuição) ---
    suspend fun getMotasAtribuidas(idUtilizador: Int): Result<List<MotaAtribuidaDto>> {
        return try {
            val res = api.getMotasDoUtilizadorById(idUtilizador)
            if (res.isSuccessful) Result.success(res.body() ?: emptyList())
            else Result.failure(Exception(errorMsg(res, "Erro ao carregar Motas Atribuídas")))
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun getMotaById(idMota: Int): Result<MotaDto> {
        return try {
            val res = api.getMota(idMota)
            if (res.isSuccessful && res.body() != null) Result.success(res.body()!!)
            else Result.failure(Exception(errorMsg(res, "Erro ao carregar Mota ID $idMota")))
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun buscarMotaPorVin(vin: String): Result<MotaDto> {
        return try {
            val res = api.getMotaByVin(vin)
            if (res.isSuccessful && res.body() != null) Result.success(res.body()!!)
            else Result.failure(Exception("Mota não encontrada com esse VIN."))
        } catch (e: Exception) { Result.failure(e) }
    }

    // --- PRODUÇÃO (Peças) ---
    suspend fun getDefinicaoPecas(idModelo: Int): Result<List<ModeloPecaSnDto>> = runCatching {
        val res = api.getPecasNecessarias(idModelo)
        if (res.isSuccessful) res.body() ?: emptyList() else throw Exception(errorMsg(res, "Erro ao carregar Definição Peças"))
    }

    suspend fun getPecasJaMontadas(idMota: Int): Result<List<MotaPecaSnDto>> = runCatching {
        val res = api.getPecasMontadas(idMota)
        if (res.isSuccessful) res.body() ?: emptyList() else throw Exception(errorMsg(res, "Erro ao carregar Peças Montadas"))
    }

    suspend fun registarMontagem(idMota: Int, idPeca: Int, sn: String): Result<AddPecaSnResponse> = runCatching {
        val req = AddPecaSnRequest(idPeca, sn)
        val res = api.registarPeca(idMota, req)
        if (res.isSuccessful && res.body() != null) res.body()!! else throw Exception(errorMsg(res, "Erro ao registar Peça"))
    }

    // --- CHECKLISTS ---
    suspend fun getChecklists(idOrdem: Int): Result<ChecklistsStatusDto> = runCatching {
        val res = api.getChecklistsDaOrdem(idOrdem)
        if (res.isSuccessful && res.body() != null) res.body()!! else throw Exception(errorMsg(res, "Erro ao carregar Checklists"))
    }

    suspend fun updateChecklist(idOrdem: Int, idChecklist: Int, tipo: ChecklistTipo, valor: Boolean): Result<Unit> = runCatching {
        val req = UpdateFlagRequest(if (valor) 1 else 0)
        val res = when (tipo) {
            ChecklistTipo.MONTAGEM -> api.updateMontagem(idOrdem, idChecklist, req)
            ChecklistTipo.EMBALAGEM -> api.updateEmbalagem(idOrdem, idChecklist, req)
            ChecklistTipo.CONTROLO -> api.updateControlo(idOrdem, idChecklist, req)
        }
        if (!res.isSuccessful) throw Exception(errorMsg(res, "Erro ao atualizar Checklist"))
    }


    // ... outros métodos ...

    // ✅ NOVO: Função para marcar mota como concluída (Estado 2)
    suspend fun concluirMota(idMota: Int): Result<Unit> {
        return try {
            // Estado 2 = FINALIZADA/CONCLUÍDA (Ajusta se a tua regra for outra)
            val req = UpdateEstadoRequest(estado = 2)
            val res = api.updateEstadoMota(idMota, req)

            if (res.isSuccessful) Result.success(Unit)
            else Result.failure(Exception("Erro ${res.code()}: Não foi possível finalizar a mota."))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}