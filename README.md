# Solidar.io
[![CI Pipeline](https://github.com/Henrique-Barbosaa/donation-app/actions/workflows/ci.yml/badge.svg)](https://github.com/Henrique-Barbosaa/donation-app/actions/workflows/ci.yml)  

> Aplicativo de doações desenvolvido para a disciplina de Desenvolvimento de Sistemas para Dispositivos Móveis.   
> **Vídeo de Apresentação (Sprint 0):** *(Link do vídeo de 5 minutos)*  
> **Vídeo de Apresentação (Sprint 1):** [Acessar vídeo da Sprint 1 (Google Drive)](https://drive.google.com/drive/folders/12fEucObhW5YguU4JAUaxxDRwLSnvapnZ)

## 👥 Equipe
* **Nome da Equipe:** Team Solidar.io
* **Coorte:** DIM0524 - DESENVOLVIMENTO DE SISTEMAS PARA DISPOSITIVOS MÓVEIS

### Integrantes
* Antonio Henrique Barbosa Lima - Matrícula: 20230048844
* Joao Batista Da Fonseca Neto - Matrícula: 20220077154
* Moab Fred Dos Santos Varela - Matrícula: 20260072967

## Visão geral

Este projeto combina:

- uma aplicação mobile multiplataforma em Kotlin Multiplatform (Android e iOS);
- um backend em Java com Spring Boot;
- integração com banco de dados MySQL;
- autenticação e autorização com Spring Security e JWT.

A ideia do sistema é facilitar o cadastro e a gestão de campanhas de doação, permitindo que usuários interajam com a aplicação mobile e consumam serviços expostos pelo backend.

## Estrutura do projeto

```text
donation-app/
├── app/                 # Aplicativo Kotlin Multiplatform
│   ├── androidApp/      # App Android
│   ├── iosApp/          # App iOS
│   ├── shared/          # Código compartilhado entre Android e iOS
│   ├── build.gradle.kts
│   ├── settings.gradle.kts
│   └── gradlew
├── backend/             # API em Spring Boot
│   ├── src/
│   ├── pom.xml
│   └── mvnw
├── README.md            # Documentação principal do projeto
└── LICENSE              # se existir no futuro
```

## Tecnologias utilizadas

### Mobile
- Kotlin Multiplatform
- Jetpack Compose
- Android Studio
- Xcode (para iOS)

### Backend
- Java 21
- Spring Boot 4
- Spring Security
- JWT
- MySQL
- Maven

## Requisitos

Antes de rodar o projeto, certifique-se de ter instalados:

- JDK 21+
- Android Studio
- Xcode (somente para compilar a versão iOS)
- MySQL
- Maven (ou use o wrapper do projeto)

## Configuração do backend

1. Acesse a pasta do backend:

```bash
cd backend
```

2. Ajuste as configurações do banco em `src/main/resources/application.properties`.

3. Inicie a API:

```bash
./mvnw spring-boot:run
```

A aplicação backend ficará disponível em:

```text
http://localhost:8080
```

## Execução do app mobile

### Android

```bash
cd app
./gradlew :androidApp:assembleDebug
```

ou abra o projeto em Android Studio e execute a configuração do módulo `androidApp`.

### iOS

Abra a pasta `app/iosApp` no Xcode e execute o projeto em um simulador ou dispositivo Apple.

### Teste de Deep Link (Android)

Para testar a abertura direta de uma campanha via deep link configurado na Sprint 1:

```bash
adb shell am start -a android.intent.action.VIEW -d "solidar://campanha/1"
```

### Acessibilidade e Registro de Testes com TalkBack (Android)

O aplicativo segue as diretrizes de acessibilidade do Material Design 3 e do leitor de tela **TalkBack** do Android:

1. **Cores Semânticas e Contraste (`MaterialTheme.colorScheme`):**
   - Todas as telas e componentes utilizam exclusivamente os papéis semânticos do tema (`primary`, `onPrimary`, `primaryContainer`, `onPrimaryContainer`, `secondaryContainer`, `onSecondaryContainer`, `background`, `onBackground`, `surface`, `onSurface`, `surfaceVariant`, `onSurfaceVariant`, `error`, `errorContainer`, `onErrorContainer`), garantindo contraste adequado tanto no **Modo Claro** quanto no **Modo Escuro**, sem cores fixas fora do tema.
2. **Hierarquia Semântica de Cabeçalhos (`heading()`):**
   - Títulos principais e cabeçalhos de seção em todas as telas (`LoginScreen`, `SignUpScreen`, `HomeScreen`, `CampanhaDetalheScreen`, `OpcoesDoacaoScreen`, `CriarDoacaoScreen`, `HistoricoScreen` e `PlaceholderScreen`) possuem `Modifier.semantics { heading() }`, permitindo navegação rápida por cabeçalhos no TalkBack.
3. **Descrições de Conteúdo (`contentDescription`) e Agrupamento Semântico:**
   - Controles interativos sem rótulo textual explícito possuem `contentDescription` descritivo da ação (ex.: `"Voltar"`, `"Adicionar campanha aos favoritos"`, `"Remover campanha dos favoritos"`, `"Diminuir quantidade"`, `"Aumentar quantidade"`, `"Mostrar senha"`, `"Ocultar senha"`, `"Criar nova doação"` e leitura percentual nas barras de progresso).
   - Ícones e elementos meramente decorativos utilizam `contentDescription = null` ou `Modifier.clearAndSetSemantics {}` para evitar leituras redundantes no TalkBack.
   - Cartões informativos (`DoacaoCard` e `CampaignItemCard`) utilizam `mergeDescendants = true` para leitura fluida em um único gesto.
4. **Alvos de Toque (`48dp x 48dp`) e Papéis de Controle (`Role`):**
   - Botões, ícones de ação, chips de categoria (`Role.RadioButton` com estado `selected`) e cartões clicáveis (`Role.Button`) respeitam a área mínima de toque de `48dp x 48dp`.

#### Roteiro de Verificação com TalkBack

Para validar a navegação por voz no dispositivo Android ou emulador:

1. Ative o TalkBack nas configurações de acessibilidade do Android (ou via ADB):
   ```bash
   adb shell settings put secure enabled_accessibility_services com.google.android.marvin.talkback/com.google.android.marvin.talkback.TalkBackService
   ```
2. **Fluxo verificado no TalkBack:**
   - **Login e Cadastro (`LoginScreen` / `SignUpScreen`):** Leitura do cabeçalho *"Bem-vindo de volta!"*, anúncio dos campos de texto *"E-mail"* e *"Senha"* sem duplicidade de ícone, botão *"Mostrar senha"* / *"Ocultar senha"* e seleção de perfil (*"Pessoa Física, botão"* e *"Instituição / ONG, botão"*).
   - **Feed Principal e Navegação (`MainNavigationScaffold` / `HomeScreen`):** Leitura do cabeçalho *"Campanhas abertas"*, cartões de campanha com anúncio do percentual da barra de progresso, abas inferiores/laterais e botão flutuante *"Criar nova doação, botão"*.
   - **Detalhes da Campanha e Opções de Doação (`CampanhaDetalheScreen` / `OpcoesDoacaoScreen`):** Navegação pelos cabeçalhos *"Campanha"*, *"Sobre a campanha:"* e *"Itens necessários para doar:"*, leitura agrupada dos itens solicitados, anúncio de estado do botão *"Adicionar campanha aos favoritos"* / *"Remover campanha dos favoritos"* e cartões de modalidade de doação (*"Contribuir com Dinheiro, botão"* e *"Doar Itens Físicos, botão"*).
   - **Nova Doação (`CriarDoacaoScreen`) e Histórico (`HistoricoScreen`):** Leitura dos botões *"Diminuir quantidade"* e *"Aumentar quantidade"*, anúncio de seleção dos chips de categoria e condição (*"Selecionado, Usado"* / *"Novo"*) e leitura agrupada dos cartões do histórico de doações.

## Execução dos testes

### Backend

```bash
cd backend
./mvnw test
```

### Mobile (KMP — ViewModel, Repositório e Interface Compose)

```bash
cd app
./gradlew :shared:jvmTest
```

## Documentação Adicional

- [Proposta e Escopo do MVP](docs/proposta.md)
- [Arquitetura em Camadas, Fluxo de Dados e Persistência Offline](docs/arquitetura.md)
- [Registro de Uso de Inteligência Artificial](docs/uso-de-ia.md)

## Observações

- O backend possui autenticação com Spring Security e uso de JWT.
- O banco de dados é configurado via `application.properties`.
- O código compartilhado da aplicação mobile fica em `app/shared`, estruturado nas camadas `domain`, `data` e `ui` com persistência local para abertura offline.

## Contribuição

Este projeto foi desenvolvido como atividade acadêmica. Qualquer melhoria ou correção é bem-vinda.
