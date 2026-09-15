# Solidar.io - Proposta

Aplicativo de doações desenvolvido para a disciplina de Desenvolvimento de Sistemas para Dispositivos Móveis.

## 1. Visão do produto

Para organizadores de campanhas solidárias, ONGs e doadores
Que enfrentam dificuldades para centralizar, divulgar e gerenciar iniciativas de arrecadação de forma segura e organizada
O Solidar.io é um aplicativo móvel multiplataforma
Que facilita o cadastro, a gestão e a participação em campanhas de doação, conectando quem precisa com quem deseja ajudar
Diferente de divulgações fragmentadas em redes sociais, grupos de mensagens ou controle em planilhas manuais
Nosso produto oferece um ambiente dedicado e seguro, permitindo o gerenciamento completo das campanhas e a interação direta dos usuários na palma da mão.

## 2. Definição do MVP (Produto Mínimo Viável)

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

## 3. Link para backlog inicial

O backlog com as histórias de usuário estimadas e priorizadas (no mínimo 5 histórias formatadas, 3 estimadas) está disponível no link abaixo:

https://github.com/users/Henrique-Barbosaa/projects/4

## 4. Plataforma-alvo escolhida e justificativa

A plataforma-alvo inicial escolhida é o Android (construída via Kotlin Multiplatform - KMP e Compose Multiplatform).

**Justificativa:** A predominância de dispositivos Android no Brasil, especialmente no contexto de ONGs e caridade, torna essa plataforma ideal para testes de campo rápidos e validação do fluxo do usuário. O uso do Compose Multiplatform permite a construção de uma interface nativa de alta performance, essencial para lidar com recursos como o carregamento do Feed principal (Infinite Scroll) e o consumo da rota SSE sem gargalos na UI-Thread. Embora o KMP permita múltiplos alvos, focaremos estritamente no Android durante as 4 sprints para proteger o escopo e garantir excelência em usabilidade e performance da primeira tela.

## 5. Estratégia de backend escolhida e justificativa

A API RESTful existente, construída com Java 21 e Spring Boot, servirá como base do sistema, conectando-se a um banco de dados MySQL e gerindo sessões com Spring Security via tokens JWT em Cookies.

**Justificativa:** Esta infraestrutura já atende os domínios mapeados do produto (Gestão de Usuários, Campanhas e Doações). A comunicação em tempo real via Server-Sent Events (/api/notificacoes/stream) será integrada para criar microinterações ágeis no aplicativo, elevando a experiência UX do usuário em dispositivos móveis. Para assegurar que o aplicativo mobile não sofra com latência ou consumo excessivo de memória ao processar grandes volumes de dados, o desenvolvimento e manutenção das querys no Spring Data JPA seguirão critérios rigorosos de desempenho (com a resolução do problema N+1 utilizando recursos adequados, como JOIN FETCH), garantindo payloads JSON leves e otimizados nas rotas paginadas de listagem, como /api/doacoes/disponiveis.

## 6. Equipe

| Nome | Matrícula | Papel Principal |
| :--- | :--- | :--- |
| Antonio Henrique Barbosa Lima| 20230048844 | Desenvolvedor Integrante |
| Joao Batista Da Fonseca Neto | 20220077154 | Desenvolvedor Integrante |
| Moab Fred Dos Santos Varela | 20260072967 | Desenvolvedor Integrante |

## 7. Coorte e Integração

* **Instituição:** UFRN / IMD (Instituto Metrópole Digital)
* **Disciplina:** DIM0524
* **Semestre/Coorte:** 2026.2 (Desenvolvimento Mobile)
