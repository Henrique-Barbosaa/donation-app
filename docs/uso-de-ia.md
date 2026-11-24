# Registro de Uso de Inteligência Artificial — Solidar.io

Este documento registra como ferramentas de Inteligência Artificial generativa foram utilizadas pela equipe durante o desenvolvimento do projeto na disciplina **DIM0524 — Desenvolvimento de Sistemas para Dispositivos Móveis**.

---

## 1. Ferramentas Utilizadas
* **Assistente de Código / Agente IA (Google Antigravity / Gemini):** Apoio em refatoração arquitetural KMP, adequação de acessibilidade (TalkBack) e escrita de testes automatizados.

---

## 2. Atividades Apoiadas por IA na Sprint 1 e Sprint 2

1. **Adequação ao Design System (Material 3) e Acessibilidade (TalkBack):**
   - Revisão dos componentes Compose para substituir cores hexadecimais fixas pelos papéis semânticos de `MaterialTheme.colorScheme` (garantindo contraste nos modos Claro e Escuro).
   - Adição de semântica de cabeçalhos (`Modifier.semantics { heading() }`), descrições de conteúdo (`contentDescription`), agrupamento semântico (`mergeDescendants = true`), papéis de controle (`Role.Button`, `Role.RadioButton`) e alvos mínimos de toque de `48dp`.
2. **Separação em Camadas (`domain`, `data`, `ui`) e Gerenciamento de Estado com `ViewModel`:**
   - Apoio na refatoração para remover dependências de DTOs da camada `domain`, estruturar o container de injeção `AppContainer`, modelar a classe selada `TelaEstado` (`Carregando`, `Vazio`, `Sucesso`, `Erro`) e migrar estados locais de `remember` para `StateFlow` consumidos com `collectAsStateWithLifecycle()`.
3. **Tratamento de Erros de Rede e Persistência Local Offline:**
   - Implementação do mapeamento de exceções do Ktor Client (`expectSuccess = true`) para as falhas de domínio `FalhaDominio.SemConexao` e `FalhaDominio.RespostaInvalida`, além do armazenamento local (`FileLocalCacheStorage`) usando o diretório nativo de cada plataforma via `expect`/`actual`.
4. **Suíte de Testes Automatizados:**
   - Apoio na estruturação dos testes de repositório com `MockEngine`, testes de `ViewModel` com `kotlinx-coroutines-test` e `Turbine`, e testes de interface Compose com `runComposeUiTest` no alvo Desktop/JVM (`:shared:jvmTest`).

---

## 3. Validação e Responsabilidade da Equipe
Todas as alterações geradas ou sugeridas com auxílio de IA foram revisadas pelos integrantes da equipe e validadas por meio das ferramentas de qualidade do projeto (`./gradlew ktlintCheck`, `./gradlew detekt`, `./gradlew :shared:jvmTest` e `./gradlew :androidApp:assembleDebug`).
