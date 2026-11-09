package br.com.financehub.app;

import br.com.financehub.api.http.FinanceHubHttpServer;
import br.com.financehub.ui.FinanceHubSwingFrame;

import javax.swing.SwingUtilities;
import java.awt.GraphicsEnvironment;
import java.nio.file.Path;

/**
 * Classe principal de inicialização do FinanceHub.
 */
public class FinanceHubApplication {

    public static void main(String[] args) {
        Path arquivoPersistencia = Path.of("data", "financehub-dados.db");

        // No computador (com tela), abre diretamente a interface gráfica Java Swing
        if (!GraphicsEnvironment.isHeadless()) {
            SwingUtilities.invokeLater(() -> {
                FinanceHubSwingFrame frame = new FinanceHubSwingFrame(arquivoPersistencia);
                frame.setVisible(true);
            });
            return;
        }

        // Em servidor sem tela (modo Headless / Docker), inicia a API HTTP
        try {
            int porta = 8080;
            String portaEnv = System.getenv("PORT");
            if (portaEnv != null && !portaEnv.isBlank()) {
                porta = Integer.parseInt(portaEnv.trim());
            }
            FinanceHubHttpServer server = new FinanceHubHttpServer(porta, arquivoPersistencia);
            server.iniciar();
            System.out.println("FinanceHub API iniciada na porta " + server.getPorta());
        } catch (Exception e) {
            System.err.println("Erro ao iniciar servidor HTTP: " + e.getMessage());
        }
    }
}
