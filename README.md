# Solidar.io
[![CI Pipeline](https://github.com/Henrique-Barbosaa/donation-app/actions/workflows/ci.yml/badge.svg)](https://github.com/Henrique-Barbosaa/donation-app/actions/workflows/ci.yml)

> Aplicativo multiplataforma de doações desenvolvido para a disciplina **DIM0524 — Desenvolvimento de Sistemas para Dispositivos Móveis** (UFRN / IMD — 2026.2).  
> **Vídeo de Apresentação (Sprint 0):** [Acessar vídeo da Sprint 0 (Google Drive)](https://drive.google.com/file/d/1XYmtlHjPJ0udTKIbNDiesJBjIHMar3O8/view?usp=sharing)  
> **Vídeo de Apresentação (Sprint 1):** [Acessar vídeo da Sprint 1 (Google Drive)](https://drive.google.com/drive/folders/12fEucObhW5YguU4JAUaxxDRwLSnvapnZ)  
> **Vídeo de Apresentação (Sprint 2):** *(Inserir link do vídeo final de 5 minutos)*

## Equipe
* **Nome da Equipe:** Team Solidar.io
* **Disciplina:** DIM0524 — Desenvolvimento de Sistemas para Dispositivos Móveis (2026.2)

### Integrantes
* Antonio Henrique Barbosa Lima — Matrícula: 20230048844
* Joao Batista Da Fonseca Neto — Matrícula: 20220077154
* Moab Fred Dos Santos Varela — Matrícula: 20260072967

---

## Visão geral

Este projeto combina:

- uma aplicação multiplataforma em **Kotlin Multiplatform (KMP)** + **Compose Multiplatform** (alvos Android, Desktop JVM e iOS);
- um backend REST em **Java 21** + **Spring Boot 4** com persistência via **JDBC** (`PostgreSQL` por padrão ou `MySQL` opcional);
- autenticação stateless com **Spring Security** e **JWT**;
- persistência local em disco (`FileLocalCacheStorage` e `PersistentTokenStorage`) permitindo abrir o app sem rede exibindo o último conteúdo salvo.

---

## Funcionalidades implementadas

### Autenticação e Sessão
- **Cadastro diferenciado (`SignUpScreen`):** suporte a contas de **Pessoa Física** (validação de CPF) e **Instituição / ONG** (validação de CNPJ).
- **Login e Sessão Persistente (`LoginScreen`, `PersistentTokenStorage`):** autenticação via JWT com salvamento local em disco; ao reabrir o app (inclusive offline), a sessão é restaurada diretamente na tela principal.
- **Logout limpo:** limpa token em memória/disco e zera os estados dos `ViewModels` para evitar vazamento de dados entre contas.

### Campanhas de Arrecadação (Instituições / ONGs)
- **Criação configurável (`CriarCampanhaScreen`):**
  - Campo de **foto de capa da campanha** (opcional) no mesmo padrão visual das doações;
  - Seletores independentes para ativar **"Pedir objetos / itens físicos"** (com quantidade unitária individual por item) e/ou **"Pedir arrecadação em dinheiro (PIX)"** (meta financeira real informada pela ONG, iniciando sempre em `R$ 0,00`);
  - Cadastro opcional de **locais físicos de coleta** (nome do ponto, logradouro, bairro, número e CEP), enviados ao backend e exibidos na tela de detalhes da campanha;
  - Bloqueio automático caso nenhuma modalidade de arrecadação esteja ativa.
- **Feed, Detalhes e Favoritos (`HomeScreen`, `CampanhaDetalheScreen`):**
  - Navegação adaptativa (`NavigationBar` em telas compactas e `NavigationRail` + grade em telas largas);
  - Barra de progresso financeiro exibida apenas em campanhas que aceitam dinheiro (`aceitaDinheiro`);
  - Favoritos isolados por usuário (`userId`) e suporte a deep link `solidar://campanha/{id}`.
- **Gestão das próprias campanhas (`HistoricoScreen` — aba `Campanhas` e `CampanhaDetalheScreen`):**
  - Listagem com suporte offline (`Minhas campanhas`), além de ações para **finalizar** ou **cancelar** campanhas abertas.

### Doação em Campanhas
- **Fluxo inteligente conforme modalidade (`OpcoesDoacaoScreen`, `DoarItensCampanhaScreen`, `DoarDinheiroScreen`):**
  - Campanhas com itens e dinheiro exibem a tela de escolha entre contribuir em itens ou via PIX;
  - Campanhas exclusivas de itens ou exclusivas de dinheiro direcionam direto para o fluxo correspondente;
  - Contribuições em dinheiro via PIX atualizam o valor `arrecadado_em_centavos` da campanha no backend;
  - Doação de itens controla as quantidades já doadas e vincula a doação à instituição organizadora.

### Publicação, Exploração e Efetivação de Doações
- **Publicação de doação (`CriarDoacaoScreen`):** envio `multipart/form-data` com campo de foto da doação, nome do item, quantidade, categoria, condição (`Novo` ou `Usado`), descrição e motivo opcional.
- **Exploração e Interesse (`ExplorarScreen` e aba `Oferecem doações` na `HomeScreen`):** busca textual, filtro por categoria e ação **"Tenho interesse"** (com envio opcional de mensagem e sincronização com o backend).
- **Histórico e Efetivação (`HistoricoScreen`, `EfetivarDoacaoScreen`):**
  - Abas **Feitas**, **Interessados**, **Recebidas** e **Campanhas** (para instituições);
  - Consulta de interessados por doação, cancelamento de doações pendentes e efetivação de entrega vinculando donatário e local de entrega.

### Modo Offline e Tratamento de Erros
- Cache local em disco por usuário para campanhas, minhas campanhas, doações disponíveis, histórico de doações, favoritos e interesses (`FileLocalCacheStorage`);
- Exibição automática do banner de **Modo offline** quando os dados vêm do cache local (`desatualizado = true`);
- Tratamento exaustivo de estados via `TelaEstado` (`Carregando`, `Vazio`, `Sucesso`, `Erro`) com mensagens distintas para ausência de conexão (`FalhaDominio.SemConexao`), resposta inválida (`FalhaDominio.RespostaInvalida`) e erros de validação (`FalhaDominio.DadosInvalidos`).

---

## Estrutura do projeto

```text
donation-app/
├── .github/workflows/ci.yml   # Pipeline CI (ktlintCheck, detekt, jvmTest, assembleDebug)
├── app/                       # Aplicativo Kotlin Multiplatform
│   ├── androidApp/            # Entrypoint Android
│   ├── desktopApp/            # Entrypoint Desktop (JVM)
│   ├── iosApp/                # Entrypoint iOS (Xcode)
│   ├── shared/                # Código compartilhado (domain, data, di, ui, navigation, commonTest)
│   ├── build.gradle.kts
│   ├── settings.gradle.kts
│   └── gradlew
├── backend/                   # API REST em Spring Boot 4 + JDBC
│   ├── src/
│   ├── pom.xml
│   └── mvnw
├── docs/                      # Proposta, arquitetura e registro de uso de IA
│   ├── proposta.md
│   ├── arquitetura.md
│   └── uso-de-ia.md
└── README.md
```

---

## Tecnologias utilizadas

### Mobile
- Kotlin Multiplatform (Android, Desktop JVM e iOS)
- Compose Multiplatform / Jetpack Compose (Material 3)
- Navigation Compose (rotas tipadas e deep link)
- Ktor Client + `kotlinx.serialization`
- `kotlinx-coroutines-test`, `Turbine` e `compose.ui:ui-test`
- Android Studio e Xcode (para iOS)

### Backend
- Java 21
- Spring Boot 4
- Spring Security + JWT
- Spring JDBC (`PostgreSQL` por padrão ou `MySQL` opcional)
- Maven (com wrapper `mvnw`)

---

## Pré-requisitos

Para rodar o projeto completo em menos de 15 minutos, certifique-se de ter instalados:

- **JDK 21+**
- **PostgreSQL** (padrão) ou **MySQL** (opcional)
- **Android Studio** (para emulador/dispositivo Android) ou **Desktop JVM** (para executar via Gradle sem emulador)
- **Xcode** (opcional, apenas para compilar o alvo iOS)

---

## 1. Como subir o Backend (Spring Boot)

1. Crie o banco de dados `donation_db` no PostgreSQL (as tabelas são criadas automaticamente pelo `schema-postgresql.sql` na primeira inicialização):

```sql
CREATE DATABASE donation_db;
```

2. Acesse a pasta do backend e inicie a API (por padrão conecta em `jdbc:postgresql://localhost:5432/donation_db` com usuário/senha `postgres`/`postgres`, sobrescrevíveis via `SPRING_DATASOURCE_USERNAME` e `SPRING_DATASOURCE_PASSWORD`):

```bash
cd backend
./mvnw spring-boot:run
```

> **Usando MySQL (opcional):** execute `./mvnw spring-boot:run -Dspring-boot.run.profiles=mysql` (com `SPRING_DATASOURCE_USERNAME` e `SPRING_DATASOURCE_PASSWORD` configurados).

A API ficará disponível em `http://localhost:8080`.

---

## 2. Como apontar o App para o Backend

O endereço do backend é resolvido automaticamente por plataforma via `expect`/`actual` (`platformDefaultBackendUrl()`):

- **Desktop (JVM):** usa `http://localhost:8080` por padrão, ou a variável de ambiente `SOLIDAR_API_URL` (ex.: `SOLIDAR_API_URL=http://192.168.0.10:8080`).
- **Android (Emulador ou Dispositivo Físico):** o Gradle detecta automaticamente o IP da máquina na rede local ou usa `10.0.2.2:8080` (emulador). Para fixar uma URL específica (HTTP ou HTTPS), crie/edite `app/local.properties`:

```properties
backend.url=http://10.0.2.2:8080
```

- **iOS:** usa `http://localhost:8080` (ou `backend.url` definido em `app/local.properties`).

---

## 3. Como executar o App

### Desktop (JVM)

```bash
cd app
./gradlew :desktopApp:run
```

### Android (Debug APK / Emulador)

```bash
cd app
./gradlew :androidApp:assembleDebug
```

O APK de debug será gerado em `app/androidApp/build/outputs/apk/debug/androidApp-debug.apk`. Você também pode abrir a pasta `app/` no Android Studio e executar a configuração `androidApp`.

### iOS

Abra a pasta `app/iosApp` no Xcode e execute em um simulador ou dispositivo Apple.

### Teste de Deep Link (Android)

Para testar a abertura direta de uma campanha via deep link (`solidar://campanha/{id}`):

```bash
adb shell am start -a android.intent.action.VIEW -d "solidar://campanha/1"
```

---

## 4. Como executar os testes e verificações de qualidade

### Mobile — Lint, Análise Estática e Testes (ViewModel com Turbine, Repositório/Cache e UI Compose)

```bash
cd app
./gradlew ktlintCheck detekt :shared:jvmTest
```

### Backend

```bash
cd backend
./mvnw test
```

---

## Acessibilidade e Registro de Testes com TalkBack (Android)

O aplicativo segue as diretrizes de acessibilidade do Material Design 3 e do leitor de tela **TalkBack** do Android:

1. **Cores Semânticas e Contraste (`MaterialTheme.colorScheme`):**
   - Todas as telas e componentes utilizam exclusivamente os papéis semânticos do tema (`primary`, `onPrimary`, `primaryContainer`, `onPrimaryContainer`, `secondaryContainer`, `onSecondaryContainer`, `background`, `onBackground`, `surface`, `onSurface`, `surfaceVariant`, `onSurfaceVariant`, `error`, `errorContainer`, `onErrorContainer`), garantindo contraste adequado tanto no **Modo Claro** quanto no **Modo Escuro**, sem cores fixas fora do tema.
2. **Hierarquia Semântica de Cabeçalhos (`heading()`):**
   - Títulos principais e cabeçalhos de seção em todas as telas (`LoginScreen`, `SignUpScreen`, `HomeScreen`, `ExplorarScreen`, `CampanhaDetalheScreen`, `OpcoesDoacaoScreen`, `CriarDoacaoScreen`, `CriarCampanhaScreen`, `DoarItensCampanhaScreen`, `DoarDinheiroScreen`, `EfetivarDoacaoScreen`, `HistoricoScreen` e `PlaceholderScreen`) possuem `Modifier.semantics { heading() }`, permitindo navegação rápida por cabeçalhos no TalkBack.
3. **Descrições de Conteúdo (`contentDescription`) e Agrupamento Semântico:**
   - Controles interativos sem rótulo textual explícito possuem `contentDescription` descritivo da ação (ex.: `"Voltar"`, `"Adicionar campanha aos favoritos"`, `"Remover campanha dos favoritos"`, `"Diminuir quantidade"`, `"Aumentar quantidade"`, `"Mostrar senha"`, `"Ocultar senha"`, `"Criar nova doação"`, `"Criar campanha ou doação"`, `"Ativar pedido de objetos"`, `"Ativar pedido de dinheiro"`, `"Remover local de coleta 1"` e leitura percentual nas barras de progresso).
   - Ícones e elementos meramente decorativos utilizam `contentDescription = null` ou `Modifier.clearAndSetSemantics {}` para evitar leituras redundantes no TalkBack.
   - Cartões informativos (`DoacaoCard`, `DoacaoDisponivelCard`, `CampaignItemCard` e cartões de locais de coleta) utilizam `mergeDescendants = true` para leitura fluida em um único gesto.
4. **Alvos de Toque (`48dp x 48dp`) e Papéis de Controle (`Role`):**
   - Botões, ícones de ação, chips de categoria (`Role.RadioButton` com estado `selected`), caixas de seleção (`Role.Checkbox`) e cartões clicáveis (`Role.Button`) respeitam a área mínima de toque de `48dp x 48dp`.

#### Roteiro de Verificação com TalkBack

Para validar a navegação por voz no dispositivo Android ou emulador:

1. Ative o TalkBack nas configurações de acessibilidade do Android (ou via ADB):
   ```bash
   adb shell settings put secure enabled_accessibility_services com.google.android.marvin.talkback/com.google.android.marvin.talkback.TalkBackService
   ```
2. **Fluxo verificado no TalkBack:**
   - **Login e Cadastro (`LoginScreen` / `SignUpScreen`):** Leitura do cabeçalho *"Bem-vindo de volta!"*, anúncio dos campos de texto *"E-mail"* e *"Senha"* sem duplicidade de ícone, botão *"Mostrar senha"* / *"Ocultar senha"*, botão *"Esqueci minha senha"* e seleção de perfil (*"Pessoa Física, botão"* e *"Instituição / ONG, botão"*).
   - **Feed Principal, Explorar e Navegação (`MainNavigationScaffold` / `HomeScreen` / `ExplorarScreen`):** Leitura dos cabeçalhos *"Campanhas abertas"* e *"Explorar doações"*, abas *"Pedem doações"* e *"Oferecem doações"*, cartões de campanha com anúncio do percentual da barra de progresso (quando aceita dinheiro), chips de categoria, ação *"Tenho interesse"*, abas inferiores/laterais e botão flutuante *"Criar campanha ou doação"* / *"Criar nova doação, botão"*.
   - **Detalhes da Campanha e Doação em Campanha (`CampanhaDetalheScreen` / `OpcoesDoacaoScreen` / `DoarItensCampanhaScreen` / `DoarDinheiroScreen`):** Navegação pelos cabeçalhos *"Campanha"*, *"Sobre a campanha:"*, *"Itens necessários para doar:"* e *"Locais físicos de coleta:"*, leitura agrupada dos itens e locais de coleta, anúncio de estado do botão *"Adicionar campanha aos favoritos"* / *"Remover campanha dos favoritos"*, cartões de modalidade (*"Contribuir com Dinheiro, botão"* e *"Doar Itens Físicos, botão"*) e confirmação de contribuição PIX/itens.
   - **Nova Doação e Nova Campanha (`CriarDoacaoScreen` / `CriarCampanhaScreen`):** Leitura dos cards de foto de capa/doação (`Role.Button`), switches de modalidade (*"Ativar pedido de objetos"* e *"Ativar pedido de dinheiro"*), botões *"Diminuir quantidade"* e *"Aumentar quantidade"*, chips de categoria e condição (*"Selecionado, Usado"* / *"Novo"*) e seção de locais físicos de coleta.
   - **Histórico e Efetivação (`HistoricoScreen` / `EfetivarDoacaoScreen`):** Navegação entre as abas *"Feitas"*, *"Interessados"*, *"Recebidas"* e *"Campanhas"*, leitura agrupada dos cartões do histórico, seleção do interessado e confirmação da entrega no local informado.

---

## Documentação Adicional

- [Proposta e Escopo do MVP](docs/proposta.md)
- [Arquitetura em Camadas, Fluxo de Dados e Persistência Offline](docs/arquitetura.md)
- [Registro de Uso de Inteligência Artificial](docs/uso-de-ia.md)

---

## Observações

- O backend possui autenticação stateless com Spring Security e uso de JWT (incluindo rotas de recuperação de senha `/api/auth/esqueci-senha` e `/api/auth/reset-senha`).
- O banco de dados é configurado via `backend/src/main/resources/application.properties` (PostgreSQL por padrão) e `application-mysql.properties` (perfil opcional `mysql`).
- O código compartilhado da aplicação mobile fica em `app/shared`, estruturado nas camadas `domain`, `data` e `ui` com persistência local (`FileLocalCacheStorage` e `PersistentTokenStorage`) para abertura offline.

## Contribuição

Este projeto foi desenvolvido como atividade acadêmica. Qualquer melhoria ou correção é bem-vinda.
