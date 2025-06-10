# Armário Inteligente - Sistema de Gerenciamento - TOKSEG

Sistema de gerenciamento de armários inteligentes para condomínios, desenvolvido com Spring Boot, Spring Security e JWT.

## Visão Geral

O **Armário Inteligente** é um sistema back-end robusto para a gestão de lockers em condomínios. 
Ele permite o controle completo de entregas e reservas de encomendas, oferecendo uma solução segura e automatizada para moradores, entregadores e administradores.
A API RESTful foi projetada para ser escalável, segura e fácil de integrar com diferentes tipos de front-end.

## Funcionalidades

-   **Autenticação e Autorização**: Sistema de segurança baseado em Roles com JSON Web Tokens (JWT).
-   **Gerenciamento Completo**: Módulos para gerenciar Condomínios, Armários, Compartimentos, Usuários e Tipos de Usuário.
-   **Controle de Fluxo de Entregas**: API para registrar entregas, registrar retiradas (com validação por código de acesso) e cancelar operações.
-   **Sistema de Reservas**: Funcionalidades para criar, cancelar e concluir reservas de compartimentos.
-   **Consultas Avançadas**: Endpoints para buscar recursos por múltiplos critérios, como status, período, usuário, entre outros.
-   **Documentação Interativa**: Documentação completa da API gerada com Swagger (OpenAPI) para fácil visualização e teste dos endpoints.

## Tecnologias Utilizadas

Este projeto foi construído utilizando um conjunto de tecnologias modernas e consolidadas no ecossistema Java.

| Tecnologia   | Versão           | Descrição |
| :--- | :--- | :--- |
| **Java**     |   17   | Linguagem de programação principal. |
| **Spring Boot**| 3.2.3 | Framework principal para a construção da aplicação. |
| **Spring Data JPA**| - | Persistência de dados em banco relacional. |
| **Spring Security**| - | Implementação de autenticação e autorização. |
| **JWT (io.jsonwebtoken)** | 0.11.5 | Geração e validação de tokens de autenticação. |
| **Maven** | 3.6+ | Ferramenta de gerenciamento de dependências e build. |
| **PostgreSQL**| - | Banco de dados para o ambiente de produção. |
| **H2 Database**| - | Banco de dados em memória para o ambiente de desenvolvimento. |
| **Swagger (springdoc-openapi)**| 2.3.0 | Documentação e teste interativo da API. |
| **Lombok**| - | Redução de código boilerplate em classes Java. |

## Como Executar o Projeto

### Pré-requisitos

Antes de começar, garanta que você tem os seguintes softwares instalados:
-   Java 17 ou superior
-   Maven 3.6 ou superior
-   PostgreSQL (caso queira rodar em ambiente de produção)
-   Um cliente de API como o Postman

### 1. Clonando o Repositório

```bash
git clone [https://github.com/sammblanc/ArmarioInteligente2.0.git](https://github.com/sammblanc/ArmarioInteligente2.0.git)
cd ArmarioInteligente2.0
```

O projeto utiliza perfis do Spring para separar as configurações de banco de dados: dev (padrão) e prod.

Ambiente de Desenvolvimento (dev): Utiliza o H2 Database em memória. Nenhuma configuração adicional é necessária.

Ambiente de Produção (prod): Utiliza o PostgreSQL. Siga os passos abaixo:
```
Crie um banco de dados e um usuário no PostgreSQL:
CREATE DATABASE armariointeligente;
CREATE USER armariointeligente WITH ENCRYPTED PASSWORD 'sua_senha';
GRANT ALL PRIVILEGES ON DATABASE armariointeligente TO armariointeligente;

Configure suas credenciais no arquivo src/main/resources/application-prod.properties:
Properties
spring.datasource.url=jdbc:postgresql://localhost:5432/armariointeligente
spring.datasource.username=armariointeligente
spring.datasource.password=sua_senha
```
Compilando e Executando
Para compilar o projeto:
```Bash

./mvnw clean package
```
Para executar em ambiente de desenvolvimento (H2):

```Bash

./mvnw spring-boot:run
```
Para executar em ambiente de produção (PostgreSQL):
```Bash

./mvnw spring-boot:run -Dspring-boot.run.profiles=prod
```

Acesso à Aplicação
API Base URL: http://localhost:8080/api/v1
Documentação Swagger: http://localhost:8080/swagger-ui.html
Console H2 (em modo dev): http://localhost:8080/h2-console
JDBC URL: jdbc:h2:mem:testdb
User Name: sa
Password: (deixe em branco)

Autenticação
A API utiliza autenticação via JWT. Para acessar os endpoints protegidos, primeiro obtenha um token de acesso.

Envie uma requisição POST para /api/v1/auth/login com as credenciais de um usuário cadastrado. O sistema é inicializado com um usuário administrador:
```
{
  "email": "admin@smartlocker.com",
  "senha": "admin123"
}
```

Use o token retornado no cabeçalho Authorization das requisições seguintes:
```
Authorization: Bearer <seu_token_jwt>
```

Documentação da API (Endpoints)
A lista completa e interativa de endpoints está disponível na Documentação Swagger. Os principais recursos disponíveis na API são:

Autenticação
Usuários
Tipos de Usuário
Condomínios
Armários
Compartimentos
Entregas
Reservas


Este projeto está licenciado sob a licença MIT. Veja o arquivo LICENSE para mais detalhes.
