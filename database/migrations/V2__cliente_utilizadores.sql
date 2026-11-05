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
