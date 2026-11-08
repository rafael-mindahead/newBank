# FinanceHub

Gerenciador financeiro pessoal desenvolvido para o PjBL de Programação Orientada a Objetos.

## Escopo
Controle de receitas, despesas, categorias, histórico, importação/exportação CSV/TXT e relatórios pessoais. **Não** inclui contas bancárias, PIX ou transferências.

## Arquitetura implementada
- **backend-java/** — monólito Java 21 com POO completa, interface gráfica desktop em **Java Swing** (`FinanceHubSwingFrame`), regras de negócio, exceção própria (`FinanceHubException`), importação/exportação CSV/TXT, API HTTP nativa (`HttpServer` do JDK) e persistência MySQL via JDBC + persistência local em arquivo (`data/financehub-dados.db`).
- **infra/** — configuração Docker Compose e schema SQL inicial (`infra/mysql/init/01-schema.sql`).
- **docs/** — arquitetura, checklist dos requisitos e diagramas (`docs/diagrams/diagramas-pjbl.md`).

## Como executar a Interface Gráfica Desktop (Java Swing)
Na raiz do projeto, execute no PowerShell:
```powershell
.\scripts\iniciar-servidor.ps1
```
*(Ou via Maven dentro de `backend-java/`: `mvn compile exec:java -Dexec.mainClass="br.com.financehub.app.FinanceHubApplication"`).*

## Como executar os testes (JUnit 5)
No diretório `backend-java/`:
```bash
mvn clean test
```
