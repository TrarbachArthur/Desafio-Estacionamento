# Especificação de Requisitos de Software (SRS): API de Gestão de Estacionamento

**Projeto:** Desafio Técnico Setis — Vaga de Estágio (Backend Java)

**Versão:** 1.0.0

**Data:** 23/07/2026

**Responsável:** Stéfano Giordano

---

## 1. Introdução

### 1.1 Propósito
Este documento descreve os requisitos de uma API REST para gestão de um estacionamento rotativo, a ser implementada como desafio técnico para a vaga de estágio em desenvolvimento backend.

O objetivo **não** é entregar um produto pronto para produção. O objetivo é observar como você:

* interpreta e segue um documento técnico de implementação;
* modela um problema de negócio usando orientação a objetos;
* estrutura uma API REST;
* organiza código, repositório e documentação;
* justifica as decisões que tomou.

### 1.2 Escopo
O sistema abrange o cadastro de vagas e clientes mensalistas, o registro de entrada e saída de veículos, o cálculo do valor a pagar conforme o plano contratado e a consulta do histórico de tickets.

**Fora de escopo:** autenticação, interface gráfica, pagamento real, integração com cancelas ou sensores, deploy em nuvem.

### 1.3 O que será avaliado (e o que não será)

| Avaliamos | Não avaliamos |
| :--- | :--- |
| Clareza da modelagem de domínio | UX |
| Correção das regras de cálculo | Cobertura de testes de 100% |
| Aderência ao contrato definido aqui | Quantidade de linhas escritas |
| Legibilidade e organização do código | Performance ou escalabilidade |
| Qualidade do README e do histórico de commits | Uso de frameworks "avançados" |
| Capacidade de defender suas escolhas | Ter concluído 100% dos requisitos |

Uma entrega **menor e bem feita** vale mais do que uma entrega completa e desorganizada. Se faltar tempo, entregue menos e documente no README o que ficou de fora e por quê.

### 1.4 Definições
* **Ticket:** registro de uma permanência, aberto na entrada e encerrado na saída.
* **Plano:** política de cobrança aplicada ao ticket.
* **Mensalista:** cliente com assinatura vigente, isento de cobrança por permanência.
* **Dia-calendário:** dia do calendário civil, independentemente do horário.

---

## 2. Regras do Desafio

### 2.1 Prazo e entrega
* **Prazo:** encaminhado junto ao e-mail contendo este documento.
* **Entrega:** link de um repositório público no GitHub, enviado por e-mail.

### 2.2 Liberdade de decisão
Você escolhe livremente:
* estilo arquitetural e padrões de projeto;
* Maven ou Gradle (recomendamos Maven, padrão utilizado pela empresa);
* bibliotecas auxiliares (Lombok, MapStruct, springdoc-openapi, etc.);
* **o formato do corpo de erro** da API;
* **o formato da resposta paginada**;
* estratégia e granularidade de testes.

**Toda escolha relevante deve estar registrada no README.** Não existe resposta certa.

### 2.3 Uso de IA e da internet
O uso de internet, documentação e ferramentas de IA (Claude, Copilot, ChatGPT) é **permitido e esperado**, utilizamos no dia a dia com ressalvas para colaboradores com menos senioridade, então esperamos que 100% das decisões sejam feitas por você, com a utilização se restringindo a pesquisa e entendimento de tecnologias.

Portanto, você deve ser capaz de **explicar e defender** a sua entrega. Haverá uma sessão de "defesa" técnica.

### 2.4 Dúvidas
Requisitos ambíguos são propositais em qualquer especificação real. Para prosseguir, você deve assumir uma premissa e **documentá-la** no README, em uma seção "Premissas assumidas".

---

## 3. Descrição Geral

### 3.1 Stack Tecnológica

**Obrigatório:**
* **Runtime:** Java 21 LTS.
* **Framework:** Spring Boot 3.x (Web, Data JPA, Validation).
* **Build:** Maven ou Gradle, com build reproduzível por um único comando.
* **Banco de dados:** H2 em memória (ou algum provisionado via Docker, sendo considerado critério de destaque. Consultar seção 8).

Docker, migrations versionadas e documentação OpenAPI **não são obrigatórios** (consultar seção 8).

### 3.2 Arquitetura sugerida
Não iremos impor uma arquitetura. Uma separação clássica em camadas já é suficiente, segue abaixo uma recomendação:

```
controller  → recebe HTTP, valida entrada, devolve DTOs
service     → orquestra regras de negócio
domain      → entidades, enums, regras invariantes
repository  → acesso a dados
dto         → contratos de entrada e saída
exception   → exceções de negócio + handler global
```

Únicas duas regras que iremos considerar como erro caso não seguidas:

* **[A01]** Entidades JPA **não** são expostas diretamente nos endpoints. Use DTOs.
* **[A02]** Regra de negócio **não** mora no `Controller`.

### 3.3 Representação monetária
Valores trafegam na API como `String` decimal com duas casas: `"35.00"`, `"0.00"`, `"12.50"`.

**É proibido o uso de `float` ou `double` para dinheiro.** Use `BigDecimal` ou `long` em centavos, a escolha é sua, mas deve ser justificada no README.

### 3.4 Tratamento de tempo
Datas e horários trafegam em ISO-8601 (`2026-07-23T14:30:00`).

Os horários de entrada e de saída são, por padrão, determinados pelo **servidor**. Para viabilizar o teste manual dos cenários de longa permanência (tabela de referência do RF05), ambos podem ser **informados opcionalmente** na requisição, conforme RF03 e RF04.

---

## 4. Requisitos Funcionais

### [RF01] Gestão de Vagas
* **Endpoints:**
  * `POST /v1/vagas` — cadastro de vaga.
  * `GET /v1/vagas` — listagem, com filtros opcionais `tipo` e `status`.
  * `GET /v1/vagas/{id}` — consulta individual.
  * `PUT /v1/vagas/{id}/status` — alteração de status operacional.
* **Entradas (`CriarVagaRequest`):**
  * `codigo` (String, obrigatório, 1–10 caracteres, **único** no sistema). Ex.: `"A-01"`.
  * `tipo` (Enum `TipoVaga`, obrigatório).
* **Entradas (`AlterarStatusVagaRequest`):**
  * `status` (Enum, obrigatório): apenas `LIVRE` ou `MANUTENCAO`.
* **Regras de Negócio:**
  1. `codigo` de vaga duplicado retorna `409 Conflict`.
  2. Toda vaga é criada com status `LIVRE`.
  3. O status `OCUPADA` **nunca** é definido manualmente, é consequência de um ticket aberto (RF03). Tentar defini-lo retorna `400`.
  4. Uma vaga `OCUPADA` não pode ir para `MANUTENCAO` (`409`).
  5. Vagas não são excluídas fisicamente.
* **Saídas:** `201 Created` com header `Location` na criação; `200 OK` nas demais.

---

### [RF02] Gestão de Clientes Mensalistas
* **Endpoints:**
  * `POST /v1/clientes` — cadastro.
  * `GET /v1/clientes/{id}` — consulta.
  * `PUT /v1/clientes/{id}` — atualização cadastral e da vigência da assinatura.
* **Entradas (`CriarClienteRequest`):**
  * `nome` (String, obrigatório, 3–120 caracteres).
  * `documento` (String, obrigatório, 11 dígitos numéricos, **único**).
  * `assinaturaValidaAte` (Date, obrigatório, formato `yyyy-MM-dd`).
* **Entradas (`AtualizarClienteRequest`):**
  * `nome` (String, obrigatório, 3–120 caracteres).
  * `assinaturaValidaAte` (Date, obrigatório, formato `yyyy-MM-dd`).
* **Regras de Negócio:**
  1. `documento` duplicado retorna `409 Conflict`.
  2. `documento` inválido retorna `400 Bad Request`.
  3. `assinaturaValidaAte` no passado é permitido no cadastro (representa assinatura vencida), mas o efeito prático aparece no RF05.

---

### [RF03] Registro de Entrada (abertura de ticket)
* **Identificador:** `POST /v1/tickets`
* **Entradas (`AbrirTicketRequest`):**

| Campo | Tipo | Obrigatório | Descrição |
| :--- | :--- | :--- | :--- |
| `placa` | String | Sim | Padrão antigo (`ABC1234`) ou Mercosul (`ABC1D23`) |
| `tipoVeiculo` | Enum `TipoVeiculo` | Sim | `MOTO`, `CARRO`, `VAN` |
| `vagaId` | UUID | Sim | Vaga escolhida |
| `plano` | Enum `Plano` | Sim | `AVULSO`, `DIARIA`, `MENSALISTA` |
| `clienteId` | UUID | Condicional | Obrigatório **somente** se `plano = MENSALISTA` |
| `entrada` | DateTime | Não | ISO-8601. Se omitido, assume o instante atual do servidor |

* **Regras de Negócio:**
  1. A placa deve respeitar um dos dois formatos aceitos (`400` caso contrário). Normalize para maiúsculas.
  2. Não pode existir outro ticket `ABERTO` para a mesma placa (`409`).
  3. A vaga deve existir (`404`) e estar `LIVRE` (`409`).
  4. A vaga deve **acomodar** o tipo de veículo, conforme a matriz de compatibilidade do dicionário de dados (Seção 5). Caso contrário, `422 Unprocessable Entity` com mensagem clara.
  5. `plano = MENSALISTA` sem `clienteId` retorna `400`; com `clienteId` inexistente, `404`.
  6. `clienteId` informado para plano diferente de `MENSALISTA` retorna `400`.
  7. Se `entrada` for omitido, assume-se o instante atual do servidor. Se informado, **não pode ser futuro** (`400`).
  8. Ao abrir o ticket, ele nasce `ABERTO` e a vaga passa a `OCUPADA`.
* **Saída:** `201 Created` com header `Location` e o `TicketResponse` no body (Seção 6.2).

---

### [RF04] Registro de Saída (encerramento e cobrança)
* **Identificador:** `POST /v1/tickets/{id}/encerramento`
* **Entradas (`EncerrarTicketRequest`, corpo opcional):**

| Campo | Tipo | Obrigatório | Descrição |
| :--- | :--- | :--- | :--- |
| `saida` | DateTime | Não | ISO-8601. Se omitido, assume o instante atual do servidor |

* **Regras de Negócio:**
  1. Aplicável apenas a tickets `ABERTO`. Qualquer outro status retorna `409 Conflict`.
  2. Se `saida` for omitido, assume-se o instante atual do servidor.
  3. `saida` anterior à `entrada` do ticket retorna `422 Unprocessable Entity`.
  4. O `valorTotal` é calculado conforme a política do plano (RF05).
  5. Ao encerrar: ticket passa a `ENCERRADO` e a vaga volta a `LIVRE`.
* **Saída:** `200 OK` com o `TicketResponse` atualizado.

---

### [RF05] Políticas de Cobrança

A permanência (`duracao`) é o intervalo entre `entrada` e `saida`.

#### Plano `AVULSO`
1. **Tolerância:** permanência de **até 15 minutos, inclusive**, é isenta (`R$ 0,00`).
2. Ultrapassada a tolerância, cobra-se a permanência **integral** (não há desconto dos 15 minutos).
3. **Primeira hora:** `R$ 10,00`. **Cada hora adicional iniciada:** `R$ 5,00`.
   * Formalmente: `horasCobradas = teto(duracaoEmMinutos / 60)` e `valor = 10,00 + (horasCobradas - 1) × 5,00`.
4. **Teto:** o valor não excede `R$ 50,00` por **período de 24 horas iniciado**.
   * Formalmente: `periodos = teto(duracaoEmHoras / 24)` e `valorFinal = mínimo(valor, 50,00 × periodos)`.

#### Plano `DIARIA`
1. `R$ 35,00` por **dia-calendário iniciado**, contando entrada e saída.
2. Não há tolerância.
3. Atenção: a contagem é por data civil, **não** por blocos de 24 horas. Entrada às 23:00 do dia 10 e saída às 01:00 do dia 11 correspondem a **2 diárias**.

#### Plano `MENSALISTA`
1. Se a assinatura do cliente estiver **vigente na data de entrada** (`assinaturaValidaAte >= data de entrada`), o valor é `R$ 0,00`.
2. Se a assinatura estiver **vencida na data de entrada**, aplica-se integralmente a política `AVULSO`, e o campo `planoAplicado` da resposta deve refletir `AVULSO`.

#### Tabela de referência (casos que serão testados)

Todos os casos abaixo são reproduzíveis pela própria API, informando `entrada` na abertura (RF03) e `saida` no encerramento (RF04). Recomendamos validá-los antes de entregar.

| # | Plano | Entrada | Saída | Valor esperado |
| :--- | :--- | :--- | :--- | :--- |
| 1 | AVULSO | 10/03 08:00 | 10/03 08:10 | `"0.00"` |
| 2 | AVULSO | 10/03 08:00 | 10/03 08:15 | `"0.00"` |
| 3 | AVULSO | 10/03 08:00 | 10/03 08:16 | `"10.00"` |
| 4 | AVULSO | 10/03 08:00 | 10/03 09:00 | `"10.00"` |
| 5 | AVULSO | 10/03 08:00 | 10/03 09:01 | `"15.00"` |
| 6 | AVULSO | 10/03 08:00 | 10/03 11:30 | `"25.00"` |
| 7 | AVULSO | 10/03 08:00 | 10/03 18:00 | `"50.00"` (teto) |
| 8 | AVULSO | 10/03 08:00 | 11/03 10:00 | `"100.00"` (2 períodos) |
| 9 | DIARIA | 10/03 08:00 | 10/03 20:00 | `"35.00"` |
| 10 | DIARIA | 10/03 23:00 | 11/03 01:00 | `"70.00"` |
| 11 | MENSALISTA (vigente) | 10/03 08:00 | 10/03 20:00 | `"0.00"` |
| 12 | MENSALISTA (vencida) | 10/03 08:00 | 10/03 09:01 | `"15.00"` |

**Nota:** provavelmente surgirão novos planos no futuro (ex.: `EVENTO`, valor fixo independente do tempo decorrido). Não faz parte desta entrega implementá-lo, mas vale considerar isso ao decidir onde o cálculo de cada plano vai morar.

---

### [RF06] Consulta de Tickets
* **Identificadores:**
  * `GET /v1/tickets/{id}` — detalhe. `404` se não existir.
  * `GET /v1/tickets` — listagem com filtros.
* **Filtros (query params, todos opcionais e combináveis):** `placa`, `status`, `plano`, `dataInicio`, `dataFim` (sobre a data de entrada, inclusivos).
* **Regras de Negócio:**
  1. Sem filtros, retorna todos os tickets.
  2. Valores inválidos de enum ou data retornam `400 Bad Request` com mensagem legível, **nunca** `500`.
  3. `dataInicio` posterior a `dataFim` retorna `400`.
  4. A resposta deve ser **paginada**. O formato é livre, desde que documentado no README.

---

## 5. Dicionário de Dados

**Os valores dos enums abaixo fazem parte do contrato e devem ser respeitados**, são eles que trafegam no JSON. Já a modelagem das entidades e a decisão de **onde mora cada comportamento** são inteiramente suas.

### 5.1 TipoVeiculo
```java
public enum TipoVeiculo { MOTO, CARRO, VAN }
```

### 5.2 TipoVaga
```java
public enum TipoVaga { MOTO, PADRAO, GRANDE }
```

**Matriz de compatibilidade**

| Tipo de vaga | MOTO | CARRO | VAN |
| :--- | :---: | :---: | :---: |
| `MOTO` | ✅ | ❌ | ❌ |
| `PADRAO` | ✅ | ✅ | ❌ |
| `GRANDE` | ✅ | ✅ | ✅ |

### 5.3 StatusVaga
```java
public enum StatusVaga { LIVRE, OCUPADA, MANUTENCAO }
```

### 5.4 Plano
```java
public enum Plano { AVULSO, DIARIA, MENSALISTA }
```

### 5.5 StatusTicket
```java
public enum StatusTicket { ABERTO, ENCERRADO }
```

---

## 6. Contratos

**Sobre campos nulos:** os exemplos abaixo exibem `null` apenas para deixar claro **quais campos existem** e quando ainda não têm valor. Omitir esses campos da resposta (via `@JsonInclude(NON_NULL)` ou equivalente) é perfeitamente aceitável, desde que o comportamento seja **consistente em toda a API** e registrado no README. O que avaliamos é o nome e o tipo dos campos preenchidos, não a presença dos nulos.

### 6.1 Abertura de ticket
```http
POST /v1/tickets
Content-Type: application/json
```
```json
{
  "placa": "abc1d23",
  "tipoVeiculo": "CARRO",
  "vagaId": "3f1a9c2e-5b7d-4a11-9c3e-8d2f6b4a1c00",
  "plano": "AVULSO"
}
```

Com horário de entrada informado (útil para reproduzir os cenários da tabela de referência do RF05):
```json
{
  "placa": "abc1d23",
  "tipoVeiculo": "CARRO",
  "vagaId": "3f1a9c2e-5b7d-4a11-9c3e-8d2f6b4a1c00",
  "plano": "AVULSO",
  "entrada": "2026-03-10T08:00:00"
}
```

### 6.2 Resposta — `201 Created`
```json
{
  "id": "b7c4d1e2-9a03-4f55-8e21-77aa10bc4d31",
  "placa": "ABC1D23",
  "tipoVeiculo": "CARRO",
  "plano": "AVULSO",
  "planoAplicado": null,
  "cliente": null,
  "vaga": {
    "id": "3f1a9c2e-5b7d-4a11-9c3e-8d2f6b4a1c00",
    "codigo": "A-01",
    "tipo": "PADRAO"
  },
  "status": "ABERTO",
  "entrada": "2026-07-23T14:30:00",
  "saida": null,
  "valorTotal": null
}
```

### 6.3 Encerramento de ticket
```http
POST /v1/tickets/{id}/encerramento
Content-Type: application/json
```
Corpo opcional. Sem corpo, o servidor usa o instante atual:
```json
{ "saida": "2026-03-10T11:30:00" }
```

**Resposta — `200 OK`**
```json
{
  "id": "b7c4d1e2-9a03-4f55-8e21-77aa10bc4d31",
  "placa": "ABC1D23",
  "tipoVeiculo": "CARRO",
  "plano": "AVULSO",
  "planoAplicado": "AVULSO",
  "cliente": null,
  "vaga": {
    "id": "3f1a9c2e-5b7d-4a11-9c3e-8d2f6b4a1c00",
    "codigo": "A-01",
    "tipo": "PADRAO"
  },
  "status": "ENCERRADO",
  "entrada": "2026-07-23T14:30:00",
  "saida": "2026-07-23T18:05:00",
  "permanenciaMinutos": 215,
  "valorTotal": "25.00"
}
```

### 6.4 Alteração de status da vaga
```http
PUT /v1/vagas/{id}/status
Content-Type: application/json
```
```json
{ "status": "MANUTENCAO" }
```

### 6.5 Vaga
```json
{
  "id": "3f1a9c2e-5b7d-4a11-9c3e-8d2f6b4a1c00",
  "codigo": "A-01",
  "tipo": "PADRAO",
  "status": "LIVRE"
}
```

### 6.6 Cliente
```json
{
  "id": "9c2e3f1a-4a11-5b7d-8d2f-6b4a1c003f1a",
  "nome": "Maria Silva",
  "documento": "12345678901",
  "assinaturaValidaAte": "2026-12-31",
  "assinaturaVigente": true
}
```

### 6.7 Erro (formato de referência)
O formato é **livre**, mas deve ser **consistente em toda a API**. Sugestão:
```json
{
  "status": 422,
  "codigo": "VAGA_INCOMPATIVEL",
  "mensagem": "A vaga A-01 (PADRAO) nao acomoda veiculos do tipo VAN.",
  "path": "/v1/tickets",
  "erros": []
}
```

**Aderência ao contrato faz parte da avaliação.** Nomes de campos, nomes de enums e status codes divergentes deste documento serão considerados desvio, salvo se justificados no README.

---

## 7. Requisitos Não Funcionais (RNF)

* **[RNF01] Tratamento de erros:** erros estruturados e consistentes, via handler global (`@RestControllerAdvice`). **Stack traces, mensagens de SQL e exceções internas nunca devem ser expostos ao cliente.**
* **[RNF02] Padrões REST:** verbos e status codes semanticamente corretos. `200`, `201`, `400`, `404`, `409` e `422` devem ser usados de forma coerente.
* **[RNF03] Validação:** validação declarativa de entrada (Bean Validation). Erros de validação retornam `400` listando **todos** os campos inválidos, não apenas o primeiro.
* **[RNF04] Build:** o projeto sobe com um único comando documentado no README, sem passos manuais extras.
* **[RNF05] Repositório:** sem segredos, sem `target/`/`build/` versionados, `.gitignore` adequado.

---

## 8. Critérios de Destaque (opcionais)

Nenhum item abaixo é obrigatório e a ausência de todos **não reprova**. Implemente apenas se o obrigatório estiver sólido. Um item mal feito pesa mais negativamente do que sua ausência.

| # | Item | O que consideramos "bem feito" |
| :--- | :--- | :--- |
| 1 | **Testes unitários das políticas de cobrança** | Os 12 casos da tabela de referência do RF05 cobertos, com atenção especial aos pares de fronteira (15 vs. 16 minutos, 09:00 vs. 09:01, virada de dia). É o destaque de maior peso. |
| 2 | **Diagrama de classes** | Reflete a **entrega final**, não o rascunho inicial. Versionado no repositório (Mermaid no README, PlantUML ou imagem em `/docs`). Diagrama que contradiz o código conta negativamente. |
| 3 | **Diagrama de sequência** | Do fluxo de encerramento do ticket, mostrando as camadas envolvidas. |
| 4 | **Docker** | `Dockerfile` da aplicação **e** `docker-compose.yml` substituindo o H2 por um banco real, com a aplicação subindo por completo em um comando. Apenas empacotar o jar acrescenta pouco. |
| 5 | **Documentação OpenAPI/Swagger** | Endpoints descritos, exemplos e códigos de resposta documentados. |
| 6 | **Migrations versionadas** | Flyway ou Liquibase. |

---

## 9. Entregáveis

1. **Repositório público no GitHub** com o código-fonte.
2. **README.md** contendo, no mínimo:
   * como rodar o projeto (pré-requisitos e comandos);
   * como rodar os testes, se houver;
   * decisões técnicas e o porquê de cada uma;
   * premissas assumidas diante de ambiguidades deste documento;
   * o formato de erro e o formato de paginação escolhidos;
   * o que ficou de fora e o motivo;
   * declaração de uso de IA (quais partes, quais ferramentas).
3. **Coleção de exemplos de requisição** (Postman, Insomnia, arquivo `.http` ou `curl` no README).
4. **Histórico de commits incremental.** Um único commit com o projeto inteiro é avaliado negativamente, queremos ver a evolução do raciocínio.

---

## 10. Checklist Final (antes de enviar)

- [ ] O projeto compila e sobe com o comando descrito no README.
- [ ] Os 12 casos da tabela de referência do RF05 produzem os valores esperados (verificáveis via API, informando `entrada` e `saida`).
- [ ] Todos os endpoints retornam status codes coerentes, inclusive nos erros.
- [ ] Nenhum endpoint devolve stack trace.
- [ ] Não existe `System.out.println` ou código comentado esquecido. Usar logger é válido e não precisa ser removido.
- [ ] O README explica decisões, premissas e o que ficou de fora.
- [ ] O histórico de commits conta a história do desenvolvimento.
- [ ] Você consegue explicar cada decisão do projeto.
