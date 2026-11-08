
package br.com.financehub.domain.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public class LancamentoTest {

    @Test
    public void testarReceita() {
        Receita receita = new Receita(
            1,
            "Salario",
            new BigDecimal("2500.00"),
            LocalDate.now()
        );

        assertEquals(
            new BigDecimal("2500.00"),
            receita.calcularImpacto()
        );
    }

    @Test
    public void testarDespesa() {
        Despesa despesa = new Despesa(
            2,
            "Mercado",
            new BigDecimal("300.00"),
            LocalDate.now()
        );

        assertEquals(
            new BigDecimal("-300.00"),
            despesa.calcularImpacto()
        );
    }

    @Test
    public void testarPolimorfismo() {
        Lancamento lancamento = new Receita(
            1, "Salario",
            new BigDecimal("2500.00"),
            LocalDate.now()
        );

        assertEquals(
            new BigDecimal("2500.00"),
            lancamento.calcularImpacto()
        );

        lancamento = new Despesa(
            2, "Mercado",
            new BigDecimal("300.00"),
            LocalDate.now()
        );

        assertEquals(
            new BigDecimal("-300.00"),
            lancamento.calcularImpacto()
        );
    }

    @Test
    public void testarSobrescrita() {
        Lancamento lancamento = new Receita(
            1, "Salario",
            new BigDecimal("2500.00"),
            LocalDate.now()
        );

        assertEquals(
            "Receita: Salario - R$ 2500.00",
            lancamento.getResumo()
        );

        lancamento = new Despesa(
            2, "Mercado",
            new BigDecimal("300.00"),
            LocalDate.now()
        );

        assertEquals(
            "Despesa: Mercado - R$ 300.00",
            lancamento.getResumo()
        );
    }
}
