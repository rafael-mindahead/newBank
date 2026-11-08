
# FinanceHub - Sprint 1

## Objetivo
Implementar os fundamentos da Programação Orientada a Objetos
em Java, seguindo os conceitos ensinados em sala de aula.

## Classes implementadas

### Lancamento
Classe abstrata que representa uma movimentação financeira.

- Atributos privados
- Construtor
- Métodos getters
- Método abstrato calcularImpacto()
- Método concreto getResumo()

### Receita
Herda de Lancamento utilizando extends.

- Implementa calcularImpacto()
- Sobrescreve getResumo()
- Representa uma entrada financeira

### Despesa
Herda de Lancamento utilizando extends.

- Implementa calcularImpacto()
- Sobrescreve getResumo()
- Representa uma saída financeira

## Conceitos aplicados

1. Encapsulamento
2. Construtores
3. Classes abstratas
4. Métodos abstratos
5. Herança
6. Sobrescrita de métodos
7. Polimorfismo

## Validação

Os quatro testes JUnit executaram com sucesso.

Comando utilizado:
mvn clean test
