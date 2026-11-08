# Arquitetura proposta — FinanceHub

## Decisão
Monólito Java com HTTP nativo `HttpServer` (sem Spring Boot); PHP como gateway intermediário, frontend web separado e MySQL para persistência. Android Kotlin e iOS Swift serão extensões futuras.

```text
Navegador / Android / iOS
            |
        HTTP + JSON
            v
    API PHP  /api/v1
            |
        HTTP + JSON
            v
     Monólito Java 21
      HttpServer + POO
            |
           JDBC
            v
          MySQL
```

## Domínio planejado
- `Usuario`: proprietário dos registros pessoais.
- `Categoria`: classificação de receitas/despesas.
- `Lancamento` (abstrata): dados comuns e método abstrato de impacto financeiro.
- `Receita` e `Despesa`: duas relações de herança a partir de `Lancamento`.
- `Relatorio` e serviços: totais e consultas sobre coleção de lançamentos.
- `FinanceHubException` (derivada de `Exception`): erros de domínio.

A definição definitiva de atributos, métodos e sobrecargas/sobrescritas acontece na implementação, com testes que comprovem os critérios do PjBL. É necessário **sobrescrever explicitamente um método concreto herdado** além de implementar abstratos.

## Contrato entre plataformas
- JSON UTF-8; `Content-Type: application/json; charset=utf-8`.
- Datas ISO 8601; documentar claramente se o campo é data (`YYYY-MM-DD`) ou instante UTC.
- Dinheiro exposto em **centavos inteiros**, evitando diferenças de precisão entre Swift, Kotlin, PHP e Java. No Java, usar representação exata (ex.: BigDecimal/centavos), nunca float/double para dinheiro.
- IDs e enumerações consistentes; objeto de erro com `code` e `message`.
- APIs versionadas em `/api/v1`; rascunho em `docs/api/openapi.yaml`.

## Ordem de desenvolvimento
1. POO Java + testes.
2. MySQL/JDBC + persistência + importação CSV/TXT.
3. HttpServer + gateway PHP + interface web.
4. Documentação/diagramas/Trello; deploy Azure por último.

**Status:** somente arquitetura e diretórios; nenhuma feature concluída.
