# Solidar.io - Proposta

Aplicativo de doações desenvolvido para a disciplina de Desenvolvimento de Sistemas para Dispositivos Móveis[cite: 3].

## 1. Visão do produto

Para organizadores de campanhas solidárias, ONGs e doadores[cite: 3]
Que enfrentam dificuldades para centralizar, divulgar e gerenciar iniciativas de arrecadação de forma segura e organizada[cite: 3]
O Solidar.io é um aplicativo móvel multiplataforma[cite: 3]
Que facilita o cadastro, a gestão e a participação em campanhas de doação, conectando quem precisa com quem deseja ajudar[cite: 3]
Diferente de divulgações fragmentadas em redes sociais, grupos de mensagens ou controle em planilhas manuais[cite: 3]
Nosso produto oferece um ambiente dedicado e seguro, permitindo o gerenciamento completo das campanhas e a interação direta dos usuários na palma da mão[cite: 3].

## 2. Definição do MVP (Produto Mínimo Viável)

O MVP focará no fluxo crítico de doações (criação, descoberta e efetivação), assegurando viabilidade no prazo de quatro sprints estabelecido[cite: 4].

| No MVP | Fora do MVP |
| :--- | :--- |
| Autenticação e roteamento de interface diferenciados para Pessoa Física e Instituição.[cite: 4] | Módulo de Gestão de Vagas e Voluntariado em campanhas.[cite: 4] |
| Criação de anúncios de doação com suporte a upload de imagens.[cite: 4] | Sistema de Avaliações pós-doação.[cite: 4] |
| Feed principal paginado para exploração de itens disponíveis.[cite: 4] | Edição e remoção de locais de coleta das campanhas.[cite: 4] |
| Funcionalidade de "demonstrar interesse" e efetivação da doação pelo doador original.[cite: 4] | Recuperação e redefinição de senha complexa.[cite: 4] |
| Notificações em tempo real (via Server-Sent Events - SSE) para respostas ágeis às intenções de doação na interface mobile.[cite: 4] | |

**Hipótese de Valor:**
Acreditamos que pessoas comuns e instituições de caridade vão publicar e buscar itens regularmente no app porque a interface mobile responsiva e o feedback em tempo real reduzem drasticamente o esforço logístico de encontrar interessados próximos[cite: 4].

## 3. Link para backlog inicial

O backlog com as histórias de usuário estimadas e priorizadas (no mínimo 5 histórias formatadas, 3 estimadas) está disponível no link abaixo:[cite: 4]

[INSERIR LINK DO GITHUB PROJECTS AQUI][cite: 4]

## 4. Plataforma-alvo escolhida e justificativa

A plataforma-alvo inicial escolhida é o Android (construída via Kotlin Multiplatform - KMP e Compose Multiplatform)[cite: 4].

**Justificativa:** A predominância de dispositivos Android no Brasil, especialmente no contexto de ONGs e caridade, torna essa plataforma ideal para testes de campo rápidos e validação do fluxo do usuário[cite: 4]. O uso do Compose Multiplatform permite a construção de uma interface nativa de alta performance, essencial para lidar com recursos como o carregamento do Feed principal (Infinite Scroll) e o consumo da rota SSE sem gargalos na UI-Thread[cite: 4]. Embora o KMP permita múltiplos alvos, focaremos estritamente no Android durante as 4 sprints para proteger o escopo e garantir excelência em usabilidade e performance da primeira tela[cite: 4].

## 5. Estratégia de backend escolhida e justificativa

A API RESTful existente, construída com Java 21 e Spring Boot, servirá como base do sistema, conectando-se a um banco de dados MySQL e gerindo sessões com Spring Security via tokens JWT em Cookies[cite: 4].

**Justificativa:** Esta infraestrutura já atende os domínios mapeados do produto (Gestão de Usuários, Campanhas e Doações)[cite: 4]. A comunicação em tempo real via Server-Sent Events (/api/notificacoes/stream) será integrada para criar microinterações ágeis no aplicativo, elevando a experiência UX do usuário em dispositivos móveis[cite: 4]. Para assegurar que o aplicativo mobile não sofra com latência ou consumo excessivo de memória ao processar grandes volumes de dados, o desenvolvimento e manutenção das querys no Spring Data JPA seguirão critérios rigorosos de desempenho (com a resolução do problema N+1 utilizando recursos adequados, como JOIN FETCH), garantindo payloads JSON leves e otimizados nas rotas paginadas de listagem, como /api/doacoes/disponiveis[cite: 4].

## 6. Equipe

| Nome | Matrícula | Papel Principal |
| :--- | :--- | :--- |
| Antonio Henrique Barbosa Lima[cite: 3, 4] | 20230048844[cite: 3] | Desenvolvedor Integrante[cite: 4] |
| Joao Batista Da Fonseca Neto[cite: 3] | 20220077154[cite: 3] | Desenvolvedor Integrante |
| Moab Fred Dos Santos Varela[cite: 3] | 20260072967[cite: 3] | Desenvolvedor Integrante |

## 7. Coorte e Integração

* **Instituição:** UFRN / IMD (Instituto Metrópole Digital)[cite: 4]
* **Disciplina:** DIM0524[cite: 1, 4]
* **Semestre/Coorte:** 2026.2 (Desenvolvimento Mobile)[cite: 4]
