@echo off
:: Script para configuração fácil do Armário Inteligente para iniciantes no Windows
:: Este script guia o usuário através da configuração e execução do sistema

:: Cores para melhor visualização
set GREEN=[92m
set YELLOW=[93m
set BLUE=[94m
set RED=[91m
set NC=[0m

:: Função para exibir mensagens de etapa
:step
echo %BLUE%=^>%NC% %GREEN%%~1%NC%
goto :eof

:: Função para exibir mensagens de informação
:info
echo %YELLOW%INFO:%NC% %~1
goto :eof

:: Função para exibir mensagens de erro
:error
echo %RED%ERRO:%NC% %~1
goto :eof

:: Cabeçalho
cls
echo %GREEN%=================================================%NC%
echo %GREEN%  Configuração do Sistema Armário Inteligente    %NC%
echo %GREEN%=================================================%NC%
echo.
echo Este script vai ajudar você a configurar e executar o sistema.
echo Siga as instruções na tela.
echo.

:: Verificar Java
call :step "Verificando se o Java está instalado..."
java -version >nul 2>&1
if %ERRORLEVEL% neq 0 (
    call :error "Java não encontrado. Por favor, instale o Java 17 ou superior."
    echo.
    echo Links para download do Java:
    echo - Windows: https://adoptium.net/
    pause
    exit /b 1
) else (
    for /f tokens^=2-5^ delims^=.-_^" %%j in ('java -version 2^>^&1') do (
        set JAVA_VERSION=%%j
        goto :break_java_version
    )
    :break_java_version
    
    call :info "Java encontrado: versão %JAVA_VERSION%"
    
    :: Verificar versão do Java (precisa ser 17 ou superior)
    if %JAVA_VERSION% LSS 17 (
        call :error "Você precisa do Java 17 ou superior. Por favor, atualize seu Java."
        pause
        exit /b 1
    )
)

echo.

:: Verificar Maven (opcional)
call :step "Verificando Maven (opcional)..."
mvn --version >nul 2>&1
if %ERRORLEVEL% neq 0 (
    call :info "Maven não encontrado. Vamos usar o Maven Wrapper incluído no projeto."
    set USE_MVN=false
) else (
    call :info "Maven encontrado."
    set USE_MVN=true
)

echo.

:: Verificar se estamos na pasta do projeto
call :step "Verificando a estrutura do projeto..."
if exist "pom.xml" if exist "mvnw.cmd" (
    call :info "Estrutura do projeto parece correta."
) else (
    if exist "armario-inteligente" (
        call :info "Entrando na pasta do projeto..."
        cd armario-inteligente
        
        if exist "pom.xml" if exist "mvnw.cmd" (
            call :info "Estrutura do projeto parece correta."
        ) else (
            call :error "Não consegui encontrar os arquivos do projeto. Certifique-se de estar na pasta correta."
            pause
            exit /b 1
        )
    ) else (
        call :error "Não consegui encontrar os arquivos do projeto. Certifique-se de estar na pasta correta."
        pause
        exit /b 1
    )
)

echo.

:: Verificar se a porta 8080 está livre
call :step "Verificando se a porta 8080 está livre..."
netstat -ano | findstr :8080 >nul
if %ERRORLEVEL% equ 0 (
    call :error "A porta 8080 já está em uso. Deseja usar outra porta? (s/n)"
    set /p USE_ANOTHER_PORT=
    if /i "%USE_ANOTHER_PORT%"=="s" (
        echo Digite o número da porta que deseja usar (ex: 8081):
        set /p CUSTOM_PORT=
        set CUSTOM_PORT_ARG=--server.port=%CUSTOM_PORT%
        call :info "Vamos usar a porta %CUSTOM_PORT%."
    ) else (
        call :error "Por favor, feche o programa que está usando a porta 8080 e tente novamente."
        pause
        exit /b 1
    )
) else (
    call :info "Porta 8080 está livre."
    set CUSTOM_PORT=8080
    set CUSTOM_PORT_ARG=
)

echo.

:: Perguntar se deseja compilar o projeto
call :step "Deseja compilar o projeto? (recomendado na primeira execução) (s/n)"
set /p COMPILE_PROJECT=
echo.

if /i "%COMPILE_PROJECT%"=="s" (
    call :step "Compilando o projeto..."
    if "%USE_MVN%"=="true" (
        mvn clean package -DskipTests
    ) else (
        mvnw.cmd clean package -DskipTests
    )
    
    :: Verificar se a compilação foi bem-sucedida
    if %ERRORLEVEL% neq 0 (
        call :error "Erro na compilação. Verifique os erros acima."
        pause
        exit /b 1
    ) else (
        call :info "Compilação concluída com sucesso!"
    )
    echo.
)

:: Executar a aplicação
call :step "Executando a aplicação..."
echo A aplicação será iniciada na porta %YELLOW%%CUSTOM_PORT%%NC%.
echo Para parar a aplicação, pressione %YELLOW%Ctrl+C%NC%.
echo.

if "%USE_MVN%"=="true" (
    if defined CUSTOM_PORT_ARG (
        mvn spring-boot:run -Dspring-boot.run.arguments="%CUSTOM_PORT_ARG%"
    ) else (
        mvn spring-boot:run
    )
) else (
    if defined CUSTOM_PORT_ARG (
        mvnw.cmd spring-boot:run -Dspring-boot.run.arguments="%CUSTOM_PORT_ARG%"
    ) else (
        mvnw.cmd spring-boot:run
    )
)

:: O script termina quando a aplicação é encerrada
