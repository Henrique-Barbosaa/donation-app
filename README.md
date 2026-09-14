# Solidar.io

Aplicativo de doações desenvolvido para a disciplina de Desenvolvimento de Sistemas para Dispositivos Móveis.

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

## Contribuição

Este projeto foi desenvolvido como atividade acadêmica. Qualquer melhoria ou correção é bem-vinda.

## Autor

Projeto desenvolvido na UFRN como parte da disciplina de Dispositivos Móveis.

