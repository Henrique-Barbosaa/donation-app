(Definir os pontos abaixo)
# Solidar.io - Proposta

Aplicativo de doações desenvolvido para a disciplina de Desenvolvimento de Sistemas para Dispositivos Móveis.

## Visão do produto

Para organizadores de campanhas solidárias, ONGs e doadores
Que enfrentam dificuldades para centralizar, divulgar e gerenciar iniciativas de arrecadação de forma segura e organizada
O Solidar.io é um aplicativo móvel multiplataforma
Que facilita o cadastro, a gestão e a participação em campanhas de doação, conectando quem precisa com quem deseja ajudar
Diferente de divulgações fragmentadas em redes sociais, grupos de mensagens ou controle em planilhas manuais
Nosso produto oferece um ambiente dedicado e seguro, permitindo o gerenciamento completo das campanhas e a interação direta dos usuários na palma da mão.

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
├── docs/
│   └── proposta.md      # visão, MVP, link do backlog, plataforma-alvo, backend, equipe, coorte/integração
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


Definição do MVP: dentro e fora do escopo
Link para backlog inicial, com as histórias priorizadas (pode ser para o próprio repositório caso o backlog esteja registrado nele — o GitHub Projects mora dentro do repositório do GitHub)
Plataforma-alvo escolhida — Android ou iOS — e justificativa
Estratégia de backend escolhida e justificativa — ver STACK.md
Equipe: nome, matrícula e papel de cada integrante
Coorte de apresentação e, se houver, integração com outra disciplina
Máximo 5 páginas. A justificativa das escolhas técnicas deve partir das características do produto.

As opções de plataforma-alvo, interface e backend, com seus limites, estão em STACK.md.
