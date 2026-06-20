package util;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        try {
            // Inicia o servidor de pagamentos
            PagamentoServer.iniciar();
            System.out.println("Iniciando o Servidor de Pagamentos!");
            
            
            System.out.println("✅ Sistema iniciado com sucesso!");
            System.out.println("   Servidor: http://localhost:8080");
            
            // Mantém o servidor rodando
            Thread.currentThread().join();
            
        } catch (IOException | InterruptedException e) {
            System.err.println("❌ Erro ao iniciar o sistema: " + e.getMessage());
        }
    }
}