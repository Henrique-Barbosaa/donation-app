# Solidar.io - Proposta

Aplicativo de doações desenvolvido para a disciplina de Desenvolvimento de Sistemas para Dispositivos Móveis.

## Visão do produto

Para organizadores de campanhas solidárias, ONGs e doadores
Que enfrentam dificuldades para centralizar, divulgar e gerenciar iniciativas de arrecadação de forma segura e organizada
O Solidar.io é um aplicativo móvel multiplataforma
Que facilita o cadastro, a gestão e a participação em campanhas de doação, conectando quem precisa com quem deseja ajudar
Diferente de divulgações fragmentadas em redes sociais, grupos de mensagens ou controle em planilhas manuais
Nosso produto oferece um ambiente dedicado e seguro, permitindo o gerenciamento completo das campanhas e a interação direta dos usuários na palma da mão.

## Definição do MVP

O MVP focará no fluxo crítico de doações (criação, descoberta e efetivação), assegurando viabilidade no prazo de quatro sprints estabelecido.

| No MVP | Fora do MVP |
| :--- | :--- |
| Autenticação e roteamento de interface diferenciados para Pessoa Física e Instituição. | Módulo de Gestão de Vagas e Voluntariado em campanhas. |
| Criação de anúncios de doação com suporte a upload de imagens. | Sistema de Avaliações pós-doação. |
| Feed principal paginado para exploração de itens disponíveis. | Edição e remoção de locais de coleta das campanhas. |
| Funcionalidade de "demonstrar interesse" e efetivação da doação pelo doador original. | Recuperação e redefinição de senha complexa. |
| Notificações em tempo real (via Server-Sent Events - SSE) para respostas ágeis às intenções de doação na interface mobile. | |

**Hipótese de Valor:**
Acreditamos que pessoas comuns e instituições de caridade vão publicar e buscar itens regularmente no app porque a interface mobile responsiva e o feedback em tempo real reduzem drasticamente o esforço logístico de encontrar interessados próximos.
## Link para backlog inicial

## Tecnologias utilizadas (Plataforma-alvo escolhida)

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

## Execução dos testes

### Backend

```bash
cd backend
./mvnw test
```

### Mobile

```bash
cd app
./gradlew test
```

## Observações

- O backend possui autenticação com Spring Security e uso de JWT.
- O banco de dados é configurado via `application.properties`.
- O código compartilhado da aplicação mobile fica em `app/shared`.

### Integrantes
* Antonio Henrique Barbosa Lima - Matrícula: 20230048844
* Joao Batista Da Fonseca Neto - Matrícula: 20220077154
* Moab Fred Dos Santos Varela - Matrícula: 20260072967
