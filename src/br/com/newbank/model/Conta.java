package src.br.com.newbank.model;
public class Conta {
    private String agencia;
    private String  numero;
    private int tipo;
    private double saldo;

    public Conta(String agencia, String numero, int tipo, double saldo) {
        this.agencia = agencia;
        this.numero = numero;
        this.tipo = tipo;
        this.saldo = saldo;
    }
    public String getAgencia() {
        return agencia;
    }
    public String getNumero() {
        return numero;
    }
    public int getTipo() {
        return tipo;
    }
    public double getSaldo() {
        return saldo;
    }
    public void setAgencia(String agencia) {
        this.agencia = agencia;
    }
    public void setNumero(String numero) {
        this.numero = numero;
    }
    public void setTipo(int tipo) {
        this.tipo = tipo;
    }
    public void setSaldo(double saldo) {
        this.saldo = saldo;
    }
    public void depositar(double valor){
        if (valor > 0) {
            saldo += valor;
            System.out.println("deposito realizado");
        }else {
            System.out.println("o valor de deposito deve ser maior que zero");
        }
    }
    public boolean sacar(double valor){
        if (valor <= 0) {
            System.out.println("o valor de saque deve ser maior que zero");
            return false;
        }
        if (valor > saldo) {
            System.out.println("saldo insuficiente");
            return false;
        }
        saldo -= valor;
        System.out.println("saque realizado com sucesso");
        return true;
    }


}
