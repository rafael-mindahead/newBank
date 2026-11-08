
# FinanceHub — Usuario

## Objetivo
Representar o usuário de um gerenciador financeiro pessoal.

## Conceitos aplicados
- Encapsulamento com atributos privados.
- Construtor para inicialização do objeto.
- Getters e setters para acesso controlado.
- Associação com Lancamento.
- ArrayList para armazenar receitas e despesas.

## Encapsulamento da coleção
O método de consulta retorna uma cópia da lista
para impedir alterações externas na coleção interna.

## Testes
UsuarioTest valida:
- Criação do usuário.
- Alteração dos dados.
- Associação com Receita e Despesa.
- Proteção da coleção.
- Rejeição de lançamento nulo.
