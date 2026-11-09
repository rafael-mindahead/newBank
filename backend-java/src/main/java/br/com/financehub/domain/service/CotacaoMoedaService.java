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
 * a cotação atual do Dólar (USD-BRL) e converter o saldo do usuário.
 * Atende ao recurso extra "Integração com API".
 */
public class CotacaoMoedaService {

    private static final String URL_API_COTACAO = "https://economia.awesomeapi.com.br/json/last/USD-BRL";
    private BigDecimal ultimaCotacaoDolar = new BigDecimal("5.75");
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
                BigDecimal extraido = extrairValorBidDoJson(response.body());
                if (extraido != null && extraido.compareTo(BigDecimal.ZERO) > 0) {
                    this.ultimaCotacaoDolar = extraido.setScale(2, RoundingMode.HALF_UP);
                    this.obtidaDaApiOnline = true;
                    return this.ultimaCotacaoDolar;
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

    public BigDecimal getUltimaCotacaoDolar() {
        return this.ultimaCotacaoDolar;
    }

    public boolean isObtidaDaApiOnline() {
        return this.obtidaDaApiOnline;
    }

    private BigDecimal extrairValorBidDoJson(String json) {
        if (json == null) {
            return null;
        }
        String marcador = "\"bid\":\"";
        int inicio = json.indexOf(marcador);
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
