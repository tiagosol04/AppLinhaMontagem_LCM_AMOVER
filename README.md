
```
    ╔═══════════════════════════════════════════════════════╗
║                                                       ║
║        █████╗       ███╗   ███╗  ██████╗ ██╗   ██╗   ║
║       ██╔══██╗      ████╗ ████║ ██╔═══██╗██║   ██║   ║
║       ███████║█████╗██╔████╔██║ ██║   ██║██║   ██║   ║
║       ██╔══██║╚════╝██║╚██╔╝██║ ██║   ██║╚██╗ ██╔╝   ║
║       ██║  ██║      ██║ ╚═╝ ██║ ╚██████╔╝ ╚████╔╝    ║
║       ╚═╝  ╚═╝      ╚═╝     ╚═╝  ╚═════╝   ╚═══╝     ║
║                                                       ║
║        G E S T Ã O   D E   F Á B R I C A              ║
║                     A - M O V E R                     ║
║                                                       ║
╚═══════════════════════════════════════════════════════╝
```

### Aplicação de Chão de Fábrica — AJP Motorcycles

*Controlo de produção em tempo real, do quadro à expedição.*

[![Android](https://img.shields.io/badge/Android-Kotlin-3DDC84?style=for-the-badge&logo=android&logoColor=white)](#)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack_Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)](#)
[![Retrofit](https://img.shields.io/badge/Retrofit-FF6B6B?style=for-the-badge)](#)
[![API REST](https://img.shields.io/badge/API_REST-005571?style=for-the-badge)](#)

---

</div>

## 🏭 O que é

A **Linha de Montagem A-MoVeR** é uma aplicação Android nativa pensada para ser usada **no chão de fábrica**, em **tablet** ou **dispositivo dedicado**, por operadores de linha que montam motociclos na AJP Motorcycles.

Não é uma app de escritório. É uma ferramenta de trabalho industrial — com botões grandes, feedback tátil, e um fluxo sequencial que guia o operador desde a identificação do quadro até à embalagem final.

```
  ┌─────────┐     ┌──────────┐     ┌───────────┐     ┌────────┐     ┌───────────┐
  │  QUADRO │────▶│ MONTAGEM │────▶│VERIFICAÇÃO│────▶│   QC   │────▶│EMBALAGEM  │
  │  / VIN  │     │  PEÇAS   │     │   PÓS     │     │ FINAL  │     │+ FINALIZAR│
  └─────────┘     └──────────┘     └───────────┘     └────────┘     └───────────┘
       ▲                                                                   │
       │                    ← FEEDBACK CONTÍNUO →                          │
       └───────────────────────────────────────────────────────────────────┘
```

---

## ⚡ Funcionalidades

### Pipeline de Produção Completo

| Etapa | O que faz | Endpoint API |
|-------|-----------|-------------|
| **Registo de Quadro** | Operador lê/digita o VIN antes de iniciar montagem | `PUT /motas/{id}/identificacao` |
| **Montagem de Peças** | Scan (simulado ou real) de cada peça serializada | `POST /motas/{id}/pecas-sn` |
| **Verificação Pós-Montagem** | Checklist de montagem — todos os pontos validados | `PUT /ordens/{id}/checklists/montagem/{checkId}` |
| **Controlo de Qualidade** | Aprovar / Reprovar / Corrigir cada item de controlo | `PUT /ordens/{id}/checklists/controlo/{checkId}` |
| **Embalagem + Finalização** | Checklist de embalagem + `POST /finalizar` via API | `POST /ordens/{id}/finalizar` |

### Auto-Inicialização da Ordem

Quando o operador entra numa mota cuja ordem ainda não foi iniciada, a app chama automaticamente `POST /ordens/{id}/iniciar`, que cria todos os checklists no servidor. **Zero configuração manual.**

### Feedback Industrial

- **Vibração tátil** em cada ação (sucesso, erro, tick) — essencial em ambiente ruidoso
- **Stepper visual** permanente — o operador sabe sempre em que etapa está
- **Botões 56-64dp** — usáveis com luvas de trabalho
- **Cores de estado claras** — verde (OK), vermelho (falha), amarelo (pendente), azul (corrigido)

### Controlo de Qualidade Inteligente

```
  ┌─────────┐     APROVAR     ┌─────────┐
  │PENDENTE │───────────────▶│ PASSOU  │
  └─────────┘                 └─────────┘
       │                           ▲
       │ REPROVAR            CORRIGIR
       ▼                           │
  ┌─────────┐                 ┌─────────┐
  │ FALHOU  │───────────────▶│CORRIGIDO│
  └─────────┘                 └─────────┘
```

O operador pode reprovar um ponto, corrigi-lo, e só avança quando tudo está resolvido. O histórico fica gravado no servidor.

---

## 🏗 Arquitetura

```
┌─────────────────────────────────────────────────────────┐
│                    UI LAYER (Compose)                     │
│  LoginScreen → Dashboard → RegisterVin → Assembly →      │
│  PostAssembly → FinalControl → Packaging                 │
├─────────────────────────────────────────────────────────┤
│                  VIEWMODEL LAYER                         │
│  AuthViewModel · ProductionViewModel · OrdersViewModel   │
├─────────────────────────────────────────────────────────┤
│                 REPOSITORY LAYER                         │
│  FactoryRepository · AuthRepository                      │
├─────────────────────────────────────────────────────────┤
│                  NETWORK LAYER                           │
│  Retrofit + OkHttp + JWT Interceptor                     │
├─────────────────────────────────────────────────────────┤
│                  API A-MoVeR (Backend)                   │
│  ASP.NET Core · SQL Server · JWT Auth                    │
└─────────────────────────────────────────────────────────┘
```

| Camada | Tecnologia | Responsabilidade |
|--------|-----------|------------------|
| **UI** | Jetpack Compose + Material 3 | Ecrãs, navegação, feedback visual |
| **State** | ViewModel + StateFlow | Estado reativo, lógica de negócio |
| **Data** | Retrofit 2 + Gson | Comunicação REST com a API |
| **Auth** | JWT + OkHttp Interceptor | Token automático em cada pedido |
| **Feedback** | HapticHelper (Vibrator API) | Vibração contextual |
| **Persistência** | DataStore Preferences | Sessão sobrevive a reinícios |

---

## 📁 Estrutura do Projeto

```
app/src/main/java/com/example/applinhamontagem/
│
├── data/
│   ├── remote/
│   │   ├── AssemblyApiService.kt      ← Interface Retrofit (todos os endpoints)
│   │   ├── RetrofitClient.kt          ← Singleton HTTP + JWT
│   │   └── dto/
│   │       └── AllDtos.kt             ← Todos os DTOs (request/response)
│   ├── repository/
│   │   ├── AuthRepository.kt          ← Login / Logout
│   │   └── FactoryRepository.kt       ← Toda a lógica de dados
│   └── utils/
│       ├── Constants.kt               ← URL da API
│       ├── HapticHelper.kt            ← Vibração (success/error/tick)
│       └── SessionManager.kt          ← Persistência de token
│
├── ui/
│   ├── components/
│   │   ├── DynamicCheckItem.kt        ← Checkbox industrial
│   │   ├── DynamicPartItem.kt         ← Card de peça
│   │   └── StepperIndicator.kt        ← Indicador de progresso (4 etapas)
│   ├── navigation/
│   │   ├── Screen.kt                  ← Rotas (8 ecrãs)
│   │   └── AppNavigation.kt           ← NavHost
│   ├── view/
│   │   ├── LoginScreen.kt             ← Login do operador
│   │   ├── DashboardScreen.kt         ← Motas atribuídas
│   │   ├── RegisterVinScreen.kt       ← Registo de quadro/VIN
│   │   ├── DynamicAssemblyScreen.kt   ← Montagem de peças (scan)
│   │   ├── PostAssemblyScreen.kt      ← Verificação pós-montagem
│   │   ├── FinalControlScreen.kt      ← Controlo de qualidade
│   │   ├── PackagingScreen.kt         ← Embalagem + finalizar
│   │   ├── OrdersListScreen.kt        ← Lista de ordens
│   │   └── OrderDetailScreen.kt       ← Detalhe de ordem
│   ├── viewmodel/
│   │   ├── AuthViewModel.kt           ← Estado de autenticação
│   │   ├── ProductionViewModel.kt     ← Toda a lógica de produção
│   │   ├── OrdersViewModel.kt         ← Ordens de produção
│   │   └── ViewModelFactory.kt        ← DI manual
│   └── theme/
│       ├── Color.kt                   ← Paleta industrial
│       ├── Theme.kt                   ← Material 3 theme
│       └── Type.kt                    ← Tipografia
```

---

## 🔌 Endpoints da API Utilizados

```
AUTH
  POST /api/Auth/login                          → Login com JWT

ORDENS
  GET  /api/ordens                              → Lista de ordens
  GET  /api/ordens/{id}                         → Detalhe da ordem
  GET  /api/ordens/{id}/resumo                  → Resumo (checklists + peças)
  POST /api/ordens/{id}/iniciar                 → Iniciar produção (cria checklists)
  POST /api/ordens/{id}/finalizar               → Finalizar (valida tudo server-side)
  GET  /api/ordens/{id}/motas                   → Motas da ordem
  POST /api/ordens/{id}/motas                   → Criar mota na ordem

MOTAS
  GET  /api/motas/{id}                          → Detalhe da mota
  GET  /api/motas/by-vin/{vin}                  → Buscar por VIN
  PUT  /api/motas/{id}/identificacao            → Registar/atualizar VIN
  PUT  /api/motas/{id}/estado                   → Alterar estado

PEÇAS
  GET  /api/modelos/{id}/pecas-sn               → Peças obrigatórias do modelo
  GET  /api/motas/{id}/pecas-sn                 → Peças já montadas
  POST /api/motas/{id}/pecas-sn                 → Registar peça serializada

CHECKLISTS
  GET  /api/ordens/{id}/checklists              → Estado dos checklists
  PUT  /api/ordens/{id}/checklists/montagem/{x} → Toggle montagem
  PUT  /api/ordens/{id}/checklists/controlo/{x} → Toggle controlo
  PUT  /api/ordens/{id}/checklists/embalagem/{x}→ Toggle embalagem
```

---

## 🚀 Setup

### Pré-requisitos

- Android Studio Hedgehog+ (2024.x)
- Kotlin 1.9+
- API A-MoVeR a correr localmente (porta 5137)

### Configuração

1. **Clonar o repositório**
2. **Ajustar a URL da API** em `Constants.kt`:
   ```kotlin
   // Emulador Android Studio
   const val BASE_URL = "http://10.0.2.2:5137/"
   
   // Dispositivo físico na mesma rede
   // const val BASE_URL = "http://192.168.1.XXX:5137/"
   ```
3. **Build & Run**

### Testar

Para encontrar um VIN válido para teste, corre esta query na base de dados:

```sql
SELECT m.NumeroIdentificacao, m.IDMota, op.NumeroOrdem
FROM Motas m
INNER JOIN OrdemProducao op ON op.IDOrdemProducao = m.IDOrdemProducao
WHERE m.NumeroIdentificacao IS NOT NULL AND m.NumeroIdentificacao != ''
  AND EXISTS (SELECT 1 FROM ChecklistMontagem cm WHERE cm.IDOrdemProducao = m.IDOrdemProducao)
ORDER BY m.IDMota DESC;
```

---

## 🔮 Roadmap

- [ ] Integração com scanner de código de barras físico (Bluetooth/USB)
- [ ] Modo offline com sincronização quando a rede volta
- [ ] Fotografia de defeitos no controlo de qualidade
- [ ] Impressão de etiqueta de expedição via impressora térmica
- [ ] Dashboard de turno com métricas de produtividade
- [ ] Suporte multi-idioma (PT/EN/FR)

---

<div align="center">

```
  Construído para quem constrói motas.
  
  AJP Motorcycles × Projeto A-MoVeR
  Penafiel, Portugal · 2025
```

</div>
