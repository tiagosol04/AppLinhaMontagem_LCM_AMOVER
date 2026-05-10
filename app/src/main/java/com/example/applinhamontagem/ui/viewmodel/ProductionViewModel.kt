package com.example.applinhamontagem.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.applinhamontagem.data.remote.dto.*
import com.example.applinhamontagem.data.repository.FactoryRepository
import com.example.applinhamontagem.data.utils.ScannerInputHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class QcState { PENDENTE, PASSOU, FALHOU, CORRIGIDO }

/**
 * Estado visual de uma mota no dashboard.
 * Mapeamento direto dos estados reais da API:
 *   0 = Em Produção
 *   1 = Ativa
 *   2 = Em Manutenção
 *   3 = Descontinuada
 */
enum class MotaUiStatus(val label: String) {
    EM_PRODUCAO("Em Produção"),
    ATIVA("Ativa"),
    EM_MANUTENCAO("Em Manutenção"),
    DESCONTINUADA("Descontinuada"),
    DESCONHECIDO("Desconhecido")
}

enum class ProductionStep(val label: String, val index: Int) {
    MONTAGEM("Montagem", 0),
    POS_MONTAGEM("Verificação", 1),
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
    val isOrdemPorIniciar: Boolean = false,
    val vinRegistado: Boolean = false
) {
    val isAssemblyComplete get() =
        listaPecasCombinada.isNotEmpty() && listaPecasCombinada.all { it.isConcluido }

    val isPostAssemblyComplete get() =
        checklists?.montagem?.all { (it.verificado ?: 0) == 1 } ?: false

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

    /**
     * Mapeia o estado numérico da API para o estado visual correto.
     * Estados API: 0=Em Produção, 1=Ativa, 2=Em Manutenção, 3=Descontinuada.
     */
    fun getStatusMota(mota: MotaAtribuidaDto): MotaUiStatus {
        return when (mota.estadoMota) {
            0 -> MotaUiStatus.EM_PRODUCAO
            1 -> MotaUiStatus.ATIVA
            2 -> MotaUiStatus.EM_MANUTENCAO
            3 -> MotaUiStatus.DESCONTINUADA
            else -> MotaUiStatus.DESCONHECIDO
        }
    }

    fun loadMinhasMotas(userIdRaw: String) {
        val idInt = userIdRaw.toIntOrNull()
        if (idInt == null) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "ID de utilizador inválido. Faça logout e volte a entrar.",
                isLoading = false
            )
            return
        }
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
                    _uiState.value = _uiState.value.copy(
                        errorMessage = ScannerInputHelper.mapApiError(it.message),
                        isLoading = false
                    )
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
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = ScannerInputHelper.mapApiError(it.message)
                    )
                }
        }
    }

    fun loadMotaByVin(vin: String) {
        val vinSanitizado = ScannerInputHelper.sanitize(vin).uppercase()
        if (!ScannerInputHelper.isValidVin(vinSanitizado)) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "VIN inválido. Mínimo 5 caracteres, máximo 30."
            )
            return
        }
        viewModelScope.launch {
            resetWorkflowOnly()
            _uiState.value = _uiState.value.copy(isLoading = true)
            repository.buscarMotaPorVin(vinSanitizado)
                .onSuccess { selectMota(it) }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "VIN não encontrado: ${ScannerInputHelper.mapApiError(it.message)}"
                    )
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

    // ── Iniciar Ordem ──
    fun iniciarOrdem(ordemId: Int) {
        val ordem = _uiState.value.currentOrdem
        if (ordem != null) {
            when (ordem.estado) {
                3 -> {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Ordem bloqueada. Contacte o supervisor."
                    )
                    return
                }
                2 -> {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Esta ordem já está concluída."
                    )
                    return
                }
            }
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            repository.iniciarOrdem(ordemId)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        ordemIniciada = true,
                        isOrdemPorIniciar = false,
                        isLoading = false,
                        successMessage = "Ordem iniciada. Checklists criadas."
                    )
                    val mota = _uiState.value.currentMota
                    if (mota != null) {
                        carregarDadosProducao(mota.idMota, mota.idModelo, mota.idOrdemProducao)
                    }
                }
                .onFailure {
                    val msg = it.message ?: ""
                    val jaIniciada = msg.contains("em produc", ignoreCase = true)
                            || msg.contains("already", ignoreCase = true)
                    if (jaIniciada) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            ordemIniciada = true,
                            isOrdemPorIniciar = false
                        )
                        val mota = _uiState.value.currentMota
                        if (mota != null) {
                            carregarDadosProducao(mota.idMota, mota.idModelo, mota.idOrdemProducao)
                        }
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "Erro ao iniciar ordem: ${ScannerInputHelper.mapApiError(it.message)}"
                        )
                    }
                }
        }
    }

    // ── Registar VIN / Número de Quadro ──
    fun registarVin(vin: String, onSuccess: () -> Unit = {}) {
        val mota = _uiState.value.currentMota ?: return
        val vinSanitizado = ScannerInputHelper.sanitize(vin).uppercase()
        if (!ScannerInputHelper.isValidVin(vinSanitizado)) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "VIN inválido. Mínimo 5 caracteres, máximo 30."
            )
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            repository.updateVin(mota.idMota, vinSanitizado)
                .onSuccess {
                    repository.getMotaById(mota.idMota)
                        .onSuccess { motaAtualizada ->
                            _uiState.value = _uiState.value.copy(
                                currentMota = motaAtualizada,
                                vinRegistado = true,
                                isLoading = false,
                                successMessage = "Quadro $vinSanitizado registado."
                            )
                            onSuccess()
                        }
                        .onFailure {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                errorMessage = ScannerInputHelper.mapApiError(it.message)
                            )
                        }
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = ScannerInputHelper.mapApiError(it.message)
                    )
                }
        }
    }

    // ── Produção (Peças) ──
    private fun carregarDadosProducao(motaId: Int, modeloId: Int, ordemId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val ordem = repository.getOrdem(ordemId).getOrNull()
            val pecasDef = repository.getDefinicaoPecas(modeloId).getOrDefault(emptyList())
            val pecasMontadas = repository.getPecasJaMontadas(motaId).getOrDefault(emptyList())
            val chk = repository.getChecklists(ordemId).getOrNull()

            val temChecklists = (chk?.montagem?.isNotEmpty() == true) ||
                    (chk?.embalagem?.isNotEmpty() == true) ||
                    (chk?.controlo?.isNotEmpty() == true)

            val lista = pecasDef.map { def ->
                PecaUiItem(def, pecasMontadas.find { it.idPeca == def.idPeca })
            }

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                currentOrdem = ordem,
                listaPecasCombinada = lista,
                checklists = chk,
                ordemIniciada = temChecklists,
                isOrdemPorIniciar = !temChecklists
            )
        }
    }

    /**
     * Regista uma peça com número de série.
     * Valida SN antes de enviar à API e verifica duplicado local.
     * Nunca gera SN falso — SN vazio é um erro que deve ser corrigido pelo operador.
     */
    fun registarPeca(idPeca: Int, sn: String, onResult: (Boolean) -> Unit = {}) {
        val m = _uiState.value.currentMota ?: run {
            onResult(false)
            return
        }

        val snSanitizado = ScannerInputHelper.sanitize(sn)

        if (snSanitizado.isBlank()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Número de série obrigatório. Leia ou introduza o S/N da peça."
            )
            onResult(false)
            return
        }

        if (!ScannerInputHelper.isValidSn(snSanitizado)) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Número de série inválido (mín. 3, máx. 60 caracteres)."
            )
            onResult(false)
            return
        }

        // Verificar duplicado local: se a peça já está registada nesta mota
        val jaRegistada = _uiState.value.listaPecasCombinada
            .find { it.defModelo.idPeca == idPeca }?.isConcluido == true
        if (jaRegistada) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Esta peça já foi registada nesta mota."
            )
            onResult(false)
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(errorMessage = null)
            repository.registarMontagem(m.idMota, idPeca, snSanitizado)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        successMessage = "Peça registada: $snSanitizado"
                    )
                    carregarDadosProducao(m.idMota, m.idModelo, m.idOrdemProducao)
                    onResult(true)
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = ScannerInputHelper.mapApiError(it.message)
                    )
                    onResult(false)
                }
        }
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
                .onFailure {
                    // Mostrar erro e recarregar para reverter estado visual
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Erro ao atualizar checklist: ${ScannerInputHelper.mapApiError(it.message)}"
                    )
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

    /**
     * Conclui a etapa de embalagem desta unidade chamando POST /api/ordens/{id}/marcar-embalada.
     * NÃO finaliza a ordem inteira — essa ação é responsabilidade do sistema de gestão
     * apenas quando todas as unidades da ordem estiverem concluídas.
     */
    fun concluirEtapaEmbalagem(userIdRaw: String, onSuccess: () -> Unit) {
        val mota = _uiState.value.currentMota ?: return
        val idInt = userIdRaw.toIntOrNull()
        if (idInt == null) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "ID de utilizador inválido. Faça logout e volte a entrar."
            )
            return
        }
        // Bloqueio de segurança: marcar-embalada requer ordem CONCLUÍDA (estado=2).
        // Não chamar se a ordem ainda estiver Em Produção (estado=1) — resultaria em 409.
        val ordemEstado = _uiState.value.currentOrdem?.estado
        if (ordemEstado != 2) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "A ordem ainda não está concluída (estado: $ordemEstado). A embalagem não pode ser registada via API neste momento."
            )
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            repository.marcarEmbalada(mota.idOrdemProducao)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Unidade marcada como embalada com sucesso."
                    )
                    resetWorkflowOnly()
                    repository.getMotasAtribuidas(idInt).onSuccess { lista ->
                        _uiState.value = _uiState.value.copy(
                            minhasAtribuidas = lista.sortedBy { m -> m.estadoMota }
                        )
                    }
                    onSuccess()
                }
                .onFailure { ex ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Não foi possível concluir a embalagem: ${ScannerInputHelper.mapApiError(ex.message)}"
                    )
                }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(errorMessage = null, successMessage = null)
    }

    fun resetWorkflowOnly() {
        _uiState.value = _uiState.value.copy(
            currentMota = null,
            currentOrdem = null,
            checklists = null,
            listaPecasCombinada = emptyList(),
            qcOverrides = emptyMap(),
            errorMessage = null,
            successMessage = null,
            currentStep = ProductionStep.MONTAGEM,
            ordemIniciada = false,
            isOrdemPorIniciar = false,
            vinRegistado = false
        )
    }
}