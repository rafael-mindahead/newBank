-- Schema inicial do FinanceHub (MySQL 8.4)
CREATE TABLE IF NOT EXISTS usuarios (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(120) NOT NULL,
    email VARCHAR(160) NOT NULL UNIQUE,
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS categorias (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL UNIQUE,
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS lancamentos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tipo ENUM('RECEITA', 'DESPESA') NOT NULL,
    descricao VARCHAR(255) NOT NULL,
    valor DECIMAL(15, 2) NOT NULL,
    valor_centavos BIGINT NOT NULL,
    data_lancamento DATE NOT NULL,
    categoria_id BIGINT NULL,
    usuario_id BIGINT NOT NULL DEFAULT 1,
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_lancamento_categoria FOREIGN KEY (categoria_id) REFERENCES categorias(id) ON DELETE SET NULL,
    CONSTRAINT fk_lancamento_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Dados iniciais padrão
INSERT IGNORE INTO usuarios (id, nome, email) VALUES
    (1, 'Usuario Principal', 'usuario@financehub.com.br');

INSERT IGNORE INTO categorias (id, nome) VALUES
    (1, 'Trabalho'),
    (2, 'Alimentacao'),
    (3, 'Moradia'),
    (4, 'Transporte'),
    (5, 'Lazer');

