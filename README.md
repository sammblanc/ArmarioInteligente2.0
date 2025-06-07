# Armário Inteligente - Sistema de Gerenciamento

## Visão Geral

O Armário Inteligente é um sistema de gerenciamento para condomínios que permite controlar armários inteligentes para recebimento e armazenamento de encomendas. O sistema oferece funcionalidades para gerenciar condomínios, armários, compartimentos, entregas e reservas.

## Requisitos do Sistema

- Java 17 ou superior
- Maven 3.6 ou superior
- PostgreSQL (para ambiente de produção)
- H2 Database (para ambiente de desenvolvimento/testes)
- Postman para testar os endpoints

## Tecnologias Utilizadas

- Spring Boot 3.2.3
- Spring Security com JWT
- Spring Data JPA
- Swagger/OpenAPI para documentação
- Lombok para redução de código boilerplate
- H2 Database (desenvolvimento) / PostgreSQL (produção)

## Configuração do Ambiente

### Clonando o Repositório

```bash
git clone https://github.com/sammblanc/ArmarioInteligente2.0.git
cd armario-inteligente
```

### Configuração do Banco de Dados

O projeto está configurado para usar H2 Database em memória para desenvolvimento e PostgreSQL para produção.

#### Configuração do PostgreSQL (Produção)

1. Crie um banco de dados PostgreSQL:

```sql
CREATE DATABASE armariointeligente;
CREATE USER armariointeligente WITH ENCRYPTED PASSWORD 'sua_senha';
GRANT ALL PRIVILEGES ON DATABASE armariointeligente TO armariointeligente;
```

2. Configure as credenciais no arquivo `src/main/resources/application-prod.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/armariointeligente
spring.datasource.username=armariointeligente
spring.datasource.password=sua_senha
spring.datasource.driver-class-name=org.postgresql.Driver
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
```

## Compilação e Execução

### Compilando o Projeto

```bash
./mvnw clean package
```

### Executando o Projeto

#### Ambiente de Desenvolvimento (H2 Database)

```bash
./mvnw spring-boot:run
```

#### Ambiente de Produção (PostgreSQL)

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod
```

## Acessando a Aplicação

- **API REST**: http://localhost:8080/api/v1
- **Documentação Swagger**: http://localhost:8080/swagger-ui.html
- **Console H2 (apenas em dev)**: http://localhost:8080/h2-console

## Autenticação

O sistema utiliza autenticação JWT. Para obter um token, faça uma requisição POST para `/api/v1/auth/login` com as seguintes credenciais:

```json
{
  "email": "admin@smartlocker.com",
  "senha": "admin123"
}
```

Use o token retornado no cabeçalho `Authorization` das requisições subsequentes:

```
Authorization: Bearer seu_token_jwt
```

## Estrutura do Projeto

```
src/main/java/br/com/unit/tokseg/armariointeligente/
├── ArmariointeligenteApplication.java
├── config/
│   └── DataInitializer.java
├── controller/
│   ├── ArmarioController.java
│   ├── AuthController.java
│   ├── CompartimentoController.java
│   ├── CondominioController.java
│   ├── EntregaController.java
│   ├── ReservaController.java
│   ├── TipoUsuarioController.java
│   └── UsuarioController.java
├── exception/
│   ├── BadRequestException.java
│   ├── ErrorResponse.java
│   ├── GlobalExceptionHandler.java
│   ├── RelatedResourceException.java
│   ├── ResourceAlreadyExistsException.java
│   └── ResourceNotFoundException.java
├── model/
│   ├── Armario.java
│   ├── Compartimento.java
│   ├── Condominio.java
│   ├── Entrega.java
│   ├── Reserva.java
│   ├── StatusEntrega.java
│   ├── StatusReserva.java
│   ├── TipoUsuario.java
│   ├── Usuario.java
│   └── auth/
│       ├── AuthRequest.java
│       └── AuthResponse.java
├── repository/
│   ├── ArmarioRepository.java
│   ├── CompartimentoRepository.java
│   ├── CondominioRepository.java
│   ├── EntregaRepository.java
│   ├── ReservaRepository.java
│   ├── TipoUsuarioRepository.java
│   └── UsuarioRepository.java
├── security/
│   ├── AuthEntryPointJwt.java
│   ├── AuthTokenFilter.java
│   ├── JwtUtils.java
│   ├── UserDetailsImpl.java
│   ├── UserDetailsServiceImpl.java
│   └── WebSecurityConfig.java
└── service/
    ├── ArmarioService.java
    ├── CompartimentoService.java
    ├── CondominioService.java
    ├── EntregaService.java
    ├── EntregaServiceImpl.java
    ├── ReservaService.java
    ├── TipoUsuarioService.java
    └── UsuarioService.java
```

## Endpoints da API

A API está organizada nos seguintes grupos de endpoints:

### Autenticação
- `POST /api/v1/auth/login` - Autenticar usuário

### Usuários
- `GET /api/v1/usuarios` - Listar todos os usuários
- `GET /api/v1/usuarios/{id}` - Buscar usuário por ID
- `POST /api/v1/usuarios` - Criar novo usuário
- `PUT /api/v1/usuarios/{id}` - Atualizar usuário
- `DELETE /api/v1/usuarios/{id}` - Deletar usuário
- `PUT /api/v1/usuarios/{id}/desativar` - Desativar usuário
- `PUT /api/v1/usuarios/{id}/ativar` - Ativar usuário
- `GET /api/v1/usuarios/ativos` - Listar usuários ativos

### Tipos de Usuário
- `GET /api/v1/tipos-usuarios` - Listar todos os tipos de usuário
- `GET /api/v1/tipos-usuarios/{id}` - Buscar tipo de usuário por ID
- `POST /api/v1/tipos-usuarios` - Criar novo tipo de usuário
- `PUT /api/v1/tipos-usuarios/{id}` - Atualizar tipo de usuário
- `DELETE /api/v1/tipos-usuarios/{id}` - Deletar tipo de usuário

### Condomínios
- `GET /api/v1/condominios` - Listar todos os condomínios
- `GET /api/v1/condominios/{id}` - Buscar condomínio por ID
- `POST /api/v1/condominios` - Criar novo condomínio
- `PUT /api/v1/condominios/{id}` - Atualizar condomínio
- `DELETE /api/v1/condominios/{id}` - Deletar condomínio

### Armários
- `GET /api/v1/armarios` - Listar todos os armários
- `GET /api/v1/armarios/{id}` - Buscar armário por ID
- `GET /api/v1/armarios/condominio/{condominioId}` - Listar armários por condomínio
- `POST /api/v1/armarios` - Criar novo armário
- `PUT /api/v1/armarios/{id}` - Atualizar armário
- `DELETE /api/v1/armarios/{id}` - Deletar armário

### Compartimentos
- `GET /api/v1/compartimentos` - Listar todos os compartimentos
- `GET /api/v1/compartimentos/{id}` - Buscar compartimento por ID
- `GET /api/v1/compartimentos/armario/{armarioId}` - Listar compartimentos por armário
- `GET /api/v1/compartimentos/status?ocupado=true|false` - Listar compartimentos por status
- `POST /api/v1/compartimentos` - Criar novo compartimento
- `PUT /api/v1/compartimentos/{id}` - Atualizar compartimento
- `PUT /api/v1/compartimentos/{id}/status?ocupado=true|false` - Atualizar status do compartimento
- `PUT /api/v1/compartimentos/{id}/codigo-acesso` - Gerar novo código de acesso
- `DELETE /api/v1/compartimentos/{id}` - Deletar compartimento

### Entregas
- `GET /api/v1/entregas` - Listar todas as entregas
- `GET /api/v1/entregas/{id}` - Buscar entrega por ID
- `GET /api/v1/entregas/compartimento/{compartimentoId}` - Listar entregas por compartimento
- `GET /api/v1/entregas/entregador/{entregadorId}` - Listar entregas por entregador
- `GET /api/v1/entregas/destinatario/{destinatarioId}` - Listar entregas por destinatário
- `GET /api/v1/entregas/status/{status}` - Listar entregas por status
- `GET /api/v1/entregas/rastreio/{codigoRastreio}` - Buscar entrega por código de rastreio
- `GET /api/v1/entregas/periodo?inicio=X&fim=Y` - Listar entregas por período
- `POST /api/v1/entregas` - Registrar nova entrega
- `PUT /api/v1/entregas/{id}/retirada?codigoAcesso=X` - Registrar retirada de entrega
- `PUT /api/v1/entregas/{id}/cancelar` - Cancelar entrega

### Reservas
- `GET /api/v1/reservas` - Listar todas as reservas
- `GET /api/v1/reservas/{id}` - Buscar reserva por ID
- `GET /api/v1/reservas/compartimento/{compartimentoId}` - Listar reservas por compartimento
- `GET /api/v1/reservas/usuario/{usuarioId}` - Listar reservas por usuário
- `GET /api/v1/reservas/status/{status}` - Listar reservas por status
- `GET /api/v1/reservas/periodo?inicio=X&fim=Y` - Listar reservas por período
- `POST /api/v1/reservas` - Criar nova reserva
- `PUT /api/v1/reservas/{id}/cancelar` - Cancelar reserva
- `PUT /api/v1/reservas/{id}/concluir` - Concluir reserva

## Dados Iniciais

O sistema é inicializado com os seguintes dados:

### Tipos de Usuário
- Administrador
- Morador
- Funcionário
- Entregador

### Usuários
- Admin: admin@smartlocker.com / admin123
- Morador: joao.silva@exemplo.com / senha123
- Entregador: maria.oliveira@exemplo.com / senha456

### Condomínios
- Residencial Parque das Flores
- Edifício Solar das Palmeiras

### Armários e Compartimentos
- Vários armários e compartimentos são criados para cada condomínio

## Solução de Problemas

### Erro de Conexão com o Banco de Dados
- Verifique se o PostgreSQL está em execução (para perfil prod)
- Verifique as credenciais no arquivo application-prod.properties
- Certifique-se de que o banco de dados foi criado

### Erro de Autenticação
- Verifique se está usando as credenciais corretas
- Certifique-se de que o token JWT está sendo enviado corretamente no cabeçalho Authorization

### Erro ao Iniciar a Aplicação
- Verifique se a porta 8080 não está sendo usada por outro processo
- Verifique os logs para identificar possíveis erros

## Contribuição

Para contribuir com o projeto:

1. Faça um fork do repositório
2. Crie uma branch para sua feature (`git checkout -b feature/nova-feature`)
3. Faça commit das suas alterações (`git commit -m 'Adiciona nova feature'`)
4. Faça push para a branch (`git push origin feature/nova-feature`)
5. Abra um Pull Request

## Licença

Este projeto está licenciado sob a licença MIT - veja o arquivo LICENSE para mais detalhes.
```

```xml file="pom.xml"
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.3</version>
        <relativePath/> &lt;!-- lookup parent from repository -->
    </parent>
    <groupId>br.com.unit.tokseg</groupId>
    <artifactId>armariointeligente</artifactId>
    <version>1.0.0</version>
    <name>Armário Inteligente</name>
    <description>Sistema de gerenciamento de armários inteligentes para condomínios</description>
    <properties>
        <java.version>17</java.version>
        <jjwt.version>0.11.5</jjwt.version>
    </properties>
    <dependencies>
        &lt;!-- Spring Boot Starters -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        
        &lt;!-- JWT -->
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-api</artifactId>
            <version>${jjwt.version}</version>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-impl</artifactId>
            <version>${jjwt.version}</version>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-jackson</artifactId>
            <version>${jjwt.version}</version>
            <scope>runtime</scope>
        </dependency>
        
        &lt;!-- Swagger/OpenAPI -->
        <dependency>
            <groupId>org.springdoc</groupId>
            <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
            <version>2.3.0</version>
        </dependency>
        
        &lt;!-- Database -->
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <scope>runtime</scope>
        </dependency>
        
        &lt;!-- Development Tools -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-devtools</artifactId>
            <scope>runtime</scope>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        
        &lt;!-- Testing -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.security</groupId>
            <artifactId>spring-security-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
        </plugins>
    </build>

    <profiles>
        <profile>
            <id>dev</id>
            <activation>
                <activeByDefault>true</activeByDefault>
            </activation>
            <properties>
                <spring.profiles.active>dev</spring.profiles.active>
            </properties>
        </profile>
        <profile>
            <id>prod</id>
            <properties>
                <spring.profiles.active>prod</spring.profiles.active>
            </properties>
        </profile>
    </profiles>
</project>
