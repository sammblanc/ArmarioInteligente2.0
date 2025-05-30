-- Script de inicialização do banco de dados
-- Este script é executado automaticamente quando o container PostgreSQL é criado

-- Criar extensões necessárias
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Criar tipos ENUM (opcional, caso queira usar ENUMs nativos do PostgreSQL)
-- Comentado pois estamos usando VARCHAR conforme recomendado anteriormente
/*
CREATE TYPE status_entrega AS ENUM (
    'AGUARDANDO_ENTREGA',
    'ENTREGUE',
    'RETIRADO',
    'CANCELADO',
    'EXPIRADO',
    'AGUARDANDO_RETIRADA'
);

CREATE TYPE status_reserva AS ENUM (
    'PENDENTE',
    'CONFIRMADA',
    'CANCELADA',
    'CONCLUIDA',
    'ATIVA'
);
*/

-- Criar usuário específico para a aplicação (se necessário)
-- O usuário principal já é criado pelas variáveis de ambiente do Docker

-- Configurações de performance
ALTER SYSTEM SET shared_preload_libraries = 'pg_stat_statements';
ALTER SYSTEM SET max_connections = 200;
ALTER SYSTEM SET shared_buffers = '256MB';
ALTER SYSTEM SET effective_cache_size = '1GB';
ALTER SYSTEM SET maintenance_work_mem = '64MB';
ALTER SYSTEM SET checkpoint_completion_target = 0.9;
ALTER SYSTEM SET wal_buffers = '16MB';
ALTER SYSTEM SET default_statistics_target = 100;
ALTER SYSTEM SET random_page_cost = 1.1;
ALTER SYSTEM SET effective_io_concurrency = 200;

-- Recarregar configurações
SELECT pg_reload_conf();
