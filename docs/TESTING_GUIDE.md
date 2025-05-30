# 🧪 Guia Completo de Testes - Armário Inteligente

Este guia foi criado para ajudar você a testar todas as funcionalidades do sistema Armário Inteligente, mesmo se você não tiver muita experiência técnica. Vamos explicar cada passo de forma clara e simples.

## 📋 Índice

1. [O que você vai precisar](#o-que-você-vai-precisar)
2. [Configuração Inicial](#configuração-inicial)
3. [Testes com Postman](#testes-com-postman)
4. [Testes Manuais Passo a Passo](#testes-manuais-passo-a-passo)
5. [Testes Automatizados](#testes-automatizados)
6. [Solução de Problemas Comuns](#solução-de-problemas-comuns)

---

## O que você vai precisar

Antes de começar, certifique-se de que você tem:

- ✅ **Java** instalado no seu computador (versão 17 ou superior)
- ✅ **Postman** instalado para testar a API ([Baixe aqui](https://www.postman.com/downloads/))
- ✅ Um navegador web como Chrome, Firefox ou Edge
- ✅ O projeto Armário Inteligente baixado no seu computador

Se você não tiver certeza se tem Java instalado, abra o terminal (Prompt de Comando no Windows) e digite:

\`\`\`bash
java -version
\`\`\`

Você deve ver algo como `java version "17.0.x"` ou superior. Se aparecer um erro, você precisará instalar o Java primeiro.

---

## Configuração Inicial

### Passo 1: Iniciar a aplicação

1. **Abra o terminal** (Prompt de Comando no Windows)
2. **Navegue até a pasta do projeto**:
   \`\`\`bash
   cd caminho/para/armario-inteligente
   \`\`\`
   
   > 💡 **Dica**: Substitua "caminho/para/armario-inteligente" pelo caminho real onde você salvou o projeto.

3. **Inicie a aplicação**:
   
   No Windows:
   \`\`\`bash
   mvnw.cmd spring-boot:run
   \`\`\`
   
   No Mac ou Linux:
   \`\`\`bash
   ./mvnw spring-boot:run
   \`\`\`

4. **Aguarde** até ver uma mensagem como esta:
   \`\`\`
   Started ArmariointeligenteApplication in X.XXX seconds
   \`\`\`

### Passo 2: Verificar se está funcionando

1. **Abra seu navegador**
2. **Digite o endereço**: http://localhost:8080/actuator/health
3. **Você deve ver**: `{"status":"UP"}`

Se você viu essa mensagem, parabéns! A aplicação está rodando corretamente.

---

## Testes com Postman

O Postman é uma ferramenta que nos permite testar APIs de forma fácil e visual. Vamos usá-lo para testar nosso sistema.

### Passo 1: Importar a coleção de testes

1. **Abra o Postman**
2. **Clique no botão "Import"** (geralmente no canto superior esquerdo)
3. **Selecione o arquivo**: `postman/Armario-Inteligente.postman_collection.json` da pasta do projeto
4. **Clique em "Import"**

Você verá uma nova coleção chamada "Armário Inteligente" no painel esquerdo.

### Passo 2: Configurar o ambiente

1. **Clique no ícone de engrenagem** (canto superior direito)
2. **Selecione "Add"** para criar um novo ambiente
3. **Dê o nome**: "Armario Local"
4. **Adicione estas variáveis**:

| Variável | Valor Inicial | Valor Atual |
|----------|---------------|-------------|
| `base_url` | `http://localhost:8080/api/v1` | `http://localhost:8080/api/v1` |
| `token` | (deixe vazio) | (deixe vazio) |

5. **Clique em "Save"**
6. **Selecione "Armario Local"** no menu suspenso de ambientes (canto superior direito)

### Passo 3: Fazer login para obter o token

1. **Expanda a pasta "Autenticação"** na coleção
2. **Clique em "Login Admin"**
3. **Clique no botão "Send"**

Você deve receber uma resposta com status 200 OK e um token. Este token será automaticamente salvo para os próximos testes.

> 💡 **O que aconteceu?** Você acabou de fazer login como administrador e recebeu um token de acesso que será usado para as próximas requisições.

---

## Testes Manuais Passo a Passo

Agora vamos testar cada funcionalidade do sistema manualmente. Siga os passos abaixo:

### Teste 1: Listar Usuários

1. **Expanda a pasta "Usuários"** na coleção do Postman
2. **Clique em "Listar Todos os Usuários"**
3. **Clique no botão "Send"**

Você deve receber uma lista de usuários em formato JSON. Se tudo estiver correto, você verá pelo menos o usuário administrador.

> 💡 **O que é JSON?** É um formato de texto usado para transmitir dados. Parece uma lista ou dicionário com chaves e valores.

### Teste 2: Criar um Novo Usuário

1. **Na pasta "Usuários"**, clique em "Criar Novo Usuário"
2. **Verifique o corpo da requisição** (aba "Body"):
   \`\`\`json
   {
     "nome": "Teste Manual",
     "email": "teste.manual@exemplo.com",
     "senha": "senha123",
     "telefone": "(81) 98765-4321",
     "tipoUsuarioId": 2
   }
   \`\`\`
3. **Clique no botão "Send"**

Você deve receber uma resposta com status 200 OK e os dados do usuário criado.

### Teste 3: Buscar um Usuário Específico

1. **Na pasta "Usuários"**, clique em "Buscar Usuário por ID"
2. **Altere o ID na URL** para o ID do usuário que você acabou de criar
3. **Clique no botão "Send"**

Você deve receber os detalhes desse usuário específico.

### Teste 4: Testar o Fluxo de Entrega

Vamos testar o fluxo completo de uma entrega:

1. **Expanda a pasta "Compartimentos"**
2. **Clique em "Listar Compartimentos Disponíveis"**
3. **Clique em "Send"** para ver compartimentos disponíveis
4. **Anote o ID** de um compartimento disponível

5. **Expanda a pasta "Entregas"**
6. **Clique em "Registrar Nova Entrega"**
7. **Modifique o corpo da requisição**:
   \`\`\`json
   {
     "codigoRastreio": "TESTE123456",
     "observacao": "Teste manual de entrega",
     "compartimentoId": 1,  // Use o ID que você anotou
     "entregadorId": 3,
     "destinatarioId": 2
   }
   \`\`\`
8. **Clique em "Send"**

9. **Clique em "Listar Todas as Entregas"**
10. **Clique em "Send"** para verificar se sua entrega foi registrada

> 💡 **O que aconteceu?** Você acabou de simular o registro de uma entrega no sistema, verificando primeiro quais compartimentos estavam disponíveis.

---

## Testes Automatizados

O sistema também possui testes automatizados que verificam se tudo está funcionando corretamente. Vamos executá-los:

### Executar Testes Unitários

1. **Abra um novo terminal** (deixe a aplicação rodando no terminal anterior)
2. **Navegue até a pasta do projeto**:
   \`\`\`bash
   cd caminho/para/armario-inteligente
   \`\`\`
3. **Execute os testes**:
   
   No Windows:
   \`\`\`bash
   mvnw.cmd test
   \`\`\`
   
   No Mac ou Linux:
   \`\`\`bash
   ./mvnw test
   \`\`\`

4. **Aguarde** até ver uma mensagem como:
   \`\`\`
   [INFO] Tests run: XX, Failures: 0, Errors: 0, Skipped: 0
   \`\`\`

Se você não vir nenhuma falha, os testes foram bem-sucedidos!

### Executar o Script de Teste Completo

Temos um script que testa automaticamente todas as funcionalidades principais:

1. **No terminal**:
   
   No Windows:
   \`\`\`bash
   scripts\test-runner.bat
   \`\`\`
   
   No Mac ou Linux:
   \`\`\`bash
   chmod +x scripts/test-runner.sh
   ./scripts/test-runner.sh
   \`\`\`

2. **Observe os resultados** que aparecerão no terminal

Este script testa todas as funcionalidades principais e mostra os resultados de forma clara.

---

## Solução de Problemas Comuns

### Problema 1: A aplicação não inicia

**Sintoma**: Você vê mensagens de erro ao tentar iniciar a aplicação.

**Solução**:
1. **Verifique se a porta 8080 está livre**:
   
   No Windows:
   \`\`\`bash
   netstat -ano | findstr :8080
   \`\`\`
   
   No Mac ou Linux:
   \`\`\`bash
   lsof -i :8080
   \`\`\`

2. **Se a porta estiver em uso**, você pode:
   - Encerrar o processo que está usando a porta, ou
   - Mudar a porta da aplicação:
     \`\`\`bash
     mvnw spring-boot:run -Dspring-boot.run.arguments=--server.port=8081
     \`\`\`
     E então acessar http://localhost:8081 em vez de 8080.

### Problema 2: Erro de autenticação (401 Unauthorized)

**Sintoma**: Você recebe um erro 401 ao tentar acessar um endpoint protegido.

**Solução**:
1. **Faça login novamente** para obter um novo token
2. **Verifique se o token está sendo enviado corretamente** no cabeçalho Authorization
3. **Certifique-se de que o formato está correto**: `Bearer seu-token-aqui`

### Problema 3: Erro "Not Found" (404)

**Sintoma**: Você recebe um erro 404 ao tentar acessar um recurso.

**Solução**:
1. **Verifique se a URL está correta**
2. **Certifique-se de que o ID existe** no sistema
3. **Verifique se você está usando a versão correta da API** (/api/v1)

### Problema 4: Java não encontrado

**Sintoma**: Você vê uma mensagem dizendo que o comando 'java' não foi encontrado.

**Solução**:
1. **Instale o Java** (versão 17 ou superior)
2. **Configure a variável de ambiente JAVA_HOME**
3. **Adicione Java ao PATH do sistema**

---

## Exemplos de Comandos Úteis

### Verificar o Status da Aplicação
\`\`\`bash
curl http://localhost:8080/actuator/health
\`\`\`

### Fazer Login via Terminal
\`\`\`bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@smartlocker.com","senha":"admin123"}'
\`\`\`

### Listar Usuários via Terminal
\`\`\`bash
curl http://localhost:8080/api/v1/usuarios \
  -H "Authorization: Bearer seu-token-aqui"
\`\`\`

---

## Checklist de Testes

Use esta lista para garantir que você testou todas as funcionalidades principais:

- [ ] **Autenticação**
  - [ ] Login como administrador
  - [ ] Login como cliente
  - [ ] Tentativa com credenciais inválidas

- [ ] **Usuários**
  - [ ] Listar todos os usuários
  - [ ] Criar novo usuário
  - [ ] Buscar usuário por ID
  - [ ] Atualizar usuário
  - [ ] Desativar usuário

- [ ] **Condomínios**
  - [ ] Listar condomínios
  - [ ] Criar novo condomínio
  - [ ] Buscar condomínio por ID

- [ ] **Armários**
  - [ ] Listar armários
  - [ ] Listar armários por condomínio
  - [ ] Criar novo armário

- [ ] **Compartimentos**
  - [ ] Listar compartimentos
  - [ ] Listar compartimentos por armário
  - [ ] Listar compartimentos disponíveis
  - [ ] Atualizar status do compartimento

- [ ] **Entregas**
  - [ ] Listar entregas
  - [ ] Registrar nova entrega
  - [ ] Buscar entrega por ID
  - [ ] Registrar retirada de entrega

- [ ] **Reservas**
  - [ ] Listar reservas
  - [ ] Criar nova reserva
  - [ ] Cancelar reserva

---

**🎉 Parabéns!** Você concluiu o guia de testes do sistema Armário Inteligente. Se tiver dúvidas ou encontrar problemas, consulte a documentação adicional ou entre em contato com a equipe de suporte.
