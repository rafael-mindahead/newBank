
# FinanceHub — Categoria

## Objetivo
Organizar os lançamentos financeiros em categorias,
permitindo adicionar, remover, consultar e calcular saldos.

## Conceitos de POO aplicados

### Encapsulamento
Os atributos da classe são privados.

O método getLancamentos() retorna uma cópia da lista,
protegendo a coleção interna.

### Associação
A classe Categoria possui uma coleção de objetos
do tipo Lancamento.

### Polimorfismo
O método calcularSaldo() chama calcularImpacto()
para cada lançamento.

Receitas contribuem positivamente para o saldo.
Despesas contribuem negativamente.

## Métodos principais

- adicionarLancamento(): adiciona um lançamento.
- removerLancamento(): remove um lançamento.
- getLancamentos(): retorna uma cópia da coleção.
- calcularSaldo(): calcula o saldo da categoria.

## Testes

Foram criados seis testes JUnit para Categoria,
incluindo associação, remoção, saldo e encapsulamento.

Validação local:
mvn clean test

Total do projeto:
15 testes executados com sucesso.
