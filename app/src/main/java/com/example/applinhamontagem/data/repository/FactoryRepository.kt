package com.example.applinhamontagem.data.repository

import com.example.applinhamontagem.data.remote.RetrofitClient
import com.example.applinhamontagem.data.remote.dto.*
import retrofit2.Response

class FactoryRepository {

    private val api = RetrofitClient.api

    enum class ChecklistTipo { MONTAGEM, EMBALAGEM, CONTROLO }

    private fun <T> unwrap(resp: Response<T>, ctx: String): T {
        if (resp.isSuccessful && resp.body() != null) return resp.body()!!
        val errorBody = resp.errorBody()?.string()?.take(200) ?: ""
        throw Exception("Erro ${resp.code()} em $ctx: $errorBody")
    }

    // --- ORDENS ---
    suspend fun getOrdens(estado: Int? = null): Result<List<OrdemProducaoDto>> = runCatching {
        unwrap(api.getOrdens(estado), "getOrdens")
    }

    suspend fun getOrdem(id: Int): Result<OrdemProducaoDto> = runCatching {
        unwrap(api.getOrdem(id), "getOrdem")
    }

    suspend fun getOrdemResumo(id: Int): Result<OrdemResumoDto> = runCatching {
        unwrap(api.getOrdemResumo(id), "getOrdemResumo")
    }

    suspend fun iniciarOrdem(id: Int): Result<IniciarOrdemResponse> = runCatching {
        unwrap(api.iniciarOrdem(id), "iniciarOrdem")
    }

    suspend fun finalizarOrdem(id: Int): Result<FinalizarOrdemResponse> = runCatching {
        unwrap(api.finalizarOrdem(id), "finalizarOrdem")
    }

    // --- ENCOMENDAS ---
    suspend fun getEncomendas(): Result<List<EncomendaDto>> = runCatching {
        unwrap(api.getEncomendas(), "getEncomendas")
    }

    // --- MOTAS ---
    suspend fun getMotasDaOrdem(idOrdem: Int): Result<List<MotaDto>> = runCatching {
        unwrap(api.getMotasDaOrdem(idOrdem), "getMotasDaOrdem")
    }

    suspend fun criarMotaNaOrdem(idOrdem: Int, req: CriarMotaRequest): Result<CriarMotaResponse> = runCatching {
        unwrap(api.criarMotaNaOrdem(idOrdem, req), "criarMotaNaOrdem")
    }

    suspend fun getMotasAtribuidas(idUtilizador: Int): Result<List<MotaAtribuidaDto>> = runCatching {
        unwrap(api.getMotasDoUtilizadorById(idUtilizador), "getMotasAtribuidas")
    }

    suspend fun getMotaById(idMota: Int): Result<MotaDto> = runCatching {
        unwrap(api.getMota(idMota), "getMota")
    }

    suspend fun buscarMotaPorVin(vin: String): Result<MotaDto> = runCatching {
        unwrap(api.getMotaByVin(vin), "getMotaByVin")
    }

    suspend fun updateVin(idMota: Int, vin: String): Result<UpdateVinResponse> = runCatching {
        unwrap(api.updateVin(idMota, UpdateVinRequest(vin)), "updateVin")
    }

    suspend fun concluirMota(idMota: Int): Result<Unit> = runCatching {
        val res = api.updateEstadoMota(idMota, UpdateEstadoRequest(estado = 2))
        if (!res.isSuccessful) throw Exception("Erro ${res.code()}: Nao foi possivel concluir a mota.")
    }

    // --- PECAS ---
    suspend fun getDefinicaoPecas(idModelo: Int): Result<List<ModeloPecaSnDto>> = runCatching {
        unwrap(api.getPecasNecessarias(idModelo), "getPecasNecessarias")
    }

    suspend fun getPecasJaMontadas(idMota: Int): Result<List<MotaPecaSnDto>> = runCatching {
        unwrap(api.getPecasMontadas(idMota), "getPecasMontadas")
    }

    suspend fun registarMontagem(idMota: Int, idPeca: Int, sn: String): Result<AddPecaSnResponse> = runCatching {
        unwrap(api.registarPeca(idMota, AddPecaSnRequest(idPeca, sn)), "registarPeca")
    }

    // --- CHECKLISTS ---
    suspend fun getChecklists(idOrdem: Int): Result<ChecklistsStatusDto> = runCatching {
        unwrap(api.getChecklistsDaOrdem(idOrdem), "getChecklists")
    }

    suspend fun updateChecklist(idOrdem: Int, idChecklist: Int, tipo: ChecklistTipo, valor: Boolean): Result<Unit> = runCatching {
        val req = UpdateFlagRequest(if (valor) 1 else 0)
        val res = when (tipo) {
            ChecklistTipo.MONTAGEM -> api.updateMontagem(idOrdem, idChecklist, req)
            ChecklistTipo.EMBALAGEM -> api.updateEmbalagem(idOrdem, idChecklist, req)
            ChecklistTipo.CONTROLO -> api.updateControlo(idOrdem, idChecklist, req)
        }
        if (!res.isSuccessful) throw Exception("Erro ${res.code()}: Falha ao atualizar checklist.")
    }
}
