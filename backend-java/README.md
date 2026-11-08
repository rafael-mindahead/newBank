# Backend Java

Monólito responsável por toda a lógica do gerenciador financeiro e persistência MySQL.

Planejado: Java 21, Maven, `com.sun.net.httpserver.HttpServer`, JDBC e JUnit. **Sem Spring Boot**.

Pacotes: `api/http` (rotas), `api/dto` (contratos), `domain/model` (entidades e herança), `domain/service` (regras de negócio), `domain/exception` (exceções próprias), `persistence/jdbc` (acesso MySQL), `config` (configuração), `app` (inicialização). Tests em `src/test`.

Este commit prepara pastas e build; ainda não inicia um servidor HTTP.
