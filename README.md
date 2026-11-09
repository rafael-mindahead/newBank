# FinanceHub — Gerenciador Financeiro Pessoal (POO Java)

Sistema computacional desktop para controle de finanças pessoais desenvolvido como **Projeto Final da disciplina de Programação Orientada a Objetos (POO)**.

---

## 1. Descrição do Problema e Funcionalidades

Muitas pessoas têm dificuldade em acompanhar suas entradas (receitas) e saídas (despesas) mensais, classificar seus gastos por categoria e visualizar o impacto real de cada movimentação no orçamento.

O **FinanceHub** resolve esse problema oferecendo:
- **Cadastro, Edição e Exclusão de Lançamentos:** Registro de receitas e despesas com descrição, valor, data e categoria associada.
- **Gerenciamento de Categorias com Saldo Individual:** Criação de categorias (ex.: *Trabalho*, *Alimentação*, *Moradia*) com cálculo automático do saldo de cada categoria e proteção contra exclusão de categorias em uso.
- **Dashboard Consolidado e Relatórios:** Cálculo automático do Total de Receitas, Total de Despesas e Saldo Consolidado, com filtros por período de datas, tipo de lançamento e busca textual.
- **Importação e Exportação de Arquivos CSV/TXT:** Leitura em lote de lançamentos a partir de arquivos texto (`.csv` ou `.txt`) e exportação do histórico.
- **Conversão de Saldo para Dólar via API Externa:** Consulta à API pública *AwesomeAPI* (`economia.awesomeapi.com.br`) para exibir a cotação atual do Dólar (`USD-BRL`) e converter o saldo consolidado.
- **Persistência Automática de Objetos:** Os dados cadastrados são salvos e recuperados automaticamente entre execuções do programa.

---

## 2. Integrantes da Equipe

- **Rodrigo Amaral** (`@istampz`)
- **Rafael** (`@rafael-mindahead`)
*(Adicione aqui os demais integrantes da equipe, se houver)*

---

## 3. Decisões de Modelagem e Aplicação dos Conceitos de POO (70%)

Abaixo está a justificativa técnica de cada conceito obrigatório aplicado no código (`backend-java/src/main/java/br/com/financehub/`):

1. **Classes e Objetos (`domain/model` e `domain/service`):**
   - Cada classe possui responsabilidade única e bem definida: `Usuario` (titular dos lançamentos), `Categoria` (agrupador financeiro), `Lancamento` / `Receita` / `Despesa` (movimentações), `Relatorio` (consolidação de totais) e os serviços `GerenciadorLancamentos`, `GerenciadorCategorias`, `ImportadorArquivoService` e `CotacaoMoedaService`.
2. **Encapsulamento:**
   - Todos os atributos das classes de domínio são `private`.
   - O estado interno é protegido por validações nos métodos modificadores (impedindo descrição vazia, valores negativos/zero ou datas nulas) e pelo retorno de **cópias defensivas** (`new ArrayList<>(...)`) em `Categoria.getLancamentos()`, `Usuario.getLancamentos()` e `GerenciadorLancamentos.listar()`, evitando que código externo adultere as listas internas dos objetos.
3. **Coleção de Objetos:**
   - Uso de `ArrayList<Lancamento>` e `ArrayList<Categoria>` para armazenar, buscar por ID, filtrar por período/texto, atualizar e remover objetos em memória.
4. **Associação e Interação entre Objetos:**
   - `Usuario` possui uma coleção de `Lancamento`.
   - `Lancamento` está associado a uma `Categoria`, e `Categoria` mantém os lançamentos a ela vinculados. Ao cadastrar, editar ou excluir um lançamento no `GerenciadorLancamentos`, a associação com a `Categoria` é atualizada automaticamente.
5. **Herança:**
   - `Receita` e `Despesa` estendem a superclasse `Lancamento` (`extends Lancamento`), reaproveitando os atributos comuns (`id`, `descricao`, `valor`, `data`, `categoria`, `usuarioId`) e especializando o comportamento financeiro.
6. **Polimorfismo:**
   - O método `calcularImpacto()` retorna valor positivo (`+valor`) em `Receita` e valor negativo (`-valor`) em `Despesa`.
   - O método concreto `getResumo()` da classe `Lancamento` é sobrescrito (`@Override`) em `Receita` e `Despesa` (chamando `super.getResumo()`).
   - Dessa forma, `Categoria.calcularSaldo()` e `GerenciadorLancamentos.calcularRelatorio()` percorrem uma lista genérica de `Lancamento` e calculam o saldo polimorficamente, **sem usar `instanceof`**.
7. **Classes Abstratas, Métodos Abstratos e Interfaces (Abstração e Contratos):**
   - **Classe e métodos abstratos:** `Lancamento` é `abstract` (não faz sentido instanciar um lançamento genérico que não seja receita nem despesa) e define os métodos abstratos `calcularImpacto()`, `getTipo()` e `isReceita()`.
   - **Interface:** A interface `RepositorioDados` (`persistence/RepositorioDados.java`) define o contrato para salvar e recuperar objetos (`salvarObjetos` e `recuperarObjetos`), implementada por `PersistenciaArquivoRepository` e `JdbcRepositorioDados`. Assim, a interface gráfica depende apenas do contrato (`RepositorioDados`), desacoplando a tela do mecanismo de armazenamento.

---

## 4. Recursos Extras Implementados (30%)

1. **Tratamento de Exceções:** Criação da exceção verificada própria `FinanceHubException extends Exception` (`domain/exception/FinanceHubException.java`), utilizada nos serviços de domínio, importação e persistência, com exibição amigável de mensagens na interface gráfica.
2. **Interface Gráfica (Java Swing):** Janela desktop completa (`ui/FinanceHubSwingFrame.java`) construída com `javax.swing`, contendo 4 cards de resumo com barras de proporção, abas para cadastro/edição de lançamentos, gerenciamento de categorias com saldo individual, importação/exportação CSV e tabela com filtros combinados.
3. **Persistência de Objetos:** Armazenamento e recuperação automática das categorias e lançamentos no arquivo `data/financehub-dados.db` (`PersistenciaArquivoRepository`) e suporte a importação/exportação CSV/TXT (`ImportadorArquivoService`).
4. **Integração com Banco de Dados:** Repositórios JDBC para MySQL (`JdbcCategoriaRepository`, `JdbcLancamentoRepository` e `JdbcRepositorioDados`) e script de criação de tabelas em `infra/mysql/init/01-schema.sql`.
5. **Integração com API Externa:** Classe `CotacaoMoedaService` (`domain/service/CotacaoMoedaService.java`) que consome via HTTP a API pública de cotações (`https://economia.awesomeapi.com.br/json/last/USD-BRL`) para converter o saldo em Reais para Dólares na tela principal.

---

## 5. Como Executar o Projeto

### Opção 1 — Pelo atalho `.bat` na raiz (Windows):
```powershell
.\ABRIR_FINANCEHUB.bat
```

### Opção 2 — Executando o JAR diretamente pelo Java 21:
```powershell
java -jar backend-java\target\financehub-core-0.1.0-SNAPSHOT.jar
```

### Opção 3 — Compilando e rodando os testes automatizados (JUnit 5):
```powershell
cd backend-java
mvn clean test
```
