
# FinanceHub — Matriz POO Sprint 1

## Evidências da Parte A — 70%

| Critério | Evidência |
|---|---|
| Encapsulamento | Usuario, Categoria e Lancamento |
| 5 ou mais classes | Lancamento, Receita, Despesa, Usuario e Categoria |
| Classe abstrata | Lancamento |
| Método abstrato | calcularImpacto() |
| 10 atributos e métodos | Atributos e métodos das classes de domínio |
| Duas heranças | Receita e Despesa extends Lancamento |
| Sobrescrita concreta | getResumo() em Receita e Despesa |
| Polimorfismo | calcularImpacto() por referência Lancamento |
| Associação | Usuario e Categoria associados a Lancamento |
| Coleção de objetos | ArrayList<Lancamento> |

## Gerenciamento de categorias

GerenciadorCategorias implementa:
- Cadastro de categorias.
- Verificação de duplicidade por ID ou nome.
- Busca por identificador.
- Remoção de categorias.
- Consulta da coleção.

## Validação local

Comando: mvn clean test

Resultado informado: 19 testes passando,
sem falhas ou erros.
