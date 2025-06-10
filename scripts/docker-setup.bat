@echo off
REM Script para configurar e executar o ambiente Docker no Windows

setlocal enabledelayedexpansion

echo 🚀 Configurando ambiente Docker para Armário Inteligente...

REM Função para exibir ajuda
if "%1"=="help" goto :show_help
if "%1"=="" goto :show_help

REM Verificar se o Docker está instalado
docker --version >nul 2>&1
if errorlevel 1 (
    echo ❌ Docker não está instalado. Por favor, instale o Docker primeiro.
    exit /b 1
)

docker-compose --version >nul 2>&1
if errorlevel 1 (
    echo ❌ Docker Compose não está instalado. Por favor, instale o Docker Compose primeiro.
    exit /b 1
)

REM Processar comando
if "%1"=="dev" goto :start_dev
if "%1"=="prod" goto :start_prod
if "%1"=="stop" goto :stop_containers
if "%1"=="clean" goto :clean_all
if "%1"=="logs" goto :show_logs
if "%1"=="db-logs" goto :show_db_logs
if "%1"=="restart" goto :restart_app
if "%1"=="build" goto :rebuild_app

goto :show_help

:start_dev
echo 🔧 Iniciando ambiente de desenvolvimento...
docker-compose -f docker-compose.dev.yml up -d
echo ✅ Ambiente de desenvolvimento iniciado!
echo 📱 Aplicação: http://localhost:8081
echo 🗄️  pgAdmin: http://localhost:5051
echo 📊 Swagger: http://localhost:8081/swagger-ui.html
goto :end

:start_prod
echo 🚀 Iniciando ambiente de produção...
docker-compose up -d
echo ✅ Ambiente de produção iniciado!
echo 📱 Aplicação: http://localhost:8080
echo 🗄️  pgAdmin: http://localhost:5050
echo 📊 Swagger: http://localhost:8080/swagger-ui.html
goto :end

:stop_containers
echo 🛑 Parando containers...
docker-compose down
docker-compose -f docker-compose.dev.yml down
echo ✅ Containers parados!
goto :end

:clean_all
echo 🧹 Limpando containers e volumes...
docker-compose down -v --remove-orphans
docker-compose -f docker-compose.dev.yml down -v --remove-orphans
docker system prune -f
echo ✅ Limpeza concluída!
goto :end

:show_logs
echo 📋 Exibindo logs da aplicação...
docker-compose logs -f app
goto :end

:show_db_logs
echo 📋 Exibindo logs do banco de dados...
docker-compose logs -f postgres
goto :end

:restart_app
echo 🔄 Reiniciando aplicação...
docker-compose restart app
echo ✅ Aplicação reiniciada!
goto :end

:rebuild_app
echo 🔨 Fazendo rebuild da aplicação...
docker-compose down app
docker-compose build --no-cache app
docker-compose up -d app
echo ✅ Rebuild concluído!
goto :end

:show_help
echo Uso: %0 [COMANDO]
echo.
echo Comandos disponíveis:
echo   dev        - Iniciar ambiente de desenvolvimento
echo   prod       - Iniciar ambiente de produção
echo   stop       - Parar todos os containers
echo   clean      - Limpar containers e volumes
echo   logs       - Exibir logs da aplicação
echo   db-logs    - Exibir logs do banco de dados
echo   restart    - Reiniciar aplicação
echo   build      - Rebuild da aplicação
echo   help       - Exibir esta ajuda
goto :end

:end
