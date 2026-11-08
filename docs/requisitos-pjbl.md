# Checklist do PjBL (extraído do documento da professora)

Marque cada item somente quando estiver implementado, demonstrável e testado.

## Parte A — Fundamentos POO (70%)
- [x] 1. Encapsulamento em classes (`Usuario`, `Categoria`, `Lancamento`, `Relatorio`).
- [x] 2. Pelo menos 5 classes (`Usuario`, `Categoria`, `Lancamento`, `Receita`, `Despesa`, `Relatorio`, `GerenciadorCategorias`, `GerenciadorLancamentos`, `ImportadorArquivoService`, `FinanceHubException`).
- [x] 3. Pelo menos 1 classe abstrata (`Lancamento`).
- [x] 4. Pelo menos 1 método abstrato (`calcularImpacto()` e `getTipo()` em `Lancamento`).
- [x] 5. Ao menos 10 atributos e 10 métodos no total.
- [x] 6. Ao menos 2 relações de herança (`Receita extends Lancamento` e `Despesa extends Lancamento`).
- [x] 7. Sobrescrita de método **concreto** herdado (`getResumo()` sobrescrito em `Receita` e `Despesa`).
- [x] 8. Chamada polimórfica (`lancamento.calcularImpacto()` em `Categoria.calcularSaldo()` e `GerenciadorLancamentos.calcularRelatorio()`).
- [x] 9. Associação entre classes (`Usuario` -> `Lancamento`, `Categoria` -> `Lancamento`).
- [x] 10. Coleção de objetos (`ArrayList<Lancamento>` e `ArrayList<Categoria>`).

## Parte B — Recursos complementares (20%)
- [x] 11. Classe que estende `Exception` (`FinanceHubException extends Exception`).
- [x] 12. Interface gráfica (`FinanceHubSwingFrame` em Java Swing — `javax.swing`).
- [x] 13. Leitura de arquivo CSV ou TXT (`ImportadorArquivoService` + `lancamentos-exemplo.csv`).
- [x] 14. Salvar e recuperar objetos persistentes (`JdbcCategoriaRepository`, `JdbcLancamentoRepository` e `PersistenciaArquivoRepository`).

## Parte C — Recursos organizacionais (10%)
- [x] Repositório GitHub com colaboradores e commits.
- [ ] Kanban no Trello com tarefas atribuídas por pessoa.
- [x] Fluxograma/diagrama simples adicionado em `docs/diagrams/diagramas-pjbl.md` (pronto para anexar ao Trello).

Fonte: `Requisitos PjBL (1).pdf`, fornecido na conversa.
