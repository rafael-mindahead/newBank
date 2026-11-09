# Checklist da Rubrica Oficial — Projeto Final POO

## Requisitos Obrigatórios (70% da nota — 7 itens de 1,0 pt)
- [x] **1. Modelagem de classes e objetos (1,0 pt):** `Usuario`, `Categoria`, `Lancamento`, `Receita`, `Despesa`, `Relatorio`, `GerenciadorCategorias`, `GerenciadorLancamentos`, `ImportadorArquivoService`, `CotacaoMoedaService`.
- [x] **2. Encapsulamento (1,0 pt):** Atributos `private`, validação de estado nos métodos modificadores e cópias defensivas das listas internas.
- [x] **3. Coleção de objetos (1,0 pt):** `ArrayList<Lancamento>` e `ArrayList<Categoria>` com operações completas de cadastro, edição, exclusão, busca e filtragem.
- [x] **4. Associação e interação entre objetos (1,0 pt):** Relacionamento consistente entre `Usuario`, `Categoria` e `Lancamento`, mantendo vínculo bidirecional atualizado e bloqueando exclusão de categoria em uso.
- [x] **5. Herança (1,0 pt):** `Receita extends Lancamento` e `Despesa extends Lancamento`.
- [x] **6. Polimorfismo (1,0 pt):** Cálculo de saldo e relatório via `calcularImpacto()`, `isReceita()` e sobrescrita de `getResumo()` sem uso de `instanceof`.
- [x] **7. Abstração e contratos — Classes abstratas, métodos abstratos e interfaces (1,0 pt):** Classe abstrata `Lancamento` + Interface `RepositorioDados` implementada por `PersistenciaArquivoRepository` e `JdbcRepositorioDados`.

## Recursos Extras (30% da nota — até 3,0 pts)
- [x] **Tratamento de exceções (1,5 pt):** `FinanceHubException extends Exception` + tratamento controlado na UI e serviços.
- [x] **Interface gráfica (1,5 pt):** `FinanceHubSwingFrame` em Java Swing com dashboard de 4 cards, barras de proporção, abas de lançamento/edição, categorias com saldo e filtros na tabela.
- [x] **Persistência de objetos (1,5 pt):** `PersistenciaArquivoRepository` (`data/financehub-dados.db`) + `ImportadorArquivoService` (CSV/TXT).
- [x] **Integração com banco de dados (1,5 pt):** `JdbcCategoriaRepository`, `JdbcLancamentoRepository`, `JdbcRepositorioDados` e `01-schema.sql`.
- [x] **Integração com API (1,5 pt):** `CotacaoMoedaService` consumindo a API externa `economia.awesomeapi.com.br` para cotação `USD-BRL`.
