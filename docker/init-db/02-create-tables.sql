-- Script para criar as tabelas do sistema
-- Este script é executado após a criação do banco de dados

-- 1. Criar tabela 'condominios'
CREATE TABLE IF NOT EXISTS condominios (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    endereco VARCHAR(255) NOT NULL,
    cep VARCHAR(50),
    cidade VARCHAR(100),
    estado VARCHAR(100),
    telefone VARCHAR(50),
    email VARCHAR(100)
);

-- 2. Criar tabela 'tipo_usuario'
CREATE TABLE IF NOT EXISTS tipo_usuario (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(255) NOT NULL UNIQUE,
    descricao VARCHAR(500)
);

-- 3. Criar tabela 'usuarios'
CREATE TABLE IF NOT EXISTS usuarios (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    senha VARCHAR(255) NOT NULL,
    telefone VARCHAR(50) NOT NULL,
    ativo BOOLEAN DEFAULT TRUE NOT NULL,
    tipo_usuario_id BIGINT NOT NULL,
    CONSTRAINT fk_tipo_usuario FOREIGN KEY (tipo_usuario_id) REFERENCES tipo_usuario(id)
);

-- 4. Criar tabela 'armarios'
CREATE TABLE IF NOT EXISTS armarios (
    id SERIAL PRIMARY KEY,
    identificacao VARCHAR(255) NOT NULL,
    localizacao VARCHAR(255),
    descricao TEXT,
    ativo BOOLEAN DEFAULT TRUE,
    condominio_id BIGINT NOT NULL,
    CONSTRAINT fk_condominio FOREIGN KEY (condominio_id) REFERENCES condominios(id)
);

-- 5. Criar tabela 'compartimentos'
CREATE TABLE IF NOT EXISTS compartimentos (
    id SERIAL PRIMARY KEY,
    numero VARCHAR(255) NOT NULL,
    tamanho VARCHAR(50),
    ocupado BOOLEAN DEFAULT FALSE,
    codigo_acesso VARCHAR(255),
    armario_id BIGINT NOT NULL,
    CONSTRAINT fk_armario FOREIGN KEY (armario_id) REFERENCES armarios(id)
);

-- 6. Criar tabela 'entregas'
CREATE TABLE IF NOT EXISTS entregas (
    id SERIAL PRIMARY KEY,
    codigo_rastreio VARCHAR(255) NOT NULL,
    data_entrega TIMESTAMP NOT NULL,
    data_retirada TIMESTAMP,
    observacao TEXT,
    status VARCHAR(50) NOT NULL,
    compartimento_id BIGINT NOT NULL,
    entregador_id BIGINT NOT NULL,
    destinatario_id BIGINT NOT NULL,
    CONSTRAINT fk_compartimento_entrega FOREIGN KEY (compartimento_id) REFERENCES compartimentos(id),
    CONSTRAINT fk_entregador FOREIGN KEY (entregador_id) REFERENCES usuarios(id),
    CONSTRAINT fk_destinatario FOREIGN KEY (destinatario_id) REFERENCES usuarios(id)
);

-- 7. Criar tabela 'reservas'
CREATE TABLE IF NOT EXISTS reservas (
    id SERIAL PRIMARY KEY,
    data_inicio TIMESTAMP NOT NULL,
    data_fim TIMESTAMP NOT NULL,
    observacao TEXT,
    status VARCHAR(50) NOT NULL,
    compartimento_id BIGINT NOT NULL,
    usuario_id BIGINT NOT NULL,
    CONSTRAINT fk_compartimento_reserva FOREIGN KEY (compartimento_id) REFERENCES compartimentos(id),
    CONSTRAINT fk_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
);

-- Criar índices para melhorar performance
CREATE INDEX IF NOT EXISTS idx_armarios_condominio ON armarios(condominio_id);
CREATE INDEX IF NOT EXISTS idx_compartimentos_armario ON compartimentos(armario_id);
CREATE INDEX IF NOT EXISTS idx_compartimentos_ocupado ON compartimentos(ocupado);
CREATE INDEX IF NOT EXISTS idx_entregas_compartimento ON entregas(compartimento_id);
CREATE INDEX IF NOT EXISTS idx_entregas_entregador ON entregas(entregador_id);
CREATE INDEX IF NOT EXISTS idx_entregas_destinatario ON entregas(destinatario_id);
CREATE INDEX IF NOT EXISTS idx_entregas_status ON entregas(status);
CREATE INDEX IF NOT EXISTS idx_entregas_codigo_rastreio ON entregas(codigo_rastreio);
CREATE INDEX IF NOT EXISTS idx_reservas_compartimento ON reservas(compartimento_id);
CREATE INDEX IF NOT EXISTS idx_reservas_usuario ON reservas(usuario_id);
CREATE INDEX IF NOT EXISTS idx_reservas_status ON reservas(status);
CREATE INDEX IF NOT EXISTS idx_reservas_data_inicio ON reservas(data_inicio);
CREATE INDEX IF NOT EXISTS idx_usuarios_email ON usuarios(email);
CREATE INDEX IF NOT EXISTS idx_usuarios_tipo ON usuarios(tipo_usuario_id);
