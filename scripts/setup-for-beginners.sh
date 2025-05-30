#!/bin/bash
# Script para configuração fácil do Armário Inteligente para iniciantes
# Este script guia o usuário através da configuração e execução do sistema

# Cores para melhor visualização
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Função para exibir mensagens de etapa
step() {
  echo -e "${BLUE}==>${NC} ${GREEN}$1${NC}"
}

# Função para exibir mensagens de informação
info() {
  echo -e "${YELLOW}INFO:${NC} $1"
}

# Função para exibir mensagens de erro
error() {
  echo -e "${RED}ERRO:${NC} $1"
}

# Função para verificar se um comando existe
command_exists() {
  command -v "$1" >/dev/null 2>&1
}

# Cabeçalho
clear
echo -e "${GREEN}=================================================${NC}"
echo -e "${GREEN}  Configuração do Sistema Armário Inteligente    ${NC}"
echo -e "${GREEN}=================================================${NC}"
echo ""
echo -e "Este script vai ajudar você a configurar e executar o sistema."
echo -e "Siga as instruções na tela."
echo ""

# Verificar Java
step "Verificando se o Java está instalado..."
if command_exists java; then
  JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}')
  info "Java encontrado: versão $JAVA_VERSION"
  
  # Verificar versão do Java (precisa ser 17 ou superior)
  JAVA_MAJOR_VERSION=$(echo $JAVA_VERSION | cut -d'.' -f1)
  if [ "$JAVA_MAJOR_VERSION" -lt 17 ]; then
    error "Você precisa do Java 17 ou superior. Por favor, atualize seu Java."
    exit 1
  fi
else
  error "Java não encontrado. Por favor, instale o Java 17 ou superior."
  echo ""
  echo "Links para download do Java:"
  echo "- Windows/Mac/Linux: https://adoptium.net/"
  echo "- Ou use o gerenciador de pacotes do seu sistema operacional"
  exit 1
fi

echo ""

# Verificar Maven (opcional)
step "Verificando Maven (opcional)..."
if command_exists mvn; then
  MVN_VERSION=$(mvn --version | head -n 1)
  info "Maven encontrado: $MVN_VERSION"
  USE_MVN=true
else
  info "Maven não encontrado. Vamos usar o Maven Wrapper incluído no projeto."
  USE_MVN=false
fi

echo ""

# Verificar se estamos na pasta do projeto
step "Verificando a estrutura do projeto..."
if [ -f "pom.xml" ] && [ -f "mvnw" ]; then
  info "Estrutura do projeto parece correta."
else
  if [ -d "armario-inteligente" ]; then
    info "Entrando na pasta do projeto..."
    cd armario-inteligente
    
    if [ -f "pom.xml" ] && [ -f "mvnw" ]; then
      info "Estrutura do projeto parece correta."
    else
      error "Não consegui encontrar os arquivos do projeto. Certifique-se de estar na pasta correta."
      exit 1
    fi
  else
    error "Não consegui encontrar os arquivos do projeto. Certifique-se de estar na pasta correta."
    exit 1
  fi
fi

echo ""

# Tornar o Maven Wrapper executável (para Linux/Mac)
if [[ "$OSTYPE" == "linux-gnu"* ]] || [[ "$OSTYPE" == "darwin"* ]]; then
  step "Tornando o Maven Wrapper executável..."
  chmod +x mvnw
  info "Permissão concedida."
  echo ""
fi

# Verificar se a porta 8080 está livre
step "Verificando se a porta 8080 está livre..."
if command_exists lsof; then
  PORT_CHECK=$(lsof -i:8080 -t)
elif command_exists netstat; then
  if [[ "$OSTYPE" == "msys" ]] || [[ "$OSTYPE" == "win32" ]]; then
    PORT_CHECK=$(netstat -ano | findstr :8080)
  else
    PORT_CHECK=$(netstat -tuln | grep :8080)
  fi
else
  info "Não foi possível verificar se a porta 8080 está livre. Vamos tentar mesmo assim."
  PORT_CHECK=""
fi

if [ -n "$PORT_CHECK" ]; then
  error "A porta 8080 já está em uso. Deseja usar outra porta? (s/n)"
  read -r USE_ANOTHER_PORT
  if [[ "$USE_ANOTHER_PORT" =~ ^[Ss]$ ]]; then
    echo "Digite o número da porta que deseja usar (ex: 8081):"
    read -r CUSTOM_PORT
    CUSTOM_PORT_ARG="--server.port=$CUSTOM_PORT"
    info "Vamos usar a porta $CUSTOM_PORT."
  else
    error "Por favor, feche o programa que está usando a porta 8080 e tente novamente."
    exit 1
  fi
else
  info "Porta 8080 está livre."
  CUSTOM_PORT=8080
  CUSTOM_PORT_ARG=""
fi

echo ""

# Perguntar se deseja compilar o projeto
step "Deseja compilar o projeto? (recomendado na primeira execução) (s/n)"
read -r COMPILE_PROJECT
echo ""

if [[ "$COMPILE_PROJECT" =~ ^[Ss]$ ]]; then
  step "Compilando o projeto..."
  if [ "$USE_MVN" = true ]; then
    mvn clean package -DskipTests
  else
    if [[ "$OSTYPE" == "msys" ]] || [[ "$OSTYPE" == "win32" ]]; then
      mvnw.cmd clean package -DskipTests
    else
      ./mvnw clean package -DskipTests
    fi
  fi
  
  # Verificar se a compilação foi bem-sucedida
  if [ $? -ne 0 ]; then
    error "Erro na compilação. Verifique os erros acima."
    exit 1
  else
    info "Compilação concluída com sucesso!"
  fi
  echo ""
fi

# Executar a aplicação
step "Executando a aplicação..."
echo -e "A aplicação será iniciada na porta ${YELLOW}$CUSTOM_PORT${NC}."
echo -e "Para parar a aplicação, pressione ${YELLOW}Ctrl+C${NC}."
echo ""

if [ "$USE_MVN" = true ]; then
  if [ -n "$CUSTOM_PORT_ARG" ]; then
    mvn spring-boot:run -Dspring-boot.run.arguments="$CUSTOM_PORT_ARG"
  else
    mvn spring-boot:run
  fi
else
  if [[ "$OSTYPE" == "msys" ]] || [[ "$OSTYPE" == "win32" ]]; then
    if [ -n "$CUSTOM_PORT_ARG" ]; then
      mvnw.cmd spring-boot:run -Dspring-boot.run.arguments="$CUSTOM_PORT_ARG"
    else
      mvnw.cmd spring-boot:run
    fi
  else
    if [ -n "$CUSTOM_PORT_ARG" ]; then
      ./mvnw spring-boot:run -Dspring-boot.run.arguments="$CUSTOM_PORT_ARG"
    else
      ./mvnw spring-boot:run
    fi
  fi
fi

# Nota: O script termina quando a aplicação é encerrada
