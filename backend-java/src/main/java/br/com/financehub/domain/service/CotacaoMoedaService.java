package br.com.financehub.domain.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Serviço que consulta uma API pública externa (AwesomeAPI) para obter
 * a cotação atual do Dólar (USD-BRL) e do Euro (EUR-BRL).
 * Atende ao recurso extra "Integração com API".
 */
public class CotacaoMoedaService {

    private static final String URL_API_COTACAO = "https://economia.awesomeapi.com.br/json/last/USD-BRL,EUR-BRL";
    private BigDecimal ultimaCotacaoDolar = new BigDecimal("5.75");
    private BigDecimal ultimaCotacaoEuro = new BigDecimal("6.20");
    private boolean obtidaDaApiOnline = false;

    public BigDecimal buscarCotacaoDolarAtual() {
        try {
            HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(4))
                .build();

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(URL_API_COTACAO))
                .timeout(Duration.ofSeconds(4))
                .GET()
                .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                String body = response.body();
                BigDecimal usd = extrairBidDaMoeda(body, "USDBRL");
                BigDecimal eur = extrairBidDaMoeda(body, "EURBRL");
                if (usd != null && usd.compareTo(BigDecimal.ZERO) > 0) {
                    this.ultimaCotacaoDolar = usd.setScale(2, RoundingMode.HALF_UP);
                    this.obtidaDaApiOnline = true;
                }
                if (eur != null && eur.compareTo(BigDecimal.ZERO) > 0) {
                    this.ultimaCotacaoEuro = eur.setScale(2, RoundingMode.HALF_UP);
                }
            }
        } catch (Exception e) {
            this.obtidaDaApiOnline = false;
        }
        return this.ultimaCotacaoDolar;
    }

    public BigDecimal converterReaisParaDolar(BigDecimal valorEmReais) {
        if (valorEmReais == null || this.ultimaCotacaoDolar.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        return valorEmReais.divide(this.ultimaCotacaoDolar, 2, RoundingMode.HALF_UP);
    }

    public BigDecimal converterReaisParaEuro(BigDecimal valorEmReais) {
        if (valorEmReais == null || this.ultimaCotacaoEuro.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        return valorEmReais.divide(this.ultimaCotacaoEuro, 2, RoundingMode.HALF_UP);
    }

    public BigDecimal getUltimaCotacaoDolar() {
        return this.ultimaCotacaoDolar;
    }

    public BigDecimal getUltimaCotacaoEuro() {
        return this.ultimaCotacaoEuro;
    }

    public boolean isObtidaDaApiOnline() {
        return this.obtidaDaApiOnline;
    }

    private BigDecimal extrairBidDaMoeda(String json, String chaveMoeda) {
        if (json == null) {
            return null;
        }
        int posMoeda = json.indexOf("\"" + chaveMoeda + "\"");
        if (posMoeda < 0) {
            posMoeda = 0;
        }
        String marcador = "\"bid\":\"";
        int inicio = json.indexOf(marcador, posMoeda);
        if (inicio < 0) {
            return null;
        }
        inicio += marcador.length();
        int fim = json.indexOf("\"", inicio);
        if (fim < 0) {
            return null;
        }
        return new BigDecimal(json.substring(inicio, fim));
    }
}
