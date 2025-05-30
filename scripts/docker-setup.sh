#!/bin/bash

# Script para configurar e executar o ambiente Docker

set -e

echo "🚀 Configurando ambiente Docker para Armário Inteligente..."

# Função para exibir ajuda
show_help() {
    echo "Uso: $0 [COMANDO]"
    echo ""
    echo "Comandos disponíveis:"
    echo "  dev        - Iniciar ambiente de desenvolvimento"
    echo "  prod       - Iniciar ambiente de produção"
    echo "  stop       - Parar todos os containers"
    echo "  clean      - Limpar containers e volumes"
    echo "  logs       - Exibir logs da aplicação"
    echo "  db-logs    - Exibir logs do banco de dados"
    echo "  restart    - Reiniciar aplicação"
    echo "  build      - Rebuild da aplicação"
    echo "  help       - Exibir esta ajuda"
}

# Função para verificar se o Docker está instalado
check_docker() {
    if ! command -v docker &> /dev/null; then
        echo "❌ Docker não está instalado. Por favor, instale o Docker primeiro."
        exit 1
    fi

    if ! command -v docker-compose &> /dev/null; then
        echo "❌ Docker Compose não está instalado. Por favor, instale o Docker Compose primeiro."
        exit 1
    fi
}

# Função para iniciar ambiente de desenvolvimento
start_dev() {
    echo "🔧 Iniciando ambiente de desenvolvimento..."
    docker-compose -f docker-compose.dev.yml up -d
    echo "✅ Ambiente de desenvolvimento iniciado!"
    echo "📱 Aplicação: http://localhost:8081"
    echo "🗄️  pgAdmin: http://localhost:5051"
    echo "📊 Swagger: http://localhost:8081/swagger-ui.html"
}

# Função para iniciar ambiente de produção
start_prod() {
    echo "🚀 Iniciando ambiente de produção..."
    docker-compose up -d
    echo "✅ Ambiente de produção iniciado!"
    echo "📱 Aplicação: http://localhost:8080"
    echo "🗄️  pgAdmin: http://localhost:5050"
    echo "📊 Swagger: http://localhost:8080/swagger-ui.html"
}

# Função para parar containers
stop_containers() {
    echo "🛑 Parando containers..."
    docker-compose down
    docker-compose -f docker-compose.dev.yml down
    echo "✅ Containers parados!"
}

# Função para limpeza completa
clean_all() {
    echo "🧹 Limpando containers e volumes..."
    docker-compose down -v --remove-orphans
    docker-compose -f docker-compose.dev.yml down -v --remove-orphans
    docker system prune -f
    echo "✅ Limpeza concluída!"
}

# Função para exibir logs da aplicação
show_logs() {
    echo "📋 Exibindo logs da aplicação..."
    docker-compose logs -f app
}

# Função para exibir logs do banco
show_db_logs() {
    echo "📋 Exibindo logs do banco de dados..."
    docker-compose logs -f postgres
}

# Função para reiniciar aplicação
restart_app() {
    echo "🔄 Reiniciando aplicação..."
    docker-compose restart app
    echo "✅ Aplicação reiniciada!"
}

# Função para rebuild
rebuild_app() {
    echo "🔨 Fazendo rebuild da aplicação..."
    docker-compose down app
    docker-compose build --no-cache app
    docker-compose up -d app
    echo "✅ Rebuild concluído!"
}

# Verificar Docker
check_docker

# Processar comando
case "${1:-help}" in
    "dev")
        start_dev
        ;;
    "prod")
        start_prod
        ;;
    "stop")
        stop_containers
        ;;
    "clean")
        clean_all
        ;;
    "logs")
        show_logs
        ;;
    "db-logs")
        show_db_logs
        ;;
    "restart")
        restart_app
        ;;
    "build")
        rebuild_app
        ;;
    "help"|*)
        show_help
        ;;
esac
