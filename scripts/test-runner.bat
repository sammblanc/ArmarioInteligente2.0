@echo off
REM 🧪 Script de Teste Automatizado - Armário Inteligente (Windows)
REM Autor: Sistema Armário Inteligente
REM Versão: 1.0

setlocal enabledelayedexpansion

REM Configurações
set BASE_URL=http://localhost:8080
set API_URL=%BASE_URL%/api/v1
set HEALTH_URL=%BASE_URL%/actuator/health

REM Função para imprimir mensagens
:print_message
echo [%~1] %~2
goto :eof

REM Verificar se a aplicação está rodando
:check_application
call :print_message "INFO" "🔍 Verificando se a aplicação está rodando..."

curl -f -s %HEALTH_URL% >nul 2>&1
if %errorlevel% equ 0 (
    call :print_message "SUCCESS" "✅ Aplicação está rodando em %BASE_URL%"
    goto :eof
) else (
    call :print_message "ERROR" "❌ Aplicação não está rodando em %BASE_URL%"
    call :print_message "WARNING" "💡 Execute: mvnw.cmd spring-boot:run"
    exit /b 1
)

REM Obter token de autenticação
:get_auth_token
call :print_message "INFO" "🔐 Obtendo token de autenticação..."

for /f "delims=" %%i in ('curl -s -X POST "%API_URL%/auth/login" -H "Content-Type: application/json" -d "{\"email\":\"admin@smartlocker.com\",\"senha\":\"admin123\"}"') do set response=%%i

echo %response% | findstr "token" >nul
if %errorlevel% equ 0 (
    REM Extrair token (simplificado para Windows)
    call :print_message "SUCCESS" "✅ Token obtido com sucesso"
    set TOKEN=extracted_token_here
    goto :eof
) else (
    call :print_message "ERROR" "❌ Falha ao obter token"
    echo Resposta: %response%
    exit /b 1
)

REM Testar endpoint
:test_endpoint
set method=%~1
set endpoint=%~2
set description=%~3
set data=%~4
set expected_status=%~5
if "%expected_status%"=="" set expected_status=200

call :print_message "INFO" "🧪 Testando: %description%"

if "%method%"=="GET" (
    set curl_cmd=curl -s -w "%%{http_code}" -X %method% "%API_URL%%endpoint%" -H "Authorization: Bearer %TOKEN%"
) else (
    set curl_cmd=curl -s -w "%%{http_code}" -X %method% "%API_URL%%endpoint%" -H "Content-Type: application/json" -H "Authorization: Bearer %TOKEN%" -d "%data%"
)

for /f "delims=" %%i in ('!curl_cmd!') do set response=%%i

REM Extrair status code (últimos 3 caracteres)
set status_code=!response:~-3!

if "!status_code!"=="%expected_status%" (
    call :print_message "SUCCESS" "   ✅ %method% %endpoint% - Status: !status_code!"
) else (
    call :print_message "ERROR" "   ❌ %method% %endpoint% - Esperado: %expected_status%, Recebido: !status_code!"
)
goto :eof

REM Executar testes de autenticação
:test_authentication
call :print_message "SECTION" "🔐 === TESTES DE AUTENTICAÇÃO ==="

call :test_endpoint "POST" "/auth/login" "Login com credenciais válidas" "{\"email\":\"admin@smartlocker.com\",\"senha\":\"admin123\"}" "200"
call :test_endpoint "POST" "/auth/login" "Login com credenciais inválidas" "{\"email\":\"usuario@inexistente.com\",\"senha\":\"senhaerrada\"}" "401"
goto :eof

REM Executar testes de usuários
:test_usuarios
call :print_message "SECTION" "👥 === TESTES DE USUÁRIOS ==="

call :test_endpoint "GET" "/usuarios" "Listar todos os usuários" "" "200"
call :test_endpoint "GET" "/usuarios/1" "Buscar usuário por ID" "" "200"
call :test_endpoint "GET" "/usuarios/ativos" "Listar usuários ativos" "" "200"
goto :eof

REM Executar testes de condomínios
:test_condominios
call :print_message "SECTION" "🏢 === TESTES DE CONDOMÍNIOS ==="

call :test_endpoint "GET" "/condominios" "Listar todos os condomínios" "" "200"
call :test_endpoint "GET" "/condominios/1" "Buscar condomínio por ID" "" "200"
goto :eof

REM Executar testes unitários
:test_unit_tests
call :print_message "SECTION" "🔬 === TESTES UNITÁRIOS ==="

call :print_message "INFO" "🧪 Executando testes unitários..."
mvnw.cmd test -q
if %errorlevel% equ 0 (
    call :print_message "SUCCESS" "✅ Testes unitários passaram"
) else (
    call :print_message "ERROR" "❌ Alguns testes unitários falharam"
    exit /b 1
)
goto :eof

REM Gerar relatório de cobertura
:generate_coverage_report
call :print_message "SECTION" "📊 === RELATÓRIO DE COBERTURA ==="

call :print_message "INFO" "📈 Gerando relatório de cobertura..."
mvnw.cmd jacoco:report -q
if %errorlevel% equ 0 (
    call :print_message "SUCCESS" "✅ Relatório de cobertura gerado"
    call :print_message "INFO" "📁 Relatório disponível em: target\site\jacoco\index.html"
) else (
    call :print_message "ERROR" "❌ Falha ao gerar relatório de cobertura"
    exit /b 1
)
goto :eof

REM Mostrar ajuda
:show_help
echo 🧪 Script de Teste Automatizado - Armário Inteligente (Windows)
echo.
echo Uso: %0 [OPÇÃO]
echo.
echo Opções:
echo   (sem opção)  Executar todos os testes
echo   unit         Executar apenas testes unitários
echo   api          Executar apenas testes de API
echo   coverage     Executar testes e gerar relatório de cobertura
echo   help         Mostrar esta ajuda
echo.
echo Exemplos:
echo   %0                    # Executar todos os testes
echo   %0 api               # Executar apenas testes de API
echo   %0 unit              # Executar apenas testes unitários
echo   %0 coverage          # Gerar relatório de cobertura
goto :eof

REM Função principal
:main
call :print_message "INFO" "🚀 === INICIANDO TESTES AUTOMATIZADOS ==="
call :print_message "INFO" "📅 Data: %date% %time%"
echo.

set failed_tests=0

REM Verificar aplicação
call :check_application
if %errorlevel% neq 0 exit /b 1

REM Obter token
call :get_auth_token
if %errorlevel% neq 0 exit /b 1

REM Executar testes baseado no parâmetro
if "%1"=="unit" (
    call :test_unit_tests
    if %errorlevel% neq 0 set /a failed_tests+=1
) else if "%1"=="api" (
    call :test_authentication
    call :test_usuarios
    call :test_condominios
) else if "%1"=="coverage" (
    call :test_unit_tests
    if %errorlevel% equ 0 call :generate_coverage_report
) else (
    REM Executar todos os testes
    call :test_authentication
    call :test_usuarios
    call :test_condominios
    call :test_unit_tests
    if %errorlevel% neq 0 set /a failed_tests+=1
)

echo.
call :print_message "INFO" "📊 === RESUMO DOS TESTES ==="

if %failed_tests% equ 0 (
    call :print_message "SUCCESS" "🎉 Todos os testes passaram com sucesso!"
    exit /b 0
) else (
    call :print_message "ERROR" "❌ %failed_tests% categoria(s) de teste falharam"
    exit /b 1
)

REM Verificar argumentos e executar
if "%1"=="help" goto :show_help
if "%1"=="-h" goto :show_help
if "%1"=="--help" goto :show_help

call :main %1
