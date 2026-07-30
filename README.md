# API de Gestão de Estacionamento

## Como rodar o projeto

Pré-requisito: JDK 21 instalado no ambiente

```bash
./mvnw spring-boot:run
```

Com a execução do comando, a API sobe em `http://localhost:8080`, sem nenhum passo adicional.

Para desabilitar o inicialização do banco de dados, basta alterar o arquivo ```application.properties``` para que possua (```true``` por padrão no repositório) ```spring.jpa.defer-datasource-initialization=false```

## Como rodar os testes

### Testes unitários
Para executas os testes unitários, basta rodar:

```./mvnw clean verify``` para reempacotar o projeto e rodar testes unitários e de integração (não implementados), ou ```./mvnw test``` que compila o código e executa apenas os testes unitários.

### Testes manuais
Os testes são executados manualmente. O projeto está configurado para inicializar o banco de dados com valores pré-determinados, para facilitar a validação dos requisitos, mas essa configuração não deve ser refletida, caso o projeto fosse colocado em produção (pode ser desativada com uma configuração em ```application.properties```). Essa decisão é totalmente baseada no uso de banco em memória, que impede que valores sejam mantidos entre execuções. Em um projeto devidamente finalizado, o H2 seria substituido por um banco real, que permitiria testes mais consistentes, sem a necessidade dessa inicialização.

Para a execução dos testes, são disponibilizados arquivos .http dentro da pasta /examples/ com diversas requisições, basta executar o projeto e executar cada uma das requisições disponíveis. Cada arquivo funciona independentemente, mas é recomendado que sejam executadas todas as requisições de um mesmo arquivo em ordem sequencial, para evitar resultados inesperados. Por conveniência, os arquivos foram separados entre os requisitos funcionais solicitados, já que foi esse o fluxo de desenvolvimento escolhido.

Para melhor visualização dos resultados de ```rf06.http``` é recomendado preencher o banco através da execução de outros arquivos anteriores.

## Decisões técnicas

### Representação monetária (BigDecimal)

O BigDecimal foi escolhido como representação monetária pela simplicidade. Com a aplicação do MapStruct, já são fornecidos métodos de conversão entre BigDecimal e String (contrato API), que simplificam a implementação e reduzem a possibilidade de erro por esquecimento. Além disso, não existe um requerimento de performance, seja em relação a tempo de processamento ou utilização de memória/armazenamento.

Sem restrições de performance, cheguei a conclusão de que a praticidade tinha mais valor, considerando o escopo do desafio, bastando prestar atenção ao realizar operações com BigDecimal, para utilizar as funções corretamente.

### Utilização de JPQL

Para possibilitar a aplicação de filtros nas consultas, optei pelo uso de JPQL em vez de JPA Specification. Essa escolha teve como principal objetivo a simplicidade do projeto, também considerando que escalabilidade não é avaliado.

O uso de JPQL elimina a necessidade de criação de novos arquivos e atende muito bem às necessidades do projeto, já que os filtros são fixos e as queries são simples.

### Políticas de cobrança

A especificação traz como premissa a possibilidade do surgimento de novas políticas de cobrança (ex.: ```EVENTO```). Pensando nisso, as políticas foram implementadas através de uma interface (Strategy), que facilita a criação de novas políticas, sendo necessário apenas a definição de um novo plano (enum Plano) e do cálculo do valor cobrado.
Juntamente à Strategy foi implementada uma Factory, responsável pelo "roteamento" das cobranças, com base no plano.

## Premissas assumidas

1. **Documento** é validado apenas pelo formato, conforme especificado, não considerei validação como CPF.


2. **AssinaturaValidaAte** no passado é permitada também para atualizações de cliente. Isso permite correções em um possível erro de registro, além de não impactar o funcionamento do sistema, visto que o cadastro com assinatura vencida é válido.


3. **POST /v1/clientes/** retorna 201 Created, com header Location, mantendo o padrão dos outros endpoints da API.


4. **Retorno de criação**: seguindo o padrão do RF03, o cadastro de vagas e clientes também retornam o respectivo objeto criado no body da resposta. Facilitando, também, a verificação do funcionamento correto dos endpoints.


5. **GET /v1/vagas não paginado.** Não foi requerido pela especificação e, diferentemente dos tickets, o número de vagas não cresce infinitamente no tempo, permitindo o retorno não paginado e reduzindo complexidade no começo do desenvolvimento do projeto.


6. **Cliente dentro de TicketResponse**: a especificação não traz exemplos onde o cliente não é nulo, por isso, criei um novo arquivo de response, permitindo manipular mais facilmente quais informações do cliente seriam retornadas junto ao ticket. Foram escolhidas informações parciais, suficientes para identificar o cliente.


7. **Permanência ignora frações de minuto**. Um ticket com tempo de permanência de 15 minutos e 59 segundos considera tempo de permanência igual a 15 minutos. Comportamento intrínseco do ```Duration.toMinutes()```.


8. **permanenciaMinutos é ```null``` na abertura do ticket**. Na especificação, o campo não existe no momento da criação do ticket, porém, como campos nulos não influenciam na avaliação da resposta final (e podem ser ocultados), o campo foi mantido na resposta da criação de tickets.


9. **Tickets encerrados permitem novos tickets no mesmo horário**. Pensando no contexto do desafio, visando manter a simplicidade, e facilitar os testes, não foi considerada uma política que proibisse tickets "duplicados" (Ex.: mesma placa com mesmo horário de entrada).

## Formato de erro

A especificação solicita o uso de `@RestControllerAdvice`. Todas as decisões, e criação de exceções, foram baseadas na tentativa de manter o projeto organizado e facilmente ajustável.

O formato de erro escolhido é o mesmo sugerido pela especificação:

```json
{
  "status": 422,
  "codigo": "VAGA_INCOMPATIVEL",
  "mensagem": "A vaga A-01 nao atende ao tipo de veiculo VAN",
  "path": "/v1/tickets",
  "erros": []
}
```

Conforme solicitado em RNF03, erros de validação listam todos os campos com erro, não apenas o primeiro. Para garantir consistência, os campos listados são ordenados, pelo tipo do campo, e em sequência pela mensagem de erro em si.

Foram criados diversos códigos de erro (enum `CodigoErro`), para facilitar a padronização das respostas e evitar erros de digitação.

Nenhuma resposta expõe stack trace, SQL ou exceções internas (RNF01). Para evitar esse comportamente, foi necessário definir alguns outros tratadores de exceção, que lidam com erros vindos diretamente do Spring Boot, especialmente considerando que os erros, por padrão, trazem `timestamp`, que fugiria ao formato definido.



Para certificar que nenhum erro fugiria às regras de retorno, foi definido também um protocolo de fallback, que usa um Logger para registrar o erro internamente, e retorna `500`, sem expor erros internos, apenas reconhecendo o erro. Esse protocolo ajuda também a encontrar possíveis exceções que não foram corretamente tratadas, e que acabam caindo no fallback.

## Formato de paginação

Apenas ```GET /v1/tickets``` é paginado, por premissa assumida.

Foi criado um formato de paginação simples, porém específico para o projeto, para evitar a exposição de componentes internos nos endpoints.

Os parâmetros de paginaçãos são ```page``` (default 0) e ```size``` (default 10, máximo 50, configurável em ```application.properties```) que representam, respectivamente, a página a ser consultada e o tamanho de cada página.
Valores fora da faixa aceita são ajustados para os limites aceitos (```page >= 0``` e ```1 <= size <= 50```).

O retorno é ordenado de maneira decrescente, considerando a ```entrada```, tendo em vista que, ao observar um histórico, costuma ser mais relevante visualizar ocorrências mais recentes do que se busca.

Formato de paginação definido:
```json
{
  "conteudo": [
    {
      "id": "73330ee4-caa7-4322-8c6a-2e03c1addd3f",
      "placa": "ABC1M23",
      "tipoVeiculo": "CARRO",
      "plano": "MENSALISTA",
      "planoAplicado": "MENSALISTA",
      "cliente": {
        "id": "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
        "nome": "Maria Silva",
        "documento": "12345678901"
      },
      "vaga": {
        "id": "22222222-2222-2222-2222-000000000007",
        "codigo": "A-07",
        "tipo": "PADRAO"
      },
      "status": "ENCERRADO",
      "entrada": "2026-03-10T08:00:00",
      "saida": "2026-03-10T20:00:00",
      "permanenciaMinutos": 720,
      "valorTotal": "0.00"
    }
  ],
  "paginaAtual": 0,
  "tamanho": 10,
  "totalElementos": 1,
  "totalPaginas": 1,
  "ultimaPagina": true
}
```

## O que ficou de fora

Os critérios de destaque não foram implementados devido ao pouco tempo para execução do desafio, considerando a necessidade familiarização com algumas tecnologias, e os contratempos profissionais durante o período de realização do teste.

Em relação aos testes unitários, a política de cobrança já foi implementada pensando na implementação dos testes (motivo da presença do método ```de``` em ```ContextoCobranca```, para os casos em que ```cliente == null```), mas infelizmente o tempo não foi suficiente para finalizar a implementação.

## Uso de IA

**Não foi utilizado IA para o desenvolvimento de nenhuma parte do código do projeto**

No sentido de desenvolvimento, o uso de inteligência artificial se restringiu ao estudo das tecnologias utilizadas e pesquisa de possíveis decisões técnicas, assim como os seus trade-offs, permitindo que decisões de projeto fossem definidas com mais assertividade.

Entre os principais motivos de consulta a ferramentas de pesquisa/IA estão:
 * Boas práticas relacionadas às tecnologias aplicadas
 * Melhores decisões de arquitetura e seus trade-offs
 * Consulta de quais exceções precisariam ser tratadas, que não estavam diretamente listadas no código desenvolvido (funcionamento interno do Spring Boot)

Fora do desenvolvimento direto do projeto, foi utilizada IA (Claude) para acelerar processos repetitivos, sendo eles:

 * Desenvolvimento dos arquivos .http, buscando testar todas as possibilidades listadas na especificação, além de algumas premissas assumidas.
 * Geração do .sql para carga inicial do banco de dados, utilizado exclusivamente para testes manuais.
 * Revisão final do projeto, buscando detectar erros relevantes antes da entrega final. Utilização apenas para detecção do que pode precisar ser corrigido, sem sugestões ou alterações diretas (se houverem correções, terão commits prefixados por ai-fix)

## Decisões tomadas que poderiam ser diferentes

Algumas decisões tomadas durante o desenvolvimento do desafio se provaram não ideais, e eu provavelmente não as repetiria em uma nova oportunidade. São algumas delas:
* **Não implementação de um banco de dados "real"**. A não permanência dos dados no banco prejudicou muito a validação rápida de novas features. Acredito que o tempo investido repetindo testes básicos poderia ter sido remanejado para um setup inicial do projeto com um banco de dados real.
* **Demora para implementação da política de exceções**. A pouca familiaridade com o framework, juntamente com as curtas seções de desenvolvimento que fui capaz de realizar, me fizeram decidir por adiar a implementação do formato de erro e da política de exceções. Essa decisão dificultou muito a implementação tardia, que precisou levar em consideração as exceções "placeholder" que eu tinha utilizado antes. Esquecer de atualizar alguma exceção, juntamente com os testes demorados (ponto anterior), tornou a implementação muito lenta.