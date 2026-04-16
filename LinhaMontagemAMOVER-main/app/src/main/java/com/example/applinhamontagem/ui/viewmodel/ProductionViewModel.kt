package com.example.applinhamontagem.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.applinhamontagem.data.remote.dto.*
import com.example.applinhamontagem.data.repository.FactoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class QcState { PENDENTE, PASSOU, FALHOU, CORRIGIDO }
enum class MotaStatus { PENDENTE, EM_TESTE, CONCLUIDA }

data class PecaUiItem(val defModelo: ModeloPecaSnDto, val montado: MotaPecaSnDto?) {
    val isConcluido: Boolean get() = montado != null
}

data class ProductionUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val minhasAtribuidas: List<MotaAtribuidaDto> = emptyList(),
    val currentMota: MotaDto? = null,
    val listaPecasCombinada: List<PecaUiItem> = emptyList(),
    val checklists: ChecklistsStatusDto? = null,
    val qcOverrides: Map<Int, QcState> = emptyMap()
) {
    val isAssemblyComplete get() = listaPecasCombinada.isNotEmpty() && listaPecasCombinada.all { it.isConcluido }
    val isPostAssemblyComplete get() = checklists?.montagem?.all { it.verificado == 1 } ?: false

    val isFinalControlComplete: Boolean get() {
        val items = checklists?.controlo.orEmpty()
        if (items.isEmpty()) return false
        return items.all { item ->
            val st = qcOverrides[item.idChecklist]
            // Só avança se for PASSOU ou CORRIGIDO. Se for FALHOU ou PENDENTE, bloqueia.
            (st == QcState.PASSOU || st == QcState.CORRIGIDO) || (st == null && item.controloFinal == 1)
        }
    }

    val isPackagingComplete: Boolean get() =
        checklists?.embalagem?.isNotEmpty() == true && checklists.embalagem.all { it.incluido == 1 }
}

class ProductionViewModel(private val repository: FactoryRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(ProductionUiState())
    val uiState = _uiState.asStateFlow()

    // --- DASHBOARD ---
    fun getStatusMota(mota: MotaAtribuidaDto): MotaStatus {
        val estado = mota.estadoMota ?: 0
        return when {
            estado >= 2 -> MotaStatus.CONCLUIDA
            estado == 1 -> MotaStatus.EM_TESTE
            else -> MotaStatus.PENDENTE
        }
    }

    fun loadMinhasMotas(userIdRaw: String) {
        val idInt = userIdRaw.toIntOrNull() ?: 1
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            repository.getMotasAtribuidas(idInt)
                .onSuccess {
                    // Ordena: Pendentes primeiro, Concluídas no fim
                    val ordenada = it.sortedBy { m -> m.estadoMota }
                    _uiState.value = _uiState.value.copy(minhasAtribuidas = ordenada, isLoading = false)
                }
                .onFailure { _uiState.value = _uiState.value.copy(errorMessage = "Erro API: ${it.message}", isLoading = false) }
        }
    }

    fun selectFromDashboard(atrib: MotaAtribuidaDto) {
        viewModelScope.launch {
            resetWorkflowOnly()
            repository.getMotaById(atrib.motaId).onSuccess { selectMota(it) }
        }
    }

    fun loadMotaByVin(vin: String) {
        viewModelScope.launch {
            resetWorkflowOnly()
            _uiState.value = _uiState.value.copy(isLoading = true)
            repository.buscarMotaPorVin(vin)
                .onSuccess { selectMota(it) }
                .onFailure { _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = "VIN não encontrado.") }
        }
    }

    fun selectMota(mota: MotaDto) {
        _uiState.value = _uiState.value.copy(currentMota = mota)
        carregarDadosProducao(mota.idMota, mota.idModelo, mota.idOrdemProducao)
    }

    private fun carregarDadosProducao(motaId: Int, modeloId: Int, ordemId: Int) {
        viewModelScope.launch {
            val pecasDef = repository.getDefinicaoPecas(modeloId).getOrDefault(emptyList())
            val pecasMontadas = repository.getPecasJaMontadas(motaId).getOrDefault(emptyList())
            val chk = repository.getChecklists(ordemId).getOrNull()

            val lista = pecasDef.map { def -> PecaUiItem(def, pecasMontadas.find { it.idPeca == def.idPeca }) }
            _uiState.value = _uiState.value.copy(isLoading = false, listaPecasCombinada = lista, checklists = chk)
        }
    }

    fun registarPeca(idPeca: Int, sn: String) {
        val m = _uiState.value.currentMota ?: return
        viewModelScope.launch {
            repository.registarMontagem(m.idMota, idPeca, sn)
                .onSuccess { carregarDadosProducao(m.idMota, m.idModelo, m.idOrdemProducao) }
        }
    }

    fun toggleChecklist(idChecklist: Int, tipoStr: String, valor: Boolean) {
        val m = _uiState.value.currentMota ?: return
        val tipoEnum = when(tipoStr.lowercase()) {
            "montagem" -> FactoryRepository.ChecklistTipo.MONTAGEM
            "embalagem" -> FactoryRepository.ChecklistTipo.EMBALAGEM
            else -> FactoryRepository.ChecklistTipo.CONTROLO
        }
        viewModelScope.launch {
            repository.updateChecklist(m.idOrdemProducao, idChecklist, tipoEnum, valor)
                .onSuccess {
                    val chk = repository.getChecklists(m.idOrdemProducao).getOrNull()
                    _uiState.value = _uiState.value.copy(checklists = chk)
                }
        }
    }

    // --- LÓGICA DE QUALIDADE (QC) MELHORADA ---
    fun qcAprovar(idChecklist: Int) {
        val overrides = _uiState.value.qcOverrides.toMutableMap()

        // Se estava FALHOU, agora passa a CORRIGIDO. Se estava PENDENTE, passa a PASSOU.
        val estadoAtual = overrides[idChecklist]
        val novoEstado = if (estadoAtual == QcState.FALHOU) QcState.CORRIGIDO else QcState.PASSOU

        overrides[idChecklist] = novoEstado
        _uiState.value = _uiState.value.copy(qcOverrides = overrides)

        // Grava no servidor como OK (1)
        toggleChecklist(idChecklist, "controlo", true)
    }

    fun qcReprovar(idChecklist: Int) {
        val overrides = _uiState.value.qcOverrides.toMutableMap()
        overrides[idChecklist] = QcState.FALHOU
        _uiState.value = _uiState.value.copy(qcOverrides = overrides)

        // Grava no servidor como NOK (0)
        toggleChecklist(idChecklist, "controlo", false)
    }

    fun finalizarMotaEVoltar(userIdRaw: String, onSuccess: () -> Unit) {
        val mota = _uiState.value.currentMota ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            repository.concluirMota(mota.idMota)
                .onSuccess {
                    resetWorkflowOnly()
                    val idInt = userIdRaw.toIntOrNull() ?: 1
                    repository.getMotasAtribuidas(idInt).onSuccess {
                        _uiState.value = _uiState.value.copy(minhasAtribuidas = it.sortedBy { m -> m.estadoMota }, isLoading = false)
                        onSuccess()
                    }
                }
                .onFailure { _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = "Erro: ${it.message}") }
        }
    }

    fun resetWorkflowOnly() {
        _uiState.value = _uiState.value.copy(
            currentMota = null, checklists = null, listaPecasCombinada = emptyList(),
            qcOverrides = emptyMap(), errorMessage = null
        )
    }
}