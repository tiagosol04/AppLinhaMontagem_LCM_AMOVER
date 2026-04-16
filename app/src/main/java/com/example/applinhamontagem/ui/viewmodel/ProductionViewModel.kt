package com.example.applinhamontagem.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.applinhamontagem.data.remote.dto.*
import com.example.applinhamontagem.data.repository.FactoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class QcState { PENDENTE, PASSOU, FALHOU, CORRIGIDO }
enum class MotaStatus { PENDENTE, EM_TESTE, CONCLUIDA }

/** Passo atual do fluxo de producao (stepper global) */
enum class ProductionStep(val label: String, val index: Int) {
    MONTAGEM("Montagem", 0),
    POS_MONTAGEM("Verificacao", 1),
    CONTROLO("Qualidade", 2),
    EMBALAGEM("Embalagem", 3)
}

data class PecaUiItem(val defModelo: ModeloPecaSnDto, val montado: MotaPecaSnDto?) {
    val isConcluido: Boolean get() = montado != null
}

data class ProductionUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val minhasAtribuidas: List<MotaAtribuidaDto> = emptyList(),
    val currentMota: MotaDto? = null,
    val currentOrdem: OrdemProducaoDto? = null,
    val listaPecasCombinada: List<PecaUiItem> = emptyList(),
    val checklists: ChecklistsStatusDto? = null,
    val qcOverrides: Map<Int, QcState> = emptyMap(),
    val currentStep: ProductionStep = ProductionStep.MONTAGEM,
    val ordemIniciada: Boolean = false,
    val vinRegistado: Boolean = false
) {
    val isAssemblyComplete get() = listaPecasCombinada.isNotEmpty() && listaPecasCombinada.all { it.isConcluido }
    val isPostAssemblyComplete get() = checklists?.montagem?.all { (it.verificado ?: 0) == 1 } ?: false

    val isFinalControlComplete: Boolean get() {
        val items = checklists?.controlo.orEmpty()
        if (items.isEmpty()) return false
        return items.all { item ->
            val st = qcOverrides[item.idChecklist]
            (st == QcState.PASSOU || st == QcState.CORRIGIDO) || (st == null && (item.controloFinal ?: 0) == 1)
        }
    }

    val isPackagingComplete: Boolean get() =
        checklists?.embalagem?.isNotEmpty() == true && checklists.embalagem.all { (it.incluido ?: 0) == 1 }

    val pecasProgresso: String get() {
        val total = listaPecasCombinada.size
        val done = listaPecasCombinada.count { it.isConcluido }
        return "$done/$total"
    }
}

class ProductionViewModel(private val repository: FactoryRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(ProductionUiState())
    val uiState = _uiState.asStateFlow()

    // ── Dashboard ──
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
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            repository.getMotasAtribuidas(idInt)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        minhasAtribuidas = it.sortedBy { m -> m.estadoMota },
                        isLoading = false
                    )
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(errorMessage = it.message, isLoading = false)
                }
        }
    }

    fun selectFromDashboard(atrib: MotaAtribuidaDto) {
        viewModelScope.launch {
            resetWorkflowOnly()
            _uiState.value = _uiState.value.copy(isLoading = true)
            repository.getMotaById(atrib.motaId)
                .onSuccess { selectMota(it) }
                .onFailure {
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = it.message)
                }
        }
    }

    fun loadMotaByVin(vin: String) {
        viewModelScope.launch {
            resetWorkflowOnly()
            _uiState.value = _uiState.value.copy(isLoading = true)
            repository.buscarMotaPorVin(vin)
                .onSuccess { selectMota(it) }
                .onFailure {
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = "VIN nao encontrado.")
                }
        }
    }

    fun selectMota(mota: MotaDto) {
        val vinOk = !mota.numeroIdentificacao.isNullOrBlank()
        _uiState.value = _uiState.value.copy(
            currentMota = mota,
            vinRegistado = vinOk,
            currentStep = ProductionStep.MONTAGEM
        )
        carregarDadosProducao(mota.idMota, mota.idModelo, mota.idOrdemProducao)
    }

    // ── Iniciar Ordem (chama POST /ordens/{id}/iniciar na API) ──
    fun iniciarOrdem(ordemId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            repository.iniciarOrdem(ordemId)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(ordemIniciada = true, isLoading = false, successMessage = "Ordem iniciada. Checklists criados.")
                    // Recarregar dados (agora tem checklists)
                    val mota = _uiState.value.currentMota
                    if (mota != null) {
                        carregarDadosProducao(mota.idMota, mota.idModelo, mota.idOrdemProducao)
                    }
                }
                .onFailure {
                    // Se ja esta iniciada, nao e erro critico
                    val msg = it.message ?: ""
                    val jaIniciada = msg.contains("em produc", ignoreCase = true) || msg.contains("already", ignoreCase = true)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        ordemIniciada = jaIniciada,
                        errorMessage = if (jaIniciada) null else "Erro ao iniciar: ${it.message}"
                    )
                }
        }
    }

    // ── Registar VIN / Numero de Quadro ──
    fun registarVin(vin: String, onSuccess: () -> Unit = {}) {
        val mota = _uiState.value.currentMota ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            repository.updateVin(mota.idMota, vin)
                .onSuccess {
                    // Recarregar mota com VIN atualizado
                    repository.getMotaById(mota.idMota).onSuccess { motaAtualizada ->
                        _uiState.value = _uiState.value.copy(
                            currentMota = motaAtualizada,
                            vinRegistado = true,
                            isLoading = false,
                            successMessage = "Quadro $vin registado."
                        )
                        onSuccess()
                    }
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = "Erro VIN: ${it.message}")
                }
        }
    }

    // ── Producao (Pecas) ──
    private fun carregarDadosProducao(motaId: Int, modeloId: Int, ordemId: Int) {
        viewModelScope.launch {
            val pecasDef = repository.getDefinicaoPecas(modeloId).getOrDefault(emptyList())
            val pecasMontadas = repository.getPecasJaMontadas(motaId).getOrDefault(emptyList())
            var chk = repository.getChecklists(ordemId).getOrNull()

            // Verificar se a ordem ja foi iniciada (tem checklists)
            val temChecklists = (chk?.montagem?.isNotEmpty() == true) ||
                    (chk?.embalagem?.isNotEmpty() == true) ||
                    (chk?.controlo?.isNotEmpty() == true)

            // AUTO-INICIAR: se não tem checklists, iniciar a ordem automaticamente
            if (!temChecklists) {
                repository.iniciarOrdem(ordemId)
                    .onSuccess {
                        // Recarregar checklists agora que foram criados
                        chk = repository.getChecklists(ordemId).getOrNull()
                    }
                    .onFailure {
                        // Se falhou porque já estava iniciada, ignorar
                        val msg = it.message ?: ""
                        if (!msg.contains("em produc", ignoreCase = true) && !msg.contains("already", ignoreCase = true)) {
                            _uiState.value = _uiState.value.copy(errorMessage = "Nota: ${it.message}")
                        }
                        // Tentar recarregar checklists de qualquer forma
                        chk = repository.getChecklists(ordemId).getOrNull()
                    }
            }

            val temChecklistsAgora = (chk?.montagem?.isNotEmpty() == true) ||
                    (chk?.embalagem?.isNotEmpty() == true) ||
                    (chk?.controlo?.isNotEmpty() == true)

            val lista = pecasDef.map { def -> PecaUiItem(def, pecasMontadas.find { it.idPeca == def.idPeca }) }
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                listaPecasCombinada = lista,
                checklists = chk,
                ordemIniciada = temChecklistsAgora
            )
        }
    }

    /** Registar peca com SN - simulado ou real */
    fun registarPeca(idPeca: Int, sn: String, onResult: (Boolean) -> Unit = {}) {
        val m = _uiState.value.currentMota ?: return
        val snFinal = sn.ifBlank { gerarSnSimulado() }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(errorMessage = null)
            repository.registarMontagem(m.idMota, idPeca, snFinal)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(successMessage = "Peca registada: $snFinal")
                    carregarDadosProducao(m.idMota, m.idModelo, m.idOrdemProducao)
                    onResult(true)
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(errorMessage = "Erro ao registar: ${it.message}")
                    onResult(false)
                }
        }
    }

    /** Gera SN simulado com formato realista (para quando nao ha scanner) */
    private fun gerarSnSimulado(): String {
        val ts = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(Date())
        val rand = (1000..9999).random()
        return "SIM-$ts-$rand"
    }

    // ── Checklists ──
    fun toggleChecklist(idChecklist: Int, tipoStr: String, valor: Boolean) {
        val m = _uiState.value.currentMota ?: return
        val tipoEnum = when (tipoStr.lowercase()) {
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

    // ── QC (Qualidade) ──
    fun qcAprovar(idChecklist: Int) {
        val overrides = _uiState.value.qcOverrides.toMutableMap()
        val estadoAtual = overrides[idChecklist]
        overrides[idChecklist] = if (estadoAtual == QcState.FALHOU) QcState.CORRIGIDO else QcState.PASSOU
        _uiState.value = _uiState.value.copy(qcOverrides = overrides)
        toggleChecklist(idChecklist, "controlo", true)
    }

    fun qcReprovar(idChecklist: Int) {
        val overrides = _uiState.value.qcOverrides.toMutableMap()
        overrides[idChecklist] = QcState.FALHOU
        _uiState.value = _uiState.value.copy(qcOverrides = overrides)
        toggleChecklist(idChecklist, "controlo", false)
    }

    // ── Stepper ──
    fun setStep(step: ProductionStep) {
        _uiState.value = _uiState.value.copy(currentStep = step)
    }

    // ── Finalizar Ordem (chama POST /ordens/{id}/finalizar na API) ──
    fun finalizarProducao(userIdRaw: String, onSuccess: () -> Unit) {
        val mota = _uiState.value.currentMota ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            // Tentar finalizar via endpoint dedicado da API
            repository.finalizarOrdem(mota.idOrdemProducao)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Producao finalizada com sucesso!"
                    )
                    resetWorkflowOnly()
                    // Recarregar motas do operador
                    val idInt = userIdRaw.toIntOrNull() ?: 1
                    repository.getMotasAtribuidas(idInt).onSuccess { lista ->
                        _uiState.value = _uiState.value.copy(
                            minhasAtribuidas = lista.sortedBy { m -> m.estadoMota }
                        )
                    }
                    onSuccess()
                }
                .onFailure { ex ->
                    // Se o finalizar falhou, mostrar o motivo (pecas em falta, checklists, etc)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Nao foi possivel finalizar: ${ex.message}"
                    )
                }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(errorMessage = null, successMessage = null)
    }

    fun resetWorkflowOnly() {
        _uiState.value = _uiState.value.copy(
            currentMota = null, currentOrdem = null, checklists = null,
            listaPecasCombinada = emptyList(), qcOverrides = emptyMap(),
            errorMessage = null, successMessage = null,
            currentStep = ProductionStep.MONTAGEM, ordemIniciada = false, vinRegistado = false
        )
    }
}
