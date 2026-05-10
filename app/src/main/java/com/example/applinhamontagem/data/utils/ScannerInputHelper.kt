package com.example.applinhamontagem.data.utils

object ScannerInputHelper {

    /** Remove caracteres indesejados que leitores HID podem introduzir automaticamente. */
    fun sanitize(raw: String): String = raw
        .replace("\r", "")
        .replace("\n", "")
        .replace("\t", "")
        .trim()

    /** Valida um número de série: mínimo 3, máximo 60 caracteres. */
    fun isValidSn(sn: String): Boolean {
        val s = sanitize(sn)
        return s.length in 3..60
    }

    /** Valida um VIN/número de quadro: mínimo 5, máximo 30 caracteres. */
    fun isValidVin(vin: String): Boolean {
        val v = sanitize(vin)
        return v.length in 5..30
    }

    /** Converte erros da API em mensagens legíveis pelo operador em português europeu. */
    fun mapApiError(rawMessage: String?, httpCode: Int? = null): String = when {
        httpCode == 401 ->
            "Sessão expirada. Faça login novamente."
        httpCode == 403 ->
            "Sem permissão para esta operação. Contacte o supervisor."
        httpCode == 409
                || rawMessage?.contains("conflict", ignoreCase = true) == true
                || rawMessage?.contains("duplicado", ignoreCase = true) == true
                || rawMessage?.contains("já existe", ignoreCase = true) == true
                || rawMessage?.contains("already", ignoreCase = true) == true ->
            "Conflito: este dado já foi registado ou foi alterado por outro operador."
        rawMessage?.contains("timeout", ignoreCase = true) == true
                || rawMessage?.contains("SocketTimeoutException", ignoreCase = true) == true
                || rawMessage?.contains("Unable to resolve", ignoreCase = true) == true
                || rawMessage?.contains("failed to connect", ignoreCase = true) == true
                || rawMessage?.contains("ConnectException", ignoreCase = true) == true ->
            "Sem ligação à API. Verifique a rede e tente novamente."
        !rawMessage.isNullOrBlank() -> rawMessage
        else -> "Erro desconhecido. Contacte o supervisor."
    }
}