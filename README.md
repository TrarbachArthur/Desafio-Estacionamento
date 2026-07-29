# API de Gestão de Estacionamento

## Como rodar o projeto

Pré-requisito: JDK 21 instalado no ambiente

```bash
./mvnw spring-boot:run
```

Com a execução do comando, a API sobe em `http://localhost:8080`, sem nenhum passo adicional.

## Como rodar os testes

Os testes são executados manualmente. O projeto está configurado para inicializar o banco de dados com valores pré-determinados, para facilitar a validação dos requisitos, mas essa configuração não deve ser refletida, caso o projeto fosse colocado em produção.

Para a execução dos testes, são disponibilizados arquivos .http dentro da pasta /examples/ com diversas requisições, basta executar o projeto e executar cada uma das requisições disponíveis. Cada arquivo funciona independentemente, mas é recomendado que sejam executadas todas as requisições de um mesmo arquivo em ordem sequencial, para evitar resultados inesperados. Por conveniência, os arquivos foram separados entre os requisitos funcionais solicitados, já que foi esse o fluxo de desenvolvimento escolhido.

## Decisões técnicas

### Utilização de JPQL

Para possibilitar a aplicação de filtros nas consultas, optei pelo uso de JPQL em vez de JPA Specification. Essa escolha teve como principal objetivo a simplicidade do projeto, também considerando que escalabilidade não é avaliado.

O uso de JPQL elimina a necessidade de criação de novos arquivos e atende muito bem às necessidades do projeto, já que os filtros são fixos e as queries são simples.

## Premissas assumidas

1. **Documento** é validado apenas pelo formato, conforme especificado, não considerei validação como CPF.

2. **AssinaturaValidaAte** no passado é permitada também para atualizações de cliente. Isso permite correções em um possível erro de registro, além de não impactar o funcionamento do sistema, visto que o cadastro com assinatura vencida é válido. 

3. **POST /v1/clientes/** retorna 201 Created, com header Location, mantendo o padrão dos outros endpoints da API.
4. **Retorno de criação**: seguindo o padrão do RF03, o cadastro de vagas e clientes também retornam o respectivo objeto criado no body da resposta. Facilitando, também, a verificação do funcionamento correto dos endpoints.
5. **GET /v1/vagas não paginado.** Não foi requerido pela especificação e, diferentemente dos tickets, o número de vagas não cresce infinitamente no tempo, permitindo o retorno não paginado e reduzindo complexidade no começo do desenvolvimento do projeto.
6. **Cliente dentro de TicketResponse**: a especificação não traz exemplos onde o cliente não é nulo, por isso, criei um novo arquivo de response, permitindo manipular mais facilmente quais informações do cliente seriam retornadas junto ao ticket. Foram escolhidas informações parciais, suficientes para identificar o cliente.


## Formatos de erro e paginação

Ainda não implementado

## O que ficou de fora

* RF04
* RF05
* RF06
* RNF01
* Possíveis descumprimentos do contrato da API, será feita checagem antes do prazo final de entrega, mas é necessário implementar RNF01.
* Critérios de Destaque

Todos os componentes listados acima não foram implementados, devido ao pouco tempo para o desenvolvimento do projeto, considerando a pouca experiência que tinha com Spring Boot e outras tecnologias utilizadas.

## Uso de IA

**Não foi utilizado IA para o desenvolvimento de nenhuma parte do código do projeto**

No sentido de desenvolvimento, o uso de inteligência artificial se restringiu a estudo das tecnologias utilizadas e pesquisa de possíveis decisões técnicas, assim como seus trade-offs.

Fora do desenvolvimento direto do projeto, foi utiliza IA (Claude) para acelerar processos repetitivos, sendo eles:

 * Desenvolvimento dos arquivos .http, buscando testar todas as possibilidades listadas na especificação, além de algumas premissas assumidas.
 * Geração do .sql para carga inicial do banco de dados, utilizado exclusivamente para testes manuais.