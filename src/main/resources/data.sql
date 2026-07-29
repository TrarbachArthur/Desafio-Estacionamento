-- Seed de apoio a avaliacao manual.
--
-- O H2 roda em memoria e perde tudo a cada restart. Sem dados previsiveis, reproduzir os 12 casos
-- da tabela de referencia do RF05 exigiria criar vaga e cliente e copiar os UUIDs gerados a cada
-- execucao. Com IDs literais, tanto docs/requests/estacionamento.http quanto
-- docs/requests/verificar-rf05.sh viram scripts fixos, executaveis quantas vezes for preciso.
--
-- Requer spring.jpa.defer-datasource-initialization: true (ver application.properties), senao este
-- arquivo roda antes de o Hibernate criar as tabelas.
--
-- Este seed existe para facilitar a verificacao do desafio. Em um cenario real ele nao iria para
-- o profile de producao.

-- Uma vaga de cada tipo, todas LIVRE.
insert into vaga (id, codigo, tipo, status) values
  ('11111111-1111-1111-1111-111111111111', 'M-01', 'MOTO',   'LIVRE'),
  ('22222222-2222-2222-2222-222222222222', 'A-01', 'PADRAO', 'LIVRE'),
  ('33333333-3333-3333-3333-333333333333', 'G-01', 'GRANDE', 'LIVRE');

-- Vagas extra do tipo PADRAO: evitar problemas com tickets abertos
insert into vaga (id, codigo, tipo, status) values
  ('22222222-2222-2222-2222-000000000002', 'A-02', 'PADRAO', 'LIVRE'),
  ('22222222-2222-2222-2222-000000000003', 'A-03', 'PADRAO', 'LIVRE'),
  ('22222222-2222-2222-2222-000000000004', 'A-04', 'PADRAO', 'LIVRE'),
  ('22222222-2222-2222-2222-000000000005', 'A-05', 'PADRAO', 'LIVRE'),
  ('22222222-2222-2222-2222-000000000006', 'A-06', 'PADRAO', 'LIVRE'),
  ('22222222-2222-2222-2222-000000000007', 'A-07', 'PADRAO', 'LIVRE'),
  ('22222222-2222-2222-2222-000000000008', 'A-08', 'PADRAO', 'LIVRE'),
  ('22222222-2222-2222-2222-000000000009', 'A-09', 'PADRAO', 'LIVRE'),
  ('22222222-2222-2222-2222-000000000010', 'A-10', 'PADRAO', 'LIVRE'),
  ('22222222-2222-2222-2222-000000000011', 'A-11', 'PADRAO', 'LIVRE'),
  ('22222222-2222-2222-2222-000000000012', 'A-12', 'PADRAO', 'LIVRE');

-- Uma vaga OCUPADA, para demonstrar o 409 ao tentar alterar para MANUTENCAO (RF01).
insert into vaga (id, codigo, tipo, status) values
  ('44444444-4444-4444-4444-444444444443', 'X-98', 'GRANDE', 'OCUPADA');

-- Uma vaga em MANUTENCAO, para demonstrar o 409 distinto de "ocupada" (RF03.3).
insert into vaga (id, codigo, tipo, status) values
  ('44444444-4444-4444-4444-444444444444', 'X-99', 'GRANDE', 'MANUTENCAO');

-- Cliente com assinatura VIGENTE: caso 11 da tabela (valor 0.00).
-- Data distante para que o caso permaneca valido independentemente de quando o projeto for avaliado.
insert into cliente (id, nome, documento, assinatura_valida_ate) values
  ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'Maria Silva', '12345678901', '2099-12-31');

-- Cliente com assinatura VENCIDA: caso 12 da tabela (cobra como AVULSO, planoAplicado = AVULSO).
-- A data e anterior a entrada de 2026-03-10 usada nos cenarios.
insert into cliente (id, nome, documento, assinatura_valida_ate) values
  ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'Joao Vencido', '98765432100', '2026-01-31');
