#!/bin/bash

# 🧪 Script de Teste Automatizado - Armário Inteligente
# Autor: Sistema Armário Inteligente
# Versão: 1.0

set -e  # Parar execução em caso de erro

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configurações
BASE_URL="http://localhost:8080"
API_URL="$BASE_URL/api/v1"
HEALTH_URL="$BASE_URL/actuator/health"

# Função para imprimir mensagens coloridas
print_message() {
    local color=$1
    local message=$2
    echo -e "${color}${message}${NC}"
}

# Função para verificar se a aplicação está rodando
check_application() {
    print_message $BLUE "🔍 Verificando se a aplicação está rodando..."
    
    if curl -f -s $HEALTH_URL > /dev/null; then
        print_message $GREEN "✅ Aplicação está rodando em $BASE_URL"
        return 0
    else
        print_message $RED "❌ Aplicação não está rodando em $BASE_URL"
        print_message $YELLOW "💡 Execute: ./mvnw spring-boot:run"
        exit 1
    fi
}

# Função para obter token de autenticação
get_auth_token() {
    print_message $BLUE "🔐 Obtendo token de autenticação..."
    
    local response=$(curl -s -X POST "$API_URL/auth/login" \
        -H "Content-Type: application/json" \
        -d '{"email":"admin@smartlocker.com","senha":"admin123"}')
    
    if echo "$response" | grep -q "token"; then
        TOKEN=$(echo "$response" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
        print_message $GREEN "✅ Token obtido com sucesso"
        return 0
    else
        print_message $RED "❌ Falha ao obter token"
        echo "Resposta: $response"
        exit 1
    fi
}

# Função para testar endpoint
test_endpoint() {
    local method=$1
    local endpoint=$2
    local description=$3
    local data=$4
    local expected_status=${5:-200}
    
    print_message $BLUE "🧪 Testando: $description"
    
    local curl_cmd="curl -s -w '%{http_code}' -X $method '$API_URL$endpoint'"
    
    if [ "$method" != "GET" ] && [ -n "$data" ]; then
        curl_cmd="$curl_cmd -H 'Content-Type: application/json' -d '$data'"
    fi
    
    if [ -n "$TOKEN" ]; then
        curl_cmd="$curl_cmd -H 'Authorization: Bearer $TOKEN'"
    fi
    
    local response=$(eval $curl_cmd)
    local status_code="${response: -3}"
    local body="${response%???}"
    
    if [ "$status_code" = "$expected_status" ]; then
        print_message $GREEN "   ✅ $method $endpoint - Status: $status_code"
        return 0
    else
        print_message $RED "   ❌ $method $endpoint - Esperado: $expected_status, Recebido: $status_code"
        if [ -n "$body" ]; then
            echo "   Resposta: $body"
        fi
        return 1
    fi
}

# Função para executar testes de autenticação
test_authentication() {
    print_message $YELLOW "🔐 === TESTES DE AUTENTICAÇÃO ==="
    
    # Teste de login válido
    test_endpoint "POST" "/auth/login" "Login com credenciais válidas" \
        '{"email":"admin@smartlocker.com","senha":"admin123"}' 200
    
    # Teste de login inválido
    test_endpoint "POST" "/auth/login" "Login com credenciais inválidas" \
        '{"email":"usuario@inexistente.com","senha":"senhaerrada"}' 401
}

# Função para executar testes de usuários
test_usuarios() {
    print_message $YELLOW "👥 === TESTES DE USUÁRIOS ==="
    
    # Listar usuários
    test_endpoint "GET" "/usuarios" "Listar todos os usuários"
    
    # Buscar usuário por ID
    test_endpoint "GET" "/usuarios/1" "Buscar usuário por ID"
    
    # Criar novo usuário
    test_endpoint "POST" "/usuarios" "Criar novo usuário" \
        '{"nome":"Teste Script","email":"teste.script@exemplo.com","senha":"senha123","telefone":"(81)99999-9999","tipoUsuarioId":2}'
    
    # Listar usuários ativos
    test_endpoint "GET" "/usuarios/ativos" "Listar usuários ativos"
}

# Função para executar testes de condomínios
test_condominios() {
    print_message $YELLOW "🏢 === TESTES DE CONDOMÍNIOS ==="
    
    # Listar condomínios
    test_endpoint "GET" "/condominios" "Listar todos os condomínios"
    
    # Buscar condomínio por ID
    test_endpoint "GET" "/condominios/1" "Buscar condomínio por ID"
    
    # Criar novo condomínio
    test_endpoint "POST" "/condominios" "Criar novo condomínio" \
        '{"nome":"Condomínio Teste Script","endereco":"Rua do Teste, 123","cep":"50000-000","cidade":"Recife","estado":"PE","telefone":"(81)3333-9999","email":"teste@script.com"}'
}

# Função para executar testes de armários
test_armarios() {
    print_message $YELLOW "🗄️ === TESTES DE ARMÁRIOS ==="
    
    # Listar armários
    test_endpoint "GET" "/armarios" "Listar todos os armários"
    
    # Listar armários por condomínio
    test_endpoint "GET" "/armarios/condominio/1" "Listar armários por condomínio"
    
    # Buscar armário por ID
    test_endpoint "GET" "/armarios/1" "Buscar armário por ID"
}

# Função para executar testes de compartimentos
test_compartimentos() {
    print_message $YELLOW "📦 === TESTES DE COMPARTIMENTOS ==="
    
    # Listar compartimentos
    test_endpoint "GET" "/compartimentos" "Listar todos os compartimentos"
    
    # Listar compartimentos por armário
    test_endpoint "GET" "/compartimentos/armario/1" "Listar compartimentos por armário"
    
    # Listar compartimentos disponíveis
    test_endpoint "GET" "/compartimentos/status?ocupado=false" "Listar compartimentos disponíveis"
    
    # Buscar compartimento por ID
    test_endpoint "GET" "/compartimentos/1" "Buscar compartimento por ID"
}

# Função para executar testes de entregas
test_entregas() {
    print_message $YELLOW "🚚 === TESTES DE ENTREGAS ==="
    
    # Listar entregas
    test_endpoint "GET" "/entregas" "Listar todas as entregas"
    
    # Listar entregas por status
    test_endpoint "GET" "/entregas/status/ENTREGUE" "Listar entregas por status"
    
    # Criar nova entrega
    test_endpoint "POST" "/entregas" "Registrar nova entrega" \
        '{"codigoRastreio":"SCRIPT123456","observacao":"Entrega teste script","compartimentoId":1,"entregadorId":3,"destinatarioId":2}'
}

# Função para executar testes de reservas
test_reservas() {
    print_message $YELLOW "📅 === TESTES DE RESERVAS ==="
    
    # Listar reservas
    test_endpoint "GET" "/reservas" "Listar todas as reservas"
    
    # Listar reservas por status
    test_endpoint "GET" "/reservas/status/ATIVA" "Listar reservas ativas"
    
    # Criar nova reserva
    test_endpoint "POST" "/reservas" "Criar nova reserva" \
        '{"dataInicio":"2024-02-01T09:00:00","dataFim":"2024-02-01T18:00:00","observacao":"Reserva teste script","compartimentoId":2,"usuarioId":2}'
}

# Função para executar testes unitários
test_unit_tests() {
    print_message $YELLOW "🔬 === TESTES UNITÁRIOS ==="
    
    print_message $BLUE "🧪 Executando testes unitários..."
    if ./mvnw test -q; then
        print_message $GREEN "✅ Testes unitários passaram"
    else
        print_message $RED "❌ Alguns testes unitários falharam"
        return 1
    fi
}

# Função para gerar relatório de cobertura
generate_coverage_report() {
    print_message $YELLOW "📊 === RELATÓRIO DE COBERTURA ==="
    
    print_message $BLUE "📈 Gerando relatório de cobertura..."
    if ./mvnw jacoco:report -q; then
        print_message $GREEN "✅ Relatório de cobertura gerado"
        print_message $BLUE "📁 Relatório disponível em: target/site/jacoco/index.html"
    else
        print_message $RED "❌ Falha ao gerar relatório de cobertura"
        return 1
    fi
}

# Função para teste de performance básico
test_performance() {
    print_message $YELLOW "⚡ === TESTE DE PERFORMANCE ==="
    
    print_message $BLUE "⏱️ Testando tempo de resposta..."
    
    local start_time=$(date +%s%N)
    test_endpoint "GET" "/usuarios" "Teste de performance - Listar usuários"
    local end_time=$(date +%s%N)
    
    local duration=$(( (end_time - start_time) / 1000000 ))
    
    if [ $duration -lt 2000 ]; then
        print_message $GREEN "✅ Tempo de resposta: ${duration}ms (< 2s)"
    else
        print_message $YELLOW "⚠️ Tempo de resposta: ${duration}ms (> 2s)"
    fi
}

# Função para executar teste de carga básico
test_load() {
    print_message $YELLOW "🔄 === TESTE DE CARGA ==="
    
    print_message $BLUE "🔄 Executando 10 requisições simultâneas..."
    
    local success_count=0
    local total_requests=10
    
    for i in $(seq 1 $total_requests); do
        if test_endpoint "GET" "/usuarios" "Requisição $i" "" 200 > /dev/null 2>&1; then
            ((success_count++))
        fi &
    done
    
    wait
    
    print_message $GREEN "✅ $success_count/$total_requests requisições bem-sucedidas"
    
    if [ $success_count -eq $total_requests ]; then
        print_message $GREEN "✅ Teste de carga passou"
    else
        print_message $YELLOW "⚠️ Algumas requisições falharam no teste de carga"
    fi
}

# Função principal
main() {
    print_message $BLUE "🚀 === INICIANDO TESTES AUTOMATIZADOS ==="
    print_message $BLUE "📅 Data: $(date)"
    echo ""
    
    local failed_tests=0
    
    # Verificar aplicação
    check_application
    
    # Obter token
    get_auth_token
    
    # Executar testes por categoria
    if [ "$1" = "unit" ]; then
        test_unit_tests || ((failed_tests++))
    elif [ "$1" = "api" ]; then
        test_authentication || ((failed_tests++))
        test_usuarios || ((failed_tests++))
        test_condominios || ((failed_tests++))
        test_armarios || ((failed_tests++))
        test_compartimentos || ((failed_tests++))
        test_entregas || ((failed_tests++))
        test_reservas || ((failed_tests++))
    elif [ "$1" = "performance" ]; then
        test_performance || ((failed_tests++))
        test_load || ((failed_tests++))
    elif [ "$1" = "coverage" ]; then
        test_unit_tests || ((failed_tests++))
        generate_coverage_report || ((failed_tests++))
    else
        # Executar todos os testes
        test_authentication || ((failed_tests++))
        test_usuarios || ((failed_tests++))
        test_condominios || ((failed_tests++))
        test_armarios || ((failed_tests++))
        test_compartimentos || ((failed_tests++))
        test_entregas || ((failed_tests++))
        test_reservas || ((failed_tests++))
        test_performance || ((failed_tests++))
        test_unit_tests || ((failed_tests++))
    fi
    
    echo ""
    print_message $BLUE "📊 === RESUMO DOS TESTES ==="
    
    if [ $failed_tests -eq 0 ]; then
        print_message $GREEN "🎉 Todos os testes passaram com sucesso!"
        exit 0
    else
        print_message $RED "❌ $failed_tests categoria(s) de teste falharam"
        exit 1
    fi
}

# Função de ajuda
show_help() {
    echo "🧪 Script de Teste Automatizado - Armário Inteligente"
    echo ""
    echo "Uso: $0 [OPÇÃO]"
    echo ""
    echo "Opções:"
    echo "  (sem opção)  Executar todos os testes"
    echo "  unit         Executar apenas testes unitários"
    echo "  api          Executar apenas testes de API"
    echo "  performance  Executar apenas testes de performance"
    echo "  coverage     Executar testes e gerar relatório de cobertura"
    echo "  help         Mostrar esta ajuda"
    echo ""
    echo "Exemplos:"
    echo "  $0                    # Executar todos os testes"
    echo "  $0 api               # Executar apenas testes de API"
    echo "  $0 unit              # Executar apenas testes unitários"
    echo "  $0 performance       # Executar testes de performance"
    echo "  $0 coverage          # Gerar relatório de cobertura"
}

# Verificar argumentos
if [ "$1" = "help" ] || [ "$1" = "-h" ] || [ "$1" = "--help" ]; then
    show_help
    exit 0
fi

# Executar função principal
main "$1"
