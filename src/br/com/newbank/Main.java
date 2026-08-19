package src.br.com.newbank;
import src.br.com.newbank.model.Cliente;
import src.br.com.newbank.model.Conta;
public class Main {
    public static void main(String[] args) {
        Cliente cliente = new Cliente(
            "RAFAEL ALVES",
            "12344455588",
            "RUA DAS LARANJAS",
            "4199999999");

            Conta conta = new Conta(
            "1234",
            "1234-5",
            1,
            1000
        );
        cliente.setConta(conta); // juntando os dois 

        System.out.println(cliente.getConta().getSaldo());
        conta.depositar(500);
        System.out.println(cliente.getConta().getSaldo());
    }
}
