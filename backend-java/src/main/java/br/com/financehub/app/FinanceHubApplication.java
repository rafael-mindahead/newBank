package br.com.financehub.app;

import br.com.financehub.api.http.FinanceHubHttpServer;
import br.com.financehub.ui.FinanceHubSwingFrame;

import javax.swing.SwingUtilities;
import java.awt.GraphicsEnvironment;
import java.nio.file.Path;

/**
 * Ponto de entrada principal do FinanceHub.
 * - Em ambientes locais com interface de vídeo (Desktop), inicia o Servidor HTTP e abre a Janela Java Swing.
 * - Em servidores e nuvem (modo headless sem monitor), executa exclusivamente como Servidor HTTP / API REST.
 */
public class FinanceHubApplication {

    public static void main(String[] args) throws Exception {
        int porta = 8080;
        String portaEnv = System.getenv("PORT");
        if (portaEnv != null && !portaEnv.isBlank()) {
            porta = Integer.parseInt(portaEnv.trim());
        }

        Path arquivoPersistencia = Path.of("data", "financehub-dados.db");

        // 1. Inicia o Servidor HTTP / API REST
        FinanceHubHttpServer server = new FinanceHubHttpServer(porta, arquivoPersistencia);
        server.iniciar();

        System.out.println("======================================================");
        System.out.println(" FinanceHub Backend Java 21 iniciado com sucesso!");
        System.out.println(" Porta HTTP ativa: " + server.getPorta());
        System.out.println(" API REST disponivel em:");
        System.out.println(" -> http://localhost:" + server.getPorta() + "/api/v1/health");
        System.out.println(" -> http://localhost:" + server.getPorta() + "/api/v1/relatorios");
        System.out.println("======================================================");

        // 2. Se houver ambiente gráfico de tela (Desktop), abre a janela Java Swing
        if (!GraphicsEnvironment.isHeadless()) {
            SwingUtilities.invokeLater(() -> {
                FinanceHubSwingFrame frame = new FinanceHubSwingFrame(arquivoPersistencia);
                frame.setVisible(true);
            });
        } else {
            System.out.println("Ambiente Headless (Servidor Nuvem) detectado. Interface grafica Swing desativada.");
        }
    }
}
