package util;

import dao.ProdutoDAO;

public class Main {
    public static void main(String[] args) {
        try {
            // Inicia o servidor de pagamentos
            PagamentoServer.iniciar();
            
            // Gera o site
            ProdutoDAO produtoDAO = new ProdutoDAO();
            produtoDAO.gerarSiteEstoque();
            
            System.out.println("✅ Sistema iniciado com sucesso!");
            System.out.println("   Servidor: http://localhost:8080");
            System.out.println("   Site gerado e enviado para o GitHub");
            
            // Mantém o servidor rodando
            Thread.currentThread().join();
            
        } catch (Exception e) {
            System.err.println("❌ Erro ao iniciar o sistema: " + e.getMessage());
            e.printStackTrace();
        }
    }
}