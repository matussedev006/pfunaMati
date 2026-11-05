CREATE DATABASE IF NOT EXISTS abastecimento
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE abastecimento;

CREATE TABLE IF NOT EXISTS clientes (
    id BINARY(16) NOT NULL,
    nome VARCHAR(120) NOT NULL,
    documento VARCHAR(40) NOT NULL,
    email VARCHAR(160) NOT NULL,
    telefone VARCHAR(30) NULL,
    data_registo DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_clientes_documento UNIQUE (documento)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS perfis (
    id BIGINT NOT NULL AUTO_INCREMENT,
    codigo VARCHAR(30) NOT NULL,
    nome VARCHAR(80) NOT NULL,
    descricao VARCHAR(255) NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_perfis_codigo UNIQUE (codigo),
    CONSTRAINT uk_perfis_nome UNIQUE (nome)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS utilizadores (
    id BINARY(16) NOT NULL,
    nome VARCHAR(120) NOT NULL,
    nome_utilizador VARCHAR(60) NOT NULL,
    email VARCHAR(160) NOT NULL,
    senha_hash VARCHAR(255) NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    data_criacao DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_utilizadores_nome_utilizador UNIQUE (nome_utilizador),
    CONSTRAINT uk_utilizadores_email UNIQUE (email)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS utilizador_perfis (
    utilizador_id BINARY(16) NOT NULL,
    perfil_id BIGINT NOT NULL,
    PRIMARY KEY (utilizador_id, perfil_id),
    CONSTRAINT fk_utilizador_perfis_utilizador
        FOREIGN KEY (utilizador_id) REFERENCES utilizadores (id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_utilizador_perfis_perfil
        FOREIGN KEY (perfil_id) REFERENCES perfis (id)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS cliente_utilizadores (
    cliente_id BINARY(16) NOT NULL,
    utilizador_id BINARY(16) NOT NULL,
    PRIMARY KEY (cliente_id, utilizador_id),
    CONSTRAINT fk_cliente_utilizadores_cliente
        FOREIGN KEY (cliente_id) REFERENCES clientes (id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_cliente_utilizadores_utilizador
        FOREIGN KEY (utilizador_id) REFERENCES utilizadores (id)
        ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS ligacoes (
    id BINARY(16) NOT NULL,
    cliente_id BINARY(16) NOT NULL,
    codigo VARCHAR(40) NOT NULL,
    endereco VARCHAR(200) NOT NULL,
    bairro VARCHAR(100) NOT NULL,
    cidade VARCHAR(100) NOT NULL,
    provincia VARCHAR(100) NULL,
    data_activacao DATE NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVA',
    data_criacao DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_ligacoes_codigo UNIQUE (codigo),
    CONSTRAINT fk_ligacoes_cliente
        FOREIGN KEY (cliente_id) REFERENCES clientes (id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT ck_ligacoes_estado
        CHECK (estado IN ('ACTIVA', 'SUSPENSA', 'ENCERRADA')),
    INDEX ix_ligacoes_cliente (cliente_id),
    INDEX ix_ligacoes_estado (estado)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS contadores (
    id BINARY(16) NOT NULL,
    ligacao_id BINARY(16) NOT NULL,
    numero_serie VARCHAR(60) NOT NULL,
    modelo VARCHAR(100) NULL,
    data_instalacao DATE NOT NULL,
    data_remocao DATE NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVO',
    PRIMARY KEY (id),
    CONSTRAINT uk_contadores_numero_serie UNIQUE (numero_serie),
    CONSTRAINT fk_contadores_ligacao
        FOREIGN KEY (ligacao_id) REFERENCES ligacoes (id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT ck_contadores_estado
        CHECK (estado IN ('ACTIVO', 'REMOVIDO', 'AVARIADO')),
    CONSTRAINT ck_contadores_datas
        CHECK (data_remocao IS NULL OR data_remocao >= data_instalacao),
    INDEX ix_contadores_ligacao (ligacao_id),
    INDEX ix_contadores_estado (estado)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS leituras (
    id BINARY(16) NOT NULL,
    contador_id BINARY(16) NOT NULL,
    utilizador_id BINARY(16) NULL,
    data_leitura DATETIME(6) NOT NULL,
    valor_m3 DECIMAL(12, 3) NOT NULL,
    tipo VARCHAR(20) NOT NULL DEFAULT 'REAL',
    observacao VARCHAR(500) NULL,
    data_registo DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_leituras_contador_data UNIQUE (contador_id, data_leitura),
    CONSTRAINT uk_leituras_id_contador UNIQUE (id, contador_id),
    CONSTRAINT fk_leituras_contador
        FOREIGN KEY (contador_id) REFERENCES contadores (id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_leituras_utilizador
        FOREIGN KEY (utilizador_id) REFERENCES utilizadores (id)
        ON UPDATE CASCADE ON DELETE SET NULL,
    CONSTRAINT ck_leituras_valor CHECK (valor_m3 >= 0),
    CONSTRAINT ck_leituras_tipo CHECK (tipo IN ('REAL', 'ESTIMADA')),
    INDEX ix_leituras_data (data_leitura)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS tarifas (
    id BINARY(16) NOT NULL,
    nome VARCHAR(100) NOT NULL,
    descricao VARCHAR(255) NULL,
    taxa_fixa DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    preco_por_m3 DECIMAL(12, 2) NOT NULL,
    vigente_desde DATE NOT NULL,
    vigente_ate DATE NULL,
    activa BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (id),
    CONSTRAINT uk_tarifas_nome_inicio UNIQUE (nome, vigente_desde),
    CONSTRAINT ck_tarifas_valores
        CHECK (taxa_fixa >= 0 AND preco_por_m3 >= 0),
    CONSTRAINT ck_tarifas_vigencia
        CHECK (vigente_ate IS NULL OR vigente_ate >= vigente_desde),
    INDEX ix_tarifas_activa_vigencia (activa, vigente_desde, vigente_ate)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS consumos (
    id BINARY(16) NOT NULL,
    ligacao_id BINARY(16) NOT NULL,
    contador_id BINARY(16) NOT NULL,
    leitura_inicial_id BINARY(16) NOT NULL,
    leitura_final_id BINARY(16) NOT NULL,
    periodo_inicio DATE NOT NULL,
    periodo_fim DATE NOT NULL,
    volume_m3 DECIMAL(12, 3) NOT NULL,
    data_calculo DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_consumos_periodo UNIQUE (contador_id, periodo_inicio, periodo_fim),
    CONSTRAINT fk_consumos_ligacao
        FOREIGN KEY (ligacao_id) REFERENCES ligacoes (id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_consumos_contador
        FOREIGN KEY (contador_id) REFERENCES contadores (id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_consumos_leitura_inicial
        FOREIGN KEY (leitura_inicial_id, contador_id)
        REFERENCES leituras (id, contador_id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_consumos_leitura_final
        FOREIGN KEY (leitura_final_id, contador_id)
        REFERENCES leituras (id, contador_id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT ck_consumos_periodo CHECK (periodo_fim > periodo_inicio),
    CONSTRAINT ck_consumos_volume CHECK (volume_m3 >= 0),
    INDEX ix_consumos_ligacao_periodo (ligacao_id, periodo_inicio, periodo_fim)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS facturas (
    id BINARY(16) NOT NULL,
    numero VARCHAR(40) NOT NULL,
    cliente_id BINARY(16) NOT NULL,
    ligacao_id BINARY(16) NOT NULL,
    consumo_id BINARY(16) NOT NULL,
    tarifa_id BINARY(16) NOT NULL,
    periodo_inicio DATE NOT NULL,
    periodo_fim DATE NOT NULL,
    data_emissao DATE NOT NULL,
    data_vencimento DATE NOT NULL,
    valor_consumo DECIMAL(12, 2) NOT NULL,
    taxa_fixa DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    valor_total DECIMAL(12, 2) NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'PENDENTE',
    data_criacao DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_facturas_numero UNIQUE (numero),
    CONSTRAINT uk_facturas_consumo UNIQUE (consumo_id),
    CONSTRAINT fk_facturas_cliente
        FOREIGN KEY (cliente_id) REFERENCES clientes (id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_facturas_ligacao
        FOREIGN KEY (ligacao_id) REFERENCES ligacoes (id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_facturas_consumo
        FOREIGN KEY (consumo_id) REFERENCES consumos (id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_facturas_tarifa
        FOREIGN KEY (tarifa_id) REFERENCES tarifas (id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT ck_facturas_valores
        CHECK (valor_consumo >= 0 AND taxa_fixa >= 0 AND valor_total >= 0),
    CONSTRAINT ck_facturas_datas
        CHECK (data_vencimento >= data_emissao AND periodo_fim > periodo_inicio),
    CONSTRAINT ck_facturas_estado
        CHECK (estado IN ('PENDENTE', 'PAGA', 'PARCIAL', 'VENCIDA', 'ANULADA')),
    INDEX ix_facturas_cliente (cliente_id),
    INDEX ix_facturas_estado_vencimento (estado, data_vencimento),
    INDEX ix_facturas_periodo (periodo_inicio, periodo_fim)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS pagamentos (
    id BINARY(16) NOT NULL,
    factura_id BINARY(16) NOT NULL,
    utilizador_id BINARY(16) NULL,
    valor DECIMAL(12, 2) NOT NULL,
    data_pagamento DATETIME(6) NOT NULL,
    metodo VARCHAR(20) NOT NULL,
    referencia VARCHAR(100) NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'CONFIRMADO',
    observacao VARCHAR(500) NULL,
    data_registo DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_pagamentos_metodo_referencia UNIQUE (metodo, referencia),
    CONSTRAINT fk_pagamentos_factura
        FOREIGN KEY (factura_id) REFERENCES facturas (id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_pagamentos_utilizador
        FOREIGN KEY (utilizador_id) REFERENCES utilizadores (id)
        ON UPDATE CASCADE ON DELETE SET NULL,
    CONSTRAINT ck_pagamentos_valor CHECK (valor > 0),
    CONSTRAINT ck_pagamentos_metodo
        CHECK (metodo IN ('DINHEIRO', 'TRANSFERENCIA', 'MPESA', 'EMOLA', 'CARTAO', 'OUTRO')),
    CONSTRAINT ck_pagamentos_estado CHECK (estado IN ('CONFIRMADO', 'ESTORNADO')),
    INDEX ix_pagamentos_factura_data (factura_id, data_pagamento)
) ENGINE = InnoDB;
