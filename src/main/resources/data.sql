-- Dados ficticios para demonstracao academica.
-- A senha em todas as contas de demonstracao e "password" (hash BCrypt).

INSERT IGNORE INTO perfis (codigo, nome, descricao) VALUES
    ('ADMIN', 'Administrador', 'Acesso administrativo ao sistema.'),
    ('LEITOR', 'Leitor de Contadores', 'Regista e consulta leituras dos contadores.'),
    ('CLIENTE', 'Cliente', 'Consulta ligacoes, consumos e facturas.');

INSERT IGNORE INTO utilizadores
    (id, nome, nome_utilizador, email, senha_hash, ativo)
VALUES
    (UUID_TO_BIN(UUID()), 'Administrador do Sistema', 'admin',
     'admin@abastecimento.example.test',
     '$2a$10$KJ5tXxBbOi1xed/ZLRbyTOyuJzekbhKVneyrotLSMTCXrJ1RRBdYq', TRUE),
    (UUID_TO_BIN(UUID()), 'Maria Isabel Macuacua', 'maria.leitora',
     'maria.leitora@abastecimento.example.test',
     '$2a$10$KJ5tXxBbOi1xed/ZLRbyTOyuJzekbhKVneyrotLSMTCXrJ1RRBdYq', TRUE),
    (UUID_TO_BIN(UUID()), 'Helena Teresa Cossa', 'helena.cliente',
     'helena.cossa@abastecimento.example.test',
     '$2a$10$KJ5tXxBbOi1xed/ZLRbyTOyuJzekbhKVneyrotLSMTCXrJ1RRBdYq', TRUE),
    (UUID_TO_BIN(UUID()), 'Joao Ernesto Mucavele', 'joao.cliente',
     'joao.mucavele@abastecimento.example.test',
     '$2a$10$KJ5tXxBbOi1xed/ZLRbyTOyuJzekbhKVneyrotLSMTCXrJ1RRBdYq', TRUE);

INSERT IGNORE INTO clientes (id, nome, documento, email, telefone) VALUES
    (UUID_TO_BIN(UUID()), 'Helena Teresa Cossa', 'BI-DEMO-0001',
     'helena.cossa@cliente.example.test', '+258 84 100 0001'),
    (UUID_TO_BIN(UUID()), 'Joao Ernesto Mucavele', 'BI-DEMO-0002',
     'joao.mucavele@cliente.example.test', '+258 85 100 0002'),
    (UUID_TO_BIN(UUID()), 'Amelia Salomao Nhantumbo', 'BI-DEMO-0003',
     'amelia.nhantumbo@cliente.example.test', '+258 86 100 0003'),
    (UUID_TO_BIN(UUID()), 'Domingos Alberto Mabote', 'BI-DEMO-0004',
     'domingos.mabote@cliente.example.test', '+258 87 100 0004'),
    (UUID_TO_BIN(UUID()), 'Celina Rosa Chissano', 'BI-DEMO-0005',
     'celina.chissano@cliente.example.test', '+258 84 100 0005'),
    (UUID_TO_BIN(UUID()), 'Ernesto Joaquim Bila', 'BI-DEMO-0006',
     'ernesto.bila@cliente.example.test', '+258 85 100 0006');

INSERT IGNORE INTO utilizador_perfis (utilizador_id, perfil_id)
SELECT u.id, p.id
FROM utilizadores u
JOIN perfis p ON p.codigo = 'ADMIN'
WHERE u.nome_utilizador = 'admin';

INSERT IGNORE INTO utilizador_perfis (utilizador_id, perfil_id)
SELECT u.id, p.id
FROM utilizadores u
JOIN perfis p ON p.codigo = 'LEITOR'
WHERE u.nome_utilizador = 'maria.leitora';

INSERT IGNORE INTO utilizador_perfis (utilizador_id, perfil_id)
SELECT u.id, p.id
FROM utilizadores u
JOIN perfis p ON p.codigo = 'CLIENTE'
WHERE u.nome_utilizador IN ('helena.cliente', 'joao.cliente');

INSERT IGNORE INTO cliente_utilizadores (cliente_id, utilizador_id)
SELECT c.id, u.id
FROM clientes c
JOIN utilizadores u ON u.nome_utilizador = 'helena.cliente'
WHERE c.documento = 'BI-DEMO-0001';

INSERT IGNORE INTO cliente_utilizadores (cliente_id, utilizador_id)
SELECT c.id, u.id
FROM clientes c
JOIN utilizadores u ON u.nome_utilizador = 'joao.cliente'
WHERE c.documento = 'BI-DEMO-0002';

INSERT IGNORE INTO ligacoes
    (id, cliente_id, codigo, endereco, bairro, cidade, provincia, data_activacao, estado)
SELECT UUID_TO_BIN(UUID()), c.id, 'LIG-MAP-0001',
       'Rua da Imprensa, 125', 'Polana', 'Maputo', 'Maputo Cidade',
       '2024-02-12', 'ACTIVA'
FROM clientes c
WHERE c.documento = 'BI-DEMO-0001';

INSERT IGNORE INTO ligacoes
    (id, cliente_id, codigo, endereco, bairro, cidade, provincia, data_activacao, estado)
SELECT UUID_TO_BIN(UUID()), c.id, 'LIG-MAP-0002',
       'Avenida Julius Nyerere, 840', 'Maxaquene', 'Maputo', 'Maputo Cidade',
       '2023-08-03', 'ACTIVA'
FROM clientes c
WHERE c.documento = 'BI-DEMO-0002';

INSERT IGNORE INTO ligacoes
    (id, cliente_id, codigo, endereco, bairro, cidade, provincia, data_activacao, estado)
SELECT UUID_TO_BIN(UUID()), c.id, 'LIG-MAP-0003',
       'Rua dos Cajueiros, 48', 'Zimpeto', 'Maputo', 'Maputo Cidade',
       '2024-05-20', 'ACTIVA'
FROM clientes c
WHERE c.documento = 'BI-DEMO-0003';

INSERT IGNORE INTO ligacoes
    (id, cliente_id, codigo, endereco, bairro, cidade, provincia, data_activacao, estado)
SELECT UUID_TO_BIN(UUID()), c.id, 'LIG-MAP-0004',
       'Rua da Resistência, 310', 'Sommerschield', 'Maputo', 'Maputo Cidade',
       '2022-11-15', 'ACTIVA'
FROM clientes c
WHERE c.documento = 'BI-DEMO-0004';

INSERT IGNORE INTO ligacoes
    (id, cliente_id, codigo, endereco, bairro, cidade, provincia, data_activacao, estado)
SELECT UUID_TO_BIN(UUID()), c.id, 'LIG-MAP-0005',
       'Avenida de Mocambique, 1660', 'Malhangalene', 'Maputo', 'Maputo Cidade',
       '2023-04-08', 'ACTIVA'
FROM clientes c
WHERE c.documento = 'BI-DEMO-0005';

INSERT IGNORE INTO ligacoes
    (id, cliente_id, codigo, endereco, bairro, cidade, provincia, data_activacao, estado)
SELECT UUID_TO_BIN(UUID()), c.id, 'LIG-MAT-0006',
       'Rua da Mozal, 72', 'Matola A', 'Matola', 'Maputo Provincia',
       '2024-01-29', 'ACTIVA'
FROM clientes c
WHERE c.documento = 'BI-DEMO-0006';

INSERT IGNORE INTO contadores
    (id, ligacao_id, numero_serie, modelo, data_instalacao, estado)
SELECT UUID_TO_BIN(UUID()), l.id, 'H2O-DEMO-0001',
       'Itron Aquadis+', '2024-02-12', 'ACTIVO'
FROM ligacoes l
WHERE l.codigo = 'LIG-MAP-0001';

INSERT IGNORE INTO contadores
    (id, ligacao_id, numero_serie, modelo, data_instalacao, estado)
SELECT UUID_TO_BIN(UUID()), l.id, 'H2O-DEMO-0002',
       'Sensus 620C', '2023-08-03', 'ACTIVO'
FROM ligacoes l
WHERE l.codigo = 'LIG-MAP-0002';

INSERT IGNORE INTO contadores
    (id, ligacao_id, numero_serie, modelo, data_instalacao, estado)
SELECT UUID_TO_BIN(UUID()), l.id, 'H2O-DEMO-0003',
       'Itron Aquadis+', '2024-05-20', 'ACTIVO'
FROM ligacoes l
WHERE l.codigo = 'LIG-MAP-0003';

INSERT IGNORE INTO contadores
    (id, ligacao_id, numero_serie, modelo, data_instalacao, estado)
SELECT UUID_TO_BIN(UUID()), l.id, 'H2O-DEMO-0004',
       'Diehl Metering ALTAIR', '2022-11-15', 'ACTIVO'
FROM ligacoes l
WHERE l.codigo = 'LIG-MAP-0004';

INSERT IGNORE INTO contadores
    (id, ligacao_id, numero_serie, modelo, data_instalacao, estado)
SELECT UUID_TO_BIN(UUID()), l.id, 'H2O-DEMO-0005',
       'Sensus 620C', '2023-04-08', 'ACTIVO'
FROM ligacoes l
WHERE l.codigo = 'LIG-MAP-0005';

INSERT IGNORE INTO contadores
    (id, ligacao_id, numero_serie, modelo, data_instalacao, estado)
SELECT UUID_TO_BIN(UUID()), l.id, 'H2O-DEMO-0006',
       'Itron Aquadis+', '2024-01-29', 'ACTIVO'
FROM ligacoes l
WHERE l.codigo = 'LIG-MAT-0006';

INSERT IGNORE INTO tarifas
    (id, nome, descricao, taxa_fixa, preco_por_m3, vigente_desde, activa)
VALUES
    (UUID_TO_BIN(UUID()), 'Residencial', 'Tarifa residencial de demonstracao.',
     150.00, 70.00, '2025-01-01', TRUE),
    (UUID_TO_BIN(UUID()), 'Comercial', 'Tarifa comercial de demonstracao.',
     500.00, 120.00, '2025-01-01', TRUE);

-- Leituras de demonstracao
INSERT IGNORE INTO leituras
    (id, contador_id, utilizador_id, data_leitura, valor_m3, tipo, observacao)
SELECT UUID_TO_BIN(UUID()), c.id, u.id, '2026-01-01 08:00:00', 0.000, 'REAL', 'Leitura inicial de instalacao'
FROM contadores c
JOIN utilizadores u ON u.nome_utilizador = 'maria.leitora'
WHERE c.numero_serie = 'H2O-DEMO-0001';

INSERT IGNORE INTO leituras
    (id, contador_id, utilizador_id, data_leitura, valor_m3, tipo, observacao)
SELECT UUID_TO_BIN(UUID()), c.id, u.id, '2026-02-01 09:30:00', 14.500, 'REAL', 'Leitura mensal regular'
FROM contadores c
JOIN utilizadores u ON u.nome_utilizador = 'maria.leitora'
WHERE c.numero_serie = 'H2O-DEMO-0001';

INSERT IGNORE INTO leituras
    (id, contador_id, utilizador_id, data_leitura, valor_m3, tipo, observacao)
SELECT UUID_TO_BIN(UUID()), c.id, u.id, '2026-03-01 10:15:00', 31.800, 'REAL', 'Leitura mensal regular'
FROM contadores c
JOIN utilizadores u ON u.nome_utilizador = 'maria.leitora'
WHERE c.numero_serie = 'H2O-DEMO-0001';

INSERT IGNORE INTO leituras
    (id, contador_id, utilizador_id, data_leitura, valor_m3, tipo, observacao)
SELECT UUID_TO_BIN(UUID()), c.id, u.id, '2026-01-01 08:30:00', 0.000, 'REAL', 'Leitura inicial de instalacao'
FROM contadores c
JOIN utilizadores u ON u.nome_utilizador = 'maria.leitora'
WHERE c.numero_serie = 'H2O-DEMO-0002';

INSERT IGNORE INTO leituras
    (id, contador_id, utilizador_id, data_leitura, valor_m3, tipo, observacao)
SELECT UUID_TO_BIN(UUID()), c.id, u.id, '2026-02-01 11:00:00', 22.000, 'REAL', 'Leitura mensal regular'
FROM contadores c
JOIN utilizadores u ON u.nome_utilizador = 'maria.leitora'
WHERE c.numero_serie = 'H2O-DEMO-0002';

