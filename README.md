# API de Gestão de Estacionamento

## Como rodar o projeto

Pré-requisito: JDK 21 instalado no ambiente

```bash
./mvnw spring-boot:run
```

Com a execução do comando, a API sobe em `http://localhost:8080`, sem nenhum passo adicional.

## Como rodar os testes

Os testes são executados manualmente. O projeto está configurado para inicializar o banco de dados com valores pré-determinados, para facilitar a validação dos requisitos, mas essa configuração não deve ser refletida, caso o projeto fosse colocado em produção (pode ser desativada com uma configuração em ```application.properties```). Essa decisão é totalmente baseada no uso de banco em memória, que impede que valores sejam mantidos entre execuções. Em um projeto devidamente finalizado, o H2 seria substituido por um banco real, que permitiria testes mais consistentes, sem a necessidade dessa inicialização.

Para a execução dos testes, são disponibilizados arquivos .http dentro da pasta /examples/ com diversas requisições, basta executar o projeto e executar cada uma das requisições disponíveis. Cada arquivo funciona independentemente, mas é recomendado que sejam executadas todas as requisições de um mesmo arquivo em ordem sequencial, para evitar resultados inesperados. Por conveniência, os arquivos foram separados entre os requisitos funcionais solicitados, já que foi esse o fluxo de desenvolvimento escolhido.

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

## Formatos de erro e paginação

Ainda não implementado

## O que ficou de fora

* RF06
* RNF01
* Possíveis descumprimentos do contrato da API, será feita checagem antes do prazo final de entrega, mas é necessário implementar RNF01.
* Critérios de Destaque

Todos os componentes listados acima não foram implementados, devido ao pouco tempo para o desenvolvimento do projeto, considerando a pouca experiência que tinha com Spring Boot e outras tecnologias utilizadas.

## Uso de IA

**Não foi utilizado IA para o desenvolvimento de nenhuma parte do código do projeto**

No sentido de desenvolvimento, o uso de inteligência artificial se restringiu a estudo das tecnologias utilizadas e pesquisa de possíveis decisões técnicas, assim como os seus trade-offs, permitindo que decisões de projeto fossem definidas com mais assertividade.

Fora do desenvolvimento direto do projeto, foi utilizada IA (Claude) para acelerar processos repetitivos, sendo eles:

 * Desenvolvimento dos arquivos .http, buscando testar todas as possibilidades listadas na especificação, além de algumas premissas assumidas.
 * Geração do .sql para carga inicial do banco de dados, utilizado exclusivamente para testes manuais.